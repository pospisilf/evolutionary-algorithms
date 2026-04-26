# Evolutionary Algorithms Teaching Platform — Design Spec

**Date:** 2026-04-26  
**Status:** Approved

---

## Overview

A Java library (`core`) implementing evolutionary algorithm components — the primary product, designed for use in any Java project as a standalone Maven dependency. A Javalin WebSocket server (`server`) and React frontend are built on top as a teaching aid for university students, but the library is the main focus and must remain completely independent of the web layer.

The platform ships with Genetic Algorithm (GA) support first. The architecture is designed to accommodate future algorithms: Genetic Programming (GP), Evolution Strategies (ES), Differential Evolution (DE), Particle Swarm Optimization (PSO), and Neuroevolution (NEAT).

---

## Repositories

| Repo | Description |
|---|---|
| `evolutionary-algorithms` (this repo) | Java — `core` library + `server` module |
| `ea-frontend` (separate repo) | React + TypeScript frontend |

---

## Section 1 — Repository & Module Structure

### Backend (`evolutionary-algorithms`)

Maven multi-module Java project (Java 21).

```
evolutionary-algorithms/
├── pom.xml                          # parent POM
├── core/                            # Pure EA library — zero framework dependencies
│   └── src/main/java/ea/
│       ├── api/                     # Public interfaces
│       ├── engine/                  # GeneticAlgorithm engine
│       ├── operator/                # Built-in selection, crossover, mutation
│       ├── problem/                 # Built-in problems
│       └── model/                   # Individual, Population, GenerationResult
└── server/                          # Javalin WebSocket server — depends only on core
    └── src/main/java/ea/server/
        ├── WebSocketHandler.java
        └── Main.java
```

### Frontend (`ea-frontend`)

React + TypeScript project.

```
ea-frontend/
└── src/
    ├── problems/                    # Per-problem visualizations
    │   ├── NQueensBoard.tsx
    │   ├── TSPMap.tsx
    │   ├── FunctionSurface.tsx
    │   └── OneMaxGrid.tsx
    ├── components/
    │   ├── Sidebar.tsx              # ProblemSelector + ParameterPanel + Controls
    │   ├── FitnessChart.tsx         # Live best/avg fitness line chart
    │   └── MainPanel.tsx            # Renders active ProblemView + FitnessChart
    ├── hooks/
    │   └── useEvolution.ts          # WebSocket lifecycle management
    └── App.tsx
```

---

## Section 2 — Core Library Design

### Public Interfaces (`ea.api`)

```java
FitnessFunction<T>       // double evaluate(T individual)
SelectionStrategy<T>     // List<Individual<T>> select(Population<T> pop, int count)
CrossoverOperator<T>     // T[] crossover(T parent1, T parent2)
MutationOperator<T>      // T mutate(T individual, double rate)
TerminationCondition<T>  // boolean shouldStop(GenerationResult<T> result)
```

### Core Models (`ea.model`)

```java
Individual<T>            // T chromosome, double fitness
Population<T>            // List<Individual<T>>, int generation
GenerationResult<T>      // generation, bestFitness, avgFitness, worstFitness, bestIndividual, population
```

### GA Engine (`ea.engine`)

`GeneticAlgorithm<T>` — built via a fluent `Builder`. Runs the loop:

1. Initialize random population
2. Evaluate fitness for all individuals
3. Check termination condition
4. Select parents → crossover → mutate → evaluate offspring
5. Emit `GenerationResult<T>` via callback
6. Repeat from 3

The callback (`Consumer<GenerationResult<T>>`) is the only output channel — works for WebSocket streaming, CLI printing, or unit tests.

```java
GeneticAlgorithm<Boolean[]> ga = new GeneticAlgorithm.Builder<Boolean[]>()
    .fitnessFunction(individual -> /* custom logic */)
    .selection(new TournamentSelection<>(5))
    .crossover(new SinglePointCrossover())
    .mutation(new BitFlipMutation(0.01))
    .populationSize(100)
    .crossoverRate(0.8)
    .termination(new MaxGenerations(500))
    .build();

ga.run(result -> System.out.println("Gen " + result.generation() + " best: " + result.bestFitness()));
```

### Built-in Operators (`ea.operator`)

| Category | Phase 1 Implementations |
|---|---|
| Selection | `TournamentSelection`, `RouletteWheelSelection`, `ElitismSelection` |
| Crossover | `SinglePointCrossover`, `UniformCrossover` |
| Mutation | `BitFlipMutation`, `SwapMutation` |
| Termination | `MaxGenerations`, `FitnessThreshold`, `NoImprovement` |

### Built-in Problems (`ea.problem`)

| Problem | Chromosome | Phase 1 |
|---|---|---|
| OneMax | `boolean[]` | yes |
| N-Queens | `int[]` (queen per row) | yes |
| TSP | `int[]` (city visit order) | yes |
| Function optimization | `double[]` | yes |

Each problem class implements `FitnessFunction<T>` and provides a static factory for typical default parameters. Custom fitness functions are supported by implementing `FitnessFunction<T>` directly — the library imposes no other constraint.

---

## Section 3 — Server & WebSocket Protocol

### Server (`ea.server`)

- **Framework:** Javalin (minimal dependencies, no Spring)
- **Port:** 8080 (configurable via env var)
- **Endpoint:** `ws://localhost:8080/evolution`
- **Threading:** GA runs on a virtual thread (Java 21) per WebSocket connection so the handler stays non-blocking

`Main.java` boots Javalin and registers the WebSocket handler. `WebSocketHandler.java` manages one GA run per connection (start, pause, resume, stop).

### WebSocket Message Protocol

**Client → Server:**

```json
{ "type": "start", "problem": "nqueens", "params": { "populationSize": 100, "mutationRate": 0.01, "crossoverRate": 0.8, "maxGenerations": 500 } }
{ "type": "pause" }
{ "type": "resume" }
{ "type": "stop" }
```

Valid `problem` values: `"onemax"`, `"nqueens"`, `"tsp"`, `"function"`.

**Server → Client:**

```json
{ "type": "generation", "gen": 42, "bestFitness": 0.94, "avgFitness": 0.71, "worstFitness": 0.43, "bestIndividual": [1,0,1,1,0,1,1,0] }
{ "type": "done", "reason": "maxGenerations" }
{ "type": "error", "message": "Unknown problem: foo" }
```

Only `bestIndividual` is sent per generation (not the full population) to keep message size small. The fitness stats (best/avg/worst) provide the full picture for charting. A future `"detail": true` flag in the `start` message can enable full population streaming if needed.

`done.reason` values: `"maxGenerations"`, `"fitnessThreshold"`, `"noImprovement"`, `"stopped"`.

---

## Section 4 — React Frontend Design

### Layout

Single page, two-panel layout:

- **Left sidebar (fixed width):** Problem selector dropdown, parameter inputs (population size, mutation rate, crossover rate, max generations), Run / Pause / Resume / Stop controls, current generation counter and best fitness display
- **Main panel:** Problem-specific visualization (top, larger) + fitness line chart (bottom)

### Problem-Specific Visualizations

| Problem | Visualization |
|---|---|
| OneMax | Gene grid — each cell colored by bit value (0/1), best individual highlighted |
| N-Queens | Chessboard — queen positions from `bestIndividual`, conflicts highlighted in red |
| TSP | Canvas/SVG map — city dots + lines connecting them in best route order |
| Function optimization | 2D line chart with population dots overlaid, best individual marked (3D surface is a future enhancement) |

### `useEvolution` Hook

Central WebSocket manager. Exported interface:

```typescript
{
  status: 'idle' | 'running' | 'paused' | 'done' | 'error',
  generation: number,
  bestFitness: number,
  avgFitness: number,
  fitnessHistory: { gen: number, best: number, avg: number }[],
  bestIndividual: number[],   // JSON-serialized; booleans arrive as 0/1
  start: (problem: string, params: EvolutionParams) => void,
  pause: () => void,
  resume: () => void,
  stop: () => void,
}
```

Components subscribe to only the fields they need — `FitnessChart` reads `fitnessHistory`, problem views read `bestIndividual`, controls read `status`.

### Key Libraries

| Purpose | Library |
|---|---|
| Charting (fitness chart) | Recharts |
| TSP / function map rendering | SVG (inline React) by default; D3.js if interaction or animation complexity requires it |
| WebSocket | Native browser WebSocket API (no extra library) |

---

## Section 5 — Extensibility

### Adding a New Algorithm (e.g. Evolution Strategy)

1. Implement a new engine class (e.g. `EvolutionStrategy<T>`) in `core/ea/engine/` using the same `Consumer<GenerationResult<T>>` callback pattern
2. Add a new `"type": "start"` algorithm field to the WebSocket protocol
3. `WebSocketHandler` routes to the correct engine based on the algorithm field
4. No changes required to the frontend beyond adding a new algorithm option to the selector

### Adding a New Problem

1. Implement `FitnessFunction<T>` in `core/ea/problem/`
2. Register the problem name in the server's problem registry
3. Add a new visualization component in `ea-frontend/src/problems/`

### Adding Custom Fitness Functions (Developer Use)

Add `core` as a Maven dependency and implement `FitnessFunction<T>`. No server or frontend required — the library runs headless via the callback.

---

## Out of Scope (Phase 1)

- User accounts / persistence of run history
- Comparing multiple runs side by side
- Exporting results to CSV/JSON
- Mobile layout
- Authentication (not needed for local use; revisit if deployed)
- GP, ES, DE, PSO, NEAT algorithm implementations
