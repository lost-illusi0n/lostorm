@file:Suppress("MemberVisibilityCanBePrivate")

package net.lostillusion.lostorm.mapper.operations

import net.lostillusion.lostorm.mapper.*
import net.lostillusion.lostorm.mapper.toSafeSQL

interface StatementGenerator {
    fun generateStatement(): String
}

sealed class Operation<D: Any, R>(
    val entity: Entity<D>,
    val executor: OperationExecutor<R>
): StatementGenerator {
    abstract class SelectCountOperation<D: Any>(entity: Entity<D>) : Operation<D, Int>(entity, SelectCountExecutor)

    abstract class SelectOperation<D: Any>(entity: Entity<D>) : Operation<D, List<D>>(entity, SelectExectutor(entity))

    abstract class UpdateOperation<D: Any>(entity: Entity<D>) : Operation<D, Int>(entity, UpdateExectuor)

    override fun toString() = generateStatement()
}

//Marker for Select OPs
interface GenericSelect

class SelectOp<D: Any>(entity: Entity<D>): Operation.SelectOperation<D>(entity), GenericSelect {
    override fun generateStatement(): String = "select * from ${entity.tableName}"
}

class SelectCountOp<D: Any>(
    entity: Entity<D>,
    private val column: Column<*, *>? = null
): Operation.SelectCountOperation<D>(entity), GenericSelect {
    override fun generateStatement(): String = "select count(${column?.columnName ?: "*"}) from ${entity.tableName}"
}

class SelectWhereOp<D: Any, R>(
    private val selectOp: Operation<D, R>,
    private val expression: Expression
): Operation<D, R>(selectOp.entity, selectOp.executor) {
    override fun generateStatement(): String = "${selectOp.generateStatement()} where ${expression.generateExpression()}"
}

class InsertOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "insert into ${entity.tableName}(${entity.columns.map(Column<*, *>::columnName).joinToString(", ")})"
}

class InsertValuesOp<D: Any>(
    private val insertOp: InsertOp<D>,
    private val data: D
): Operation.UpdateOperation<D>(insertOp.entity) {
    override fun generateStatement(): String = "${insertOp.generateStatement()} values(${entity.columnsToValues.map { it.value.get(data)?.let(it.key.valueConverter::convertToSql) }.joinToString(", ", transform = ::toSafeSQL)})"
}

class InsertOnConflictOp<D: Any>(
    private val insertOp: InsertValuesOp<D>,
    private vararg val conflictingColumns: Column<*, *>,
    private val action: ConflictingUpdateSetOp<D>?
): Operation.UpdateOperation<D>(insertOp.entity) {
    override fun generateStatement(): String = "${insertOp.generateStatement()} on conflict (${conflictingColumns.joinToString(", ", transform = Column<*, *>::columnName)}) do ${action?.generateStatement() ?: "nothing"}"
}

class UpdateOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "update ${entity.tableName}"
}

class UpdateSetOp<D: Any>(
    private val updateOp: UpdateOp<D>,
    private val eqs: List<EqExpression<*>>
): Operation.UpdateOperation<D>(updateOp.entity) {
    override fun generateStatement(): String =
        "${updateOp.generateStatement()} set ${eqs.joinToString(", ", transform = EqExpression<*>::generateExpression)}"
}

class UpdateSetWhereOp<D: Any>(
    private val updateOp: UpdateSetOp<D>,
    private val expression: Expression
): Operation.UpdateOperation<D>(updateOp.entity) {
    override fun generateStatement(): String = "${updateOp.generateStatement()} where ${expression.generateExpression()}"
}

class ConflictingUpdateOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "update"
}

class ConflictingUpdateSetOp<D: Any>(
    private val updateOp: ConflictingUpdateOp<D>,
    private val eqs: List<EqExpression<*>>
): Operation.UpdateOperation<D>(updateOp.entity) {
    override fun generateStatement(): String = "${updateOp.generateStatement()} set ${eqs.joinToString(", ", transform = EqExpression<*>::generateExpression)}"
}

class DeleteOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "delete from ${entity.tableName}"
}

class DeleteWhereOp<D: Any>(
    private val deleteOp: DeleteOp<D>,
    private val expression: Expression
): Operation.UpdateOperation<D>(deleteOp.entity) {
    override fun generateStatement(): String = "${deleteOp.generateStatement()} where ${expression.generateExpression()}"
}

class ConflictContext<D: Any>(private val entity: Entity<D>) {
    fun update() = ConflictingUpdateOp(entity)
    fun nothing(): ConflictingUpdateSetOp<D>? = null
}

fun <D: Any> select(entity: Entity<D>) = SelectOp(entity)

fun <D: Any> SelectOp<D>.count() = SelectCountOp(entity, null)

infix fun <D: Any> SelectOp<D>.count(column: Column<*, *>) = SelectCountOp(entity, column)

infix fun <D: Any, R, S> S.where(condition: Expression) where S: GenericSelect, S: Operation<D, R> = SelectWhereOp(this, condition)

fun <D: Any> insert(entity: Entity<D>) = InsertOp(entity)

infix fun <D: Any> InsertOp<D>.values(data: D) = InsertValuesOp(this, data)

fun <D: Any> InsertValuesOp<D>.onConflict(vararg conflictingColumns: Column<*, *>, action: ConflictingUpdateSetOp<D>) = InsertOnConflictOp(this, *conflictingColumns, action = action)

fun <D: Any> InsertValuesOp<D>.onConflict(vararg conflictingColumns: Column<*, *>, action: ConflictContext<D>.() -> ConflictingUpdateSetOp<D>?) = InsertOnConflictOp(this, *conflictingColumns, action = action(ConflictContext(entity)))

fun <D: Any> InsertValuesOp<D>.onConflictDoNothing(vararg conflictingColumns: Column<*, *>) = InsertOnConflictOp(this, *conflictingColumns, action = null)

fun <D: Any> update(entity: Entity<D>) = UpdateOp(entity)

infix fun <D: Any> UpdateOp<D>.set(data: D) = UpdateSetOp(this, entity.toEqExpressions(data))

fun <D: Any> UpdateOp<D>.set(vararg expressions: EqExpression<*>) = UpdateSetOp(this, expressions.toList())

infix fun <D: Any> UpdateSetOp<D>.where(condition: Expression) = UpdateSetWhereOp(this, condition)

fun <D: Any> conflictingUpdate(entity: Entity<D>) = ConflictingUpdateOp(entity)

infix fun <D: Any> ConflictingUpdateOp<D>.set(data: D) = ConflictingUpdateSetOp(this, entity.toEqExpressions(data))

fun <D: Any> ConflictingUpdateOp<D>.set(vararg expressions: EqExpression<*>) = ConflictingUpdateSetOp(this, expressions.toList())

fun <D: Any> delete(entity: Entity<D>) = DeleteOp(entity)

fun <D: Any> DeleteOp<D>.where(condition: Expression) = DeleteWhereOp(this, condition)

fun <D: Any> DeleteOp<D>.where(data: D) = DeleteWhereOp(this, entity.toExpression(data))
