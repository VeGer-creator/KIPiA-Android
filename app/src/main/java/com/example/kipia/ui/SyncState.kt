package com.example.kipia.ui

// В SyncState.kt добавьте поля:
data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0,
    val syncProgress: Int = 0,
    val syncStatus: String = "",
    val isAdvertising: Boolean = false,
    val isDiscovering: Boolean = false,
    val connectedDevices: List<String> = emptyList(),
    val errorMessage: String? = null,
    val syncCompleted: Boolean = false, // Добавьте это
    val newItemsCount: Int = 0 // Добавьте это
)