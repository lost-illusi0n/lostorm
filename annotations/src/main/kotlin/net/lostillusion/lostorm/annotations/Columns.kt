package net.lostillusion.lostorm.annotations

/**
 * Describes the properties of a Column.
 *
 * @property columnName The SQL name of the Column.
 * @property primaryKey Whether this Column is apart of the Primary key.
 * @property nullable Whether this Column is nullable.
 * @property unique Whether this Column is unique.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Columns(
    val columnName: String = "",
    val primaryKey: Boolean = false,
    val nullable: Boolean = false,
    val unique: Boolean = false,
    val defaultValue: String = "¯\\_(ツ)_/¯"
)
