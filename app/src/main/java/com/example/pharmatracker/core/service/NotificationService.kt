package com.example.pharmatracker.core.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pharmatracker.MainActivity
import com.example.pharmatracker.R
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.data.model.Reminder
import com.example.pharmatracker.core.data.model.Weekday
import com.example.pharmatracker.core.receiver.AlarmReceiver
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.UUID

/**
 * Service to handle notification scheduling and display
 */
class NotificationService(private val context: Context) {
    
    companion object {
        private const val REMINDER_NOTIFICATION_CHANNEL_ID = "medication_reminders"
        private const val EXPIRY_NOTIFICATION_CHANNEL_ID = "medication_expiry"
        
        @Volatile
        private var INSTANCE: NotificationService? = null
        
        fun getInstance(context: Context): NotificationService {
            return INSTANCE ?: synchronized(this) {
                val instance = NotificationService(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
        
        // Generate a unique notification ID based on a string
        private fun generateNotificationId(key: String): Int {
            return key.hashCode() and 0x7fffffff // Ensure positive value
        }
    }
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Create notification channels for Android 8.0 (API level 26) and higher
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Reminder notification channel
            val reminderChannel = NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.reminder_channel_description)
                enableVibration(true)
            }
            
            // Expiry notification channel
            val expiryChannel = NotificationChannel(
                EXPIRY_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.expiry_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.expiry_channel_description)
            }
            
            // Register the channels with the system
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(expiryChannel)
        }
    }
    
    /**
     * Schedule a reminder notification for a medication
     */
    fun scheduleReminder(medication: Medication, reminder: Reminder, weekdays: Set<Weekday>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create a unique ID for this reminder based on its ID
        val notificationId = generateNotificationId(reminder.id)
        
        // Set up the intent that will be triggered when the alarm goes off
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_ID", medication.id)
            putExtra("REMINDER_ID", reminder.id)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("NOTIFICATION_TYPE", "REMINDER")
        }
        
        // Create a PendingIntent with a unique request code
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get current day of week
        val today = LocalDate.now()
        val currentDayOfWeek = today.dayOfWeek.value
        
        // Check if this reminder should be scheduled for today based on days of week
        val dayValues = weekdays.map { it.value }
        
        if (dayValues.contains(currentDayOfWeek)) {
            // Calculate time for today's reminder
            val reminderTime = reminder.time
            val now = LocalTime.now()
            
            // Only schedule if the time hasn't passed yet today
            if (reminderTime.isAfter(now)) {
                val scheduledTime = LocalDateTime.of(today, reminderTime)
                val scheduledTimeMillis = scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // Schedule the alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTimeMillis,
                        pendingIntent
                    )
                }
            }
        }
        
        // Find the next day this reminder should trigger
        scheduleNextReminder(medication, reminder, weekdays)
    }
    
    /**
     * Schedule the next occurrence of a reminder
     */
    private fun scheduleNextReminder(medication: Medication, reminder: Reminder, weekdays: Set<Weekday>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Current date and time
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val currentDayOfWeek = today.dayOfWeek.value
        
        // Find the next day of week for this reminder
        val dayValues = weekdays.map { it.value }
        if (dayValues.isEmpty()) return
        
        // Sort the day values to find the next one
        val sortedDays = dayValues.sorted()
        
        // Find the next day after today
        val nextDay = sortedDays.find { it > currentDayOfWeek } ?: sortedDays.first()
        
        // Calculate days to add to get to the next occurrence
        val daysToAdd = if (nextDay > currentDayOfWeek) {
            nextDay - currentDayOfWeek
        } else {
            7 - (currentDayOfWeek - nextDay) // Wrap to next week
        }
        
        // Calculate the date for the next reminder
        val nextReminderDate = today.plusDays(daysToAdd.toLong())
        val nextReminderDateTime = LocalDateTime.of(nextReminderDate, reminder.time)
        
        // Convert to milliseconds
        val nextReminderTimeMillis = nextReminderDateTime
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // Create a unique ID for this next reminder
        val notificationId = generateNotificationId("${reminder.id}_next")
        
        // Set up the intent
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_ID", medication.id)
            putExtra("REMINDER_ID", reminder.id)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("NOTIFICATION_TYPE", "REMINDER")
            putExtra("IS_REPEATING", true)
        }
        
        // Create a PendingIntent with a unique request code
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextReminderTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextReminderTimeMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Schedule an expiration notification for a medication
     * This will be triggered a configurable number of days before expiration
     */
    fun scheduleExpiryNotification(medication: Medication, daysBeforeExpiry: Int = 7) {
        if (medication.expirationDate == null) return
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Create a unique ID for this notification
        val notificationId = generateNotificationId("${medication.id}_expiry")
        
        // Calculate notification time (days before expiry at 9:00)
        val daysBeforeExpiryMillis = daysBeforeExpiry * 24 * 60 * 60 * 1000L
        val notificationDateMillis = medication.expirationDate?.time?.minus(daysBeforeExpiryMillis)
        val calendar = Calendar.getInstance().apply {
            notificationDateMillis?.let { timeInMillis = it }
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val notificationTimeMillis = calendar.timeInMillis
        
        // Only schedule if the notification time is in the future
        if (notificationTimeMillis <= System.currentTimeMillis()) {
            return
        }
        
        // Set up the intent
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICATION_NAME", medication.name)
            putExtra("MEDICATION_ID", medication.id)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("NOTIFICATION_TYPE", "EXPIRY")
            putExtra("DAYS_BEFORE_EXPIRY", daysBeforeExpiry)
        }
        
        // Create a PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        }
    }
    
    /**
     * Show a reminder notification
     */
    fun showReminderNotification(
        notificationId: Int, 
        medicationName: String, 
        medicationId: String
    ) {
        // Create an intent to open the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("MEDICATION_ID", medicationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_pill)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_text, medicationName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()
        
        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
    
    /**
     * Show an expiration notification
     */
    fun showExpiryNotification(
        notificationId: Int,
        medicationName: String,
        medicationId: String,
        daysBeforeExpiry: Int
    ) {
        // Create an intent to open the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("MEDICATION_ID", medicationId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, EXPIRY_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(context.getString(R.string.expiry_notification_title))
            .setContentText(context.getString(R.string.expiry_notification_text, medicationName))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show the notification
        val notificationManager = NotificationManagerCompat.from(context)
        try {
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handle notification permission not granted
        }
    }
    
    /**
     * Cancel reminders for a medication
     */
    fun cancelReminders(medicationId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel the expiry notification
        val expiryNotificationId = generateNotificationId("${medicationId}_expiry")
        
        val expiryIntent = Intent(context, AlarmReceiver::class.java)
        val expiryPendingIntent = PendingIntent.getBroadcast(
            context,
            expiryNotificationId,
            expiryIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        expiryPendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    /**
     * Cancel a specific reminder
     */
    fun cancelReminder(reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel the immediate reminder
        val notificationId = generateNotificationId(reminderId)
        
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
        
        // Cancel the next reminder
        val nextNotificationId = generateNotificationId("${reminderId}_next")
        
        val nextIntent = Intent(context, AlarmReceiver::class.java)
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            nextNotificationId,
            nextIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        nextPendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    
    /**
     * Cancel all notifications for a medication
     */
    fun cancelAllNotifications(medicationId: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Cancel expiry notification
        val expiryNotificationId = generateNotificationId("${medicationId}_expiry")
        notificationManager.cancel(expiryNotificationId)
        
        // Cancel reminders (would need a list of reminder IDs to be comprehensive)
        cancelReminders(medicationId)
    }
    
    /**
     * Request notification permissions for Android 13+ (API 33+)
     */
    fun requestNotificationPermission() {
        // Required for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // This should be called from an Activity
            // Using the newer notification permission API would go here
            // However, this would be implemented in a Fragment or Activity
        }
    }
}