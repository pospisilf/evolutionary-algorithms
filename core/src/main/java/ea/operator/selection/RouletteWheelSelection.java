package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouletteWheelSelection<T> implements SelectionStrategy<T> {

    private final Random random;

    public RouletteWheelSelection() {
        this(new Random());
    }

    public RouletteWheelSelection(Random random) {
        this.random = random;
    }

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        List<Individual<T>> individuals = population.individuals();
        double total = individuals.stream().mapToDouble(Individual::fitness).sum();
        List<Individual<T>> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(spin(individuals, total));
        }
        return result;
    }

    private Individual<T> spin(List<Individual<T>> individuals, double total) {
        double point = random.nextDouble() * total;
        double cumulative = 0;
        for (Individual<T> ind : individuals) {
            cumulative += ind.fitness();
            if (cumulative >= point) return ind;
        }
        return individuals.getLast();
    }
}
