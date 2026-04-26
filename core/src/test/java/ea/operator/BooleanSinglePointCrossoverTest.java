package ea.operator;

import ea.operator.crossover.BooleanSinglePointCrossover;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class BooleanSinglePointCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new BooleanSinglePointCrossover();
        boolean[] p1 = {true, true, true, true, true};
        boolean[] p2 = {false, false, false, false, false};
        List<boolean[]> children = crossover.crossover(p1, p2);
        assertEquals(2, children.size());
        assertEquals(5, children.get(0).length);
        assertEquals(5, children.get(1).length);
    }

    @Test
    void eachGeneComesFromOneParent() {
        var crossover = new BooleanSinglePointCrossover(new Random(0));
        boolean[] p1 = {true, true, true, true, true};
        boolean[] p2 = {false, false, false, false, false};
        List<boolean[]> children = crossover.crossover(p1, p2);
        boolean[] c = children.get(0);
        boolean seenP2 = false;
        for (boolean gene : c) {
            if (!gene) seenP2 = true;
            if (seenP2) assertFalse(gene, "After first p2 gene, all should be p2");
        }
    }
}
