// app/src/main/java/com/example/kipia/ui/AddEquipmentDialog.kt
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
import com.example.kipia.model.EquipmentType

@Composable
fun AddEquipmentDialog(
    nodeId: Long? = null,
    sectionId: Long? = null,
    nodeName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (EquipmentType) -> Unit
) {
    var selectedType by remember { mutableStateOf<EquipmentType?>(null) }

    // Определяем доступные типы оборудования в зависимости от контекста
    val availableEquipmentTypes = remember(nodeName) {
        when {
            nodeName.contains("Задвижка", ignoreCase = true) -> {
                // Только оборудование для задвижек
                listOf(
                    EquipmentType.VALVE,
                    EquipmentType.ELECTRIC_DRIVE,
                    EquipmentType.BUR,
                    EquipmentType.BKP
                )
            }
            nodeName.contains("ОД", ignoreCase = true) -> {
                // Оборудование для отбора давления
                listOf(
                    EquipmentType.FLOOD_DETECTOR,
                    EquipmentType.OPENING_DETECTOR,
                    EquipmentType.PRESSURE_GAUGE,
                    EquipmentType.PRESSURE_TRANSDUCER,
                    EquipmentType.DPS
                )
            }
            nodeName.contains("В", ignoreCase = true) -> {
                // Оборудование для вантузных колодцев
                listOf(
                    EquipmentType.FLOOD_DETECTOR,
                    EquipmentType.OPENING_DETECTOR
                )
            }
            else -> EquipmentType.values().toList()
        }
    }

    // Устанавливаем первый элемент по умолчанию
    LaunchedEffect(availableEquipmentTypes) {
        if (availableEquipmentTypes.isNotEmpty() && selectedType == null) {
            selectedType = availableEquipmentTypes.first()
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
                    text = "Добавить оборудование",
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Выбор типа оборудования
                Text("Выберите тип оборудования:")
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(availableEquipmentTypes) { type ->
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
                            selectedType?.let { onConfirm(it) }
                            onDismiss()
                        },
                        enabled = selectedType != null
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}