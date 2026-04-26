package ea.operator;

import ea.model.Individual;
import ea.model.Population;
import ea.operator.selection.RouletteWheelSelection;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RouletteWheelSelectionTest {

    @Test
    void returnsRequestedCount() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.6),
            Individual.of(new boolean[]{}, 0.4)
        ), 0);
        assertEquals(5, new RouletteWheelSelection<boolean[]>().select(population, 5).size());
    }

    @Test
    void biasesTowardHighFitness() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.1)
        ), 0);
        var selected = new RouletteWheelSelection<boolean[]>(new Random(42)).select(population, 100);
        long highCount = selected.stream().filter(i -> i.fitness() == 0.9).count();
        assertTrue(highCount > 60, "Roulette should heavily favour fitness=0.9, highCount=" + highCount);
    }
}
