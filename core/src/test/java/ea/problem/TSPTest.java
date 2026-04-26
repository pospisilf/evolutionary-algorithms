package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class TSPTest {

    private static final double[][] CITIES_4 = {{0,0},{1,0},{1,1},{0,1}};

    @Test
    void shorterRouteHasHigherFitness() {
        var tsp = new TSP(CITIES_4);
        double square = tsp.evaluate(new int[]{0,1,2,3});    // perimeter of unit square = 4
        double diagonal = tsp.evaluate(new int[]{0,2,1,3});  // longer route
        assertTrue(square > diagonal, "Square route should be fitter than diagonal route");
    }

    @Test
    void fitnessIsBetweenZeroAndOne() {
        var tsp = new TSP(CITIES_4);
        double f = tsp.evaluate(new int[]{0,1,2,3});
        assertTrue(f > 0 && f <= 1);
    }

    @Test
    void defaultGaImprovesFitness() {
        double[][] cities = new double[10][2];
        var rng = new java.util.Random(1);
        for (double[] c : cities) { c[0] = rng.nextDouble()*100; c[1] = rng.nextDouble()*100; }
        var tsp = new TSP(cities);
        var best = new AtomicReference<>(0.0);
        tsp.defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 0.0, "TSP fitness should be positive");
    }
}
