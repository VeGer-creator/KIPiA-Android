package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kipia.database.RemarkEntity

@Composable
fun RemarksTab(
    remarks: List<RemarkEntity>,
    onAddRemarkWithPhotos: (String, String, String, String, String, List<String>) -> Unit,
    onEditRemark: (RemarkEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    controlPointName: String = ""
) {
    var selectedCategory by remember { mutableStateOf("Все") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingRemark by remember { mutableStateOf<RemarkEntity?>(null) }
    var viewingRemark by remember { mutableStateOf<RemarkEntity?>(null) }

    val filteredRemarks = if (selectedCategory == "Все") {
        remarks
    } else {
        remarks.filter { it.category == selectedCategory }
    }

    // ИСПОЛЬЗУЕМ Box для плавающей кнопки
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // УБИРАЕМ КНОПКУ ИЗ ВЕРХНЕЙ ЧАСТИ

            // Статистика
            if (remarks.isNotEmpty()) {
                RemarkStats(remarks)
            }

            // Быстрые фильтры
            RemarkFilters(
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Список замечаний
            if (filteredRemarks.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(filteredRemarks) { remark ->
                        // КАРТОЧКА С КЛИКОМ ДЛЯ ПРОСМОТРА
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { viewingRemark = remark }, // ОТКРЫВАЕМ ПРОСМОТР
                            elevation = 2.dp,
                            backgroundColor = getRemarkCardColor(remark.priority, remark.status)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // Первая строка: заголовок + приоритет + дата
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = remark.title,
                                        style = MaterialTheme.typography.subtitle1,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    PriorityBadge(remark.priority)

                                    Text(
                                        text = remark.deadline,
                                        style = MaterialTheme.typography.caption,
                                        color = getDeadlineColor(remark.deadline),
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                // Вторая строка: описание (если есть)
                                if (remark.description.isNotEmpty()) {
                                    Text(
                                        text = remark.description,
                                        style = MaterialTheme.typography.body2,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                // Индикатор фото
                                val photoPaths = remark.getPhotoList()
                                if (photoPaths.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📷 ${photoPaths.size} фото",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                // Третья строка: категория + статус + действия
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Категория
                                    Text(
                                        text = when (remark.category) {
                                            "Документация" -> "Документы"
                                            "Оборудование" -> "Оборуд."
                                            else -> remark.category
                                        },
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Селектор статуса
                                    StatusDropdown(
                                        currentStatus = remark.status,
                                        onStatusChange = { newStatus ->
                                            onUpdateStatus(remark.id, newStatus)
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Кнопка редактирования
                                    IconButton(
                                        onClick = { editingRemark = remark },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Редактировать",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Сообщение, если замечаний нет
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (remarks.isEmpty()) "Нет замечаний" else "Нет замечаний в выбранной категории",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )

                        if (remarks.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Создать первое замечание")
                            }
                        }
                    }
                }
            }
        }

        // ПЛАВАЮЩАЯ КНОПКА В ПРАВОМ НИЖНЕМ УГЛУ
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить замечание", tint = MaterialTheme.colors.onPrimary)
        }
    }

    // Диалоги (без изменений)
    if (showAddDialog) {
        AddRemarkDialog(
            controlPointName = controlPointName,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, description, category, priority, deadline, photoPaths ->
                onAddRemarkWithPhotos(title, description, category, priority, deadline, photoPaths)
                showAddDialog = false
            }
        )
    }

    if (editingRemark != null) {
        EditRemarkDialog(
            remark = editingRemark!!,
            onDismiss = { editingRemark = null },
            onConfirm = { title, description, category, priority, deadline, photoPaths ->
                val updatedRemark = editingRemark!!.copy(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    deadline = deadline
                ).withPhotos(photoPaths)

                onEditRemark(updatedRemark)
                editingRemark = null
            }
        )
    }

    if (viewingRemark != null) {
        ViewRemarkDialog(
            remark = viewingRemark!!,
            onDismiss = { viewingRemark = null },
            onEdit = {
                editingRemark = viewingRemark
                viewingRemark = null
            }
        )
    }
}

// УДАЛИТЕ ЭТУ ДУБЛИРУЮЩУЮСЯ ФУНКЦИЮ - она вызывает ошибки!
// @Composable
// fun AddRemarkDialog(
//     controlPointName: String,
//     onDismiss: () -> Unit,
//     onConfirm: (ERROR, ERROR, ERROR, ERROR, ERROR, ERROR) -> Unit
// ) {
//     TODO("Not yet implemented")
// }