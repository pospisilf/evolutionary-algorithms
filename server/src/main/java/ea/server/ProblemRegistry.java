package ea.server;

import ea.api.FitnessFunction;
import ea.problem.*;

public class ProblemRegistry {

    public FitnessFunction<?> resolve(String name, int size, int[][] extraData) {
        return switch (name) {
            case "onemax" -> new OneMax(size);
            case "nqueens" -> new NQueens(size);
            case "tsp" -> {
                double[][] cities = new double[extraData.length][2];
                for (int i = 0; i < extraData.length; i++) {
                    cities[i][0] = extraData[i][0];
                    cities[i][1] = extraData[i][1];
                }
                yield extraData.length > 0 ? new TSP(cities) : new TSP(defaultCities(size));
            }
            case "function" -> FunctionOptimization.minimize(
                vars -> vars[0] * vars[0] + vars[1] * vars[1],
                2, new double[]{-5, -5}, new double[]{5, 5}
            );
            default -> throw new IllegalArgumentException("Unknown problem: " + name);
        };
    }

    private double[][] defaultCities(int count) {
        double[][] cities = new double[count][2];
        var rng = new java.util.Random(42);
        for (double[] city : cities) { city[0] = rng.nextDouble() * 100; city[1] = rng.nextDouble() * 100; }
        return cities;
    }
}
