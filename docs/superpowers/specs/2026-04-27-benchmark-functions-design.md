# Benchmark Functions — Design Spec

**Date:** 2026-04-27  
**Status:** Approved

---

## Overview

Add 12 standard continuous optimisation benchmark functions to the `core` library, expose each via the WebSocket server by name, and cover them with deterministic formula tests. All functions plug into the existing `FunctionOptimization` wrapper — no new interfaces or engine changes required.

---

## Section 1 — Files Changed

**New:**
- `core/src/main/java/ea/problem/BenchmarkFunctions.java`
- `core/src/test/java/ea/problem/BenchmarkFunctionsTest.java`

**Modified:**
- `server/src/main/java/ea/server/ProblemRegistry.java` — 12 new `case` entries

No changes to `FunctionOptimization`, any existing problem class, or any interface.

---

## Section 2 — `BenchmarkFunctions` API

A non-instantiable utility class in `ea.problem`. Each method returns `FunctionOptimization` so `defaultGa()` works for free.

### n-dimensional functions (require explicit `int dimensions`)

| Method | Formula summary | Global min | Bounds per dimension |
|---|---|---|---|
| `sphere(int d)` | Σ xᵢ² | 0 at origin | [−5.12, 5.12] |
| `ackley(int d)` | −20 exp(−0.2 √(Σxᵢ²/d)) − exp(Σcos(2πxᵢ)/d) + 20 + e | 0 at origin | [−32.768, 32.768] |
| `griewank(int d)` | 1 + Σ(xᵢ²/4000) − Π cos(xᵢ/√i) | 0 at origin | [−600, 600] |
| `rastrigin(int d)` | 10d + Σ(xᵢ² − 10 cos(2πxᵢ)) | 0 at origin | [−5.12, 5.12] |
| `schwefel(int d)` | 418.9829d − Σ xᵢ sin(√\|xᵢ\|) | ≈ 0 at xᵢ ≈ 420.9687 | [−500, 500] |
| `rosenbrock(int d)` | Σ [100(x_{i+1}−xᵢ²)² + (1−xᵢ)²] | 0 at (1,…,1) | [−5, 10] |
| `trid(int d)` | Σ(xᵢ−1)² − Σ xᵢ x_{i−1} | −d(d+4)(d−1)/6 at xᵢ=i(d+1−i) | [−d², d²] |
| `styblinskiTang(int d)` | ½ Σ(xᵢ⁴ − 16xᵢ² + 5xᵢ) | ≈ −39.1662·d at xᵢ ≈ −2.9035 | [−5, 5] |
| `levy(int d)` | sin²(πw₁) + Σ(wᵢ−1)²(1+10sin²(πw_{i+1})) + (w_d−1)²(1+sin²(2πw_d)), wᵢ=1+(xᵢ−1)/4 | 0 at (1,…,1) | [−10, 10] |
| `michalewicz(int d)` | −Σ sin(xᵢ) sin²ᵐ(i xᵢ²/π), m=10 | depends on d | [0, π] |

### 2D-only functions (no `dimensions` parameter)

| Method | Formula summary | Global min | Bounds |
|---|---|---|---|
| `bukin()` | 100√(\|y − 0.01x²\|) + 0.01\|x+10\| | 0 at (−10, 1) | x∈[−15,−5], y∈[−3,3] |
| `carromTable()` | −(cos(x₁)cos(x₂) exp(\|1−√(x₁²+x₂²)/π\|))²/30 | ≈ −24.1568 at (±9.6461, ±9.6461) | [−10, 10]² |

---

## Section 3 — Tests

All tests live in `BenchmarkFunctionsTest`.

### Formula correctness (all 12 functions)

Call `evaluate()` at the known global optimum; assert the result is within 1e-6 of the expected negated value (since `FunctionOptimization.minimize` negates internally).

Examples:
- `sphere(3).evaluate(new double[]{0,0,0})` → `0.0`
- `rosenbrock(2).evaluate(new double[]{1,1})` → `0.0`
- `styblinskiTang(1).evaluate(new double[]{-2.903534})` → `≈ 39.1662` (`evaluate` returns `−f(x*)` = `−(−39.1662)`)

### GA convergence (Sphere, Rosenbrock, Styblinski-Tang only)

Run `defaultGa()` and assert that the best fitness after completion is within `0.5` of the known optimum (in the negated-fitness space). Hard multimodal functions (Schwefel, Rastrigin, Michalewicz) are excluded — a simple GA with default settings won't reliably find their global minimum, and a flaky convergence test teaches nothing.

---

## Section 4 — `ProblemRegistry` Changes

12 new `case` entries in the existing `switch`. `size` maps to `dimensions`; defaults to `2` when `size` is 0 or absent. The 2D-only functions ignore `size`.

```java
case "sphere"       -> BenchmarkFunctions.sphere(size > 0 ? size : 2);
case "ackley"       -> BenchmarkFunctions.ackley(size > 0 ? size : 2);
case "griewank"     -> BenchmarkFunctions.griewank(size > 0 ? size : 2);
case "rastrigin"    -> BenchmarkFunctions.rastrigin(size > 0 ? size : 2);
case "schwefel"     -> BenchmarkFunctions.schwefel(size > 0 ? size : 2);
case "rosenbrock"   -> BenchmarkFunctions.rosenbrock(size > 0 ? size : 2);
case "trid"         -> BenchmarkFunctions.trid(size > 0 ? size : 2);
case "styblinski"   -> BenchmarkFunctions.styblinskiTang(size > 0 ? size : 2);
case "levy"         -> BenchmarkFunctions.levy(size > 0 ? size : 2);
case "michalewicz"  -> BenchmarkFunctions.michalewicz(size > 0 ? size : 2);
case "bukin"        -> BenchmarkFunctions.bukin();
case "carrom"       -> BenchmarkFunctions.carromTable();
```

The existing `"function"` case is unchanged.

---

## Out of Scope

- Per-function tuned GA defaults (mutation sigma, population size) — `defaultGa()` from `FunctionOptimization` is a reasonable starting point
- 3D surface visualisation in the frontend — tracked separately
- Functions beyond these 12
