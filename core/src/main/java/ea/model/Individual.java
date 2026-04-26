package ea.model;

public record Individual<T>(T chromosome, double fitness) {
    public static <T> Individual<T> of(T chromosome, double fitness) {
        return new Individual<>(chromosome, fitness);
    }
}
