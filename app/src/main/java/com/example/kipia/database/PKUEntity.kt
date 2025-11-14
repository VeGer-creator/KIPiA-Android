package com.example.kipia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pku_table")
data class PKUEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val name: String,
    val description: String = "",
    val isCompleted: Boolean = false
)