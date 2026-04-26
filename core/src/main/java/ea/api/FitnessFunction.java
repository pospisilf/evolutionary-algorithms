package ea.api;

@FunctionalInterface
public interface FitnessFunction<T> {
    double evaluate(T individual);
}
