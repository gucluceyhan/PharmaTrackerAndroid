package com.example.pharmatracker.core.data.database

import androidx.room.TypeConverter
import com.example.pharmatracker.core.data.model.MedicationForm
import com.example.pharmatracker.core.data.model.MedicationColor
import com.example.pharmatracker.core.data.model.Weekday
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * Type converters for Room database to handle complex types
 */
class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val gson = Gson()
    
    /**
     * Convert LocalDate to String for storage
     */
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    /**
     * Convert String to LocalDate for retrieval
     */
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    /**
     * Convert LocalTime to String for storage
     */
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.format(timeFormatter)
    }
    
    /**
     * Convert String to LocalTime for retrieval
     */
    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it, timeFormatter) }
    }
    
    /**
     * Convert MedicationForm to String for storage
     */
    @TypeConverter
    fun fromMedicationForm(form: MedicationForm): String {
        return form.name
    }
    
    /**
     * Convert String to MedicationForm for retrieval
     */
    @TypeConverter
    fun toMedicationForm(name: String): MedicationForm {
        return try {
            MedicationForm.valueOf(name)
        } catch (e: IllegalArgumentException) {
            MedicationForm.OTHER
        }
    }

    @TypeConverter
    fun fromMedicationColor(color: MedicationColor): String {
        return color.name
    }

    @TypeConverter
    fun toMedicationColor(name: String): MedicationColor {
        return try {
            MedicationColor.valueOf(name)
        } catch (e: IllegalArgumentException) {
            MedicationColor.WHITE
        }
    }

    @TypeConverter
    fun fromWeekdaySet(weekdays: Set<Weekday>): String {
        return gson.toJson(weekdays.map { it.value })
    }

    @TypeConverter
    fun toWeekdaySet(value: String): Set<Weekday> {
        val listType = object : TypeToken<List<Int>>() {}.type
        val weekdayValues: List<Int> = gson.fromJson(value, listType)
        return weekdayValues.mapNotNull { Weekday.fromValue(it) }.toSet()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}