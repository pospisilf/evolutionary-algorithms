package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BooleanSinglePointCrossover implements CrossoverOperator<boolean[]> {

    private final Random random;

    public BooleanSinglePointCrossover() {
        this(new Random());
    }

    public BooleanSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public List<boolean[]> crossover(boolean[] parent1, boolean[] parent2) {
        int point = random.nextInt(parent1.length);
        boolean[] child1 = Arrays.copyOf(parent1, parent1.length);
        boolean[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return List.of(child1, child2);
    }
}
