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
    internal val value: V?
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

infix fun <V: Any> Column<V, *>.eq(value: V?) =
    EqExpression(this, value)

infix fun <V: Any> Column<V, *>.neq(value: V?) =
    NeqExpression(this, value)

infix fun Expression.and(other: Expression) = AndExpression(this, other)