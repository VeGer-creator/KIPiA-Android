package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface PKUDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pku: PKUEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pkus: List<PKUEntity>)

    @Query("SELECT * FROM pku_table ORDER BY name ASC")
    suspend fun getAllPKUs(): List<PKUEntity>

    @Query("SELECT * FROM pku_table WHERE controlPointId = :controlPointId")
    suspend fun getPKUsByControlPointId(controlPointId: Long): List<PKUEntity>

    @Query("SELECT * FROM pku_table WHERE id = :id")
    suspend fun getPKUById(id: Long): PKUEntity?

    @Delete
    suspend fun delete(pku: PKUEntity)

    @Query("DELETE FROM pku_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE pku_table SET name = :name, description = :description WHERE id = :id")
    suspend fun update(id: Long, name: String, description: String)
}