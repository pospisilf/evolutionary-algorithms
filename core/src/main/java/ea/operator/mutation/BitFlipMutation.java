package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class BitFlipMutation implements MutationOperator<boolean[]> {

    private final Random random;

    public BitFlipMutation() {
        this(new Random());
    }

    public BitFlipMutation(Random random) {
        this.random = random;
    }

    @Override
    public boolean[] mutate(boolean[] individual, double rate) {
        boolean[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) result[i] = !result[i];
        }
        return result;
    }
}
