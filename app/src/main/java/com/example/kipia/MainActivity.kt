// app/src/main/java/com/example/kipia/MainActivity.kt
package com.example.kipia

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Sync
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.ControlPointEntity
import com.example.kipia.ui.ControlPointDetailScreen
import com.example.kipia.ui.ControlPointViewModel
import com.example.kipia.ui.ControlPointViewModelFactory
import com.example.kipia.ui.EditEquipmentScreen
import com.example.kipia.ui.EquipmentDetailScreen
import com.example.kipia.ui.EquipmentPhotoScreen
import com.example.kipia.ui.EquipmentViewModel
import com.example.kipia.ui.EquipmentViewModelFactory
import com.example.kipia.ui.FullScreenPhotoView
import com.example.kipia.ui.NodeViewModel
import com.example.kipia.ui.NodeViewModelFactory
import com.example.kipia.ui.PKUViewModel
import com.example.kipia.ui.PKUViewModelFactory
import com.example.kipia.ui.SyncScreen
import com.example.kipia.ui.TubeViewModel
import com.example.kipia.ui.TubeViewModelFactory
import com.example.kipia.ui.ViewEquipmentScreen
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
                    AppNavigation()
                }
            }
        }
    }
}

// Расширенное состояние навигации
data class NavigationState(
    val selectedControlPoint: ControlPointEntity? = null,
    val selectedNodeForEquipment: com.example.kipia.database.NodeEntity? = null,
    val selectedSectionForEquipment: com.example.kipia.database.SectionEntity? = null,
    val selectedEquipmentForPhotos: Pair<Long, String>? = null,
    val selectedEquipmentForView: Long? = null,
    val selectedEquipmentForEdit: Long? = null,
    val fullScreenPhotoUri: Uri? = null,
    val showSyncScreen: Boolean = false // Добавляем флаг для экрана синхронизации
)

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val controlPointViewModel: ControlPointViewModel = viewModel(
        factory = ControlPointViewModelFactory(AppDatabase.getInstance(context))
    )
    val controlPoints by controlPointViewModel.controlPoints.collectAsState()

    var navState by remember { mutableStateOf(NavigationState()) }

    // Обновляем обработчик BackHandler
    BackHandler(
        enabled = navState.selectedControlPoint != null ||
                navState.selectedNodeForEquipment != null ||
                navState.selectedSectionForEquipment != null ||
                navState.selectedEquipmentForPhotos != null ||
                navState.selectedEquipmentForView != null ||
                navState.selectedEquipmentForEdit != null ||
                navState.fullScreenPhotoUri != null ||
                navState.showSyncScreen
    ) {
        navState = when {
            navState.fullScreenPhotoUri != null -> navState.copy(fullScreenPhotoUri = null)
            navState.selectedEquipmentForEdit != null -> navState.copy(selectedEquipmentForEdit = null)
            navState.selectedEquipmentForView != null -> navState.copy(selectedEquipmentForView = null)
            navState.selectedEquipmentForPhotos != null -> navState.copy(selectedEquipmentForPhotos = null)
            navState.selectedNodeForEquipment != null -> navState.copy(selectedNodeForEquipment = null)
            navState.selectedSectionForEquipment != null -> navState.copy(selectedSectionForEquipment = null)
            navState.showSyncScreen -> navState.copy(showSyncScreen = false)
            else -> navState.copy(selectedControlPoint = null)
        }
    }

    // Объединяем все when условия в один блок
    when {
        navState.showSyncScreen -> {
            SyncScreen(
                onBackClick = {
                    navState = navState.copy(showSyncScreen = false)
                }
            )
        }
        navState.fullScreenPhotoUri != null -> {
            FullScreenPhotoView(
                photoUri = navState.fullScreenPhotoUri!!,
                onDismiss = {
                    navState = navState.copy(fullScreenPhotoUri = null)
                }
            )
        }
        navState.selectedEquipmentForEdit != null -> {
            EditEquipmentScreen(
                equipmentId = navState.selectedEquipmentForEdit!!,
                onBackClick = {
                    navState = navState.copy(selectedEquipmentForEdit = null)
                },
                onSaveClick = {
                    navState = navState.copy(selectedEquipmentForEdit = null)
                }
            )
        }
        navState.selectedEquipmentForView != null -> {
            ViewEquipmentScreen(
                equipmentId = navState.selectedEquipmentForView!!,
                onBackClick = {
                    navState = navState.copy(selectedEquipmentForView = null)
                },
                onEditClick = {
                    navState = navState.copy(
                        selectedEquipmentForEdit = navState.selectedEquipmentForView,
                        selectedEquipmentForView = null
                    )
                }
            )
        }
        navState.selectedEquipmentForPhotos != null -> {
            val (equipmentId, equipmentName) = navState.selectedEquipmentForPhotos!!
            EquipmentPhotoScreen(
                equipmentId = equipmentId,
                equipmentName = equipmentName,
                onBackClick = {
                    navState = navState.copy(selectedEquipmentForPhotos = null)
                },
                onViewFullScreen = { uri ->
                    navState = navState.copy(fullScreenPhotoUri = uri)
                }
            )
        }
        navState.selectedNodeForEquipment != null || navState.selectedSectionForEquipment != null -> {
            // Экран оборудования узла или отсека
            EquipmentNavigation(
                navState = navState,
                onUpdateNavState = { newState -> navState = newState },
                controlPointViewModel = controlPointViewModel
            )
        }
        navState.selectedControlPoint != null -> {
            // Экран контрольной точки
            ControlPointNavigation(
                controlPoint = navState.selectedControlPoint!!,
                onBackClick = {
                    navState = navState.copy(selectedControlPoint = null)
                },
                onNodeSelected = { node ->
                    navState = navState.copy(selectedNodeForEquipment = node)
                },
                onSectionSelected = { section ->
                    navState = navState.copy(selectedSectionForEquipment = section)
                },
                onEquipmentViewSelected = { equipmentId ->
                    navState = navState.copy(selectedEquipmentForView = equipmentId)
                },
                onEquipmentEditSelected = { equipmentId ->
                    navState = navState.copy(selectedEquipmentForEdit = equipmentId)
                },
                onEquipmentPhotosSelected = { equipmentId, equipmentName ->
                    navState = navState.copy(selectedEquipmentForPhotos = Pair(equipmentId, equipmentName))
                }
            )
        }
        else -> {
            // Список контрольных точек
            ControlPointListScreen(
                controlPoints = controlPoints,
                onControlPointClick = { cp ->
                    navState = navState.copy(selectedControlPoint = cp)
                },
                onAddControlPoint = { name, desc ->
                    controlPointViewModel.addControlPoint(name, desc)
                },
                onDeleteControlPoint = { id ->
                    controlPointViewModel.deleteControlPoint(id)
                },
                onEditControlPoint = { id, newName, description ->
                    controlPointViewModel.updateControlPoint(id, newName, description)
                },
                onSyncClick = {
                    navState = navState.copy(showSyncScreen = true)
                }
            )
        }
    }
}

// Выносим ControlPointListScreen как отдельную функцию
// В MainActivity.kt замените текущий ControlPointListScreen на:

@Composable
fun ControlPointListScreen(
    controlPoints: List<ControlPointEntity>,
    onControlPointClick: (ControlPointEntity) -> Unit,
    onAddControlPoint: (String, String) -> Unit,
    onDeleteControlPoint: (Long) -> Unit,
    onEditControlPoint: (Long, String, String) -> Unit,
    onSyncClick: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newControlPointName by remember { mutableStateOf("") }
    var newControlPointDescription by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Контрольные точки") }
            )
        },
        floatingActionButton = {
            Column {
                // Кнопка синхронизации
                FloatingActionButton(
                    onClick = onSyncClick,
                    modifier = Modifier.padding(bottom = 8.dp),
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Синхронизация")
                }
                // Кнопка добавления
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить КП")
                }
            }
        }
    ) { padding ->
        if (controlPoints.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Нет КП",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Нет контрольных точек",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Нажмите + чтобы добавить первую КП",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(controlPoints) { controlPoint ->
                    ControlPointListItem(
                        controlPoint = controlPoint,
                        onItemClick = { onControlPointClick(controlPoint) },
                        onDeleteClick = { onDeleteControlPoint(controlPoint.id) },
                        onEditClick = { name, description ->
                            onEditControlPoint(controlPoint.id, name, description)
                        }
                    )
                    Divider()
                }
            }
        }
    }

    // Диалог добавления КП
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Добавить контрольную точку") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newControlPointName,
                        onValueChange = { newControlPointName = it },
                        label = { Text("Название КП") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newControlPointDescription,
                        onValueChange = { newControlPointDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newControlPointName.isNotBlank()) {
                            onAddControlPoint(newControlPointName, newControlPointDescription)
                            newControlPointName = ""
                            newControlPointDescription = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newControlPointName.isNotBlank()
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ControlPointListItem(
    controlPoint: ControlPointEntity,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: (String, String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(controlPoint.name) }
    var editDescription by remember { mutableStateOf(controlPoint.description) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 2.dp,
        onClick = onItemClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = controlPoint.name,
                    style = MaterialTheme.typography.h6
                )
                if (controlPoint.description.isNotBlank()) {
                    Text(
                        text = controlPoint.description,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colors.error)
                }
            }
        }
    }

    // Диалог редактирования
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редактировать КП") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Название КП") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            onEditClick(editName, editDescription)
                            showEditDialog = false
                        }
                    },
                    enabled = editName.isNotBlank()
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun EquipmentNavigation(
    navState: NavigationState,
    onUpdateNavState: (NavigationState) -> Unit,
    controlPointViewModel: ControlPointViewModel
) {
    val node = navState.selectedNodeForEquipment
    val section = navState.selectedSectionForEquipment

    if (node != null) {
        EquipmentDetailScreen(
            nodeName = node.name,
            nodeId = node.id,
            onBackClick = {
                onUpdateNavState(navState.copy(selectedNodeForEquipment = null))
            },
            onViewEquipment = { equipmentId ->
                // Находим имя оборудования для передачи
                val equipmentName = "Оборудование $equipmentId" // Можно улучшить, получая реальное имя
                onUpdateNavState(navState.copy(
                    selectedEquipmentForView = equipmentId
                ))
            },
            onViewEquipmentPhotos = { equipmentId, photoPaths ->
                // Передаем имя узла как контекст
                onUpdateNavState(navState.copy(
                    selectedEquipmentForPhotos = Pair(equipmentId, node.name)
                ))
            },
            onAddPhotosToEquipment = { equipmentId, equipmentName ->
                onUpdateNavState(navState.copy(
                    selectedEquipmentForPhotos = Pair(equipmentId, equipmentName)
                ))
            }
        )
    } else if (section != null) {
        EquipmentDetailScreen(
            sectionName = section.name,
            sectionId = section.id,
            onBackClick = {
                onUpdateNavState(navState.copy(selectedSectionForEquipment = null))
            },
            onViewEquipment = { equipmentId ->
                // Находим имя оборудования для передачи
                val equipmentName = "Оборудование $equipmentId" // Можно улучшить, получая реальное имя
                onUpdateNavState(navState.copy(
                    selectedEquipmentForView = equipmentId
                ))
            },
            onViewEquipmentPhotos = { equipmentId, photoPaths ->
                // Передаем имя секции как контекст
                onUpdateNavState(navState.copy(
                    selectedEquipmentForPhotos = Pair(equipmentId, section.name)
                ))
            },
            onAddPhotosToEquipment = { equipmentId, equipmentName ->
                onUpdateNavState(navState.copy(
                    selectedEquipmentForPhotos = Pair(equipmentId, equipmentName)
                ))
            }
        )
    }
}

@Composable
fun ControlPointNavigation(
    controlPoint: ControlPointEntity,
    onBackClick: () -> Unit,
    onNodeSelected: (com.example.kipia.database.NodeEntity) -> Unit,
    onSectionSelected: (com.example.kipia.database.SectionEntity) -> Unit,
    onEquipmentViewSelected: (Long) -> Unit,
    onEquipmentEditSelected: (Long) -> Unit,
    onEquipmentPhotosSelected: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val pkuViewModel: PKUViewModel = viewModel(
        factory = PKUViewModelFactory(AppDatabase.getInstance(context))
    )
    val tubeViewModel: TubeViewModel = viewModel(
        factory = TubeViewModelFactory(AppDatabase.getInstance(context))
    )
    val nodeViewModel: NodeViewModel = viewModel(
        factory = NodeViewModelFactory(AppDatabase.getInstance(context))
    )
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    LaunchedEffect(controlPoint) {
        pkuViewModel.loadPKUsByControlPointId(controlPoint.id)
        tubeViewModel.loadTubesByControlPointId(controlPoint.id)
    }

    ControlPointDetailScreen(
        controlPoint = controlPoint,
        pkuViewModel = pkuViewModel,
        tubeViewModel = tubeViewModel,
        nodeViewModel = nodeViewModel,
        equipmentViewModel = equipmentViewModel,
        onBackClick = onBackClick,
        onAddPKU = { name, desc ->
            pkuViewModel.addPKU(name, desc, controlPoint.id)
        },
        onDeletePKU = { id ->
            pkuViewModel.deletePKU(id, controlPoint.id)
        },
        onAddTube = { name ->
            tubeViewModel.addTube(name, controlPoint.id)
        },
        onDeleteTube = { id ->
            tubeViewModel.deleteTube(id, controlPoint.id)
        },
        onAddNode = { name, tubeId, type ->
            nodeViewModel.addNode(name, tubeId, type)
        },
        onDeleteNode = { nodeId, tubeId ->
            nodeViewModel.deleteNode(nodeId, tubeId)
        },
        onViewEquipment = { nodeId, nodeName ->
            // Находим узел по ID и передаем его
            val nodes = nodeViewModel.nodes.value
            val node = nodes.find { it.id == nodeId }
            node?.let { onNodeSelected(it) }
        },
        onViewSectionEquipment = { sectionId, sectionName ->
            // Создаем временную секцию или находим реальную
            val section = com.example.kipia.database.SectionEntity(
                id = sectionId,
                name = sectionName,
                pkuId = 0 // Временное значение
            )
            onSectionSelected(section)
        },
        onViewEquipmentPhotos = { equipmentId, photoPaths ->
            onEquipmentPhotosSelected(equipmentId, "Оборудование $equipmentId")
        }
    )
}