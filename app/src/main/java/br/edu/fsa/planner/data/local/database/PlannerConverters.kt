package br.edu.fsa.planner.data.local.database

import androidx.room.TypeConverter
import br.edu.fsa.planner.domain.model.*
import java.time.LocalDate
import java.time.LocalTime

class PlannerConverters {
    @TypeConverter fun dateToLong(value: LocalDate): Long = value.toEpochDay()
    @TypeConverter fun longToDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)
    @TypeConverter fun timeToInt(value: LocalTime?): Int? = value?.let { it.hour * 60 + it.minute }
    @TypeConverter fun intToTime(value: Int?): LocalTime? = value?.let { LocalTime.of(it / 60, it % 60) }
    @TypeConverter fun typeToString(value: ItemType): String = value.name
    @TypeConverter fun stringToType(value: String): ItemType = ItemType.valueOf(value)
    @TypeConverter fun priorityToString(value: Priority): String = value.name
    @TypeConverter fun stringToPriority(value: String): Priority = Priority.valueOf(value)
    @TypeConverter fun categoryToString(value: Category): String = value.name
    @TypeConverter fun stringToCategory(value: String): Category = Category.valueOf(value)
    @TypeConverter fun moodToString(value: Mood?): String? = value?.name
    @TypeConverter fun stringToMood(value: String?): Mood? = value?.let(Mood::valueOf)
}
