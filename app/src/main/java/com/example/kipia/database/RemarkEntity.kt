// app/src/main/java/com/example/kipia/database/RemarkEntity.kt
package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "remark_table",
    foreignKeys = [
        ForeignKey(
            entity = ControlPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["controlPointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["controlPointId"])]
)
data class RemarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val controlPointId: Long,
    val remarkNumber: String, // Номер замечания
    val description: String, // Описание замечания
    val deadline: String, // Срок устранения
    val commission: Boolean = false, // Замечание комиссии
    val isCompleted: Boolean = false, // Выполнено
    val completionDate: String = "", // Дата выполнения
    val photosPath: String = "" // Пути к фотографиям (разделенные запятыми)
)