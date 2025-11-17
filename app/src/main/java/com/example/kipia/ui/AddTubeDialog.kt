package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kipia.utils.NameUtils

@Composable
fun AddTubeDialog(
    kpName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    val tubeOptions = remember(kpName) {
        NameUtils.getTubeNameOptionsFromKP(kpName)
    }

    // Выбираем первый вариант по умолчанию
    LaunchedEffect(tubeOptions) {
        if (tubeOptions.isNotEmpty() && selectedOption.isEmpty()) {
            selectedOption = tubeOptions.first()
        }
    }

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
                Text("Добавить участок МН", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Выберите вариант:", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))

                // Список вариантов
                LazyColumn(
                    modifier = Modifier.height(120.dp)
                ) {
                    items(tubeOptions) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = option }
                                .padding(8.dp),
                            verticalAlignment = CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поле для своего варианта
                OutlinedTextField(
                    value = customName,
                    onValueChange = {
                        customName = it
                        selectedOption = it
                    },
                    label = { Text("Свой вариант") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Введите свое название") }
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
                            if (selectedOption.isNotBlank()) {
                                onConfirm(selectedOption)
                            }
                        },
                        enabled = selectedOption.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}