package net.lostillusion.lostorm.mapper

class Column<T>(
    val columnName: String,
    val defaultValue: T? = null,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val converter: (Any) -> T?
) {
    fun defaultValue(value: T) = Column<T>(columnName, value, nullable, unique, converter)
    fun nullable() = Column<T?>(columnName, defaultValue, true, unique, converter)
    fun unique() = Column<T>(columnName, defaultValue, nullable, true, converter)
}

fun bool(columnName: String) = Column(columnName, converter = { it as? Boolean })
fun int(columnName: String) = Column(columnName, converter = { it as? Int })
fun text(columnName: String) = Column(columnName, converter = { it as? String })