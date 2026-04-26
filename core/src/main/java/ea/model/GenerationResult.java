package ea.model;

public record GenerationResult<T>(
    int generation,
    double bestFitness,
    double avgFitness,
    double worstFitness,
    Individual<T> bestIndividual,
    Population<T> population
) {
    public static <T> GenerationResult<T> of(Population<T> population) {
        Individual<T> best = population.best();
        return new GenerationResult<>(
            population.generation(),
            best.fitness(),
            population.averageFitness(),
            population.worstFitness(),
            best,
            population
        );
    }
}
