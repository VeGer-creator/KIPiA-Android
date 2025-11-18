package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kipia.database.ControlPointEntity

@Composable
fun ControlPointListScreen(
    controlPoints: List<ControlPointEntity>,
    onControlPointClick: (ControlPointEntity) -> Unit,
    onAddControlPoint: (String, String) -> Unit,
    onDeleteControlPoint: (Long) -> Unit,
    onEditControlPoint: (Long, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingControlPoint by remember { mutableStateOf<ControlPointEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок и кнопка добавления в одной строке
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Список КП", style = MaterialTheme.typography.h5)
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить КП")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(controlPoints) { cp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Название КП - клик для перехода к деталям
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onControlPointClick(cp) }
                        ) {
                            Text(text = cp.name, style = MaterialTheme.typography.body1)
                            if (cp.description.isNotEmpty()) {
                                Text(
                                    text = cp.description,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Кнопка редактирования
                        IconButton(
                            onClick = {
                                editingControlPoint = cp
                                showEditDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать КП")
                        }

                        // Кнопка удаления
                        IconButton(onClick = { onDeleteControlPoint(cp.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить КП")
                        }
                    }
                }
            }
        }
    }

    // Диалог добавления КП
    if (showAddDialog) {
        EditNameDialog(
            currentName = "",
            currentDescription = "",
            title = "Добавить КП",
            nameHint = "Название КП",
            descriptionHint = "Описание КП",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, description ->
                onAddControlPoint(name, description)
                showAddDialog = false
            }
        )
    }

    // Диалог редактирования КП
    if (showEditDialog && editingControlPoint != null) {
        EditNameDialog(
            currentName = editingControlPoint!!.name,
            currentDescription = editingControlPoint!!.description,
            title = "Редактировать КП",
            nameHint = "Название КП",
            descriptionHint = "Описание КП",
            onDismiss = {
                showEditDialog = false
                editingControlPoint = null
            },
            onConfirm = { newName, newDescription ->
                editingControlPoint?.let { cp ->
                    onEditControlPoint(cp.id, newName, newDescription)
                }
                showEditDialog = false
                editingControlPoint = null
            }
        )
    }
}