package br.edu.fsa.planner.domain.model

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class PlannerForm(
    val title: String = "", val description: String = "", val date: String = LocalDate.now().toString(),
    val startTime: String = "", val endTime: String = "", val type: ItemType = ItemType.TASK,
    val priority: Priority = Priority.MEDIUM, val category: Category = Category.PERSONAL, val isCompleted: Boolean = false,
) {
    fun toItem(original: PlannerItem? = null): PlannerItem {
        require(validatePlannerForm(this).isValid)
        return PlannerItem(original?.id ?: 0, title.trim(), description.trim(), LocalDate.parse(date),
            parseOptionalTime(startTime), parseOptionalTime(endTime), type, priority, category, isCompleted,
            original?.createdAt ?: 0, original?.updatedAt ?: 0)
    }
}

enum class TitleError { REQUIRED, TOO_LONG }
data class FormErrors(
    val title: TitleError? = null, val description: Boolean = false, val date: Boolean = false,
    val startTime: Boolean = false, val endTime: Boolean = false, val timeOrder: Boolean = false,
) {
    val isValid get() = title == null && !description && !date && !startTime && !endTime && !timeOrder
}

fun parseOptionalTime(value: String): LocalTime? {
    if (value.isBlank()) return null
    require(Regex("^[0-2][0-9]:[0-5][0-9]$").matches(value))
    return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME)
}

fun validatePlannerForm(form: PlannerForm): FormErrors {
    val start = runCatching { parseOptionalTime(form.startTime) }
    val end = runCatching { parseOptionalTime(form.endTime) }
    return FormErrors(
        title = when { form.title.isBlank() -> TitleError.REQUIRED; form.title.trim().length > 120 -> TitleError.TOO_LONG; else -> null },
        description = form.description.length > 2000,
        date = runCatching { LocalDate.parse(form.date) }.isFailure,
        startTime = start.isFailure || (form.endTime.isNotBlank() && form.startTime.isBlank()),
        endTime = end.isFailure,
        timeOrder = start.isSuccess && end.isSuccess && start.getOrNull() != null && end.getOrNull() != null &&
            !end.getOrThrow()!!.isAfter(start.getOrThrow()!!),
    )
}

fun PlannerItem.toForm() = PlannerForm(title, description, date.toString(), startTime?.toString().orEmpty(),
    endTime?.toString().orEmpty(), type, priority, category, isCompleted)
