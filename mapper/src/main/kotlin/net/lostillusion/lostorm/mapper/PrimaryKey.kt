package net.lostillusion.lostorm.mapper

/**
 * Represents an SQL Primary Key with the specified [columns].
 *
 * @param columns The [Column]s to be part of the Primay Key.
 */
class PrimaryKey(vararg val columns: Column<*, *>) {
    /**
     * The SQL Primary Key.
     */
    val asSQL = columns.joinToString(", ", transform = Column<*, *>::columnName).let {
        if(it.isEmpty()) ""
        else "primary key($it)"
    }
}