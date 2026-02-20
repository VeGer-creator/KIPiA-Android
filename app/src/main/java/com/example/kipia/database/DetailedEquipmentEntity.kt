package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detailed_equipment_table",
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
        Index(value = ["nodeId"]),
        Index(value = ["sectionId"])
    ]
)
data class DetailedEquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val equipmentType: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val name: String = "", // ИСПРАВЛЕНО: значение по умолчанию
    val nodeId: Long? = null,
    val sectionId: Long? = null,

    // Общие характеристики
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val productionYear: String = "",
    val verificationYear: String = "",

    // Специфические характеристики
    val nominal: String = "",
    val pressureLimit: String = "",
    val softwareVersion: String = "",
    val mo: String = "",
    val mz: String = "",
    val mto: String = "",
    val mtz: String = "",
    val muo: String = "",
    val muz: String = "",
    val outputContacts: String = "",

    // ДОБАВЛЕНО ПОЛЕ ДЛЯ ФОТО
    val photoPaths: String = ""
)