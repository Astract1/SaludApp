package com.astract.saludapp

object TimeParser {
    fun parseCustomTime(input: String): Pair<Int, String>? {
        val regex = """^(\d+)\s*(hora(s)?|día(s)?|semana(s)?|mes(es)?)$""".toRegex(RegexOption.IGNORE_CASE)
        val matchResult = regex.matchEntire(input.trim())

        return matchResult?.let {
            val amount = it.groupValues[1].toInt()
            val unit = when(it.groupValues[2].lowercase()) {
                "hora", "horas" -> "horas"
                "día", "días" -> "días"
                "semana", "semanas" -> "semanas"
                "mes", "meses" -> "meses"
                else -> null
            }

            unit?.let { Pair(amount, it) }
        }
    }

    fun formatTimeDisplay(amount: Int, unit: String): String {
        return "$amount $unit"
    }

    fun getIntervalMillis(amount: Int, unit: String): Long {
        return when(unit) {
            "horas" -> amount * 60 * 60 * 1000L
            "días" -> amount * 24 * 60 * 60 * 1000L
            "semanas" -> amount * 7 * 24 * 60 * 60 * 1000L
            "meses" -> amount * 30 * 24 * 60 * 60 * 1000L
            else -> 0L
        }
    }
}