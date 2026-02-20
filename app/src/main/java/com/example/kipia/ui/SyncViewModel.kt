// app/src/main/java/com/example/kipia/ui/SyncViewModel.kt
package com.example.kipia.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.data.PreferencesManager
import com.example.kipia.sync.NearbySyncService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class SyncViewModel(private val context: Context) : ViewModel() {
    private val preferencesManager = PreferencesManager(context)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val syncUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                NearbySyncService.ACTION_SYNC_UPDATE -> {
                    val isAdvertising = intent.getBooleanExtra("is_advertising", false)
                    val isDiscovering = intent.getBooleanExtra("is_discovering", false)
                    val connectedDevices = intent.getStringArrayExtra("connected_devices")?.toList() ?: emptyList()
                    val error = intent.getStringExtra("error")
                    val syncCompleted = intent.getBooleanExtra("sync_completed", false)
                    val newItemsCount = intent.getIntExtra("new_items", 0)

                    _syncState.value = _syncState.value.copy(
                        isAdvertising = isAdvertising,
                        isDiscovering = isDiscovering,
                        connectedDevices = connectedDevices,
                        errorMessage = error,
                        syncCompleted = syncCompleted,
                        newItemsCount = newItemsCount,
                        lastSyncTime = if (syncCompleted) System.currentTimeMillis() else _syncState.value.lastSyncTime
                    )

                    if (syncCompleted) {
                        android.util.Log.d("SyncViewModel", "🎉 Синхронизация завершена! Новых элементов: $newItemsCount")
                    }
                }
            }
        }
    }

    init {
        // Регистрируем BroadcastReceiver с правильными флагами для Android 14+
        val filter = IntentFilter(NearbySyncService.ACTION_SYNC_UPDATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Для Android 13+ нужно указать флаг экспорта
            context.registerReceiver(
                syncUpdateReceiver,
                filter,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Context.RECEIVER_NOT_EXPORTED
                } else {
                    Context.RECEIVER_EXPORTED  // Для версий ниже
                }
            )
        } else {
            // Для старых версий Android
            context.registerReceiver(syncUpdateReceiver, filter)
        }

        viewModelScope.launch {
            preferencesManager.lastSyncTimestamp.collect { timestamp ->
                _syncState.value = _syncState.value.copy(lastSyncTime = timestamp)
            }
        }

        viewModelScope.launch {
            preferencesManager.syncEnabled.collect { enabled ->
                if (enabled) {
                    startSyncService()
                } else {
                    stopSyncService()
                }
            }
        }
    }

    fun hasRequiredPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
        }

        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun startSync() {
        if (!hasRequiredPermissions()) {
            _syncState.value = _syncState.value.copy(
                errorMessage = "Для синхронизации требуются разрешения на доступ к местоположению и Bluetooth"
            )
            return
        }

        _syncState.value = _syncState.value.copy(
            isSyncing = true,
            syncProgress = 0,
            syncStatus = "Начало синхронизации..."
        )

        viewModelScope.launch {
            try {
                val intent = android.content.Intent(context, NearbySyncService::class.java)
                context.startService(intent)

                _syncState.value = _syncState.value.copy(
                    syncProgress = 50,
                    syncStatus = "Поиск устройств..."
                )

            } catch (e: Exception) {
                _syncState.value = _syncState.value.copy(
                    isSyncing = false,
                    errorMessage = "Ошибка синхронизации: ${e.message}"
                )
            }
        }
    }

    fun stopSync() {
        _syncState.value = _syncState.value.copy(
            isSyncing = false,
            syncStatus = "Синхронизация остановлена"
        )

        stopSyncService()
    }

    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSyncEnabled(enabled)
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoSync(enabled)
        }
    }

    fun clearError() {
        _syncState.value = _syncState.value.copy(errorMessage = null)
    }

    fun prepareManualSync() {
        viewModelScope.launch {
            try {
                _syncState.value = _syncState.value.copy(
                    syncStatus = "Данные подготовлены для экспорта"
                )
            } catch (e: Exception) {
                _syncState.value = _syncState.value.copy(
                    errorMessage = "Ошибка подготовки данных: ${e.message}"
                )
            }
        }
    }

    private fun startSyncService() {
        val intent = android.content.Intent(context, NearbySyncService::class.java)
        context.startService(intent)

        _syncState.value = _syncState.value.copy(
            isAdvertising = true,
            isDiscovering = true
        )
    }

    private fun stopSyncService() {
        val intent = android.content.Intent(context, NearbySyncService::class.java)
        context.stopService(intent)

        _syncState.value = _syncState.value.copy(
            isAdvertising = false,
            isDiscovering = false
        )
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(syncUpdateReceiver)
        } catch (e: Exception) {
            // Игнорируем ошибки при отмене регистрации
            android.util.Log.e("SyncViewModel", "Ошибка при отмене регистрации receiver", e)
        }
    }
}