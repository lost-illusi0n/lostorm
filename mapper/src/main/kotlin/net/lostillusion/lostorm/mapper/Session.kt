package net.lostillusion.lostorm.mapper

import net.lostillusion.lostorm.mapper.operations.Operation
import java.sql.Connection
import java.sql.DriverManager

/**
 * Represents a session to the database.
 *
 * @param host The database's JDBC Url.
 * @param user The username of the database account.
 * @param pass The password of the database account.
 * @param driver The PostgreSQL driver (should usually not be changed).
 * @constructor Creates a session to the specified [host].
 */
class Session(private val host: String, private val user: String, private val pass: String?, driver: String = "org.postgresql.Driver") {
    init {
        Class.forName(driver)
    }

    /**
     * Creates a connection to the database.
     *
     * @param action The action to do with a connection.
     * @return The result of the [action].
     */
    fun <T> connection(action: Connection.() -> T) =
        DriverManager.getConnection(host, user, pass).use(action)

    /**
     * Applies the [function] to the current session.
     *
     * @param function The function to be applied to the current session.
     * @return The result of the [function].
     */
    operator fun <T> invoke(function: Session.() -> T) = function()
}

/**
 * Creates a transaction of the [operation] with this [Session].
 *
 * @param operation The [Operation] to be executed.
 * @return The result of the [Operation].
 */
infix fun <R> Session.transaction(operation: () -> Operation<*, R>) = Transaction(operation(), this).commit()
