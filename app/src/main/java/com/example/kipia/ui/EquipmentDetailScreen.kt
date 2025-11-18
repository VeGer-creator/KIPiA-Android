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

@Composable
fun EquipmentDetailScreen(
    nodeName: String? = null,
    sectionName: String? = null,
    nodeId: Long? = null,
    sectionId: Long? = null,
    onBackClick: () -> Unit,
    onViewEquipment: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    val equipment by equipmentViewModel.equipment.collectAsState()

    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showAddSectionEquipmentDialog by remember { mutableStateOf(false) }

    // Формируем правильное название для заголовка
    val screenTitle = when {
        nodeName != null -> nodeName
        sectionName != null -> sectionName
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
                    Text(
                        text = "Оборудование не добавлено",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(equipment) { item ->
                        EquipmentItemCard(
                            equipment = item,
                            onView = { onViewEquipment(item.id) },
                            onDelete = { equipmentViewModel.deleteEquipment(item) }
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
            nodeName = nodeName ?: "", // Передаем имя узла для фильтрации оборудования
            onDismiss = { showAddEquipmentDialog = false },
            onConfirm = { type ->
                // Создаем новое оборудование с названием по типу
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
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onView() },
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = equipment.name,
                style = MaterialTheme.typography.h6
            )

            if (equipment.model.isNotEmpty()) {
                Text(
                    text = "Модель: ${equipment.model}",
                    style = MaterialTheme.typography.body2
                )
            }
            if (equipment.serialNumber.isNotEmpty()) {
                Text(
                    text = "Заводской номер: ${equipment.serialNumber}",
                    style = MaterialTheme.typography.body2
                )
            }

            // Только кнопка удаления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Text("Удалить", color = MaterialTheme.colors.error)
                }
            }
        }
    }
}