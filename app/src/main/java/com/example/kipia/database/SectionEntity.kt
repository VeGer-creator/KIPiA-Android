package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "section_table",
    foreignKeys = [
        ForeignKey(
            entity = PKUEntity::class,
            parentColumns = ["id"],
            childColumns = ["pkuId"],
            onDelete = ForeignKey.CASCADE // Если ПКУ удалится, отсек тоже удалится
        )
    ]
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Название отсека, например "Инженерный отсек", "Трансформаторный отсек"
    val name: String,

    // ID ПКУ, к которому принадлежит отсек
    val pkuId: Long
)