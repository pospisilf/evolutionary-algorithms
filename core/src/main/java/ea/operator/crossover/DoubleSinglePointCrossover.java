package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DoubleSinglePointCrossover implements CrossoverOperator<double[]> {

    private final Random random;

    public DoubleSinglePointCrossover() {
        this(new Random());
    }

    public DoubleSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public List<double[]> crossover(double[] parent1, double[] parent2) {
        int point = random.nextInt(parent1.length);
        double[] child1 = Arrays.copyOf(parent1, parent1.length);
        double[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return List.of(child1, child2);
    }
}
