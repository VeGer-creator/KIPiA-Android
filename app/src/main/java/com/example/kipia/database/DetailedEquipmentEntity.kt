// DetailedEquipmentEntity.kt
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
    val equipmentType: String, // Тип оборудования из EquipmentType
    val name: String, // Название оборудования
    val nodeId: Long? = null, // Привязка к узлу (ОД, В, Задвижка)
    val sectionId: Long? = null, // Привязка к отсеку ПКУ

    // Общие характеристики
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val productionYear: String = "",
    val verificationYear: String = "",

    // Специфические характеристики
    val nominal: String = "", // Номинал
    val pressureLimit: String = "", // Граница давления для манометров
    val softwareVersion: String = "", // Версия ПО
    val mo: String = "", // МО
    val mz: String = "", // МЗ
    val mto: String = "", // МТО
    val mtz: String = "", // МТЗ
    val muo: String = "", // МУО
    val muz: String = "", // МУЗ
    val outputContacts: String = "" // Кол.об.вых.зв
)
