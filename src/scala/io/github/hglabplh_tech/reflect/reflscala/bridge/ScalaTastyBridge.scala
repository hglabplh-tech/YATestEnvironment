package io.github.hglabplh_tech.reflect.reflscala.bridge

import java.util.{ArrayList, List => JList, Map => JMap}
import scala.jdk.CollectionConverters.*
import scala.tasty.inspector.TastyInspector

object ScalaTastyBridge {

  /**
   * Inspect one Scala 3 .tasty file.
   *
   * The public bridge deliberately exposes only Java collection types.
   * No quotes.reflect.Symbol or Scala collection escapes to Clojure.
   */
  def inspectTastyFile(path: String): JList[JMap[String, Object]] = {
    val result = new ArrayList[JMap[String, Object]]()

    TastyInspector.inspectTastyFiles(
      List(path)
    )(new ScalaReflectionInspector(result))

    result
  }

  /**
   * Inspect several Scala 3 .tasty files.
   */
  def inspectTastyFiles(
      paths: JList[String]
  ): JList[JMap[String, Object]] = {

    val result = new ArrayList[JMap[String, Object]]()

    TastyInspector.inspectTastyFiles(
      paths.asScala.toList
    )(new ScalaReflectionInspector(result))

    result
  }
}
