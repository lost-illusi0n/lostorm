package net.lostillusion.lostorm.annotationprocessor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.*
import kotlinx.metadata.KmClassifier
import net.lostillusion.lostorm.annotations.Columns
import net.lostillusion.lostorm.annotations.EntityDataClass
import net.lostillusion.lostorm.annotations.ValueConverter
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import kotlin.reflect.KClass


const val LOSTORM_PACKAGE = "net.lostillusion.lostorm.mapper"
const val SQL_TYPE = "sqlType"
const val CUSTOM_TYPE = "customType"

lateinit var types: Types
lateinit var elements: Elements

fun getColumnMemberName(typeName: TypeName): MemberName {
    return when (typeName) {
        String::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "text")
        Boolean::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "bool")
        Int::class.asTypeName() -> MemberName(LOSTORM_PACKAGE, "int")
        Long::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "long")
        Short::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "short")
        Float::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "real")
        Double::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "float")
//        Array<Byte>::class.asClassName() -> MemberName(LOSTORM_PACKAGE, "binary")
        else -> throw Exception("Tried to get an invalid column member name, make sure you are using valid datatypes!")
    }
}

@KotlinPoetMetadataPreview
@ExperimentalStdlibApi
@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(EntityDataClass::class.java.name)
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        types = processingEnv.typeUtils
        elements = processingEnv.elementUtils
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(EntityDataClass::class.java).forEach(::processEntity)
        return true
    }

    @Suppress("DEPRECATION")
    private fun processEntity(element: Element) {
        //utility
        fun extractTableName(): String = element.getAnnotation(EntityDataClass::class.java)!!.tableName.let {
            if (it != "_none") it else "${element.simpleName.toString().toLowerCase()}table"
        }

        fun generateInitializer(property: Element, kmProperty: ImmutableKmProperty): String {
            val columns = property.getAnnotation(Columns::class.java)
            val columnName = columns
                ?.columnName
                ?.let { if (it.isEmpty()) null else it }
                ?: property.simpleName.toString()
            return buildString {
                append("""%M("$columnName")""")
                columns
                    ?.let { ColumnProperty.fromColumn(columns, kmProperty) }
                    ?.map(ColumnProperty::initializer)
                    ?.forEach { append(it) }
                if (property.getAnnotation(ValueConverter::class.java) != null) append(".converter(%M)")
            }
        }

        val entityRenderer = EntityRenderer(
            processingEnv.elementUtils.getPackageOf(element).toString(),
            "${element.simpleName}Entity",
            extractTableName(),
            element.asType().asTypeName() as ClassName
        )

        val metadataProperties = (element as TypeElement).toImmutableKmClass().properties

        for (property in element.enclosedElements) {
            if (property.kind == ElementKind.FIELD) {
                val kmProperty = metadataProperties.find { it.name == property.simpleName.toString() }!!
                val memberNames = mutableListOf<MemberName>()

                val typeArguments = processTypes(property, kmProperty)
                typeArguments.getValue(SQL_TYPE).let { memberNames.add(getColumnMemberName(it)) }

                property.getAnnotationClassValue<ValueConverter> { converter }
                    ?.let(types::asElement)
                    ?.let {
                        memberNames.add(
                            MemberName(
                                elements.getPackageOf(it).qualifiedName.toString(),
                                it.simpleName.toString()
                            )
                        )
                    }
                entityRenderer.addColumnRenderer(
                    ColumnRenderer(
                        property,
                        typeArguments.getValue(SQL_TYPE),
                        typeArguments.getValue(CUSTOM_TYPE),
                        generateInitializer(property, kmProperty),
                        *memberNames.toTypedArray()
                    )
                )
            }
        }

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        entityRenderer.render().writeTo(File(kaptKotlinGeneratedDir!!))
    }
}

sealed class ColumnProperty {
    abstract val initializer: String

    object UniqueProperty : ColumnProperty() {
        override val initializer = ".unique()"
    }

    object PrimaryProperty : ColumnProperty() {
        override val initializer = ".primary()"
    }

    object NullableProperty : ColumnProperty() {
        override val initializer = ".nullable()"
    }

    @KotlinPoetMetadataPreview
    class DefaultValueProperty(private val defaultValue: String, private val kmProperty: ImmutableKmProperty) :
        ColumnProperty() {
        override val initializer: String
            get() {
                val defaultValueArg = when (sanitizedClassNameBestGuess(kmProperty.returnType)) {
                    String::class.asClassName() -> "'$defaultValue"
                    else -> defaultValue
                }
                return ".defaultValue($defaultValueArg)"
            }
    }

    companion object {
        @KotlinPoetMetadataPreview
        @ExperimentalStdlibApi
        fun fromColumn(columns: Columns, kmProperty: ImmutableKmProperty) =
            buildList {
                if (columns.unique) add(UniqueProperty)
                if (columns.primaryKey) add(PrimaryProperty)
                if (columns.nullable) add(NullableProperty)
                if (columns.defaultValue != "¯\\_(ツ)_/¯") add(DefaultValueProperty(columns.defaultValue, kmProperty))
            }
    }
}

class ConverterNotAnObjectException(converter: TypeElement) :
    Exception("Converter ${converter.simpleName} is not an object and thus cannot be used!")

@ExperimentalStdlibApi
@KotlinPoetMetadataPreview
private fun processTypes(actual: Element, kmProperty: ImmutableKmProperty): Map<String, ClassName> {
    return if (actual.getAnnotation(ValueConverter::class.java) != null) {
        val converter = types.asElement(actual.getAnnotationClassValue<ValueConverter> { converter }) as TypeElement
        val converterKm = converter.toImmutableKmClass()
        if (!converterKm.isObject) throw ConverterNotAnObjectException(converter)
        val superConverter =
            converterKm.supertypes.find { (it.classifier as KmClassifier.Class).name == "net/lostillusion/lostorm/mapper/Converter" }!!
        val typeArgs = superConverter.arguments
            .map { it.type!!.classifier as KmClassifier.Class }
            .map { it.name.replace("/", ".") }

        buildMap {
            put(CUSTOM_TYPE, ClassName.bestGuess(typeArgs[0]))
            put(SQL_TYPE, ClassName.bestGuess(typeArgs[1]))
        }
    } else {
        val type = sanitizedClassNameBestGuess(kmProperty.returnType)
        mapOf(Pair(CUSTOM_TYPE, type), Pair(SQL_TYPE, type))
    }
}

private inline fun <reified T : Annotation> Element.getAnnotationClassValue(crossinline f: T.() -> KClass<*>): TypeMirror? {
    return try {
        val annotation = getAnnotation(T::class.java) ?: return null
        annotation.f()
        throw Exception("Expected to get a MirroredTypeException!")
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }
}

@KotlinPoetMetadataPreview
private fun sanitizedClassNameBestGuess(type: ImmutableKmType) =
    ClassName.bestGuess((type.classifier as KmClassifier.Class).name.replace("/", "."))
