package com.example.pharmatracker.core.data.database

import androidx.room.*
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.data.model.MedicationWithReminders
import kotlinx.coroutines.flow.Flow

/**
 * İlaç veritabanı işlemleri için DAO (Data Access Object) arayüzü
 */
@Dao
interface MedicationDao {
    /**
     * Insert a new medication
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)
    
    /**
     * Update an existing medication
     */
    @Update
    suspend fun updateMedication(medication: Medication)
    
    /**
     * Delete a medication
     */
    @Delete
    suspend fun deleteMedication(medication: Medication)
    
    /**
     * Get all medications as a Flow object
     */
    @Query("SELECT * FROM medications")
    fun getAllMedications(): Flow<List<Medication>>
    
    /**
     * Get a single medication by ID
     */
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: String): Medication?
    
    /**
     * Get all medications with their reminders as a Flow object
     */
    @Transaction
    @Query("SELECT * FROM medications")
    fun getAllMedicationsWithReminders(): Flow<List<MedicationWithReminders>>
    
    /**
     * Get a single medication with its reminders by ID
     */
    @Transaction
    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationWithRemindersById(id: String): MedicationWithReminders?
    
    /**
     * Get a medication by barcode number
     */
    @Query("SELECT * FROM medications WHERE barcodeNumber = :barcode")
    suspend fun getMedicationByBarcode(barcode: String): Medication?
    
    /**
     * Delete a medication by ID
     */
    @Query("DELETE FROM medications WHERE id = :medicationId")
    suspend fun deleteMedicationById(medicationId: String)
    
    /**
     * Delete all medications
     */
    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()
    
    /**
     * Get medications expiring within the given number of days
     */
    @Query("SELECT * FROM medications WHERE expirationDate <= date('now', '+' || :days || ' days')")
    suspend fun getMedicationsExpiringWithinDays(days: Int): List<Medication>
}