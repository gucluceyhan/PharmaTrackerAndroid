package com.example.pharmatracker.core.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Data class for medication information from the PharmaDB database
 */
data class PharmaDbItem(
    @SerializedName("İlaç Adı Temel")
    val ilacAdiTemel: String,
    
    @SerializedName("Doz Bilgisi")
    val dozBilgisi: String,
    
    @SerializedName("Kutu İçeriği")
    val kutuIcerigi: String,
    
    @SerializedName("Barkod")
    val barkod: Long,
    
    @SerializedName("ATC Kodu")
    val atcKodu: String
)

/**
 * Service for querying medication information from a local JSON database
 */
class PharmaDbService(private val context: Context) {
    
    private var pharmaDatabase: List<PharmaDbItem>? = null
    
    /**
     * Load the database from JSON file
     */
    private suspend fun loadDatabase() = withContext(Dispatchers.IO) {
        try {
            // Try to load from assets/PharmaDB.json
            val jsonString = context.assets.open("PharmaDB.json").bufferedReader().use { it.readText() }
            
            val listType = object : TypeToken<List<PharmaDbItem>>() {}.type
            pharmaDatabase = Gson().fromJson(jsonString, listType)
            
            println("PharmaDB.json successfully loaded. ${pharmaDatabase?.size ?: 0} medications found.")
        } catch (e: IOException) {
            println("Error loading PharmaDB.json: ${e.localizedMessage}")
            pharmaDatabase = emptyList()
        } catch (e: JsonSyntaxException) {
            println("Error parsing PharmaDB.json: ${e.localizedMessage}")
            pharmaDatabase = emptyList()
        }
    }
    
    /**
     * Find a medication by barcode
     */
    suspend fun findMedicationByBarcode(barcode: String): PharmaDbItem? {
        if (pharmaDatabase == null) {
            loadDatabase()
        }
        
        val barcodeNumber = barcode.toLongOrNull() ?: run {
            println("Invalid barcode number: $barcode")
            return null
        }
        
        return pharmaDatabase?.find { it.barkod == barcodeNumber }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: PharmaDbService? = null
        
        fun getInstance(context: Context): PharmaDbService {
            return INSTANCE ?: synchronized(this) {
                val instance = PharmaDbService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}