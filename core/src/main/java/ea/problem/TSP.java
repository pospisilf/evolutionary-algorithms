package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.IntegerSinglePointCrossover;
import ea.operator.mutation.SwapMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;
import java.util.stream.IntStream;

public class TSP implements FitnessFunction<int[]> {

    private final double[][] cities;

    public TSP(double[][] cities) {
        this.cities = cities;
    }

    @Override
    public double evaluate(int[] route) {
        double distance = 0;
        for (int i = 0; i < route.length; i++) {
            int from = route[i];
            int to = route[(i + 1) % route.length];
            double dx = cities[from][0] - cities[to][0];
            double dy = cities[from][1] - cities[to][1];
            distance += Math.sqrt(dx * dx + dy * dy);
        }
        return 1.0 / (1.0 + distance);
    }

    public GeneticAlgorithm<int[]> defaultGa() {
        int n = cities.length;
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<int[]>()
            .chromosomeSupplier(() -> {
                int[] order = IntStream.range(0, n).toArray();
                for (int i = n - 1; i > 0; i--) {
                    int j = rng.nextInt(i + 1);
                    int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
                }
                return order;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(5))
            .crossover(new IntegerSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new SwapMutation())
            .mutationRate(0.05)
            .populationSize(200)
            .termination(new MaxGenerations<>(500))
            .build();
    }
}
