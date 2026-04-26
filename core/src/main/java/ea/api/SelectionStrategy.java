package ea.api;

import ea.model.Individual;
import ea.model.Population;
import java.util.List;

public interface SelectionStrategy<T> {
    List<Individual<T>> select(Population<T> population, int count);
}
