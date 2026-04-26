package ea.problem;

import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class OneMaxTest {

    @Test
    void evaluateCountsOnes() {
        var problem = new OneMax(10);
        assertEquals(0.0, problem.evaluate(new boolean[]{false, false, false, false, false, false, false, false, false, false}));
        assertEquals(1.0, problem.evaluate(new boolean[]{true, true, true, true, true, true, true, true, true, true}));
        assertEquals(0.5, problem.evaluate(new boolean[]{true, false, true, false, true, false, true, false, true, false}));
    }

    @Test
    void defaultGaSolvesToNearOptimal() {
        var problem = new OneMax(20);
        var ga = problem.defaultGa();
        var best = new AtomicReference<>(0.0);
        ga.run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 0.9, "OneMax(20) should solve to >90%, got " + best.get());
    }
}
