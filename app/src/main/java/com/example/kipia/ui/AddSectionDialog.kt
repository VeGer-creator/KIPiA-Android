// app/src/main/java/com/example/kipia/ui/AddSectionDialog.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun AddSectionDialog(
    pkuName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var sectionName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Добавить секцию", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Название секции") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например: Секция 1") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (sectionName.isNotBlank()) {
                                onConfirm(sectionName)
                            }
                        },
                        enabled = sectionName.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}