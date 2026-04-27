package ea.problem;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkFunctionsTest {

    // ── sphere ────────────────────────────────────────────────────────────────
    // f(x) = Σ xᵢ², global min 0 at origin
    @Test
    void sphereAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.sphere(3).evaluate(new double[]{0, 0, 0}), 1e-9);
    }

    // ── griewank ──────────────────────────────────────────────────────────────
    // f(x) = 1 + Σ(xᵢ²/4000) − Π cos(xᵢ/√(i+1)), global min 0 at origin
    @Test
    void griewankAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.griewank(3).evaluate(new double[]{0, 0, 0}), 1e-9);
    }

    // ── rastrigin ─────────────────────────────────────────────────────────────
    // f(x) = 10d + Σ(xᵢ² − 10 cos(2πxᵢ)), global min 0 at origin
    @Test
    void rastriginAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.rastrigin(2).evaluate(new double[]{0, 0}), 1e-9);
    }

    // ── ackley ────────────────────────────────────────────────────────────────
    // f(x) = −20 exp(−0.2√(Σxᵢ²/d)) − exp(Σcos(2πxᵢ)/d) + 20 + e
    // global min 0 at origin
    @Test
    void ackleyAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.ackley(2).evaluate(new double[]{0, 0}), 1e-9);
    }

    // ── rosenbrock ────────────────────────────────────────────────────────────
    // f(x) = Σ [100(x_{i+1}−xᵢ²)² + (1−xᵢ)²], global min 0 at (1,…,1)
    @Test
    void rosenbrockAtOnesIsZero() {
        assertEquals(0.0, BenchmarkFunctions.rosenbrock(2).evaluate(new double[]{1, 1}), 1e-9);
    }

    // ── levy ──────────────────────────────────────────────────────────────────
    // wᵢ = 1 + (xᵢ−1)/4; global min 0 at (1,…,1)
    @Test
    void levyAtOnesIsZero() {
        assertEquals(0.0, BenchmarkFunctions.levy(2).evaluate(new double[]{1, 1}), 1e-9);
    }

    // ── trid ──────────────────────────────────────────────────────────────────
    // f(x) = Σ(xᵢ−1)² − Σ xᵢ x_{i−1}
    // d=2 optimum at x=(2,2): f = (1²+1²) − (2×2) = 2−4 = −2 → evaluate = 2.0
    @Test
    void tridAtOptimumForD2() {
        assertEquals(2.0, BenchmarkFunctions.trid(2).evaluate(new double[]{2, 2}), 1e-9);
    }
}
