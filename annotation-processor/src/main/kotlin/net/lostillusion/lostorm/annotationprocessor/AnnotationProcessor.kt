package net.lostillusion.lostorm.annotationprocessor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import net.lostillusion.lostorm.mapper.Column
import net.lostillusion.lostorm.mapper.Entity
import net.lostillusion.lostorm.annotations.Columns
import net.lostillusion.lostorm.annotations.EntityDataClass
import java.io.File
import java.lang.Exception
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeVisitor
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

@Suppress("unused")
@AutoService(Processor::class)
class AnnotationProcessor: AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(EntityDataClass::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(EntityDataClass::class.java).forEach(::processEntityAnnotation)
        return false
    }

    @Suppress("DEPRECATION")
    private fun processEntityAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()
        val tableName = element.getAnnotation(EntityDataClass::class.java).tableName.let {
            if(it != "_none") it else "${className.toLowerCase()}table"
        }

        val entityName = "${className}Entity"
        val fileBuilder = FileSpec.builder(pack, entityName)
        val entityBuilder = TypeSpec.objectBuilder(entityName)
        entityBuilder.superclass(Entity::class.asTypeName().parameterizedBy(element.asType().asTypeName()))
        entityBuilder.addSuperclassConstructorParameter("\"$tableName\"")
        val columnsBuilder = PropertySpec.builder("columns", List::class.asTypeName().parameterizedBy(Column::class.asTypeName().parameterizedBy(STAR)))
        columnsBuilder.addModifiers(KModifier.OVERRIDE)
        val columns: MutableList<String> = mutableListOf()
        val columnsToValuesBuilder = PropertySpec.builder("columnsToValues", Map::class.asTypeName().parameterizedBy(
            Column::class.asTypeName().parameterizedBy(STAR),
            KProperty1::class.asTypeName().parameterizedBy(element.asType().asTypeName(), STAR)
        )).addModifiers(KModifier.OVERRIDE)
        val ctvs = mutableListOf<String>()

        for(enclosed in element.enclosedElements) {
            if(enclosed.kind == ElementKind.FIELD) {
                val config = enclosed.getAnnotation(Columns::class.java)
                var columnName = enclosed.simpleName.toString()
                if(config != null && config.columnName != "") columnName = config.columnName
                var typeName: TypeName?
                var initializer: String?
                val initMember: MemberName?
                when(enclosed.asType().kind) {
                    TypeKind.DECLARED -> {
                        when(Class.forName(enclosed.asType().toString()).kotlin) {
                            String::class -> {
                                typeName = Column::class.asTypeName().parameterizedBy(String::class.asTypeName().copy(nullable = config?.nullable == true))
                                initializer = """%M("$columnName")"""
                                initMember = MemberName("net.lostillusion.lostorm.mapper", "text")
                            }
                            else -> {
                                throw UnsupportedEntityValueType("Declared value ${enclosed.simpleName} in $className cannot be converted to a column! Type found: ${enclosed.asType()}")
                            }
                        }
                    }
                    TypeKind.BOOLEAN -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Boolean::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "bool")
                    }
                    TypeKind.INT -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Int::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "int")
                    }
                    TypeKind.LONG -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Long::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "long")
                    }
                    TypeKind.SHORT -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Short::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "short")
                    }
                    TypeKind.FLOAT -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Float::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "real")
                    }
                    TypeKind.DOUBLE -> {
                        typeName = Column::class.asTypeName().parameterizedBy(Double::class.asTypeName().copy(nullable = config?.nullable == true))
                        initializer = """%M("$columnName")"""
                        initMember = MemberName("net.lostillusion.lostorm.mapper", "float")
                    }
                    TypeKind.ARRAY -> {
                        when(enclosed.asType().asTypeName()) {
                            Array<Byte>::class.parameterizedBy(Byte::class) -> {
                                typeName = Column::class.asTypeName().parameterizedBy(ByteArray::class.asTypeName().copy(nullable = config?.nullable == true))
                                initializer = """%M("$columnName")"""
                                initMember = MemberName("net.lostillusion.lostorm.mapper", "binary")
                            }
                            else -> {
                                throw UnsupportedEntityValueType("Array ${enclosed.simpleName} in $className cannot be converted to a column! Type found: ${enclosed.asType()}")
                            }
                        }
                    }
                    else -> throw UnsupportedEntityValueType("Value ${enclosed.simpleName} in $className cannot be converted to a column! Type found: ${enclosed.asType().kind.name}")
                }
                if(config?.unique == true) initializer += ".unique()"
                if(config?.nullable == true) initializer += ".nullable()"
                if(config?.primaryKey == true) initializer += ".primary()"
                columns.add(enclosed.simpleName.toString())
                ctvs.add("${enclosed.simpleName} to ${className}::${enclosed.simpleName}")
                PropertySpec.builder(enclosed.simpleName.toString(), typeName).initializer(initializer, initMember).mutable(false).build().let(entityBuilder::addProperty)
            }
        }
        columnsBuilder.initializer("listOf(${columns.joinToString(", ")})")
        columnsToValuesBuilder.initializer("mapOf(${ctvs.joinToString(", ")})")

        val dataClassFunBuilder = FunSpec.builder("createDataClass")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("values", List::class.asTypeName().parameterizedBy(STAR))
            .addCode("return $className::class.constructors.first().call(*values.toTypedArray())")
            .returns(element.asType().asTypeName())

        entityBuilder.addFunction(dataClassFunBuilder.build())
        entityBuilder.addProperty(columnsBuilder.build())
        entityBuilder.addProperty(columnsToValuesBuilder.build())

        fileBuilder.addType(entityBuilder.build())
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        println("writing generated files to: $kaptKotlinGeneratedDir")
        fileBuilder.build().writeTo(File(kaptKotlinGeneratedDir!!))
    }
}

class UnsupportedEntityValueType(override val message: String): Exception()