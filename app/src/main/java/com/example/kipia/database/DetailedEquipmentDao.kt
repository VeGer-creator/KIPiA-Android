package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface DetailedEquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: DetailedEquipmentEntity)

    @Query("SELECT * FROM detailed_equipment_table WHERE nodeId = :nodeId")
    suspend fun getEquipmentByNodeId(nodeId: Long): List<DetailedEquipmentEntity>

    @Query("SELECT * FROM detailed_equipment_table WHERE sectionId = :sectionId")
    suspend fun getEquipmentBySectionId(sectionId: Long): List<DetailedEquipmentEntity>

    @Query("SELECT * FROM detailed_equipment_table")
    suspend fun getAllDetailedEquipment(): List<DetailedEquipmentEntity>

    @Query("SELECT * FROM detailed_equipment_table WHERE id = :id")
    suspend fun getEquipmentById(id: Long): DetailedEquipmentEntity?

    @Delete
    suspend fun delete(equipment: DetailedEquipmentEntity)

    @Query("DELETE FROM detailed_equipment_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE detailed_equipment_table SET photoPaths = :photoPaths WHERE id = :id")
    suspend fun updatePhotoPaths(id: Long, photoPaths: String)

    @Query("SELECT photoPaths FROM detailed_equipment_table WHERE id = :id")
    suspend fun getPhotoPaths(id: Long): String?

    @Update
    suspend fun update(equipment: DetailedEquipmentEntity)
}