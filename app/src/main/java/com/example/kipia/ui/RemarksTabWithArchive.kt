package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kipia.database.RemarkEntity

@Composable
fun RemarksTabWithArchive(
    activeRemarks: List<RemarkEntity>,
    archivedRemarks: List<RemarkEntity>,
    onAddRemarkWithPhotos: (String, String, String, String, String, List<String>) -> Unit,
    onEditRemark: (RemarkEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onArchiveRemark: (Long) -> Unit,
    onUnarchiveRemark: (Long) -> Unit,
    onDeleteRemark: (android.content.Context, RemarkEntity) -> Unit,
    controlPointName: String = ""
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val tabs = listOf("Активные (${activeRemarks.size})", "Архив (${archivedRemarks.size})")

    Column(modifier = Modifier.fillMaxSize()) {
        // Вкладки
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.caption
                        )
                    },
                    selected = selectedTab == index,
                    onClick = { selectedTab = index }
                )
            }
        }

        // Содержимое вкладок
        when (selectedTab) {
            0 -> {
                // Активные замечания
                RemarksTabContent(
                    remarks = activeRemarks,
                    onAddRemark = { showAddDialog = true },
                    onEditRemark = onEditRemark,
                    onUpdateStatus = onUpdateStatus,
                    onArchiveRemark = onArchiveRemark,
                    onDeleteRemark = { remark -> onDeleteRemark(context, remark) },
                    emptyMessage = "Нет активных замечаний",
                    showArchiveButton = true
                )
            }
            1 -> {
                // Архивные замечания
                RemarksTabContent(
                    remarks = archivedRemarks,
                    onAddRemark = { showAddDialog = true },
                    onEditRemark = onEditRemark,
                    onUpdateStatus = onUpdateStatus,
                    onArchiveRemark = { remarkId -> onUnarchiveRemark(remarkId) },
                    onDeleteRemark = { remark -> onDeleteRemark(context, remark) },
                    emptyMessage = "Архив пуст",
                    showArchiveButton = false,
                    archiveButtonText = "Вернуть из архива"
                )
            }
        }
    }

    // Диалог добавления замечания
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
}

@Composable
fun RemarksTabContent(
    remarks: List<RemarkEntity>,
    onAddRemark: () -> Unit,
    onEditRemark: (RemarkEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onArchiveRemark: (Long) -> Unit,
    onDeleteRemark: (RemarkEntity) -> Unit,
    emptyMessage: String,
    showArchiveButton: Boolean,
    archiveButtonText: String = "В архив"
) {
    var selectedCategory by remember { mutableStateOf("Все") }
    var editingRemark by remember { mutableStateOf<RemarkEntity?>(null) }
    var viewingRemark by remember { mutableStateOf<RemarkEntity?>(null) }

    val filteredRemarks = if (selectedCategory == "Все") {
        remarks
    } else {
        remarks.filter { it.category == selectedCategory }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Статистика
            if (remarks.isNotEmpty()) {
                RemarkStats(remarks)
            }

            // Фильтры
            RemarkFilters(
                selectedCategory = selectedCategory,
                onCategoryChange = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Список замечаний
            if (filteredRemarks.isNotEmpty()) {
                RemarkList(
                    remarks = filteredRemarks,
                    onEditRemark = { editingRemark = it },
                    onViewRemark = { viewingRemark = it },
                    onUpdateStatus = onUpdateStatus,
                    onArchiveRemark = onArchiveRemark,
                    onDeleteRemark = onDeleteRemark,
                    showArchiveButton = showArchiveButton,
                    archiveButtonText = archiveButtonText
                )
            } else {
                EmptyRemarksMessage(
                    message = emptyMessage,
                    onAddRemark = onAddRemark,
                    showAddButton = emptyMessage == "Нет активных замечаний"
                )
            }
        }

        // Плавающая кнопка только для активных замечаний
        if (emptyMessage == "Нет активных замечаний") {
            FloatingActionButton(
                onClick = onAddRemark,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить замечание")
            }
        }
    }

    // Диалоги
    editingRemark?.let { remark ->
        EditRemarkDialog(
            remark = remark,
            onDismiss = { editingRemark = null },
            onConfirm = { title, description, category, priority, deadline, photoPaths ->
                val updatedRemark = remark.copy(
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

    viewingRemark?.let { remark ->
        ViewRemarkDialog(
            remark = remark,
            onDismiss = { viewingRemark = null },
            onEdit = {
                editingRemark = remark
                viewingRemark = null
            }
        )
    }
}

@Composable
fun RemarkList(
    remarks: List<RemarkEntity>,
    onEditRemark: (RemarkEntity) -> Unit,
    onViewRemark: (RemarkEntity) -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onArchiveRemark: (Long) -> Unit,
    onDeleteRemark: (RemarkEntity) -> Unit,
    showArchiveButton: Boolean,
    archiveButtonText: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        remarks.forEach { remark ->
            RemarkItemWithActions(
                remark = remark,
                onView = { onViewRemark(remark) },
                onEdit = { onEditRemark(remark) },
                onStatusChange = { newStatus -> onUpdateStatus(remark.id, newStatus) },
                onArchive = { onArchiveRemark(remark.id) },
                onDelete = { onDeleteRemark(remark) },
                showArchiveButton = showArchiveButton,
                archiveButtonText = archiveButtonText
            )
        }
    }
}

@Composable
fun RemarkItemWithActions(
    remark: RemarkEntity,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onStatusChange: (String) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    showArchiveButton: Boolean,
    archiveButtonText: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(onClick = onView),
        elevation = 2.dp,
        backgroundColor = getRemarkCardColor(remark.priority, remark.status)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Основная информация замечания (используем существующий RemarkItemCard)
            RemarkItemCardContent(remark = remark, onStatusChange = onStatusChange, onEdit = onEdit)

            // Кнопки действий
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (showArchiveButton) {
                    TextButton(
                        onClick = onArchive,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(archiveButtonText, style = MaterialTheme.typography.caption)
                    }
                } else {
                    TextButton(
                        onClick = onArchive,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Icon(Icons.Default.Unarchive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(archiveButtonText, style = MaterialTheme.typography.caption)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Удалить", style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}

@Composable
fun EmptyRemarksMessage(
    message: String,
    onAddRemark: () -> Unit,
    showAddButton: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            if (showAddButton) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddRemark,
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

// Вспомогательная функция для отображения контента карточки
@Composable
fun RemarkItemCardContent(
    remark: RemarkEntity,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit
) {
    Column {
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
                onStatusChange = onStatusChange
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Кнопка редактирования
            IconButton(
                onClick = onEdit,
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