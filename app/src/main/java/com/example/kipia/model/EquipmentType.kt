package com.example.kipia.model

enum class EquipmentType(val displayName: String) {
    // Оборудование для узлов (ОД, В)
    FLOOD_DETECTOR("Сигнализатор затопления"),
    OPENING_DETECTOR("Сигнализатор вскрытия"),
    PRESSURE_GAUGE("Манометр давления"),
    PRESSURE_TRANSDUCER("Преобразователь давления"),
    DPS("ДПС"),

    // Оборудование для задвижек
    VALVE("Задвижка"),
    ELECTRIC_DRIVE("Эл.привод"),
    BUR("БУР"),
    BKP("БКП"),

    // Оборудование для инженерного отсека
    SHTM("ШТМ"),
    FIRE_ALARM("Прибор пожаро-охранный"),
    SMOKE_DETECTOR("Извещатель дымовой"),
    MANUAL_DETECTOR("Извещатель ручной"),
    SIREN("Оповещатель свето-звуковой"),
    AIR_CONDITIONER("Кондиционер"),
    CONTROL_PANEL("ЩСУ"),

    // Оборудование для трансформаторного отсека
    // (используем те же извещатели, но с другим контекстом)

    // Дополнительное оборудование
    TEMP_TRANSDUCER("Преобразователь температуры"),
    UZR("УЗР"),
    LEVEL_METER("Уровнемер"),
    DGK("ДГК"),
    BKEP("БКЭП")
}