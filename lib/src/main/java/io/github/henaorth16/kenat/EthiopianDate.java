package io.github.henaorth16.kenat;

/**
 * Immutable value type representing a date in the Ethiopian calendar.
 *
 * <p>Month 13 (Pagume) has 5 days in a common year and 6 days in an Ethiopian
 * leap year.  All other months have exactly 30 days.
 *
 * @param year  Ethiopian year (e.g. 2017)
 * @param month Ethiopian month, 1–13
 * @param day   Day of month, 1–30 (or 1–5/6 for month 13)
 */
public record EthiopianDate(int year, int month, int day) {

    /**
     * Compact constructor — validates range on construction so invalid dates
     * can never be stored in domain objects.
     *
     * <p>Deep validation (leap-year Pagume check) is deferred to
     * {@code EthiopianCalendarUtils.isValidEthiopianDate()} to avoid a
     * circular dependency at this layer.
     *
     * @throws io.github.henaorth16.kenat.exception.InvalidEthiopianDateException
     *         if any component is out of range
     */
    public EthiopianDate {
        // Month 13 (Pagume) has at most 6 days; all other months have at most 30.
        int maxDay = (month == 13) ? 6 : 30;
        if (month < 1 || month > 13 || day < 1 || day > maxDay) {
            throw new io.github.henaorth16.kenat.exception.InvalidEthiopianDateException(year, month, day);
        }
    }

    /**
     * Returns a human-readable representation in {@code yyyy/mm/dd} form.
     */
    @Override
    public String toString() {
        return String.format("%d/%02d/%02d", year, month, day);
    }
}
