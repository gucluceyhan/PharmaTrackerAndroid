package com.example.pharmatracker.core.data.database

import androidx.room.*
import com.example.pharmatracker.core.data.model.WeekdayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for WeekdayEntity
 */
@Dao
interface WeekdayDao {
    /**
     * Insert a weekday entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeekday(weekday: WeekdayEntity)
    
    /**
     * Insert all weekday values if they don't exist
     * This is used to pre-populate the weekday table
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllWeekdays(weekdays: List<WeekdayEntity>)

    /**
     * Get all weekdays
     */
    @Query("SELECT * FROM weekdays")
    fun getAllWeekdays(): Flow<List<WeekdayEntity>>

    @Query("SELECT * FROM weekdays WHERE value = :value")
    suspend fun getWeekdayByValue(value: Int): WeekdayEntity?
}