package ea.server;

import org.junit.jupiter.api.Test;
import java.util.List;
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
    void registryResolvesAllBenchmarkFunctions() {
        var registry = new ProblemRegistry();
        for (String name : List.of("sphere", "ackley", "griewank", "rastrigin", "schwefel",
                                    "rosenbrock", "trid", "styblinski", "levy", "michalewicz",
                                    "bukin", "carrom")) {
            assertNotNull(registry.resolve(name, 2, new int[0][]),
                "Expected non-null for: " + name);
        }
    }

    @Test
    void registryThrowsOnUnknownProblem() {
        var registry = new ProblemRegistry();
        assertThrows(IllegalArgumentException.class,
            () -> registry.resolve("unknown", 10, new int[0][]));
    }
}
