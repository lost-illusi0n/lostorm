package net.lostillusion.lostorm.annotations

/**
 * Marks the class to have an Entity generated for it.
 * Required for any data class you want to use in Lostorm.
 *
 * @property tableName The SQL Table name, or a default (``${ClassName}Table``) is generated for it.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EntityDataClass(val tableName: String = "_none")