package com.example.kipia.sync

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.kipia.data.PreferencesManager
import com.example.kipia.database.AppDatabase
import com.example.kipia.sync.EntityConverters.toSyncEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object EnhancedSyncManager {
    private const val TAG = "EnhancedSyncManager"
    private val json = Json { ignoreUnknownKeys = true }

    // Новый метод: получает все файлы для синхронизации
    suspend fun getSyncFiles(context: Context): List<File> = withContext(Dispatchers.IO) {
        try {
            val allFiles = mutableListOf<File>()

            // Получаем все сущности
            val database = AppDatabase.getInstance(context)

            // 1. Файлы из remarks (замечаний)
            val remarks = database.remarkDao().getAllRemarks()
            remarks.forEach { remark ->
                val photos = getEntityPhotos(context, remark.photos, "remark", remark.id)
                allFiles.addAll(photos)
            }

            // 2. Файлы из equipment (оборудования) - используем photoPaths
            val equipmentList = database.equipmentDao().getAllEquipment()
            equipmentList.forEach { equipment ->
                val photos = getEntityPhotos(context, equipment.photoPaths, "equipment", equipment.id)
                allFiles.addAll(photos)
            }

            // 3. Файлы из detailedEquipment (детального оборудования) - используем photoPaths
            val detailedEquipmentList = database.detailedEquipmentDao().getAllDetailedEquipment()
            detailedEquipmentList.forEach { detailedEquipment ->
                val photos = getEntityPhotos(context, detailedEquipment.photoPaths, "detailed_equipment", detailedEquipment.id)
                allFiles.addAll(photos)
            }

            // 4. ControlPoints не имеют фотографий в Entity - пропускаем

            Log.d(TAG, "Найдено ${allFiles.size} файлов для синхронизации")

            // Фильтруем только существующие файлы
            return@withContext allFiles.filter { it.exists() && it.length() > 0 }

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при получении файлов для синхронизации", e)
            return@withContext emptyList()
        }
    }

    // Новый метод: создает метаданные для файлов
    suspend fun getFileMetadata(context: Context): List<FileMetadata> =
        withContext(Dispatchers.IO) {
            try {
                val metadataList = mutableListOf<FileMetadata>()

                val database = AppDatabase.getInstance(context)
                val prefs = PreferencesManager(context)
                val deviceId = prefs.deviceId.first()

                // 1. Метаданные для remarks
                val remarks = database.remarkDao().getAllRemarks()
                remarks.forEach { remark ->
                    val photos = getEntityPhotoFiles(context, remark.photos)
                    photos.forEach { file ->
                        metadataList.add(
                            FileMetadata(
                                fileName = file.name,
                                fileSize = file.length(),
                                totalChunks = calculateChunks(file.length()),
                                entityType = "remark",
                                entityId = remark.id.toString(),
                                fileType = getFileType(file),
                                originalPath = getRelativePath(context, file)
                            )
                        )
                    }
                }

                // 2. Метаданные для equipment - используем photoPaths
                val equipmentList = database.equipmentDao().getAllEquipment()
                equipmentList.forEach { equipment ->
                    val photos = getEntityPhotoFiles(context, equipment.photoPaths)
                    photos.forEach { file ->
                        metadataList.add(
                            FileMetadata(
                                fileName = file.name,
                                fileSize = file.length(),
                                totalChunks = calculateChunks(file.length()),
                                entityType = "equipment",
                                entityId = equipment.id.toString(),
                                fileType = getFileType(file),
                                originalPath = getRelativePath(context, file)
                            )
                        )
                    }
                }

                // 3. Метаданные для detailedEquipment - используем photoPaths
                val detailedEquipmentList = database.detailedEquipmentDao().getAllDetailedEquipment()
                detailedEquipmentList.forEach { detailedEquipment ->
                    val photos = getEntityPhotoFiles(context, detailedEquipment.photoPaths)
                    photos.forEach { file ->
                        metadataList.add(
                            FileMetadata(
                                fileName = file.name,
                                fileSize = file.length(),
                                totalChunks = calculateChunks(file.length()),
                                entityType = "detailed_equipment",
                                entityId = detailedEquipment.id.toString(),
                                fileType = getFileType(file),
                                originalPath = getRelativePath(context, file)
                            )
                        )
                    }
                }

                Log.d(TAG, "Создано ${metadataList.size} метаданных файлов")
                return@withContext metadataList

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при создании метаданных файлов", e)
                return@withContext emptyList()
            }
        }

    // Вспомогательный метод: получает файлы фотографий сущности
    private fun getEntityPhotos(context: Context, photosJson: String?, entityType: String, entityId: Long): List<File> {
        if (photosJson.isNullOrEmpty()) return emptyList()

        return try {
            // Пробуем декодировать как JSON, иначе как строку с разделителями
            val photoPaths = try {
                json.decodeFromString<List<String>>(photosJson)
            } catch (e: Exception) {
                photosJson.split(",").filter { it.isNotBlank() }
            }

            val files = mutableListOf<File>()

            photoPaths.forEach { photoPath ->
                val file = if (photoPath.startsWith("/")) {
                    File(photoPath)
                } else {
                    File(context.filesDir, photoPath)
                }

                if (file.exists()) {
                    files.add(file)
                    Log.d(TAG, "Найден файл для $entityType ID $entityId: ${file.name}")
                } else {
                    Log.w(TAG, "Файл не найден: $photoPath")
                }
            }

            files

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при парсинге фотографий для $entityType ID $entityId", e)
            emptyList()
        }
    }

    // Вспомогательный метод: получает файлы фотографий сущности (альтернативная версия)
    private fun getEntityPhotoFiles(context: Context, photosJson: String?): List<File> {
        if (photosJson.isNullOrEmpty()) return emptyList()

        return try {
            // Пробуем декодировать как JSON, иначе как строку с разделителями
            val photoPaths = try {
                json.decodeFromString<List<String>>(photosJson)
            } catch (e: Exception) {
                photosJson.split(",").filter { it.isNotBlank() }
            }

            photoPaths.mapNotNull { path ->
                val file = if (path.startsWith("/")) {
                    File(path)
                } else {
                    File(context.filesDir, path)
                }
                file.takeIf { it.exists() && it.length() > 0 }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Вспомогательный метод: вычисляет количество чанков
    private fun calculateChunks(fileSize: Long): Int {
        val CHUNK_SIZE = 65536 // 64KB
        return ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
    }

    // Вспомогательный метод: определяет тип файла
    private fun getFileType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "photo"
            name.endsWith(".png") -> "photo"
            name.endsWith(".gif") -> "photo"
            name.endsWith(".bmp") -> "photo"
            name.endsWith(".pdf") -> "document"
            name.endsWith(".doc") || name.endsWith(".docx") -> "document"
            name.endsWith(".xls") || name.endsWith(".xlsx") -> "document"
            else -> "unknown"
        }
    }

    // Вспомогательный метод: получает относительный путь файла
    fun getRelativePath(context: Context, file: File): String? {
        val absolutePath = file.absolutePath
        val filesDir = context.filesDir.absolutePath

        return if (absolutePath.startsWith(filesDir)) {
            absolutePath.substring(filesDir.length + 1)
        } else {
            null
        }
    }

    suspend fun prepareCompleteSyncPackage(context: Context): SyncPackage {
        return withContext(Dispatchers.IO) {
            try {
                val database = AppDatabase.getInstance(context)
                val prefs = PreferencesManager(context)
                val deviceId = prefs.deviceId.first()

                // Собираем все сущности - используем kotlin.runCatching для обработки ошибок
                val controlPoints = database.controlPointDao().getAllControlPoints()
                    .map { it.toSyncEntity(deviceId) }

                val pkus = database.pkuDao().getAllPKUs()
                    .map { it.toSyncEntity(deviceId) }

                val tubes = database.tubeDao().getAllTubes()
                    .map { it.toSyncEntity(deviceId) }

                val nodes = database.nodeDao().getAllNodes()
                    .map { it.toSyncEntity(deviceId) }

                val sections = database.sectionDao().getAllSections()
                    .map { it.toSyncEntity(deviceId) }

                val equipment = database.equipmentDao().getAllEquipment()
                    .map { it.toSyncEntity(deviceId) }

                val detailedEquipment = database.detailedEquipmentDao().getAllDetailedEquipment()
                    .map { it.toSyncEntity(deviceId) }

                val remarks = database.remarkDao().getAllRemarks()
                    .map { it.toSyncEntity(deviceId) }

                // Добавляем конвертер для EventEntity
                val events = database.eventDao().getAllEvents()
                    .map { event ->
                        // Создаем EventSyncEntity вручную, если нет конвертера
                        EventSyncEntity(
                            id = event.id,
                            controlPointId = event.controlPointId,
                            title = event.title,
                            description = event.description,
                            type = event.type,
                            date = event.date,
                            time = event.time,
                            isCompleted = event.isCompleted,
                            participants = event.participants,
                            lastModified = System.currentTimeMillis(),
                            deviceId = deviceId
                        )
                    }

                val syncEntities = SyncEntities(
                    controlPoints = controlPoints,
                    pkus = pkus,
                    tubes = tubes,
                    nodes = nodes,
                    sections = sections,
                    equipment = equipment,
                    detailedEquipment = detailedEquipment,
                    remarks = remarks,
                    events = events
                )

                // Собираем вложения (фотографии) - обновленная версия
                val fileAttachments = buildList<FileAttachment> {
                    // Собираем все файлы и конвертируем их в FileAttachment
                    val allFiles = getSyncFiles(context)
                    allFiles.forEach { file ->
                        try {
                            val fileData = file.readBytes()
                            val base64Data = Base64.encodeToString(fileData, Base64.DEFAULT)

                            add(FileAttachment(
                                entityType = determineEntityTypeFromFile(context, file),
                                entityId = determineEntityIdFromFile(context, file).toLong(), // конвертируем в Long
                                fieldName = "photos", // добавляем обязательный параметр
                                fileName = file.name,
                                fileData = base64Data,
                                fileType = getFileType(file),
                                relativePath = getRelativePath(context, file)
                            ))
                        } catch (e: Exception) {
                            Log.e(TAG, "Ошибка при чтении файла: ${file.name}", e)
                        }
                    }

                    Log.d(TAG, "Создано ${size} вложений файлов")
                }

                SyncPackage(
                    deviceId = deviceId,
                    timestamp = System.currentTimeMillis(),
                    entities = syncEntities,
                    fileAttachments = fileAttachments
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error preparing complete sync package", e)
                throw e
            }
        }
    }

    // Новый метод: определяет тип сущности по файлу
    fun determineEntityTypeFromFile(context: Context, file: File): String {
        val path = file.absolutePath

        return when {
            path.contains("remarks_photos") || path.contains("remark") -> "remark"
            path.contains("equipment_photos") || path.contains("equipment") -> "equipment"
            path.contains("detailed_equipment_photos") || path.contains("detailed_equipment") -> "detailed_equipment"
            path.contains("control_points_photos") || path.contains("control_point") -> "control_point"
            else -> "unknown"
        }
    }

    // Новый метод: определяет ID сущности по файлу (попытка из пути)
    fun determineEntityIdFromFile(context: Context, file: File): String {
        val prefs = PreferencesManager(context)
        val deviceId = kotlin.runCatching {
            runBlocking { prefs.deviceId.first() }
        }.getOrDefault("unknown_device")

        // Пытаемся извлечь ID из имени файла (например: remark_123_photo.jpg)
        val fileName = file.nameWithoutExtension
        val parts = fileName.split("_")

        if (parts.size >= 2) {
            // Предполагаем формат: entityType_id_rest
            val potentialId = parts.getOrNull(1)
            if (potentialId != null && potentialId.toLongOrNull() != null) {
                return potentialId // возвращаем только ID, а не deviceId + ID
            }
        }

        // Пробуем извлечь из пути
        val path = file.absolutePath
        val regex = ".*_(\\d+)_".toRegex()
        val matchResult = regex.find(path)
        matchResult?.groupValues?.getOrNull(1)?.let {
            return it
        }

        return "0" // возвращаем 0 по умолчанию
    }

    // Метод для определения типа файла по расширению
    fun getFileTypeFromExtension(file: File): String {
        return getFileType(file)
    }

    // Метод для получения относительного пути из абсолютного
    fun getRelativePathFromAbsolute(context: Context, file: File): String? {
        return getRelativePath(context, file)
    }

    suspend fun processCompleteSyncPackage(context: Context, syncPackage: SyncPackage) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing complete sync package from device: ${syncPackage.deviceId}")

                // Восстанавливаем фотографии
                val restoredPhotos = PhotoSyncUtils.restorePhotosFromAttachments(
                    context,
                    syncPackage.fileAttachments
                )

                // Обновляем сущности с восстановленными путями к фотографиям
                val updatedEntities = PhotoSyncUtils.updateEntitiesWithRestoredPhotos(
                    syncPackage.entities,
                    restoredPhotos
                )

                // Выполняем слияние
                val database = AppDatabase.getInstance(context)
                val merger = DataMerger
                val mergeResult = merger.mergeData(database, updatedEntities)

                // Обновляем timestamp последней синхронизации
                PreferencesManager(context).setLastSyncTimestamp(System.currentTimeMillis())

                Log.d(TAG, "Enhanced sync completed. Results: $mergeResult")
                Log.d(TAG, "Restored ${restoredPhotos.size} photo sets")

            } catch (e: Exception) {
                Log.e(TAG, "Error processing complete sync package", e)
                throw e
            }
        }
    }

    // Новый метод: обрабатывает полученные файлы и связывает их с сущностями
    suspend fun processReceivedFiles(context: Context, receivedFiles: List<File>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Обработка ${receivedFiles.size} полученных файлов")

                val database = AppDatabase.getInstance(context)

                receivedFiles.forEach { file ->
                    try {
                        // Определяем тип сущности из имени файла или пути
                        val entityInfo = parseEntityInfoFromFileName(file.name)

                        // Перемещаем файл в правильную директорию
                        val destinationDir = when (entityInfo.entityType) {
                            "remark" -> File(context.filesDir, "remarks_photos")
                            "equipment" -> File(context.filesDir, "equipment_photos")
                            "detailed_equipment" -> File(context.filesDir, "detailed_equipment_photos")
                            "control_point" -> File(context.filesDir, "control_points_photos")
                            else -> File(context.filesDir, "received_photos")
                        }

                        if (!destinationDir.exists()) {
                            destinationDir.mkdirs()
                        }

                        val destinationFile = File(destinationDir, file.name)
                        file.copyTo(destinationFile, overwrite = true)

                        Log.d(TAG, "Файл сохранен: ${destinationFile.absolutePath}")

                        // Обновляем сущность с новым путем к фото
                        updateEntityWithPhoto(database, entityInfo, destinationFile.relativeTo(context.filesDir).path)

                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при обработке файла: ${file.name}", e)
                    }
                }

                Log.d(TAG, "Все полученные файлы обработаны")
                return@withContext true

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при обработке полученных файлов", e)
                return@withContext false
            }
        }
    }

    // Вспомогательный метод: парсит информацию о сущности из имени файла
    private fun parseEntityInfoFromFileName(fileName: String): EntityInfo {
        // Предполагаем формат: entityType_entityId_rest.jpg
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val parts = nameWithoutExt.split("_")

        return if (parts.size >= 2) {
            EntityInfo(
                entityType = parts[0],
                entityId = parts[1].toLongOrNull() ?: 0L,
                fileName = fileName
            )
        } else {
            EntityInfo(
                entityType = "unknown",
                entityId = 0L,
                fileName = fileName
            )
        }
    }

    // Вспомогательный метод: обновляет сущность с новым путем к фото
    private suspend fun updateEntityWithPhoto(database: AppDatabase, entityInfo: EntityInfo, photoPath: String) {
        try {
            when (entityInfo.entityType) {
                "remark" -> {
                    val remark = database.remarkDao().getRemarkById(entityInfo.entityId)
                    if (remark != null) {
                        val currentPhotos = remark.photos
                        val photos = if (currentPhotos.isNullOrEmpty()) {
                            listOf(photoPath)
                        } else {
                            val existingPhotos = try {
                                json.decodeFromString<List<String>>(currentPhotos)
                            } catch (e: Exception) {
                                emptyList()
                            }
                            (existingPhotos + photoPath).distinct()
                        }

                        val updatedRemark = remark.copy(photos = json.encodeToString(photos))
                        database.remarkDao().update(updatedRemark)
                        Log.d(TAG, "Обновлено фото для замечания ID ${entityInfo.entityId}")
                    }
                }
                "equipment" -> {
                    val equipment = database.equipmentDao().getEquipmentById(entityInfo.entityId)
                    if (equipment != null) {
                        val currentPhotoPaths = equipment.photoPaths
                        val photoPaths = if (currentPhotoPaths.isNullOrEmpty()) {
                            listOf(photoPath)
                        } else {
                            val existingPhotoPaths = try {
                                json.decodeFromString<List<String>>(currentPhotoPaths)
                            } catch (e: Exception) {
                                emptyList()
                            }
                            (existingPhotoPaths + photoPath).distinct()
                        }

                        val updatedEquipment = equipment.copy(photoPaths = json.encodeToString(photoPaths))
                        database.equipmentDao().update(updatedEquipment)
                        Log.d(TAG, "Обновлено фото для оборудования ID ${entityInfo.entityId}")
                    }
                }
                // Добавьте обработку других типов сущностей по аналогии
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при обновлении сущности с фото", e)
        }
    }

    data class EntityInfo(
        val entityType: String,
        val entityId: Long,
        val fileName: String
    )

    suspend fun exportSyncDataToFile(context: Context, filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val syncPackage = prepareCompleteSyncPackage(context)
                val jsonData = json.encodeToString(syncPackage)

                File(filePath).writeText(jsonData)
                Log.d(TAG, "Sync data exported to: $filePath")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting sync data", e)
                false
            }
        }
    }

    suspend fun importSyncDataFromFile(context: Context, filePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonData = File(filePath).readText()
                val syncPackage = json.decodeFromString<SyncPackage>(jsonData)

                processCompleteSyncPackage(context, syncPackage)
                Log.d(TAG, "Sync data imported from: $filePath")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error importing sync data", e)
                false
            }
        }
    }

    // Метод для получения ID сущности по файлу (публичная версия)
    fun getEntityIdFromFile(context: Context, file: File): Long {
        val idString = determineEntityIdFromFile(context, file)
        return idString.toLongOrNull() ?: 0L
    }

    // Метод для получения типа сущности по файлу (публичная версия)
    fun getEntityTypeFromFile(context: Context, file: File): String {
        return determineEntityTypeFromFile(context, file)
    }
}