package net.lostillusion.lostorm.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Columns(
    val columnName: String = "",
    val nullable: Boolean = false,
    val unique: Boolean = false
)
