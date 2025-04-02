package com.example.pharmatracker

import android.app.Application
import com.example.pharmatracker.core.data.database.AppDatabase
import com.example.pharmatracker.core.data.repository.MedicationRepository
import com.example.pharmatracker.core.service.NotificationService
import com.example.pharmatracker.core.service.PharmaDbService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Application class for PharmaTracker
 * Used to initialize components and provide global access to repositories and services
 */
class PharmaTrackerApplication : Application() {
    
    // Application scope for coroutines
    private val applicationScope = CoroutineScope(SupervisorJob())
    
    // Lazy initialization of the database
    private val database by lazy {
        AppDatabase.getDatabase(this, applicationScope)
    }
    
    // Repository that will be available throughout the app
    val repository by lazy {
        MedicationRepository(
            database.medicationDao(),
            database.reminderDao(),
            database.weekdayDao()
        )
    }
    
    // Services that will be available throughout the app
    val notificationService by lazy {
        NotificationService.getInstance(this)
    }
    
    val pharmaDbService by lazy {
        PharmaDbService.getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize anything that needs to be created at app start
        initializeApp()
    }
    
    /**
     * Initialize app components
     */
    private fun initializeApp() {
        // Schedule a WorkManager job to reschedule notifications for all medications
        // This ensures notifications are restored after app updates or device restarts
        scheduleReminderRestorationWork()
    }
    
    /**
     * Schedule a WorkManager job to restore all reminders
     * This would typically use WorkManager, which is omitted here for brevity
     */
    private fun scheduleReminderRestorationWork() {
        // Implementation would use WorkManager to schedule a job
        // that reads all medications and reminders from the database
        // and reschedules notifications
    }
}