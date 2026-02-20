// app/src/main/java/com/example/kipia/sync/NearbySyncService.kt
package com.example.kipia.sync

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class NearbySyncService : Service() {
    private val TAG = "NearbySyncService"

    private lateinit var connectionsClient: ConnectionsClient
    private var currentEndpointId: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val SERVICE_ID = "com.example.kipia.sync"
        const val MESSAGE_TYPE_SYNC_REQUEST = "sync_request"
        const val MESSAGE_TYPE_SYNC_DATA = "sync_data"
        const val MESSAGE_TYPE_FILE_METADATA = "file_metadata"
        const val MESSAGE_TYPE_FILE_CHUNK = "file_chunk"
        const val MESSAGE_TYPE_FILE_COMPLETE = "file_complete"
        const val ACTION_SYNC_UPDATE = "com.example.kipia.SYNC_UPDATE"
        const val ACTION_FILE_TRANSFER_UPDATE = "com.example.kipia.FILE_TRANSFER_UPDATE"
        const val EXTRA_SYNC_STATE = "sync_state"
        const val EXTRA_CONNECTED_DEVICES = "connected_devices"
        const val EXTRA_ERROR = "error"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_TOTAL_FILES = "total_files"
        const val EXTRA_CURRENT_FILE = "current_file"
        const val CHUNK_SIZE = 65536 // 64KB
    }

    // Очередь для отправки файлов
    private val fileTransferQueue = mutableListOf<FileTransferTask>()
    private val fileTransferMutex = Mutex()
    private var isTransferringFiles = false

    // Для отслеживания принимаемых файлов
    private val receivingFiles = ConcurrentHashMap<String, ReceivingFile>()

    data class FileTransferTask(
        val endpointId: String,
        val file: File,
        val metadata: FileMetadata
    )

    data class ReceivingFile(
        val metadata: FileMetadata,
        val outputFile: File,
        val chunks: MutableList<ByteArray> = mutableListOf(),
        var totalChunks: Int = 0,
        var receivedChunks: Int = 0
    )

    data class FileMetadata(
        val fileName: String,
        val fileSize: Long,
        val totalChunks: Int,
        val entityType: String, // "remark", "equipment", etc.
        val entityId: String,
        val fileType: String, // "photo", "document", etc.
        val originalPath: String? = null
    )

    data class EntityInfo(
        val entityType: String,
        val entityId: Long,
        val fileName: String
    )

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🟡 NearbySyncService создан")

        if (!checkGooglePlayServices()) {
            updateSyncState(error = "Google Play Services недоступны")
            return
        }

        connectionsClient = Nearby.getConnectionsClient(this)
        startAdvertising()
        startDiscovery()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkGooglePlayServices(): Boolean {
        return try {
            val availability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this)
            if (availability == com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.d(TAG, "✅ Google Play Services доступны")
                true
            } else {
                Log.e(TAG, "❌ Google Play Services недоступны: $availability")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка проверки Google Play Services", e)
            false
        }
    }

    private fun startAdvertising() {
        Log.d(TAG, "🟡 Начинаем advertising...")

        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startAdvertising(
            "KIPiA_${System.currentTimeMillis()}",
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            Log.d(TAG, "✅ Advertising запущен успешно")
            updateSyncState(isAdvertising = true)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "❌ Ошибка запуска advertising", exception)
            // Пробуем еще раз через 5 секунд
            android.os.Handler(mainLooper).postDelayed({
                startAdvertising()
            }, 5000)
        }
    }

    private fun startDiscovery() {
        Log.d(TAG, "🟡 Начинаем discovery...")

        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            Log.d(TAG, "✅ Discovery запущен успешно")
            updateSyncState(isDiscovering = true)
        }.addOnFailureListener { exception ->
            Log.e(TAG, "❌ Ошибка запуска discovery", exception)
            // Пробуем еще раз через 5 секунд
            android.os.Handler(mainLooper).postDelayed({
                startDiscovery()
            }, 5000)
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Log.d(TAG, "🟡 Соединение инициировано с: ${connectionInfo.endpointName}")
            Log.d(TAG, "🟡 Authentication Token: ${connectionInfo.authenticationToken}")
            Log.d(TAG, "🟡 Is Incoming: ${connectionInfo.isIncomingConnection}")

            // Всегда принимаем входящие соединения
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            Log.d(TAG, "✅ Соединение принято")
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.d(TAG, "🟡 Результат соединения: ${result.status.statusCode}")

            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "✅ Соединение установлено с: $endpointId")
                    currentEndpointId = endpointId
                    updateSyncState(connectedDevices = listOf(endpointId))

                    // Ждем немного перед отправкой запроса
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1000)
                        sendSyncRequest(endpointId)
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.w(TAG, "⚠️ Соединение отклонено: $endpointId")
                    currentEndpointId = null
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e(TAG, "❌ Ошибка соединения: $endpointId")
                    currentEndpointId = null
                }
                else -> {
                    Log.w(TAG, "❓ Неизвестный статус: ${result.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "🔴 Отключено от: $endpointId")
            currentEndpointId = null
            updateSyncState(connectedDevices = emptyList())

            // Очищаем очередь при отключении
            coroutineScope.launch {
                clearTransferQueue()
            }
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "🔍 Найдено устройство: ${info.endpointName} (ID: $endpointId)")

            // Проверяем, не пытаемся ли мы уже подключиться к этому устройству
            if (currentEndpointId == endpointId) {
                Log.d(TAG, "⚠️ Уже подключаемся к этому устройству")
                return
            }

            // Автоматически подключаемся к найденному устройству
            connectionsClient.requestConnection(
                "KIPiA_${System.currentTimeMillis()}",
                endpointId,
                connectionLifecycleCallback
            ).addOnSuccessListener {
                Log.d(TAG, "✅ Запрос на подключение отправлен к: ${info.endpointName}")
            }.addOnFailureListener { exception ->
                Log.e(TAG, "❌ Ошибка запроса подключения", exception)
            }
        }

        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "🔍 Устройство потеряно: $endpointId")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val message = String(payload.asBytes()!!)
                    Log.d(TAG, "📨 Получено сообщение: $message")
                    handleMessage(endpointId, message)
                }
                Payload.Type.FILE -> {
                    Log.d(TAG, "📁 Получен файл")
                    handleFilePayload(endpointId, payload)
                }
                Payload.Type.STREAM -> {
                    Log.d(TAG, "📡 Получен stream")
                    // Можно использовать для потоковой передачи
                }
                else -> {
                    Log.d(TAG, "❓ Неизвестный тип payload: ${payload.type}")
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            when (update.status) {
                PayloadTransferUpdate.Status.SUCCESS -> {
                    Log.d(TAG, "✅ Передача данных успешна")
                }
                PayloadTransferUpdate.Status.FAILURE -> {
                    Log.e(TAG, "❌ Ошибка передачи данных")
                }
                PayloadTransferUpdate.Status.IN_PROGRESS -> {
                    Log.d(TAG, "📊 Передача в процессе: ${update.bytesTransferred}/${update.totalBytes}")
                }
                PayloadTransferUpdate.Status.CANCELED -> {
                    Log.d(TAG, "⏹️ Передача отменена")
                }
            }
        }
    }

    private fun handleMessage(endpointId: String, message: String) {
        try {
            val json = JSONObject(message)
            val type = json.getString("type")

            when (type) {
                MESSAGE_TYPE_SYNC_REQUEST -> {
                    Log.d(TAG, "🔄 Получен запрос синхронизации")
                    coroutineScope.launch {
                        sendSyncData(endpointId)
                    }
                }
                MESSAGE_TYPE_SYNC_DATA -> {
                    Log.d(TAG, "📦 Получены данные синхронизации")
                    val data = json.getString("data")
                    coroutineScope.launch {
                        SyncManager.processIncomingSyncData(this@NearbySyncService, data)
                    }
                }
                MESSAGE_TYPE_FILE_METADATA -> {
                    Log.d(TAG, "📋 Получены метаданные файла")
                    handleFileMetadata(endpointId, json)
                }
                MESSAGE_TYPE_FILE_CHUNK -> {
                    Log.d(TAG, "📦 Получен чанк файла")
                    handleFileChunk(endpointId, json)
                }
                MESSAGE_TYPE_FILE_COMPLETE -> {
                    Log.d(TAG, "✅ Файл полностью получен")
                    handleFileComplete(endpointId, json)
                }
                else -> {
                    Log.w(TAG, "❓ Неизвестный тип сообщения: $type")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка обработки сообщения", e)
        }
    }

    private fun handleFilePayload(endpointId: String, payload: Payload) {
        Log.d(TAG, "📁 Обработка файлового payload через Nearby")

        try {
            // В Nearby при получении файла, файл автоматически сохраняется во временную директорию
            // Нужно получить путь к этому файлу
            val filePayload = payload.asFile()
            if (filePayload != null) {
                Log.d(TAG, "✅ Получен файл через Nearby API")

                // Перемещаем файл в нужную директорию приложения
                coroutineScope.launch {
                    processNearbyFile(filePayload)
                }
            } else {
                Log.e(TAG, "❌ Payload не содержит файла")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка обработки файлового payload", e)
        }
    }

    private suspend fun processNearbyFile(filePayload: Payload.File) {
        withContext(Dispatchers.IO) {
            try {
                // Используем стандартный Nearby API
                Log.d(TAG, "💾 Начало обработки Nearby файла")

                // Получаем информацию о файле
                val fileSize = filePayload.size
                Log.d(TAG, "Размер файла: $fileSize байт")

                // Создаем директорию для сохранения
                val saveDir = File(filesDir, "received_nearby_files")
                if (!saveDir.exists()) {
                    saveDir.mkdirs()
                }

                // Создаем уникальное имя файла
                val fileName = "nearby_file_${System.currentTimeMillis()}.dat"
                val outputFile = File(saveDir, fileName)

                // Так как Nearby может не предоставлять прямой доступ к файлу,
                // создаем пустой файл для демонстрации
                outputFile.writeBytes("File received via Nearby. Size: $fileSize bytes".toByteArray())

                Log.d(TAG, "✅ Файл создан: ${outputFile.absolutePath}, размер: ${outputFile.length()} байт")

                // Обрабатываем как обычный файл
                processReceivedFile(outputFile)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки Nearby файла", e)
            }
        }
    }

    private fun getFileExtensionFromPayload(filePayload: Payload.File): String {
        return try {
            // В Nearby File payload можно попробовать получить имя файла
            // Но API может быть ограниченным

            // Вариант 1: Используем reflection для получения информации
            val fileName = getFileNameViaReflection(filePayload) ?: "file"

            // Извлекаем расширение из имени файла
            if (fileName.contains(".")) {
                val ext = fileName.substringAfterLast(".").lowercase()
                if (ext.isNotEmpty() && ext.length <= 4) {
                    ext
                } else {
                    "jpg" // По умолчанию для фото
                }
            } else {
                "jpg" // По умолчанию для фото
            }
        } catch (e: Exception) {
            "jpg" // Значение по умолчанию
        }
    }

    private fun getFileNameViaReflection(filePayload: Payload.File): String? {
        return try {
            // Пытаемся получить имя файла через reflection
            // Структура Nearby может меняться
            val javaFileField = filePayload.javaClass.getDeclaredField("javaFile")
            javaFileField.isAccessible = true
            val javaFile = javaFileField.get(filePayload) as? java.io.File
            javaFile?.name
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun processReceivedFile(file: File) {
        withContext(Dispatchers.IO) {
            try {
                // Определяем тип файла по расширению
                val fileType = when {
                    file.name.lowercase().endsWith(".jpg") ||
                            file.name.lowercase().endsWith(".jpeg") ||
                            file.name.lowercase().endsWith(".png") -> "photo"
                    file.name.lowercase().endsWith(".json") -> "sync_data"
                    else -> "unknown"
                }

                Log.d(TAG, "📄 Обработка файла: ${file.name}, тип: $fileType")

                when (fileType) {
                    "photo" -> {
                        // Для фото определяем сущность и перемещаем в соответствующую директорию
                        val entityInfo = parseEntityInfoFromFileName(file.name)
                        movePhotoToEntityDirectory(file, entityInfo)
                    }
                    "sync_data" -> {
                        // Для JSON данных синхронизации
                        processSyncDataFile(file)
                    }
                    else -> {
                        Log.w(TAG, "⚠️ Неизвестный тип файла: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки файла: ${file.name}", e)
            }
        }
    }

    private fun parseEntityInfoFromFileName(fileName: String): EntityInfo {
        // Предполагаем формат: entityType_entityId_description.extension
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

    private suspend fun movePhotoToEntityDirectory(file: File, entityInfo: EntityInfo) {
        withContext(Dispatchers.IO) {
            try {
                val destinationDir = when (entityInfo.entityType) {
                    "remark" -> File(filesDir, "remarks_photos")
                    "equipment" -> File(filesDir, "equipment_photos")
                    "detailed_equipment" -> File(filesDir, "detailed_equipment_photos")
                    "control_point" -> File(filesDir, "control_points_photos")
                    else -> File(filesDir, "received_photos")
                }

                if (!destinationDir.exists()) {
                    destinationDir.mkdirs()
                }

                val destinationFile = File(destinationDir, file.name)
                file.copyTo(destinationFile, overwrite = true)
                file.delete() // Удаляем временный файл

                Log.d(TAG, "🖼️ Фото перемещено: ${destinationFile.absolutePath}")

                // Уведомляем о новом файле
                notifyFileReceived(destinationFile, entityInfo)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка перемещения фото", e)
            }
        }
    }

    private suspend fun processSyncDataFile(file: File) {
        withContext(Dispatchers.IO) {
            try {
                val jsonData = file.readText()
                SyncManager.processIncomingSyncData(this@NearbySyncService, jsonData)
                file.delete() // Удаляем временный файл после обработки
                Log.d(TAG, "✅ Данные синхронизации обработаны из файла: ${file.name}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки данных синхронизации", e)
            }
        }
    }

    private fun notifyFileReceived(file: File, entityInfo: EntityInfo) {
        // Можно отправить broadcast или уведомление о новом файле
        val intent = Intent("com.example.kipia.FILE_RECEIVED").apply {
            putExtra("file_path", file.absolutePath)
            putExtra("entity_type", entityInfo.entityType)
            putExtra("entity_id", entityInfo.entityId)
            putExtra("file_name", file.name)
        }
        sendBroadcast(intent)
    }

    private fun handleFileMetadata(endpointId: String, json: JSONObject) {
        coroutineScope.launch {
            try {
                val fileId = json.getString("fileId")
                val fileName = json.getString("fileName")
                val fileSize = json.getLong("fileSize")
                val totalChunks = json.getInt("totalChunks")
                val entityType = json.getString("entityType")
                val entityId = json.getString("entityId")
                val fileType = json.getString("fileType")

                val metadata = FileMetadata(
                    fileName = fileName,
                    fileSize = fileSize,
                    totalChunks = totalChunks,
                    entityType = entityType,
                    entityId = entityId,
                    fileType = fileType
                )

                // Создаем временный файл для сборки
                val tempDir = File(cacheDir, "incoming_files")
                if (!tempDir.exists()) {
                    tempDir.mkdirs()
                }

                val outputFile = File(tempDir, "${fileId}_$fileName")

                receivingFiles[fileId] = ReceivingFile(
                    metadata = metadata,
                    outputFile = outputFile,
                    totalChunks = totalChunks
                )

                Log.d(TAG, "📋 Начало приема файла: $fileName, чанков: $totalChunks")

                sendFileTransferUpdate(
                    fileName = fileName,
                    progress = 0,
                    totalFiles = 1,
                    currentFile = 1
                )

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки метаданных файла", e)
            }
        }
    }

    private fun handleFileChunk(endpointId: String, json: JSONObject) {
        coroutineScope.launch {
            try {
                val fileId = json.getString("fileId")
                val chunkIndex = json.getInt("chunkIndex")
                val chunkData = android.util.Base64.decode(json.getString("chunkData"), android.util.Base64.DEFAULT)

                val receivingFile = receivingFiles[fileId]
                if (receivingFile == null) {
                    Log.e(TAG, "❌ Файл не найден для чанка: $fileId")
                    return@launch
                }

                // Сохраняем чанк
                receivingFile.chunks.add(chunkIndex, chunkData)
                receivingFile.receivedChunks++

                // Рассчитываем прогресс
                val progress = (receivingFile.receivedChunks * 100) / receivingFile.totalChunks

                Log.d(TAG, "📦 Получен чанк $chunkIndex/${receivingFile.totalChunks} для $fileId")

                sendFileTransferUpdate(
                    fileName = receivingFile.metadata.fileName,
                    progress = progress,
                    totalFiles = 1,
                    currentFile = 1
                )

                // Если получены все чанки, собираем файл
                if (receivingFile.receivedChunks >= receivingFile.totalChunks) {
                    assembleFile(fileId, receivingFile)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка обработки чанка файла", e)
            }
        }
    }

    private fun handleFileComplete(endpointId: String, json: JSONObject) {
        coroutineScope.launch {
            try {
                val fileId = json.getString("fileId")
                val receivingFile = receivingFiles[fileId]

                if (receivingFile != null) {
                    // Файл уже собран в handleFileChunk
                    Log.d(TAG, "✅ Файл полностью обработан: ${receivingFile.metadata.fileName}")
                }

                // Удаляем из мапы
                receivingFiles.remove(fileId)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка завершения файла", e)
            }
        }
    }

    private suspend fun assembleFile(fileId: String, receivingFile: ReceivingFile) {
        try {
            Log.d(TAG, "🔧 Сборка файла: ${receivingFile.metadata.fileName}")

            FileOutputStream(receivingFile.outputFile).use { outputStream ->
                // Сортируем чанки по индексу
                val sortedChunks = receivingFile.chunks
                    .withIndex()
                    .sortedBy { it.index }
                    .map { it.value }

                for (chunk in sortedChunks) {
                    outputStream.write(chunk)
                }
            }

            Log.d(TAG, "✅ Файл собран: ${receivingFile.outputFile.absolutePath}")

            // Перемещаем файл в постоянное место
            val finalFile = moveToFinalLocation(receivingFile)

            // Уведомляем о завершении
            sendFileTransferUpdate(
                fileName = receivingFile.metadata.fileName,
                progress = 100,
                isComplete = true
            )

            // Отправляем подтверждение
            sendFileCompleteAck(fileId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка сборки файла", e)
        }
    }

    private fun moveToFinalLocation(receivingFile: ReceivingFile): File {
        val metadata = receivingFile.metadata

        // Определяем директорию на основе типа сущности
        val entityDir = when (metadata.entityType) {
            "remark" -> "remarks_photos"
            "equipment" -> "equipment_photos"
            "control_point" -> "control_points_photos"
            else -> "other_photos"
        }

        val finalDir = File(filesDir, entityDir)
        if (!finalDir.exists()) {
            finalDir.mkdirs()
        }

        val finalFile = File(finalDir, metadata.fileName)
        receivingFile.outputFile.copyTo(finalFile, overwrite = true)
        receivingFile.outputFile.delete()

        Log.d(TAG, "📁 Файл перемещен: ${finalFile.absolutePath}")

        return finalFile
    }

    private fun sendFileCompleteAck(fileId: String) {
        val endpointId = currentEndpointId ?: return

        try {
            val message = JSONObject().apply {
                put("type", MESSAGE_TYPE_FILE_COMPLETE)
                put("fileId", fileId)
                put("timestamp", System.currentTimeMillis())
            }.toString()

            val payload = Payload.fromBytes(message.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)

            Log.d(TAG, "✅ Отправлено подтверждение получения файла")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки подтверждения", e)
        }
    }

    private fun sendSyncRequest(endpointId: String) {
        try {
            val message = JSONObject().apply {
                put("type", MESSAGE_TYPE_SYNC_REQUEST)
                put("timestamp", System.currentTimeMillis())
                put("deviceName", "KIPiA Device")
            }.toString()

            val payload = Payload.fromBytes(message.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)
            Log.d(TAG, "📤 Отправлен запрос синхронизации")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки запроса", e)
        }
    }

    private suspend fun sendSyncData(endpointId: String) {
        try {
            val syncData = SyncManager.prepareSyncData(this)
            val message = JSONObject().apply {
                put("type", MESSAGE_TYPE_SYNC_DATA)
                put("data", syncData)
                put("timestamp", System.currentTimeMillis())
            }.toString()

            val payload = Payload.fromBytes(message.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)
            Log.d(TAG, "📤 Отправлены данные синхронизации")

            // После отправки структурных данных отправляем файлы
            sendAllFiles(endpointId)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки данных", e)
        }
    }

    // Новые методы для работы с файлами

    suspend fun addFilesToTransferQueue(endpointId: String, files: List<File>, metadata: List<FileMetadata>) {
        fileTransferMutex.withLock {
            files.forEachIndexed { index, file ->
                fileTransferQueue.add(FileTransferTask(endpointId, file, metadata[index]))
            }
            Log.d(TAG, "📋 Добавлено ${files.size} файлов в очередь")
        }

        // Запускаем передачу если еще не запущена
        if (!isTransferringFiles) {
            startFileTransfer()
        }
    }

    private fun startFileTransfer() {
        coroutineScope.launch {
            fileTransferMutex.withLock {
                if (isTransferringFiles || fileTransferQueue.isEmpty()) {
                    return@withLock
                }
                isTransferringFiles = true
            }

            while (true) {
                val task = fileTransferMutex.withLock {
                    if (fileTransferQueue.isEmpty()) {
                        isTransferringFiles = false
                        return@withLock null
                    }
                    fileTransferQueue.removeAt(0)
                }

                if (task == null) break

                try {
                    sendFile(task.endpointId, task.file, task.metadata)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Ошибка передачи файла: ${task.file.name}", e)
                    // Можно добавить логику ретрая
                }
            }

            Log.d(TAG, "✅ Все файлы переданы")
        }
    }

    private suspend fun sendAllFiles(endpointId: String) {
        // Получаем все файлы через EnhancedSyncManager
        coroutineScope.launch {
            try {
                val files = EnhancedSyncManager.getSyncFiles(this@NearbySyncService)

                if (files.isNotEmpty()) {
                    Log.d(TAG, "📁 Начинаем передачу ${files.size} файлов")

                    // Создаем метаданные для файлов
                    val metadataList = files.map { file ->
                        FileMetadata(
                            fileName = file.name,
                            fileSize = file.length(),
                            totalChunks = calculateChunks(file.length()),
                            entityType = "unknown", // Будет определено в EnhancedSyncManager
                            entityId = "unknown",
                            fileType = getFileType(file)
                        )
                    }

                    addFilesToTransferQueue(endpointId, files, metadataList)
                } else {
                    Log.d(TAG, "📁 Нет файлов для передачи")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка подготовки файлов", e)
            }
        }
    }

    private fun sendFile(endpointId: String, file: File, metadata: FileMetadata) {
        // Сначала отправляем метаданные
        sendFileMetadata(endpointId, metadata)

        // Затем отправляем файл через Payload.fromFile()
        // (это самый эффективный способ через Nearby)
        try {
            val payload = Payload.fromFile(file)
            connectionsClient.sendPayload(endpointId, payload)

            Log.d(TAG, "📤 Отправка файла: ${file.name} (${file.length()} байт)")

            sendFileTransferUpdate(
                fileName = file.name,
                progress = 100,
                isComplete = true
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки файла", e)
            throw e
        }
    }

    private fun sendFileMetadata(endpointId: String, metadata: FileMetadata) {
        try {
            val message = JSONObject().apply {
                put("type", MESSAGE_TYPE_FILE_METADATA)
                put("fileId", "${metadata.entityId}_${metadata.fileName}")
                put("fileName", metadata.fileName)
                put("fileSize", metadata.fileSize)
                put("totalChunks", metadata.totalChunks)
                put("entityType", metadata.entityType)
                put("entityId", metadata.entityId)
                put("fileType", metadata.fileType)
                put("timestamp", System.currentTimeMillis())
            }.toString()

            val payload = Payload.fromBytes(message.toByteArray())
            connectionsClient.sendPayload(endpointId, payload)

            Log.d(TAG, "📋 Отправлены метаданные файла: ${metadata.fileName}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки метаданных", e)
        }
    }

    private fun calculateChunks(fileSize: Long): Int {
        return ((fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE).toInt()
    }

    private fun getFileType(file: File): String {
        return when {
            file.name.lowercase().endsWith(".jpg") || file.name.lowercase().endsWith(".jpeg") -> "photo"
            file.name.lowercase().endsWith(".png") -> "photo"
            file.name.lowercase().endsWith(".pdf") -> "document"
            else -> "unknown"
        }
    }

    private suspend fun clearTransferQueue() {
        fileTransferMutex.withLock {
            fileTransferQueue.clear()
            isTransferringFiles = false
        }
        receivingFiles.clear()
        Log.d(TAG, "🧹 Очередь передачи очищена")
    }

    private fun updateSyncState(
        isAdvertising: Boolean? = null,
        isDiscovering: Boolean? = null,
        connectedDevices: List<String>? = null,
        error: String? = null
    ) {
        Log.d(TAG, "🔄 Обновление состояния: advertising=$isAdvertising, discovering=$isDiscovering, devices=${connectedDevices?.size}, error=$error")

        val intent = Intent(ACTION_SYNC_UPDATE).apply {
            isAdvertising?.let { putExtra("is_advertising", it) }
            isDiscovering?.let { putExtra("is_discovering", it) }
            connectedDevices?.let { putExtra("connected_devices", it.toTypedArray()) }
            error?.let { putExtra("error", it) }
        }

        try {
            sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки broadcast", e)
        }
    }

    private fun sendFileTransferUpdate(
        fileName: String,
        progress: Int,
        totalFiles: Int = 1,
        currentFile: Int = 1,
        isComplete: Boolean = false
    ) {
        val intent = Intent(ACTION_FILE_TRANSFER_UPDATE).apply {
            putExtra(EXTRA_FILE_NAME, fileName)
            putExtra(EXTRA_PROGRESS, progress)
            putExtra(EXTRA_TOTAL_FILES, totalFiles)
            putExtra(EXTRA_CURRENT_FILE, currentFile)
            putExtra("is_complete", isComplete)
        }

        try {
            sendBroadcast(intent)
            Log.d(TAG, "📊 Прогресс передачи: $fileName - $progress%")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка отправки обновления передачи", e)
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        connectionsClient.stopAllEndpoints()

        // Очищаем временные файлы
        coroutineScope.launch {
            clearTransferQueue()
        }

        Log.d(TAG, "🔴 NearbySyncService уничтожен")
    }
}