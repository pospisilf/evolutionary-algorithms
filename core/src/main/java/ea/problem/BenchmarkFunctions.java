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
                for (int i = 0; i < x.length; i++) {
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
                double s = 10.0 * x.length;
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
                return -20 * Math.exp(-0.2 * Math.sqrt(sumSq / x.length))
                     - Math.exp(sumCos / x.length)
                     + 20 + Math.E;
            },
            d, fill(d, -32.768), fill(d, 32.768)
        );
    }

    public static FunctionOptimization rosenbrock(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (int i = 0; i < x.length - 1; i++) {
                    double t = x[i + 1] - x[i] * x[i];
                    double u = 1 - x[i];
                    s += 100 * t * t + u * u;
                }
                return s;
            },
            d, fill(d, -5), fill(d, 10)
        );
    }

    public static FunctionOptimization levy(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double[] w = new double[x.length];
                for (int i = 0; i < x.length; i++) w[i] = 1 + (x[i] - 1) / 4.0;
                double s = Math.pow(Math.sin(Math.PI * w[0]), 2);
                for (int i = 0; i < x.length - 1; i++) {
                    double wi1sin = Math.sin(Math.PI * w[i + 1]);
                    s += (w[i] - 1) * (w[i] - 1) * (1 + 10 * wi1sin * wi1sin);
                }
                double wdsin = Math.sin(2 * Math.PI * w[x.length - 1]);
                s += (w[x.length - 1] - 1) * (w[x.length - 1] - 1) * (1 + wdsin * wdsin);
                return s;
            },
            d, fill(d, -10), fill(d, 10)
        );
    }

    public static FunctionOptimization trid(int d) {
        double bound = (double) d * d;
        double[] lo = fill(d, -bound);
        double[] hi = fill(d,  bound);
        return FunctionOptimization.minimize(
            x -> {
                double s1 = 0, s2 = 0;
                for (int i = 0; i < x.length; i++) s1 += (x[i] - 1) * (x[i] - 1);
                for (int i = 1; i < x.length; i++) s2 += x[i] * x[i - 1];
                return s1 - s2;
            },
            d, lo, hi
        );
    }

    public static FunctionOptimization schwefel(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (double v : x) s += v * Math.sin(Math.sqrt(Math.abs(v)));
                return 418.9829 * x.length - s;
            },
            d, fill(d, -500), fill(d, 500)
        );
    }

    public static FunctionOptimization styblinskiTang(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (double v : x) s += v * v * v * v - 16 * v * v + 5 * v;
                return s / 2.0;
            },
            d, fill(d, -5), fill(d, 5)
        );
    }

    public static FunctionOptimization michalewicz(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (int i = 0; i < x.length; i++) {
                    double inner = (i + 1) * x[i] * x[i] / Math.PI;
                    s += Math.sin(x[i]) * Math.pow(Math.sin(inner), 20);
                }
                return -s;
            },
            d, fill(d, 0), fill(d, Math.PI)
        );
    }

    public static FunctionOptimization bukin() {
        return FunctionOptimization.minimize(
            x -> 100 * Math.sqrt(Math.abs(x[1] - 0.01 * x[0] * x[0]))
               + 0.01 * Math.abs(x[0] + 10),
            2,
            new double[]{-15, -3},
            new double[]{-5,   3}
        );
    }

    public static FunctionOptimization carromTable() {
        return FunctionOptimization.minimize(
            x -> {
                double r = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
                double e = Math.exp(Math.abs(1 - r / Math.PI));
                double c = Math.cos(x[0]) * Math.cos(x[1]) * e;
                return -(c * c) / 30.0;
            },
            2,
            new double[]{-10, -10},
            new double[]{ 10,  10}
        );
    }

    private static double[] fill(int d, double v) {
        double[] a = new double[d]; Arrays.fill(a, v); return a;
    }
}
