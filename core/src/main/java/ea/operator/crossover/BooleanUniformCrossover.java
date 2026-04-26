package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.List;
import java.util.Random;

public class BooleanUniformCrossover implements CrossoverOperator<boolean[]> {

    private final Random random;

    public BooleanUniformCrossover() {
        this(new Random());
    }

    public BooleanUniformCrossover(Random random) {
        this.random = random;
    }

    @Override
    public List<boolean[]> crossover(boolean[] parent1, boolean[] parent2) {
        boolean[] child1 = new boolean[parent1.length];
        boolean[] child2 = new boolean[parent1.length];
        for (int i = 0; i < parent1.length; i++) {
            if (random.nextBoolean()) {
                child1[i] = parent1[i];
                child2[i] = parent2[i];
            } else {
                child1[i] = parent2[i];
                child2[i] = parent1[i];
            }
        }
        return List.of(child1, child2);
    }
}
