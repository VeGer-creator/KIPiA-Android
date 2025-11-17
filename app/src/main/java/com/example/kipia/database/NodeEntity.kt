package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "node_table",
    foreignKeys = [
        ForeignKey(
            entity = TubeEntity::class,
            parentColumns = ["id"],
            childColumns = ["tubeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Название узла, например "ОД 937/1", "В 937/1", "Задвижка"
    val name: String,

    // ID трубы, к которой принадлежит узел
    val tubeId: Long,

    // Тип узла как строка
    val nodeType: String
)