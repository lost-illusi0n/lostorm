package net.lostillusion.lostorm.mapper

//T = Base
//B = SQL
interface Converter<T: Any, B: Any> {
    fun convertToKotlin(value: B): T

    fun convertToSql(value: T): B
}

class DefaultConverter<A: Any>: Converter<A, A> {
    override fun convertToKotlin(value: A): A = value

    override fun convertToSql(value: A): A = value
}

fun <T: Any, B: Any> Converter<T, B>.orConvertToSql(from: T?) = from?.let(::convertToSql)
fun <T: Any, B: Any> Converter<T, B>.orConvertToKotlin(from: B?) = from?.let(::convertToKotlin)