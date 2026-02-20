package com.example.kipia.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

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

    @Update
    suspend fun update(section: SectionEntity)

    @Delete
    suspend fun delete(section: SectionEntity)

    @Query("DELETE FROM section_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM section_table")
    suspend fun getAllSections(): List<SectionEntity>
}