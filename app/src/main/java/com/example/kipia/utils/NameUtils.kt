package com.example.kipia.utils

object NameUtils {

    // Соответствие КП для СП и ХК
    private val kpMapping = mapOf(
        "867" to "1101",
        "878" to "1113",
        "890" to "1126",
        "911" to "1146",
        "912" to "1147",
        "924" to "1159",
        "937" to "1172",
        "950" to "1185"
        // 1194 остается без изменений
    )

    // Получить название ПКУ от КП
    fun getPKUNameFromKP(kpName: String): String {
        return "ПКУ $kpName"
    }

    // Получить варианты названий участков МН от КП
    fun getTubeNameOptionsFromKP(kpName: String): List<String> {
        val kpNumber = extractNumber(kpName)
        val options = mutableListOf<String>()

        // Вариант СП
        options.add("Участок МН $kpNumber км СП")

        // Вариант ХК (если есть соответствие)
        kpMapping[kpNumber]?.let { hkNumber ->
            options.add("Участок МН $hkNumber км ХК")
        }

        // Свой вариант
        options.add("Участок МН $kpNumber км")

        return options
    }

    // Получить префикс для колодцев от участка МН
    fun getNodePrefixFromTube(tubeName: String): Pair<String, String> {
        return when {
            tubeName.contains("ОД") || tubeName.contains("СП") -> "ОД" to "ОД"
            tubeName.contains("В") || tubeName.contains("ХК") -> "В" to "В"
            tubeName.contains("Задвижка") -> "Задвижка" to "Задвижка"
            else -> "Объект" to ""
        }
    }

    // Извлечь число из названия КП
    private fun extractNumber(kpName: String): String {
        val regex = """(\d+)""".toRegex()
        return regex.find(kpName)?.value ?: ""
    }

    // Получить номер для колодца из названия участка МН
    fun getNodeNumberFromTube(tubeName: String): String {
        val regex = """(\d+)""".toRegex()
        return regex.find(tubeName)?.value ?: ""
    }
}