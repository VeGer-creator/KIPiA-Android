package com.example.kipia.ui

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
import com.example.kipia.database.NodeEntity
import kotlinx.coroutines.launch

@Composable
fun TubeItem(
    tube: com.example.kipia.database.TubeEntity,
    nodeViewModel: NodeViewModel,
    onEdit: (com.example.kipia.database.TubeEntity) -> Unit,
    onDelete: () -> Unit,
    onAddNode: (String, com.example.kipia.model.NodeType) -> Unit,
    onDeleteNode: (Long) -> Unit,
    onEditNode: (com.example.kipia.database.NodeEntity) -> Unit,
    onViewEquipment: (com.example.kipia.database.NodeEntity) -> Unit = {}
) {
    val nodesForThisTube by nodeViewModel.getNodesFlowForTube(tube.id).collectAsState()
    var showAddNodeDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tube.id) {
        nodeViewModel.loadNodesByTubeId(tube.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tube.name,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colors.onSurface
                )

                IconButton(onClick = { onEdit(tube) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                IconButton(onClick = { showAddNodeDialog = true }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Добавить объект",
                        tint = MaterialTheme.colors.onSurface
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colors.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Список объектов (ОД, В, Задвижки) - теперь без дублирования
            if (nodesForThisTube.isNotEmpty()) {
                val sortedNodes = nodesForThisTube.sortedBy { it.orderIndex }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    items(sortedNodes) { node ->
                        // Используем NodeItemWithEquipment для всех объектов
                        NodeItemWithEquipment(
                            node = node,
                            onEdit = { onEditNode(node) },
                            onDelete = { onDeleteNode(node.id) },
                            onViewEquipment = { onViewEquipment(node) }
                        )
                    }
                }
            } else {
                Text(
                    text = "📝 Нет объектов. Нажмите '+' чтобы добавить",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }

    if (showAddNodeDialog) {
        AddNodeDialog(
            tubeName = tube.name,
            onDismiss = { showAddNodeDialog = false },
            onConfirm = { name, type ->
                onAddNode(name, type)
                showAddNodeDialog = false
            }
        )
    }


}