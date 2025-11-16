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
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.ControlPointEntity
import com.example.kipia.ui.ControlPointViewModel
import com.example.kipia.ui.PKUViewModel
import com.example.kipia.ui.TubeViewModel
import com.example.kipia.ui.NodeViewModel
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
                    val controlPointViewModel = remember {
                        ControlPointViewModel(AppDatabase.getDatabase(context))
                    }
                    val controlPoints by controlPointViewModel.controlPoints.collectAsState()

                    // Поднимаем состояние выбора КП в MainActivity
                    var selectedControlPoint by remember { mutableStateOf<ControlPointEntity?>(null) }

                    if (selectedControlPoint == null) {
                        // Показываем список КП
                        ControlPointListScreen(
                            controlPoints = controlPoints,
                            onControlPointClick = { cp -> selectedControlPoint = cp },
                            onAddControlPoint = { name, desc -> controlPointViewModel.addControlPoint(name, desc) },
                            onDeleteControlPoint = { id -> controlPointViewModel.deleteControlPoint(id) }
                        )
                    } else {
                        // Показываем ПКУ и Трубы для выбранного КП
                        // Создаём ViewModel для ПКУ и Труб, передаём текущее выбранное КП
                        val pkuViewModel = remember { PKUViewModel(AppDatabase.getDatabase(context)) }
                        val tubeViewModel = remember { TubeViewModel(AppDatabase.getDatabase(context)) }
                        val nodeViewModel = remember { NodeViewModel(AppDatabase.getDatabase(context)) } // Добавляем NodeViewModel

                        // Загружаем данные при изменении selectedControlPoint
                        LaunchedEffect(selectedControlPoint) {
                            selectedControlPoint?.let { cp ->
                                pkuViewModel.loadPKUsByControlPointId(cp.id)
                                // Для труб и узлов будем загружать по мере необходимости
                            }
                        }

                        // Передаём selectedControlPoint.value (а не сам selectedControlPoint) в ControlPointDetailScreen
                        // Это immutable значение на момент вызова
                        ControlPointDetailScreen(
                            controlPoint = selectedControlPoint!!,
                            pkuViewModel = pkuViewModel,
                            tubeViewModel = tubeViewModel,
                            nodeViewModel = nodeViewModel, // Передаём NodeViewModel
                            onBackClick = { selectedControlPoint = null }, // Сбрасываем выбор
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
                            onAddNode = { name, tubeId ->
                                // Вызываем callback для добавления Node
                                nodeViewModel.addNode(name, tubeId)
                            },
                            onDeleteNode = { nodeId, tubeId ->
                                // Вызываем callback для удаления Node
                                nodeViewModel.deleteNode(nodeId, tubeId)
                            },
                            onAddEquipment = { name, nodeId, sectionId ->
                                // Пока не реализовано
                            },
                            onDeleteEquipment = { id ->
                                // Пока не реализовано
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
    onDeleteControlPoint: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Список КП", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(controlPoints) { cp ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onControlPointClick(cp) }, // Позволяет выбрать КП
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cp.name, style = MaterialTheme.typography.body1)
                            if (cp.description.isNotEmpty()) {
                                Text(
                                    text = cp.description,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(onClick = { onDeleteControlPoint(cp.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить КП")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onAddControlPoint("Новое КП", "Описание") }) {
            Text("Добавить КП")
        }
    }
}

@Composable
fun ControlPointDetailScreen(
    controlPoint: ControlPointEntity,
    pkuViewModel: PKUViewModel,
    tubeViewModel: TubeViewModel,
    nodeViewModel: NodeViewModel,
    onBackClick: () -> Unit,
    onAddPKU: (String, String) -> Unit,
    onDeletePKU: (Long) -> Unit,
    onAddTube: (String) -> Unit,
    onDeleteTube: (Long) -> Unit,
    onAddNode: (String, Long) -> Unit,
    onDeleteNode: (Long, Long) -> Unit,
    onAddEquipment: (String, Long?, Long?) -> Unit,
    onDeleteEquipment: (Long) -> Unit
) {
    // Получаем состояния из ViewModel
    val pkus by pkuViewModel.pkus.collectAsState()
    val tubes by tubeViewModel.tubes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Кнопка "Назад"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(onClick = onBackClick) {
                Text("Назад к КП")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "КП: ${controlPoint.name}", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Секция ПКУ ---
        Text(text = "ПКУ", style = MaterialTheme.typography.h6)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(pkus) { pku ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pku.name, style = MaterialTheme.typography.body2)
                            if (pku.description.isNotEmpty()) {
                                Text(
                                    text = pku.description,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                onDeletePKU(pku.id)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить ПКУ")
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                onAddPKU("Новая ПКУ", "Описание")
            }
        ) {
            Text("Добавить ПКУ")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- Секция Трубы ---
        Text(text = "Трубы", style = MaterialTheme.typography.h6)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(tubes) { tube ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    elevation = 2.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = tube.name, style = MaterialTheme.typography.body2)
                            }
                            IconButton(
                                onClick = {
                                    onDeleteTube(tube.id)
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить Трубу")
                            }
                        }

                        // --- Секция Колодцев ---
                        Text(text = "Колодцы", style = MaterialTheme.typography.h6)

                        // Загружаем узлы при изменении tube.id
                        LaunchedEffect(tube.id) {
                            nodeViewModel.loadNodesByTubeId(tube.id)
                        }

                        // Получаем все узлы из ViewModel
                        val nodesForThisTube by nodeViewModel.nodes.collectAsState()

                        // Отображаем список узлов для этой трубы
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight() // Убедимся, что LazyColumn имеет ограниченную высоту
                        ) {
                            // Фильтруем узлы по текущему tube.id
                            items(nodesForThisTube.filter { it.tubeId == tube.id }, key = { it.id }) { node ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    elevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = node.name, style = MaterialTheme.typography.body2)
                                        }
                                        IconButton(
                                            onClick = {
                                                onDeleteNode(node.id, tube.id)
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Удалить Колодец")
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onAddNode("Новый Колодец", tube.id)
                            }
                        ) {
                            Text("Добавить Колодец")
                        }
                    }
                }
            }
        }
        Button(
            onClick = {
                onAddTube("Новая Труба")
            }
        ) {
            Text("Добавить Трубу")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KIPITheme {
        val controlPoints = listOf(
            ControlPointEntity(id = 1, name = "КП 937 км", description = "Описание КП"),
            ControlPointEntity(id = 2, name = "КП 867 км", description = "Описание КП 2")
        )
        ControlPointListScreen(
            controlPoints = controlPoints,
            onControlPointClick = {},
            onAddControlPoint = { _, _ -> },
            onDeleteControlPoint = {}
        )
    }
}