package com.example.pharmatracker.core.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Enum representing days of the week for reminders
 */
enum class Weekday(val value: Int, val displayName: String) {
    MONDAY(1, "Pazartesi"),
    TUESDAY(2, "Salı"),
    WEDNESDAY(3, "Çarşamba"),
    THURSDAY(4, "Perşembe"),
    FRIDAY(5, "Cuma"),
    SATURDAY(6, "Cumartesi"),
    SUNDAY(7, "Pazar");

    companion object {
        fun fromValue(value: Int): Weekday? = values().find { it.value == value }
        
        val ALL_DAYS = values().toSet()
    }
}

/**
 * Helper function to convert a set of Weekday to a formatted string
 */
fun Set<Weekday>.toFormattedString(): String {
    if (this.isEmpty()) return ""
    if (this.size == 7) return "Her gün"
    
    if (this.size == 5 && 
        this.contains(Weekday.MONDAY) && 
        this.contains(Weekday.TUESDAY) && 
        this.contains(Weekday.WEDNESDAY) && 
        this.contains(Weekday.THURSDAY) && 
        this.contains(Weekday.FRIDAY)) {
        return "Hafta içi her gün"
    }
    
    if (this.size == 2 && 
        this.contains(Weekday.SATURDAY) && 
        this.contains(Weekday.SUNDAY)) {
        return "Hafta sonu"
    }
    
    return this.sorted().joinToString(", ") { it.displayName }
}

/**
 * Sorts weekdays according to their natural order
 */
fun Collection<Weekday>.sorted(): List<Weekday> {
    return this.sortedBy { it.value }
}

@Entity(tableName = "weekdays")
data class WeekdayEntity(
    @PrimaryKey
    val value: Int,
    val name: String
) {
    companion object {
        fun fromWeekday(weekday: Weekday): WeekdayEntity {
            return WeekdayEntity(weekday.value, weekday.displayName)
        }
    }
}