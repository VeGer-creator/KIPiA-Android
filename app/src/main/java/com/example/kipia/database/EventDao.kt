package com.example.kipia.database

import androidx.room.*

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM event_table WHERE controlPointId = :controlPointId ORDER BY date DESC")
    suspend fun getEventsByControlPointId(controlPointId: Long): List<EventEntity>

    @Query("SELECT * FROM event_table")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM event_table WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM event_table WHERE controlPointId = :controlPointId")
    suspend fun deleteByControlPointId(controlPointId: Long)
}