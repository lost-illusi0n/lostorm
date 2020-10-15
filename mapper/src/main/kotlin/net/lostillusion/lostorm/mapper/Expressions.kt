package net.lostillusion.lostorm.mapper

interface Expression {
    fun generateExpression(): String
}

class EqExpression<V: Any>(
    internal val column: Column<V, *>,
    internal val value: V?
): Expression {
    override fun generateExpression() = "${column.columnName} = ${toSafeSQL(column.valueConverter.orConvertToSql(value))}"
}

class NeqExpression<V: Any>(
    internal val column: Column<V, *>,
    internal val value: V?
): Expression {
    override fun generateExpression(): String = "${column.columnName} != ${toSafeSQL(column.valueConverter.orConvertToSql(value))}"
}

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