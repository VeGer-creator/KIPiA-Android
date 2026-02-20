// app/src/main/java/com/example/kipia/ui/EquipmentDetailScreen.kt
package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.model.EquipmentType
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun EquipmentDetailScreen(
    nodeName: String? = null,
    sectionName: String? = null,
    nodeId: Long? = null,
    sectionId: Long? = null,
    onBackClick: () -> Unit,
    onViewEquipment: (Long) -> Unit = {},
    onViewEquipmentPhotos: (Long, List<String>) -> Unit = { _, _ -> },
    onAddPhotosToEquipment: (Long, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    val equipment by equipmentViewModel.equipment.collectAsState()

    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showAddSectionEquipmentDialog by remember { mutableStateOf(false) }
    var currentSelectedEquipment by remember { mutableStateOf<Pair<Long, String>?>(null) }

    // Лаунчер для выбора фото
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        currentSelectedEquipment?.let { (equipmentId, _) ->
            if (uris.isNotEmpty()) {
                val photoPaths = com.example.kipia.utils.EquipmentPhotoUtils.saveEquipmentPhotos(context, uris)
                equipmentViewModel.addPhotosToEquipment(equipmentId, photoPaths)
            }
        }
        currentSelectedEquipment = null
    }

    // Формируем правильное название для заголовка
    val screenTitle = when {
        nodeName != null -> "Оборудование: $nodeName"
        sectionName != null -> "Оборудование: $sectionName"
        else -> "Оборудование"
    }

    // Загружаем оборудование при открытии экрана
    LaunchedEffect(nodeId, sectionId) {
        when {
            nodeId != null -> equipmentViewModel.loadEquipmentByNodeId(nodeId)
            sectionId != null -> equipmentViewModel.loadEquipmentBySectionId(sectionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            // Только один FAB для добавления оборудования
            FloatingActionButton(
                onClick = {
                    if (sectionId != null) {
                        showAddSectionEquipmentDialog = true
                    } else {
                        showAddEquipmentDialog = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить оборудование")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (equipment.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Оборудование не добавлено",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Нажмите + чтобы добавить оборудование",
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equipment) { item ->
                        EquipmentItemCard(
                            equipment = item,
                            onView = {
                                println("DEBUG: View equipment ${item.id}")
                                onViewEquipment(item.id)
                            },
                            onDelete = { equipmentViewModel.deleteEquipment(item) },
                            onViewPhotos = {
                                val photoPaths = com.example.kipia.utils.EquipmentPhotoUtils.getPhotoPathsFromJson(item.photoPaths)
                                if (photoPaths.isNotEmpty()) {
                                    println("DEBUG: View photos for equipment ${item.id}")
                                    onViewEquipmentPhotos(item.id, photoPaths)
                                } else {
                                    println("DEBUG: No photos for equipment ${item.id}")
                                }
                            },
                            onAddPhotos = {
                                println("DEBUG: Add photos to equipment ${item.id}")
                                currentSelectedEquipment = Pair(item.id, item.name)
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEquipmentDialog) {
        AddEquipmentDialog(
            nodeId = nodeId,
            sectionId = sectionId,
            nodeName = nodeName ?: "",
            onDismiss = { showAddEquipmentDialog = false },
            onConfirm = { type ->
                val newEquipment = com.example.kipia.database.DetailedEquipmentEntity(
                    equipmentType = type.name,
                    name = type.displayName,
                    nodeId = nodeId,
                    sectionId = sectionId
                )
                equipmentViewModel.addEquipment(newEquipment)
            }
        )
    }

    if (showAddSectionEquipmentDialog) {
        AddSectionEquipmentDialog(
            sectionName = sectionName ?: "секцию",
            onDismiss = { showAddSectionEquipmentDialog = false },
            onConfirm = { type ->
                val newEquipment = com.example.kipia.database.DetailedEquipmentEntity(
                    equipmentType = type.name,
                    name = type.displayName,
                    sectionId = sectionId
                )
                equipmentViewModel.addEquipment(newEquipment)
            }
        )
    }
}

@Composable
fun EquipmentItemCard(
    equipment: com.example.kipia.database.DetailedEquipmentEntity,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onViewPhotos: () -> Unit,
    onAddPhotos: () -> Unit
) {
    val photoPaths = com.example.kipia.utils.EquipmentPhotoUtils.getPhotoPathsFromJson(equipment.photoPaths)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                println("DEBUG: Equipment card clicked: ${equipment.name}")
                onView()
            },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // ВЕРХНЯЯ ЧАСТЬ: Название и характеристики в одной строке с кнопкой удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Левая часть: название и характеристики
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = equipment.name,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Характеристики в компактном виде
                    if (equipment.model.isNotEmpty() || equipment.serialNumber.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (equipment.model.isNotEmpty()) {
                                Text(
                                    text = "Модель: ${equipment.model}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            if (equipment.serialNumber.isNotEmpty()) {
                                Text(
                                    text = "№: ${equipment.serialNumber}",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                // Правая часть: кнопка удаления
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        "Удалить",
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            // НИЖНЯЯ ЧАСТЬ: Превью фото и кнопка добавления фото на одном уровне
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Левая часть: превью фото (если есть)
                if (photoPaths.isNotEmpty()) {
                    CompactPhotoPreviewRow(
                        photoPaths = photoPaths,
                        onPhotoClick = onViewPhotos,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Если нет фото - занимаем пространство
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Правая часть: кнопка добавления фото
                CompactAddPhotoButton(
                    onClick = onAddPhotos,
                    hasPhotos = photoPaths.isNotEmpty(),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// Компактное превью фото
@Composable
fun CompactPhotoPreviewRow(
    photoPaths: List<String>,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Фото:",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(end = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(photoPaths.take(3)) { path ->
                AsyncImage(
                    model = Uri.parse("file://$path"),
                    contentDescription = "Превью фото",
                    modifier = Modifier
                        .size(40.dp) // Уменьшенный размер
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onPhotoClick() },
                    contentScale = ContentScale.Crop
                )
            }

            // Показываем количество фото если их больше 3
            if (photoPaths.size > 3) {
                item {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${photoPaths.size - 3}",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.primary
                        )
                    }
                }
            }
        }
    }
}

// Компактная кнопка добавления фото
@Composable
fun CompactAddPhotoButton(
    onClick: () -> Unit,
    hasPhotos: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable { onClick() }
            .background(
                color = if (hasPhotos) MaterialTheme.colors.primary.copy(alpha = 0.1f)
                else MaterialTheme.colors.secondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.h6,
            color = if (hasPhotos) MaterialTheme.colors.primary else MaterialTheme.colors.secondary
        )
    }
}