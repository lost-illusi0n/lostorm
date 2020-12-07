package net.lostillusion.lostorm.mapper.operations

import net.lostillusion.lostorm.mapper.*

/**
 * A generic interface to represent SQL statements.
 */
interface Statement {
    /**
     * Generates an SQL statement that represents this [Statement].
     *
     * @return the generated SQL statement.
     */
    fun generateStatement(): String
}

/**
 * Base for all supported [Operation]s
 *
 * @param entity The [Entity] that the operation will be executed against.
 * @param executor The [OperationExecutor] which will execute the operation.
 */
sealed class Operation<D: Any, R>(
    val entity: Entity<D>,
    val executor: OperationExecutor<R>
): Statement {
    /**
     * The abstract [Operation] for any operation which is a Select statement and returns the Count.
     *
     * @param entity The entity that the operation will be executed against.
     */
    abstract class SelectCountOperation<D: Any>(entity: Entity<D>) : Operation<D, Int>(entity, SelectCountExecutor)

    /**
     * The abstract [Operation] for any operation which is a Select statement and returns data.
     *
     * @param entity The entity that the operation will be executed against.
     */
    abstract class SelectOperation<D: Any>(entity: Entity<D>) : Operation<D, List<D>>(entity, SelectExectutor(entity))

    /**
     * The abstract [Operation] for any operation which is a Update or Insert statement and returns the rows updated.
     *
     * @param entity The entity that the operation will be executed against.
     */
    abstract class UpdateOperation<D: Any>(entity: Entity<D>) : Operation<D, Int>(entity, UpdateExectuor)

    /**
     * @return The SQL statement as a [String] which represents this [Operation].
     */
    override fun toString() = generateStatement()
}

/**
 * A generic interface for Select operations
 */
interface GenericSelect

/**
 * An SQL select statement equivalent.
 *
 * @param entity the Table this operation will target.
 */
class SelectOp<D: Any>(entity: Entity<D>): Operation.SelectOperation<D>(entity), GenericSelect {
    override fun generateStatement(): String = "select * from ${entity.tableName}"
}

/**
 * An SQL ``select count`` statement equivalent.
 *
 * @param entity The [Entity] that this [SelectCountOp] will be executed against.
 * @param column The [Column] the count will be operated on, or all the [Column]s of this [entity] if none is explicitly specified.
 */
class SelectCountOp<D: Any>(
    entity: Entity<D>,
    private val column: Column<*, *>? = null
): Operation.SelectCountOperation<D>(entity), GenericSelect {
    override fun generateStatement(): String = "select count(${column?.columnName ?: "*"}) from ${entity.tableName}"
}

/**
 * An SQL ``select where`` statement equivalent.
 *
 * @param selectOp The original Select statement which the Where statement will be added onto.
 * @param expression The Where condition.
 */
class SelectWhereOp<D: Any, R>(
    private val selectOp: Operation<D, R>,
    private val expression: Expression
): Operation<D, R>(selectOp.entity, selectOp.executor) {
    override fun generateStatement(): String = "${selectOp.generateStatement()} where ${expression.generateExpression()}"
}

/**
 * An SQL ``select limit`` statement equivalent.
 *
 * @param selectOp the original Select statement which the Where statement will be added onto.
 * @param limit the limit condition.
 */
class SelectLimitOp<D: Any, R>(
    private val selectOp: Operation<D, R>,
    private val limit: Int
): Operation<D, R>(selectOp.entity, selectOp.executor) {
    override fun generateStatement(): String = "${selectOp.generateStatement()} limit $limit"
}

/**
 * An SQL ``select offset`` statement equivalent.
 * @param selectOp the original Select operation which the offset clause will be added onto.
 * @param offset the offset condition.
 */
class SelectOffsetOp<D: Any, R>(
    private val selectOp: Operation<D, R>,
    private val offset: Int
): Operation<D, R>(selectOp.entity, selectOp.executor) {
    override fun generateStatement(): String = "${selectOp.generateStatement()} offset $offset"
}

/**
 * An SQL ``select order by`` clause equivalent.
 *
 * @param selectOp The original [SelectOp] which the order by clause will be added onto.
 * @param expressions the sort expressions.
 */
class SelectOrderByOp<D: Any, R>(
    private val selectOp: Operation<D, R>,
    private vararg val expressions: OrderByExpression
): Operation<D, R>(selectOp.entity, selectOp.executor) {
    override fun generateStatement(): String = "${selectOp.generateStatement()} order by ${expressions.joinToString(", ") { it.generateExpression() }}"
}

/**
 * An SQL ``insert into`` statement equivalent.
 *
 * @param entity the Table this operation will target.
 */
class InsertOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "insert into ${entity.tableName}(${entity.columns.map(Column<*, *>::columnName).joinToString(", ")})"
}

/**
 * An SQL ``insert into values()`` statement equivalent.
 *
 * @param insertOp The original [InsertOp] which the Values statement will be added onto.
 * @param inserts The values that will be inserted.
 */
class InsertValuesOp<D: Any>(
    private val insertOp: InsertOp<D>,
    private vararg val inserts: EqExpression<*>
): Operation.UpdateOperation<D>(insertOp.entity) {
    override fun generateStatement(): String = "${insertOp.generateStatement()} values(${entity.columns.joinToString(", ") { toSafeSQL(inserts.mapNotNull { ins -> if(ins.column == it) it.valueConverter.orConvertToSql(ins.value) else null }.firstOrNull()) }})"
}

/**
 * An SQL ``insert into values() on conflict action`` statement equivalent.
 *
 * @param insertValuesOp The [InsertValuesOp] which the Conflict statement will be added onto.
 * @param conflictingColumns The SQL ``conflict_target``.
 * @param action The SQL ``conflict_action``.
 */
class InsertOnConflictOp<D: Any>(
    private val insertValuesOp: InsertValuesOp<D>,
    private vararg val conflictingColumns: Column<*, *>,
    private val action: ConflictingUpdateSetOp<D>?
): Operation.UpdateOperation<D>(insertValuesOp.entity) {
    override fun generateStatement(): String = "${insertValuesOp.generateStatement()} on conflict (${conflictingColumns.joinToString(", ", transform = Column<*, *>::columnName)}) do ${action?.generateStatement() ?: "nothing"}"
}

/**
 * An SQL ``update`` statement equivalent.
 *
 * @param entity the Table this operation will target.
 */
class UpdateOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "update ${entity.tableName}"
}

/**
 * An SQL ``update set`` statement equivalent.
 *
 * @param updateOp The original [UpdateOp] which the Set statement will be appended onto.
 * @param eqs The [EqExpression]s which represent the values of the Set operation.
 */
class UpdateSetOp<D: Any>(
    private val updateOp: UpdateOp<D>,
    private val eqs: List<EqExpression<*>>
): Operation.UpdateOperation<D>(updateOp.entity) {
    override fun generateStatement(): String =
        "${updateOp.generateStatement()} set ${eqs.joinToString(", ", transform = EqExpression<*>::generateExpression)}"
}

/**
 * An SQL ``update set where`` statement equivalent.
 *
 * @param updateSetOp The original [UpdateSetOp] which the Where statement will be appended onto.
 * @param expression The Where condition.
 */
class UpdateSetWhereOp<D: Any>(
    private val updateSetOp: UpdateSetOp<D>,
    private val expression: Expression
): Operation.UpdateOperation<D>(updateSetOp.entity) {
    override fun generateStatement(): String = "${updateSetOp.generateStatement()} where ${expression.generateExpression()}"
}

/**
 * Represents the Update part of ``conflict_action`` SQL statements.
 *
 * @param entity the Table this operation will target.
 */
class ConflictingUpdateOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "update"
}

/**
 * Represents the Update Set part of ``conflict_action`` SQL statements.
 *
 * @param conflictingUpdateOp The original [ConflictingUpdateOp] which the Set statement will be appended onto.
 * @param eqs The [EqExpression]s which represent the values of the Set statement.
 */
class ConflictingUpdateSetOp<D: Any>(
    private val conflictingUpdateOp: ConflictingUpdateOp<D>,
    private val eqs: List<EqExpression<*>>
): Operation.UpdateOperation<D>(conflictingUpdateOp.entity) {
    override fun generateStatement(): String = "${conflictingUpdateOp.generateStatement()} set ${eqs.joinToString(", ", transform = EqExpression<*>::generateExpression)}"
}

/**
 * An SQL ``delete from`` statement equivalent.
 *
 * @param entity the Table this operation will target.
 */
class DeleteOp<D: Any>(entity: Entity<D>): Operation.UpdateOperation<D>(entity) {
    override fun generateStatement(): String = "delete from ${entity.tableName}"
}

/**
 * An SQL ``delete from where`` statement equivalent.
 *
 * @param deleteOp The original [DeleteOp] which the Where statement will be appended onto.
 * @param expression The Where condition.
 */
class DeleteWhereOp<D: Any>(
    private val deleteOp: DeleteOp<D>,
    private val expression: Expression
): Operation.UpdateOperation<D>(deleteOp.entity) {
    override fun generateStatement(): String = "${deleteOp.generateStatement()} where ${expression.generateExpression()}"
}

/**
 * A helper class to better write out ``conflict_action``s.
 *
 * @param entity the Table this operation will target.
 */
class ConflictContext<D: Any>(private val entity: Entity<D>) {
    fun update() = ConflictingUpdateOp(entity)
    fun nothing(): ConflictingUpdateSetOp<D>? = null
}

/**
 * Creates a [SelectOp] query.
 *
 * @param entity the Table this operation will target.
 * @return the [SelectOp].
 */
fun <D: Any> select(entity: Entity<D>) = SelectOp(entity)

/**
 * Creates a [SelectCountOp] query.
 * This will return the count of the query.
 *
 * @return the [SelectCountOp].
 */
fun <D: Any> SelectOp<D>.count() = SelectCountOp(entity, null)

/**
 * Creates a [SelectCountOp] query.
 * This will return the count of the specified [column].
 *
 * @param column the [Column] for the Count part of this query.
 * @return the [SelectCountOp].
 */
infix fun <D: Any> SelectOp<D>.count(column: Column<*, *>) = SelectCountOp(entity, column)

/**
 * Creates a [SelectLimitOp] query.
 * This will return the query limited by the [limit].
 *
 * @param limit the limit part of this query.
 * @return the [SelectLimitOp].
 */
infix fun <D: Any> SelectOp<D>.limit(limit: Int) = SelectLimitOp(this, limit)

/**
 * Creates a [SelectLimitOp] query.
 * This will return the query limited by the [limit].
 *
 * @param limit the limit part of this query.
 * @return the [SelectLimitOp].
 */
infix fun <D: Any, R> SelectWhereOp<D, R>.limit(limit: Int) = SelectLimitOp(this, limit)

/**
 * Creates a [SelectLimitOp] query.
 * This will return the query limited to the [limit].
 *
 * @param limit the limit part of this query.
 * @return the [SelectLimitOp].
 */
infix fun <D: Any, R> SelectOrderByOp<D, R>.limit(limit: Int) = SelectLimitOp(this, limit)

/**
 * Creates a [SelectOffsetOp] query.
 * This will return the query offset by the [offset].
 *
 * @param offset the offset parameter.
 * @return the [SelectOffsetOp].
 */
infix fun <D: Any, R> Operation<D, R>.offset(offset: Int) = SelectOffsetOp(this, offset)

/**
 * Adds an order by clause to this statement.
 * This will return the query ordered by the [orderByExpressions].
 *
 * @param orderByExpressions the order by expressions.
 * @return the [SelectOrderByOp].
 */
fun <D: Any> SelectOp<D>.orderBy(vararg orderByExpressions: OrderByExpression) = SelectOrderByOp(this, *orderByExpressions)

/**
 * Adds an order by clause to this statement.
 * This will return the query ordered by the [orderByExpressions].
 *
 * @param orderByExpressions the order by expressions.
 * @return the [SelectOrderByOp].
 */
fun <D: Any, R> SelectWhereOp<D, R>.orderBy(vararg orderByExpressions: OrderByExpression) = SelectOrderByOp(this, *orderByExpressions)

/**
 * Adds an order by clause to this statement.
 * This will return the query ordered by the [orderByExpressions].
 *
 * @param orderByExpressions the order by expressions.
 * @return the [SelectOrderByOp].
 */
fun <D: Any, R> SelectLimitOp<D, R>.orderBy(vararg orderByExpressions: OrderByExpression) = SelectOrderByOp(this, *orderByExpressions)

/**
 * Creates a [SelectWhereOp] query.
 * This will specify the Where condition.
 *
 * @param condition the Where condition of this query.
 * @return the [SelectWhereOp].
 */
infix fun <D: Any, R, S> S.where(condition: Expression) where S: GenericSelect, S: Operation<D, R> = SelectWhereOp(this, condition)

/**
 * Creates a [SelectWhereOp] query.
 * This will specify the Where condition.
 *
 * @param condition the Where condition of this query.
 * @return the [SelectWhereOp].
 */
infix fun <D: Any, R> SelectLimitOp<D, R>.where(condition: Expression) = SelectWhereOp(this, condition)

/**
 * Creates a [SelectWhereOp] query.
 * This will specify the Where condition.
 *
 * @param condition the Where condition of this query.
 * @return the [SelectWhereOp].
 */
infix fun <D: Any, R> SelectOrderByOp<D, R>.where(condition: Expression) = SelectWhereOp(this, condition)

/**
 * Creates a [InsertOp] update.
 *
 * @param entity the Table this operation will target.
 * @return the [InsertOp].
 */
fun <D: Any> insert(entity: Entity<D>) = InsertOp(entity)

/**
 * Creates a [InsertValuesOp].
 * This will specify the values expression.
 *
 * @param data the data to be inserted.
 * @return the [InsertValuesOp].
 */
infix fun <D: Any> InsertOp<D>.values(data: D) = InsertValuesOp(this, *entity.toEqExpressions(data).toTypedArray())

/**
 * Creates a [InsertValuesOp].
 * This will specify the values expression.
 *
 * @param inserts the values to be inserted.
 * @return the [InsertValuesOp].
 */
fun <D: Any> InsertOp<D>.values(vararg inserts: EqExpression<*>) = InsertValuesOp(this, *inserts)

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict`` statement.
 *
 * @param conflictingColumns the columns to be apart of ``conflict_target``.
 * @param action the [ConflictingUpdateSetOp] to be apart of ``conflict_action``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflictDoUpdate(vararg conflictingColumns: Column<*, *>, action: ConflictingUpdateSetOp<D>) = InsertOnConflictOp(this, *conflictingColumns, action = action)

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict`` statement.
 *
 * @param conflictingColumns the columns to be apart of ``conflict_target``.
 * @param action the [ConflictingUpdateSetOp] to be apart of ``conflict_action``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflict(vararg conflictingColumns: Column<*, *>, action: ConflictContext<D>.() -> ConflictingUpdateSetOp<D>?) = InsertOnConflictOp(this, *conflictingColumns, action = action(ConflictContext(entity)))

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict`` clause.
 *
 * @param primaryKey the ``conflict_target``.
 * @param action the ``conflict_action``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflict(primaryKey: PrimaryKey, action: ConflictContext<D>.() -> ConflictingUpdateSetOp<D>?) = InsertOnConflictOp(this, *primaryKey.columns, action = action(ConflictContext(entity)))

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict`` clause.
 *
 * @param primaryKey the ``conflict_target``.
 * @param action the [ConflictingUpdateSetOp] which is the ``conflict_action``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflictDoUpdate(primaryKey: PrimaryKey, action: ConflictingUpdateSetOp<D>) = InsertOnConflictOp(this, *primaryKey.columns, action = action)

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict`` clause.
 *
 * @param primaryKey the ``conflict_target``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflictDoNothing(primaryKey: PrimaryKey) = InsertOnConflictOp(this, *primaryKey.columns, action = null)

/**
 * Creates a [InsertOnConflictOp].
 * This will start an ``on conflict do nothing`` statement.
 *
 * @param conflictingColumns the columns to be apart of ``conflict_target``.
 * @return the [InsertOnConflictOp].
 */
fun <D: Any> InsertValuesOp<D>.onConflictDoNothing(vararg conflictingColumns: Column<*, *>) = InsertOnConflictOp(this, *conflictingColumns, action = null)

/**
 * Creates a [UpdateOp].
 *
 * @param entity the Table this operation will target.
 * @return the [UpdateOp].
 */
fun <D: Any> update(entity: Entity<D>) = UpdateOp(entity)

/**
 * Creates a [UpdateSetOp].
 * This will specify the values expression.
 *
 * @param data the data to be used as values.
 * @return the [UpdateSetOp].
 */
infix fun <D: Any> UpdateOp<D>.set(data: D) = UpdateSetOp(this, entity.toEqExpressions(data))

/**
 * Creates a [UpdateSetOp]
 * This will specify the values expression.
 *
 * @param expressions the expressions that will be used as values.
 * @return the [UpdateSetOp].
 */
fun <D: Any> UpdateOp<D>.set(vararg expressions: EqExpression<*>) = UpdateSetOp(this, expressions.toList())

/**
 * Creates a [UpdateSetWhereOp].
 * This will specify the Where condition.
 *
 * @param condition the Where condition.
 * @return the [UpdateSetWhereOp].
 */
infix fun <D: Any> UpdateSetOp<D>.where(condition: Expression) = UpdateSetWhereOp(this, condition)

/**
 * Creates a [ConflictingUpdateOp].
 * This will start a ``on conflict`` statement.
 *
 * @param entity the Table this operation will target.
 * @return the [ConflictingUpdateOp].
 */
fun <D: Any> conflictingUpdate(entity: Entity<D>) = ConflictingUpdateOp(entity)

/**
 * Creates a [ConflictingUpdateSetOp].
 * This will specify the Update values.
 *
 * @param data the data to be updated.
 * @return the [ConflictingUpdateSetOp].
 */
infix fun <D: Any> ConflictingUpdateOp<D>.set(data: D) = ConflictingUpdateSetOp(this, entity.toEqExpressions(data))

/**
 * Creates a [ConflictingUpdateSetOp].
 * This will specify the Update values.
 *
 * @param expressions the expressions that will be used as values.
 * @return the [ConflictingUpdateSetOp].
 */
fun <D: Any> ConflictingUpdateOp<D>.set(vararg expressions: EqExpression<*>) = ConflictingUpdateSetOp(this, expressions.toList())

/**
 * Creates a [DeleteOp].
 *
 * @param entity the Table this operation will target.
 * @return the [DeleteOp].
 */
fun <D: Any> delete(entity: Entity<D>) = DeleteOp(entity)

/**
 * Creates a [DeleteWhereOp].
 * This will specify the Where condition.
 *
 * @param condition the Where condition.
 * @return the [DeleteWhereOp].
 */
fun <D: Any> DeleteOp<D>.where(condition: Expression) = DeleteWhereOp(this, condition)

/**
 * Creates a [DeleteWhereOp].
 * This will specify the Where condition.
 *
 * @param data the data to be used as the Where condition.
 * @return the [DeleteWhereOp].
 */
fun <D: Any> DeleteOp<D>.where(data: D) = DeleteWhereOp(this, entity.toExpression(data))
