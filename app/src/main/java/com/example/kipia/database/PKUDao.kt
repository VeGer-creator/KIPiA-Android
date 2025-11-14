package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PKUDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pku: PKUEntity)

    @Query("SELECT * FROM pku_table ORDER BY name ASC")
    suspend fun getAllPKUs(): List<PKUEntity> // <-- Это ПРАВИЛЬНО

    @Query("DELETE FROM pku_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}