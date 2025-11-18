package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipment_table",
    foreignKeys = [
        ForeignKey(
            entity = NodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["nodeId"]), // ДОБАВИТЬ ЭТУ СТРОКУ
        Index(value = ["sectionId"]) // ДОБАВИТЬ ЭТУ СТРОКУ
    ]
)
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val nodeId: Long? = null,
    val sectionId: Long? = null,
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val nominal: String = "",
    val verificationYear: String = ""
)