package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kipia.database.PKUEntity
import com.example.kipia.database.SectionEntity

@Composable
fun PKUItem(
    pku: PKUEntity,
    sectionViewModel: SectionViewModel,
    onEdit: (PKUEntity) -> Unit,
    onDelete: () -> Unit,
    onAddSection: (String, Long) -> Unit,
    onViewSectionEquipment: (Long) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showAddSectionDialog by remember { mutableStateOf(false) }

    val sections by sectionViewModel.sections.collectAsState()

    // Загружаем секции при создании
    LaunchedEffect(pku.id) {
        sectionViewModel.loadSectionsByPKUId(pku.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка раскрытия/скрытия секций
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Скрыть секции" else "Показать секции"
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pku.name,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    if (pku.description.isNotEmpty()) {
                        Text(
                            text = pku.description,
                            style = MaterialTheme.typography.body2,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Кнопка добавления секции
                IconButton(
                    onClick = { showAddSectionDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить секцию",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                // Кнопка редактирования ПКУ
                IconButton(
                    onClick = { onEdit(pku) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать ПКУ",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                // Кнопка удаления ПКУ
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить ПКУ",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }

            // Секции ПКУ (показываются при раскрытии)
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (sections.isEmpty()) {
                        Text(
                            text = "Нет секций",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 32.dp, top = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        sections.forEach { section ->
                            SectionItem(
                                section = section,
                                onViewEquipment = { onViewSectionEquipment(section.id) },
                                onDeleteSection = { sectionViewModel.deleteSection(section) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSectionDialog) {
        AddSectionDialog(
            pkuName = pku.name,
            onDismiss = { showAddSectionDialog = false },
            onConfirm = { sectionName ->
                onAddSection(sectionName, pku.id)
                showAddSectionDialog = false
            }
        )
    }
}

// Обновленный компонент для отображения секции
@Composable
fun SectionItem(
    section: SectionEntity,
    onViewEquipment: () -> Unit,
    onDeleteSection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 4.dp, bottom = 4.dp)
            .clickable { onViewEquipment() },
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.name,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colors.onSurface
            )

            // Кнопка просмотра оборудования
            IconButton(
                onClick = onViewEquipment,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Просмотр оборудования",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }

            // Кнопка удаления секции
            IconButton(
                onClick = onDeleteSection,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить секцию",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}