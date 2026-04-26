# Evolutionary Algorithms

A pure Java 21 genetic algorithm library with a live-streaming WebSocket server.

## Modules

| Module | Description |
|--------|-------------|
| `core` | GA engine and operators — zero framework dependencies, publishable as a standalone Maven artifact |
| `server` | Javalin 6 WebSocket server that streams evolution data to a frontend |

## Requirements

- Java 21+
- Maven 3.9+

## Quick Start

```bash
# Run all tests
mvn test

# Build the server fat JAR
mvn package -DskipTests

# Start the server (default port 8080)
java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar
```

Set `PORT` env var to change the port:
```bash
PORT=9090 java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar
```

## Using the Core Library

```java
var ga = new GeneticAlgorithm.Builder<boolean[]>()
    .chromosomeSupplier(() -> randomBits(20))
    .fitnessFunction(chr -> countOnes(chr) / (double) chr.length)
    .selection(new TournamentSelection<>(3))
    .crossover(new BooleanSinglePointCrossover())
    .crossoverRate(0.8)
    .mutation(new BitFlipMutation())
    .mutationRate(0.05)
    .populationSize(100)
    .termination(new MaxGenerations<>(200))
    .build();

ga.run(result -> System.out.printf("Gen %d — best: %.3f%n",
    result.generation(), result.bestFitness()));
```

Or use a built-in problem:

```java
new OneMax(20).defaultGa().run(r -> System.out.println(r.bestFitness()));
```

## Operators

**Selection**
- `TournamentSelection` — picks the best from k random candidates
- `RouletteWheelSelection` — fitness-proportionate selection
- `ElitismSelection` — always selects the top N individuals

**Crossover**
- `BooleanSinglePointCrossover` — single-point crossover for `boolean[]`
- `BooleanUniformCrossover` — gene-by-gene random swap for `boolean[]`
- `IntegerSinglePointCrossover` — single-point crossover for `int[]`
- `DoubleSinglePointCrossover` — single-point crossover for `double[]`

**Mutation**
- `BitFlipMutation` — randomly flips bits in a `boolean[]`
- `SwapMutation` — randomly swaps positions in an `int[]`
- `GaussianMutation` — adds Gaussian noise to a `double[]`

**Termination**
- `MaxGenerations` — stops after N generations
- `FitnessThreshold` — stops when best fitness ≥ threshold
- `NoImprovement` — stops after N generations without improvement

## Built-in Problems

| Problem | Chromosome | Description |
|---------|------------|-------------|
| `OneMax` | `boolean[]` | Maximise the number of `true` bits |
| `NQueens` | `int[]` | Place N queens with no conflicts |
| `TSP` | `int[]` | Minimise total route distance |
| `FunctionOptimization` | `double[]` | Minimise an arbitrary function |

## WebSocket Server

Connect to `ws://localhost:8080/evolution` and send:

```json
{
  "type": "start",
  "problem": "onemax",
  "params": {
    "size": 30,
    "populationSize": 100,
    "mutationRate": 0.05,
    "crossoverRate": 0.8,
    "maxGenerations": 200
  }
}
```

The server streams one JSON frame per generation:

```json
{
  "type": "generation",
  "gen": 42,
  "bestFitness": 0.933,
  "avgFitness": 0.781,
  "worstFitness": 0.567,
  "bestIndividual": [true, true, false, true, ...]
}
```

Send `{"type": "stop"}` to halt evolution early. Supported problems: `onemax`, `nqueens`, `tsp`, `function`.

## Project Structure

```
evolutionary-algorithms/
├── core/                          # Pure Java GA library
│   └── src/main/java/ea/
│       ├── api/                   # FitnessFunction, SelectionStrategy, ...
│       ├── engine/                # GeneticAlgorithm + Builder
│       ├── model/                 # Individual, Population, GenerationResult
│       ├── operator/
│       │   ├── crossover/
│       │   ├── mutation/
│       │   ├── selection/
│       │   └── termination/
│       └── problem/               # OneMax, NQueens, TSP, FunctionOptimization
└── server/                        # Javalin WebSocket server
    └── src/main/java/ea/server/
        ├── Main.java
        ├── ProblemRegistry.java
        └── WebSocketHandler.java
```
