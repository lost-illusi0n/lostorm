package net.lostillusion.lostorm.mapper

import java.lang.IllegalArgumentException

internal fun toSafeSQL(value: Any?): String {
    return when(value) {
        is String -> "'$value'"
        is Int -> "$value"
        is Long -> "$value"
        is Short -> "$value"
        is Boolean -> "$value"
        null -> "NULL"
        else -> throw IllegalArgumentException("Could not convert value of type: ${value::class.simpleName} to safe sql!")
    }
}