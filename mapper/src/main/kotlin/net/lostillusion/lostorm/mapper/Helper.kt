package net.lostillusion.lostorm.mapper

import java.lang.IllegalArgumentException
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

internal fun toSafeSQL(value: Any?): String {
    return when(value) {
        is String -> "'$value'"
        is Date -> "'$value'"
        is Time -> "'$value'"
        is Timestamp -> "'$value'"
        //Double, Float, Long, Short, and Int should all fall under this
        is Number -> "$value"
        is Boolean -> "$value"
        null -> "NULL"
        else -> throw IllegalArgumentException("Could not convert value of type: ${value::class.simpleName} to safe sql!")
    }
}