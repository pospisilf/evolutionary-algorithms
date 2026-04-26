package ea.operator;

import ea.operator.crossover.BooleanUniformCrossover;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BooleanUniformCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new BooleanUniformCrossover();
        boolean[] p1 = new boolean[10];
        boolean[] p2 = new boolean[10];
        List<boolean[]> children = crossover.crossover(p1, p2);
        assertEquals(10, children.get(0).length);
        assertEquals(10, children.get(1).length);
    }
}
