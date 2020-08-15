package net.lostillusion.lostorm.mapper

interface Expression {
    fun generateExpression(): String
}

class EqExpression<V>(
    private val column: Column<V>,
    private val value: V
): Expression {
    override fun generateExpression() = "${column.columnName} = ${toSafeSQL(value)}"
}

class NeqExpression<V>(
    private val column: Column<V>,
    private val value: V
): Expression {
    override fun generateExpression(): String = "${column.columnName} != $value"
}

class AndExpression(
    private val first: Expression,
    private val second: Expression
): Expression {
    override fun generateExpression(): String = "${first.generateExpression()} and ${second.generateExpression()}"
}

infix fun <V> Column<V>.eq(value: V) =
    EqExpression(this, value)

infix fun <V> Column<V>.neq(value: V) =
    NeqExpression(this, value)

infix fun Expression.and(other: Expression) = AndExpression(this, other)