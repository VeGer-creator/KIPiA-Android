package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun EditNameDialog(
    currentName: String,
    currentDescription: String = "",
    title: String,
    nameHint: String = "Введите название",
    descriptionHint: String = "Введите описание",
    showDescription: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var newDescription by remember { mutableStateOf(currentDescription) }

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
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поле для названия
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(nameHint) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Поле для описания (если нужно)
                if (showDescription) {
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text(descriptionHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Кнопки
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
                            if (newName.isNotBlank()) {
                                onConfirm(newName, newDescription)
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}