package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.DoubleSinglePointCrossover;
import ea.operator.mutation.GaussianMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;
import java.util.function.ToDoubleFunction;

public class FunctionOptimization implements FitnessFunction<double[]> {

    private final ToDoubleFunction<double[]> function;
    private final int dimensions;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    private FunctionOptimization(ToDoubleFunction<double[]> function, int dimensions,
                                  double[] lowerBounds, double[] upperBounds) {
        this.function = function;
        this.dimensions = dimensions;
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    public static FunctionOptimization minimize(ToDoubleFunction<double[]> f, int dimensions,
                                                 double[] lowerBounds, double[] upperBounds) {
        return new FunctionOptimization(vars -> -f.applyAsDouble(vars), dimensions, lowerBounds, upperBounds);
    }

    public int dimensions() { return dimensions; }
    public double[] lowerBounds() { return lowerBounds.clone(); }
    public double[] upperBounds() { return upperBounds.clone(); }

    @Override
    public double evaluate(double[] individual) {
        return function.applyAsDouble(individual);
    }

    public GeneticAlgorithm<double[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<double[]>()
            .chromosomeSupplier(() -> {
                double[] chr = new double[dimensions];
                for (int i = 0; i < dimensions; i++) {
                    chr[i] = lowerBounds[i] + rng.nextDouble() * (upperBounds[i] - lowerBounds[i]);
                }
                return chr;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(5))
            .crossover(new DoubleSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new GaussianMutation(0.5))
            .mutationRate(0.2)
            .populationSize(100)
            .termination(new MaxGenerations<>(300))
            .build();
    }
}
