# AGENTS.md — Kenat Java Migration

Guidance for Cursor agents (and other AI assistants) working on the Java port of [Kenat](https://github.com/MelakuDemeke/kenat).

## What this is

**Kenat** (Amharic: ቀናት) is an Ethiopian calendar library. The authoritative implementation lives in **`../kenat`** (TypeScript, npm package `kenat` v4.x). This repository (**`kenat_java`**) is an idiomatic **Java 21** rewrite—not a line-by-line translation.

## Hard rules

| Rule | Detail |
|------|--------|
| **Never modify `../kenat`** | Read-only source of truth. No edits, no formatting, no test fixes. |
| **All code goes in `kenat_java`** | Implementation, tests, docs, build config. |
| **Idiomatic Java** | Use records, sealed types, `java.time` where appropriate, clear packages—not TS patterns copied verbatim. |
| **Java 21** | Toolchain target; prefer modern APIs (pattern matching, records, `Optional`, etc.). |
| **SOLID** | Small focused types; pure functions for date math; thin façade for the main API. |
| **API parity** | Keep public names and behavior close to the TS library unless Java offers a clearly better design (document deviations). |
| **Update docs when done** | After each task, update this file and/or `docs/MIGRATION.md` (status, decisions, next steps). |

## Repository layout

```
kenat/                  ← TypeScript source (DO NOT EDIT)
kenat_java/
├── AGENTS.md           ← you are here
├── docs/
│   └── MIGRATION.md    ← module map, API map, progress checklist
├── lib/
│   └── src/
│       ├── main/java/io/github/melakudemeke/kenat/   ← library (target)
│       └── test/java/io/github/melakudemeke/kenat/   ← tests (mirror ../kenat/tests)
├── settings.gradle.kts
└── gradle/
```

**Target package:** `io.github.henaorth16.kenat`

Gradle multi-project: single module `lib` (may split later if size warrants).

## Commands

```bash
cd kenat_java
./gradlew test          # run all JUnit 5 tests
./gradlew build         # compile + test + jars
./gradlew javadoc       # API docs (when public API exists)
```

Reference TS tests (read-only):

```bash
cd ../kenat
npm test
npx jest tests/conversions.test.ts   # single file
```

## Architecture (TypeScript → Java)

The TS codebase is layered bottom-up. Preserve the same **dependency direction** in Java.

```
constants          → static data (month names, holiday tags, Bahire Hasab tables)
utils              → validation, leap year, weekday, day-of-year
conversions        → toEC / toGC (+ Hijri helpers)
dayArithmetic      → add/diff on plain Ethiopian dates
bahireHasab        → movable feast algorithm (Nineveh / tewsak)
holidays           → fixed + movable holidays, filtering
fasting            → Orthodox fasts + Ramadan
formatting         → localized string output
geezConverter      → Arabic ↔ Geez numerals
Time               → Ethiopian 12-hour day/night time
MonthGrid          → calendar grid builder (+ nigs.json for Christian mode)
Kenat              → immutable façade (date + time, delegates to modules)
errors             → typed exceptions (KenatError hierarchy)
```

**Key invariant (from TS):** Core modules operate on plain Ethiopian date values `{year, month, day}`, not on `Kenat` instances. `Kenat` is a thin immutable wrapper.

See `../kenat/CLAUDE.md` for deeper TS architecture notes.

## Java design choices (planned)

Document here when implemented; justify any API difference.

### 1. Date value types → Java records

```java
public record EthiopianDate(int year, int month, int day) {}
public record GregorianDate(int year, int month, int day) {}
```

Immutable, value-based equality—matches TS interfaces without mutable POJOs.

### 2. `java.time` for Gregorian side only

Use `LocalDate` internally for Gregorian math where it simplifies code. Ethiopian calendar has no `java.time` equivalent; keep custom conversion/arithmetic in dedicated classes (`EthiopianCalendar`, `Conversions`).

**Do not** force Ethiopian dates into `LocalDate`—that would be incorrect.

### 3. Main entry: `Kenat` class

Mirror TS: construct from `LocalDate`, Ethiopian string, `EthiopianDate`, or “today”. Instance methods return **new** `Kenat` instances (immutable, chainable).

Static utilities (`toEC`, `toGC`, `getHolidaysForYear`, …) live in dedicated classes or as static methods on `Kenat` to match TS named exports—prefer **`Kenat` + top-level utility classes** in the same package for discoverability.

### 4. Exceptions instead of error objects

TS errors implement `toJSON()`. Java: unchecked exception hierarchy rooted at `KenatException` with structured fields (`InvalidEthiopianDateException`, etc.). No `toJSON()` unless we add a separate DTO for serialization.

### 5. JSON data (`nigs.json`)

Copy into `lib/src/main/resources/kenat/nigs.json` (read-only at runtime). Load via classpath resource stream—not a dependency on `../kenat` at runtime.

### 6. No runtime dependencies

Match TS “zero dependencies” goal. JUnit for tests; add AssertJ when porting real tests.

### 7. Naming

Keep TS public names where reasonable: `toEC`, `toGC`, `getBahireHasab`, `MonthGrid`, `HolidayTags`. Java style for types: `DiffBreakdown`, `TimePeriod`.

## Public API surface (from `../kenat/src/index.ts`)

**Default export:** `Kenat` class

**Named exports to port:**

| TS export | Java target |
|-----------|-------------|
| `Kenat` | `Kenat` |
| `toEC`, `toGC` | `Conversions.toEC()`, `Conversions.toGC()` (or static on `Kenat`) |
| `toGeez`, `toArabic` | `GeezConverter` |
| `getHolidaysInMonth`, `getHolidaysForYear`, `getHoliday` | `Holidays` |
| `getBahireHasab` | `BahireHasab` |
| `getFastingPeriod`, `getFastingInfo`, `getFastingDays` | `Fasting` |
| `MonthGrid` | `MonthGrid` |
| `Time` | `EthiopianTime` or `Time` |
| `HolidayTags`, `HolidayNames`, `FastingKeys`, `monthNames` | constants classes / enums |
| `diffBreakdown` | `DayArithmetic.diffBreakdown()` |

Types: see `docs/MIGRATION.md` for the full type mapping.

## Test strategy

1. Port regression tests from `../kenat/tests/*.test.ts` to JUnit 5 + AssertJ.
2. **Bahire Hasab / holiday tests are fixtures**—expected dates for specific Ethiopian years must match TS exactly.
3. One Java test class per TS test file where possible (`ConversionsTest` ← `conversions.test.ts`).
4. Do not modify TS tests to make Java pass—fix Java.

## Migration status

**Phase:** Bootstrap (Gradle scaffold only; no Kenat implementation yet)

| Module | Status | Notes |
|--------|--------|-------|
| Gradle / Java 21 | Done | `settings.gradle.kts` fixed; placeholder `Library` still present |
| Package structure | Not started | Replace `org.example` with `io.github.henaorth16.kenat` |
| constants | Not started | |
| utils | Not started | |
| conversions | Not started | Start here—foundation for everything else |
| dayArithmetic | Not started | |
| errors | Not started | |
| bahireHasab | Not started | |
| holidays | Not started | |
| fasting | Not started | |
| formatting | Not started | |
| geezConverter | Not started | |
| Time | Not started | |
| MonthGrid + nigs | Not started | |
| Kenat | Not started | |
| Tests ported | Not started | |

Update this table after every task.

## Suggested implementation order

1. `errors` + `EthiopianDate` / `GregorianDate` records  
2. `constants`  
3. `utils` (validation, leap year)  
4. `conversions` + tests  
5. `dayArithmetic` + tests  
6. `geezConverter` + tests  
7. `formatting`  
8. `bahireHasab` + tests (critical regression fixtures)  
9. `holidays` + `fasting` + tests  
10. `Time`  
11. `MonthGrid` (+ resource copy of `nigs.json`)  
12. `Kenat` façade + integration tests  

## Publishing (future)

Group ID: `io.github.henaorth16`. Maven Central publishing (Dokka, signing) deferred until API stabilizes. A draft publishing block was previously misplaced in `settings.gradle.kts`—belongs in `lib/build.gradle.kts` when ready.

## Related files

- `../kenat/CLAUDE.md` — TS agent guide (architecture, commands, invariants)
- `../kenat/README.md` — user-facing API examples
- `docs/MIGRATION.md` — detailed mapping and checklist
- `docs/DEPENDENCY_GRAPH.md` — module imports, call hierarchy, init order
- `docs/DEPENDENCY_ANALYSIS.md` — madge-verified cycles, fan-in/out, port order
- `docs/EXECUTION_FLOW.md` — runtime flows, build/release pipeline
- `docs/import-graph.dot` — Graphviz graph (render with `dot -Tsvg`)
