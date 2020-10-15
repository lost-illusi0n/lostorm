package net.lostillusion.lostorm.annotations

import net.lostillusion.lostorm.mapper.Converter
import kotlin.reflect.KClass

/**
 * Marks a field to apply the specified [converter] to it.
 *
 * @property converter The [KClass] of a [Converter] for this type of field.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class ValueConverter(
    val converter: KClass<out Converter<*, *>>
)