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

    // ── schwefel ──────────────────────────────────────────────────────────────
    // f(x) = 418.9829d − Σ xᵢ sin(√|xᵢ|), global min ≈ 0 at xᵢ ≈ 420.9687
    @Test
    void schwefelNearZeroAtKnownOptimum() {
        double v = BenchmarkFunctions.schwefel(2)
            .evaluate(new double[]{420.9687, 420.9687});
        assertEquals(0.0, v, 0.01);
    }

    // ── styblinskiTang ────────────────────────────────────────────────────────
    // f(x) = ½ Σ(xᵢ⁴ − 16xᵢ² + 5xᵢ), global min ≈ −39.1662 per dimension at xᵢ ≈ −2.903534
    // evaluate returns −f(x*) ≈ 39.1662
    @Test
    void styblinskiTangAtKnownOptimum() {
        double v = BenchmarkFunctions.styblinskiTang(1)
            .evaluate(new double[]{-2.903534});
        assertEquals(39.1662, v, 0.001);
    }

    // ── michalewicz ───────────────────────────────────────────────────────────
    // f(x) = −Σ sin(xᵢ) sin²⁰(i xᵢ²/π), domain [0,π], global min ≈ −1.8013 for d=2
    // evaluate returns −f(x*) ≈ 1.8013
    @Test
    void michalewiczNearOptimumForD2() {
        double v = BenchmarkFunctions.michalewicz(2)
            .evaluate(new double[]{2.2029, Math.PI / 2});
        assertEquals(1.8013, v, 0.05);
    }

    // ── bukin N.6 ─────────────────────────────────────────────────────────────
    // f(x,y) = 100√(|y − 0.01x²|) + 0.01|x+10|, global min 0 at (−10, 1)
    @Test
    void bukinAtOptimumIsZero() {
        assertEquals(0.0, BenchmarkFunctions.bukin().evaluate(new double[]{-10, 1}), 1e-9);
    }

    // ── carrom table ──────────────────────────────────────────────────────────
    // f(x1,x2) = −[cos(x1)cos(x2)exp(|1−√(x1²+x2²)/π|)]²/30
    // global min ≈ −24.1568 at (±9.6461, ±9.6461) → evaluate ≈ 24.1568
    @Test
    void carromTableNearOptimum() {
        double v = BenchmarkFunctions.carromTable()
            .evaluate(new double[]{9.6461, 9.6461});
        assertEquals(24.1568, v, 0.1);
    }
}
