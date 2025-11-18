package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tube_table",
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
data class TubeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val controlPointId: Long
)