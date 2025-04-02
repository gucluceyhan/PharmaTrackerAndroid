package com.example.pharmatracker.core.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatDate(date: Date?): String {
        return date?.let { dateFormat.format(it) } ?: ""
    }

    fun calculateDaysUntil(date: Date?): Int {
        if (date == null) return 0
        val today = Calendar.getInstance()
        val expiryDate = Calendar.getInstance().apply { time = date }
        
        // Reset time part to compare only dates
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        expiryDate.set(Calendar.HOUR_OF_DAY, 0)
        expiryDate.set(Calendar.MINUTE, 0)
        expiryDate.set(Calendar.SECOND, 0)
        expiryDate.set(Calendar.MILLISECOND, 0)
        
        val diff = expiryDate.timeInMillis - today.timeInMillis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
} 