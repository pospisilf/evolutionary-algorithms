package ea.operator;

import ea.operator.crossover.IntegerSinglePointCrossover;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IntegerSinglePointCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new IntegerSinglePointCrossover();
        int[] p1 = {1, 2, 3, 4, 5};
        int[] p2 = {6, 7, 8, 9, 10};
        List<int[]> children = crossover.crossover(p1, p2);
        assertEquals(5, children.get(0).length);
        assertEquals(5, children.get(1).length);
    }

    @Test
    void genesOnlyFromParents() {
        var crossover = new IntegerSinglePointCrossover();
        int[] p1 = {1, 1, 1, 1, 1};
        int[] p2 = {9, 9, 9, 9, 9};
        List<int[]> children = crossover.crossover(p1, p2);
        for (int gene : children.get(0)) assertTrue(gene == 1 || gene == 9);
        for (int gene : children.get(1)) assertTrue(gene == 1 || gene == 9);
    }
}
