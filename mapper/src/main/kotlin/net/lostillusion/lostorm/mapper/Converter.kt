package net.lostillusion.lostorm.mapper

/**
 * A converter for Custom types to SQL types.
 *
 * @param T The custom type.
 * @param B The SQL type.
 */
interface Converter<T: Any, B: Any> {
    /**
     * Convert the [B] (SQL type) to [T] (Custom Type)
     *
     * @param value The SQL type, see [B]
     * @return The Custom type, see [T]
     */
    fun convertToKotlin(value: B): T

    /**
     * Convert the [T] (Custom type) to [B] (SQL type)
     *
     * @param value The Custom type, see [T]
     * @return The SQL type, see [B]
     */
    fun convertToSql(value: T): B
}

/**
 * A generic default converter which does no converting.
 *
 * @param A The SQL type.
 */
class DefaultConverter<A: Any>: Converter<A, A> {
    override fun convertToKotlin(value: A): A = value

    override fun convertToSql(value: A): A = value
}

/**
 * The null safe version of [Converter.convertToSql]
 *
 * @param from A nullable Custom type, see [T]
 * @return The SQL type, or null if [from] was null.
 */
fun <T: Any, B: Any> Converter<T, B>.orConvertToSql(from: T?) = from?.let(::convertToSql)
/**
 * The null safe version of [Converter.convertToKotlin]
 *
 * @param from A nullable SQL type, see [B]
 * @return The Custom type, or null if [from] was null.
 */
fun <T: Any, B: Any> Converter<T, B>.orConvertToKotlin(from: B?) = from?.let(::convertToKotlin)