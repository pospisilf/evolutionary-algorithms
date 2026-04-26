package ea.operator;

import ea.model.Individual;
import ea.model.Population;
import ea.operator.selection.TournamentSelection;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class TournamentSelectionTest {

    private Population<boolean[]> population() {
        return new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.8),
            Individual.of(new boolean[]{}, 0.7),
            Individual.of(new boolean[]{}, 0.2),
            Individual.of(new boolean[]{}, 0.1)
        ), 0);
    }

    @Test
    void returnsRequestedCount() {
        var selected = new TournamentSelection<boolean[]>(3).select(population(), 4);
        assertEquals(4, selected.size());
    }

    @Test
    void biasesHighFitness() {
        var selection = new TournamentSelection<boolean[]>(4, new Random(42));
        double avg = selection.select(population(), 20).stream()
            .mapToDouble(Individual::fitness).average().orElse(0);
        assertTrue(avg > 0.5, "Tournament should favour higher fitness, avg=" + avg);
    }
}
