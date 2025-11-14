package com.example.kipia.model

data class PKU(
    val id: Long = 0L, // ID для базы данных
    val name: String,   // например, "КП 867 км"
    val description: String = "", // описание, если нужно
    val isCompleted: Boolean = false // флаг, выполнена ли проверка
)