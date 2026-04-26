package ea.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PopulationTest {

    @Test
    void bestReturnsHighestFitnessIndividual() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.3),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.5)
        ), 0);
        assertEquals(0.9, population.best().fitness());
    }

    @Test
    void averageFitnessIsCorrect() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.4),
            Individual.of(new boolean[]{}, 0.6)
        ), 0);
        assertEquals(0.5, population.averageFitness(), 1e-9);
    }

    @Test
    void worstFitnessIsCorrect() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.4),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.1)
        ), 0);
        assertEquals(0.1, population.worstFitness(), 1e-9);
    }
}
