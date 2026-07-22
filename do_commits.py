#!/usr/bin/env python3
import subprocess, sys, os

ROOT = "/home/hena/Documents/side projects/kenat/kenat-java"
os.chdir(ROOT)

def git(*args, check=True):
    r = subprocess.run(["git"] + list(args), capture_output=True, text=True)
    out = (r.stdout + r.stderr).strip()
    if out:
        print(out)
    if check and r.returncode != 0:
        print(f"FAILED: git {' '.join(args)}")
        sys.exit(1)
    return r

BASE = "lib/src/main/java/io/github/henaorth16/kenat"

# ── Commit 1: fix DateRange + DiffBreakdown ──────────────────────────────────
git("add", f"{BASE}/DateRange.java", f"{BASE}/DiffBreakdown.java")
git("commit", "-m",
    "fix: throw IllegalArgumentException for null DateRange fields\n\n"
    "DateRange compact constructor now throws IllegalArgumentException\n"
    "on null start/end (tests already asserted this; was NPE before).\n"
    "DiffBreakdown simplified back to 5-param record matching the test suite.")

# ── Commit 2: EthiopianCalendarUtils ────────────────────────────────────────
git("add", f"{BASE}/EthiopianCalendarUtils.java")
git("commit", "-m",
    "feat: implement EthiopianCalendarUtils (utils.ts port)\n\n"
    "Adds leap-year checks, days-in-month, dayOfYear helpers, getWeekday,\n"
    "isValidEthiopianDate, and Ethiopian New Year helpers.")

# ── Commit 3: Conversions ────────────────────────────────────────────────────
git("add", f"{BASE}/Conversions.java")
git("commit", "-m",
    "feat: implement Conversions — Ethiopian <-> Gregorian date math\n\n"
    "toGC() converts Ethiopian date to GregorianDate.\n"
    "toEC() converts Gregorian LocalDate or year/month/day to EthiopianDate.\n"
    "Validates input ranges and throws typed Kenat exceptions.")

# ── Commit 4: DayArithmetic ──────────────────────────────────────────────────
git("add", f"{BASE}/DayArithmetic.java")
git("commit", "-m",
    "feat: implement DayArithmetic — date add/subtract/diff\n\n"
    "addDays, addMonths, addYears with wrap-around and leap-year clamping.\n"
    "diffInDays, diffInMonths, diffInYears. diffBreakdown with optional units.")

# ── Commit 5: GeezConverter ──────────────────────────────────────────────────
git("add", f"{BASE}/GeezConverter.java")
git("commit", "-m",
    "feat: implement GeezConverter — Arabic <-> Ge'ez numeral conversion\n\n"
    "toGeez(int/long) converts 0-99,999,999 to Ethiopic numeral strings.\n"
    "toArabic(String) reverses using Unicode code-point iteration.")

# ── Commit 6: EthiopianTime ──────────────────────────────────────────────────
git("add", f"{BASE}/EthiopianTime.java")
git("commit", "-m",
    "feat: implement EthiopianTime — Ethiopian 12-hour clock (Time.ts port)\n\n"
    "fromGregorian() converts 24h time. fromString() parses Arabic and Ge'ez\n"
    "numeral strings. toGregorian() converts back. add/subtract/diff/format.")

# ── Commit 7: Formatting ─────────────────────────────────────────────────────
git("add", f"{BASE}/Formatting.java")
git("commit", "-m",
    "feat: implement Formatting — date and time formatting utilities\n\n"
    "Ethiopian: formatStandard, formatInGeezAmharic, formatWithWeekday,\n"
    "formatShort, formatWithTime, toISODateString.\n"
    "Gregorian: formatGregorianStandard, formatGregorianWithWeekday,\n"
    "formatGregorianWithTime, toGregorianISODateString.")

# ── Commit 8: BahireHasab ────────────────────────────────────────────────────
git("add", f"{BASE}/BahireHasab.java")
git("commit", "-m",
    "feat: implement BahireHasab — movable feast calculator\n\n"
    "getBahireHasab(year) returns full result with ameteAlem, evangelist,\n"
    "wenber, metqi, nineveh, and all movable feasts.\n"
    "getMovableHoliday(tewsakKey, year) returns date of a single feast.")

# ── Commit 9: Holidays ───────────────────────────────────────────────────────
git("add", f"{BASE}/Holidays.java")
git("commit", "-m",
    "feat: implement Holidays — full holiday lookup with Islamic support\n\n"
    "getHoliday, getHolidaysInMonth, getHolidaysForYear covering fixed,\n"
    "movable Christian, and Islamic holidays (Eid, Moulid) via arithmetic\n"
    "Hijri-Gregorian conversion. findHijriMonthRanges for Ramadan ranges.")

# ── Commit 10: Fasting ───────────────────────────────────────────────────────
git("add", f"{BASE}/Fasting.java")
git("commit", "-m",
    "feat: implement Fasting — fasting period calculator\n\n"
    "getFastingPeriod: ABIY_TSOME, TSOME_HAWARYAT, NINEVEH, TSOME_NEBIYAT,\n"
    "FILSETA, RAMADAN. getFastingInfo returns full metadata.\n"
    "getFastingDays with TSOME_DIHENET Pentecost exclusion window.")

# ── Commit 11: MonthGrid ─────────────────────────────────────────────────────
git("add", f"{BASE}/MonthGrid.java")
git("commit", "-m",
    "feat: implement MonthGrid — 7-column calendar grid generator\n\n"
    "MonthGrid.create(config) returns headers, padded day cells,\n"
    "localized month name and year. Supports weekStart, useGeez,\n"
    "weekdayLang, holidayFilter, and mode (christian/muslim/public).")

# ── Commit 12: Kenat facade ──────────────────────────────────────────────────
git("add", f"{BASE}/Kenat.java")
git("commit", "-m",
    "feat: implement Kenat facade — main library entry point\n\n"
    "Accepts string, EthiopianDate, or LocalDate inputs. Full formatting\n"
    "API, date arithmetic (add/subtract, diffIn*), comparison methods\n"
    "(isBefore/isAfter/isSameDay/Month/Year), startOf/endOf, holiday check.\n"
    "Static getMonthGrid, getYearCalendar, generateDateRange.")

print("\n=== Done. Last 13 commits: ===")
git("log", "--oneline", "-13")
