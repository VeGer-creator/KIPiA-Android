package com.example.kipia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.ControlPointEntity
import com.example.kipia.ui.*
import com.example.kipia.ui.theme.KIPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KIPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val context = LocalContext.current
                    val controlPointViewModel: ControlPointViewModel = viewModel(
                        factory = ControlPointViewModelFactory(AppDatabase.getDatabase(context))
                    )
                    val controlPoints by controlPointViewModel.controlPoints.collectAsState()

                    // Поднимаем состояние выбора КП в MainActivity
                    var selectedControlPoint by remember { mutableStateOf<ControlPointEntity?>(null) }

                    if (selectedControlPoint == null) {
                        // Показываем список КП
                        ControlPointListScreen(
                            controlPoints = controlPoints,
                            onControlPointClick = { cp -> selectedControlPoint = cp },
                            onAddControlPoint = { name, desc -> controlPointViewModel.addControlPoint(name, desc) },
                            onDeleteControlPoint = { id -> controlPointViewModel.deleteControlPoint(id) },
                            onEditControlPoint = { id, newName, description ->
                                controlPointViewModel.updateControlPoint(id, newName, description)
                            }
                        )
                    } else {
                        // Показываем ПКУ и Участки МН для выбранного КП
                        val pkuViewModel: PKUViewModel = viewModel(
                            factory = PKUViewModelFactory(AppDatabase.getDatabase(context))
                        )
                        val tubeViewModel: TubeViewModel = viewModel(
                            factory = TubeViewModelFactory(AppDatabase.getDatabase(context))
                        )
                        val nodeViewModel: NodeViewModel = viewModel(
                            factory = NodeViewModelFactory(AppDatabase.getDatabase(context))
                        )

                        // Загружаем данные при изменении selectedControlPoint
                        LaunchedEffect(selectedControlPoint) {
                            selectedControlPoint?.let { cp ->
                                pkuViewModel.loadPKUsByControlPointId(cp.id)
                                tubeViewModel.loadTubesByControlPointId(cp.id)
                            }
                        }

                        ControlPointDetailScreen(
                            controlPoint = selectedControlPoint!!,
                            pkuViewModel = pkuViewModel,
                            tubeViewModel = tubeViewModel,
                            nodeViewModel = nodeViewModel,
                            onBackClick = { selectedControlPoint = null },
                            onAddPKU = { name, desc ->
                                selectedControlPoint?.let { cp ->
                                    pkuViewModel.addPKU(name, desc, cp.id)
                                }
                            },
                            onDeletePKU = { id ->
                                selectedControlPoint?.let { cp ->
                                    pkuViewModel.deletePKU(id, cp.id)
                                }
                            },
                            onAddTube = { name ->
                                selectedControlPoint?.let { cp ->
                                    tubeViewModel.addTube(name, cp.id)
                                }
                            },
                            onDeleteTube = { id ->
                                selectedControlPoint?.let { cp ->
                                    tubeViewModel.deleteTube(id, cp.id)
                                }
                            },
                            onAddNode = { name, tubeId, type ->
                                nodeViewModel.addNode(name, tubeId, type)
                            },
                            onDeleteNode = { nodeId, tubeId ->
                                nodeViewModel.deleteNode(nodeId, tubeId)
                            }
                        )
                    }
                }
            }
        }
    }
}

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
        Text(text = "Список КП", style = MaterialTheme.typography.h5)
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

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить КП")
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