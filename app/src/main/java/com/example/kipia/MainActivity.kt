package com.example.kipia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.ControlPointEntity
import com.example.kipia.ui.*
import com.example.kipia.ui.theme.KIPITheme
import androidx.compose.material.Surface
import androidx.compose.material.MaterialTheme

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
                        factory = ControlPointViewModelFactory(AppDatabase.getInstance(context))
                    )
                    val controlPoints by controlPointViewModel.controlPoints.collectAsState()

                    // Поднимаем состояние выбора КП в MainActivity
                    var selectedControlPoint by remember { mutableStateOf<ControlPointEntity?>(null) }

                    // Правильная обработка кнопки "Назад"
                    BackHandler(enabled = selectedControlPoint != null) {
                        selectedControlPoint = null
                    }

                    if (selectedControlPoint == null) {
                        // Показываем список КП
                        ControlPointListScreen(
                            controlPoints = controlPoints,
                            onControlPointClick = { cp: ControlPointEntity -> selectedControlPoint = cp },
                            onAddControlPoint = { name: String, desc: String ->
                                controlPointViewModel.addControlPoint(name, desc)
                            },
                            onDeleteControlPoint = { id: Long ->
                                controlPointViewModel.deleteControlPoint(id)
                            },
                            onEditControlPoint = { id: Long, newName: String, description: String ->
                                controlPointViewModel.updateControlPoint(id, newName, description)
                            }
                        )
                    } else {
                        // Показываем ПКУ и Участки МН для выбранного КП
                        val pkuViewModel: PKUViewModel = viewModel(
                            factory = PKUViewModelFactory(AppDatabase.getInstance(context))
                        )
                        val tubeViewModel: TubeViewModel = viewModel(
                            factory = TubeViewModelFactory(AppDatabase.getInstance(context))
                        )
                        val nodeViewModel: NodeViewModel = viewModel(
                            factory = NodeViewModelFactory(AppDatabase.getInstance(context))
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
                            onAddPKU = { name: String, desc: String ->
                                selectedControlPoint?.let { cp ->
                                    pkuViewModel.addPKU(name, desc, cp.id)
                                }
                            },
                            onDeletePKU = { id: Long ->
                                selectedControlPoint?.let { cp ->
                                    pkuViewModel.deletePKU(id, cp.id)
                                }
                            },
                            onAddTube = { name: String ->
                                selectedControlPoint?.let { cp ->
                                    tubeViewModel.addTube(name, cp.id)
                                }
                            },
                            onDeleteTube = { id: Long ->
                                selectedControlPoint?.let { cp ->
                                    tubeViewModel.deleteTube(id, cp.id)
                                }
                            },
                            onAddNode = { name: String, tubeId: Long, type: com.example.kipia.model.NodeType ->
                                nodeViewModel.addNode(name, tubeId, type)
                            },
                            onDeleteNode = { nodeId: Long, tubeId: Long ->
                                nodeViewModel.deleteNode(nodeId, tubeId)
                            }
                        )
                    }
                }
            }
        }
    }
}