package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.Comparator;
import java.util.List;

public class ElitismSelection<T> implements SelectionStrategy<T> {

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        return population.individuals().stream()
            .sorted(Comparator.comparingDouble((Individual<T> i) -> i.fitness()).reversed())
            .limit(count)
            .toList();
    }
}
