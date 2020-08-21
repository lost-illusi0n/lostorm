package net.lostillusion.lostorm.mapper

import kotlin.reflect.KProperty1

//Type-parameter D = base data class
abstract class Entity<D: Any>(
    val tableName: String
) {
    abstract val columns: List<Column<*>>
    abstract val columnsToValues: Map<Column<*>, KProperty1<D, *>>
    val primaryKey by lazy { PrimaryKey(*columns.filter { it.isPrimary }.toTypedArray()) }

    abstract fun createDataClass(values: List<*>): D

    @Suppress("UNCHECKED_CAST")
    fun toEqExpressions(data: D): List<EqExpression<*>> =
        columnsToValues.map { (it.key as Column<Any?>) eq it.value.get(data) }

    fun toExpression(data: D): Expression {
        val eqs = toEqExpressions(data).toMutableList()
        val first = eqs.removeAt(0)
        var andExp: AndExpression? = null
        for(eq in eqs) {
            andExp = if(andExp == null) first and eq else andExp and eq
        }
        return andExp!!
    }
}