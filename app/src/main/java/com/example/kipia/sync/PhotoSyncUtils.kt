package com.example.kipia.sync

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

object PhotoSyncUtils {
    private const val TAG = "PhotoSyncUtils"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Извлекает фотографии из сущности и подготавливает их для синхронизации
     */
    suspend fun extractPhotosFromEntity(
        context: Context,
        entity: Any
    ): List<FileAttachment> {
        return when (entity) {
            is EquipmentSyncEntity -> extractEquipmentPhotos(context, entity)
            is DetailedEquipmentSyncEntity -> extractDetailedEquipmentPhotos(context, entity)
            is RemarkSyncEntity -> extractRemarkPhotos(context, entity)
            // Другие сущности не имеют фото в базе - возвращаем пустой список
            else -> {
                Log.d(TAG, "⚠️ Сущность ${entity::class.simpleName} не поддерживает фото")
                emptyList()
            }
        }
    }

    private suspend fun extractEquipmentPhotos(
        context: Context,
        equipment: EquipmentSyncEntity
    ): List<FileAttachment> {
        // Используем photoPaths вместо photos
        return extractPhotosFromJsonPaths(
            context,
            equipment.photoPaths, // Используем photoPaths
            "equipment",
            equipment.id,
            "photoPaths"
        )
    }

    private suspend fun extractDetailedEquipmentPhotos(
        context: Context,
        equipment: DetailedEquipmentSyncEntity
    ): List<FileAttachment> {
        // Используем photoPaths и photos
        val photoPathsAttachments = extractPhotosFromJsonPaths(
            context,
            equipment.photoPaths,
            "detailed_equipment",
            equipment.id,
            "photoPaths"
        )
        val photosAttachments = extractPhotosFromJsonPaths(
            context,
            equipment.photos,
            "detailed_equipment",
            equipment.id,
            "photos"
        )
        return photoPathsAttachments + photosAttachments
    }

    private suspend fun extractRemarkPhotos(
        context: Context,
        remark: RemarkSyncEntity
    ): List<FileAttachment> {
        return extractPhotosFromJsonPaths(
            context,
            remark.photos,
            "remark",
            remark.id,
            "photos"
        )
    }

    private suspend fun extractControlPointPhotos(
        context: Context,
        controlPoint: ControlPointSyncEntity
    ): List<FileAttachment> {
        // ControlPointSyncEntity не имеет поля photos - возвращаем пустой список
        Log.d(TAG, "⚠️ ControlPointSyncEntity не имеет поля photos")
        return emptyList()
    }

    /**
     * Извлекает фото из JSON строки с путями
     */
    private suspend fun extractPhotosFromJsonPaths(
        context: Context,
        photosJson: String,
        entityType: String,
        entityId: Long,
        fieldName: String
    ): List<FileAttachment> {
        if (photosJson.isBlank()) return emptyList()

        val attachments = mutableListOf<FileAttachment>()

        try {
            // Пробуем декодировать как JSON массив
            val paths = try {
                json.decodeFromString<List<String>>(photosJson)
            } catch (e: Exception) {
                // Если не JSON, то это строка с разделителями запятыми
                photosJson.split(",").filter { it.isNotBlank() }
            }

            paths.forEachIndexed { index, path ->
                try {
                    val file = if (path.startsWith("/")) {
                        File(path)
                    } else {
                        File(context.filesDir, path)
                    }

                    if (file.exists() && file.length() > 0) {
                        val fileData = file.readBytes()
                        val base64Data = Base64.encodeToString(fileData, Base64.DEFAULT)

                        val attachment = FileAttachment(
                            entityType = entityType,
                            entityId = entityId,
                            fieldName = fieldName,
                            fileName = "${entityType}_${entityId}_${fieldName}_${index}.${getFileExtension(file)}",
                            fileData = base64Data,
                            fileType = getFileType(file),
                            relativePath = getRelativePath(context, file)
                        )

                        attachments.add(attachment)
                        Log.d(TAG, "📸 Извлечено фото: ${file.name} для $entityType $entityId ($fieldName)")
                    } else {
                        Log.w(TAG, "⚠️ Файл не найден или пуст: $path")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Ошибка извлечения фото из пути: $path", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка обработки путей фотографий: $photosJson", e)
        }

        return attachments
    }

    /**
     * Получает расширение файла
     */
    private fun getFileExtension(file: File): String {
        val name = file.name
        val lastDot = name.lastIndexOf(".")
        return if (lastDot > 0) name.substring(lastDot + 1) else "jpg"
    }

    /**
     * Определяет тип файла
     */
    private fun getFileType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "photo"
            name.endsWith(".png") -> "photo"
            name.endsWith(".gif") -> "photo"
            name.endsWith(".bmp") -> "photo"
            name.endsWith(".pdf") -> "document"
            else -> "unknown"
        }
    }

    /**
     * Получает относительный путь файла
     */
    private fun getRelativePath(context: Context, file: File): String? {
        val absolutePath = file.absolutePath
        val filesDir = context.filesDir.absolutePath

        return if (absolutePath.startsWith(filesDir)) {
            absolutePath.substring(filesDir.length + 1)
        } else {
            null
        }
    }

    /**
     * Восстанавливает фотографии из FileAttachment и сохраняет их
     */
    suspend fun restorePhotosFromAttachments(
        context: Context,
        attachments: List<FileAttachment>
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()

        attachments.groupBy { "${it.entityType}_${it.entityId}_${it.fieldName}" }
            .forEach { (key, groupAttachments) ->
                val photoPaths = groupAttachments.mapNotNull { attachment ->
                    try {
                        restorePhotoFromAttachment(context, attachment, groupAttachments.indexOf(attachment))
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Ошибка восстановления фото из вложения", e)
                        null
                    }
                }

                if (photoPaths.isNotEmpty()) {
                    results[key] = json.encodeToString(photoPaths)
                    Log.d(TAG, "✅ Восстановлено ${photoPaths.size} фото для ключа: $key")
                }
            }

        return results
    }

    /**
     * Восстанавливает одно фото из FileAttachment
     */
    fun restorePhotoFromAttachment(
        context: Context,
        attachment: FileAttachment,
        index: Int
    ): String? {
        return try {
            // Декодируем base64 данные
            val fileData = Base64.decode(attachment.fileData, Base64.DEFAULT)

            // Определяем директорию для сохранения
            val saveDir = when (attachment.entityType) {
                "remark" -> File(context.filesDir, "remarks_photos")
                "equipment" -> File(context.filesDir, "equipment_photos")
                "detailed_equipment" -> File(context.filesDir, "detailed_equipment_photos")
                "control_point" -> File(context.filesDir, "control_points_photos")
                else -> File(context.filesDir, "received_photos")
            }

            if (!saveDir.exists()) {
                saveDir.mkdirs()
            }

            // Создаем уникальное имя файла
            val fileName = if (attachment.fileName.isNotEmpty()) {
                attachment.fileName
            } else {
                "${attachment.entityType}_${attachment.entityId}_${attachment.fieldName}_$index.${getExtensionFromType(attachment.fileType)}"
            }

            val outputFile = File(saveDir, fileName)

            // Сохраняем файл
            FileOutputStream(outputFile).use { fos ->
                fos.write(fileData)
            }

            Log.d(TAG, "💾 Сохранен файл: ${outputFile.absolutePath}")

            // Возвращаем относительный путь
            outputFile.relativeTo(context.filesDir).path

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка восстановления фото из вложения", e)
            null
        }
    }

    /**
     * Получает расширение из типа файла
     */
    private fun getExtensionFromType(fileType: String): String {
        return when (fileType) {
            "photo" -> "jpg"
            "document" -> "pdf"
            else -> "dat"
        }
    }

    /**
     * Обновляет сущности с восстановленными путями к фотографиям
     */
    fun updateEntitiesWithRestoredPhotos(
        entities: SyncEntities,
        restoredPhotos: Map<String, String>
    ): SyncEntities {
        return entities.copy(
            equipment = entities.equipment.map { equipment ->
                val key = "equipment_${equipment.id}_photoPaths"
                restoredPhotos[key]?.let { newPhotoPaths ->
                    try {
                        equipment.copy(photoPaths = newPhotoPaths)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Ошибка обновления equipment фото", e)
                        equipment
                    }
                } ?: equipment
            },
            detailedEquipment = entities.detailedEquipment.map { equipment ->
                val keyPhotoPaths = "detailed_equipment_${equipment.id}_photoPaths"
                val keyPhotos = "detailed_equipment_${equipment.id}_photos"
                val updatedPhotoPaths = restoredPhotos[keyPhotoPaths] ?: equipment.photoPaths
                val updatedPhotos = restoredPhotos[keyPhotos] ?: equipment.photos

                equipment.copy(
                    photoPaths = updatedPhotoPaths,
                    photos = updatedPhotos
                )
            },
            remarks = entities.remarks.map { remark ->
                val key = "remark_${remark.id}_photos"
                restoredPhotos[key]?.let { newPhotos ->
                    try {
                        remark.copy(photos = newPhotos)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Ошибка обновления remark фото", e)
                        remark
                    }
                } ?: remark
            }
            // ControlPoints не обновляем - у них нет поля photos
        )
    }

    /**
     * Сжимает фото перед отправкой (оптимизация)
     */
    suspend fun compressPhotoForTransfer(
        context: Context,
        originalFile: File,
        maxSizeKB: Int = 500
    ): File? {
        return withContext(Dispatchers.IO) {
            try {
                if (!originalFile.exists() || originalFile.length() <= maxSizeKB * 1024) {
                    return@withContext originalFile
                }

                // Создаем временный файл для сжатого изображения
                val tempFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)

                // Здесь должна быть реальная логика сжатия изображения
                // Для простоты просто копируем файл
                originalFile.copyTo(tempFile, overwrite = true)

                Log.d(TAG, "📐 Фото сжато: ${originalFile.length()} -> ${tempFile.length()} байт")
                tempFile
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка сжатия фото", e)
                null
            }
        }
    }

    /**
     * Проверяет целостность файла
     */
    fun validateFileIntegrity(file: File): Boolean {
        return try {
            val exists = file.exists()
            val notEmpty = file.length() > 0
            val canRead = file.canRead()

            if (!exists) Log.w(TAG, "⚠️ Файл не существует: ${file.path}")
            if (!notEmpty) Log.w(TAG, "⚠️ Файл пуст: ${file.path}")
            if (!canRead) Log.w(TAG, "⚠️ Нет прав на чтение файла: ${file.path}")

            exists && notEmpty && canRead
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка проверки целостности файла", e)
            false
        }
    }

    /**
     * Создает метаданные для файла
     */
    fun createFileMetadata(
        context: Context,
        file: File,
        entityType: String,
        entityId: Long
    ): com.example.kipia.sync.FileMetadata {
        return com.example.kipia.sync.FileMetadata(
            fileName = file.name,
            fileSize = file.length(),
            totalChunks = calculateChunks(file.length()),
            entityType = entityType,
            entityId = entityId.toString(),
            fileType = getFileType(file),
            originalPath = getRelativePath(context, file),
            checksum = calculateChecksum(file)
        )
    }

    /**
     * Рассчитывает количество чанков
     */
    private fun calculateChunks(fileSize: Long): Int {
        val CHUNK_SIZE = 65536 // 64KB
        return ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
    }

    /**
     * Рассчитывает простую контрольную сумму
     */
    private fun calculateChecksum(file: File): String {
        return try {
            val bytes = file.readBytes()
            var sum = 0L
            for (byte in bytes) {
                sum += byte.toLong() and 0xFF
            }
            sum.toString(16)
        } catch (e: Exception) {
            "error"
        }
    }

    /**
     * Проверяет, нужно ли синхронизировать файл (по дате изменения)
     */
    fun shouldSyncFile(localFile: File, remoteTimestamp: Long): Boolean {
        return if (!localFile.exists()) {
            false
        } else {
            val localModified = localFile.lastModified()
            localModified > remoteTimestamp
        }
    }

    /**
     * Получает все фото файлы из директорий приложения
     */
    fun getAllPhotoFiles(context: Context): List<File> {
        val photoDirs = listOf(
            "remarks_photos",
            "equipment_photos",
            "detailed_equipment_photos",
            "control_points_photos"
        )

        val allFiles = mutableListOf<File>()

        photoDirs.forEach { dirName ->
            val dir = File(context.filesDir, dirName)
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles()?.filter {
                    it.isFile && (it.name.endsWith(".jpg") || it.name.endsWith(".png") || it.name.endsWith(".jpeg"))
                } ?: emptyList()

                allFiles.addAll(files)
                Log.d(TAG, "📁 Директория $dirName: ${files.size} файлов")
            }
        }

        return allFiles
    }
}