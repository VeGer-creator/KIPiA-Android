package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pku_table",
    foreignKeys = [
        ForeignKey(
            entity = ControlPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["controlPointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["controlPointId"])] // ДОБАВИТЬ ЭТУ СТРОКУ
)
data class PKUEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val controlPointId: Long
)