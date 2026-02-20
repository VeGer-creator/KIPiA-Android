package com.example.kipia.database

import androidx.room.*

@Dao
interface RemarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(remark: RemarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remarks: List<RemarkEntity>)

    @Query("SELECT * FROM remark_table WHERE controlPointId = :controlPointId ORDER BY " +
            "CASE WHEN priority = 'Высокий' THEN 1 " +
            "WHEN priority = 'Средний' THEN 2 " +
            "ELSE 3 END, deadline ASC")
    suspend fun getRemarksByControlPointId(controlPointId: Long): List<RemarkEntity>

    @Query("SELECT * FROM remark_table")
    suspend fun getAllRemarks(): List<RemarkEntity>

    @Query("SELECT * FROM remark_table WHERE controlPointId = :controlPointId AND isArchived = 0 ORDER BY " +
            "CASE WHEN priority = 'Высокий' THEN 1 " +
            "WHEN priority = 'Средний' THEN 2 " +
            "ELSE 3 END, deadline ASC")
    suspend fun getActiveRemarksByControlPointId(controlPointId: Long): List<RemarkEntity>

    @Query("SELECT * FROM remark_table WHERE controlPointId = :controlPointId AND isArchived = 1 ORDER BY " +
            "CASE WHEN priority = 'Высокий' THEN 1 " +
            "WHEN priority = 'Средний' THEN 2 " +
            "ELSE 3 END, deadline ASC")
    suspend fun getArchivedRemarksByControlPointId(controlPointId: Long): List<RemarkEntity>

    @Query("SELECT * FROM remark_table WHERE id = :id")
    suspend fun getRemarkById(id: Long): RemarkEntity?

    @Query("UPDATE remark_table SET status = :status, completedDate = :completedDate WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, completedDate: String)

    @Query("UPDATE remark_table SET isArchived = 1 WHERE id = :id")
    suspend fun archiveRemark(id: Long)

    @Query("UPDATE remark_table SET isArchived = 0 WHERE id = :id")
    suspend fun unarchiveRemark(id: Long)

    @Update
    suspend fun update(remark: RemarkEntity)

    @Delete
    suspend fun delete(remark: RemarkEntity)

    @Query("DELETE FROM remark_table WHERE controlPointId = :controlPointId")
    suspend fun deleteByControlPointId(controlPointId: Long)
}