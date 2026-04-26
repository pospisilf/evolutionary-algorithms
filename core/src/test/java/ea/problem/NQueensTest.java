package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class NQueensTest {

    @Test
    void perfectSolutionHasFitnessOne() {
        // 4-queens solution: [1, 3, 0, 2]
        var problem = new NQueens(4);
        assertEquals(1.0, problem.evaluate(new int[]{1, 3, 0, 2}), 1e-9);
    }

    @Test
    void allSameColumnHasWorstFitness() {
        var problem = new NQueens(4);
        assertTrue(problem.evaluate(new int[]{0, 0, 0, 0}) < 0.5);
    }

    @Test
    void defaultGaImprovesFitness() {
        var problem = new NQueens(8);
        var best = new AtomicReference<>(0.0);
        problem.defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 0.8, "NQueens(8) should reach >0.8 fitness, got " + best.get());
    }
}
