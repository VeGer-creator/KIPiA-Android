package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface EquipmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: EquipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipments: List<EquipmentEntity>)

    @Query("SELECT * FROM equipment_table WHERE nodeId = :nodeId")
    suspend fun getEquipmentByNodeId(nodeId: Long): List<EquipmentEntity>

    @Query("SELECT * FROM equipment_table WHERE sectionId = :sectionId")
    suspend fun getEquipmentBySectionId(sectionId: Long): List<EquipmentEntity>

    @Query("SELECT * FROM equipment_table WHERE id = :id")
    suspend fun getEquipmentById(id: Long): EquipmentEntity?

    @Delete
    suspend fun delete(equipment: EquipmentEntity)

    @Query("DELETE FROM equipment_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}