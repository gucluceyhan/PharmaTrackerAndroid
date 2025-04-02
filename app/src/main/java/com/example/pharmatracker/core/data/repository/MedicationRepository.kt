package com.example.pharmatracker.core.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pharmatracker.core.data.database.MedicationDao
import com.example.pharmatracker.core.data.database.ReminderDao
import com.example.pharmatracker.core.data.database.WeekdayDao
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.data.model.MedicationWithReminders
import com.example.pharmatracker.core.data.model.Reminder
import com.example.pharmatracker.core.data.model.ReminderWeekdayCrossRef
import com.example.pharmatracker.core.data.model.Weekday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class that provides an abstraction layer over the data sources
 * (Room database in this case)
 */
@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val reminderDao: ReminderDao,
    private val weekdayDao: WeekdayDao
) {
    // Get all medications with their reminders as LiveData
    private val _allMedications = MutableLiveData<List<Medication>>()
    val allMedications: LiveData<List<Medication>> = _allMedications
    
    private suspend fun loadMedications() {
        withContext(Dispatchers.IO) {
            try {
                medicationDao.getAllMedicationsWithReminders().collectLatest { medicationsWithReminders ->
                    val medications = ArrayList<Medication>()
                    
                    for (medicationWithReminder in medicationsWithReminders) {
                        val medication = medicationWithReminder.medication
                        medication.reminders = medicationWithReminder.reminders
                        medications.add(medication)
                    }
                    
                    _allMedications.postValue(medications)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    /**
     * Get a medication by ID with its reminders
     */
    suspend fun getMedicationById(id: String): Medication? = withContext(Dispatchers.IO) {
        try {
            val medicationWithReminders = medicationDao.getMedicationWithRemindersById(id)
            if (medicationWithReminders != null) {
                val medication = medicationWithReminders.medication
                medication.reminders = medicationWithReminders.reminders
                medication
            } else {
                null
            }
        } catch (e: Exception) {
            // Log error
            null
        }
    }
    
    /**
     * Get a medication by barcode
     */
    suspend fun getMedicationByBarcode(barcode: String): Medication? = withContext(Dispatchers.IO) {
        medicationDao.getMedicationByBarcode(barcode)
    }
    
    /**
     * Save a medication with its reminders
     */
    suspend fun insertMedication(medication: Medication) = withContext(Dispatchers.IO) {
        try {
            medicationDao.insertMedication(medication)
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Update an existing medication
     */
    suspend fun updateMedication(medication: Medication) = withContext(Dispatchers.IO) {
        try {
            medicationDao.updateMedication(medication)
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Delete a medication by ID
     */
    suspend fun deleteMedicationById(id: String) = withContext(Dispatchers.IO) {
        try {
            medicationDao.deleteMedicationById(id)
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Delete a medication
     */
    suspend fun deleteMedication(medication: Medication) = withContext(Dispatchers.IO) {
        try {
            medicationDao.deleteMedicationById(medication.id)
        } catch (e: Exception) {
            // Log error
        }
    }
    
    /**
     * Delete all medications
     */
    suspend fun deleteAllMedications() = withContext(Dispatchers.IO) {
        medicationDao.deleteAllMedications()
    }
    
    /**
     * Add a reminder to a medication
     */
    suspend fun addReminder(medicationId: String, reminder: Reminder) = withContext(Dispatchers.IO) {
        // Create a new reminder with the medication ID
        val reminderToSave = Reminder(
            id = reminder.id,
            medicationId = medicationId,
            time = reminder.time,
            isActive = reminder.isActive
        )
        
        reminderDao.insertReminder(reminderToSave)
        
        // Insert weekday cross-references
        reminder.weekdays.forEach { weekday ->
            reminderDao.insertReminderWeekdayCrossRef(
                ReminderWeekdayCrossRef(reminder.id, weekday.value)
            )
        }
    }
    
    /**
     * Update an existing reminder
     */
    suspend fun updateReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        reminderDao.updateReminder(reminder)
        
        // Delete existing weekday cross-references
        reminderDao.deleteWeekdaysForReminder(reminder.id)
        
        // Insert new weekday cross-references
        reminder.weekdays.forEach { weekday ->
            reminderDao.insertReminderWeekdayCrossRef(
                ReminderWeekdayCrossRef(reminder.id, weekday.value)
            )
        }
    }
    
    /**
     * Delete a reminder by ID
     */
    suspend fun deleteReminderById(reminderId: String) = withContext(Dispatchers.IO) {
        // Delete weekday cross-references first
        reminderDao.deleteWeekdaysForReminder(reminderId)
        
        // Delete the reminder
        reminderDao.deleteReminderById(reminderId)
    }
    
    /**
     * Get medications that expire within a certain number of days
     */
    suspend fun getMedicationsExpiringWithinDays(days: Int): List<Medication> = withContext(Dispatchers.IO) {
        medicationDao.getMedicationsExpiringWithinDays(days)
    }
    
    /**
     * Get all active reminders for a specific day of the week
     */
    suspend fun getActiveRemindersForDay(dayOfWeek: Int): List<Reminder> = withContext(Dispatchers.IO) {
        reminderDao.getActiveRemindersForDay(dayOfWeek)
    }
    
    init {
        // Create a collector for the flow
        CoroutineScope(Dispatchers.IO).launch {
            medicationDao.getAllMedicationsWithReminders().collectLatest { medicationsWithReminders ->
                val medications = ArrayList<Medication>()
                
                for (medicationWithReminder in medicationsWithReminders) {
                    val medication = medicationWithReminder.medication
                    medication.reminders = medicationWithReminder.reminders
                    medications.add(medication)
                }
                
                _allMedications.postValue(medications)
            }
        }
    }
}