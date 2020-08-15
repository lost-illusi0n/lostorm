package net.lostillusion.lostorm.mapper.operations

import net.lostillusion.lostorm.mapper.Entity
import java.lang.RuntimeException
import java.sql.Connection
import java.sql.SQLException

interface OperationExecutor<R> {
    fun execute(generator: StatementGenerator, connection: Connection): R
}

object SelectCountExecutor: OperationExecutor<Int> {
    override fun execute(generator: StatementGenerator, connection: Connection): Int {
        println("Executing query: ${generator.generateStatement()}")
        val result = connection.prepareStatement(generator.generateStatement()).executeQuery()
        result.next()
        return result.getInt(1)
    }
}

class SelectExectutor<D: Any>(private val entity: Entity<D>): OperationExecutor<List<D>> {
    override fun execute(generator: StatementGenerator, connection: Connection): List<D> {
        println("Executing query: ${generator.generateStatement()}")
        val result = connection.prepareStatement(generator.generateStatement()).executeQuery()
        val values = mutableListOf<MutableList<Any?>>()
        while(result.next()) {
            val currentResult = mutableListOf<Any?>()
            entity.columns.forEach {
                try {
                    currentResult += result.getObject(it.columnName)
                } catch(e: SQLException) {
                    if(it.nullable) currentResult.add(null)
                    //TODO: Make this a unique exception
                    else throw RuntimeException("No value found for non-nullable column: ${it.columnName}")
                }
            }
            values += currentResult
        }
        return values.map(entity::createDataClass)
    }
}

object UpdateExectuor: OperationExecutor<Int> {
    override fun execute(generator: StatementGenerator, connection: Connection): Int {
        println("Executing update: ${generator.generateStatement()}")
        return connection.prepareStatement(generator.generateStatement()).executeUpdate()
    }
}
