// app/src/main/java/com/example/kipia/database/RemarkDao.kt
package com.example.kipia.database

import androidx.room.*
import com.example.kipia.database.RemarkEntity

@Dao
interface RemarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remark: RemarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remarks: List<RemarkEntity>)

    @Query("SELECT * FROM remark_table WHERE controlPointId = :controlPointId")
    suspend fun getRemarksByControlPointId(controlPointId: Long): List<RemarkEntity>

    @Query("SELECT * FROM remark_table WHERE id = :id")
    suspend fun getRemarkById(id: Long): RemarkEntity?

    @Query("UPDATE remark_table SET isCompleted = :isCompleted, completionDate = :completionDate WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completionDate: String)

    @Update
    suspend fun update(remark: RemarkEntity)

    @Delete
    suspend fun delete(remark: RemarkEntity)

    @Query("DELETE FROM remark_table WHERE controlPointId = :controlPointId")
    suspend fun deleteByControlPointId(controlPointId: Long)
}