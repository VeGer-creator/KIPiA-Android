package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kipia.model.NodeType
import com.example.kipia.utils.NameUtils

@Composable
fun AddNodeDialog(
    tubeName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, NodeType) -> Unit
) {
    var nodeName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NodeType.PRESSURE_WELL) }

    // Получаем номер участка МН
    val tubeNumber = NameUtils.getNodeNumberFromTube(tubeName)

    // Автоматически определяем тип по названию участка МН
    LaunchedEffect(tubeName) {
        val (prefix, _) = NameUtils.getNodePrefixFromTube(tubeName)
        selectedType = when (prefix) {
            "ОД" -> NodeType.PRESSURE_WELL
            "В" -> NodeType.VENT_WELL
            "Задвижка" -> NodeType.VALVE
            else -> NodeType.PRESSURE_WELL
        }

        // Автозаполнение номера
        if (tubeNumber.isNotEmpty() && nodeName.isEmpty()) {
            nodeName = "/1" // Начальный номер
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
                Text(
                    text = "Добавить объект",
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Выбор типа узла
                Text("Тип объекта:")
                LazyColumn(
                    modifier = Modifier.height(160.dp)
                ) {
                    items(NodeType.values()) { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(
                                text = type.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поле для ввода названия
                OutlinedTextField(
                    value = nodeName,
                    onValueChange = { nodeName = it },
                    label = {
                        Text(
                            when (selectedType) {
                                NodeType.PRESSURE_WELL -> "Номер ОД (например: /1)"
                                NodeType.VENT_WELL -> "Номер В (например: /2)"
                                NodeType.VALVE -> "Номер задвижки (например: 878)"
                                NodeType.CUSTOM -> "Название объекта"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            when (selectedType) {
                                NodeType.PRESSURE_WELL -> "Введите номер ОД"
                                NodeType.VENT_WELL -> "Введите номер В"
                                NodeType.VALVE -> "Введите номер задвижки"
                                NodeType.CUSTOM -> "Введите название"
                            }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Предпросмотр полного имени
                if (nodeName.isNotBlank()) {
                    val fullName = when (selectedType) {
                        NodeType.CUSTOM -> nodeName
                        NodeType.VALVE -> "${selectedType.prefix} $nodeName"
                        else -> "${selectedType.prefix} $tubeNumber$nodeName"
                    }
                    Text(
                        text = "Полное имя: $fullName",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            if (nodeName.isNotBlank()) {
                                // Формируем полное имя для сохранения
                                val fullNameToSave = when (selectedType) {
                                    NodeType.CUSTOM -> nodeName
                                    NodeType.VALVE -> "${selectedType.prefix} $nodeName"
                                    else -> "${selectedType.prefix} $tubeNumber$nodeName"
                                }
                                onConfirm(fullNameToSave, selectedType)
                                onDismiss()
                            }
                        },
                        enabled = nodeName.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}