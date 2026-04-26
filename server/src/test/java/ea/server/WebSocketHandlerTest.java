package ea.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketHandlerTest {

    @Test
    void registryResolvesKnownProblems() {
        var registry = new ProblemRegistry();
        assertNotNull(registry.resolve("onemax", 20, new int[0][]));
        assertNotNull(registry.resolve("nqueens", 8, new int[0][]));
        assertNotNull(registry.resolve("tsp", 10, new int[0][]));
        assertNotNull(registry.resolve("function", 2, new int[0][]));
    }

    @Test
    void registryThrowsOnUnknownProblem() {
        var registry = new ProblemRegistry();
        assertThrows(IllegalArgumentException.class,
            () -> registry.resolve("unknown", 10, new int[0][]));
    }
}
