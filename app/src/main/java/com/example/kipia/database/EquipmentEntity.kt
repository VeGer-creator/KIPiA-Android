package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipment_table",
    foreignKeys = [
        ForeignKey(
            entity = NodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["nodeId"],
            onDelete = ForeignKey.CASCADE // Если Узел удалится, оборудование тоже удалится
        ),
        ForeignKey(
            entity = SectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.CASCADE // Если Отсек удалится, оборудование тоже удалится
        )
    ]
)
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Название оборудования, например "Сигнализатор затопления", "Электропривод"
    val name: String,

    // ID узла, к которому принадлежит оборудование (может быть null)
    val nodeId: Long? = null,

    // ID отсека, к которому принадлежит оборудование (может быть null)
    val sectionId: Long? = null,

    // Модель, завод, номер, номинал, год поверки и т.д. - добавим позже
    val model: String = "",
    val manufacturer: String = "",
    val serialNumber: String = "",
    val nominal: String = "",
    val verificationYear: String = ""
)