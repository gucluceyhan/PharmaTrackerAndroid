package com.example.pharmatracker.core.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.Embedded
import java.util.Date
import java.util.UUID
import java.util.Calendar

/**
 * Represents a medication entity
 */
@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey 
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcodeNumber: String? = null,
    val quantity: Int = 0,
    val expirationDate: Date? = null,
    val notes: String? = null,
    val form: MedicationForm = MedicationForm.TABLET,
) {
    @Ignore
    var reminders: List<Reminder> = emptyList()
    
    companion object {
        /**
         * Creates demo data for testing
         */
        fun getDemoData(): List<Medication> {
            val calendar = Calendar.getInstance()
            
            // 6 ay sonrası
            val sixMonthsLater = Calendar.getInstance().apply {
                add(Calendar.MONTH, 6)
            }.time
            
            // 10 gün öncesi
            val tenDaysAgo = Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_MONTH, -10)
            }.time
            
            return listOf(
                Medication(
                    name = "Aspirin",
                    barcodeNumber = "8699546500017",
                    quantity = 20,
                    expirationDate = sixMonthsLater,
                    notes = "Yemeklerden sonra al",
                    form = MedicationForm.TABLET
                ),
                Medication(
                    name = "Parol",
                    quantity = 12,
                    expirationDate = tenDaysAgo,
                    notes = "Günde 2 tablet",
                    form = MedicationForm.TABLET
                )
            )
        }
    }
    
    /**
     * Factory methods for creating a Medication with a specific property updated
     */
    fun withExpirationDate(expirationDate: Date?): Medication {
        return this.copy(expirationDate = expirationDate)
    }
    
    fun withQuantity(quantity: Int): Medication {
        return this.copy(quantity = quantity)
    }
    
    fun withNotes(notes: String?): Medication {
        return this.copy(notes = notes)
    }
    
    fun withForm(form: MedicationForm): Medication {
        return this.copy(form = form)
    }
}

/**
 * Represents a medication with its associated reminders for database operations
 */
data class MedicationWithReminders(
    @Embedded val medication: Medication,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicationId"
    )
    val reminders: List<Reminder>
)