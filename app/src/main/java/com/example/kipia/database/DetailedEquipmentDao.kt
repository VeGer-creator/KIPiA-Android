// app/src/main/java/com/example/kipia/database/dao/DetailedEquipmentDao.kt
package com.example.kipia.database

import androidx.room.*
import com.example.kipia.database.DetailedEquipmentEntity

@Dao
interface DetailedEquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: DetailedEquipmentEntity)

    @Query("SELECT * FROM detailed_equipment_table WHERE nodeId = :nodeId")
    suspend fun getEquipmentByNodeId(nodeId: Long): List<DetailedEquipmentEntity>

    @Query("SELECT * FROM detailed_equipment_table WHERE sectionId = :sectionId")
    suspend fun getEquipmentBySectionId(sectionId: Long): List<DetailedEquipmentEntity>

    @Query("SELECT * FROM detailed_equipment_table WHERE id = :id")
    suspend fun getEquipmentById(id: Long): DetailedEquipmentEntity?

    @Update
    suspend fun update(equipment: DetailedEquipmentEntity)

    @Delete
    suspend fun delete(equipment: DetailedEquipmentEntity)

    @Query("DELETE FROM detailed_equipment_table WHERE nodeId = :nodeId")
    suspend fun deleteByNodeId(nodeId: Long)

    @Query("DELETE FROM detailed_equipment_table WHERE sectionId = :sectionId")
    suspend fun deleteBySectionId(sectionId: Long)
}