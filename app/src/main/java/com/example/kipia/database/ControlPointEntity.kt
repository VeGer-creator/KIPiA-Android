package com.example.kipia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "control_point_table")
data class ControlPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Название КП, например "КП 937 км"
    val name: String,

    // Описание КП
    val description: String = ""

)

