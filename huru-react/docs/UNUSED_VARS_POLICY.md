# Unused Variable / Import Policy — Huru Bazar Frontend

## Problem
`react-scripts` (CRA5) runs two independent checkers on every file:
TypeScript (`fork-ts-checker-webpack-plugin`) and ESLint. With
`noUnusedLocals`/`noUnusedParameters` on in `tsconfig.json`, tsc treated
every unused variable as a **hard compile error** with no way to mark
something as "intentionally unused for now" — which blocked `npm start`
and `npm run build` during active MVP development.

## Decision
- **tsc**: enforces type safety only. Unused-var checks removed from
  `tsconfig.json`.
- **ESLint** (`eslint-plugin-unused-imports`): owns unused-var/import
  enforcement, because it supports:
  - `warn` severity for unused **variables/params** (non-blocking)
  - `error` severity for unused **imports** (these are always dead code —
    CRA5's automatic JSX runtime means `import React from 'react'` is
    never required, so these should simply be removed)
  - a `^_` ignore pattern for variables genuinely staged for near-term
    work

## Conventions
| Situation | What to do |
|---|---|
| Import is genuinely dead code | Delete it. Never suppress. |
| Variable/state/function is staged for a near-term feature | Prefix with `_` (e.g. `_accountStatus`) and add a `// TODO(<ticket-or-feature>): ...` comment |
| A prop/type mismatch (e.g. `TS2322`) | Fix the type — never suppress. These are real bugs. |

## Guardrail against regression
A pre-existing warning count doesn't get worse silently: CI runs
`scripts/check-lint-budget.js`, which fails the build only if the
warning count **increases** beyond the committed
`.eslint-warning-budget.json`. Cleaning up debt lowers the budget
(`--update` flag); nothing can raise it without a reviewer seeing it
in the diff.

## One-time setup
```bash
npm install
npm run fix:react-imports -- --write   # safe, mechanical, review the diff
npm run lint:report
node scripts/check-lint-budget.js --update
git add .eslint-warning-budget.json
git commit -m "chore: establish lint warning budget baseline"
```

## Local dev
```bash
npm start   # CI=false — warnings visible in terminal, do not block hot reload
```

## Board / demo builds
```bash
npm run build          # CI=false, same as above — safe for a live demo
npm run build:strict    # CI=true — treats warnings as errors; run this
                         # occasionally to see the full, unfiltered picture
```
