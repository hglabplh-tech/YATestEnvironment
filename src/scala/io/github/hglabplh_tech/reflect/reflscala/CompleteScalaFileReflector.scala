package io.github.hglabplh_tech.reflect.reflscala

import java.lang.Class
import java.lang.reflect.{Constructor, Method, Field, AnnotatedType, AnnotatedElement, Parameter, Modifier}
import java.util.{ArrayList, HashMap, List as JList, Map as JMap}
import scala.annotation.static
import scala.jdk.CollectionConverters.*

object CompleteScalaFileReflector {

  // ------------------------------------------------------------
  // Öffentliche API
  // ------------------------------------------------------------

  /**
   * Inspiziert eine Klasse anhand ihres vollqualifizierten Klassennamens.
   *
   * Beispiel:
   *   ScalaInspector.inspectClass("mypackage.MyClass")
   *
   * Rückgabe:
   *   java.util.Map[String, Object]
   *
   * Damit direkt aus Java, Clojure, Kotlin etc. verwendbar.
   */

  def  inspectClass(className: String): JMap[String, Object] = {
    val clazz = Class.forName(className)
    inspectClass(clazz)
  }

  /**
   * Inspiziert direkt ein java.lang.Class-Objekt.
   */
  def inspectClass(clazz: Class[?]): JMap[String, Object] = {

    val result = new HashMap[String, Object]()

    result.put("name", clazz.getName)
    result.put("simpleName", clazz.getSimpleName)
    result.put("canonicalName", clazz.getCanonicalName)

    result.put("modifiers", Integer.valueOf(clazz.getModifiers))
    result.put("modifierText", Modifier.toString(clazz.getModifiers))

    result.put(
      "isInterface",
      java.lang.Boolean.valueOf(clazz.isInterface)
    )

    result.put(
      "isEnum",
      java.lang.Boolean.valueOf(clazz.isEnum)
    )

    result.put(
      "isAnnotation",
      java.lang.Boolean.valueOf(clazz.isAnnotation)
    )

    result.put(
      "isArray",
      java.lang.Boolean.valueOf(clazz.isArray)
    )

    result.put(
      "isPrimitive",
      java.lang.Boolean.valueOf(clazz.isPrimitive)
    )

    result.put(
      "isRecord",
      java.lang.Boolean.valueOf(clazz.isRecord)
    )

    result.put("superClass", classNameOrNull(clazz.getSuperclass))

    result.put(
      "interfaces",
      clazz.getInterfaces
        .map(_.getName)
        .toList
        .asJava
    )

    result.put(
      "annotations",
      inspectAnnotations(clazz.getAnnotations)
    )

    result.put(
      "constructors",
      inspectConstructors(clazz.getDeclaredConstructors)
    )

    result.put(
      "methods",
      inspectMethods(clazz.getDeclaredMethods)
    )

    result.put(
      "fields",
      inspectFields(clazz.getDeclaredFields)
    )

    result.put(
      "nestedClasses",
      inspectNestedClasses(clazz.getDeclaredClasses)
    )

    result
  }

  // ------------------------------------------------------------
  // Methoden
  // ------------------------------------------------------------

  private def inspectMethods(
                              methods: Array[Method]
                            ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    methods.foreach { method =>

      val info =
        new HashMap[String, Object]()

      info.put("name", method.getName)

      info.put(
        "declaringClass",
        method.getDeclaringClass.getName
      )

      info.put(
        "returnType",
        method.getReturnType.getTypeName
      )

      info.put(
        "genericReturnType",
        method.getGenericReturnType.getTypeName
      )

      info.put(
        "modifiers",
        Integer.valueOf(method.getModifiers)
      )

      info.put(
        "modifierText",
        Modifier.toString(method.getModifiers)
      )

      info.put(
        "isBridge",
        java.lang.Boolean.valueOf(method.isBridge)
      )

      info.put(
        "isSynthetic",
        java.lang.Boolean.valueOf(method.isSynthetic)
      )

      info.put(
        "isVarArgs",
        java.lang.Boolean.valueOf(method.isVarArgs)
      )

      info.put(
        "parameters",
        inspectParameters(method.getParameters)
      )

      info.put(
        "parameterTypes",
        method.getParameterTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "genericParameterTypes",
        method.getGenericParameterTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "exceptionTypes",
        method.getExceptionTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "typeParameters",
        method.getTypeParameters
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "annotations",
        inspectAnnotations(method.getAnnotations)
      )

      info.put(
        "signature",
        buildMethodSignature(method)
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Konstruktoren
  // ------------------------------------------------------------

  private def inspectConstructors(
                                   constructors: Array[Constructor[?]]
                                 ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    constructors.foreach { constructor =>

      val info =
        new HashMap[String, Object]()

      info.put("name", constructor.getName)

      info.put(
        "modifiers",
        Integer.valueOf(constructor.getModifiers)
      )

      info.put(
        "modifierText",
        Modifier.toString(constructor.getModifiers)
      )

      info.put(
        "parameters",
        inspectParameters(constructor.getParameters)
      )

      info.put(
        "parameterTypes",
        constructor.getParameterTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "genericParameterTypes",
        constructor.getGenericParameterTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "exceptionTypes",
        constructor.getExceptionTypes
          .map(_.getTypeName)
          .toList
          .asJava
      )

      info.put(
        "annotations",
        inspectAnnotations(constructor.getAnnotations)
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Parameter
  // ------------------------------------------------------------

  private def inspectParameters(
                                 parameters: Array[Parameter]
                               ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    parameters.foreach { parameter =>

      val info =
        new HashMap[String, Object]()

      info.put("name", parameter.getName)
      info.put("type", parameter.getType.getTypeName)

      info.put(
        "parameterizedType",
        parameter.getParameterizedType.getTypeName
      )

      info.put(
        "modifiers",
        Integer.valueOf(parameter.getModifiers)
      )

      info.put(
        "isNamePresent",
        java.lang.Boolean.valueOf(parameter.isNamePresent)
      )

      info.put(
        "isImplicit",
        java.lang.Boolean.valueOf(parameter.isImplicit)
      )

      info.put(
        "isSynthetic",
        java.lang.Boolean.valueOf(parameter.isSynthetic)
      )

      info.put(
        "isVarArgs",
        java.lang.Boolean.valueOf(parameter.isVarArgs)
      )

      info.put(
        "annotations",
        inspectAnnotations(parameter.getAnnotations)
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Felder
  // ------------------------------------------------------------

  private def inspectFields(
                             fields: Array[Field]
                           ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    fields.foreach { field =>

      val info =
        new HashMap[String, Object]()

      info.put("name", field.getName)
      info.put("type", field.getType.getTypeName)

      info.put(
        "genericType",
        field.getGenericType.getTypeName
      )

      info.put(
        "declaringClass",
        field.getDeclaringClass.getName
      )

      info.put(
        "modifiers",
        Integer.valueOf(field.getModifiers)
      )

      info.put(
        "modifierText",
        Modifier.toString(field.getModifiers)
      )

      info.put(
        "isSynthetic",
        java.lang.Boolean.valueOf(field.isSynthetic)
      )

      info.put(
        "enumConstant",
        java.lang.Boolean.valueOf(field.isEnumConstant)
      )

      info.put(
        "annotations",
        inspectAnnotations(field.getAnnotations)
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Nested Classes
  // ------------------------------------------------------------

  private def inspectNestedClasses(
                                    classes: Array[Class[?]]
                                  ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    classes.foreach { clazz =>

      val info =
        new HashMap[String, Object]()

      info.put("name", clazz.getName)
      info.put("simpleName", clazz.getSimpleName)

      info.put(
        "modifiers",
        Integer.valueOf(clazz.getModifiers)
      )

      info.put(
        "modifierText",
        Modifier.toString(clazz.getModifiers)
      )

      info.put(
        "isInterface",
        java.lang.Boolean.valueOf(clazz.isInterface)
      )

      info.put(
        "isEnum",
        java.lang.Boolean.valueOf(clazz.isEnum)
      )

      info.put(
        "isAnnotation",
        java.lang.Boolean.valueOf(clazz.isAnnotation)
      )

      info.put(
        "isSynthetic",
        java.lang.Boolean.valueOf(clazz.isSynthetic)
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Annotationen
  // ------------------------------------------------------------

  private def inspectAnnotations(
                                  annotations: Array[java.lang.annotation.Annotation]
                                ): JList[JMap[String, Object]] = {

    val result =
      new ArrayList[JMap[String, Object]]()

    annotations.foreach { annotation =>

      val info =
        new HashMap[String, Object]()

      info.put(
        "type",
        annotation.annotationType().getName
      )

      info.put(
        "value",
        annotation.toString
      )

      result.add(info)
    }

    result
  }

  // ------------------------------------------------------------
  // Hilfsfunktionen
  // ------------------------------------------------------------

  private def classNameOrNull(
                               clazz: Class[?]
                             ): Object = {
    if clazz == null then null
    else clazz.getName
  }

  private def buildMethodSignature(
                                    method: Method
                                  ): String = {

    val parameters =
      method.getGenericParameterTypes
        .map(_.getTypeName)
        .mkString(", ")

    s"${method.getName}($parameters): ${method.getGenericReturnType.getTypeName}"
  }
}