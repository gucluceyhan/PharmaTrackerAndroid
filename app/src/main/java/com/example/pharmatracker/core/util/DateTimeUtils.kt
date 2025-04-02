package com.example.pharmatracker.core.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utility class for date and time operations
 */
object DateTimeUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    /**
     * Format a date for display
     */
    fun formatDate(date: LocalDate?): String {
        return date?.format(dateFormatter) ?: "Tarih belirtilmemiş"
    }
    
    /**
     * Format a date in short format
     */
    fun formatShortDate(date: LocalDate?): String {
        return date?.format(shortDateFormatter) ?: "Tarih belirtilmemiş"
    }
    
    /**
     * Format time for display
     */
    fun formatTime(time: LocalTime): String {
        return time.format(timeFormatter)
    }
    
    /**
     * Calculate days until a date
     * @return Positive number if date is in the future, negative if in the past, null if date is null
     */
    fun daysUntil(date: Date?): Int? {
        if (date == null) return null
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val targetDay = Calendar.getInstance().apply {
            timeInMillis = date.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diff = targetDay.timeInMillis - today.timeInMillis
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }
    
    /**
     * Check if a date is expired (before today)
     */
    fun isExpired(date: Date?): Boolean {
        if (date == null) return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val targetDay = Calendar.getInstance().apply {
            timeInMillis = date.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return targetDay.before(today)
    }
    
    /**
     * Get relative time description
     */
    fun getRelativeDateDescription(date: Date?): String {
        if (date == null) return "Tarih belirtilmemiş"
        val days = daysUntil(date) ?: return "Tarih belirtilmemiş"
        
        return when {
            days > 365 -> "${days / 365} yıl sonra"
            days > 30 -> "${days / 30} ay sonra"
            days > 0 -> "$days gün sonra"
            days == 0 -> "Bugün"
            days == -1 -> "Dün"
            days > -30 -> "${-days} gün önce"
            days > -365 -> "${-days / 30} ay önce"
            else -> "${-days / 365} yıl önce"
        }
    }
    
    /**
     * Get expiration status description
     */
    fun getExpirationStatus(date: Date?): ExpirationStatus {
        if (date == null) return ExpirationStatus.VALID
        val days = daysUntil(date) ?: return ExpirationStatus.VALID
        
        return when {
            days < 0 -> ExpirationStatus.EXPIRED
            days < 30 -> ExpirationStatus.SOON
            else -> ExpirationStatus.VALID
        }
    }
    
    /**
     * Enum representing expiration status
     */
    enum class ExpirationStatus {
        VALID,    // Not expired and not close to expiration
        SOON,     // Will expire within 30 days
        EXPIRED   // Already expired
    }

    /**
     * Tarihi biçimlendirir
     */
    fun formatDate(date: Date?): String {
        if (date == null) return ""
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}