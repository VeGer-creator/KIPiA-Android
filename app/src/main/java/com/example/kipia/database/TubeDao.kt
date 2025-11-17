package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface TubeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tube: TubeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tubes: List<TubeEntity>)

    @Query("SELECT * FROM tube_table WHERE controlPointId = :controlPointId")
    suspend fun getTubesByControlPointId(controlPointId: Long): List<TubeEntity>

    @Query("SELECT * FROM tube_table WHERE id = :id")
    suspend fun getTubeById(id: Long): TubeEntity?

    @Delete
    suspend fun delete(tube: TubeEntity)

    @Query("DELETE FROM tube_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tube_table SET name = :name WHERE id = :id")
    suspend fun update(id: Long, name: String)
}