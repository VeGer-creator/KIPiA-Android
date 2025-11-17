package com.example.kipia.model

enum class NodeType(val displayName: String, val prefix: String) {
    PRESSURE_WELL("Колодец отбора давления", "ОД"),
    VENT_WELL("Вантузный колодец", "В"),
    VALVE("Задвижка", "Задвижка"),
    CUSTOM("Другой", "");

    companion object {
        fun fromString(value: String): NodeType {
            return values().find { it.name == value } ?: CUSTOM
        }
    }
}