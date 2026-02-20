package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.kipia.ui.SyncState // ДОБАВЬТЕ ЭТУ СТРОКУ

@Composable
fun SyncScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(context)
    )
    val syncState by syncViewModel.syncState.collectAsState()

    var showPermissionScreen by remember { mutableStateOf(false) }

    // Показываем экран разрешений при первом входе или если нужны разрешения
    LaunchedEffect(Unit) {
        if (!syncViewModel.hasRequiredPermissions()) {
            showPermissionScreen = true
        }
    }

    if (showPermissionScreen) {
        PermissionScreen(
            onPermissionsGranted = {
                showPermissionScreen = false
                // Автоматически запускаем синхронизацию после предоставления разрешений
                syncViewModel.startSync()
            },
            onSkip = {
                showPermissionScreen = false
                // Пользователь может использовать ручную синхронизацию
            }
        )
    } else {
        // Стандартный экран синхронизации
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Синхронизация данных") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                    actions = {
                        // Кнопка для повторного запроса разрешений
                        IconButton(onClick = { showPermissionScreen = true }) {
                            Icon(Icons.Default.Security, contentDescription = "Разрешения")
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
                    SyncControlsCard(syncState, syncViewModel)
                }

                item {
                    SyncSettingsCard(syncViewModel, syncState)
                }

                item {
                    LastSyncInfo(syncState)
                }

                if (syncState.connectedDevices.isNotEmpty()) {
                    item {
                        ConnectedDevicesCard(syncState)
                    }
                }
            }
        }
    }
}
@Composable
fun SyncStatusCard(
    state: SyncState,
    viewModel: SyncViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (state.isSyncing) Icons.Default.Sync else Icons.Default.SyncDisabled,
                    contentDescription = null,
                    tint = if (state.isSyncing) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = if (state.isSyncing) "Синхронизация..." else "Синхронизация отключена",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (state.isSyncing) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = state.syncProgress / 100f
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.syncStatus,
                    fontSize = 14.sp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                )
            }

            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colors.error
                    )
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = MaterialTheme.colors.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.clearError() },
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("ОК")
                }
            }
        }
    }
}

@Composable
private fun SyncControlsCard(
    state: SyncState,
    viewModel: SyncViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Управление синхронизацией",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.startSync() },
                    enabled = !state.isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Начать синхронизацию")
                }

                Button(
                    onClick = { viewModel.stopSync() },
                    enabled = state.isSyncing,
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SyncDisabled, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Остановить")
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Исправлено: было Mod вместо Modifier

            Button(
                onClick = { viewModel.prepareManualSync() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Text("Подготовить данные для экспорта")
            }
        }
    }
}

@Composable
private fun SyncSettingsCard(
    viewModel: SyncViewModel,
    state: SyncState // Добавляем параметр state
) {
    var syncEnabled by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(false) }

    // Используем состояние из ViewModel
    LaunchedEffect(state) {
        // Здесь можно обновлять локальное состояние на основе state
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Настройки синхронизации",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Синхронизация по Wi-Fi Direct")
                Switch(
                    checked = syncEnabled,
                    onCheckedChange = { enabled -> // Явно указываем тип параметра
                        syncEnabled = enabled
                        viewModel.setSyncEnabled(enabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Автоматическая синхронизация")
                Switch(
                    checked = autoSync,
                    onCheckedChange = { enabled -> // Явно указываем тип параметра
                        autoSync = enabled
                        viewModel.setAutoSync(enabled)
                    }
                )
            }
        }
    }
}

// В SyncScreen.kt обновите LastSyncInfo:
@Composable
private fun LastSyncInfo(state: SyncState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Информация о синхронизации",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.lastSyncTime > 0) {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val dateString = dateFormat.format(Date(state.lastSyncTime))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Text("Последняя синхронизация: $dateString")
                }

                if (state.syncCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✅ Синхронизация завершена",
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                    if (state.newItemsCount > 0) {
                        Text(
                            text = "Добавлено новых элементов: ${state.newItemsCount}",
                            color = MaterialTheme.colors.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                Text("Синхронизация еще не выполнялась")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Sync, contentDescription = null)
                Text("Статус: ${getSyncStatus(state)}")
            }

            // Показываем подключенные устройства
            if (state.connectedDevices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Подключенные устройства: ${state.connectedDevices.size}")
            }
        }
    }
}

@Composable
private fun getSyncStatus(state: SyncState): String {
    return when {
        state.isSyncing -> "Синхронизация..."
        state.connectedDevices.isNotEmpty() -> "Устройства подключены"
        state.isAdvertising && state.isDiscovering -> "Поиск устройств..."
        state.isAdvertising -> "Ожидание подключения..."
        else -> "Ожидание"
    }
}
@Composable
private fun ConnectedDevicesCard(state: SyncState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Подключенные устройства",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.connectedDevices.forEach { device ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Wifi, contentDescription = null)
                    Text(device)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}