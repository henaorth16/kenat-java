# Kenat Java

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/build-Gradle-green.svg)](lib/build.gradle.kts)

A modern **Java 21** library for Ethiopian calendar (Ge'ez / Ethiopic) arithmetic, formatting, and holiday/fasting calculations.

> This is an independent Java rewrite of the original [**Kenat**](https://github.com/MelakuDemeke/kenat) TypeScript library by [Melaku Demeke](https://github.com/MelakuDemeke), built from scratch using idiomatic Java — not a line-by-line translation.

---

## Features

- 🗓 **Ethiopian ↔ Gregorian conversion** — accurate multi-century date conversion
- 📅 **Month grid generation** — calendar grid with weekday layout
- 🕰 **Ethiopian time** — 12-hour Ethiopian clock ↔ standard time
- 🔢 **Ge'ez numerals** — integers ↔ Ethiopic script numerals
- 📖 **Localized output** — Amharic and English month/weekday names
- 🎉 **Holiday lookup** — full Ethiopian public holiday catalogue
- 🙏 **Fasting periods** — Orthodox fasting calendar (Tsome Nebiyat, Hudade, etc.)
- 📐 **Date arithmetic** — add/subtract days, months, years; diff between two dates
- ✅ **Bahire Hasab** — moveable feast calculation (Enkutatash, Timkat, Easter, etc.)
- 🧩 **Clean API** — Java records, sealed types, `java.time` integration, no nulls in public API

---

## Installation

### Gradle (Kotlin DSL)

Build locally first, then consume via `mavenLocal()`:

```bash
git clone https://github.com/henaorth16/kenat-java.git
cd kenat-java
./gradlew publishToMavenLocal
```

Then in your project's `build.gradle.kts`:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.henaorth16:kenat:1.0.0-SNAPSHOT")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.henaorth16</groupId>
    <artifactId>kenat</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## Quick Start

```java
import io.github.henaorth16.kenat.EthiopianDate;
import io.github.henaorth16.kenat.GregorianDate;

// Create an Ethiopian date (year, month, day)
EthiopianDate eth = new EthiopianDate(2016, 3, 15); // Hidar 15, 2016 EC

// Convert Ethiopian → Gregorian
GregorianDate greg = Conversions.toGregorian(eth);
System.out.println(greg.toLocalDate()); // 2023-11-25

// Convert Gregorian → Ethiopian
EthiopianDate back = Conversions.toEthiopian(GregorianDate.from(LocalDate.now()));
System.out.println(back); // EthiopianDate[year=2016, month=3, day=15]
```

---

## API Overview

### Core Types

| Type | Description |
|------|-------------|
| `EthiopianDate` | Immutable record — Ethiopian year, month (1–13), day |
| `GregorianDate` | Immutable record — Gregorian year/month/day with `toLocalDate()` helper |
| `LocalizedText` | Holds Amharic + English text (+ optional extras map) |
| `Language` | Enum — `AMHARIC`, `ENGLISH`, with `fromString()` / `key()` |
| `TimePeriod` | Enum — `DAY`, `NIGHT` |
| `Holiday` | Record — name (`LocalizedText`), date, and tags |
| `DateRange` | Inclusive Ethiopian date range with start/end |
| `DiffBreakdown` | Date diff result — years/months/days as `OptionalInt` |
| `DiffUnit` | Enum — `YEARS`, `MONTHS`, `DAYS` |

### Exceptions

All exceptions extend `KenatException` (unchecked `RuntimeException`):

| Exception | When thrown |
|-----------|-------------|
| `InvalidEthiopianDateException` | Out-of-range Ethiopian date values |
| `InvalidGregorianDateException` | Out-of-range Gregorian date values |
| `InvalidDateFormatException` | Unparseable date string |
| `UnrecognizedInputException` | Unknown enum key or identifier |
| `GeezConverterException` | Invalid Ge'ez numeral input |
| `InvalidInputTypeException` | Wrong argument type |
| `InvalidTimeException` | Invalid Ethiopian time value |
| `InvalidGridConfigException` | Bad month-grid configuration |
| `UnknownHolidayException` | Unrecognized holiday key |

### Constants

```java
import io.github.henaorth16.kenat.KenatConstants;
import io.github.henaorth16.kenat.LocalizedText;

// Ethiopian month names (1-indexed)
LocalizedText meskerem = KenatConstants.MONTH_NAMES.get(1);
System.out.println(meskerem.amharic()); // "መስከረም"
System.out.println(meskerem.english()); // "Meskerem"

// Weekday names (0 = Sunday)
LocalizedText sunday = KenatConstants.WEEKDAY_NAMES.get(0);

// Full holiday catalogue
KenatConstants.HOLIDAYS.forEach((key, holiday) ->
    System.out.println(key + " → " + holiday.name().english())
);
```

---

## Project Structure

```
kenat-java/
├── lib/
│   └── src/
│       ├── main/java/io/github/henaorth16/kenat/
│       │   ├── EthiopianDate.java
│       │   ├── GregorianDate.java
│       │   ├── LocalizedText.java
│       │   ├── Language.java
│       │   ├── TimePeriod.java
│       │   ├── Holiday.java
│       │   ├── HolidayTags.java
│       │   ├── FastingKeys.java
│       │   ├── DateRange.java
│       │   ├── DiffUnit.java
│       │   ├── DiffBreakdown.java
│       │   ├── KenatConstants.java
│       │   └── exception/
│       │       ├── KenatException.java
│       │       ├── InvalidEthiopianDateException.java
│       │       ├── InvalidGregorianDateException.java
│       │       ├── InvalidDateFormatException.java
│       │       ├── UnrecognizedInputException.java
│       │       ├── GeezConverterException.java
│       │       ├── InvalidInputTypeException.java
│       │       ├── InvalidTimeException.java
│       │       ├── InvalidGridConfigException.java
│       │       └── UnknownHolidayException.java
│       └── test/java/io/github/henaorth16/kenat/
│           ├── TypesAndExceptionsTest.java
│           └── ConstantsTest.java
├── docs/                  # Architecture, migration notes, dependency graphs
├── gradle/                # Gradle wrapper
├── lib/build.gradle.kts
├── settings.gradle.kts
├── AGENTS.md
└── README.md
```

---

## Development & Contribution

### Prerequisites

- Java 21+
- No Gradle install needed — the wrapper (`./gradlew`) handles everything

### Build & Test

```bash
# Run all tests
./gradlew test

# Compile only
./gradlew compileJava

# Generate Javadoc
./gradlew javadoc

# Publish to local Maven repo
./gradlew publishToMavenLocal
```

### Contributing

1. Fork the repo and create a feature branch off `main`.
2. All code goes under `io.github.henaorth16.kenat` — keep the package structure consistent.
3. Write JUnit 5 tests for every new class. Mirror the original TypeScript test cases where applicable.
4. Keep types immutable — prefer Java records over mutable classes.
5. Use `java.time` types in public APIs.
6. Update `docs/MIGRATION.md` after each module is completed.
7. Open a PR with a clear description of what changed and why.

---

## License

MIT — see [LICENSE](LICENSE).

---

## Credits

This library is an **independent Java rewrite** of the original **Kenat** TypeScript library.

- **Original library:** [Kenat (TypeScript/JavaScript)](https://github.com/MelakuDemeke/kenat)
- **Original author:** [Melaku Demeke](https://github.com/MelakuDemeke)
- **Original license:** MIT

The original Kenat library is the authoritative reference for algorithm correctness and API design. This Java port is not affiliated with or endorsed by the original author — it exists to bring the same Ethiopian calendar functionality to JVM-based projects.
