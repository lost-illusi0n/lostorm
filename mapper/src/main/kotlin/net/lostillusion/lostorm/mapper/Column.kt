package net.lostillusion.lostorm.mapper

import kotlin.reflect.KClass

class Column<T: Any, B: Any>(
    val columnName: String,
    val isPrimary: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: T? = null,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val valueConverter: Converter<T, B>,
    val valueClass: KClass<out Any>
) {
    fun primary() = Column(columnName, true, hasDefaultValue, defaultValue, nullable, unique, valueConverter, valueClass)
    fun defaultValue(value: T) = Column(columnName, isPrimary, true, value, nullable, unique, valueConverter, valueClass)
    fun nullable() = Column(columnName, isPrimary, hasDefaultValue, defaultValue, true, unique, valueConverter, valueClass)
    fun unique() = Column(columnName, isPrimary, hasDefaultValue, defaultValue, nullable, true, valueConverter, valueClass)
    fun <R: Any> converter(converter: Converter<T, R>) = Column(columnName, isPrimary, hasDefaultValue, defaultValue, nullable, unique, converter, valueClass)
}

fun bool(columnName: String) = Column<Boolean, Boolean>(columnName, valueConverter = DefaultConverter(), valueClass = Boolean::class)
fun int(columnName: String) = Column<Int, Int>(columnName, valueConverter = DefaultConverter(), valueClass = Integer::class)
fun text(columnName: String) = Column<String, String>(columnName, valueConverter = DefaultConverter(), valueClass = String::class)
fun long(columnName: String) = Column<Long, Long>(columnName, valueConverter = DefaultConverter(), valueClass = Long::class)
fun short(columnName: String) = Column<Short, Short>(columnName, valueConverter = DefaultConverter(), valueClass = Short::class)
fun binary(columnName: String) = Column<ByteArray, ByteArray>(columnName, valueConverter = DefaultConverter(), valueClass = ByteArray::class)
fun real(columnName: String) = Column<Float, Float>(columnName, valueConverter = DefaultConverter(), valueClass = Float::class)
fun float(columnName: String) = Column<Double, Double>(columnName, valueConverter = DefaultConverter(), valueClass = Double::class)

fun <C: Any> bool(columnName: String, converter: Converter<Boolean, C>) = Column(columnName, valueConverter = converter, valueClass = Boolean::class)
fun <C: Any> int(columnName: String, converter: Converter<Int, C>) = Column(columnName, valueConverter = converter, valueClass = Integer::class)
fun <C: Any> text(columnName: String, converter: Converter<String, C>) = Column(columnName, valueConverter = converter, valueClass = String::class)
fun <C: Any> long(columnName: String, converter: Converter<Long, C>) = Column(columnName, valueConverter = converter, valueClass = Long::class)
fun <C: Any> short(columnName: String, converter: Converter<Short, C>) = Column(columnName, valueConverter = converter, valueClass = Short::class)
fun <C: Any> binary(columnName: String, converter: Converter<ByteArray, C>) = Column(columnName, valueConverter = converter, valueClass = ByteArray::class)
fun <C: Any> real(columnName: String, converter: Converter<Float, C>) = Column(columnName, valueConverter = converter, valueClass = Float::class)
fun <C: Any> float(columnName: String, converter: Converter<Double, C>) = Column(columnName, valueConverter = converter, valueClass = Double::class)