package io.github.hglabplh_tech.reflect.reflkotlin

import java.lang.reflect.Modifier
import java.util.ArrayList
import java.util.HashMap
import kotlin.collections.iterator

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

object CompleteKTFileReflector {

    // ------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------

    @JvmStatic
    fun inspectClass(className: String): Map<String, Any?> {
        val clazz = Class.forName(className)
        return inspectClass(clazz)
    }

    @JvmStatic
    fun inspectClass(clazz: Class<*>): Map<String, Any?> {

        val result = HashMap<String, Any?>()

        val kClass = clazz.kotlin

        result["language"] = "kotlin"
        result["name"] = clazz.name
        result["simpleName"] = clazz.simpleName

        result["jvm"] = inspectJvmClass(clazz)
        result["kotlin"] = inspectKotlinClass(kClass)

        return result
    }


    // ============================================================
    // Kotlin Reflection
    // ============================================================

    private fun inspectKotlinClass(
        kClass: KClass<*>
    ): Map<String, Any?> {

        val result = HashMap<String, Any?>()

        result["qualifiedName"] =
            kClass.qualifiedName

        result["simpleName"] =
            kClass.simpleName

        result["visibility"] =
            kClass.visibility?.name

        result["isAbstract"] =
            kClass.isAbstract

        result["isFinal"] =
            kClass.isFinal

        result["isOpen"] =
            kClass.isOpen

        result["isSealed"] =
            kClass.isSealed

        result["isData"] =
            kClass.isData

        result["isInner"] =
            kClass.isInner

        result["isCompanion"] =
            kClass.isCompanion

        result["isFun"] =
            kClass.isFun

        result["isValue"] =
            kClass.isValue

        result["objectInstance"] =
            kClass.objectInstance != null

        result["typeParameters"] =
            inspectTypeParameters(
                kClass.typeParameters
            )

        result["supertypes"] =
            inspectTypes(
                kClass.supertypes
            )

        result["constructors"] =
            inspectConstructors(
                kClass.constructors
            )

        result["functions"] =
            inspectFunctions(
                kClass
            )

        result["properties"] =
            inspectProperties(
                kClass
            )

        result["nestedClasses"] =
            inspectNestedClasses(
                kClass
            )

        result["sealedSubclasses"] =
            inspectSealedSubclasses(
                kClass
            )

        result["annotations"] =
            inspectAnnotations(
                kClass.annotations
            )

        return result
    }


    // ------------------------------------------------------------
    // Functions
    // ------------------------------------------------------------

    private fun inspectFunctions(
        kClass: KClass<*>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (function in kClass.functions) {

            val info =
                HashMap<String, Any?>()

            info["name"] =
                function.name

            info["returnType"] =
                inspectType(
                    function.returnType
                )

            info["visibility"] =
                function.visibility?.name

            info["isAbstract"] =
                function.isAbstract

            info["isFinal"] =
                function.isFinal

            info["isOpen"] =
                function.isOpen

            info["isSuspend"] =
                function.isSuspend

            info["isInline"] =
                function.isInline

            info["isExternal"] =
                function.isExternal

            info["isOperator"] =
                function.isOperator

            info["isInfix"] =
                function.isInfix

            info["parameters"] =
                inspectParameters(
                    function.parameters
                )

            info["typeParameters"] =
                inspectTypeParameters(
                    function.typeParameters
                )

            info["annotations"] =
                inspectAnnotations(
                    function.annotations
                )

            // Kotlin extension receiver
            val extensionReceiver =
                function.extensionReceiverParameter

            info["isExtension"] =
                extensionReceiver != null

            if (extensionReceiver != null) {
                info["extensionReceiver"] =
                    inspectParameter(
                        extensionReceiver
                    )
            }

            // instance / dispatch receiver
            val instanceReceiver =
                function.instanceParameter

            if (instanceReceiver != null) {
                info["instanceReceiver"] =
                    inspectParameter(
                        instanceReceiver
                    )
            }

            // actual JVM method
            val javaMethod =
                function.javaMethod

            if (javaMethod != null) {

                val jvm =
                    HashMap<String, Any?>()

                jvm["name"] =
                    javaMethod.name

                jvm["declaringClass"] =
                    javaMethod.declaringClass.name

                jvm["returnType"] =
                    javaMethod.returnType.typeName

                jvm["parameterTypes"] =
                    javaMethod.parameterTypes
                        .map { it.typeName }

                jvm["genericReturnType"] =
                    javaMethod.genericReturnType
                        .typeName

                jvm["modifiers"] =
                    javaMethod.modifiers

                jvm["modifierText"] =
                    Modifier.toString(
                        javaMethod.modifiers
                    )

                jvm["isSynthetic"] =
                    javaMethod.isSynthetic

                jvm["isBridge"] =
                    javaMethod.isBridge

                jvm["isVarArgs"] =
                    javaMethod.isVarArgs

                info["jvm"] = jvm
            }

            result.add(info)
        }

        return result
    }


    // ------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------

    private fun inspectConstructors(
        constructors:
        Collection<KFunction<*>>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (constructor in constructors) {

            val info =
                HashMap<String, Any?>()

            info["name"] =
                constructor.name

            info["visibility"] =
                constructor.visibility?.name

            info["parameters"] =
                inspectParameters(
                    constructor.parameters
                )

            info["typeParameters"] =
                inspectTypeParameters(
                    constructor.typeParameters
                )

            info["annotations"] =
                inspectAnnotations(
                    constructor.annotations
                )

            val javaConstructor =
                constructor.javaConstructor

            if (javaConstructor != null) {

                val jvm =
                    HashMap<String, Any?>()

                jvm["parameterTypes"] =
                    javaConstructor
                        .parameterTypes
                        .map { it.typeName }

                jvm["genericParameterTypes"] =
                    javaConstructor
                        .genericParameterTypes
                        .map { it.typeName }

                jvm["modifiers"] =
                    javaConstructor.modifiers

                jvm["isSynthetic"] =
                    javaConstructor.isSynthetic

                jvm["isVarArgs"] =
                    javaConstructor.isVarArgs

                info["jvm"] = jvm
            }

            result.add(info)
        }

        return result
    }


    // ------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------

    private fun inspectParameters(
        parameters: List<KParameter>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (parameter in parameters) {
            result.add(
                inspectParameter(parameter)
            )
        }

        return result
    }


    private fun inspectParameter(
        parameter: KParameter
    ): Map<String, Any?> {

        val result =
            HashMap<String, Any?>()

        result["name"] =
            parameter.name

        result["index"] =
            parameter.index

        result["kind"] =
            parameter.kind.name

        result["type"] =
            inspectType(
                parameter.type
            )

        result["isOptional"] =
            parameter.isOptional

        result["isVararg"] =
            parameter.isVararg

        result["annotations"] =
            inspectAnnotations(
                parameter.annotations
            )

        return result
    }


    // ------------------------------------------------------------
    // Properties
    // ------------------------------------------------------------

    private fun inspectProperties(
        kClass: KClass<*>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (property in kClass.memberProperties) {

            val info =
                HashMap<String, Any?>()

            info["name"] =
                property.name

            info["returnType"] =
                inspectType(
                    property.returnType
                )

            info["visibility"] =
                property.visibility?.name

            info["isAbstract"] =
                property.isAbstract

            info["isFinal"] =
                property.isFinal

            info["isOpen"] =
                property.isOpen

            info["isLateinit"] =
                property.isLateinit

            info["isConst"] =
                property.isConst

            info["mutable"] =
                property is KMutableProperty<*>

            info["annotations"] =
                inspectAnnotations(
                    property.annotations
                )

            val javaField =
                property.javaField

            if (javaField != null) {

                val field =
                    HashMap<String, Any?>()

                field["name"] =
                    javaField.name

                field["type"] =
                    javaField.type.typeName

                field["genericType"] =
                    javaField.genericType
                        .typeName

                field["modifiers"] =
                    javaField.modifiers

                field["isSynthetic"] =
                    javaField.isSynthetic

                info["javaField"] = field
            }

            val getter =
                property.javaGetter

            if (getter != null) {
                info["javaGetter"] =
                    getter.name
            }

            if (property
                        is KMutableProperty<*>) {

                val setter =
                    property.javaSetter

                if (setter != null) {
                    info["javaSetter"] =
                        setter.name
                }
            }

            result.add(info)
        }

        return result
    }


    // ============================================================
    // Kotlin Type System
    // ============================================================

    private fun inspectType(
        type: KType
    ): Map<String, Any?> {

        val result =
            HashMap<String, Any?>()

        result["text"] =
            type.toString()

        result["nullable"] =
            type.isMarkedNullable

        val classifier =
            type.classifier

        when (classifier) {

            is KClass<*> -> {

                result["classifierKind"] =
                    "class"

                result["classifier"] =
                    classifier.qualifiedName
            }

            is KTypeParameter -> {

                result["classifierKind"] =
                    "typeParameter"

                result["classifier"] =
                    classifier.name
            }

            else -> {

                result["classifierKind"] =
                    "unknown"

                result["classifier"] =
                    classifier?.toString()
            }
        }

        val arguments =
            ArrayList<Map<String, Any?>>()

        for (argument in type.arguments) {

            val arg =
                HashMap<String, Any?>()

            arg["variance"] =
                argument.variance?.name

            if (argument.type != null) {
                arg["type"] =
                    inspectType(
                        argument.type!!
                    )
            } else {
                arg["starProjection"] = true
            }

            arguments.add(arg)
        }

        result["arguments"] =
            arguments

        return result
    }


    private fun inspectTypes(
        types: Collection<KType>
    ): List<Map<String, Any?>> {

        return types.map {
            inspectType(it)
        }
    }


    // ------------------------------------------------------------
    // Type parameters
    // ------------------------------------------------------------

    private fun inspectTypeParameters(
        parameters:
        List<KTypeParameter>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (parameter in parameters) {

            val info =
                HashMap<String, Any?>()

            info["name"] =
                parameter.name

            info["variance"] =
                parameter.variance.name

            info["isReified"] =
                parameter.isReified

            info["upperBounds"] =
                parameter.upperBounds.map {
                    inspectType(it)
                }

            result.add(info)
        }

        return result
    }


    // ------------------------------------------------------------
    // Nested classes
    // ------------------------------------------------------------

    private fun inspectNestedClasses(
        kClass: KClass<*>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (nested in kClass.nestedClasses) {

            val info =
                HashMap<String, Any?>()

            info["name"] =
                nested.simpleName

            info["qualifiedName"] =
                nested.qualifiedName

            info["isCompanion"] =
                nested.isCompanion

            info["isData"] =
                nested.isData

            info["isInner"] =
                nested.isInner

            info["isValue"] =
                nested.isValue

            result.add(info)
        }

        return result
    }


    // ------------------------------------------------------------
    // Sealed hierarchy
    // ------------------------------------------------------------

    private fun inspectSealedSubclasses(
        kClass: KClass<*>
    ): List<Map<String, Any?>> {

        return kClass
            .sealedSubclasses
            .map {

                hashMapOf<String, Any?>(
                    "name" to it.simpleName,
                    "qualifiedName"
                            to it.qualifiedName
                )
            }
    }


    // ------------------------------------------------------------
    // Annotations
    // ------------------------------------------------------------

    private fun inspectAnnotations(
        annotations: List<Annotation>
    ): List<Map<String, Any?>> {

        val result =
            ArrayList<Map<String, Any?>>()

        for (annotation in annotations) {

            val info =
                HashMap<String, Any?>()

            info["type"] =
                annotation.annotationClass
                    .qualifiedName

            info["value"] =
                annotation.toString()

            result.add(info)
        }

        return result
    }


    // ============================================================
    // Plain JVM Reflection
    // ============================================================

    private fun inspectJvmClass(
        clazz: Class<*>
    ): Map<String, Any?> {

        val result =
            HashMap<String, Any?>()

        result["name"] =
            clazz.name

        result["superclass"] =
            clazz.superclass?.name

        result["interfaces"] =
            clazz.interfaces
                .map { it.name }

        result["methods"] =
            clazz.declaredMethods.map {

                hashMapOf<String, Any?>(
                    "name" to it.name,

                    "returnType"
                            to it.returnType.typeName,

                    "parameterTypes"
                            to it.parameterTypes
                        .map { p ->
                            p.typeName
                        },

                    "modifiers"
                            to it.modifiers,

                    "synthetic"
                            to it.isSynthetic,

                    "bridge"
                            to it.isBridge
                )
            }

        result["fields"] =
            clazz.declaredFields.map {

                hashMapOf<String, Any?>(
                    "name" to it.name,
                    "type"
                            to it.type.typeName,
                    "synthetic"
                            to it.isSynthetic
                )
            }

        result["constructors"] =
            clazz.declaredConstructors
                .map {

                    hashMapOf<String, Any?>(
                        "parameterTypes"
                                to it.parameterTypes
                            .map { p ->
                                p.typeName
                            },

                        "synthetic"
                                to it.isSynthetic
                    )
                }

        return result
    }
}
