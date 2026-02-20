// app/src/main/java/com/example/kipia/ui/SettingsScreen.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    initialMuteSplash: Boolean,
    onMuteSplashChange: (Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    var muteSplash by remember { mutableStateOf(initialMuteSplash) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Отключить звук заставки",
                    style = MaterialTheme.typography.body1
                )
                Switch(
                    checked = muteSplash,
                    onCheckedChange = { newValue ->
                        muteSplash = newValue
                        onMuteSplashChange(newValue)
                    }
                )
            }
        }
    }
}