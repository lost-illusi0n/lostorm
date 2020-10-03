package net.lostillusion.lostorm.mapper

interface Converter<B: Any, T: Any> {
    fun convertTo(value: B): T

    fun convertFrom(from: T): B
}

class DefaultConverter<A: Any>: Converter<A, A> {
    override fun convertTo(value: A): A = value

    override fun convertFrom(from: A): A = from
}