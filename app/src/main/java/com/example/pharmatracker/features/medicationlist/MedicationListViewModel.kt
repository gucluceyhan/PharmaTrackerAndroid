package com.example.pharmatracker.features.medicationlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.data.repository.MedicationRepository
import com.example.pharmatracker.core.util.DateTimeUtils
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for the medication list screen
 */
class MedicationListViewModel(private val repository: MedicationRepository) : ViewModel() {
    
    // LiveData for all medications from the repository
    private val _allMedications = repository.allMedications
    
    // LiveData for the search text
    private val _searchText = MutableLiveData<String>("")
    val searchText: LiveData<String> = _searchText
    
    // LiveData for sort order
    private val _sortOrder = MutableLiveData(SortOrder.NAME_AZ)
    val sortOrder: LiveData<SortOrder> = _sortOrder
    
    // Loading state
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Error messages
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Filtered and sorted medications
    val medications = MediatorLiveData<List<Medication>>()
    
    init {
        // Set up the data sources for the filtered medications
        medications.addSource(_allMedications) { medicationList ->
            medications.value = filterAndSortMedications(medicationList, _searchText.value, _sortOrder.value)
        }
        
        medications.addSource(_searchText) { searchText ->
            medications.value = filterAndSortMedications(_allMedications.value, searchText, _sortOrder.value)
        }
        
        medications.addSource(_sortOrder) { sortOrder ->
            medications.value = filterAndSortMedications(_allMedications.value, _searchText.value, sortOrder)
        }
    }
    
    /**
     * Set the search text to filter medications
     */
    fun setSearchText(text: String) {
        _searchText.value = text
    }
    
    /**
     * Set the sort order for medications
     */
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }
    
    /**
     * Delete a medication
     */
    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                repository.deleteMedicationById(medication.id)
            } catch (e: Exception) {
                _errorMessage.value = "İlaç silinirken hata oluştu: ${e.localizedMessage}"
            }
        }
    }
    
    /**
     * Calculate days until expiration for a medication
     */
    fun daysUntilExpiration(medication: Medication): Int? {
        return DateTimeUtils.daysUntil(medication.expirationDate)
    }
    
    /**
     * Filter and sort medications based on search text and sort order
     */
    private fun filterAndSortMedications(
        medications: List<Medication>?,
        searchText: String?,
        sortOrder: SortOrder?
    ): List<Medication> {
        if (medications == null) return emptyList()
        
        // Filter by search text
        val filteredList = if (searchText.isNullOrBlank()) {
            medications
        } else {
            medications.filter { medication ->
                medication.name.contains(searchText, ignoreCase = true) ||
                        medication.barcodeNumber?.contains(searchText, ignoreCase = true) == true
            }
        }
        
        // Sort according to the sort order
        return when (sortOrder) {
            SortOrder.NAME_AZ -> filteredList.sortedBy { it.name.lowercase() }
            SortOrder.NAME_ZA -> filteredList.sortedByDescending { it.name.lowercase() }
            SortOrder.EXPIRATION_CLOSEST -> filteredList.sortedWith(compareBy(nullsLast()) { it.expirationDate })
            SortOrder.EXPIRATION_FARTHEST -> filteredList.sortedWith(compareByDescending(nullsLast()) { it.expirationDate })
            null -> filteredList
        }
    }
    
    /**
     * Refresh the medication list
     */
    fun refreshMedications() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // The repository already uses LiveData so it will be refreshed automatically
                // This is mainly to update the loading state
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "İlaçlar yüklenirken hata oluştu: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear any error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Medication sort options
     */
    enum class SortOrder {
        NAME_AZ,                // A-Z
        NAME_ZA,                // Z-A
        EXPIRATION_CLOSEST,     // Soonest expiration first
        EXPIRATION_FARTHEST     // Latest expiration first
    }
    
    /**
     * ViewModel Factory for creating the ViewModel with dependencies
     */
    class Factory(private val repository: MedicationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedicationListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedicationListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    /**
     * Set expiration date for a medication
     */
    fun setExpirationDate(medication: Medication, expirationDate: Date?) {
        // Implementation of the method
    }

    /**
     * Update medication with a new expiration date
     */
    fun updateMedicationWithExpirationDate(medicationId: String, expirationDate: Date?) {
        viewModelScope.launch {
            val medication = repository.getMedicationById(medicationId)
            medication?.let {
                val updatedMedication = it.withExpirationDate(expirationDate)
                repository.updateMedication(updatedMedication)
            }
        }
    }
}