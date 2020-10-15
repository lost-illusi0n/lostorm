package net.lostillusion.lostorm.mapper

import kotlin.reflect.KProperty1

/**
 * An Entity which represents a Lostorm wrapper for the [D] data class.
 *
 * @param D The data class which this Entity was generated from.
 * @property tableName The SQL table name.
 */
abstract class Entity<D: Any>(
    val tableName: String
) {
    /**
     * A list of all the [Column]s generated in this [Entity].
     */
    abstract val columns: List<Column<Any, Any>>

    /**
     * A [Map] that maps the [Entity]'s [Column]s to its corresponding [D] property.
     */
    abstract val columnsToValues: Map<Column<Any, Any>, KProperty1<D, *>>

    /**
     * The primary key for this Entity.
     */
    val primaryKey by lazy { PrimaryKey(*columns.filter { it.isPrimary }.toTypedArray()) }

    /**
     * Creates the original [D] with the [values].
     *
     * @param values The list of values for the [D].
     * @return A [D] from the [values].
     */
    abstract fun createDataClass(values: List<*>): D

    /**
     * Converts the [data] into a [List] of [EqExpression]s.
     *
     * @param data The [D] to be converted into a [List] of [EqExpression]
     * @return The [List] of [EqExpression] which represents the [data].
     */
    fun toEqExpressions(data: D): List<EqExpression<*>> =
        columnsToValues.map { it.key eq it.value.get(data) }

    /**
     * Converts the [data] into an [AndExpression] that maps all the values of the [data] into [EqExpression]s.
     *
     * @param data The [D] to be converted into an [Expression].
     * @return The expression which represents the [data].
     */
    fun toExpression(data: D): Expression {
        val eqs = toEqExpressions(data).toMutableList()
        val first = eqs.removeAt(0)
        var andExp: AndExpression? = null
        for(eq in eqs) andExp = if(andExp == null) first and eq else andExp and eq
        return andExp!!
    }
}