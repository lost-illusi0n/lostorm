package net.lostillusion.lostorm.mapper

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

object DataTypes {
    val kotlinToSQL = mapOf(
        String::class to "text",
        Boolean::class to "boolean",
        Integer::class to "integer",
        Short::class to "smallint",
//        ByteArray::class to "bytea",
        Float::class to "real",
        Double::class to "float",
        Long::class to "bigint",
        Date::class to "date",
        Time::class to "time",
        Timestamp::class to "timestamptz"
    )
}
