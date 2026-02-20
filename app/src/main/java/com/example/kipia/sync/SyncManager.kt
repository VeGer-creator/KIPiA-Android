// app/src/main/java/com/example/kipia/sync/SyncManager.kt
package com.example.kipia.sync

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.kipia.data.PreferencesManager
import com.example.kipia.database.AppDatabase
import com.example.kipia.sync.EntityConverters.toSyncEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object SyncManager {
    private const val TAG = "SyncManager"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun prepareSyncData(context: Context): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Подготовка данных для синхронизации...")

                val database = AppDatabase.getInstance(context)
                val prefs = PreferencesManager(context)
                val deviceId = prefs.deviceId.first()

                // Собираем ВСЕ данные из всех таблиц
                val controlPoints = database.controlPointDao().getAllControlPoints()
                val pkus = database.pkuDao().getAllPKUs()
                val tubes = database.tubeDao().getAllTubes()
                val nodes = database.nodeDao().getAllNodes()
                val sections = database.sectionDao().getAllSections()
                val equipment = database.equipmentDao().getAllEquipment()
                val detailedEquipment = database.detailedEquipmentDao().getAllDetailedEquipment()
                val remarks = database.remarkDao().getAllRemarks()
                val events = database.eventDao().getAllEvents()

                Log.d(TAG, "📊 Статистика данных:")
                Log.d(TAG, "  • Контрольные точки: ${controlPoints.size}")
                Log.d(TAG, "  • ПКУ: ${pkus.size}")
                Log.d(TAG, "  • Трубы: ${tubes.size}")
                Log.d(TAG, "  • Узлы: ${nodes.size}")
                Log.d(TAG, "  • Отсеки: ${sections.size}")
                Log.d(TAG, "  • Оборудование: ${equipment.size}")
                Log.d(TAG, "  • Детальное оборудование: ${detailedEquipment.size}")
                Log.d(TAG, "  • Замечания: ${remarks.size}")
                Log.d(TAG, "  • События: ${events.size}")

                // Обновляем пути фотографий на относительные перед отправкой
                val updatedRemarks = remarks.map { remark ->
                    remark.copy(
                        photos = convertToRelativePaths(context, remark.photos ?: "")
                    )
                }

                val updatedEquipment = equipment.map { equip ->
                    equip.copy(
                        photoPaths = convertToRelativePaths(context, equip.photoPaths ?: "")
                    )
                }

                val updatedDetailedEquipment = detailedEquipment.map { detailed ->
                    detailed.copy(
                        photoPaths = convertToRelativePaths(context, detailed.photoPaths ?: "")
                        // Убрано поле photos - его нет в DetailedEquipmentSyncEntity
                    )
                }
                // Для ControlPoint нет поля photos в Entity, поэтому пропускаем

                val syncEntities = SyncEntities(
                    controlPoints = controlPoints.map { it.toSyncEntity(deviceId) },
                    pkus = pkus.map { it.toSyncEntity(deviceId) },
                    tubes = tubes.map { it.toSyncEntity(deviceId) },
                    nodes = nodes.map { it.toSyncEntity(deviceId) },
                    sections = sections.map { it.toSyncEntity(deviceId) },
                    equipment = updatedEquipment.map { it.toSyncEntity(deviceId) },
                    detailedEquipment = updatedDetailedEquipment.map { it.toSyncEntity(deviceId) },
                    remarks = updatedRemarks.map { it.toSyncEntity(deviceId) },
                    events = events.map { it.toSyncEntity(deviceId) }
                )

                // Создаем полный пакет с вложениями
                val fileAttachments = buildList<FileAttachment> {
                    // Используем EnhancedSyncManager для получения файлов
                    val files = EnhancedSyncManager.getSyncFiles(context)

                    files.take(10).forEach { file -> // Берем первые 10 файлов для теста
                        try {
                            val fileData = file.readBytes()
                            val base64Data = android.util.Base64.encodeToString(fileData, android.util.Base64.DEFAULT)

                            add(FileAttachment(
                                fileName = file.name,
                                fileData = base64Data,
                                entityType = EnhancedSyncManager.getEntityTypeFromFile(context, file),
                                entityId = EnhancedSyncManager.getEntityIdFromFile(context, file).toLong(),
                                fieldName = "photos", // Указываем поле для загрузки
                                fileType = EnhancedSyncManager.getFileTypeFromExtension(file),
                                relativePath = EnhancedSyncManager.getRelativePathFromAbsolute(context, file)
                            ))

                            Log.d(TAG, "📎 Добавлено вложение: ${file.name}")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Ошибка чтения файла: ${file.name}", e)
                        }
                    }
                }

                val syncPackage = SyncPackage(
                    deviceId = deviceId,
                    timestamp = System.currentTimeMillis(),
                    entities = syncEntities,
                    fileAttachments = fileAttachments
                )

                val result = json.encodeToString(syncPackage)
                Log.d(TAG, "✅ Данные подготовлены, размер: ${result.length} символов")
                Log.d(TAG, "📎 Вложений файлов: ${fileAttachments.size}")
                result
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка подготовки данных", e)
                // Возвращаем минимальный валидный JSON
                "{\"deviceId\":\"error\",\"timestamp\":0,\"entities\":{},\"fileAttachments\":[]}"
            }
        }
    }

    // Конвертирует абсолютные пути в относительные
    private fun convertToRelativePaths(context: Context, photosJson: String): String {
        if (photosJson.isEmpty()) return photosJson

        return try {
            val photoPaths = if (photosJson.startsWith("[")) {
                json.decodeFromString<List<String>>(photosJson)
            } else {
                // Если это не JSON массив, то это строка с разделителями
                photosJson.split(",").filter { it.isNotBlank() }
            }

            val relativePaths = photoPaths.mapNotNull { path ->
                if (path.startsWith("/")) {
                    // Абсолютный путь - конвертируем в относительный
                    val file = File(path)
                    if (file.exists()) {
                        val relativePath = file.relativeTo(context.filesDir).path
                        Log.d(TAG, "🔄 Конвертация пути: $path -> $relativePath")
                        relativePath
                    } else {
                        Log.w(TAG, "⚠️ Файл не существует: $path")
                        null
                    }
                } else {
                    // Уже относительный путь
                    path
                }
            }

            if (relativePaths.isEmpty()) {
                ""
            } else {
                json.encodeToString(relativePaths)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка конвертации путей", e)
            photosJson
        }
    }

    suspend fun processIncomingSyncData(context: Context, data: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 Обработка входящих данных...")

                val syncPackage = json.decodeFromString<SyncPackage>(data)
                Log.d(TAG, "📦 Получен пакет от устройства: ${syncPackage.deviceId}")
                Log.d(TAG, "📊 Статистика полученных данных:")
                Log.d(TAG, "  • Контрольные точки: ${syncPackage.entities.controlPoints.size}")
                Log.d(TAG, "  • ПКУ: ${syncPackage.entities.pkus.size}")
                Log.d(TAG, "  • Трубы: ${syncPackage.entities.tubes.size}")
                Log.d(TAG, "  • Узлы: ${syncPackage.entities.nodes.size}")
                Log.d(TAG, "  • Отсеки: ${syncPackage.entities.sections.size}")
                Log.d(TAG, "  • Оборудование: ${syncPackage.entities.equipment.size}")
                Log.d(TAG, "  • Детальное оборудование: ${syncPackage.entities.detailedEquipment.size}")
                Log.d(TAG, "  • Замечания: ${syncPackage.entities.remarks.size}")
                Log.d(TAG, "  • События: ${syncPackage.entities.events.size}")
                Log.d(TAG, "  • Вложений файлов: ${syncPackage.fileAttachments.size}")

                // Восстанавливаем фотографии из вложений
                if (syncPackage.fileAttachments.isNotEmpty()) {
                    Log.d(TAG, "🖼️ Восстановление фотографий из вложений...")

                    val restoredPhotos = mutableMapOf<Long, String>()

                    syncPackage.fileAttachments.forEachIndexed { index, attachment ->
                        try {
                            // Используем PhotoSyncUtils для восстановления файла
                            val restoredPath = PhotoSyncUtils.restorePhotoFromAttachment(
                                context,
                                attachment,
                                index
                            )

                            if (restoredPath != null) {
                                restoredPhotos[attachment.entityId] = restoredPath
                                Log.d(TAG, "✅ Восстановлено фото: ${attachment.fileName}")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Ошибка восстановления фото: ${attachment.fileName}", e)
                        }
                    }

                    Log.d(TAG, "🖼️ Восстановлено ${restoredPhotos.size} фотографий")

                    // Обновляем сущности с восстановленными путями
                    val updatedEntities = updateEntitiesWithPhotoPaths(
                        syncPackage.entities,
                        restoredPhotos
                    )

                    // Выполняем слияние данных с обновленными путями
                    val database = AppDatabase.getInstance(context)
                    val merger = DataMerger
                    val mergeResult = merger.mergeData(database, updatedEntities)

                    Log.d(TAG, "✅ Синхронизация завершена:")
                    Log.d(TAG, "  • Новых КП: ${mergeResult.controlPointsAdded}")
                    Log.d(TAG, "  • Новых ПКУ: ${mergeResult.pkusAdded}")
                    Log.d(TAG, "  • Новых труб: ${mergeResult.tubesAdded}")
                    Log.d(TAG, "  • Новых узлов: ${mergeResult.nodesAdded}")
                    Log.d(TAG, "  • Новых отсеков: ${mergeResult.sectionsAdded}")
                    Log.d(TAG, "  • Нового оборудования: ${mergeResult.equipmentAdded}")
                    Log.d(TAG, "  • Нового детального оборудования: ${mergeResult.detailedEquipmentAdded}")
                    Log.d(TAG, "  • Новых замечаний: ${mergeResult.remarksAdded}")
                    Log.d(TAG, "  • Новых событий: ${mergeResult.eventsAdded}")
                } else {
                    // Нет вложений - обычная синхронизация
                    val database = AppDatabase.getInstance(context)
                    val merger = DataMerger
                    val mergeResult = merger.mergeData(database, syncPackage.entities)

                    Log.d(TAG, "✅ Синхронизация завершена (без файлов):")
                    Log.d(TAG, "  • Новых КП: ${mergeResult.controlPointsAdded}")
                    Log.d(TAG, "  • Новых ПКУ: ${mergeResult.pkusAdded}")
                    Log.d(TAG, "  • Новых труб: ${mergeResult.tubesAdded}")
                    Log.d(TAG, "  • Новых узлов: ${mergeResult.nodesAdded}")
                    Log.d(TAG, "  • Новых отсеков: ${mergeResult.sectionsAdded}")
                    Log.d(TAG, "  • Нового оборудования: ${mergeResult.equipmentAdded}")
                    Log.d(TAG, "  • Нового детального оборудования: ${mergeResult.detailedEquipmentAdded}")
                    Log.d(TAG, "  • Новых замечаний: ${mergeResult.remarksAdded}")
                    Log.d(TAG, "  • Новых событий: ${mergeResult.eventsAdded}")
                }

                // Обновляем timestamp
                PreferencesManager(context).setLastSyncTimestamp(System.currentTimeMillis())

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки данных", e)
            }
        }
    }

    // Обновляет пути фотографий в сущностях
    private fun updateEntitiesWithPhotoPaths(
        entities: SyncEntities,
        restoredPhotos: Map<Long, String>
    ): SyncEntities {
        return entities.copy(
            remarks = entities.remarks.map { remark ->
                val photoPath = restoredPhotos[remark.id]
                if (photoPath != null) {
                    val currentPhotos = if (remark.photos.isNotEmpty()) {
                        val photosList = remark.photos.split(",").toMutableList()
                        photosList.add(photoPath)
                        photosList.joinToString(",")
                    } else {
                        photoPath
                    }
                    remark.copy(photos = currentPhotos)
                } else {
                    remark
                }
            },
            equipment = entities.equipment.map { equipment ->
                val photoPath = restoredPhotos[equipment.id]
                if (photoPath != null) {
                    val currentPhotoPaths = if (equipment.photoPaths.isNotEmpty()) {
                        val pathsList = equipment.photoPaths.split(",").toMutableList()
                        pathsList.add(photoPath)
                        pathsList.joinToString(",")
                    } else {
                        photoPath
                    }
                    equipment.copy(photoPaths = currentPhotoPaths)
                } else {
                    equipment
                }
            },
            detailedEquipment = entities.detailedEquipment.map { detailed ->
                val photoPath = restoredPhotos[detailed.id]
                if (photoPath != null) {
                    val currentPhotoPaths = if (detailed.photoPaths.isNotEmpty()) {
                        val pathsList = detailed.photoPaths.split(",").toMutableList()
                        pathsList.add(photoPath)
                        pathsList.joinToString(",")
                    } else {
                        photoPath
                    }
                    val currentPhotos = if (detailed.photos.isNotEmpty()) {
                        val photosList = detailed.photos.split(",").toMutableList()
                        photosList.add(photoPath)
                        photosList.joinToString(",")
                    } else {
                        photoPath
                    }
                    detailed.copy(
                        photoPaths = currentPhotoPaths,
                        photos = currentPhotos
                    )
                } else {
                    detailed
                }
            }
        )
    }

    // Новый метод: инициирует передачу файлов через Nearby
    suspend fun initiateFileTransfer(context: Context, endpointId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🚀 Инициализация передачи файлов для endpoint: $endpointId")

                // Получаем файлы и метаданные через EnhancedSyncManager
                val files = EnhancedSyncManager.getSyncFiles(context)

                if (files.isNotEmpty()) {
                    // Отправляем broadcast для начала передачи
                    val intent = Intent("com.example.kipia.START_FILE_TRANSFER").apply {
                        putExtra("endpointId", endpointId)
                        putExtra("fileCount", files.size)
                    }
                    context.sendBroadcast(intent)

                    Log.d(TAG, "📤 Передача ${files.size} файлов через Nearby")
                } else {
                    Log.d(TAG, "📭 Нет файлов для передачи")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка инициализации передачи файлов", e)
            }
        }
    }

    // Вспомогательный метод для отправки файлов
    private suspend fun sendFilesViaNearby(context: Context, endpointId: String) {
        Log.d(TAG, "📤 Запрос на отправку файлов для: $endpointId")
    }
}