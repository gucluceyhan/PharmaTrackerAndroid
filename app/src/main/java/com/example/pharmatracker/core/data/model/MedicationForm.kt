package com.example.pharmatracker.core.data.model

import android.graphics.Color
import androidx.annotation.DrawableRes
import com.example.pharmatracker.R

/**
 * İlaç formlarını temsil eden enum sınıfı
 */
enum class MedicationForm(
    val displayName: String,
    @DrawableRes val iconRes: Int,
    val colorValue: Int
) {
    TABLET("Tablet", R.drawable.ic_pill, Color.parseColor("#2196F3")),       // Tablet (Mavi)
    CAPSULE("Kapsül", R.drawable.ic_capsule, Color.parseColor("#FF9800")),      // Kapsül (Turuncu)
    SYRUP("Şurup", R.drawable.ic_liquid, Color.parseColor("#00BCD4")),         // Şurup (Cam Göbeği)
    INJECTION("Enjeksiyon", R.drawable.ic_syringe, Color.parseColor("#F44336")), // Enjeksiyon (Kırmızı)
    CREAM("Krem", R.drawable.ic_cream, Color.parseColor("#FFEB3B")),          // Krem (Sarı)
    DROPS("Damla", R.drawable.ic_drops, Color.parseColor("#009688")),         // Damla (Teal)
    INHALER("İnhaler", R.drawable.ic_inhaler, Color.parseColor("#9C27B0")),     // İnhaler (Mor)
    POWDER("Toz", R.drawable.ic_powder, Color.parseColor("#9E9E9E")),          // Toz (Gri)
    PATCH("Bant", R.drawable.ic_patch, Color.parseColor("#4CAF50")),          // Bant (Yeşil)
    OTHER("Diğer", R.drawable.ic_other, Color.parseColor("#757575"));         // Diğer (Koyu Gri)

    companion object {
        // İsme göre enum değerini al
        fun fromDisplayName(name: String?): MedicationForm {
            return values().find { it.displayName == name } ?: OTHER
        }
        
        // UI için tüm görüntü isimlerinin listesini al
        fun getAllDisplayNames(): List<String> {
            return values().map { it.displayName }
        }
    }
}