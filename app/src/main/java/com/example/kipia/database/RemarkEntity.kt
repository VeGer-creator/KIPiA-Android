package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import android.util.Log

@Entity(
    tableName = "remark_table",
    foreignKeys = [
        ForeignKey(
            entity = ControlPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["controlPointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["controlPointId"])]
)
data class RemarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val controlPointId: Long,
    val title: String,
    val description: String = "",
    val category: String = "Оборудование",
    val priority: String = "Средний",
    val status: String = "Открыто",
    val createdDate: String,
    val deadline: String,
    val completedDate: String = "",
    val photos: String = "",
    val isArchived: Boolean = false // ДОБАВЛЕНО ПОЛЕ АРХИВА
) {
    fun getPhotoList(): List<String> {
        Log.d("RemarkEntity", "Getting photos from: $photos")
        return if (photos.isNotEmpty()) {
            try {
                val paths = photos.split(",").filter { it.isNotBlank() }
                Log.d("RemarkEntity", "Parsed ${paths.size} photos")
                paths
            } catch (e: Exception) {
                Log.e("RemarkEntity", "Error parsing photos", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun withPhotos(photoPaths: List<String>): RemarkEntity {
        Log.d("RemarkEntity", "Setting ${photoPaths.size} photos")
        val photosString = if (photoPaths.isNotEmpty()) {
            photoPaths.joinToString(",")
        } else {
            ""
        }
        Log.d("RemarkEntity", "Photos string: $photosString")
        return this.copy(photos = photosString)
    }
}