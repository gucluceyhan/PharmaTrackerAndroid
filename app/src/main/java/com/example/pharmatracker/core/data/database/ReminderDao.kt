package com.example.pharmatracker.core.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.pharmatracker.core.data.model.Reminder
import com.example.pharmatracker.core.data.model.ReminderWeekdayCrossRef
import com.example.pharmatracker.core.data.model.ReminderWithWeekdays
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Reminder entities
 */
@Dao
interface ReminderDao {
    /**
     * Get all reminders for a specific medication
     */
    @Query("SELECT * FROM reminders WHERE medicationId = :medicationId")
    fun getRemindersForMedication(medicationId: String): LiveData<List<Reminder>>
    
    /**
     * Get all reminders with their weekdays
     */
    @Transaction
    @Query("SELECT * FROM reminders")
    suspend fun getRemindersWithWeekdays(): List<ReminderWithWeekdays>
    
    /**
     * Get a specific reminder with its weekdays
     */
    @Transaction
    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    suspend fun getReminderWithWeekdaysById(reminderId: String): ReminderWithWeekdays?
    
    /**
     * Get all reminders with weekdays for a specific medication
     */
    @Transaction
    @Query("SELECT * FROM reminders WHERE medicationId = :medicationId")
    suspend fun getRemindersWithWeekdaysForMedication(medicationId: String): List<ReminderWithWeekdays>
    
    /**
     * Insert a new reminder
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long
    
    /**
     * Insert a reminder-weekday cross reference
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderWeekdayCrossRef(crossRef: ReminderWeekdayCrossRef)
    
    /**
     * Update an existing reminder
     */
    @Update
    suspend fun updateReminder(reminder: Reminder)
    
    /**
     * Delete a reminder
     */
    @Delete
    suspend fun deleteReminder(reminder: Reminder)
    
    /**
     * Delete a reminder by ID
     */
    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: String)
    
    /**
     * Delete all reminders for a medication
     */
    @Query("DELETE FROM reminders WHERE medicationId = :medicationId")
    suspend fun deleteRemindersForMedication(medicationId: String)
    
    /**
     * Delete all reminder-weekday cross references for a reminder
     */
    @Query("DELETE FROM reminder_weekday_cross_ref WHERE reminderId = :reminderId")
    suspend fun deleteWeekdaysForReminder(reminderId: String)
    
    /**
     * Get all active reminders for the current day
     */
    @Query("SELECT r.* FROM reminders r " +
           "JOIN reminder_weekday_cross_ref rw ON r.id = rw.reminderId " +
           "WHERE r.isActive = 1 AND rw.weekdayValue = :dayOfWeek")
    suspend fun getActiveRemindersForDay(dayOfWeek: Int): List<Reminder>

    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: String): Reminder?

    @Query("SELECT * FROM reminders WHERE medicationId = :medicationId")
    fun getRemindersByMedicationId(medicationId: String): Flow<List<Reminder>>

    @Transaction
    @Query("SELECT * FROM reminders")
    fun getAllRemindersWithWeekdays(): Flow<List<ReminderWithWeekdays>>
}