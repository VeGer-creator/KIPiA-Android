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
            childColumns = ["sectionId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["nodeId"]),
        Index(value = ["sectionId"])
    ]
)
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val nodeId: Long? = null,
    val sectionId: Long? = null,
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val nominal: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val verificationYear: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val photoPaths: String = ""
)