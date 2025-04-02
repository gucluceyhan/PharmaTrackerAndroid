package com.example.pharmatracker.core.data.model

import androidx.room.*
import java.time.LocalTime
import java.util.UUID

/**
 * İlaç hatırlatıcılarını temsil eden veri sınıfı
 */
@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medicationId")]
)
data class Reminder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val medicationId: String,
    val time: LocalTime,
    val isActive: Boolean = true
) {
    @Ignore
    var weekdays: Set<Weekday> = emptySet()
}

/**
 * Hatırlatıcı ve haftanın günlerini birlikte temsil eden veri sınıfı
 */
data class ReminderWithWeekdays(
    @Embedded
    val reminder: Reminder,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "value",
        associateBy = Junction(
            value = ReminderWeekdayCrossRef::class,
            parentColumn = "reminderId",
            entityColumn = "weekdayValue"
        )
    )
    val weekdays: List<WeekdayEntity>
)