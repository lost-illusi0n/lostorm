package net.lostillusion.lostorm.mapper

object SchemaUtils {
    @Suppress("NestedLambdaShadowedImplicitParameter")
    fun create(session: Session, vararg entities: Entity<*>) {
        session.connection {
            entities.forEach {
                val sql =
                    "create table if not exists"  +
                    " ${it.tableName}" +
                    "(" +
                    it.columns.joinToString(", ") {
                        "${it.columnName}" +
                        " ${DataTypes.kotlinToSQL[it.valueClass]}" +
                        "${if (!it.nullable) " not null" else ""}" +
                        if (it.hasDefaultValue) " default ${toSafeSQL(it.defaultValue)}" else ""
                    } +
                    (if(it.primaryKey.asSQL.isNotEmpty()) ", ${it.primaryKey.asSQL}" else "") +
                    ")"
                println("Executing call: $sql")
                prepareCall(sql).execute()
            }
        }
    }
}