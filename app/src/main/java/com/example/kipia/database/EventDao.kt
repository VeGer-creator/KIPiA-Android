// app/src/main/java/com/example/kipia/database/EventDao.kt
package com.example.kipia.database

import androidx.room.*
import com.example.kipia.database.EventEntity

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM event_table WHERE controlPointId = :controlPointId")
    suspend fun getEventsByControlPointId(controlPointId: Long): List<EventEntity>

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)
}