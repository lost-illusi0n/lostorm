package net.lostillusion.lostorm.mapper

object DataTypes {
    val kotlinToSQL = mapOf(
        String::class to "text",
        Boolean::class to "boolean",
        Integer::class to "integer",
        Short::class to "smallint",
        ByteArray::class to "bytea",
        Float::class to "real",
        Double::class to "float",
        Long::class to "bigint"
    )
}
