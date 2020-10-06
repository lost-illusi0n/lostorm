package net.lostillusion.lostorm.mapper

import net.lostillusion.lostorm.mapper.operations.Operation

/**
 * A class representing a SQL transaction.
 *
 * @param O The result of the operation.
 * @property operation The operation that will be executed by this transaction.
 * @property session The session which this transacation will be executed on.
 * @constructor Creates a transaction with the specified [operation] and [session].
 */
class Transaction<O>(
    private val operation: Operation<*, O>,
    private val session: Session
) {
    /**
     * Commits the [operation] to the [session]'s database.
     *
     * @return The result of the operation.
     */
    fun commit() = session.connection { operation.executor.execute(operation, this) }
}