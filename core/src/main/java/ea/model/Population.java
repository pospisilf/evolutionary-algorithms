package ea.model;

import java.util.Comparator;
import java.util.List;

public record Population<T>(List<Individual<T>> individuals, int generation) {

    public Individual<T> best() {
        return individuals.stream()
            .max(Comparator.comparingDouble(Individual::fitness))
            .orElseThrow(() -> new IllegalStateException("Population is empty"));
    }

    public double averageFitness() {
        return individuals.stream()
            .mapToDouble(Individual::fitness)
            .average()
            .orElse(0.0);
    }

    public double worstFitness() {
        return individuals.stream()
            .mapToDouble(Individual::fitness)
            .min()
            .orElse(0.0);
    }
}
