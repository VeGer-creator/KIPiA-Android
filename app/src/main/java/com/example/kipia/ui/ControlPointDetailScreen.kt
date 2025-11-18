package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.kipia.database.SectionEntity
import com.example.kipia.database.NodeEntity
import kotlinx.coroutines.launch

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
        factory = ControlPointViewModelFactory(AppDatabase.getInstance(context))
    )

    val sectionViewModel: SectionViewModel = viewModel(
        factory = SectionViewModelFactory(AppDatabase.getInstance(context))
    )

    val pkus by pkuViewModel.pkus.collectAsState()
    val tubes by tubeViewModel.tubes.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showAddPKUDialog by remember { mutableStateOf(false) }
    var showAddTubeDialog by remember { mutableStateOf(false) }
    var showEditControlPointDialog by remember { mutableStateOf(false) }

    // Состояния для редактирования
    var editingPKU by remember { mutableStateOf<PKUEntity?>(null) }
    var editingTube by remember { mutableStateOf<TubeEntity?>(null) }
    var editingNode by remember { mutableStateOf<NodeEntity?>(null) }
    var selectedEquipmentForEditing by remember { mutableStateOf<Long?>(null) }

    // Состояния для навигации
    var selectedNodeForEquipment by remember { mutableStateOf<NodeEntity?>(null) }
    var selectedSectionForEquipment by remember { mutableStateOf<SectionEntity?>(null) }
    var selectedEquipmentForView by remember { mutableStateOf<Long?>(null) }
    var selectedEquipmentForEdit by remember { mutableStateOf<Long?>(null) }

    // Загружаем данные при изменении controlPoint
    LaunchedEffect(controlPoint.id) {
        pkuViewModel.loadPKUsByControlPointId(controlPoint.id)
        tubeViewModel.loadTubesByControlPointId(controlPoint.id)
    }

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
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Секция ПКУ с кнопкой добавления в одной строке
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ПКУ", style = MaterialTheme.typography.h6)
                Row {
                    // Кнопка добавления ПКУ
                    IconButton(
                        onClick = { showAddPKUDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить ПКУ")
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Список ПКУ с фиксированной высотой
            if (pkus.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    items(pkus) { pku ->
                        PKUItem(
                            pku = pku,
                            sectionViewModel = sectionViewModel,
                            onEdit = { editingPKU = pku },
                            onDelete = { onDeletePKU(pku.id) },
                            onAddSection = { sectionName, pkuId ->
                                sectionViewModel.addSection(sectionName, pkuId)
                            },
                            onViewSectionEquipment = { sectionId ->
                                // Находим реальную секцию по ID
                                val sections = sectionViewModel.sections.value
                                val section = sections.find { it.id == sectionId }
                                selectedSectionForEquipment = section ?: SectionEntity(
                                    id = sectionId,
                                    name = "Отсек $sectionId",
                                    pkuId = pku.id
                                )
                            }                        )
                    }
                }
            } else {
                Text(
                    text = "Нет ПКУ",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Секция Участков МН с кнопкой добавления в одной строке
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Участки МН", style = MaterialTheme.typography.h6)
                IconButton(
                    onClick = { showAddTubeDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить участок МН")
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Список участков МН занимает оставшееся пространство
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                tubes.forEach { tube ->
                    TubeItem(
                        tube = tube,
                        nodeViewModel = nodeViewModel,
                        onEdit = { editingTube = tube },
                        onDelete = {
                            onDeleteTube(tube.id)
                            coroutineScope.launch {
                                tubeViewModel.loadTubesByControlPointId(controlPoint.id)
                            }
                        },
                        onAddNode = { name, type ->
                            onAddNode(name, tube.id, type)
                            coroutineScope.launch {
                                nodeViewModel.loadNodesByTubeId(tube.id)
                            }
                        },
                        onDeleteNode = { nodeId ->
                            onDeleteNode(nodeId, tube.id)
                            coroutineScope.launch {
                                nodeViewModel.loadNodesByTubeId(tube.id)
                            }
                        },
                        onEditNode = { node -> editingNode = node },
                        onViewEquipment = { node ->
                            selectedNodeForEquipment = node
                        }
                    )                }
            }
        }
    }

    // Диалоги
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

    // 1. ЕСЛИ ВЫБРАН УЗЕЛ ДЛЯ ПРОСМОТРА ОБОРУДОВАНИЯ - ПОКАЗЫВАЕМ ЭКРАН ОБОРУДОВАНИЯ
    if (selectedNodeForEquipment != null) {
        EquipmentDetailScreen(
            nodeName = selectedNodeForEquipment!!.name,
            nodeId = selectedNodeForEquipment!!.id,
            onBackClick = { selectedNodeForEquipment = null },
            onViewEquipment = { equipmentId ->
                selectedEquipmentForView = equipmentId
                selectedNodeForEquipment = null
            }
        )
        return
    }

    // 2. ЕСЛИ ВЫБРАНО ОБОРУДОВАНИЕ ДЛЯ ПРОСМОТРА - ПОКАЗЫВАЕМ ПРОСМОТР
    if (selectedEquipmentForView != null) {
        ViewEquipmentScreen(
            equipmentId = selectedEquipmentForView!!,
            onBackClick = {
                selectedEquipmentForView = null
                selectedNodeForEquipment = selectedNodeForEquipment
            },
            onEditClick = {
                selectedEquipmentForEdit = selectedEquipmentForView
                selectedEquipmentForView = null
            }
        )
        return
    }

    // 3. ЕСЛИ ВЫБРАНО ОБОРУДОВАНИЕ ДЛЯ РЕДАКТИРОВАНИЯ - ПОКАЗЫВАЕМ РЕДАКТОР
    if (selectedEquipmentForEdit != null) {
        EditEquipmentScreen(
            equipmentId = selectedEquipmentForEdit!!,
            onBackClick = {
                selectedEquipmentForView = selectedEquipmentForEdit
                selectedEquipmentForEdit = null
            },
            onSaveClick = {
                selectedEquipmentForView = selectedEquipmentForEdit
                selectedEquipmentForEdit = null
            }
        )
        return
    }

    // 4. ЕСЛИ ВЫБРАН ОТСЕК ДЛЯ ПРОСМОТРА ОБОРУДОВАНИЯ - ПОКАЗЫВАЕМ ЭКРАН ОБОРУДОВАНИЯ
    if (selectedSectionForEquipment != null) {
        EquipmentDetailScreen(
            sectionName = selectedSectionForEquipment!!.name,
            sectionId = selectedSectionForEquipment!!.id,
            onBackClick = { selectedSectionForEquipment = null },
            onViewEquipment = { equipmentId ->
                selectedEquipmentForView = equipmentId
                selectedSectionForEquipment = null
            }
        )
        return
    }

    if (selectedEquipmentForEditing != null) {
        EditEquipmentScreen(
            equipmentId = selectedEquipmentForEditing!!,
            onBackClick = { selectedEquipmentForEditing = null },
            onSaveClick = { selectedEquipmentForEditing = null }
        )
        return
    }
}