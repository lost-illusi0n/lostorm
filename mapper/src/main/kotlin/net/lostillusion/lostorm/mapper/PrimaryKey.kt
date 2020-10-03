package net.lostillusion.lostorm.mapper

class PrimaryKey(vararg val columns: Column<*, *>) {
    val asSQL = columns.joinToString(", ", transform = Column<*, *>::columnName).let {
        if(it.isEmpty()) ""
        else "primary key($it)"
    }
}