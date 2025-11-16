// app/src/main/java/com/example/kipia/model/Tube.kt
package com.example.kipia.model

data class Tube(
    val id: Long = 0L,
    val name: String, // Например, "Сургут-Полоцк" или "Холмогоры-Клин"
    val pkuId: Long // Связь с PKU
)