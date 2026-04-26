package ea.api;

public interface MutationOperator<T> {
    T mutate(T individual, double rate);
}
