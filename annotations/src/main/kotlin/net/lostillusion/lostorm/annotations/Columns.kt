package net.lostillusion.lostorm.annotations

/**
 * Describes the properties of a Column.
 * Nullability of a column is set by whether the type of the field is nullable.
 *
 * @property columnName The SQL name of the Column.
 * @property primaryKey Whether this Column is apart of the Primary key.
 * @property unique Whether this Column is unique.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Columns(
    val columnName: String = "",
    val primaryKey: Boolean = false,
    val unique: Boolean = false,
    val defaultValue: String = "¯\\_(ツ)_/¯"
)
