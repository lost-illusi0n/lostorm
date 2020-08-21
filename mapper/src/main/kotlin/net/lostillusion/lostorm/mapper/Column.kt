package net.lostillusion.lostorm.mapper

import kotlin.reflect.KClass

class Column<T>(
    val columnName: String,
    val isPrimary: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: T? = null,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val converter: (Any) -> T?,
    val valueClass: KClass<out Any>
) {
    fun primary() = Column<T>(columnName, true, hasDefaultValue, defaultValue, nullable, unique, converter, valueClass)
    fun defaultValue(value: T) = Column<T>(columnName, isPrimary, true, value, nullable, unique, converter, valueClass)
    fun nullable() = Column<T?>(columnName, isPrimary, hasDefaultValue, defaultValue, true, unique, converter, valueClass)
    fun unique() = Column<T>(columnName, isPrimary, hasDefaultValue, defaultValue, nullable, true, converter, valueClass)
}

fun bool(columnName: String) = Column(columnName, converter = { it as? Boolean }, valueClass = Boolean::class)
fun int(columnName: String) = Column(columnName, converter = { it as? Int }, valueClass = Integer::class)
fun text(columnName: String) = Column(columnName, converter = { it as? String }, valueClass = String::class)
fun long(columnName: String) = Column(columnName, converter = { it as? Long}, valueClass = Long::class)
fun short(columnName: String) = Column(columnName, converter = { it as? Short }, valueClass = Short::class)
fun binary(columnName: String) = Column(columnName, converter = { it as? ByteArray }, valueClass = ByteArray::class)
fun real(columnName: String) = Column(columnName, converter = { it as? Float }, valueClass = Float::class)
fun float(columnName: String) = Column(columnName, converter = { it as? Double }, valueClass = Double::class)