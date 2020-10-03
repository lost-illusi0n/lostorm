package net.lostillusion.lostorm.annotations

import net.lostillusion.lostorm.mapper.Converter
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ValueConverter(
    val converter: KClass<out Converter<*, *>>
)