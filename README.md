# Kenat Java

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/build-Gradle-green.svg)](lib/build.gradle.kts)

A modern **Java 21** library for Ethiopian calendar (Ge'ez / Ethiopic) arithmetic, formatting, holiday and fasting calculations.

> This is an independent Java rewrite of the original [**Kenat**](https://github.com/MelakuDemeke/kenat) TypeScript library by [Melaku Demeke](https://github.com/MelakuDemeke), built from scratch using idiomatic Java — not a line-by-line translation.

---

## Features

- 🗓 **Ethiopian ↔ Gregorian conversion** — accurate multi-century date math
- 📅 **Month & year grid generation** — 7-column calendar grid with holidays and padding
- 🕰 **Ethiopian time** — 12-hour Ethiopian clock ↔ 24h Gregorian; add/subtract/diff
- 🔢 **Ge'ez numerals** — integers ↔ Ethiopic script (supports 0–99,999,999)
- 📖 **Localized output** — Amharic and English month/weekday names
- 🎉 **Holiday lookup** — fixed, movable Christian, and Islamic holidays
- 🙏 **Fasting periods** — Abiy Tsome, Tsome Hawaryat, Nineveh, Tsome Nebiyat, Filseta, Ramadan, Tsome Dihenet
- 📐 **Date arithmetic** — add/subtract days/months/years; full diff breakdown
- ✅ **Bahire Hasab** — complete movable feast algorithm (Enkutatash, Timkat, Easter, Pentecost, etc.)
- 🧩 **Clean API** — Java 21 records, `java.time` integration, immutable types, no external dependencies

---

## Installation

### Gradle (Kotlin DSL)

Build locally and consume via `mavenLocal()`:

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
import io.github.henaorth16.kenat.*;

// Today's Ethiopian date
Kenat today = Kenat.now();
System.out.println(today.format());                      // "ሐምሌ 15 2016"
System.out.println(today.format("english"));             // "Hamle 15 2016"
System.out.println(today.formatShort());                 // "2016/07/15"
System.out.println(today.formatInGeezAmharic());         // "ሐምሌ ፲፭ ፳፻፲፮"

// Convert Ethiopian → Gregorian
EthiopianDate eth = new EthiopianDate(2016, 3, 15);      // Hidar 15, 2016 EC
GregorianDate  greg = Conversions.toGC(eth);
System.out.println(greg.toLocalDate());                  // 2023-11-25

// Convert Gregorian → Ethiopian
EthiopianDate back = Conversions.toEC(LocalDate.of(2023, 11, 25));
System.out.println(back);                                // 2016/03/15

// Date arithmetic — immutable, chainable
Kenat nextMonth = Kenat.now().add(1, "months");
Kenat lastWeek  = Kenat.now().subtract(7, "days");

// Diff
Kenat a = new Kenat("2016/1/1");
Kenat b = new Kenat("2017/3/15");
System.out.println(a.diffInDays(b));   // -434
System.out.println(a.diffInYears(b));  // -1
```

---

## API Overview

### `Kenat` — main facade

```java
// Constructors
new Kenat()                              // today
new Kenat("2016/1/1")                    // from Ethiopian date string (yyyy/mm/dd or yyyy-mm-dd)
new Kenat(new EthiopianDate(2016, 1, 1)) // from record
new Kenat(LocalDate.of(2023, 9, 12))     // from Gregorian LocalDate
Kenat.now()                              // static factory

// Getters
k.getEthiopian()    // EthiopianDate
k.getGregorian()    // GregorianDate
k.getTime()         // EthiopianTime

// Formatting
k.format()                                        // "መስከረም 1 2016" (Amharic)
k.format("english")                               // "Meskerem 1 2016"
k.format("ethiopian","amharic", true, true, true) // weekday + Geez + time
k.formatShort()                                   // "2016/01/01"
k.formatInGeezAmharic()                           // "መስከረም ፩ ፳፻፲፮"
k.formatWithWeekday("amharic", false)
k.toISOString()                                   // "2016-01-01T12:00" (Ethiopian)
k.toISOString("gregorian")                        // "2023-09-12T06:00" (true ISO 8601)
k.toString()                                      // "መስከረም 1 2016 12:00 ጠዋት"

// Arithmetic (all return new Kenat)
k.add(3, "days")   .add(1, "months").add(2, "years")
k.subtract(1, "months")
k.addDays(n)  .addMonths(n)  .addYears(n)

// Diff
k.diffInDays(other)    // int
k.diffInMonths(other)  // int
k.diffInYears(other)   // int

// Comparison
k.isBefore(other)  .isAfter(other)
k.isSameDay(other) .isSameMonth(other) .isSameYear(other)

// Boundaries
k.startOf("month")  .endOf("month")
k.startOf("year")   .endOf("year")

// Bahire Hasab
BahireHasab.BahireHasabResult bh = k.getBahireHasab();
bh.movableFeasts().get("fasika");    // Easter this year

// Holiday check
List<Holiday> holidays = k.isHoliday();  // empty if not a holiday

// Calendar grids (static)
Kenat.getMonthGrid(2016, 1)              // MonthGridResult
Kenat.getYearCalendar(2016)              // List<MonthGridResult> (13 months)
Kenat.generateDateRange(start, end)      // List<Kenat>
```

### Conversions

```java
GregorianDate g = Conversions.toGC(2016, 1, 1);
EthiopianDate e = Conversions.toEC(2023, 9, 12);
LocalDate     d = Conversions.toGCDate(2016, 1, 1);
EthiopianDate e2 = Conversions.toEC(LocalDate.now());
```

### DayArithmetic

```java
EthiopianDate d  = new EthiopianDate(2016, 1, 1);
EthiopianDate d2 = DayArithmetic.addDays(d, 45);
EthiopianDate d3 = DayArithmetic.addMonths(d, 3);
int days         = DayArithmetic.diffInDays(d, d2);
DiffBreakdown br = DayArithmetic.diffBreakdown(d, d2, null); // all units
br.yearsOpt();   // OptionalInt
br.monthsOpt();
br.daysOpt();
```

### GeezConverter

```java
String geez = GeezConverter.toGeez(2016);   // "፳፻፲፮"
int    n    = GeezConverter.toArabic("፳፻"); // 2000
```

### EthiopianTime

```java
EthiopianTime t  = new EthiopianTime(6, 30, TimePeriod.DAY);
EthiopianTime t2 = EthiopianTime.fromGregorian(12, 30);  // 12:30 Gregorian → 6:30 Ethiopian day
EthiopianTime t3 = EthiopianTime.fromString("6:30 night");
t.toGregorian();         // int[]{18, 30}
t.add(2, 15);            // +2h15m → new EthiopianTime
t.format();              // "፮:፴ ጠዋት"
t.format("english", false, true, false);  // "06:30 day"
```

### Holidays

```java
Holiday h = Holidays.getHoliday("fasika", 2016);
List<Holiday> month  = Holidays.getHolidaysInMonth(2016, 1, "amharic");
List<Holiday> year   = Holidays.getHolidaysForYear(2016, "english");
// Filter by tag
List<Holiday> pub = Holidays.getHolidaysForYear(2016, "amharic",
                        List.of(HolidayTags.PUBLIC));
```

### Fasting

```java
Optional<DateRange>          period = Fasting.getFastingPeriod(FastingKeys.ABIY_TSOME, 2016);
Optional<Fasting.FastingInfoResult> info = Fasting.getFastingInfo(FastingKeys.RAMADAN, 2016, "english");
List<Integer> days = Fasting.getFastingDays(FastingKeys.TSOME_DIHENET, 2016, 3);
```

### BahireHasab

```java
var bh = BahireHasab.getBahireHasab(2016);
System.out.println(bh.ameteAlem());          // 7516
System.out.println(bh.evangelistName());     // "ዮሐንስ"
System.out.println(bh.ninevehDate());        // EthiopianDate
bh.movableFeasts().forEach((key, h) ->
    System.out.println(key + ": " + h.ethiopian()));
```

### MonthGrid

```java
var config = new MonthGrid.MonthGridConfig(2016, 1, 1, false, "amharic", null, "public", false);
MonthGrid.MonthGridResult grid = MonthGrid.create(config);
grid.headers();     // ["ሰኞ", "ማክሰኞ", ...]
grid.days();        // List<MonthGridDay | null> — null = padding
grid.monthName();   // "መስከረም"
```

---

## Core Types

| Type | Description |
|------|-------------|
| `EthiopianDate` | Immutable record — Ethiopian year, month (1–13), day |
| `GregorianDate` | Immutable record — Gregorian year/month/day with `toLocalDate()` |
| `EthiopianTime` | Mutable value — hour 1–12, minute, `TimePeriod` |
| `LocalizedText` | Amharic + English + optional extras map |
| `Holiday` | Key, tags, movable flag, name, description, Ethiopian + Gregorian dates |
| `DateRange` | Inclusive Ethiopian date range (start, end) |
| `DiffBreakdown` | sign, totalDays, optional years/months/days |
| `Language` | Enum: `AMHARIC`, `ENGLISH` |
| `TimePeriod` | Enum: `DAY`, `NIGHT` |
| `DiffUnit` | Enum: `YEARS`, `MONTHS`, `DAYS` |

## Exceptions

All extend `KenatException` (unchecked):

| Exception | When thrown |
|-----------|-------------|
| `InvalidEthiopianDateException` | Out-of-range Ethiopian date |
| `InvalidGregorianDateException` | Out-of-range Gregorian date |
| `InvalidDateFormatException` | Malformed date string |
| `UnrecognizedInputException` | Unknown enum key or identifier |
| `GeezConverterException` | Invalid Ge'ez numeral input |
| `InvalidInputTypeException` | Wrong argument type |
| `InvalidTimeException` | Invalid Ethiopian time values |
| `InvalidGridConfigException` | Bad month-grid configuration |
| `UnknownHolidayException` | Unrecognised holiday key |

---

## Project Structure

```
kenat-java/
├── lib/
│   └── src/
│       ├── main/java/io/github/henaorth16/kenat/
│       │   ├── Kenat.java                   ← main facade
│       │   ├── Conversions.java
│       │   ├── DayArithmetic.java
│       │   ├── EthiopianCalendarUtils.java
│       │   ├── GeezConverter.java
│       │   ├── EthiopianTime.java
│       │   ├── Formatting.java
│       │   ├── BahireHasab.java
│       │   ├── Holidays.java
│       │   ├── Fasting.java
│       │   ├── MonthGrid.java
│       │   ├── EthiopianDate.java
│       │   ├── GregorianDate.java
│       │   ├── LocalizedText.java
│       │   ├── Holiday.java
│       │   ├── DateRange.java
│       │   ├── DiffBreakdown.java
│       │   ├── DiffUnit.java
│       │   ├── Language.java
│       │   ├── TimePeriod.java
│       │   ├── HolidayTags.java
│       │   ├── FastingKeys.java
│       │   ├── KenatConstants.java
│       │   └── exception/           ← 10 typed exception classes
│       └── test/java/io/github/henaorth16/kenat/
│           ├── TypesAndExceptionsTest.java  (92 tests ✅)
│           └── ConstantsTest.java
├── docs/
│   ├── AGENT_CONTEXT.md
│   ├── MIGRATION.md
│   ├── ARCHITECTURE.md
│   └── ...
├── gradle/
├── lib/build.gradle.kts
├── settings.gradle.kts
├── AGENTS.md
└── README.md
```

---

## Development & Contribution

### Prerequisites

- Java 21+
- No Gradle install needed — `./gradlew` handles everything

### Build & Test

```bash
./gradlew test                  # run all JUnit 5 tests
./gradlew compileJava           # compile only
./gradlew javadoc               # generate Javadoc
./gradlew publishToMavenLocal   # install to local Maven repo
```

### Contributing

1. Fork and create a feature branch off `main`.
2. All code goes under `io.github.henaorth16.kenat`.
3. Write JUnit 5 tests for every new class — mirror the TypeScript test fixtures.
4. Keep types immutable (prefer Java records).
5. Use `java.time` in public APIs; no external dependencies.
6. Update `docs/MIGRATION.md` status after each module.
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
