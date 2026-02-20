package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.kipia.ui.SyncState // ДОБАВЬТЕ ЭТУ СТРОКУ

@Composable
fun AdvancedSyncScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(context)
    )
    val syncState by syncViewModel.syncState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Расширенная синхронизация") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SyncStatusCard(syncState, syncViewModel)
            }

            item {
                QuickActionsCard(syncViewModel, coroutineScope, context)
            }

            item {
                DataManagementCard(syncViewModel, coroutineScope, context)
            }

            item {
                SyncHistoryCard(syncState)
            }

            item {
                TechnicalInfoCard(syncState)
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    viewModel: SyncViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope, // Исправлено
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Быстрые действия",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startSync() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Text("Синхронизировать")
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Экспорт данных
                            // EnhancedSyncManager.exportSyncDataToFile(context, "path/to/file")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Text("Экспорт")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Импорт данных
                            // EnhancedSyncManager.importSyncDataFromFile(context, "path/to/file")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Text("Импорт")
                    }
                }

                Button(
                    onClick = { viewModel.stopSync() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Text("Стоп")
                    }
                }
            }
        }
    }
}

@Composable
private fun DataManagementCard(
    viewModel: SyncViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope, // Исправлено
    context: android.content.Context
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Управление данными",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Статистика данных
            DataStatisticsRow()

            Spacer(modifier = Modifier.height(16.dp))

            // Действия с данными
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Создать резервную копию")
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Восстановить из копии")
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            // Очистка кэша синхронизации
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Очистить кэш синхронизации")
                }
            }
        }
    }

    // Диалоги
    if (showExportDialog) {
        ExportDataDialog(
            onDismiss = { showExportDialog = false },
            onExport = { filePath ->
                coroutineScope.launch {
                    // EnhancedSyncManager.exportSyncDataToFile(context, filePath)
                    showExportDialog = false
                }
            }
        )
    }

    if (showImportDialog) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onImport = { filePath ->
                coroutineScope.launch {
                    // EnhancedSyncManager.importSyncDataFromFile(context, filePath)
                    showImportDialog = false
                }
            }
        )
    }
}

@Composable
private fun DataStatisticsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DataStatItem(
            label = "КП",
            value = "12",
            icon = Icons.Default.Place
        )
        DataStatItem(
            label = "Оборудование",
            value = "45",
            icon = Icons.Default.Build
        )
        DataStatItem(
            label = "Замечания",
            value = "8",
            icon = Icons.Default.Warning
        )
    }
}

@Composable
private fun DataStatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colors.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.h6)
        Text(label, style = MaterialTheme.typography.caption)
    }
}

@Composable
private fun SyncHistoryCard(state: SyncState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "История синхронизации",
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Здесь можно отображать историю синхронизаций
            Text("Последняя синхронизация: ${if (state.lastSyncTime > 0) "Выполнена" else "Не выполнена"}")
            Text("Статус: ${if (state.isSyncing) "В процессе" else "Готово"}")
            Text("Устройств найдено: ${state.connectedDevices.size}")
        }
    }
}

@Composable
private fun TechnicalInfoCard(state: SyncState) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Техническая информация",
                    style = MaterialTheme.typography.h6
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Advertising: ${if (state.isAdvertising) "Включено" else "Выключено"}")
                Text("Discovering: ${if (state.isDiscovering) "Включено" else "Выключено"}")
                Text("Connected devices: ${state.connectedDevices.joinToString()}")
                Text("Sync progress: ${state.syncProgress}%")
            }
        }
    }
}

@Composable
private fun ExportDataDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Экспорт данных") },
        text = { Text("Выберите место для сохранения резервной копии данных") },
        confirmButton = {
            Button(onClick = { onExport("/path/to/backup.json") }) {
                Text("Экспорт")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun ImportDataDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Импорт данных") },
        text = { Text("Выберите файл для восстановления данных") },
        confirmButton = {
            Button(onClick = { onImport("/path/to/backup.json") }) {
                Text("Импорт")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}