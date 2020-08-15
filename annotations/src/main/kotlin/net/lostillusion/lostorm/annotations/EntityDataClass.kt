package net.lostillusion.lostorm.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class EntityDataClass(val tableName: String = "_none")