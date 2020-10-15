package net.lostillusion.lostorm.mapper

import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

object SchemaUtils {
    @Suppress("NestedLambdaShadowedImplicitParameter")
    fun create(session: Session, vararg entities: Entity<*>) {
        session.connection {
            entities.forEach {
                val sql =
                    """create table if not exists ${it.tableName}
                        | (${
                    it.columns.joinToString(", ") {
                        """${it.columnName}
                                        | ${DataTypes.kotlinToSQL[it.sqlClass]}
                                        |${if (it.nullable) "" else " not null"}
                                        |${if (it.hasDefaultValue) " default ${it.defaultValue}" else ""}
                                    """.trimMargin()
                    }
                    }
                        |${if (it.primaryKey.asSQL.isNotEmpty()) ", ${it.primaryKey.asSQL}" else ""})
                    """.trimMargin()
                LOGGER.debug { "Executing call: $sql" }
                prepareCall(sql).execute()
            }
        }
    }
}
