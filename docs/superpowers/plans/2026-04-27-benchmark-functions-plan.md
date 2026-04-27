# Benchmark Functions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 12 standard benchmark functions to the `core` library as a `BenchmarkFunctions` factory class, expose each via the WebSocket server by name, and cover them with formula-correctness and GA convergence tests.

**Architecture:** A single non-instantiable `BenchmarkFunctions` class in `ea.problem` provides static factory methods returning `FunctionOptimization` instances. All functions use `FunctionOptimization.minimize(f, dimensions, lowerBounds, upperBounds)` which negates `f` internally so the GA maximises `-f`. `ProblemRegistry` gets 12 new `case` entries mapping string names to factory calls, using the existing `size` parameter for dimensions.

**Tech Stack:** Java 21, JUnit 5, Maven (`mvn -pl core test` to run core tests, `mvn test` for all)

---

## File Map

| Action | Path | Responsibility |
|---|---|---|
| Create | `core/src/main/java/ea/problem/BenchmarkFunctions.java` | All 12 factory methods + private `fill()` helper |
| Create | `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java` | Formula-correctness tests + GA convergence tests |
| Modify | `server/src/main/java/ea/server/ProblemRegistry.java` | 12 new `case` entries in `resolve()` |

---

## Key concept: evaluate() vs f(x)

`FunctionOptimization.minimize(f, ...)` stores `-f` internally. So:
- `problem.evaluate(x)` returns `-f(x)`, not `f(x)`
- The GA maximises `evaluate`, which is equivalent to minimising `f`
- Tests that call `evaluate(x*)` at the global minimiser `x*` must assert `-f(x*)`, not `f(x*)`

Example: `sphere.evaluate(new double[]{0,0,0})` returns `-(0² + 0² + 0²)` = `0.0`.  
Example: `trid.evaluate(new double[]{2,2})` returns `-(−2)` = `2.0`.

---

## Task 1: Scaffold + origin-zero functions (sphere, griewank, rastrigin, ackley)

**Files:**
- Create: `core/src/main/java/ea/problem/BenchmarkFunctions.java`
- Create: `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

All four functions have a global minimum of `f(x) = 0` at the origin, so `evaluate(zeros)` must return `0.0`.

- [ ] **Step 1: Write the failing tests**

Create `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`:

```java
package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkFunctionsTest {

    // ── sphere ────────────────────────────────────────────────────────────────
    // f(x) = Σ xᵢ², global min 0 at origin
    @Test
    void sphereAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.sphere(3).evaluate(new double[]{0, 0, 0}), 1e-9);
    }

    // ── griewank ──────────────────────────────────────────────────────────────
    // f(x) = 1 + Σ(xᵢ²/4000) − Π cos(xᵢ/√(i+1)), global min 0 at origin
    @Test
    void griewankAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.griewank(3).evaluate(new double[]{0, 0, 0}), 1e-9);
    }

    // ── rastrigin ─────────────────────────────────────────────────────────────
    // f(x) = 10d + Σ(xᵢ² − 10 cos(2πxᵢ)), global min 0 at origin
    @Test
    void rastriginAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.rastrigin(2).evaluate(new double[]{0, 0}), 1e-9);
    }

    // ── ackley ────────────────────────────────────────────────────────────────
    // f(x) = −20 exp(−0.2√(Σxᵢ²/d)) − exp(Σcos(2πxᵢ)/d) + 20 + e
    // global min 0 at origin
    @Test
    void ackleyAtOriginIsZero() {
        assertEquals(0.0, BenchmarkFunctions.ackley(2).evaluate(new double[]{0, 0}), 1e-9);
    }
}
```

- [ ] **Step 2: Run to confirm all 4 tests fail (class not found)**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: compilation error — `BenchmarkFunctions` does not exist yet.

- [ ] **Step 3: Create `BenchmarkFunctions.java` with sphere, griewank, rastrigin, ackley**

Create `core/src/main/java/ea/problem/BenchmarkFunctions.java`:

```java
package ea.problem;

import java.util.Arrays;

public final class BenchmarkFunctions {

    private BenchmarkFunctions() {}

    public static FunctionOptimization sphere(int d) {
        return FunctionOptimization.minimize(
            x -> { double s = 0; for (double v : x) s += v * v; return s; },
            d, fill(d, -5.12), fill(d, 5.12)
        );
    }

    public static FunctionOptimization griewank(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double sum = 0, prod = 1;
                for (int i = 0; i < d; i++) {
                    sum += x[i] * x[i] / 4000.0;
                    prod *= Math.cos(x[i] / Math.sqrt(i + 1));
                }
                return 1 + sum - prod;
            },
            d, fill(d, -600), fill(d, 600)
        );
    }

    public static FunctionOptimization rastrigin(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 10.0 * d;
                for (double v : x) s += v * v - 10 * Math.cos(2 * Math.PI * v);
                return s;
            },
            d, fill(d, -5.12), fill(d, 5.12)
        );
    }

    public static FunctionOptimization ackley(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double sumSq = 0, sumCos = 0;
                for (double v : x) { sumSq += v * v; sumCos += Math.cos(2 * Math.PI * v); }
                return -20 * Math.exp(-0.2 * Math.sqrt(sumSq / d))
                     - Math.exp(sumCos / d)
                     + 20 + Math.E;
            },
            d, fill(d, -32.768), fill(d, 32.768)
        );
    }

    private static double[] fill(int d, double v) {
        double[] a = new double[d]; Arrays.fill(a, v); return a;
    }
}
```

- [ ] **Step 4: Run to confirm all 4 tests pass**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/BenchmarkFunctions.java \
        core/src/test/java/ea/problem/BenchmarkFunctionsTest.java
git commit -m "feat: add BenchmarkFunctions scaffold with sphere, griewank, rastrigin, ackley"
```

---

## Task 2: rosenbrock, levy, trid

**Files:**
- Modify: `core/src/main/java/ea/problem/BenchmarkFunctions.java`
- Modify: `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

- rosenbrock and levy have `f(x*) = 0` at known points; evaluate returns `0.0`.
- trid has `f(2, 2) = −2` for `d=2`; evaluate returns `2.0`.

- [ ] **Step 1: Add failing tests** (append inside the class, before the closing `}`)

```java
    // ── rosenbrock ────────────────────────────────────────────────────────────
    // f(x) = Σ [100(x_{i+1}−xᵢ²)² + (1−xᵢ)²], global min 0 at (1,…,1)
    @Test
    void rosenbrockAtOnesIsZero() {
        assertEquals(0.0, BenchmarkFunctions.rosenbrock(2).evaluate(new double[]{1, 1}), 1e-9);
    }

    // ── levy ──────────────────────────────────────────────────────────────────
    // wᵢ = 1 + (xᵢ−1)/4; global min 0 at (1,…,1)
    @Test
    void levyAtOnesIsZero() {
        assertEquals(0.0, BenchmarkFunctions.levy(2).evaluate(new double[]{1, 1}), 1e-9);
    }

    // ── trid ──────────────────────────────────────────────────────────────────
    // f(x) = Σ(xᵢ−1)² − Σ xᵢ x_{i−1}
    // d=2 optimum at x=(2,2): f = (1²+1²) − (2×2) = 2−4 = −2 → evaluate = 2.0
    @Test
    void tridAtOptimumForD2() {
        assertEquals(2.0, BenchmarkFunctions.trid(2).evaluate(new double[]{2, 2}), 1e-9);
    }
```

- [ ] **Step 2: Run to confirm 3 new tests fail**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: 3 failures — `rosenbrock`, `levy`, `trid` not found on `BenchmarkFunctions`.

- [ ] **Step 3: Add rosenbrock, levy, trid to `BenchmarkFunctions.java`** (before the `fill` helper)

```java
    public static FunctionOptimization rosenbrock(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (int i = 0; i < d - 1; i++) {
                    double t = x[i + 1] - x[i] * x[i];
                    double u = 1 - x[i];
                    s += 100 * t * t + u * u;
                }
                return s;
            },
            d, fill(d, -5), fill(d, 10)
        );
    }

    public static FunctionOptimization levy(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double[] w = new double[d];
                for (int i = 0; i < d; i++) w[i] = 1 + (x[i] - 1) / 4.0;
                double s = Math.pow(Math.sin(Math.PI * w[0]), 2);
                for (int i = 0; i < d - 1; i++) {
                    double wi1sin = Math.sin(Math.PI * w[i + 1]);
                    s += (w[i] - 1) * (w[i] - 1) * (1 + 10 * wi1sin * wi1sin);
                }
                double wdsin = Math.sin(2 * Math.PI * w[d - 1]);
                s += (w[d - 1] - 1) * (w[d - 1] - 1) * (1 + wdsin * wdsin);
                return s;
            },
            d, fill(d, -10), fill(d, 10)
        );
    }

    public static FunctionOptimization trid(int d) {
        double bound = (double) d * d;
        double[] lo = fill(d, -bound);
        double[] hi = fill(d,  bound);
        return FunctionOptimization.minimize(
            x -> {
                double s1 = 0, s2 = 0;
                for (int i = 0; i < d; i++) s1 += (x[i] - 1) * (x[i] - 1);
                for (int i = 1; i < d; i++) s2 += x[i] * x[i - 1];
                return s1 - s2;
            },
            d, lo, hi
        );
    }
```

- [ ] **Step 4: Run to confirm all tests pass**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/BenchmarkFunctions.java \
        core/src/test/java/ea/problem/BenchmarkFunctionsTest.java
git commit -m "feat: add rosenbrock, levy, trid benchmark functions"
```

---

## Task 3: schwefel, styblinskiTang, michalewicz

**Files:**
- Modify: `core/src/main/java/ea/problem/BenchmarkFunctions.java`
- Modify: `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

These have specific (non-zero-at-origin) optima:
- schwefel: `f(420.9687, 420.9687) ≈ 0`; `evaluate ≈ 0`, tolerance `1.0`
- styblinskiTang(1): `f(−2.903534) ≈ −39.1662`; `evaluate ≈ 39.1662`, tolerance `0.001`
- michalewicz(2): `f(2.2029, π/2) ≈ −1.8013`; `evaluate ≈ 1.8013`, tolerance `0.05`

- [ ] **Step 1: Add failing tests**

```java
    // ── schwefel ──────────────────────────────────────────────────────────────
    // f(x) = 418.9829d − Σ xᵢ sin(√|xᵢ|), global min ≈ 0 at xᵢ ≈ 420.9687
    @Test
    void schwefelNearZeroAtKnownOptimum() {
        double v = BenchmarkFunctions.schwefel(2)
            .evaluate(new double[]{420.9687, 420.9687});
        assertEquals(0.0, v, 1.0);
    }

    // ── styblinskiTang ────────────────────────────────────────────────────────
    // f(x) = ½ Σ(xᵢ⁴ − 16xᵢ² + 5xᵢ), global min ≈ −39.1662 per dimension at xᵢ ≈ −2.903534
    // evaluate returns −f(x*) ≈ 39.1662
    @Test
    void styblinskiTangAtKnownOptimum() {
        double v = BenchmarkFunctions.styblinskiTang(1)
            .evaluate(new double[]{-2.903534});
        assertEquals(39.1662, v, 0.001);
    }

    // ── michalewicz ───────────────────────────────────────────────────────────
    // f(x) = −Σ sin(xᵢ) sin²⁰(i xᵢ²/π), domain [0,π], global min ≈ −1.8013 for d=2
    // evaluate returns −f(x*) ≈ 1.8013
    @Test
    void michalewiczNearOptimumForD2() {
        double v = BenchmarkFunctions.michalewicz(2)
            .evaluate(new double[]{2.2029, Math.PI / 2});
        assertEquals(1.8013, v, 0.05);
    }
```

- [ ] **Step 2: Run to confirm 3 new tests fail**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: 3 failures — methods not found on `BenchmarkFunctions`.

- [ ] **Step 3: Add schwefel, styblinskiTang, michalewicz to `BenchmarkFunctions.java`**

```java
    public static FunctionOptimization schwefel(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (double v : x) s += v * Math.sin(Math.sqrt(Math.abs(v)));
                return 418.9829 * d - s;
            },
            d, fill(d, -500), fill(d, 500)
        );
    }

    public static FunctionOptimization styblinskiTang(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (double v : x) s += v * v * v * v - 16 * v * v + 5 * v;
                return s / 2.0;
            },
            d, fill(d, -5), fill(d, 5)
        );
    }

    public static FunctionOptimization michalewicz(int d) {
        return FunctionOptimization.minimize(
            x -> {
                double s = 0;
                for (int i = 0; i < d; i++) {
                    double inner = (i + 1) * x[i] * x[i] / Math.PI;
                    s += Math.sin(x[i]) * Math.pow(Math.sin(inner), 20);
                }
                return -s;
            },
            d, fill(d, 0), fill(d, Math.PI)
        );
    }
```

- [ ] **Step 4: Run to confirm all tests pass**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: `Tests run: 10, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/BenchmarkFunctions.java \
        core/src/test/java/ea/problem/BenchmarkFunctionsTest.java
git commit -m "feat: add schwefel, styblinskiTang, michalewicz benchmark functions"
```

---

## Task 4: bukin, carromTable (2D-only)

**Files:**
- Modify: `core/src/main/java/ea/problem/BenchmarkFunctions.java`
- Modify: `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

- bukin: `f(−10, 1) = 0`; `evaluate = 0.0`
- carromTable: `f(9.6461, 9.6461) ≈ −24.1568`; `evaluate ≈ 24.1568`, tolerance `0.1`

- [ ] **Step 1: Add failing tests**

```java
    // ── bukin N.6 ─────────────────────────────────────────────────────────────
    // f(x,y) = 100√(|y − 0.01x²|) + 0.01|x+10|, global min 0 at (−10, 1)
    @Test
    void bukinAtOptimumIsZero() {
        assertEquals(0.0, BenchmarkFunctions.bukin().evaluate(new double[]{-10, 1}), 1e-9);
    }

    // ── carrom table ──────────────────────────────────────────────────────────
    // f(x1,x2) = −[cos(x1)cos(x2)exp(|1−√(x1²+x2²)/π|)]²/30
    // global min ≈ −24.1568 at (±9.6461, ±9.6461) → evaluate ≈ 24.1568
    @Test
    void carromTableNearOptimum() {
        double v = BenchmarkFunctions.carromTable()
            .evaluate(new double[]{9.6461, 9.6461});
        assertEquals(24.1568, v, 0.1);
    }
```

- [ ] **Step 2: Run to confirm 2 new tests fail**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: 2 failures — `bukin()` and `carromTable()` not found.

- [ ] **Step 3: Add bukin and carromTable to `BenchmarkFunctions.java`**

```java
    public static FunctionOptimization bukin() {
        return FunctionOptimization.minimize(
            x -> 100 * Math.sqrt(Math.abs(x[1] - 0.01 * x[0] * x[0]))
               + 0.01 * Math.abs(x[0] + 10),
            2,
            new double[]{-15, -3},
            new double[]{-5,   3}
        );
    }

    public static FunctionOptimization carromTable() {
        return FunctionOptimization.minimize(
            x -> {
                double r = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
                double e = Math.exp(Math.abs(1 - r / Math.PI));
                double c = Math.cos(x[0]) * Math.cos(x[1]) * e;
                return -(c * c) / 30.0;
            },
            2,
            new double[]{-10, -10},
            new double[]{ 10,  10}
        );
    }
```

- [ ] **Step 4: Run to confirm all 12 formula tests pass**

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: `Tests run: 12, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/BenchmarkFunctions.java \
        core/src/test/java/ea/problem/BenchmarkFunctionsTest.java
git commit -m "feat: add bukin and carromTable benchmark functions — all 12 formulas complete"
```

---

## Task 5: GA convergence tests (sphere, rosenbrock, styblinskiTang)

**Files:**
- Modify: `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

These three functions are GA-friendly with a simple GA (i.e., `defaultGa()` converges reliably). Hard multimodal functions are excluded — a flaky convergence test teaches nothing.

`defaultGa()` runs 300 generations, population 100, `GaussianMutation(0.5)`, `TournamentSelection(5)`, `DoubleSinglePointCrossover`.  
`bestFitness` from the GA = `-f(bestX)`. A value close to 0 means `f(bestX) ≈ 0` (for functions minimised to 0). For styblinskiTang the minimum is negative, so `bestFitness` is large and positive.

- [ ] **Step 1: Add 3 failing convergence tests**

```java
    // ── GA convergence: sphere ────────────────────────────────────────────────
    // GA should find x near origin → f(x) ≤ 0.1 → bestFitness ≥ −0.1
    @Test
    void sphereGaConvergesNearZero() {
        var best = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        BenchmarkFunctions.sphere(2).defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > -0.1,
            "sphere GA should reach bestFitness > −0.1, got " + best.get());
    }

    // ── GA convergence: rosenbrock ────────────────────────────────────────────
    // GA should find x near (1,1) → f(x) ≤ 0.5 → bestFitness ≥ −0.5
    @Test
    void rosenbrockGaConvergesNearZero() {
        var best = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        BenchmarkFunctions.rosenbrock(2).defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > -0.5,
            "rosenbrock GA should reach bestFitness > −0.5, got " + best.get());
    }

    // ── GA convergence: styblinskiTang ────────────────────────────────────────
    // Global min ≈ −78.332 for d=2. GA should reach bestFitness > 77.5
    // (i.e., f(bestX) < −77.5, within 0.832 of the minimum)
    @Test
    void styblinskiTangGaConvergesNearOptimum() {
        var best = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        BenchmarkFunctions.styblinskiTang(2).defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 77.5,
            "styblinskiTang GA should reach bestFitness > 77.5, got " + best.get());
    }
```

- [ ] **Step 2: Run to confirm all 15 tests pass**

All three methods already exist from Tasks 1–4, so these tests compile and pass immediately — there is no red phase here. The value is in having the assertions on record.

```bash
mvn -pl core test -Dtest=BenchmarkFunctionsTest
```

Expected: `Tests run: 15, Failures: 0, Errors: 0`

- [ ] **Step 3: Run all core tests to confirm nothing else broke**

```bash
mvn -pl core test
```

Expected: all existing tests still pass.

- [ ] **Step 4: Commit**

```bash
git add core/src/test/java/ea/problem/BenchmarkFunctionsTest.java
git commit -m "test: add GA convergence tests for sphere, rosenbrock, styblinskiTang"
```

---

## Task 6: ProblemRegistry — expose all 12 functions by name

**Files:**
- Modify: `server/src/main/java/ea/server/ProblemRegistry.java`

The existing `"function"` case stays untouched. Each new case maps a string name to a `BenchmarkFunctions` factory call, using `size` for dimensions (default `2` when `size` is 0).

- [ ] **Step 1: Read the current ProblemRegistry**

Open `server/src/main/java/ea/server/ProblemRegistry.java` and locate the `switch` statement in `resolve()`. It currently has `"onemax"`, `"nqueens"`, `"tsp"`, `"function"`.

- [ ] **Step 2: Add 12 new cases** (add before the `default` case)

```java
case "sphere"      -> BenchmarkFunctions.sphere(size > 0 ? size : 2);
case "ackley"      -> BenchmarkFunctions.ackley(size > 0 ? size : 2);
case "griewank"    -> BenchmarkFunctions.griewank(size > 0 ? size : 2);
case "rastrigin"   -> BenchmarkFunctions.rastrigin(size > 0 ? size : 2);
case "schwefel"    -> BenchmarkFunctions.schwefel(size > 0 ? size : 2);
case "rosenbrock"  -> BenchmarkFunctions.rosenbrock(size > 0 ? size : 2);
case "trid"        -> BenchmarkFunctions.trid(size > 0 ? size : 2);
case "styblinski"  -> BenchmarkFunctions.styblinskiTang(size > 0 ? size : 2);
case "levy"        -> BenchmarkFunctions.levy(size > 0 ? size : 2);
case "michalewicz" -> BenchmarkFunctions.michalewicz(size > 0 ? size : 2);
case "bukin"       -> BenchmarkFunctions.bukin();
case "carrom"      -> BenchmarkFunctions.carromTable();
```

The full `resolve()` method after the edit:

```java
public FitnessFunction<?> resolve(String name, int size, int[][] extraData) {
    return switch (name) {
        case "onemax" -> new OneMax(size);
        case "nqueens" -> new NQueens(size);
        case "tsp" -> {
            double[][] cities = new double[extraData.length][2];
            for (int i = 0; i < extraData.length; i++) {
                cities[i][0] = extraData[i][0];
                cities[i][1] = extraData[i][1];
            }
            yield extraData.length > 0 ? new TSP(cities) : new TSP(defaultCities(size));
        }
        case "function"    -> FunctionOptimization.minimize(
            vars -> vars[0] * vars[0] + vars[1] * vars[1],
            2, new double[]{-5, -5}, new double[]{5, 5}
        );
        case "sphere"      -> BenchmarkFunctions.sphere(size > 0 ? size : 2);
        case "ackley"      -> BenchmarkFunctions.ackley(size > 0 ? size : 2);
        case "griewank"    -> BenchmarkFunctions.griewank(size > 0 ? size : 2);
        case "rastrigin"   -> BenchmarkFunctions.rastrigin(size > 0 ? size : 2);
        case "schwefel"    -> BenchmarkFunctions.schwefel(size > 0 ? size : 2);
        case "rosenbrock"  -> BenchmarkFunctions.rosenbrock(size > 0 ? size : 2);
        case "trid"        -> BenchmarkFunctions.trid(size > 0 ? size : 2);
        case "styblinski"  -> BenchmarkFunctions.styblinskiTang(size > 0 ? size : 2);
        case "levy"        -> BenchmarkFunctions.levy(size > 0 ? size : 2);
        case "michalewicz" -> BenchmarkFunctions.michalewicz(size > 0 ? size : 2);
        case "bukin"       -> BenchmarkFunctions.bukin();
        case "carrom"      -> BenchmarkFunctions.carromTable();
        default -> throw new IllegalArgumentException("Unknown problem: " + name);
    };
}
```

- [ ] **Step 3: Run all tests to confirm nothing broke**

```bash
mvn test
```

Expected: all tests pass including existing `WebSocketHandlerTest`.

- [ ] **Step 4: Commit**

```bash
git add server/src/main/java/ea/server/ProblemRegistry.java
git commit -m "feat: expose all 12 benchmark functions in ProblemRegistry"
```
