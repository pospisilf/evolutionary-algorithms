package ea.operator;

import ea.model.GenerationResult;
import ea.model.Individual;
import ea.model.Population;
import ea.operator.termination.FitnessThreshold;
import ea.operator.termination.MaxGenerations;
import ea.operator.termination.NoImprovement;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TerminationConditionTest {

    private GenerationResult<boolean[]> resultAt(int gen, double bestFitness) {
        var ind = Individual.of(new boolean[]{}, bestFitness);
        var pop = new Population<>(List.of(ind), gen);
        return new GenerationResult<>(gen, bestFitness, bestFitness, bestFitness, ind, pop);
    }

    @Test
    void maxGenerationsStopsAtLimit() {
        var cond = new MaxGenerations<boolean[]>(10);
        assertFalse(cond.shouldStop(resultAt(9, 0.5)));
        assertTrue(cond.shouldStop(resultAt(10, 0.5)));
    }

    @Test
    void fitnessThresholdStopsWhenReached() {
        var cond = new FitnessThreshold<boolean[]>(0.9);
        assertFalse(cond.shouldStop(resultAt(5, 0.89)));
        assertTrue(cond.shouldStop(resultAt(5, 0.9)));
    }

    @Test
    void noImprovementStopsAfterPatience() {
        var cond = new NoImprovement<boolean[]>(3);
        assertFalse(cond.shouldStop(resultAt(1, 0.5)));
        assertFalse(cond.shouldStop(resultAt(2, 0.6))); // improvement resets counter
        assertFalse(cond.shouldStop(resultAt(3, 0.6))); // no improvement: count=1
        assertFalse(cond.shouldStop(resultAt(4, 0.6))); // count=2
        assertTrue(cond.shouldStop(resultAt(5, 0.6)));  // count=3 → stop
    }
}
