package com.example.pharmatracker.core.data.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "reminder_weekday_cross_ref",
    primaryKeys = ["reminderId", "weekdayValue"],
    indices = [Index(value = ["weekdayValue"])]
)
data class ReminderWeekdayCrossRef(
    val reminderId: String,
    val weekdayValue: Int
) 