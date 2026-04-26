package ea.engine;

import ea.api.*;
import ea.model.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneticAlgorithm<T> {

    private final FitnessFunction<T> fitnessFunction;
    private final SelectionStrategy<T> selection;
    private final CrossoverOperator<T> crossover;
    private final MutationOperator<T> mutation;
    private final TerminationCondition<T> termination;
    private final Supplier<T> chromosomeSupplier;
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final Random random;

    private GeneticAlgorithm(Builder<T> builder) {
        this.fitnessFunction = builder.fitnessFunction;
        this.selection = builder.selection;
        this.crossover = builder.crossover;
        this.mutation = builder.mutation;
        this.termination = builder.termination;
        this.chromosomeSupplier = builder.chromosomeSupplier;
        this.populationSize = builder.populationSize;
        this.crossoverRate = builder.crossoverRate;
        this.mutationRate = builder.mutationRate;
        this.random = builder.random;
    }

    public void run(Consumer<GenerationResult<T>> onGeneration) {
        Population<T> population = evaluate(initialize());
        while (!termination.shouldStop(GenerationResult.of(population))) {
            population = evaluate(breed(population));
            onGeneration.accept(GenerationResult.of(population));
        }
    }

    private Population<T> initialize() {
        List<Individual<T>> individuals = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            individuals.add(Individual.of(chromosomeSupplier.get(), 0.0));
        }
        return new Population<>(individuals, 0);
    }

    private Population<T> evaluate(Population<T> population) {
        List<Individual<T>> evaluated = population.individuals().stream()
            .map(ind -> Individual.of(ind.chromosome(), fitnessFunction.evaluate(ind.chromosome())))
            .toList();
        return new Population<>(evaluated, population.generation());
    }

    private Population<T> breed(Population<T> population) {
        List<Individual<T>> offspring = new ArrayList<>(populationSize);
        while (offspring.size() < populationSize) {
            List<Individual<T>> parents = selection.select(population, 2);
            T p1 = parents.get(0).chromosome();
            T p2 = parents.get(1).chromosome();

            T c1, c2;
            if (random.nextDouble() < crossoverRate) {
                List<T> children = crossover.crossover(p1, p2);
                c1 = children.get(0);
                c2 = children.get(1);
            } else {
                c1 = p1;
                c2 = p2;
            }

            offspring.add(Individual.of(mutation.mutate(c1, mutationRate), 0.0));
            if (offspring.size() < populationSize) {
                offspring.add(Individual.of(mutation.mutate(c2, mutationRate), 0.0));
            }
        }
        return new Population<>(Collections.unmodifiableList(offspring), population.generation() + 1);
    }

    public static class Builder<T> {
        private FitnessFunction<T> fitnessFunction;
        private SelectionStrategy<T> selection;
        private CrossoverOperator<T> crossover;
        private MutationOperator<T> mutation;
        private TerminationCondition<T> termination;
        private Supplier<T> chromosomeSupplier;
        private int populationSize = 100;
        private double crossoverRate = 0.8;
        private double mutationRate = 0.01;
        private Random random = new Random();

        public Builder<T> fitnessFunction(FitnessFunction<T> fn) { this.fitnessFunction = fn; return this; }
        public Builder<T> selection(SelectionStrategy<T> s) { this.selection = s; return this; }
        public Builder<T> crossover(CrossoverOperator<T> c) { this.crossover = c; return this; }
        public Builder<T> crossoverRate(double r) { this.crossoverRate = r; return this; }
        public Builder<T> mutation(MutationOperator<T> m) { this.mutation = m; return this; }
        public Builder<T> mutationRate(double r) { this.mutationRate = r; return this; }
        public Builder<T> populationSize(int n) { this.populationSize = n; return this; }
        public Builder<T> termination(TerminationCondition<T> t) { this.termination = t; return this; }
        public Builder<T> chromosomeSupplier(Supplier<T> s) { this.chromosomeSupplier = s; return this; }
        public Builder<T> seed(long seed) { this.random = new Random(seed); return this; }

        public GeneticAlgorithm<T> build() {
            Objects.requireNonNull(fitnessFunction, "fitnessFunction required");
            Objects.requireNonNull(selection, "selection required");
            Objects.requireNonNull(crossover, "crossover required");
            Objects.requireNonNull(mutation, "mutation required");
            Objects.requireNonNull(termination, "termination required");
            Objects.requireNonNull(chromosomeSupplier, "chromosomeSupplier required");
            return new GeneticAlgorithm<>(this);
        }
    }
}
