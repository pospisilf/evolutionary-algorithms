package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IntegerSinglePointCrossover implements CrossoverOperator<int[]> {

    private final Random random;

    public IntegerSinglePointCrossover() {
        this(new Random());
    }

    public IntegerSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public List<int[]> crossover(int[] parent1, int[] parent2) {
        int point = random.nextInt(parent1.length);
        int[] child1 = Arrays.copyOf(parent1, parent1.length);
        int[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return List.of(child1, child2);
    }
}
