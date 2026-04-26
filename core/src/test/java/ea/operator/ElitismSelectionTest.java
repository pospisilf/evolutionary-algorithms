package ea.operator;

import ea.model.Individual;
import ea.model.Population;
import ea.operator.selection.ElitismSelection;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ElitismSelectionTest {

    @Test
    void returnsTopNByFitness() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.3),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.6)
        ), 0);
        var selected = new ElitismSelection<boolean[]>().select(population, 2);
        assertEquals(2, selected.size());
        assertTrue(selected.stream().allMatch(i -> i.fitness() >= 0.6));
    }
}
