package ea.api;

import java.util.List;

public interface CrossoverOperator<T> {
    List<T> crossover(T parent1, T parent2);
}
