package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.*
import org.junit.Test

class PlannerFormTest {
    private val valid = PlannerForm(title = "Estudar", date = "2026-10-03")
    @Test fun titleAndDateAreRequired() {
        val errors = validatePlannerForm(valid.copy(title = "   ", date = "2025-02-29"))
        assertEquals(TitleError.REQUIRED, errors.title)
        assertTrue(errors.date)
        assertFalse(errors.isValid)
    }
    @Test fun longTitleAndDescriptionAreRejected() {
        val errors = validatePlannerForm(valid.copy(title = "a".repeat(121), description = "a".repeat(2001)))
        assertEquals(TitleError.TOO_LONG, errors.title)
        assertTrue(errors.description)
    }
    @Test fun itemMayHaveNoTime() {
        assertTrue(validatePlannerForm(valid).isValid)
        assertNull(valid.toItem().startTime)
        assertNull(valid.toItem().endTime)
    }
    @Test fun malformedOrOutOfRangeTimeIsRejected() {
        for (value in listOf("24:00", "12:60", "9:00", "abc", "12:00:00")) {
            assertTrue("Should reject $value", validatePlannerForm(valid.copy(startTime = value)).startTime)
        }
    }
    @Test fun endRequiresStartAndMustBeLaterOnSameDay() {
        assertTrue(validatePlannerForm(valid.copy(endTime = "10:00")).startTime)
        assertTrue(validatePlannerForm(valid.copy(startTime = "10:00", endTime = "10:00")).timeOrder)
        assertTrue(validatePlannerForm(valid.copy(startTime = "10:00", endTime = "09:59")).timeOrder)
        assertTrue(validatePlannerForm(valid.copy(startTime = "09:00", endTime = "10:00")).isValid)
    }
    @Test fun conversionTrimsTextAndKeepsOriginalIdAndCreationTime() {
        val original = PlannerItem(id = 7, title = "Anterior", date = LocalDate.of(2026, 10, 2), createdAt = 123)
        val item = valid.copy(title = "  Estudar  ", description = " nota ", startTime = "09:00").toItem(original)
        assertEquals(7L, item.id)
        assertEquals(123L, item.createdAt)
        assertEquals("Estudar", item.title)
        assertEquals("nota", item.description)
        assertEquals(LocalTime.of(9, 0), item.startTime)
    }
    @Test fun loginValidatesEmailAndPassword() {
        assertFalse(validateLogin("invalido", "123").isValid)
        assertTrue(validateLogin(" estudante@example.com ", "123456").isValid)
        assertTrue(validateLogin("alguem @example.com", "123456").emailInvalid)
    }
}
