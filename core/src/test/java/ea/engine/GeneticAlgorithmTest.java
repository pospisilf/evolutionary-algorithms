package ea.engine;

import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GeneticAlgorithmTest {

    private GeneticAlgorithm<boolean[]> oneMaxGa(int chromosomeLength, int populationSize, int generations) {
        Random rng = new Random(42);
        return new GeneticAlgorithm.Builder<boolean[]>()
            .chromosomeSupplier(() -> {
                boolean[] chr = new boolean[chromosomeLength];
                for (int i = 0; i < chromosomeLength; i++) chr[i] = rng.nextBoolean();
                return chr;
            })
            .fitnessFunction(chr -> {
                int count = 0;
                for (boolean b : chr) if (b) count++;
                return (double) count / chr.length;
            })
            .selection(new TournamentSelection<>(3, new Random(42)))
            .crossover(new BooleanSinglePointCrossover(new Random(42)))
            .crossoverRate(0.8)
            .mutation(new BitFlipMutation(new Random(42)))
            .mutationRate(0.01)
            .populationSize(populationSize)
            .termination(new MaxGenerations<>(generations))
            .build();
    }

    @Test
    void callsCallbackForEachGeneration() {
        var ga = oneMaxGa(20, 50, 10);
        var results = new ArrayList<>();
        ga.run(results::add);
        assertEquals(10, results.size());
    }

    @Test
    void generationNumbersIncrement() {
        var ga = oneMaxGa(20, 50, 5);
        var gens = new ArrayList<Integer>();
        ga.run(r -> gens.add(r.generation()));
        assertEquals(java.util.List.of(1, 2, 3, 4, 5), gens);
    }

    @Test
    void fitnessImprovesSolveOneMax() {
        var ga = oneMaxGa(20, 100, 100);
        var bestFitnesses = new ArrayList<Double>();
        ga.run(r -> bestFitnesses.add(r.bestFitness()));
        double finalBest = bestFitnesses.getLast();
        assertTrue(finalBest > 0.8, "GA should solve OneMax to >80% after 100 gens, got " + finalBest);
    }
}
