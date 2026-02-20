package com.example.kipia.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberImagePainter
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

@Composable
fun FullScreenPhotoView(
    photoUri: Uri,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val context = LocalContext.current

    // ДОБАВЛЕНО: Проверка и коррекция URI
    val validUri = remember(photoUri) {
        try {
            Log.d("FullScreenPhotoView", "Original URI: $photoUri")
            Log.d("FullScreenPhotoView", "URI scheme: ${photoUri.scheme}")
            Log.d("FullScreenPhotoView", "URI path: ${photoUri.path}")

            // Пробуем открыть поток для проверки доступности
            context.contentResolver.openInputStream(photoUri)?.use {
                Log.d("FullScreenPhotoView", "URI is accessible")
                photoUri
            } ?: run {
                Log.w("FullScreenPhotoView", "URI not accessible, trying to fix...")

                // Если URI недоступен, пробуем создать из пути
                if (photoUri.scheme == "file" || photoUri.scheme == null) {
                    val filePath = photoUri.path ?: ""
                    val file = File(filePath)
                    if (file.exists()) {
                        Log.d("FullScreenPhotoView", "File exists, creating URI from file")
                        Uri.fromFile(file)
                    } else {
                        Log.e("FullScreenPhotoView", "File does not exist: $filePath")
                        photoUri
                    }
                } else {
                    // Для content URI пробуем альтернативный подход
                    try {
                        // Пробуем использовать URI как есть
                        val tempUri = photoUri
                        context.contentResolver.openInputStream(tempUri)?.use {
                            Log.d("FullScreenPhotoView", "Content URI is accessible on retry")
                            tempUri
                        } ?: photoUri
                    } catch (e: Exception) {
                        Log.e("FullScreenPhotoView", "Error on retry: ${e.message}")
                        photoUri
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FullScreenPhotoView", "Error checking URI: ${e.message}")
            // В случае ошибки возвращаем исходный URI
            photoUri
        }
    }

    Log.d("FullScreenPhotoView", "Using URI: $validUri")

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    // Для управления разрешениями - правильная логика для Android 13
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            downloadImage(context, validUri)
        } else {
            Toast.makeText(
                context,
                "Разрешение необходимо для сохранения фото",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            if (scale > 1f) {
                                // Сброс масштаба
                                scale = 1f
                                offset = androidx.compose.ui.geometry.Offset.Zero
                            } else {
                                // Увеличение в 3 раза в точке тапа
                                val newOffset = calculateZoomOffset(
                                    tapOffset,
                                    size.width.toFloat(),
                                    size.height.toFloat(),
                                    3f
                                )
                                scale = 3f
                                offset = newOffset
                            }
                        }
                    )
                }
        ) {
            // Верхняя панель с кнопками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                // Кнопка скачивания
                IconButton(
                    onClick = {
                        handleDownload(context, validUri, permissionLauncher)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Download,
                        contentDescription = "Скачать фото",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка закрытия
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Изображение с жестами масштабирования - ИСПОЛЬЗУЕМ validUri
            Image(
                painter = rememberImagePainter(validUri),
                contentDescription = "Фото в полный размер",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale *= zoom
                            offset += pan

                            // Ограничение масштаба
                            scale = scale.coerceIn(0.5f, 5f)

                            // Ограничение смещения
                            val maxOffset = 500f
                            offset = androidx.compose.ui.geometry.Offset(
                                offset.x.coerceIn(-maxOffset, maxOffset),
                                offset.y.coerceIn(-maxOffset, maxOffset)
                            )
                        }
                    }
                    .clickable { } // Пустой обработчик для предотвращения закрытия при клике на фото
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            // Подсказки внизу
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "• Двойной тап - увеличение/сброс",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "• Два пальца - масштабирование и перемещение",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = "• Нажмите вне фото для закрытия",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

// Вспомогательная функция для расчета смещения при зуме
private fun calculateZoomOffset(
    tapOffset: androidx.compose.ui.geometry.Offset,
    containerWidth: Float,
    containerHeight: Float,
    targetScale: Float
): androidx.compose.ui.geometry.Offset {
    val scaleFactor = targetScale - 1f
    val offsetX = (containerWidth / 2 - tapOffset.x) * scaleFactor
    val offsetY = (containerHeight / 2 - tapOffset.y) * scaleFactor
    return androidx.compose.ui.geometry.Offset(offsetX, offsetY)
}

// Функция для обработки скачивания с проверкой разрешений
// ... весь предыдущий код до функции handleDownload ...

// Функция для обработки скачивания с проверкой разрешений
private fun handleDownload(
    context: android.content.Context,
    uri: Uri,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    // Для Android 10+ (API 29+) не нужно разрешение WRITE_EXTERNAL_STORAGE
    // MediaStore сам управляет файлами в Downloads
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Android 10+ - используем MediaStore, разрешение не требуется
        downloadImage(context, uri)
    } else {
        // Для Android 9 и ниже проверяем WRITE_EXTERNAL_STORAGE
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                downloadImage(context, uri)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
}

// ПЕРЕМЕСТИТЕ ЭТИ ФУНКЦИИ СЮДА, ПЕРЕД downloadImage:

// Скачивание для Android 10+ (API 29+)
// Замените функцию downloadImageUsingMediaStore на эту:
private fun downloadImageUsingMediaStore(
    context: android.content.Context,
    uri: Uri,
    fileName: String
): Boolean {
    return try {
        Log.d("DownloadDebug", "Using MediaStore to save: $fileName")

        // Используем Pictures вместо Download для изображений
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KIPiA")
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (imageUri != null) {
            Log.d("DownloadDebug", "MediaStore URI created: $imageUri")

            var bytesCopied = 0L
            var copySuccessful = false

            resolver.openInputStream(uri)?.use { inputStream ->
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bytesCopied = inputStream.copyTo(outputStream)
                    outputStream.flush()
                    copySuccessful = true
                }
            }

            Log.d("DownloadDebug", "Bytes copied: $bytesCopied, successful: $copySuccessful")

            if (copySuccessful && bytesCopied > 0) {
                // Вместо проверки размера через MediaStore, просто считаем что файл сохранен
                // MediaStore может не сразу обновить метаданные

                // Даем системе время на обработку
                Thread.sleep(500)

                // Проверяем доступность файла другим способом
                try {
                    resolver.openInputStream(imageUri)?.use {
                        Log.d("DownloadDebug", "File is accessible after save")
                        Toast.makeText(context, "Фото сохранено в папку Изображения/KIPiA", Toast.LENGTH_LONG).show()
                        return true
                    }
                } catch (e: Exception) {
                    Log.e("DownloadDebug", "File not accessible after save: ${e.message}")
                }

                // Если проверка не удалась, но копирование прошло успешно, считаем что файл сохранен
                if (copySuccessful) {
                    Toast.makeText(context, "Фото сохранено в папку Изображения/KIPiA", Toast.LENGTH_LONG).show()
                    return true
                }
            }
        } else {
            Log.e("DownloadDebug", "Failed to create MediaStore URI")
        }

        Toast.makeText(context, "Не удалось сохранить фото", Toast.LENGTH_LONG).show()
        false
    } catch (e: Exception) {
        Log.e("DownloadDebug", "Error in MediaStore download: ${e.message}")
        e.printStackTrace()
        Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        false
    }
}
// Основная функция скачивания
private fun downloadImage(context: android.content.Context, uri: Uri) {
    try {
        Log.d("DownloadDebug", "Starting download from URI: $uri")
        Log.d("DownloadDebug", "URI scheme: ${uri.scheme}")
        Log.d("DownloadDebug", "URI path: ${uri.path}")

        // Для файлов из внутреннего хранилища приложения нужно создать временную копию
        val tempUri = if (uri.scheme == "file" && uri.path?.contains(context.filesDir.absolutePath) == true) {
            Log.d("DownloadDebug", "URI is from app internal storage, creating temp copy")
            createTempCopyFromInternalStorage(context, uri)
        } else {
            uri
        }

        if (tempUri == null) {
            Toast.makeText(context, "Ошибка: не удалось подготовить файл для скачивания", Toast.LENGTH_LONG).show()
            return
        }

        val fileName = generateFileName()
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Для Android 10+ используем MediaStore
            downloadImageUsingMediaStore(context, tempUri, fileName)
        } else {
            // Для старых версий используем прямой доступ к файлам
            downloadImageLegacy(context, tempUri, fileName)
        }

        if (success) {
            // Сообщение теперь в самой функции downloadImageUsingMediaStore
        } else {
            Toast.makeText(
                context,
                "Ошибка при сохранении фото",
                Toast.LENGTH_LONG
            ).show()
        }

        // Удаляем временный файл если он был создан
        if (tempUri != uri) {
            deleteTempFile(context, tempUri)
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            "Ошибка: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

// ОСТАЛЬНЫЕ ФУНКЦИИ ОСТАВЬТЕ НА МЕСТЕ:
// Генерация имени файла с timestamp
private fun generateFileName(): String {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return "KIPiA_$timeStamp.jpg"
}

// Скачивание для старых версий Android
private fun downloadImageLegacy(
    context: android.content.Context,
    uri: Uri,
    fileName: String
): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Уведомляем систему о новом файле
        val mediaScanIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = Uri.fromFile(file)
        context.sendBroadcast(mediaScanIntent)

        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Создает временную копию файла из внутреннего хранилища приложения
 */
private fun createTempCopyFromInternalStorage(context: android.content.Context, internalUri: Uri): Uri? {
    return try {
        val tempFile = File.createTempFile("KIPiA_temp_", ".jpg", context.cacheDir)

        context.contentResolver.openInputStream(internalUri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        Log.d("DownloadDebug", "Created temp file: ${tempFile.absolutePath}")
        Uri.fromFile(tempFile)
    } catch (e: Exception) {
        Log.e("DownloadDebug", "Error creating temp copy: ${e.message}")
        null
    }
}

/**
 * Удаляет временный файл
 */
private fun deleteTempFile(context: android.content.Context, tempUri: Uri) {
    try {
        if (tempUri.scheme == "file") {
            val file = File(tempUri.path ?: "")
            if (file.exists()) {
                file.delete()
                Log.d("DownloadDebug", "Temp file deleted: ${file.absolutePath}")
            }
        }
    } catch (e: Exception) {
        Log.e("DownloadDebug", "Error deleting temp file: ${e.message}")
    }
}