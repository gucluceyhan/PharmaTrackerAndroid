package com.example.pharmatracker.core.data.model

/**
 * İlaç renklerini temsil eden enum sınıfı
 */
enum class MedicationColor(val hexCode: String) {
    WHITE("#FFFFFF"),       // Beyaz
    RED("#FF0000"),        // Kırmızı
    BLUE("#0000FF"),       // Mavi
    GREEN("#00FF00"),      // Yeşil
    YELLOW("#FFFF00"),     // Sarı
    ORANGE("#FFA500"),     // Turuncu
    PURPLE("#800080"),     // Mor
    PINK("#FFC0CB"),       // Pembe
    BROWN("#A52A2A"),      // Kahverengi
    BLACK("#000000"),      // Siyah
    GRAY("#808080"),       // Gri
    TRANSPARENT("#00000000") // Şeffaf
} 