// app/src/main/java/com/example/kipia/database/EventEntity.kt
package com.example.kipia.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_table")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val controlPointId: Long,
    val title: String,
    val description: String,
    val date: String,
    val isCompleted: Boolean = false
)