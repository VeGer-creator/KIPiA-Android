// app/src/main/java/com/example/kipia/ui/EditEquipmentScreen.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.DetailedEquipmentEntity
import com.example.kipia.model.EquipmentType

@Composable
fun EditEquipmentScreen(
    equipmentId: Long,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    var currentEquipment by remember { mutableStateOf<DetailedEquipmentEntity?>(null) }

    // Загружаем оборудование при открытии
    LaunchedEffect(equipmentId) {
        val equipment = equipmentViewModel.getEquipmentById(equipmentId)
        currentEquipment = equipment
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование оборудования") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentEquipment?.let {
                                equipmentViewModel.updateEquipment(it)
                                onSaveClick()
                            }
                        },
                        enabled = currentEquipment != null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        currentEquipment?.let { equipment ->
            EquipmentForm(
                equipment = equipment,
                onEquipmentUpdate = { updatedEquipment ->
                    currentEquipment = updatedEquipment
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        } ?: run {
            // Показываем загрузку, если оборудование еще не загружено
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun EquipmentForm(
    equipment: DetailedEquipmentEntity,
    onEquipmentUpdate: (DetailedEquipmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Основная информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Основная информация",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Название оборудования
                OutlinedTextField(
                    value = equipment.name,
                    onValueChange = { newName ->
                        onEquipmentUpdate(equipment.copy(name = newName))
                    },
                    label = { Text("Название оборудования") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Тип оборудования (только для чтения)
                OutlinedTextField(
                    value = EquipmentType.valueOf(equipment.equipmentType).displayName,
                    onValueChange = { },
                    label = { Text("Тип оборудования") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Общие характеристики
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Общие характеристики",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = equipment.model,
                    onValueChange = { newModel ->
                        onEquipmentUpdate(equipment.copy(model = newModel))
                    },
                    label = { Text("Модель") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.manufacturer,
                    onValueChange = { newManufacturer ->
                        onEquipmentUpdate(equipment.copy(manufacturer = newManufacturer))
                    },
                    label = { Text("Производитель") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.serialNumber,
                    onValueChange = { newSerial ->
                        onEquipmentUpdate(equipment.copy(serialNumber = newSerial))
                    },
                    label = { Text("Заводской номер") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.productionYear,
                    onValueChange = { newYear ->
                        onEquipmentUpdate(equipment.copy(productionYear = newYear))
                    },
                    label = { Text("Год выпуска") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.verificationYear,
                    onValueChange = { newVerification ->
                        onEquipmentUpdate(equipment.copy(verificationYear = newVerification))
                    },
                    label = { Text("Год поверки") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

// В функции EquipmentForm в EditEquipmentScreen.kt обновляем блок специфических характеристик:

// Специфические характеристики
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Специфические характеристики",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = equipment.nominal,
                    onValueChange = { newNominal ->
                        onEquipmentUpdate(equipment.copy(nominal = newNominal))
                    },
                    label = { Text("Номинал") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.pressureLimit,
                    onValueChange = { newPressure ->
                        onEquipmentUpdate(equipment.copy(pressureLimit = newPressure))
                    },
                    label = { Text("Граница давления") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = equipment.softwareVersion,
                    onValueChange = { newSoftware ->
                        onEquipmentUpdate(equipment.copy(softwareVersion = newSoftware))
                    },
                    label = { Text("Версия ПО") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Дополнительные характеристики для БУР, БКЭП и других релейных защит
                if (equipment.equipmentType in listOf("BUR", "BKEP", "DPS", "BKP", "UZR")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = equipment.mo,
                        onValueChange = { newMo ->
                            onEquipmentUpdate(equipment.copy(mo = newMo))
                        },
                        label = { Text("МО") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.mz,
                        onValueChange = { newMz ->
                            onEquipmentUpdate(equipment.copy(mz = newMz))
                        },
                        label = { Text("МЗ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.mto,
                        onValueChange = { newMto ->
                            onEquipmentUpdate(equipment.copy(mto = newMto))
                        },
                        label = { Text("МТО") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.mtz,
                        onValueChange = { newMtz ->
                            onEquipmentUpdate(equipment.copy(mtz = newMtz))
                        },
                        label = { Text("МТЗ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.muo,
                        onValueChange = { newMuo ->
                            onEquipmentUpdate(equipment.copy(muo = newMuo))
                        },
                        label = { Text("МУО") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.muz,
                        onValueChange = { newMuz ->
                            onEquipmentUpdate(equipment.copy(muz = newMuz))
                        },
                        label = { Text("МУЗ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = equipment.outputContacts,
                        onValueChange = { newContacts ->
                            onEquipmentUpdate(equipment.copy(outputContacts = newContacts))
                        },
                        label = { Text("Кол.об.вых.зв") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}