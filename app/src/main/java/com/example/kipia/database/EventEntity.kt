package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_table",
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
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val controlPointId: Long,
    val title: String,                    // Краткое название события
    val description: String = "",         // Описание
    val type: String = "Проверка",       // "Проверка", "Обслуживание", "Ремонт"
    val date: String,                     // Дата события
    val time: String = "",                // Время
    val isCompleted: Boolean = false,
    val participants: String = ""         // Участники (JSON)
)