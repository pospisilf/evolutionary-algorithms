package ea.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ea.engine.GeneticAlgorithm;
import ea.model.GenerationResult;
import ea.operator.crossover.*;
import ea.operator.mutation.*;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import ea.problem.*;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();
    private final ProblemRegistry registry = new ProblemRegistry();

    public void onMessage(WsContext ctx, String message) throws Exception {
        try {
            JsonNode msg = mapper.readTree(message);
            JsonNode typeNode = msg.get("type");
            if (typeNode == null) {
                ctx.send(errorJson("Missing 'type' field"));
                return;
            }
            String type = typeNode.asText();
            switch (type) {
                case "start" -> startEvolution(ctx, msg);
                case "stop" -> stopEvolution(ctx.sessionId());
                default -> ctx.send(errorJson("Unknown message type: " + type));
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            ctx.send(errorJson("Invalid JSON: " + e.getOriginalMessage()));
        }
    }

    public void onClose(WsContext ctx) {
        stopEvolution(ctx.sessionId());
    }

    private void startEvolution(WsContext ctx, JsonNode msg) {
        String sessionId = ctx.sessionId();
        stopEvolution(sessionId);

        String problem = msg.get("problem").asText();
        JsonNode params = msg.get("params");
        int populationSize = params.get("populationSize").asInt(100);
        double mutationRate = params.get("mutationRate").asDouble(0.01);
        double crossoverRate = params.get("crossoverRate").asDouble(0.8);
        int maxGenerations = params.get("maxGenerations").asInt(500);
        int size = params.has("size") ? params.get("size").asInt(20) : 20;

        try {
            registry.resolve(problem, size, new int[0][]);
        } catch (IllegalArgumentException e) {
            try { ctx.send(errorJson("Unknown problem: " + problem)); } catch (Exception ignored) {}
            return;
        }

        AtomicBoolean running = new AtomicBoolean(true);
        runningFlags.put(sessionId, running);

        Thread.ofVirtual().start(() -> {
            try {
                GeneticAlgorithm<?> ga = buildGa(problem, size, populationSize, mutationRate, crossoverRate, maxGenerations);
                ga.run(result -> {
                    if (!running.get()) throw new RuntimeException("stopped");
                    try {
                        ctx.send(generationJson(result));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                if (running.get()) try { ctx.send(doneJson("maxGenerations")); } catch (Exception ignored) {}
            } catch (RuntimeException e) {
                if (running.get()) {
                    try { ctx.send(errorJson(e.getMessage())); } catch (Exception ignored) {}
                }
            }
            runningFlags.remove(sessionId);
        });
    }

    private void stopEvolution(String sessionId) {
        AtomicBoolean flag = runningFlags.remove(sessionId);
        if (flag != null) flag.set(false);
    }

    private GeneticAlgorithm<?> buildGa(String problem, int size, int populationSize,
                                          double mutationRate, double crossoverRate, int maxGenerations) {
        Random rng = new Random();
        return switch (problem) {
            case "onemax" -> {
                var p = new OneMax(size);
                yield new GeneticAlgorithm.Builder<boolean[]>()
                    .chromosomeSupplier(() -> { boolean[] c = new boolean[size]; for (int i=0;i<size;i++) c[i]=rng.nextBoolean(); return c; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(3))
                    .crossover(new BooleanSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new BitFlipMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "nqueens" -> {
                var p = new NQueens(size);
                yield new GeneticAlgorithm.Builder<int[]>()
                    .chromosomeSupplier(() -> { int[] c = new int[size]; for (int i=0;i<size;i++) c[i]=rng.nextInt(size); return c; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new IntegerSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new SwapMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "tsp" -> {
                double[][] cities = new double[size][2]; for(double[] c: cities){c[0]=rng.nextDouble()*100;c[1]=rng.nextDouble()*100;}
                var p = new TSP(cities);
                yield new GeneticAlgorithm.Builder<int[]>()
                    .chromosomeSupplier(() -> { int[] o=java.util.stream.IntStream.range(0,size).toArray(); for(int i=size-1;i>0;i--){int j=rng.nextInt(i+1);int t=o[i];o[i]=o[j];o[j]=t;} return o; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new IntegerSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new SwapMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "function" -> {
                var p = FunctionOptimization.minimize(v -> v[0]*v[0]+v[1]*v[1], 2, new double[]{-5,-5}, new double[]{5,5});
                yield new GeneticAlgorithm.Builder<double[]>()
                    .chromosomeSupplier(() -> new double[]{rng.nextDouble()*10-5, rng.nextDouble()*10-5})
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new DoubleSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new GaussianMutation(0.5)).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "sphere", "ackley", "griewank", "rastrigin", "schwefel",
                 "rosenbrock", "trid", "styblinski", "levy", "michalewicz",
                 "bukin", "carrom" -> {
                var p = (FunctionOptimization) registry.resolve(problem, size > 0 ? size : 2, new int[0][]);
                yield buildDoubleGa(p, populationSize, mutationRate, crossoverRate, maxGenerations);
            }
            default -> throw new IllegalArgumentException("Unknown problem: " + problem);
        };
    }

    private GeneticAlgorithm<double[]> buildDoubleGa(FunctionOptimization p,
            int populationSize, double mutationRate, double crossoverRate, int maxGenerations) {
        Random rng = new Random();
        double[] lo = p.lowerBounds();
        double[] hi = p.upperBounds();
        int d = p.dimensions();
        return new GeneticAlgorithm.Builder<double[]>()
            .chromosomeSupplier(() -> {
                double[] chr = new double[d];
                for (int i = 0; i < d; i++) chr[i] = lo[i] + rng.nextDouble() * (hi[i] - lo[i]);
                return chr;
            })
            .fitnessFunction(p)
            .selection(new TournamentSelection<>(5))
            .crossover(new DoubleSinglePointCrossover())
            .crossoverRate(crossoverRate)
            .mutation(new GaussianMutation(0.5))
            .mutationRate(mutationRate)
            .populationSize(populationSize)
            .termination(new MaxGenerations<>(maxGenerations))
            .build();
    }

    private String generationJson(GenerationResult<?> result) throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "generation");
        node.put("gen", result.generation());
        node.put("bestFitness", result.bestFitness());
        node.put("avgFitness", result.avgFitness());
        node.put("worstFitness", result.worstFitness());
        node.set("bestIndividual", mapper.valueToTree(result.bestIndividual().chromosome()));
        return mapper.writeValueAsString(node);
    }

    private String doneJson(String reason) throws Exception {
        return mapper.writeValueAsString(Map.of("type", "done", "reason", reason));
    }

    private String errorJson(String message) throws Exception {
        return mapper.writeValueAsString(Map.of("type", "error", "message", message));
    }
}
