package net.lostillusion.lostorm.mapper

import net.lostillusion.lostorm.mapper.operations.Operation

//NOTE: Can only support one operation at a time
class Transaction<O>(
    private val operation: Operation<*, O>,
    private val session: Session
) {
    fun commit() = session.connection { operation.executor.execute(operation, this) }
}