package net.lostillusion.lostorm.mapper

import mu.KotlinLogging

private val LOGGER = KotlinLogging.logger {}

object SchemaUtils {
    fun create(session: Session, vararg entities: Entity<*>) {
        session.connection {
            entities.forEach { entity ->
                val sql = "create table if not exists ${entity.tableName} (" +
                    //add column information
                    entity.columns.joinToString(", ") { column ->
                        ("${column.columnName} ${DataTypes.kotlinToSQL[column.sqlClass]}" +             //column_name column_type
                            (if(!column.nullable) " not null " else "") +                               //not null
                            (if(column.hasDefaultValue) " default ${column.defaultValue} " else "")     //default value
                                ).trim()
                    } +
                    //primary key information
                    (if(entity.primaryKey.asSQL.isNotEmpty()) ", ${entity.primaryKey.asSQL}" else "") + //primary keys
                    ")"
                LOGGER.debug { "Executing call: $sql" }
                prepareCall(sql).execute()
            }
        }
    }
}
