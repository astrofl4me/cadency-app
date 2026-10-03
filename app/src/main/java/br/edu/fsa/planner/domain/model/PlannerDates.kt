package br.edu.fsa.planner.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class DateInterval(val start: LocalDate, val endInclusive: LocalDate) {
    init { require(!endInclusive.isBefore(start)) }
    operator fun contains(date: LocalDate) = !date.isBefore(start) && !date.isAfter(endInclusive)
    val days: List<LocalDate> get() = (0L..java.time.temporal.ChronoUnit.DAYS.between(start, endInclusive)).map(start::plusDays)
}

object PlannerDates {
    fun week(date: LocalDate): DateInterval {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return DateInterval(monday, monday.plusDays(6))
    }

    fun month(month: YearMonth) = DateInterval(month.atDay(1), month.atEndOfMonth())

    // Calendar columns begin on Sunday; padding contains real adjacent-month dates.
    fun monthGrid(month: YearMonth): List<LocalDate> {
        val first = month.atDay(1)
        val offset = first.dayOfWeek.value % 7
        val start = first.minusDays(offset.toLong())
        val cells = ((offset + month.lengthOfMonth() + 6) / 7) * 7
        return (0 until cells).map { start.plusDays(it.toLong()) }
    }

    fun itemsIn(items: List<PlannerItem>, interval: DateInterval, completed: Boolean? = null) = items.filter {
        it.date in interval && (completed == null || it.isCompleted == completed)
    }
}
