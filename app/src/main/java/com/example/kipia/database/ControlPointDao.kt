package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ControlPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(controlPoint: ControlPointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(controlPoints: List<ControlPointEntity>)

    @Query("SELECT * FROM control_point_table ORDER BY name ASC")
    suspend fun getAllControlPoints(): List<ControlPointEntity>

    @Query("SELECT * FROM control_point_table ORDER BY name ASC")
    fun getAllControlPointsFlow(): Flow<List<ControlPointEntity>>

    @Query("SELECT * FROM control_point_table WHERE id = :id")
    suspend fun getControlPointById(id: Long): ControlPointEntity?

    @Delete
    suspend fun delete(controlPoint: ControlPointEntity)

    @Query("DELETE FROM control_point_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE control_point_table SET name = :name, description = :description WHERE id = :id")
    suspend fun update(id: Long, name: String, description: String)
}