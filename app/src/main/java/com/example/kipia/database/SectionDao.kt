package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface SectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(section: SectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sections: List<SectionEntity>)

    @Query("SELECT * FROM section_table WHERE pkuId = :pkuId")
    suspend fun getSectionsByPKUId(pkuId: Long): List<SectionEntity>

    @Query("SELECT * FROM section_table WHERE id = :id")
    suspend fun getSectionById(id: Long): SectionEntity?

    @Delete
    suspend fun delete(section: SectionEntity)

    @Query("DELETE FROM section_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}