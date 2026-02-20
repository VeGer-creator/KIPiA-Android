// app/src/main/java/com/example/kipia/ui/SyncViewModelFactory.kt
package com.example.kipia.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SyncViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SyncViewModel::class.java)) {
            return SyncViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}