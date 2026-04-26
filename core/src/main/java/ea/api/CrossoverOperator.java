package ea.api;

public interface CrossoverOperator<T> {
    T[] crossover(T parent1, T parent2);
}
