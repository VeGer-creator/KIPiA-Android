package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.example.kipia.database.PKUEntity
import com.example.kipia.database.TubeEntity

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
    onAddNode: (String, Long, com.example.kipia.model.NodeType) -> Unit,
    onDeleteNode: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    val controlPointViewModel: ControlPointViewModel = viewModel(
        factory = ControlPointViewModelFactory(AppDatabase.getDatabase(context))
    )

    val pkus by pkuViewModel.pkus.collectAsState()
    val tubes by tubeViewModel.tubes.collectAsState()

    var showAddPKUDialog by remember { mutableStateOf(false) }
    var showAddTubeDialog by remember { mutableStateOf(false) }
    var showEditControlPointDialog by remember { mutableStateOf(false) }

    // Состояния для редактирования
    var editingPKU by remember { mutableStateOf<PKUEntity?>(null) }
    var editingTube by remember { mutableStateOf<TubeEntity?>(null) }
    var editingNode by remember { mutableStateOf<com.example.kipia.database.NodeEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(controlPoint.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditControlPointDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать КП")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (controlPoint.description.isNotEmpty()) {
                Text(
                    text = controlPoint.description,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Секция ПКУ
            Text("ПКУ (Пункты контроля и управления)", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pkus) { pku ->
                    PKUItem(
                        pku = pku,
                        onEdit = { editingPKU = pku },
                        onDelete = { onDeletePKU(pku.id) }
                    )
                }
            }

            Button(
                onClick = { showAddPKUDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить ПКУ")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Секция Участков МН
            Text("Участки МН", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tubes) { tube ->
                    TubeItem(
                        tube = tube,
                        nodeViewModel = nodeViewModel,
                        onEdit = { editingTube = tube },
                        onDelete = { onDeleteTube(tube.id) },
                        onAddNode = { name, type -> onAddNode(name, tube.id, type) },
                        onDeleteNode = { nodeId -> onDeleteNode(nodeId, tube.id) },
                        onEditNode = { node -> editingNode = node }
                    )
                }
            }

            Button(
                onClick = { showAddTubeDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить участок МН")
            }
        }
    }

    // Диалог добавления ПКУ
    if (showAddPKUDialog) {
        AddPKUDialog(
            kpName = controlPoint.name,
            onDismiss = { showAddPKUDialog = false },
            onConfirm = { name, description ->
                onAddPKU(name, description)
                showAddPKUDialog = false
            }
        )
    }

    // Диалог добавления участка МН
    if (showAddTubeDialog) {
        AddTubeDialog(
            kpName = controlPoint.name,
            onDismiss = { showAddTubeDialog = false },
            onConfirm = { name ->
                onAddTube(name)
                showAddTubeDialog = false
            }
        )
    }

    // Диалог редактирования КП
    if (showEditControlPointDialog) {
        EditNameDialog(
            currentName = controlPoint.name,
            currentDescription = controlPoint.description,
            title = "Редактировать КП",
            nameHint = "Название КП",
            descriptionHint = "Описание КП",
            onDismiss = { showEditControlPointDialog = false },
            onConfirm = { newName, newDescription ->
                controlPointViewModel.updateControlPoint(controlPoint.id, newName, newDescription)
                showEditControlPointDialog = false
            }
        )
    }

    // Диалог редактирования ПКУ
    if (editingPKU != null) {
        EditNameDialog(
            currentName = editingPKU!!.name,
            currentDescription = editingPKU!!.description,
            title = "Редактировать ПКУ",
            nameHint = "Название ПКУ",
            descriptionHint = "Описание ПКУ",
            onDismiss = { editingPKU = null },
            onConfirm = { newName, newDescription ->
                pkuViewModel.updatePKU(editingPKU!!.id, newName, newDescription)
                editingPKU = null
            }
        )
    }

    // Диалог редактирования участка МН
    if (editingTube != null) {
        EditNameDialog(
            currentName = editingTube!!.name,
            currentDescription = "",
            title = "Редактировать участок МН",
            nameHint = "Название участка МН",
            showDescription = false,
            onDismiss = { editingTube = null },
            onConfirm = { newName, _ ->
                tubeViewModel.updateTube(editingTube!!.id, newName)
                editingTube = null
            }
        )
    }

    // Диалог редактирования объекта
    if (editingNode != null) {
        EditNameDialog(
            currentName = editingNode!!.name,
            currentDescription = "",
            title = "Редактировать объект",
            nameHint = "Название объекта",
            showDescription = false,
            onDismiss = { editingNode = null },
            onConfirm = { newName, _ ->
                nodeViewModel.updateNode(editingNode!!.id, newName, editingNode!!.tubeId)
                editingNode = null
            }
        )
    }
}

@Composable
fun PKUItem(
    pku: PKUEntity,
    onEdit: (PKUEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = pku.name, style = MaterialTheme.typography.body1)
                if (pku.description.isNotEmpty()) {
                    Text(
                        text = pku.description,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            // Кнопка редактирования
            IconButton(onClick = { onEdit(pku) }) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать ПКУ")
            }
            // Кнопка удаления
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить ПКУ")
            }
        }
    }
}

@Composable
fun TubeItem(
    tube: TubeEntity,
    nodeViewModel: NodeViewModel,
    onEdit: (TubeEntity) -> Unit,
    onDelete: () -> Unit,
    onAddNode: (String, com.example.kipia.model.NodeType) -> Unit,
    onDeleteNode: (Long) -> Unit,
    onEditNode: (com.example.kipia.database.NodeEntity) -> Unit
) {
    val nodes by nodeViewModel.nodes.collectAsState()
    var showAddNodeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(tube.id) {
        nodeViewModel.loadNodesByTubeId(tube.id)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tube.name,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f)
                )
                // Кнопка редактирования участка МН
                IconButton(onClick = { onEdit(tube) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать участок МН")
                }
                // Кнопка добавления объекта
                IconButton(onClick = { showAddNodeDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить объект")
                }
                // Кнопка удаления участка МН
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить участок МН")
                }
            }

            // Список объектов для этого участка МН
            nodes.forEach { node ->
                NodeItem(
                    node = node,
                    onEdit = { onEditNode(node) },
                    onDelete = { onDeleteNode(node.id) }
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

@Composable
fun NodeItem(
    node: com.example.kipia.database.NodeEntity,
    onEdit: (com.example.kipia.database.NodeEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f)
            )
            // Кнопка редактирования объекта
            IconButton(
                onClick = { onEdit(node) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать объект",
                    modifier = Modifier.size(16.dp)
                )
            }
            // Кнопка удаления объекта
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить объект",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}