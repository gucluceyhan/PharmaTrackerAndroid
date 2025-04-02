package com.example.pharmatracker.core.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pharmatracker.core.service.NotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling alarm intents and showing notifications
 */
class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService = NotificationService.getInstance(context)
        
        // Get notification type (reminder or expiry)
        val notificationType = intent.getStringExtra("NOTIFICATION_TYPE") ?: return
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: return
        val medicationId = intent.getStringExtra("MEDICATION_ID") ?: return
        
        when (notificationType) {
            "REMINDER" -> {
                // Show reminder notification
                notificationService.showReminderNotification(
                    notificationId,
                    medicationName,
                    medicationId
                )
                
                // If this is a repeating reminder, schedule the next occurrence
                val isRepeating = intent.getBooleanExtra("IS_REPEATING", false)
                val reminderId = intent.getStringExtra("REMINDER_ID")
                
                if (isRepeating && reminderId != null) {
                    // Ideally, we would re-schedule here but we need repository access
                    // This would typically be handled through a WorkManager job that
                    // can access the database
                    CoroutineScope(Dispatchers.IO).launch {
                        // Re-scheduling logic would go here but requires database access
                        // In a real implementation, you would use WorkManager or a Service
                    }
                }
            }
            "EXPIRY" -> {
                // Show expiration notification
                val daysBeforeExpiry = intent.getIntExtra("DAYS_BEFORE_EXPIRY", 7)
                notificationService.showExpiryNotification(
                    notificationId,
                    medicationName,
                    medicationId,
                    daysBeforeExpiry
                )
            }
        }
    }
}

/**
 * BroadcastReceiver that runs when the device boots to reschedule all notifications
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // This would ideally schedule a WorkManager job to reschedule all notifications
            // This requires database access and NotificationService, which should be handled
            // by a dependency injection framework or application class
            
            // Example (pseudo-code):
            // val workManager = WorkManager.getInstance(context)
            // val rescheduleWork = OneTimeWorkRequestBuilder<RescheduleNotificationsWorker>().build()
            // workManager.enqueue(rescheduleWork)
        }
    }
}