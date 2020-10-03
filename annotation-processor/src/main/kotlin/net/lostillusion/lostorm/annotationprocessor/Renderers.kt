package net.lostillusion.lostorm.annotationprocessor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.lostillusion.lostorm.mapper.Column
import net.lostillusion.lostorm.mapper.Entity
import javax.lang.model.element.Element
import kotlin.reflect.KProperty1

interface Renderer<T> {
    fun render(): T
}

class ColumnRenderer(
    property: Element,
    private val sqlType: TypeName,
    private val outputType: TypeName,
    private val initializer: String,
    private vararg val members: MemberName
) : Renderer<PropertySpec> {
    val name = property.simpleName.toString()

    override fun render(): PropertySpec = PropertySpec.builder(name, Column::class.asTypeName().parameterizedBy(sqlType, outputType))
        .initializer(initializer, *members)
        .mutable(false)
        .build()
}

class EntityRenderer(
    private val packageName: String,
    private val entityName: String,
    private val tableName: String,
    private val dataType: ClassName
) : Renderer<FileSpec> {
    private val columns = mutableListOf<ColumnRenderer>()

    fun addColumnRenderer(columnRenderer: ColumnRenderer) { columns.add(columnRenderer) }

    override fun render(): FileSpec {
        val file = FileSpec.builder(packageName, entityName)
        val entity = TypeSpec.objectBuilder(entityName)

        entity.superclass(Entity::class.asTypeName().parameterizedBy(dataType))
        entity.addSuperclassConstructorParameter(""""$tableName"""")

        val columnsBuilder = generateColumnsList()
        val columnsToValuesBuilder = generateColumnsToValuesMap()

        val columnsInitializer = mutableListOf<String>()
        val columnsToValuesInitializer = mutableListOf<String>()

        for(columnRenderer in columns) {
            columnsInitializer.add(columnRenderer.name)
            columnsToValuesInitializer.add("${columnRenderer.name} to ${dataType.simpleName}::${columnRenderer.name}")
            entity.addProperty(columnRenderer.render())
        }

        columnsBuilder.initializer("listOf(${columnsInitializer.joinToString(", ")}) as List<Column<Any, Any>>")
        columnsToValuesBuilder.initializer("mapOf(\n${columnsToValuesInitializer.joinToString(", \n")}\n) as Map<Column<Any, Any>, KProperty1<Human, *>>")

        entity.addFunction(generateCreateDataClass())
        entity.addProperty(columnsBuilder.build())
        entity.addProperty(columnsToValuesBuilder.build())

        file.addType(entity.build())
        return file.build()
    }

    private fun generateCreateDataClass() = FunSpec.builder("createDataClass")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("values", List::class.asTypeName().parameterizedBy(STAR))
        .addCode("return ${dataType.simpleName}::class.constructors.first().call(*values.toTypedArray())\n")
        .returns(dataType)
        .build()

    private fun generateColumnsToValuesMap() = PropertySpec
        .builder("columnsToValues", Map::class.asTypeName().parameterizedBy(
            Column::class.parameterizedBy(Any::class, Any::class),
            KProperty1::class.asTypeName().parameterizedBy(dataType, STAR)
        )).addModifiers(KModifier.OVERRIDE)

    private fun generateColumnsList() = PropertySpec
        .builder("columns", List::class.asTypeName().parameterizedBy(Column::class.parameterizedBy(Any::class, Any::class)))
        .addModifiers(KModifier.OVERRIDE)
}