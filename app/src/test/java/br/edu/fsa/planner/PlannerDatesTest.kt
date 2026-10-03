package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.*
import org.junit.Test

class PlannerDatesTest {
    @Test fun monthGridKeepsEveryDayAndCompleteSundayToSaturdayRows() {
        // Includes non-leap centuries and a leap century, as well as every week offset.
        for (year in listOf(1900, 2000, 2024, 2025, 2026, 2100)) for (number in 1..12) {
            val month = YearMonth.of(year, number)
            val grid = PlannerDates.monthGrid(month)
            assertEquals(0, grid.size % 7)
            assertEquals(DayOfWeek.SUNDAY, grid.first().dayOfWeek)
            assertEquals(DayOfWeek.SATURDAY, grid.last().dayOfWeek)
            assertEquals(month.lengthOfMonth(), grid.count { YearMonth.from(it) == month })
            assertEquals(grid.size, grid.distinct().size)
            grid.zipWithNext().forEach { (first, second) -> assertEquals(first.plusDays(1), second) }
            assertTrue(grid.size in listOf(28, 35, 42))
        }
    }

    @Test fun leapDayAppearsOnlyInLeapYears() {
        assertTrue(LocalDate.of(2024, 2, 29) in PlannerDates.monthGrid(YearMonth.of(2024, 2)))
        assertEquals(28, PlannerDates.monthGrid(YearMonth.of(2025, 2)).count { it.monthValue == 2 })
    }

    @Test fun monthBeginningOnSundayHasNoExtraLeadingWeek() {
        assertEquals(LocalDate.of(2026, 2, 1), PlannerDates.monthGrid(YearMonth.of(2026, 2)).first())
        assertEquals(28, PlannerDates.monthGrid(YearMonth.of(2026, 2)).size)
    }

    @Test fun weekStartsMondayAndCrossesYearBoundary() {
        val interval = PlannerDates.week(LocalDate.of(2025, 1, 1))
        assertEquals(LocalDate.of(2024, 12, 30), interval.start)
        assertEquals(LocalDate.of(2025, 1, 5), interval.endInclusive)
        assertEquals(7, interval.days.size)
        interval.days.forEach { assertEquals(interval, PlannerDates.week(it)) }
    }

    @Test fun sundayBelongsToPrecedingMonday() {
        assertEquals(LocalDate.of(2026, 9, 28), PlannerDates.week(LocalDate.of(2026, 10, 4)).start)
    }

    @Test fun dateAndCompletionFiltersIncludeBothBoundaries() {
        val start = LocalDate.of(2026, 10, 1)
        val interval = DateInterval(start, start.plusDays(2))
        val items = listOf(
            PlannerItem(id = 1, title = "Antes", date = start.minusDays(1)),
            PlannerItem(id = 2, title = "Início", date = start),
            PlannerItem(id = 3, title = "Fim", date = start.plusDays(2), isCompleted = true),
            PlannerItem(id = 4, title = "Depois", date = start.plusDays(3)),
        )
        assertEquals(listOf(2L, 3L), PlannerDates.itemsIn(items, interval).map { it.id })
        assertEquals(listOf(3L), PlannerDates.itemsIn(items, interval, true).map { it.id })
        assertEquals(listOf(2L), PlannerDates.itemsIn(items, interval, false).map { it.id })
    }
}
