package ea.operator;

import ea.operator.mutation.SwapMutation;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class SwapMutationTest {

    @Test
    void outputHasSameLength() {
        int[] individual = {1, 2, 3, 4, 5};
        assertEquals(5, new SwapMutation().mutate(individual, 0.5).length);
    }

    @Test
    void rateZeroProducesNoChange() {
        int[] individual = {1, 2, 3, 4, 5};
        assertArrayEquals(individual, new SwapMutation().mutate(individual, 0.0));
    }

    @Test
    void containsSameElements() {
        int[] individual = {1, 2, 3, 4, 5};
        int[] result = new SwapMutation().mutate(individual, 1.0);
        int[] sortedOriginal = Arrays.copyOf(individual, individual.length);
        int[] sortedResult = Arrays.copyOf(result, result.length);
        Arrays.sort(sortedOriginal);
        Arrays.sort(sortedResult);
        assertArrayEquals(sortedOriginal, sortedResult);
    }
}
