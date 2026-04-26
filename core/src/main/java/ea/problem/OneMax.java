package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;

public class OneMax implements FitnessFunction<boolean[]> {

    private final int length;

    public OneMax(int length) {
        this.length = length;
    }

    @Override
    public double evaluate(boolean[] individual) {
        int count = 0;
        for (boolean gene : individual) if (gene) count++;
        return (double) count / individual.length;
    }

    public GeneticAlgorithm<boolean[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<boolean[]>()
            .chromosomeSupplier(() -> {
                boolean[] chr = new boolean[length];
                for (int i = 0; i < length; i++) chr[i] = rng.nextBoolean();
                return chr;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(3))
            .crossover(new BooleanSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new BitFlipMutation())
            .mutationRate(1.0 / length)
            .populationSize(100)
            .termination(new MaxGenerations<>(200))
            .build();
    }
}
