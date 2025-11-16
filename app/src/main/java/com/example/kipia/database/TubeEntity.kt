package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tube_table",
    foreignKeys = [
        ForeignKey(
            entity = ControlPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["controlPointId"],
            onDelete = ForeignKey.CASCADE // Если КП удалится, труба тоже удалится
        )
    ]
)
data class TubeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Название трубы, например "МН СП" или "МН ХК"
    val name: String,

    // ID КП, к которому принадлежит труба
    val controlPointId: Long
)