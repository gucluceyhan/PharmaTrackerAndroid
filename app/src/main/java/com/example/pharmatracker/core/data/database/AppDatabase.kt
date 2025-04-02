package com.example.pharmatracker.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.pharmatracker.core.data.model.Medication
import com.example.pharmatracker.core.data.model.Reminder
import com.example.pharmatracker.core.data.model.ReminderWeekdayCrossRef
import com.example.pharmatracker.core.data.model.Weekday
import com.example.pharmatracker.core.data.model.WeekdayEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for the application, contains all entities and DAOs
 */
@Database(
    entities = [
        Medication::class,
        Reminder::class,
        WeekdayEntity::class,
        ReminderWeekdayCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun weekdayDao(): WeekdayDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pharmatracker_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback for database creation, used to prepopulate weekdays
         */
        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        // Prepopulate weekdays
                        populateWeekdays(database.weekdayDao())
                    }
                }
            }
            
            /**
             * Prepopulate the weekdays table with all weekday values
             */
            private suspend fun populateWeekdays(weekdayDao: WeekdayDao) {
                val weekdays = Weekday.values().map { WeekdayEntity.fromWeekday(it) }
                weekdayDao.insertAllWeekdays(weekdays)
            }
        }
    }
}