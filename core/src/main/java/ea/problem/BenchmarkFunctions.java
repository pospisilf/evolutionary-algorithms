package ea.problem;

import java.util.Arrays;

public final class BenchmarkFunctions {

    private BenchmarkFunctions() {}

    public static FunctionOptimization sphere(int d) {
        return FunctionOptimization.minimize(
            x -> { double s = 0; for (double v : x) s += v * v; return s; },
            d, fill(d, -5.12), fill(d, 5.12)
        );
    }

    public static FunctionOptimization griewank(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double sum = 0, prod = 1;
                for (int i = 0; i < d; i++) {
                    sum += x[i] * x[i] / 4000.0;
                    prod *= Math.cos(x[i] / Math.sqrt(i + 1));
                }
                return 1 + sum - prod;
            },
            d, fill(d, -600), fill(d, 600)
        );
    }

    public static FunctionOptimization rastrigin(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 10.0 * d;
                for (double v : x) s += v * v - 10 * Math.cos(2 * Math.PI * v);
                return s;
            },
            d, fill(d, -5.12), fill(d, 5.12)
        );
    }

    public static FunctionOptimization ackley(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double sumSq = 0, sumCos = 0;
                for (double v : x) { sumSq += v * v; sumCos += Math.cos(2 * Math.PI * v); }
                return -20 * Math.exp(-0.2 * Math.sqrt(sumSq / d))
                     - Math.exp(sumCos / d)
                     + 20 + Math.E;
            },
            d, fill(d, -32.768), fill(d, 32.768)
        );
    }

    private static double[] fill(int d, double v) {
        double[] a = new double[d]; Arrays.fill(a, v); return a;
    }
}
