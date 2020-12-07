package net.lostillusion.lostorm.mapper

/**
 * A generic interface to represent SQL expressions.
 */
interface Expression {
    /**
     * Generates an SQL expression that represents this [Expression].
     *
     * @return the generated SQL expression.
     */
    fun generateExpression(): String
}

/**
 * An SQL expression that represents an equals expression.
 * Interchangable for equality checks and to set values.
 *
 * @param V The value type.
 * @param column The [Column] which is the key part of the equal expression.
 * @param value the [V] which is the value part of the equal expression.
 */
class EqExpression<V: Any>(
    internal val column: Column<V, *>,
    internal val value: V?
): Expression {
    override fun generateExpression() = "${column.columnName} = ${toSafeSQL(column.valueConverter.orConvertToSql(value))}"
}

/**
 * An SQL expression that represents an inequality expression.
 *
 * @param V The value type.
 * @param column The [Column] which is the key part of the inequality expression.
 * @param value the [V] which is the value part of the inequality expression.
 */
class NeqExpression<V: Any>(
    internal val column: Column<V, *>,
    private val value: V?
): Expression {
    override fun generateExpression(): String = "${column.columnName} != ${toSafeSQL(column.valueConverter.orConvertToSql(value))}"
}

/**
 * An SQL expression that represents a And operation of two different expression.
 *
 * @param first The first [Expression] to be part of the And expression.
 * @param second The second [Expression] to be part of the And expression.
 */
class AndExpression(
    private val first: Expression,
    private val second: Expression
): Expression {
    override fun generateExpression(): String = "${first.generateExpression()} and ${second.generateExpression()}"
}

class OrderByExpression(
    private val column: Column<*, *>,
    private val orderBy: OrderBy,
    private val nullsOrder: NullsOrder?
): Expression {
    override fun generateExpression(): String = "${column.columnName} ${orderBy.sql} ${nullsOrder?.sql ?: ""}".trim()
}

class ValueInExpression<V: Any>(
    private val column: Column<V, *>,
    private vararg val values: V
): Expression {
    override fun generateExpression(): String = "${column.columnName} in (${values.map(column.valueConverter::convertToSql).joinToString(", ", transform = ::toSafeSQL)})"
}

enum class OrderBy(internal val sql: String) {
    ASCENDING("asc"),
    DESCENDING("desc")
}

enum class NullsOrder(internal val sql: String) {
    NULLS_FIRST("nulls first"),
    NULLS_LAST("nulls last")
}

infix fun <V: Any> Column<V, *>.eq(value: V?) =
    EqExpression(this, value)

infix fun <V: Any> Column<V, *>.neq(value: V?) =
    NeqExpression(this, value)

infix fun Expression.and(other: Expression) = AndExpression(this, other)

fun Column<*, *>.ascending(nullsOrder: NullsOrder? = null)  = OrderByExpression(this, OrderBy.ASCENDING, nullsOrder)
fun Column<*, *>.descending(nullsOrder: NullsOrder? = null) = OrderByExpression(this, OrderBy.DESCENDING, nullsOrder)

fun <V: Any> Column<V, *>.valueIn(vararg values: V) = ValueInExpression(this, *values)
infix inline fun <reified V: Any> Column<V, *>.valueIn(values: List<V>) = ValueInExpression(this, *values.toTypedArray())