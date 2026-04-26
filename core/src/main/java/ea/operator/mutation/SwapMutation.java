package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class SwapMutation implements MutationOperator<int[]> {

    private final Random random;

    public SwapMutation() {
        this(new Random());
    }

    public SwapMutation(Random random) {
        this.random = random;
    }

    @Override
    public int[] mutate(int[] individual, double rate) {
        int[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) {
                int j = random.nextInt(result.length);
                int tmp = result[i];
                result[i] = result[j];
                result[j] = tmp;
            }
        }
        return result;
    }
}
