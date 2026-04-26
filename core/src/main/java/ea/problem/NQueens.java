package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.IntegerSinglePointCrossover;
import ea.operator.mutation.SwapMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;

public class NQueens implements FitnessFunction<int[]> {

    private final int n;

    public NQueens(int n) {
        this.n = n;
    }

    @Override
    public double evaluate(int[] individual) {
        int totalPairs = n * (n - 1) / 2;
        int conflicts = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (individual[i] == individual[j]) conflicts++;
                else if (Math.abs(individual[i] - individual[j]) == Math.abs(i - j)) conflicts++;
            }
        }
        return (double) (totalPairs - conflicts) / totalPairs;
    }

    public GeneticAlgorithm<int[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<int[]>()
            .chromosomeSupplier(() -> {
                int[] chr = new int[n];
                for (int i = 0; i < n; i++) chr[i] = rng.nextInt(n);
                return chr;
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
