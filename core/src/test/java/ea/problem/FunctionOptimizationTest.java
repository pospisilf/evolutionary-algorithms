package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class FunctionOptimizationTest {

    @Test
    void minimizesNegativeParabolaAtOrigin() {
        // f(x) = x^2, minimum at x=0. We minimize → fitness = -f(x) = -x^2
        // GA should find x near 0 → fitness near 0
        var problem = FunctionOptimization.minimize(
            vars -> vars[0] * vars[0],  // f(x) = x^2
            1,                           // 1 variable
            new double[]{-5.0},          // lower bound
            new double[]{5.0}            // upper bound
        );
        var best = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        problem.defaultGa().run(r -> best.set(r.bestFitness()));
        // bestFitness = -f(best_x), so best_x near 0 → bestFitness near 0
        assertTrue(best.get() > -0.1, "Should minimize x^2 near 0, got fitness=" + best.get());
    }

    @Test
    void evaluateReturnsNegatedFunctionValue() {
        var problem = FunctionOptimization.minimize(
            vars -> vars[0] + vars[1],
            2,
            new double[]{-10, -10},
            new double[]{10, 10}
        );
        assertEquals(-3.0, problem.evaluate(new double[]{1.0, 2.0}), 1e-9);
    }
}
