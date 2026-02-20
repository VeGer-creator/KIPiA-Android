package com.example.kipia.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object PhotoStorageUtils {

    /**
     * Копирует фото из временного URI в постоянное хранилище приложения
     */
    fun copyPhotoToAppStorage(context: Context, uri: Uri): String? {
        return try {
            val fileName = "KIPiA_${System.currentTimeMillis()}.jpg"
            val storageDir = File(context.filesDir, "remarks_photos")

            // Создаем директорию если не существует
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val outputFile = File(storageDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Возвращаем путь к файлу внутри приложения
            outputFile.absolutePath

        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error copying photo", e)
            null
        }
    }

    /**
     * Получает URI для файла в хранилище приложения
     */
    fun getUriForAppFile(context: Context, filePath: String): Uri {
        return Uri.fromFile(File(filePath))
    }

    /**
     * Удаляет фото из хранилища приложения
     */
    fun deletePhotoFromStorage(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e("PhotoStorage", "Error deleting photo", e)
        }
    }

    /**
     * Конвертирует список URI в список постоянных путей
     */
    fun convertUrisToPersistentPaths(context: Context, uris: List<Uri>): List<String> {
        Log.d("PhotoStorage", "Converting ${uris.size} URIs to persistent paths")
        return uris.mapNotNull { uri ->
            copyPhotoToAppStorage(context, uri)
        }
    }
}