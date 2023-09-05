package org.escalaralcoiaicomtat.android.utils

import java.time.Month
import java.time.Year
import java.time.YearMonth

/**
 * Checks whether the given [month] has a day of number [day].
 */
fun isDayMonthPossible(day: String?, month: Month?): Boolean {
    if (day == null || month == null) return false

    val dayNumber = day.toUShortOrNull() ?: return false
    if (dayNumber <= 0U) return false

    val thisYear = Year.now()
    val monthDaysThisYear = YearMonth.of(thisYear.value, month).lengthOfMonth()
    val monthDaysNextYear = YearMonth.of(thisYear.plusYears(1).value, month).lengthOfMonth()
    // Make sure to only allow number of days inside non-leap year
    val monthDaysLength = minOf(monthDaysThisYear, monthDaysNextYear)
    return dayNumber.toInt() <= monthDaysLength
}
