package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "section_table",
    foreignKeys = [
        ForeignKey(
            entity = PKUEntity::class,
            parentColumns = ["id"],
            childColumns = ["pkuId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["pkuId"])] // ДОБАВИТЬ ЭТУ СТРОКУ
)
data class SectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val pkuId: Long
)