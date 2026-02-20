package com.example.kipia.utils

import android.content.Context
import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object EquipmentPhotoUtils {

    fun saveEquipmentPhotos(context: Context, uris: List<Uri>): List<String> {
        return uris.mapNotNull { uri ->
            PhotoStorageUtils.copyPhotoToAppStorage(context, uri)
        }
    }

    fun getPhotoPathsFromJson(json: String): List<String> {
        return if (json.isNotEmpty()) {
            try {
                // Простая реализация без сложной сериализации
                if (json.startsWith("[") && json.endsWith("]")) {
                    json.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotEmpty() }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun convertPhotoPathsToJson(photoPaths: List<String>): String {
        return try {
            Json.encodeToString(photoPaths)
        } catch (e: Exception) {
            // Альтернативная реализация если сериализация не работает
            if (photoPaths.isEmpty()) {
                "[]"
            } else {
                "[" + photoPaths.joinToString(",") { "\"$it\"" } + "]"
            }
        }
    }
}