// app/src/main/java/com/example/kipia/ui/EquipmentPhotoScreen.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import com.example.kipia.database.AppDatabase

@Composable
fun EquipmentPhotoScreen(
    equipmentId: Long,
    equipmentName: String, // ДОБАВЛЕНО: имя оборудования
    onBackClick: () -> Unit,
    onViewFullScreen: (Uri) -> Unit
) {
    val context = LocalContext.current
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    // Лаунчер для добавления фото
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val photoPaths = com.example.kipia.utils.EquipmentPhotoUtils.saveEquipmentPhotos(context, uris)
            equipmentViewModel.addPhotosToEquipment(equipmentId, photoPaths)
        }
    }

    // Загружаем оборудование для получения актуальных фото
    val equipment by equipmentViewModel.equipment.collectAsState()
    val currentEquipment = equipment.find { it.id == equipmentId }

    // Обновляем заголовок при изменении оборудования
    val actualEquipmentName = currentEquipment?.name ?: equipmentName

    val photoPaths = currentEquipment?.let {
        com.example.kipia.utils.EquipmentPhotoUtils.getPhotoPathsFromJson(it.photoPaths)
    } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Фото оборудования")
                        Text(
                            text = actualEquipmentName,
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить фото")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (photoPaths.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Фото не добавлены",
                            style = MaterialTheme.typography.h6,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Оборудование: $actualEquipmentName",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить первое фото")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Информация об оборудовании вверху
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Оборудование:",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = actualEquipmentName,
                                    style = MaterialTheme.typography.body1
                                )
                                if (currentEquipment?.model?.isNotEmpty() == true) {
                                    Text(
                                        text = "Модель: ${currentEquipment.model}",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    items(photoPaths) { path ->
                        PhotoItem(
                            photoPath = path,
                            onViewFullScreen = { onViewFullScreen(Uri.parse("file://$path")) },
                            onDelete = { showDeleteDialog = path }
                        )
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления
    showDeleteDialog?.let { pathToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить фото?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(
                    onClick = {
                        equipmentViewModel.removePhotoFromEquipment(equipmentId, pathToDelete)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun PhotoItem(
    photoPath: String,
    onViewFullScreen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column {
            AsyncImage(
                model = Uri.parse("file://$photoPath"),
                contentDescription = "Фото оборудования",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onViewFullScreen() },
                contentScale = ContentScale.Crop
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить фото",
                        tint = MaterialTheme.colors.error
                    )
                }
            }
        }
    }
}