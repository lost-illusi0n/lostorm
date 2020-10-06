@file:Suppress("unused")

package net.lostillusion.lostorm.mapper

import kotlin.reflect.KClass

//T = Base
//B = SQL
/**
 * A class to represent an SQL column with Lostorm additions.
 *
 * @param T The Custom type of this column.
 * @param B The SQL type of this column.
 * @param columnName The name of this column.
 * @param isPrimary Whether this column is part of the primary key.
 * @param hasDefaultValue Whether this column has a default value.
 * @param defaultValue The default value of the column.
 * @param nullable Whether this column is nullable.
 * @param unique Whether this column is unique.
 * @param valueConverter The [Converter] for this column.
 * @param sqlClass The [KClass] representation of [B].
 */
class Column<T: Any, B: Any>(
    val columnName: String,
    val isPrimary: Boolean = false,
    val hasDefaultValue: Boolean = false,
    val defaultValue: String? = null,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val valueConverter: Converter<T, B>,
    val sqlClass: KClass<out Any>
) {
    /**
     * Creates a new [Column] with [isPrimary] set to true.
     * @return The new [Column].
     */
    fun primary() = Column(columnName, true, hasDefaultValue, defaultValue, nullable, unique, valueConverter, sqlClass)
    /**
     * Creates a new [Column] with [hasDefaultValue] set to true and [defaultValue] set to [value].
     * @return The new [Column].
     */
    fun defaultValue(value: String?) = Column(columnName, isPrimary, true, value, nullable, unique, valueConverter, sqlClass)
    /**
     * Creates a new [Column] with [nullable] set to true.
     * @return The new [Column].
     */
    fun nullable() = Column(columnName, isPrimary, hasDefaultValue, defaultValue, true, unique, valueConverter, sqlClass)
    /**
     * Creates a new [Column] with [unique] set to true.
     * @return The new [Column].
     */
    fun unique() = Column(columnName, isPrimary, hasDefaultValue, defaultValue, nullable, true, valueConverter, sqlClass)
    /**
     * Creates a new [Column] with [valueConverter] set to [converter].
     * @return The new [Column].
     */
    fun <T1: Any> converter(converter: Converter<T1, B>) = Column(columnName, isPrimary, hasDefaultValue, defaultValue, nullable, unique, converter, sqlClass)
}

/**
 * Creates a new [Boolean] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun bool(columnName: String) = Column<Boolean, Boolean>(columnName, valueConverter = DefaultConverter(), sqlClass = Boolean::class)

/**
 * Creates a new [Int] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun int(columnName: String) = Column<Int, Int>(columnName, valueConverter = DefaultConverter(), sqlClass = Integer::class)

/**
 * Creates a new [String] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun text(columnName: String) = Column<String, String>(columnName, valueConverter = DefaultConverter(), sqlClass = String::class)

/**
 * Creates a new [Long] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun long(columnName: String) = Column<Long, Long>(columnName, valueConverter = DefaultConverter(), sqlClass = Long::class)

/**
 * Creates a new [Short] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun short(columnName: String) = Column<Short, Short>(columnName, valueConverter = DefaultConverter(), sqlClass = Short::class)

/**
 * Creates a new [ByteArray] [Column]
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun binary(columnName: String) = Column<ByteArray, ByteArray>(columnName, valueConverter = DefaultConverter(), sqlClass = ByteArray::class)

/**
 * Creates a new [Float] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun real(columnName: String) = Column<Float, Float>(columnName, valueConverter = DefaultConverter(), sqlClass = Float::class)

/**
 * Creates a new [Double] [Column].
 * @param columnName The name of the [Column].
 * @return The new [Column].
 */
fun float(columnName: String) = Column<Double, Double>(columnName, valueConverter = DefaultConverter(), sqlClass = Double::class)

/**
 * Creates a new [Boolean] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> bool(columnName: String, converter: Converter<Boolean, C>) = Column(columnName, valueConverter = converter, sqlClass = Boolean::class)

/**
 * Creates a new [Int] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> int(columnName: String, converter: Converter<Int, C>) = Column(columnName, valueConverter = converter, sqlClass = Integer::class)

/**
 * Creates a new [String] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> text(columnName: String, converter: Converter<String, C>) = Column(columnName, valueConverter = converter, sqlClass = String::class)

/**
 * Creates a new [Long] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> long(columnName: String, converter: Converter<Long, C>) = Column(columnName, valueConverter = converter, sqlClass = Long::class)

/**
 * Creates a new [Short] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> short(columnName: String, converter: Converter<Short, C>) = Column(columnName, valueConverter = converter, sqlClass = Short::class)

/**
 * Creates a new [ByteArray] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> binary(columnName: String, converter: Converter<ByteArray, C>) = Column(columnName, valueConverter = converter, sqlClass = ByteArray::class)

/**
 * Creates a new [Float] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
fun <C: Any> real(columnName: String, converter: Converter<Float, C>) = Column(columnName, valueConverter = converter, sqlClass = Float::class)

/**
 * Creates a new [Double] [Column] but with a custom [Converter]
 * @param columnName The name of the [Column].
 * @param converter The custom [Converter].
 * @return The new [Column].
 */
