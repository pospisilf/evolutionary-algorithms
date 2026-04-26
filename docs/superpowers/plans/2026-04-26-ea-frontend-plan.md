# EA Frontend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

> **Note:** This plan lives in `evolutionary-algorithms` repo temporarily. Move it to `ea-frontend` repo when created.

> **Prerequisite:** The backend server (`evolutionary-algorithms-server`) must be running on `ws://localhost:8080/evolution` before starting UI tasks. Run: `java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar`

**Goal:** Build a React + TypeScript single-page app that visualises a running Genetic Algorithm in real time, connected to the Java backend over WebSocket.

**Architecture:** Single page, two-panel layout. Left sidebar holds problem selector, parameters, and controls. Main panel shows a problem-specific visualisation (top) and a live fitness chart (bottom). A single `useEvolution` hook owns the WebSocket connection and distributes state to components.

**Tech Stack:** React 18, TypeScript, Vite, Recharts (fitness chart), native WebSocket API

---

## File Map

```
ea-frontend/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── package.json
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── types.ts                          # shared TypeScript types
    ├── hooks/
    │   └── useEvolution.ts               # WebSocket lifecycle + state
    ├── components/
    │   ├── Sidebar.tsx                   # problem selector + params + controls
    │   ├── ParameterPanel.tsx            # sliders/inputs for GA params
    │   ├── Controls.tsx                  # Run / Pause / Resume / Stop buttons
    │   ├── MainPanel.tsx                 # routes to active problem view + chart
    │   └── FitnessChart.tsx              # Recharts line chart — best/avg per gen
    └── problems/
        ├── OneMaxGrid.tsx                # gene grid visualisation
        ├── NQueensBoard.tsx              # chessboard visualisation
        ├── TSPMap.tsx                    # SVG city map + route
        └── FunctionSurface.tsx           # 2D line chart with population dot
```

---

## Shared Types (`src/types.ts`)

Referenced throughout all tasks — defined once here.

```typescript
export type ProblemId = 'onemax' | 'nqueens' | 'tsp' | 'function';

export interface EvolutionParams {
  populationSize: number;
  mutationRate: number;
  crossoverRate: number;
  maxGenerations: number;
  size: number;        // chromosome length / board size / city count
}

export type EvolutionStatus = 'idle' | 'running' | 'paused' | 'done' | 'error';

export interface GenerationSnapshot {
  gen: number;
  bestFitness: number;
  avgFitness: number;
  worstFitness: number;
  bestIndividual: number[];  // booleans arrive as 0/1
}

export interface EvolutionState {
  status: EvolutionStatus;
  generation: number;
  bestFitness: number;
  avgFitness: number;
  fitnessHistory: { gen: number; best: number; avg: number }[];
  bestIndividual: number[];
  errorMessage: string | null;
}
```

---

## Task 1: Project Scaffold

**Files:**
- Create: `package.json`
- Create: `vite.config.ts`
- Create: `tsconfig.json`
- Create: `index.html`
- Create: `src/main.tsx`
- Create: `src/App.tsx`
- Create: `src/types.ts`

- [ ] **Step 1: Initialise Vite + React + TypeScript project**

```bash
npm create vite@latest ea-frontend -- --template react-ts
cd ea-frontend
npm install
npm install recharts
npm install -D @types/recharts
```

- [ ] **Step 2: Create `src/types.ts`**

```typescript
export type ProblemId = 'onemax' | 'nqueens' | 'tsp' | 'function';

export interface EvolutionParams {
  populationSize: number;
  mutationRate: number;
  crossoverRate: number;
  maxGenerations: number;
  size: number;
}

export type EvolutionStatus = 'idle' | 'running' | 'paused' | 'done' | 'error';

export interface GenerationSnapshot {
  gen: number;
  bestFitness: number;
  avgFitness: number;
  worstFitness: number;
  bestIndividual: number[];
}

export interface EvolutionState {
  status: EvolutionStatus;
  generation: number;
  bestFitness: number;
  avgFitness: number;
  fitnessHistory: { gen: number; best: number; avg: number }[];
  bestIndividual: number[];
  errorMessage: string | null;
}
```

- [ ] **Step 3: Replace `src/App.tsx` with a shell**

```tsx
import './App.css';

export default function App() {
  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif' }}>
      <aside style={{ width: 240, background: '#1e1e2e', padding: 16, color: '#cdd6f4' }}>
        Sidebar placeholder
      </aside>
      <main style={{ flex: 1, padding: 16, background: '#181825', color: '#cdd6f4' }}>
        Main panel placeholder
      </main>
    </div>
  );
}
```

- [ ] **Step 4: Start dev server and verify it loads**

```bash
npm run dev
```

Open `http://localhost:5173`. Expected: two-panel layout with placeholder text.

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "chore: scaffold Vite React TypeScript project"
```

---

## Task 2: `useEvolution` Hook

**Files:**
- Create: `src/hooks/useEvolution.ts`

- [ ] **Step 1: Create `src/hooks/useEvolution.ts`**

```typescript
import { useCallback, useRef, useState } from 'react';
import type { EvolutionParams, EvolutionState, GenerationSnapshot, ProblemId } from '../types';

const WS_URL = 'ws://localhost:8080/evolution';

const INITIAL_STATE: EvolutionState = {
  status: 'idle',
  generation: 0,
  bestFitness: 0,
  avgFitness: 0,
  fitnessHistory: [],
  bestIndividual: [],
  errorMessage: null,
};

export function useEvolution() {
  const [state, setState] = useState<EvolutionState>(INITIAL_STATE);
  const wsRef = useRef<WebSocket | null>(null);

  const send = useCallback((msg: object) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(msg));
    }
  }, []);

  const start = useCallback((problem: ProblemId, params: EvolutionParams) => {
    wsRef.current?.close();
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    setState({ ...INITIAL_STATE, status: 'running' });

    ws.onopen = () => {
      ws.send(JSON.stringify({ type: 'start', problem, params }));
    };

    ws.onmessage = (event) => {
      const msg = JSON.parse(event.data as string);

      if (msg.type === 'generation') {
        const snap = msg as GenerationSnapshot;
        setState((prev) => ({
          ...prev,
          status: 'running',
          generation: snap.gen,
          bestFitness: snap.bestFitness,
          avgFitness: snap.avgFitness,
          bestIndividual: snap.bestIndividual,
          fitnessHistory: [...prev.fitnessHistory, { gen: snap.gen, best: snap.bestFitness, avg: snap.avgFitness }],
        }));
      } else if (msg.type === 'done') {
        setState((prev) => ({ ...prev, status: 'done' }));
      } else if (msg.type === 'error') {
        setState((prev) => ({ ...prev, status: 'error', errorMessage: msg.message }));
      }
    };

    ws.onerror = () => {
      setState((prev) => ({ ...prev, status: 'error', errorMessage: 'Connection failed' }));
    };

    ws.onclose = () => {
      setState((prev) => prev.status === 'running' ? { ...prev, status: 'idle' } : prev);
    };
  }, []);

  const pause = useCallback(() => {
    send({ type: 'pause' });
    setState((prev) => ({ ...prev, status: 'paused' }));
  }, [send]);

  const resume = useCallback(() => {
    send({ type: 'resume' });
    setState((prev) => ({ ...prev, status: 'running' }));
  }, [send]);

  const stop = useCallback(() => {
    send({ type: 'stop' });
    wsRef.current?.close();
    setState((prev) => ({ ...prev, status: 'idle' }));
  }, [send]);

  return { ...state, start, pause, resume, stop };
}
```

- [ ] **Step 2: Commit**

```bash
git add src/hooks/useEvolution.ts src/types.ts
git commit -m "feat: add useEvolution WebSocket hook"
```

---

## Task 3: Sidebar — Problem Selector & Parameters

**Files:**
- Create: `src/components/ParameterPanel.tsx`
- Create: `src/components/Sidebar.tsx`

- [ ] **Step 1: Create `src/components/ParameterPanel.tsx`**

```tsx
import type { EvolutionParams, ProblemId } from '../types';

const DEFAULTS: Record<ProblemId, EvolutionParams> = {
  onemax:   { populationSize: 100, mutationRate: 0.05, crossoverRate: 0.8, maxGenerations: 200, size: 20 },
  nqueens:  { populationSize: 200, mutationRate: 0.05, crossoverRate: 0.8, maxGenerations: 500, size: 8 },
  tsp:      { populationSize: 200, mutationRate: 0.05, crossoverRate: 0.8, maxGenerations: 500, size: 10 },
  function: { populationSize: 100, mutationRate: 0.2,  crossoverRate: 0.8, maxGenerations: 300, size: 2 },
};

interface Props {
  problem: ProblemId;
  params: EvolutionParams;
  onChange: (params: EvolutionParams) => void;
}

interface SliderProps {
  label: string;
  value: number;
  min: number;
  max: number;
  step: number;
  onChange: (v: number) => void;
}

function Slider({ label, value, min, max, step, onChange }: SliderProps) {
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginBottom: 4 }}>
        <span style={{ color: '#a6adc8' }}>{label}</span>
        <span style={{ color: '#cdd6f4' }}>{value}</span>
      </div>
      <input
        type="range" min={min} max={max} step={step} value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        style={{ width: '100%', accentColor: '#a6e3a1' }}
      />
    </div>
  );
}

export function ParameterPanel({ problem, params, onChange }: Props) {
  const set = (key: keyof EvolutionParams) => (v: number) => onChange({ ...params, [key]: v });
  const sizeLabel = problem === 'nqueens' ? 'Board size (N)'
    : problem === 'tsp' ? 'Cities'
    : problem === 'onemax' ? 'Chromosome length'
    : 'Dimensions';

  return (
    <div>
      <Slider label={sizeLabel}        value={params.size}           min={4}    max={32}  step={1}     onChange={set('size')} />
      <Slider label="Population size"  value={params.populationSize} min={20}   max={500} step={10}    onChange={set('populationSize')} />
      <Slider label="Mutation rate"    value={params.mutationRate}   min={0.001} max={0.5} step={0.001} onChange={set('mutationRate')} />
      <Slider label="Crossover rate"   value={params.crossoverRate}  min={0.1}  max={1.0} step={0.05}  onChange={set('crossoverRate')} />
      <Slider label="Max generations"  value={params.maxGenerations} min={50}   max={2000} step={50}   onChange={set('maxGenerations')} />
    </div>
  );
}

export { DEFAULTS };
```

- [ ] **Step 2: Create `src/components/Sidebar.tsx`**

```tsx
import { useState } from 'react';
import type { EvolutionParams, EvolutionStatus, ProblemId } from '../types';
import { DEFAULTS, ParameterPanel } from './ParameterPanel';

const PROBLEMS: { id: ProblemId; label: string }[] = [
  { id: 'onemax',   label: 'OneMax' },
  { id: 'nqueens',  label: 'N-Queens' },
  { id: 'tsp',      label: 'Travelling Salesman' },
  { id: 'function', label: 'Function Optimisation' },
];

interface Props {
  status: EvolutionStatus;
  generation: number;
  bestFitness: number;
  onStart: (problem: ProblemId, params: EvolutionParams) => void;
  onPause: () => void;
  onResume: () => void;
  onStop: () => void;
}

const btn = (color: string): React.CSSProperties => ({
  width: '100%', padding: '8px 0', marginBottom: 8, borderRadius: 6, border: 'none',
  background: color, color: '#1e1e2e', fontWeight: 'bold', cursor: 'pointer', fontSize: 14,
});

export function Sidebar({ status, generation, bestFitness, onStart, onPause, onResume, onStop }: Props) {
  const [problem, setProblem] = useState<ProblemId>('onemax');
  const [params, setParams] = useState<EvolutionParams>(DEFAULTS.onemax);

  const handleProblemChange = (id: ProblemId) => {
    setProblem(id);
    setParams(DEFAULTS[id]);
  };

  return (
    <aside style={{ width: 240, background: '#1e1e2e', padding: 16, color: '#cdd6f4', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 16 }}>
      <div>
        <div style={{ fontSize: 11, color: '#cba6f7', fontWeight: 'bold', marginBottom: 6, letterSpacing: 1 }}>PROBLEM</div>
        <select
          value={problem}
          onChange={(e) => handleProblemChange(e.target.value as ProblemId)}
          style={{ width: '100%', padding: '6px 8px', background: '#313244', color: '#cdd6f4', border: 'none', borderRadius: 6, fontSize: 14 }}
          disabled={status === 'running'}
        >
          {PROBLEMS.map((p) => <option key={p.id} value={p.id}>{p.label}</option>)}
        </select>
      </div>

      <div>
        <div style={{ fontSize: 11, color: '#cba6f7', fontWeight: 'bold', marginBottom: 8, letterSpacing: 1 }}>PARAMETERS</div>
        <ParameterPanel problem={problem} params={params} onChange={setParams} />
      </div>

      <div>
        <div style={{ fontSize: 11, color: '#cba6f7', fontWeight: 'bold', marginBottom: 8, letterSpacing: 1 }}>CONTROLS</div>
        {status === 'idle' || status === 'done' || status === 'error' ? (
          <button style={btn('#a6e3a1')} onClick={() => onStart(problem, params)}>▶ Run</button>
        ) : status === 'running' ? (
          <>
            <button style={btn('#f9e2af')} onClick={onPause}>⏸ Pause</button>
            <button style={btn('#f38ba8')} onClick={onStop}>⏹ Stop</button>
          </>
        ) : (
          <>
            <button style={btn('#a6e3a1')} onClick={onResume}>▶ Resume</button>
            <button style={btn('#f38ba8')} onClick={onStop}>⏹ Stop</button>
          </>
        )}
      </div>

      {status !== 'idle' && (
        <div style={{ fontSize: 13, color: '#a6adc8' }}>
          <div>Generation: <span style={{ color: '#cdd6f4' }}>{generation}</span></div>
          <div>Best fitness: <span style={{ color: '#a6e3a1' }}>{bestFitness.toFixed(4)}</span></div>
          <div>Status: <span style={{ color: '#cba6f7' }}>{status}</span></div>
        </div>
      )}
    </aside>
  );
}
```

- [ ] **Step 3: Wire Sidebar into `App.tsx`**

```tsx
import { useEvolution } from './hooks/useEvolution';
import { Sidebar } from './components/Sidebar';
import './App.css';

export default function App() {
  const evolution = useEvolution();

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'sans-serif' }}>
      <Sidebar
        status={evolution.status}
        generation={evolution.generation}
        bestFitness={evolution.bestFitness}
        onStart={evolution.start}
        onPause={evolution.pause}
        onResume={evolution.resume}
        onStop={evolution.stop}
      />
      <main style={{ flex: 1, padding: 16, background: '#181825', color: '#cdd6f4' }}>
        Main panel placeholder
      </main>
    </div>
  );
}
```

- [ ] **Step 4: Verify sidebar renders and controls are clickable**

Start the backend server, then open `http://localhost:5173`. Expected: sidebar shows problem selector, sliders, and Run button. Clicking Run should attempt a WebSocket connection (check browser DevTools → Network → WS tab).

- [ ] **Step 5: Commit**

```bash
git add src/components/ src/App.tsx
git commit -m "feat: add Sidebar with problem selector and parameter controls"
```

---

## Task 4: Fitness Chart

**Files:**
- Create: `src/components/FitnessChart.tsx`

- [ ] **Step 1: Create `src/components/FitnessChart.tsx`**

```tsx
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';

interface Props {
  history: { gen: number; best: number; avg: number }[];
}

export function FitnessChart({ history }: Props) {
  if (history.length === 0) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#45475a', fontSize: 14 }}>
        Run an algorithm to see fitness progress
      </div>
    );
  }

  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={history} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <XAxis dataKey="gen" stroke="#45475a" tick={{ fill: '#a6adc8', fontSize: 11 }} label={{ value: 'Generation', position: 'insideBottom', offset: -2, fill: '#a6adc8', fontSize: 11 }} />
        <YAxis domain={[0, 1]} stroke="#45475a" tick={{ fill: '#a6adc8', fontSize: 11 }} />
        <Tooltip contentStyle={{ background: '#313244', border: 'none', color: '#cdd6f4', fontSize: 12 }} />
        <Legend wrapperStyle={{ fontSize: 12, color: '#a6adc8' }} />
        <Line type="monotone" dataKey="best" stroke="#a6e3a1" dot={false} strokeWidth={2} name="Best" isAnimationActive={false} />
        <Line type="monotone" dataKey="avg"  stroke="#89b4fa" dot={false} strokeWidth={2} name="Average" strokeDasharray="4 2" isAnimationActive={false} />
      </LineChart>
    </ResponsiveContainer>
  );
}
```

- [ ] **Step 2: Commit**

```bash
git add src/components/FitnessChart.tsx
git commit -m "feat: add live FitnessChart component"
```

---

## Task 5: Problem Visualisations

**Files:**
- Create: `src/problems/OneMaxGrid.tsx`
- Create: `src/problems/NQueensBoard.tsx`
- Create: `src/problems/TSPMap.tsx`
- Create: `src/problems/FunctionSurface.tsx`

- [ ] **Step 1: Create `src/problems/OneMaxGrid.tsx`**

```tsx
interface Props {
  individual: number[];  // 0 or 1 per gene
}

export function OneMaxGrid({ individual }: Props) {
  if (individual.length === 0) return <Placeholder text="Waiting for data..." />;
  const ones = individual.filter(Boolean).length;

  return (
    <div style={{ padding: 16 }}>
      <div style={{ marginBottom: 12, fontSize: 13, color: '#a6adc8' }}>
        Best individual — <span style={{ color: '#a6e3a1' }}>{ones}/{individual.length}</span> ones ({(ones / individual.length * 100).toFixed(1)}%)
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
        {individual.map((gene, i) => (
          <div
            key={i}
            style={{
              width: 24, height: 24, borderRadius: 4,
              background: gene ? '#a6e3a1' : '#313244',
              border: '1px solid #45475a',
              transition: 'background 0.2s',
            }}
          />
        ))}
      </div>
    </div>
  );
}

function Placeholder({ text }: { text: string }) {
  return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#45475a', fontSize: 14 }}>{text}</div>;
}
```

- [ ] **Step 2: Create `src/problems/NQueensBoard.tsx`**

```tsx
interface Props {
  individual: number[];  // individual[row] = column index
}

function hasConflict(individual: number[], row: number): boolean {
  const col = individual[row];
  for (let r = 0; r < individual.length; r++) {
    if (r === row) continue;
    if (individual[r] === col) return true;
    if (Math.abs(individual[r] - col) === Math.abs(r - row)) return true;
  }
  return false;
}

export function NQueensBoard({ individual }: Props) {
  const n = individual.length;
  if (n === 0) return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#45475a', fontSize: 14 }}>Waiting for data...</div>;

  const cellSize = Math.min(48, Math.floor(400 / n));
  const conflicts = individual.filter((_, i) => hasConflict(individual, i)).length;

  return (
    <div style={{ padding: 16 }}>
      <div style={{ marginBottom: 12, fontSize: 13, color: '#a6adc8' }}>
        Conflicts: <span style={{ color: conflicts === 0 ? '#a6e3a1' : '#f38ba8' }}>{conflicts}</span>
        {conflicts === 0 && ' ✓ Solved!'}
      </div>
      <div style={{ display: 'inline-block', border: '2px solid #45475a', borderRadius: 4 }}>
        {Array.from({ length: n }, (_, row) => (
          <div key={row} style={{ display: 'flex' }}>
            {Array.from({ length: n }, (_, col) => {
              const isQueen = individual[row] === col;
              const isConflict = isQueen && hasConflict(individual, row);
              const isDark = (row + col) % 2 === 1;
              return (
                <div
                  key={col}
                  style={{
                    width: cellSize, height: cellSize,
                    background: isConflict ? '#f38ba840' : isDark ? '#313244' : '#45475a',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: cellSize * 0.6, lineHeight: 1,
                  }}
                >
                  {isQueen ? '♛' : ''}
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}
```

- [ ] **Step 3: Create `src/problems/TSPMap.tsx`**

The cities are generated server-side with a fixed seed, so we need the frontend to regenerate the same city layout. The server uses `Random(42)` for default cities — replicate that deterministic layout using a seeded RNG.

```tsx
interface Props {
  individual: number[];  // city visit order
  cityCount: number;
}

function seededCities(count: number): { x: number; y: number }[] {
  // Mirrors the Java server's default city generation: Random(42), nextDouble()*100
  let seed = 42;
  const next = () => {
    seed = (seed * 1664525 + 1013904223) & 0xffffffff;
    return (seed >>> 0) / 0x100000000;
  };
  return Array.from({ length: count }, () => ({ x: next() * 100, y: next() * 100 }));
}

export function TSPMap({ individual, cityCount }: Props) {
  if (individual.length === 0) return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#45475a', fontSize: 14 }}>Waiting for data...</div>;

  const cities = seededCities(cityCount);
  const W = 400, H = 320, PAD = 20;

  const sx = (x: number) => PAD + (x / 100) * (W - 2 * PAD);
  const sy = (y: number) => PAD + (y / 100) * (H - 2 * PAD);

  const route = [...individual, individual[0]];
  const points = route.map((i) => `${sx(cities[i].x)},${sy(cities[i].y)}`).join(' ');

  let totalDist = 0;
  for (let i = 0; i < individual.length; i++) {
    const a = cities[individual[i]], b = cities[individual[(i + 1) % individual.length]];
    totalDist += Math.sqrt((a.x - b.x) ** 2 + (a.y - b.y) ** 2);
  }

  return (
    <div style={{ padding: 16 }}>
      <div style={{ marginBottom: 8, fontSize: 13, color: '#a6adc8' }}>
        Route distance: <span style={{ color: '#89b4fa' }}>{totalDist.toFixed(1)}</span>
      </div>
      <svg width={W} height={H} style={{ background: '#1e1e2e', borderRadius: 8 }}>
        <polyline points={points} fill="none" stroke="#a6e3a1" strokeWidth={1.5} opacity={0.8} />
        {cities.map((c, i) => (
          <g key={i}>
            <circle cx={sx(c.x)} cy={sy(c.y)} r={5} fill="#89b4fa" />
            <text x={sx(c.x) + 7} y={sy(c.y) + 4} fill="#a6adc8" fontSize={10}>{i}</text>
          </g>
        ))}
      </svg>
    </div>
  );
}
```

- [ ] **Step 4: Create `src/problems/FunctionSurface.tsx`**

```tsx
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, ReferenceLine } from 'recharts';

interface Props {
  individual: number[];  // [x] or [x, y] — use first dimension for 2D chart
  bestFitness: number;   // = -f(best_x) so f(best_x) = -bestFitness
}

export function FunctionSurface({ individual, bestFitness }: Props) {
  const bestX = individual[0] ?? 0;
  const fValue = -bestFitness;

  const curveData = Array.from({ length: 100 }, (_, i) => {
    const x = -5 + (i / 99) * 10;
    return { x: parseFloat(x.toFixed(2)), y: parseFloat((x * x + (individual[1] ?? 0) ** 2).toFixed(4)) };
  });

  return (
    <div style={{ padding: 16 }}>
      <div style={{ marginBottom: 8, fontSize: 13, color: '#a6adc8' }}>
        Best x₁: <span style={{ color: '#89b4fa' }}>{bestX.toFixed(4)}</span>
        {individual[1] !== undefined && <> | x₂: <span style={{ color: '#89b4fa' }}>{individual[1].toFixed(4)}</span></>}
        {' '}— f(x): <span style={{ color: '#a6e3a1' }}>{fValue.toFixed(6)}</span>
      </div>
      <ResponsiveContainer width="100%" height={280}>
        <LineChart data={curveData}>
          <XAxis dataKey="x" stroke="#45475a" tick={{ fill: '#a6adc8', fontSize: 11 }} />
          <YAxis stroke="#45475a" tick={{ fill: '#a6adc8', fontSize: 11 }} />
          <Tooltip contentStyle={{ background: '#313244', border: 'none', fontSize: 12 }} />
          <Line type="monotone" dataKey="y" stroke="#89b4fa" dot={false} strokeWidth={2} name="f(x)" isAnimationActive={false} />
          <ReferenceLine x={bestX} stroke="#a6e3a1" strokeWidth={2} label={{ value: 'best', fill: '#a6e3a1', fontSize: 11 }} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
```

- [ ] **Step 5: Commit**

```bash
git add src/problems/
git commit -m "feat: add problem-specific visualisations (OneMax, NQueens, TSP, FunctionSurface)"
```

---

## Task 6: Main Panel & Final Wiring

**Files:**
- Create: `src/components/MainPanel.tsx`
- Modify: `src/App.tsx`

- [ ] **Step 1: Create `src/components/MainPanel.tsx`**

```tsx
import type { EvolutionStatus, ProblemId } from '../types';
import { FitnessChart } from './FitnessChart';
import { OneMaxGrid } from '../problems/OneMaxGrid';
import { NQueensBoard } from '../problems/NQueensBoard';
import { TSPMap } from '../problems/TSPMap';
import { FunctionSurface } from '../problems/FunctionSurface';

interface Props {
  problem: ProblemId;
  status: EvolutionStatus;
  bestIndividual: number[];
  bestFitness: number;
  fitnessHistory: { gen: number; best: number; avg: number }[];
  cityCount: number;
  errorMessage: string | null;
}

function ProblemView({ problem, bestIndividual, bestFitness, cityCount }: Omit<Props, 'status' | 'fitnessHistory' | 'errorMessage'>) {
  switch (problem) {
    case 'onemax':   return <OneMaxGrid individual={bestIndividual} />;
    case 'nqueens':  return <NQueensBoard individual={bestIndividual} />;
    case 'tsp':      return <TSPMap individual={bestIndividual} cityCount={cityCount} />;
    case 'function': return <FunctionSurface individual={bestIndividual} bestFitness={bestFitness} />;
  }
}

export function MainPanel({ problem, status, bestIndividual, bestFitness, fitnessHistory, cityCount, errorMessage }: Props) {
  return (
    <main style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#181825', overflow: 'hidden' }}>
      {errorMessage && (
        <div style={{ background: '#f38ba820', color: '#f38ba8', padding: '8px 16px', fontSize: 13 }}>
          Error: {errorMessage}
        </div>
      )}
      <div style={{ flex: 2, borderBottom: '1px solid #313244', overflow: 'auto' }}>
        <ProblemView
          problem={problem}
          bestIndividual={bestIndividual}
          bestFitness={bestFitness}
          cityCount={cityCount}
        />
      </div>
      <div style={{ flex: 1, padding: 16 }}>
        <FitnessChart history={fitnessHistory} />
      </div>
    </main>
  );
}
```

- [ ] **Step 2: Update `src/App.tsx` to wire everything together**

```tsx
import { useState } from 'react';
import type { EvolutionParams, ProblemId } from './types';
import { useEvolution } from './hooks/useEvolution';
import { Sidebar } from './components/Sidebar';
import { MainPanel } from './components/MainPanel';
import './App.css';

export default function App() {
  const evolution = useEvolution();
  const [activeProblem, setActiveProblem] = useState<ProblemId>('onemax');
  const [cityCount, setCityCount] = useState(10);

  const handleStart = (problem: ProblemId, params: EvolutionParams) => {
    setActiveProblem(problem);
    if (problem === 'tsp') setCityCount(params.size);
    evolution.start(problem, params);
  };

  return (
    <div style={{ display: 'flex', height: '100vh', fontFamily: 'system-ui, sans-serif' }}>
      <Sidebar
        status={evolution.status}
        generation={evolution.generation}
        bestFitness={evolution.bestFitness}
        onStart={handleStart}
        onPause={evolution.pause}
        onResume={evolution.resume}
        onStop={evolution.stop}
      />
      <MainPanel
        problem={activeProblem}
        status={evolution.status}
        bestIndividual={evolution.bestIndividual}
        bestFitness={evolution.bestFitness}
        fitnessHistory={evolution.fitnessHistory}
        cityCount={cityCount}
        errorMessage={evolution.errorMessage}
      />
    </div>
  );
}
```

- [ ] **Step 3: Replace `src/App.css` with minimal global reset**

```css
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  overflow: hidden;
}
```

- [ ] **Step 4: End-to-end test — run the full app against the backend**

Start the backend:
```bash
# In evolutionary-algorithms repo:
java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar
```

Start the frontend:
```bash
npm run dev
```

Open `http://localhost:5173`. Test each problem:
1. Select **OneMax** → Run → verify gene grid updates each generation, fitness chart rises
2. Select **N-Queens** → Run → verify chessboard updates, conflicts count decreases
3. Select **TSP** → Run → verify route redraws, distance decreases
4. Select **Function Optimisation** → Run → verify reference line moves toward x=0

- [ ] **Step 5: Build for production and verify no TypeScript errors**

```bash
npm run build
```

Expected: `dist/` folder created, zero TypeScript errors

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: wire MainPanel and complete full application"
```

---

## Notes

- **Backend required:** The server must be running before the frontend can connect. Start with `java -jar server/target/evolutionary-algorithms-server-1.0.0-SNAPSHOT.jar`.
- **TSP city layout:** The `seededCities` function in `TSPMap.tsx` mirrors the Java server's `Random(42)` default city generation. If the server's city generation changes, update this function to match.
- **Adding a new problem:** Add a `ProblemId` variant to `types.ts`, add defaults to `ParameterPanel.tsx`, add a visualisation component to `src/problems/`, add a case in `MainPanel.tsx`'s `ProblemView`.
- **CORS:** The Javalin server is configured with `anyHost()` for local development. Restrict this if deploying publicly.
