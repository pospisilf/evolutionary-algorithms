package ea.operator;

import ea.operator.mutation.BitFlipMutation;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class BitFlipMutationTest {

    @Test
    void outputHasSameLength() {
        boolean[] individual = {true, false, true, false};
        boolean[] result = new BitFlipMutation().mutate(individual, 0.5);
        assertEquals(4, result.length);
    }

    @Test
    void rateZeroProducesNoChange() {
        boolean[] individual = {true, true, true, true};
        boolean[] result = new BitFlipMutation().mutate(individual, 0.0);
        assertArrayEquals(individual, result);
    }

    @Test
    void rateOneFlipsAllBits() {
        boolean[] individual = {true, false, true, false};
        boolean[] result = new BitFlipMutation(new Random(0)).mutate(individual, 1.0);
        assertArrayEquals(new boolean[]{false, true, false, true}, result);
    }
}
