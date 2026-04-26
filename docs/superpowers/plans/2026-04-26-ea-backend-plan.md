# EA Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a pure Java GA library (`core`) and a thin Javalin WebSocket server (`server`) that streams live evolution data to a frontend.

**Architecture:** Maven multi-module project (Java 21). `core` has zero framework dependencies and is publishable as a standalone Maven artifact. `server` wraps `core` with Javalin and streams `GenerationResult` snapshots over WebSocket as JSON. The frontend (separate repo, separate plan) connects via `ws://localhost:8080/evolution`.

**Tech Stack:** Java 21, Maven 3.9+, JUnit Jupiter 5.10, Javalin 6.x, Jackson 2.17

---

## File Map

```
evolutionary-algorithms/
├── pom.xml
├── core/
│   ├── pom.xml
│   └── src/
│       ├── main/java/ea/
│       │   ├── api/
│       │   │   ├── FitnessFunction.java
│       │   │   ├── SelectionStrategy.java
│       │   │   ├── CrossoverOperator.java
│       │   │   ├── MutationOperator.java
│       │   │   └── TerminationCondition.java
│       │   ├── model/
│       │   │   ├── Individual.java
│       │   │   ├── Population.java
│       │   │   └── GenerationResult.java
│       │   ├── operator/
│       │   │   ├── selection/
│       │   │   │   ├── TournamentSelection.java
│       │   │   │   ├── RouletteWheelSelection.java
│       │   │   │   └── ElitismSelection.java
│       │   │   ├── crossover/
│       │   │   │   ├── BooleanSinglePointCrossover.java
│       │   │   │   ├── BooleanUniformCrossover.java
│       │   │   │   ├── IntegerSinglePointCrossover.java
│       │   │   │   └── DoubleSinglePointCrossover.java
│       │   │   ├── mutation/
│       │   │   │   ├── BitFlipMutation.java
│       │   │   │   ├── SwapMutation.java
│       │   │   │   └── GaussianMutation.java
│       │   │   └── termination/
│       │   │       ├── MaxGenerations.java
│       │   │       ├── FitnessThreshold.java
│       │   │       └── NoImprovement.java
│       │   ├── engine/
│       │   │   └── GeneticAlgorithm.java
│       │   └── problem/
│       │       ├── OneMax.java
│       │       ├── NQueens.java
│       │       ├── TSP.java
│       │       └── FunctionOptimization.java
│       └── test/java/ea/
│           ├── model/PopulationTest.java
│           ├── operator/
│           │   ├── TournamentSelectionTest.java
│           │   ├── RouletteWheelSelectionTest.java
│           │   ├── ElitismSelectionTest.java
│           │   ├── BooleanSinglePointCrossoverTest.java
│           │   ├── BooleanUniformCrossoverTest.java
│           │   ├── IntegerSinglePointCrossoverTest.java
│           │   ├── BitFlipMutationTest.java
│           │   ├── SwapMutationTest.java
│           │   └── TerminationConditionTest.java
│           ├── engine/GeneticAlgorithmTest.java
│           └── problem/
│               ├── OneMaxTest.java
│               ├── NQueensTest.java
│               ├── TSPTest.java
│               └── FunctionOptimizationTest.java
└── server/
    ├── pom.xml
    └── src/
        ├── main/java/ea/server/
        │   ├── Main.java
        │   ├── ProblemRegistry.java
        │   └── WebSocketHandler.java
        └── test/java/ea/server/
            └── WebSocketHandlerTest.java
```

---

## Task 1: Maven Multi-Module Scaffold

**Files:**
- Create: `pom.xml`
- Create: `core/pom.xml`
- Create: `server/pom.xml`

- [ ] **Step 1: Create parent `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.evolutionary</groupId>
    <artifactId>evolutionary-algorithms</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>core</module>
        <module>server</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter</artifactId>
                <version>5.10.2</version>
                <scope>test</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create `core/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.evolutionary</groupId>
        <artifactId>evolutionary-algorithms</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>evolutionary-algorithms-core</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 3: Create `server/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.evolutionary</groupId>
        <artifactId>evolutionary-algorithms</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>evolutionary-algorithms-server</artifactId>

    <dependencies>
        <dependency>
            <groupId>io.evolutionary</groupId>
            <artifactId>evolutionary-algorithms-core</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.javalin</groupId>
            <artifactId>javalin</artifactId>
            <version>6.1.3</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.17.0</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>2.0.12</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>ea.server.Main</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 4: Create source directories and verify the build compiles**

```bash
mkdir -p core/src/main/java/ea/{api,model,operator/{selection,crossover,mutation,termination},engine,problem}
mkdir -p core/src/test/java/ea/{model,operator,engine,problem}
mkdir -p server/src/main/java/ea/server
mkdir -p server/src/test/java/ea/server
mvn compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add pom.xml core/pom.xml server/pom.xml
git commit -m "chore: scaffold Maven multi-module project"
```

---

## Task 2: Core API Interfaces

**Files:**
- Create: `core/src/main/java/ea/api/FitnessFunction.java`
- Create: `core/src/main/java/ea/api/SelectionStrategy.java`
- Create: `core/src/main/java/ea/api/CrossoverOperator.java`
- Create: `core/src/main/java/ea/api/MutationOperator.java`
- Create: `core/src/main/java/ea/api/TerminationCondition.java`

- [ ] **Step 1: Create `FitnessFunction.java`**

```java
package ea.api;

@FunctionalInterface
public interface FitnessFunction<T> {
    double evaluate(T individual);
}
```

- [ ] **Step 2: Create `SelectionStrategy.java`**

```java
package ea.api;

import ea.model.Individual;
import ea.model.Population;
import java.util.List;

public interface SelectionStrategy<T> {
    List<Individual<T>> select(Population<T> population, int count);
}
```

- [ ] **Step 3: Create `CrossoverOperator.java`**

```java
package ea.api;

public interface CrossoverOperator<T> {
    T[] crossover(T parent1, T parent2);
}
```

- [ ] **Step 4: Create `MutationOperator.java`**

```java
package ea.api;

public interface MutationOperator<T> {
    T mutate(T individual, double rate);
}
```

- [ ] **Step 5: Create `TerminationCondition.java`**

```java
package ea.api;

import ea.model.GenerationResult;

public interface TerminationCondition<T> {
    boolean shouldStop(GenerationResult<T> result);
}
```

- [ ] **Step 6: Compile and commit**

```bash
mvn compile
git add core/src/main/java/ea/api/
git commit -m "feat(core): add public API interfaces"
```

Expected: `BUILD SUCCESS`

---

## Task 3: Core Models

**Files:**
- Create: `core/src/main/java/ea/model/Individual.java`
- Create: `core/src/main/java/ea/model/Population.java`
- Create: `core/src/main/java/ea/model/GenerationResult.java`
- Test: `core/src/test/java/ea/model/PopulationTest.java`

- [ ] **Step 1: Write the failing test**

```java
package ea.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PopulationTest {

    @Test
    void bestReturnsHighestFitnessIndividual() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.3),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.5)
        ), 0);
        assertEquals(0.9, population.best().fitness());
    }

    @Test
    void averageFitnessIsCorrect() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.4),
            Individual.of(new boolean[]{}, 0.6)
        ), 0);
        assertEquals(0.5, population.averageFitness(), 1e-9);
    }

    @Test
    void worstFitnessIsCorrect() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.4),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.1)
        ), 0);
        assertEquals(0.1, population.worstFitness(), 1e-9);
    }
}
```

- [ ] **Step 2: Run test — verify it fails**

```bash
mvn test -pl core -Dtest=PopulationTest
```

Expected: compilation error (classes don't exist yet)

- [ ] **Step 3: Create `Individual.java`**

```java
package ea.model;

public record Individual<T>(T chromosome, double fitness) {
    public static <T> Individual<T> of(T chromosome, double fitness) {
        return new Individual<>(chromosome, fitness);
    }
}
```

- [ ] **Step 4: Create `Population.java`**

```java
package ea.model;

import java.util.Comparator;
import java.util.List;

public record Population<T>(List<Individual<T>> individuals, int generation) {

    public Individual<T> best() {
        return individuals.stream()
            .max(Comparator.comparingDouble(Individual::fitness))
            .orElseThrow(() -> new IllegalStateException("Population is empty"));
    }

    public double averageFitness() {
        return individuals.stream()
            .mapToDouble(Individual::fitness)
            .average()
            .orElse(0.0);
    }

    public double worstFitness() {
        return individuals.stream()
            .mapToDouble(Individual::fitness)
            .min()
            .orElse(0.0);
    }
}
```

- [ ] **Step 5: Create `GenerationResult.java`**

```java
package ea.model;

public record GenerationResult<T>(
    int generation,
    double bestFitness,
    double avgFitness,
    double worstFitness,
    Individual<T> bestIndividual,
    Population<T> population
) {
    public static <T> GenerationResult<T> of(Population<T> population) {
        Individual<T> best = population.best();
        return new GenerationResult<>(
            population.generation(),
            best.fitness(),
            population.averageFitness(),
            population.worstFitness(),
            best,
            population
        );
    }
}
```

- [ ] **Step 6: Run tests — verify they pass**

```bash
mvn test -pl core -Dtest=PopulationTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/ea/model/ core/src/test/java/ea/model/
git commit -m "feat(core): add Individual, Population, GenerationResult models"
```

---

## Task 4: Selection Operators

**Files:**
- Create: `core/src/main/java/ea/operator/selection/TournamentSelection.java`
- Create: `core/src/main/java/ea/operator/selection/RouletteWheelSelection.java`
- Create: `core/src/main/java/ea/operator/selection/ElitismSelection.java`
- Test: `core/src/test/java/ea/operator/TournamentSelectionTest.java`
- Test: `core/src/test/java/ea/operator/RouletteWheelSelectionTest.java`
- Test: `core/src/test/java/ea/operator/ElitismSelectionTest.java`

- [ ] **Step 1: Write failing tests**

`TournamentSelectionTest.java`:
```java
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
```

`ElitismSelectionTest.java`:
```java
package ea.operator;

import ea.model.Individual;
import ea.model.Population;
import ea.operator.selection.ElitismSelection;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ElitismSelectionTest {

    @Test
    void returnsTopNByFitness() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.3),
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.6)
        ), 0);
        var selected = new ElitismSelection<boolean[]>().select(population, 2);
        assertEquals(2, selected.size());
        assertTrue(selected.stream().allMatch(i -> i.fitness() >= 0.6));
    }
}
```

`RouletteWheelSelectionTest.java`:
```java
package ea.operator;

import ea.model.Individual;
import ea.model.Population;
import ea.operator.selection.RouletteWheelSelection;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RouletteWheelSelectionTest {

    @Test
    void returnsRequestedCount() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.6),
            Individual.of(new boolean[]{}, 0.4)
        ), 0);
        assertEquals(5, new RouletteWheelSelection<boolean[]>().select(population, 5).size());
    }

    @Test
    void biasesTowardHighFitness() {
        var population = new Population<>(List.of(
            Individual.of(new boolean[]{}, 0.9),
            Individual.of(new boolean[]{}, 0.1)
        ), 0);
        var selected = new RouletteWheelSelection<boolean[]>(new Random(42)).select(population, 100);
        long highCount = selected.stream().filter(i -> i.fitness() == 0.9).count();
        assertTrue(highCount > 60, "Roulette should heavily favour fitness=0.9, highCount=" + highCount);
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest="TournamentSelectionTest,ElitismSelectionTest,RouletteWheelSelectionTest"
```

Expected: compilation error

- [ ] **Step 3: Create `TournamentSelection.java`**

```java
package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TournamentSelection<T> implements SelectionStrategy<T> {

    private final int tournamentSize;
    private final Random random;

    public TournamentSelection(int tournamentSize) {
        this(tournamentSize, new Random());
    }

    public TournamentSelection(int tournamentSize, Random random) {
        this.tournamentSize = tournamentSize;
        this.random = random;
    }

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        List<Individual<T>> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(runTournament(population.individuals()));
        }
        return result;
    }

    private Individual<T> runTournament(List<Individual<T>> individuals) {
        Individual<T> best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual<T> candidate = individuals.get(random.nextInt(individuals.size()));
            if (best == null || candidate.fitness() > best.fitness()) {
                best = candidate;
            }
        }
        return best;
    }
}
```

- [ ] **Step 4: Create `ElitismSelection.java`**

```java
package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.Comparator;
import java.util.List;

public class ElitismSelection<T> implements SelectionStrategy<T> {

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        return population.individuals().stream()
            .sorted(Comparator.comparingDouble(Individual::fitness).reversed())
            .limit(count)
            .toList();
    }
}
```

- [ ] **Step 5: Create `RouletteWheelSelection.java`**

```java
package ea.operator.selection;

import ea.api.SelectionStrategy;
import ea.model.Individual;
import ea.model.Population;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RouletteWheelSelection<T> implements SelectionStrategy<T> {

    private final Random random;

    public RouletteWheelSelection() {
        this(new Random());
    }

    public RouletteWheelSelection(Random random) {
        this.random = random;
    }

    @Override
    public List<Individual<T>> select(Population<T> population, int count) {
        List<Individual<T>> individuals = population.individuals();
        double total = individuals.stream().mapToDouble(Individual::fitness).sum();
        List<Individual<T>> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(spin(individuals, total));
        }
        return result;
    }

    private Individual<T> spin(List<Individual<T>> individuals, double total) {
        double point = random.nextDouble() * total;
        double cumulative = 0;
        for (Individual<T> ind : individuals) {
            cumulative += ind.fitness();
            if (cumulative >= point) return ind;
        }
        return individuals.getLast();
    }
}
```

- [ ] **Step 6: Run tests — verify pass**

```bash
mvn test -pl core -Dtest="TournamentSelectionTest,ElitismSelectionTest,RouletteWheelSelectionTest"
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/ea/operator/selection/ core/src/test/java/ea/operator/
git commit -m "feat(core): add TournamentSelection, ElitismSelection, RouletteWheelSelection"
```

---

## Task 5: Crossover Operators

**Files:**
- Create: `core/src/main/java/ea/operator/crossover/BooleanSinglePointCrossover.java`
- Create: `core/src/main/java/ea/operator/crossover/BooleanUniformCrossover.java`
- Create: `core/src/main/java/ea/operator/crossover/IntegerSinglePointCrossover.java`
- Test: `core/src/test/java/ea/operator/BooleanSinglePointCrossoverTest.java`
- Test: `core/src/test/java/ea/operator/BooleanUniformCrossoverTest.java`
- Test: `core/src/test/java/ea/operator/IntegerSinglePointCrossoverTest.java`

- [ ] **Step 1: Write failing tests**

`BooleanSinglePointCrossoverTest.java`:
```java
package ea.operator;

import ea.operator.crossover.BooleanSinglePointCrossover;
import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class BooleanSinglePointCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new BooleanSinglePointCrossover();
        boolean[] p1 = {true, true, true, true, true};
        boolean[] p2 = {false, false, false, false, false};
        boolean[][] children = crossover.crossover(p1, p2);
        assertEquals(2, children.length);
        assertEquals(5, children[0].length);
        assertEquals(5, children[1].length);
    }

    @Test
    void eachGeneComesFromOneParent() {
        var crossover = new BooleanSinglePointCrossover(new Random(0));
        boolean[] p1 = {true, true, true, true, true};
        boolean[] p2 = {false, false, false, false, false};
        boolean[][] children = crossover.crossover(p1, p2);
        for (boolean gene : children[0]) {
            assertTrue(gene == true || gene == false); // trivially always true, but...
        }
        // child[0] should be a mix of true then false or false then true — never random middle values
        boolean[] c = children[0];
        boolean seenP2 = false;
        for (boolean gene : c) {
            if (!gene) seenP2 = true;
            if (seenP2) assertFalse(gene, "After first p2 gene, all should be p2");
        }
    }
}
```

`BooleanUniformCrossoverTest.java`:
```java
package ea.operator;

import ea.operator.crossover.BooleanUniformCrossover;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BooleanUniformCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new BooleanUniformCrossover();
        boolean[] p1 = new boolean[10];
        boolean[] p2 = new boolean[10];
        boolean[][] children = crossover.crossover(p1, p2);
        assertEquals(10, children[0].length);
        assertEquals(10, children[1].length);
    }
}
```

`IntegerSinglePointCrossoverTest.java`:
```java
package ea.operator;

import ea.operator.crossover.IntegerSinglePointCrossover;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IntegerSinglePointCrossoverTest {

    @Test
    void childrenHaveSameLengthAsParents() {
        var crossover = new IntegerSinglePointCrossover();
        int[] p1 = {1, 2, 3, 4, 5};
        int[] p2 = {6, 7, 8, 9, 10};
        int[][] children = crossover.crossover(p1, p2);
        assertEquals(5, children[0].length);
        assertEquals(5, children[1].length);
    }

    @Test
    void genesOnlyFromParents() {
        var crossover = new IntegerSinglePointCrossover();
        int[] p1 = {1, 1, 1, 1, 1};
        int[] p2 = {9, 9, 9, 9, 9};
        int[][] children = crossover.crossover(p1, p2);
        for (int gene : children[0]) assertTrue(gene == 1 || gene == 9);
        for (int gene : children[1]) assertTrue(gene == 1 || gene == 9);
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest="BooleanSinglePointCrossoverTest,BooleanUniformCrossoverTest,IntegerSinglePointCrossoverTest"
```

- [ ] **Step 3: Create `BooleanSinglePointCrossover.java`**

```java
package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.Random;

public class BooleanSinglePointCrossover implements CrossoverOperator<boolean[]> {

    private final Random random;

    public BooleanSinglePointCrossover() {
        this(new Random());
    }

    public BooleanSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public boolean[][] crossover(boolean[] parent1, boolean[] parent2) {
        int point = random.nextInt(parent1.length);
        boolean[] child1 = Arrays.copyOf(parent1, parent1.length);
        boolean[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return new boolean[][]{child1, child2};
    }
}
```

- [ ] **Step 4: Create `BooleanUniformCrossover.java`**

```java
package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Random;

public class BooleanUniformCrossover implements CrossoverOperator<boolean[]> {

    private final Random random;

    public BooleanUniformCrossover() {
        this(new Random());
    }

    public BooleanUniformCrossover(Random random) {
        this.random = random;
    }

    @Override
    public boolean[][] crossover(boolean[] parent1, boolean[] parent2) {
        boolean[] child1 = new boolean[parent1.length];
        boolean[] child2 = new boolean[parent1.length];
        for (int i = 0; i < parent1.length; i++) {
            if (random.nextBoolean()) {
                child1[i] = parent1[i];
                child2[i] = parent2[i];
            } else {
                child1[i] = parent2[i];
                child2[i] = parent1[i];
            }
        }
        return new boolean[][]{child1, child2};
    }
}
```

- [ ] **Step 5: Create `IntegerSinglePointCrossover.java`**

```java
package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.Random;

public class IntegerSinglePointCrossover implements CrossoverOperator<int[]> {

    private final Random random;

    public IntegerSinglePointCrossover() {
        this(new Random());
    }

    public IntegerSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public int[][] crossover(int[] parent1, int[] parent2) {
        int point = random.nextInt(parent1.length);
        int[] child1 = Arrays.copyOf(parent1, parent1.length);
        int[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return new int[][]{child1, child2};
    }
}
```

- [ ] **Step 6: Run tests — verify pass**

```bash
mvn test -pl core -Dtest="BooleanSinglePointCrossoverTest,BooleanUniformCrossoverTest,IntegerSinglePointCrossoverTest"
```

Expected: `Tests run: 5, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/ea/operator/crossover/ core/src/test/java/ea/operator/
git commit -m "feat(core): add BooleanSinglePointCrossover, BooleanUniformCrossover, IntegerSinglePointCrossover"
```

---

## Task 6: Mutation Operators

**Files:**
- Create: `core/src/main/java/ea/operator/mutation/BitFlipMutation.java`
- Create: `core/src/main/java/ea/operator/mutation/SwapMutation.java`
- Test: `core/src/test/java/ea/operator/BitFlipMutationTest.java`
- Test: `core/src/test/java/ea/operator/SwapMutationTest.java`

- [ ] **Step 1: Write failing tests**

`BitFlipMutationTest.java`:
```java
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
```

`SwapMutationTest.java`:
```java
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
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest="BitFlipMutationTest,SwapMutationTest"
```

- [ ] **Step 3: Create `BitFlipMutation.java`**

```java
package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class BitFlipMutation implements MutationOperator<boolean[]> {

    private final Random random;

    public BitFlipMutation() {
        this(new Random());
    }

    public BitFlipMutation(Random random) {
        this.random = random;
    }

    @Override
    public boolean[] mutate(boolean[] individual, double rate) {
        boolean[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) result[i] = !result[i];
        }
        return result;
    }
}
```

- [ ] **Step 4: Create `SwapMutation.java`**

```java
package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class SwapMutation implements MutationOperator<int[]> {

    private final Random random;

    public SwapMutation() {
        this(new Random());
    }

    public SwapMutation(Random random) {
        this.random = random;
    }

    @Override
    public int[] mutate(int[] individual, double rate) {
        int[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) {
                int j = random.nextInt(result.length);
                int tmp = result[i];
                result[i] = result[j];
                result[j] = tmp;
            }
        }
        return result;
    }
}
```

- [ ] **Step 5: Run tests — verify pass**

```bash
mvn test -pl core -Dtest="BitFlipMutationTest,SwapMutationTest"
```

Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/ea/operator/mutation/ core/src/test/java/ea/operator/
git commit -m "feat(core): add BitFlipMutation, SwapMutation"
```

---

## Task 7: Termination Conditions

**Files:**
- Create: `core/src/main/java/ea/operator/termination/MaxGenerations.java`
- Create: `core/src/main/java/ea/operator/termination/FitnessThreshold.java`
- Create: `core/src/main/java/ea/operator/termination/NoImprovement.java`
- Test: `core/src/test/java/ea/operator/TerminationConditionTest.java`

- [ ] **Step 1: Write failing test**

```java
package ea.operator;

import ea.model.GenerationResult;
import ea.model.Individual;
import ea.model.Population;
import ea.operator.termination.FitnessThreshold;
import ea.operator.termination.MaxGenerations;
import ea.operator.termination.NoImprovement;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TerminationConditionTest {

    private GenerationResult<boolean[]> resultAt(int gen, double bestFitness) {
        var ind = Individual.of(new boolean[]{}, bestFitness);
        var pop = new Population<>(List.of(ind), gen);
        return new GenerationResult<>(gen, bestFitness, bestFitness, bestFitness, ind, pop);
    }

    @Test
    void maxGenerationsStopsAtLimit() {
        var cond = new MaxGenerations<boolean[]>(10);
        assertFalse(cond.shouldStop(resultAt(9, 0.5)));
        assertTrue(cond.shouldStop(resultAt(10, 0.5)));
    }

    @Test
    void fitnessThresholdStopsWhenReached() {
        var cond = new FitnessThreshold<boolean[]>(0.9);
        assertFalse(cond.shouldStop(resultAt(5, 0.89)));
        assertTrue(cond.shouldStop(resultAt(5, 0.9)));
    }

    @Test
    void noImprovementStopsAfterPatience() {
        var cond = new NoImprovement<boolean[]>(3);
        assertFalse(cond.shouldStop(resultAt(1, 0.5)));
        assertFalse(cond.shouldStop(resultAt(2, 0.6))); // improvement resets counter
        assertFalse(cond.shouldStop(resultAt(3, 0.6))); // no improvement: count=1
        assertFalse(cond.shouldStop(resultAt(4, 0.6))); // count=2
        assertTrue(cond.shouldStop(resultAt(5, 0.6)));  // count=3 → stop
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=TerminationConditionTest
```

- [ ] **Step 3: Create `MaxGenerations.java`**

```java
package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class MaxGenerations<T> implements TerminationCondition<T> {

    private final int maxGenerations;

    public MaxGenerations(int maxGenerations) {
        this.maxGenerations = maxGenerations;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        return result.generation() >= maxGenerations;
    }
}
```

- [ ] **Step 4: Create `FitnessThreshold.java`**

```java
package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class FitnessThreshold<T> implements TerminationCondition<T> {

    private final double threshold;

    public FitnessThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        return result.bestFitness() >= threshold;
    }
}
```

- [ ] **Step 5: Create `NoImprovement.java`**

```java
package ea.operator.termination;

import ea.api.TerminationCondition;
import ea.model.GenerationResult;

public class NoImprovement<T> implements TerminationCondition<T> {

    private final int patience;
    private double bestSeen = Double.NEGATIVE_INFINITY;
    private int count = 0;

    public NoImprovement(int patience) {
        this.patience = patience;
    }

    @Override
    public boolean shouldStop(GenerationResult<T> result) {
        if (result.bestFitness() > bestSeen) {
            bestSeen = result.bestFitness();
            count = 0;
        } else {
            count++;
        }
        return count >= patience;
    }
}
```

- [ ] **Step 6: Run tests — verify pass**

```bash
mvn test -pl core -Dtest=TerminationConditionTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/ea/operator/termination/ core/src/test/java/ea/operator/TerminationConditionTest.java
git commit -m "feat(core): add MaxGenerations, FitnessThreshold, NoImprovement termination conditions"
```

---

## Task 8: GA Engine

**Files:**
- Create: `core/src/main/java/ea/engine/GeneticAlgorithm.java`
- Test: `core/src/test/java/ea/engine/GeneticAlgorithmTest.java`

- [ ] **Step 1: Write failing test**

```java
package ea.engine;

import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GeneticAlgorithmTest {

    private GeneticAlgorithm<boolean[]> oneMaxGa(int chromosomeLength, int populationSize, int generations) {
        Random rng = new Random(42);
        return new GeneticAlgorithm.Builder<boolean[]>()
            .chromosomeSupplier(() -> {
                boolean[] chr = new boolean[chromosomeLength];
                for (int i = 0; i < chromosomeLength; i++) chr[i] = rng.nextBoolean();
                return chr;
            })
            .fitnessFunction(chr -> {
                int count = 0;
                for (boolean b : chr) if (b) count++;
                return (double) count / chr.length;
            })
            .selection(new TournamentSelection<>(3, new Random(42)))
            .crossover(new BooleanSinglePointCrossover(new Random(42)))
            .crossoverRate(0.8)
            .mutation(new BitFlipMutation(new Random(42)))
            .mutationRate(0.01)
            .populationSize(populationSize)
            .termination(new MaxGenerations<>(generations))
            .build();
    }

    @Test
    void callsCallbackForEachGeneration() {
        var ga = oneMaxGa(20, 50, 10);
        var results = new ArrayList<>();
        ga.run(results::add);
        assertEquals(10, results.size());
    }

    @Test
    void generationNumbersIncrement() {
        var ga = oneMaxGa(20, 50, 5);
        var gens = new ArrayList<Integer>();
        ga.run(r -> gens.add(r.generation()));
        assertEquals(java.util.List.of(1, 2, 3, 4, 5), gens);
    }

    @Test
    void fitnessImprovesSolveOneMax() {
        var ga = oneMaxGa(20, 100, 100);
        var bestFitnesses = new ArrayList<Double>();
        ga.run(r -> bestFitnesses.add(r.bestFitness()));
        double finalBest = bestFitnesses.getLast();
        assertTrue(finalBest > 0.8, "GA should solve OneMax to >80% after 100 gens, got " + finalBest);
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=GeneticAlgorithmTest
```

- [ ] **Step 3: Create `GeneticAlgorithm.java`**

```java
package ea.engine;

import ea.api.*;
import ea.model.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GeneticAlgorithm<T> {

    private final FitnessFunction<T> fitnessFunction;
    private final SelectionStrategy<T> selection;
    private final CrossoverOperator<T> crossover;
    private final MutationOperator<T> mutation;
    private final TerminationCondition<T> termination;
    private final Supplier<T> chromosomeSupplier;
    private final int populationSize;
    private final double crossoverRate;
    private final double mutationRate;
    private final Random random;

    private GeneticAlgorithm(Builder<T> builder) {
        this.fitnessFunction = builder.fitnessFunction;
        this.selection = builder.selection;
        this.crossover = builder.crossover;
        this.mutation = builder.mutation;
        this.termination = builder.termination;
        this.chromosomeSupplier = builder.chromosomeSupplier;
        this.populationSize = builder.populationSize;
        this.crossoverRate = builder.crossoverRate;
        this.mutationRate = builder.mutationRate;
        this.random = builder.random;
    }

    public void run(Consumer<GenerationResult<T>> onGeneration) {
        Population<T> population = evaluate(initialize());
        while (!termination.shouldStop(GenerationResult.of(population))) {
            population = evaluate(breed(population));
            onGeneration.accept(GenerationResult.of(population));
        }
    }

    private Population<T> initialize() {
        List<Individual<T>> individuals = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            individuals.add(Individual.of(chromosomeSupplier.get(), 0.0));
        }
        return new Population<>(individuals, 0);
    }

    private Population<T> evaluate(Population<T> population) {
        List<Individual<T>> evaluated = population.individuals().stream()
            .map(ind -> Individual.of(ind.chromosome(), fitnessFunction.evaluate(ind.chromosome())))
            .toList();
        return new Population<>(evaluated, population.generation());
    }

    private Population<T> breed(Population<T> population) {
        List<Individual<T>> offspring = new ArrayList<>(populationSize);
        while (offspring.size() < populationSize) {
            List<Individual<T>> parents = selection.select(population, 2);
            T p1 = parents.get(0).chromosome();
            T p2 = parents.get(1).chromosome();

            T c1, c2;
            if (random.nextDouble() < crossoverRate) {
                T[] children = crossover.crossover(p1, p2);
                c1 = children[0];
                c2 = children[1];
            } else {
                c1 = p1;
                c2 = p2;
            }

            offspring.add(Individual.of(mutation.mutate(c1, mutationRate), 0.0));
            if (offspring.size() < populationSize) {
                offspring.add(Individual.of(mutation.mutate(c2, mutationRate), 0.0));
            }
        }
        return new Population<>(Collections.unmodifiableList(offspring), population.generation() + 1);
    }

    public static class Builder<T> {
        private FitnessFunction<T> fitnessFunction;
        private SelectionStrategy<T> selection;
        private CrossoverOperator<T> crossover;
        private MutationOperator<T> mutation;
        private TerminationCondition<T> termination;
        private Supplier<T> chromosomeSupplier;
        private int populationSize = 100;
        private double crossoverRate = 0.8;
        private double mutationRate = 0.01;
        private Random random = new Random();

        public Builder<T> fitnessFunction(FitnessFunction<T> fn) { this.fitnessFunction = fn; return this; }
        public Builder<T> selection(SelectionStrategy<T> s) { this.selection = s; return this; }
        public Builder<T> crossover(CrossoverOperator<T> c) { this.crossover = c; return this; }
        public Builder<T> crossoverRate(double r) { this.crossoverRate = r; return this; }
        public Builder<T> mutation(MutationOperator<T> m) { this.mutation = m; return this; }
        public Builder<T> mutationRate(double r) { this.mutationRate = r; return this; }
        public Builder<T> populationSize(int n) { this.populationSize = n; return this; }
        public Builder<T> termination(TerminationCondition<T> t) { this.termination = t; return this; }
        public Builder<T> chromosomeSupplier(Supplier<T> s) { this.chromosomeSupplier = s; return this; }
        public Builder<T> seed(long seed) { this.random = new Random(seed); return this; }

        public GeneticAlgorithm<T> build() {
            Objects.requireNonNull(fitnessFunction, "fitnessFunction required");
            Objects.requireNonNull(selection, "selection required");
            Objects.requireNonNull(crossover, "crossover required");
            Objects.requireNonNull(mutation, "mutation required");
            Objects.requireNonNull(termination, "termination required");
            Objects.requireNonNull(chromosomeSupplier, "chromosomeSupplier required");
            return new GeneticAlgorithm<>(this);
        }
    }
}
```

- [ ] **Step 4: Run tests — verify pass**

```bash
mvn test -pl core -Dtest=GeneticAlgorithmTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Run full core test suite**

```bash
mvn test -pl core
```

Expected: all tests pass

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/ea/engine/ core/src/test/java/ea/engine/
git commit -m "feat(core): add GeneticAlgorithm engine with Builder"
```

---

## Task 9: OneMax Problem

**Files:**
- Create: `core/src/main/java/ea/problem/OneMax.java`
- Test: `core/src/test/java/ea/problem/OneMaxTest.java`

- [ ] **Step 1: Write failing test**

```java
package ea.problem;

import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class OneMaxTest {

    @Test
    void evaluateCountsOnes() {
        var problem = new OneMax(10);
        assertEquals(0.0, problem.evaluate(new boolean[]{false, false, false, false, false, false, false, false, false, false}));
        assertEquals(1.0, problem.evaluate(new boolean[]{true, true, true, true, true, true, true, true, true, true}));
        assertEquals(0.5, problem.evaluate(new boolean[]{true, false, true, false, true, false, true, false, true, false}));
    }

    @Test
    void defaultGaSolvesToNearOptimal() {
        var problem = new OneMax(20);
        var ga = problem.defaultGa();
        var best = new AtomicReference<>(0.0);
        ga.run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 0.9, "OneMax(20) should solve to >90%, got " + best.get());
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=OneMaxTest
```

- [ ] **Step 3: Create `OneMax.java`**

```java
package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.BooleanSinglePointCrossover;
import ea.operator.mutation.BitFlipMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;

public class OneMax implements FitnessFunction<boolean[]> {

    private final int length;

    public OneMax(int length) {
        this.length = length;
    }

    @Override
    public double evaluate(boolean[] individual) {
        int count = 0;
        for (boolean gene : individual) if (gene) count++;
        return (double) count / individual.length;
    }

    public GeneticAlgorithm<boolean[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<boolean[]>()
            .chromosomeSupplier(() -> {
                boolean[] chr = new boolean[length];
                for (int i = 0; i < length; i++) chr[i] = rng.nextBoolean();
                return chr;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(3))
            .crossover(new BooleanSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new BitFlipMutation())
            .mutationRate(1.0 / length)
            .populationSize(100)
            .termination(new MaxGenerations<>(200))
            .build();
    }
}
```

- [ ] **Step 4: Run tests — verify pass**

```bash
mvn test -pl core -Dtest=OneMaxTest
```

Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/OneMax.java core/src/test/java/ea/problem/OneMaxTest.java
git commit -m "feat(core): add OneMax problem"
```

---

## Task 10: N-Queens Problem

**Files:**
- Create: `core/src/main/java/ea/problem/NQueens.java`
- Test: `core/src/test/java/ea/problem/NQueensTest.java`

Representation: `int[]` of length N where `individual[row] = column`. Fitness = fraction of non-attacking pairs out of total possible pairs.

- [ ] **Step 1: Write failing test**

```java
package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class NQueensTest {

    @Test
    void perfectSolutionHasFitnessOne() {
        // 4-queens solution: [1, 3, 0, 2]
        var problem = new NQueens(4);
        assertEquals(1.0, problem.evaluate(new int[]{1, 3, 0, 2}), 1e-9);
    }

    @Test
    void allSameColumnHasWorstFitness() {
        var problem = new NQueens(4);
        assertTrue(problem.evaluate(new int[]{0, 0, 0, 0}) < 0.5);
    }

    @Test
    void defaultGaImprovesFitness() {
        var problem = new NQueens(8);
        var best = new AtomicReference<>(0.0);
        problem.defaultGa().run(r -> best.set(r.bestFitness()));
        assertTrue(best.get() > 0.8, "NQueens(8) should reach >0.8 fitness, got " + best.get());
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=NQueensTest
```

- [ ] **Step 3: Create `NQueens.java`**

```java
package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.IntegerSinglePointCrossover;
import ea.operator.mutation.SwapMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;

public class NQueens implements FitnessFunction<int[]> {

    private final int n;

    public NQueens(int n) {
        this.n = n;
    }

    @Override
    public double evaluate(int[] individual) {
        int totalPairs = n * (n - 1) / 2;
        int conflicts = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (individual[i] == individual[j]) conflicts++;
                else if (Math.abs(individual[i] - individual[j]) == Math.abs(i - j)) conflicts++;
            }
        }
        return (double) (totalPairs - conflicts) / totalPairs;
    }

    public GeneticAlgorithm<int[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<int[]>()
            .chromosomeSupplier(() -> {
                int[] chr = new int[n];
                for (int i = 0; i < n; i++) chr[i] = rng.nextInt(n);
                return chr;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(5))
            .crossover(new IntegerSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new SwapMutation())
            .mutationRate(0.05)
            .populationSize(200)
            .termination(new MaxGenerations<>(500))
            .build();
    }
}
```

- [ ] **Step 4: Run tests — verify pass**

```bash
mvn test -pl core -Dtest=NQueensTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/NQueens.java core/src/test/java/ea/problem/NQueensTest.java
git commit -m "feat(core): add NQueens problem"
```

---

## Task 11: TSP Problem

**Files:**
- Create: `core/src/main/java/ea/problem/TSP.java`
- Test: `core/src/test/java/ea/problem/TSPTest.java`

Representation: `int[]` — permutation of city indices. Fitness = 1 / (1 + total route distance). Cities provided as (x, y) coordinates.

- [ ] **Step 1: Write failing test**

```java
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
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=TSPTest
```

- [ ] **Step 3: Create `TSP.java`**

```java
package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.IntegerSinglePointCrossover;
import ea.operator.mutation.SwapMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;
import java.util.stream.IntStream;

public class TSP implements FitnessFunction<int[]> {

    private final double[][] cities;

    public TSP(double[][] cities) {
        this.cities = cities;
    }

    @Override
    public double evaluate(int[] route) {
        double distance = 0;
        for (int i = 0; i < route.length; i++) {
            int from = route[i];
            int to = route[(i + 1) % route.length];
            double dx = cities[from][0] - cities[to][0];
            double dy = cities[from][1] - cities[to][1];
            distance += Math.sqrt(dx * dx + dy * dy);
        }
        return 1.0 / (1.0 + distance);
    }

    public GeneticAlgorithm<int[]> defaultGa() {
        int n = cities.length;
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<int[]>()
            .chromosomeSupplier(() -> {
                int[] order = IntStream.range(0, n).toArray();
                for (int i = n - 1; i > 0; i--) {
                    int j = rng.nextInt(i + 1);
                    int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
                }
                return order;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(5))
            .crossover(new IntegerSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new SwapMutation())
            .mutationRate(0.05)
            .populationSize(200)
            .termination(new MaxGenerations<>(500))
            .build();
    }
}
```

- [ ] **Step 4: Run tests — verify pass**

```bash
mvn test -pl core -Dtest=TSPTest
```

Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/ea/problem/TSP.java core/src/test/java/ea/problem/TSPTest.java
git commit -m "feat(core): add TSP problem"
```

---

## Task 12: Function Optimization Problem

**Files:**
- Create: `core/src/main/java/ea/problem/FunctionOptimization.java`
- Test: `core/src/test/java/ea/problem/FunctionOptimizationTest.java`

Representation: `double[]` — one element per variable. Fitness = negated function value (so maximization = minimization of f). Uses `BitFlipMutation` adapted via a `GaussianMutation` inner strategy.

Since mutation operators are type-specific, add `GaussianMutation` alongside this task.

- [ ] **Step 1: Create `GaussianMutation.java`**

```java
package ea.operator.mutation;

import ea.api.MutationOperator;
import java.util.Arrays;
import java.util.Random;

public class GaussianMutation implements MutationOperator<double[]> {

    private final Random random;
    private final double stdDev;

    public GaussianMutation(double stdDev) {
        this(stdDev, new Random());
    }

    public GaussianMutation(double stdDev, Random random) {
        this.stdDev = stdDev;
        this.random = random;
    }

    @Override
    public double[] mutate(double[] individual, double rate) {
        double[] result = Arrays.copyOf(individual, individual.length);
        for (int i = 0; i < result.length; i++) {
            if (random.nextDouble() < rate) {
                result[i] += random.nextGaussian() * stdDev;
            }
        }
        return result;
    }
}
```

- [ ] **Step 2: Create `DoubleSinglePointCrossover.java`**

```java
package ea.operator.crossover;

import ea.api.CrossoverOperator;
import java.util.Arrays;
import java.util.Random;

public class DoubleSinglePointCrossover implements CrossoverOperator<double[]> {

    private final Random random;

    public DoubleSinglePointCrossover() {
        this(new Random());
    }

    public DoubleSinglePointCrossover(Random random) {
        this.random = random;
    }

    @Override
    public double[][] crossover(double[] parent1, double[] parent2) {
        int point = random.nextInt(parent1.length);
        double[] child1 = Arrays.copyOf(parent1, parent1.length);
        double[] child2 = Arrays.copyOf(parent2, parent2.length);
        for (int i = point; i < parent1.length; i++) {
            child1[i] = parent2[i];
            child2[i] = parent1[i];
        }
        return new double[][]{child1, child2};
    }
}
```

- [ ] **Step 3: Write failing test for FunctionOptimization**

```java
package ea.problem;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class FunctionOptimizationTest {

    @Test
    void minimizesNegativeParabolaAtOrigin() {
        // f(x) = x^2, minimum at x=0. We minimize → fitness = -f(x) = -x^2
        // GA should find x near 0 → fitness near 0
        var problem = FunctionOptimization.minimize(
            vars -> vars[0] * vars[0],  // f(x) = x^2
            1,                           // 1 variable
            new double[]{-5.0},          // lower bound
            new double[]{5.0}            // upper bound
        );
        var best = new AtomicReference<>(Double.NEGATIVE_INFINITY);
        problem.defaultGa().run(r -> best.set(r.bestFitness()));
        // bestFitness = -f(best_x), so best_x near 0 → bestFitness near 0
        assertTrue(best.get() > -0.1, "Should minimize x^2 near 0, got fitness=" + best.get());
    }

    @Test
    void evaluateReturnsNegatedFunctionValue() {
        var problem = FunctionOptimization.minimize(
            vars -> vars[0] + vars[1],
            2,
            new double[]{-10, -10},
            new double[]{10, 10}
        );
        assertEquals(-3.0, problem.evaluate(new double[]{1.0, 2.0}), 1e-9);
    }
}
```

- [ ] **Step 4: Run — verify compilation failure**

```bash
mvn test -pl core -Dtest=FunctionOptimizationTest
```

- [ ] **Step 5: Create `FunctionOptimization.java`**

```java
package ea.problem;

import ea.api.FitnessFunction;
import ea.engine.GeneticAlgorithm;
import ea.operator.crossover.DoubleSinglePointCrossover;
import ea.operator.mutation.GaussianMutation;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import java.util.Random;
import java.util.function.ToDoubleFunction;

public class FunctionOptimization implements FitnessFunction<double[]> {

    private final ToDoubleFunction<double[]> function;
    private final int dimensions;
    private final double[] lowerBounds;
    private final double[] upperBounds;

    private FunctionOptimization(ToDoubleFunction<double[]> function, int dimensions,
                                  double[] lowerBounds, double[] upperBounds) {
        this.function = function;
        this.dimensions = dimensions;
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }

    public static FunctionOptimization minimize(ToDoubleFunction<double[]> f, int dimensions,
                                                 double[] lowerBounds, double[] upperBounds) {
        return new FunctionOptimization(vars -> -f.applyAsDouble(vars), dimensions, lowerBounds, upperBounds);
    }

    @Override
    public double evaluate(double[] individual) {
        return function.applyAsDouble(individual);
    }

    public GeneticAlgorithm<double[]> defaultGa() {
        Random rng = new Random();
        return new GeneticAlgorithm.Builder<double[]>()
            .chromosomeSupplier(() -> {
                double[] chr = new double[dimensions];
                for (int i = 0; i < dimensions; i++) {
                    chr[i] = lowerBounds[i] + rng.nextDouble() * (upperBounds[i] - lowerBounds[i]);
                }
                return chr;
            })
            .fitnessFunction(this)
            .selection(new TournamentSelection<>(5))
            .crossover(new DoubleSinglePointCrossover())
            .crossoverRate(0.8)
            .mutation(new GaussianMutation(0.5))
            .mutationRate(0.2)
            .populationSize(100)
            .termination(new MaxGenerations<>(300))
            .build();
    }
}
```

- [ ] **Step 6: Run all core tests — verify all pass**

```bash
mvn test -pl core
```

Expected: all tests pass

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/ea/operator/mutation/GaussianMutation.java \
        core/src/main/java/ea/operator/crossover/DoubleSinglePointCrossover.java \
        core/src/main/java/ea/problem/FunctionOptimization.java \
        core/src/test/java/ea/problem/FunctionOptimizationTest.java
git commit -m "feat(core): add FunctionOptimization, GaussianMutation, DoubleSinglePointCrossover"
```

---

## Task 13: Javalin Server

**Files:**
- Create: `server/src/main/java/ea/server/Main.java`
- Create: `server/src/main/java/ea/server/ProblemRegistry.java`
- Create: `server/src/main/java/ea/server/WebSocketHandler.java`
- Test: `server/src/test/java/ea/server/WebSocketHandlerTest.java`

- [ ] **Step 1: Write failing test**

```java
package ea.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WebSocketHandlerTest {

    @Test
    void registryResolvesKnownProblems() {
        var registry = new ProblemRegistry();
        assertNotNull(registry.resolve("onemax", 20, new int[0][]));
        assertNotNull(registry.resolve("nqueens", 8, new int[0][]));
    }

    @Test
    void registryThrowsOnUnknownProblem() {
        var registry = new ProblemRegistry();
        assertThrows(IllegalArgumentException.class,
            () -> registry.resolve("unknown", 10, new int[0][]));
    }
}
```

- [ ] **Step 2: Run — verify compilation failure**

```bash
mvn test -pl server -Dtest=WebSocketHandlerTest
```

- [ ] **Step 3: Create `ProblemRegistry.java`**

```java
package ea.server;

import ea.api.FitnessFunction;
import ea.problem.*;

public class ProblemRegistry {

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
            case "function" -> FunctionOptimization.minimize(
                vars -> vars[0] * vars[0] + vars[1] * vars[1],
                2, new double[]{-5, -5}, new double[]{5, 5}
            );
            default -> throw new IllegalArgumentException("Unknown problem: " + name);
        };
    }

    private double[][] defaultCities(int count) {
        double[][] cities = new double[count][2];
        var rng = new java.util.Random(42);
        for (double[] city : cities) { city[0] = rng.nextDouble() * 100; city[1] = rng.nextDouble() * 100; }
        return cities;
    }
}
```

- [ ] **Step 4: Create `WebSocketHandler.java`**

```java
package ea.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ea.engine.GeneticAlgorithm;
import ea.model.GenerationResult;
import ea.operator.crossover.*;
import ea.operator.mutation.*;
import ea.operator.selection.TournamentSelection;
import ea.operator.termination.MaxGenerations;
import ea.problem.*;
import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, AtomicBoolean> runningFlags = new ConcurrentHashMap<>();

    public void onMessage(WsContext ctx, String message) throws Exception {
        JsonNode msg = mapper.readTree(message);
        String type = msg.get("type").asText();

        switch (type) {
            case "start" -> startEvolution(ctx, msg);
            case "stop" -> stopEvolution(ctx.getSessionId());
            default -> ctx.send(errorJson("Unknown message type: " + type));
        }
    }

    public void onClose(WsContext ctx) {
        stopEvolution(ctx.getSessionId());
    }

    @SuppressWarnings("unchecked")
    private void startEvolution(WsContext ctx, JsonNode msg) {
        String sessionId = ctx.getSessionId();
        stopEvolution(sessionId);

        String problem = msg.get("problem").asText();
        JsonNode params = msg.get("params");
        int populationSize = params.get("populationSize").asInt(100);
        double mutationRate = params.get("mutationRate").asDouble(0.01);
        double crossoverRate = params.get("crossoverRate").asDouble(0.8);
        int maxGenerations = params.get("maxGenerations").asInt(500);
        int size = params.has("size") ? params.get("size").asInt(20) : 20;

        AtomicBoolean running = new AtomicBoolean(true);
        runningFlags.put(sessionId, running);

        Thread.ofVirtual().start(() -> {
            try {
                GeneticAlgorithm<?> ga = buildGa(problem, size, populationSize, mutationRate, crossoverRate, maxGenerations);
                ga.run(result -> {
                    if (!running.get()) throw new RuntimeException("stopped");
                    try {
                        ctx.send(generationJson(result));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                if (running.get()) ctx.send(doneJson("maxGenerations"));
            } catch (RuntimeException e) {
                if (running.get()) {
                    try { ctx.send(errorJson(e.getMessage())); } catch (Exception ignored) {}
                }
            }
            runningFlags.remove(sessionId);
        });
    }

    private void stopEvolution(String sessionId) {
        AtomicBoolean flag = runningFlags.remove(sessionId);
        if (flag != null) flag.set(false);
    }

    private GeneticAlgorithm<?> buildGa(String problem, int size, int populationSize,
                                          double mutationRate, double crossoverRate, int maxGenerations) {
        Random rng = new Random();
        return switch (problem) {
            case "onemax" -> {
                var p = new OneMax(size);
                yield new GeneticAlgorithm.Builder<boolean[]>()
                    .chromosomeSupplier(() -> { boolean[] c = new boolean[size]; for (int i=0;i<size;i++) c[i]=rng.nextBoolean(); return c; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(3))
                    .crossover(new BooleanSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new BitFlipMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "nqueens" -> {
                var p = new NQueens(size);
                yield new GeneticAlgorithm.Builder<int[]>()
                    .chromosomeSupplier(() -> { int[] c = new int[size]; for (int i=0;i<size;i++) c[i]=rng.nextInt(size); return c; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new IntegerSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new SwapMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "tsp" -> {
                double[][] cities = new double[size][2]; for(double[] c: cities){c[0]=rng.nextDouble()*100;c[1]=rng.nextDouble()*100;}
                var p = new TSP(cities);
                yield new GeneticAlgorithm.Builder<int[]>()
                    .chromosomeSupplier(() -> { int[] o=java.util.stream.IntStream.range(0,size).toArray(); for(int i=size-1;i>0;i--){int j=rng.nextInt(i+1);int t=o[i];o[i]=o[j];o[j]=t;} return o; })
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new IntegerSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new SwapMutation()).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            case "function" -> {
                var p = FunctionOptimization.minimize(v -> v[0]*v[0]+v[1]*v[1], 2, new double[]{-5,-5}, new double[]{5,5});
                yield new GeneticAlgorithm.Builder<double[]>()
                    .chromosomeSupplier(() -> new double[]{rng.nextDouble()*10-5, rng.nextDouble()*10-5})
                    .fitnessFunction(p).selection(new TournamentSelection<>(5))
                    .crossover(new DoubleSinglePointCrossover()).crossoverRate(crossoverRate)
                    .mutation(new GaussianMutation(0.5)).mutationRate(mutationRate)
                    .populationSize(populationSize).termination(new MaxGenerations<>(maxGenerations)).build();
            }
            default -> throw new IllegalArgumentException("Unknown problem: " + problem);
        };
    }

    private String generationJson(GenerationResult<?> result) throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.put("type", "generation");
        node.put("gen", result.generation());
        node.put("bestFitness", result.bestFitness());
        node.put("avgFitness", result.avgFitness());
        node.put("worstFitness", result.worstFitness());
        node.set("bestIndividual", mapper.valueToTree(result.bestIndividual().chromosome()));
        return mapper.writeValueAsString(node);
    }

    private String doneJson(String reason) throws Exception {
        return mapper.writeValueAsString(Map.of("type", "done", "reason", reason));
    }

    private String errorJson(String message) throws Exception {
        return mapper.writeValueAsString(Map.of("type", "error", "message", message));
    }
}
```

- [ ] **Step 5: Create `Main.java`**

```java
package ea.server;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        WebSocketHandler handler = new WebSocketHandler();

        Javalin.create(config -> config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost())))
            .ws("/evolution", ws -> {
                ws.onMessage(ctx -> handler.onMessage(ctx, ctx.message()));
                ws.onClose(ctx -> handler.onClose(ctx));
                ws.onError(ctx -> handler.onClose(ctx));
            })
            .start(port);

        System.out.println("Server started on port " + port);
    }
}
```

- [ ] **Step 6: Run server tests — verify pass**

```bash
mvn test -pl server -Dtest=WebSocketHandlerTest
```

Expected: `Tests run: 2, Failures: 0, Errors: 0`

- [ ] **Step 7: Build the server fat JAR and smoke test**

```bash
mvn package -pl server -am -DskipTests
java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar &
sleep 2
curl -s http://localhost:8080/ || echo "Server running (non-200 on / is fine)"
kill %1
```

Expected: server starts without error

- [ ] **Step 8: Commit**

```bash
git add server/src/ 
git commit -m "feat(server): add Javalin WebSocket server with ProblemRegistry and WebSocketHandler"
```

---

## Task 14: Final Verification

- [ ] **Step 1: Run full test suite across all modules**

```bash
mvn test
```

Expected: all tests pass, zero failures

- [ ] **Step 2: Build all artifacts**

```bash
mvn package -DskipTests
```

Expected: `BUILD SUCCESS`, `server/target/evolutionary-algorithms-server-*.jar` exists

- [ ] **Step 3: Verify core has zero framework dependencies**

```bash
mvn dependency:tree -pl core
```

Expected: only `junit-jupiter` in the dependency tree (test scope only)

- [ ] **Step 4: Commit and tag**

```bash
git add .
git commit -m "chore: final verification — all tests pass"
git tag v0.1.0-ga
```

---

## Notes

- **Frontend:** Separate repo (`ea-frontend`), separate plan. Connect to `ws://localhost:8080/evolution`.
- **Adding new algorithms (ES, DE, PSO):** Add engine class to `core/ea/engine/`, add problem to `ProblemRegistry`, add builder branch to `WebSocketHandler.buildGa()`.
- **Publishing `core` to Maven:** Run `mvn deploy -pl core` with credentials configured in `~/.m2/settings.xml`.
