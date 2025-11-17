package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pku_table",
    foreignKeys = [
        ForeignKey(
            entity = ControlPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["controlPointId"],
            onDelete = ForeignKey.CASCADE // Если КП удалится, ПКУ тоже удалится
        )
    ]
)
data class PKUEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false,

    // ID связанного КП
    val controlPointId: Long
)
