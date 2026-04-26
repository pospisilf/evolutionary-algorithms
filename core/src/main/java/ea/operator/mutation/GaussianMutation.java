package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class GaussianMutation implements MutationOperator<double[]> {

    private final Random random;
    private final double stdDev;

    public GaussianMutation(double stdDev) {
        this(stdDev, new Random());
    }

    public GaussianMutation(double stdDev, Random random) {
        this.stdDev = stdDev;
        this.random = random;
    }

    @Override
    public double[] mutate(double[] individual, double rate) {
        double[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) {
                result[i] += random.nextGaussian() * stdDev;
            }
        }
        return result;
    }
}
