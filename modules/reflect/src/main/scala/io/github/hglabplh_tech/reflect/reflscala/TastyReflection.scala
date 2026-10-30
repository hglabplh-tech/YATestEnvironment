package io.github.hglabplh_tech.reflect.reflscala

import java.util.{ArrayList, HashMap, List as JList, Map as JMap}
import scala.annotation.static
import scala.quoted.*
import scala.tasty.inspector.*

object TastyReflection {

  /**
   * Inspiziert eine Liste konkreter .tasty-Dateien.
   *
   * Rückgabe:
   *
   * java.util.List[
   *   java.util.Map[String, Object]
   * ]
   */

  def inspectTastyFiles(
      tastyFiles: JList[String]
  ): JList[JMap[String, Object]] =

    val inspector = new MetadataInspector

    TastyInspector.inspectTastyFiles(
      tastyFiles.toArray(new Array[String](0)).toList
    )(inspector)

    inspector.result


  private class MetadataInspector extends Inspector:

    val result =
      new ArrayList[JMap[String, Object]]()

    override def inspect(using Quotes)(
        tastys: List[Tasty[quotes.type]]
    ): Unit =

      import quotes.reflect.*

      tastys.foreach { tasty =>

        val fileInfo =
          new HashMap[String, Object]()

        val tree = tasty.ast

        fileInfo.put(
          "tree",
          tree.show
        )

        fileInfo.put(
          "treeStructure",
          tree.show(using Printer.TreeStructure)
        )

        fileInfo.put(
          "definitions",
          inspectTree(tree)
        )

        result.add(fileInfo)
      }


  private def inspectTree(using Quotes)(
      root: quotes.reflect.Tree
  ): JList[JMap[String, Object]] =

    import quotes.reflect.*

    val result =
      new ArrayList[JMap[String, Object]]()

    class Traverser extends TreeTraverser:

      override def traverseTree(
          tree: Tree
      )(owner: Symbol): Unit =

        tree match

          case classDef: ClassDef =>
            result.add(
              inspectClassDef(classDef)
            )

          case method: DefDef =>
            result.add(
              inspectDefDef(method)
            )

          case value: ValDef =>
            result.add(
              inspectValDef(value)
            )

          case typeDef: TypeDef =>
            result.add(
              inspectTypeDef(typeDef)
            )

          case _ =>
            ()

        super.traverseTree(tree)(owner)

    val traverser = new Traverser

    traverser.traverseTree(root)(root.symbol)

    result


  // ----------------------------------------------------------
  // Class / Trait / Object / Enum
  // ----------------------------------------------------------

  private def inspectClassDef(using Quotes)(
      tree: quotes.reflect.ClassDef
  ): JMap[String, Object] =

    import quotes.reflect.*

    val result =
      new HashMap[String, Object]()

    val symbol = tree.symbol

    result.put(
      "kind",
      "class"
    )

    result.put(
      "name",
      tree.name
    )

    addSymbolInformation(
      result,
      symbol
    )

    val parents =
      new ArrayList[String]()

    tree.parents.foreach { parent =>
      parents.add(
        parent.show
      )
    }

    result.put(
      "parents",
      parents
    )

    val constructorParams =
      new ArrayList[JMap[String, Object]]()

    tree.constructor.paramss.foreach {

      case termClause: TermParamClause =>

        termClause.params.foreach { param =>

          val p =
            new HashMap[String, Object]()

          p.put(
            "name",
            param.name
          )

          p.put(
            "type",
            param.tpt.tpe.show
          )

          p.put(
            "symbol",
            param.symbol.fullName
          )

          constructorParams.add(p)
        }

      case _ =>
        ()
    }

    result.put(
      "constructorParameters",
      constructorParams
    )

    result


  // ----------------------------------------------------------
  // Methoden / Funktionen
  // ----------------------------------------------------------

  private def inspectDefDef(using Quotes)(
      tree: quotes.reflect.DefDef
  ): JMap[String, Object] =

    import quotes.reflect.*

    val result =
      new HashMap[String, Object]()

    val symbol = tree.symbol

    result.put(
      "kind",
      "method"
    )

    result.put(
      "name",
      tree.name
    )

    addSymbolInformation(
      result,
      symbol
    )

    result.put(
      "returnType",
      tree.returnTpt.tpe.show
    )

    result.put(
      "methodType",
      symbol.typeRef.show
    )

    val parameterLists =
      new ArrayList[JList[JMap[String, Object]]]()

    tree.paramss.foreach {

      case termClause: TermParamClause =>

        val parameters =
          new ArrayList[JMap[String, Object]]()

        termClause.params.foreach { param =>

          val p =
            new HashMap[String, Object]()

          p.put(
            "name",
            param.name
          )

          p.put(
            "type",
            param.tpt.tpe.show
          )

          p.put(
            "flags",
            flags(param.symbol)
          )

          p.put(
            "isGiven",
            bool(
              param.symbol.flags.is(Flags.Given)
            )
          )

          p.put(
            "isImplicit",
            bool(
              param.symbol.flags.is(Flags.Implicit)
            )
          )

          parameters.add(p)
        }

        parameterLists.add(parameters)

      case typeClause: TypeParamClause =>

        val parameters =
          new ArrayList[JMap[String, Object]]()

        typeClause.params.foreach { param =>

          val p =
            new HashMap[String, Object]()

          p.put(
            "name",
            param.name
          )

          p.put(
            "kind",
            "typeParameter"
          )

          p.put(
            "type",
            param.show
          )

          parameters.add(p)
        }

        parameterLists.add(parameters)
    }

    result.put(
      "parameterLists",
      parameterLists
    )

    result.put(
      "isExtension",
      bool(
        symbol.flags.is(Flags.ExtensionMethod)
      )
    )

    result.put(
      "isGiven",
      bool(
        symbol.flags.is(Flags.Given)
      )
    )

    result.put(
      "isImplicit",
      bool(
        symbol.flags.is(Flags.Implicit)
      )
    )

    result.put(
      "isInline",
      bool(
        symbol.flags.is(Flags.Inline)
      )
    )

    result.put(
      "isTransparent",
      bool(
        symbol.flags.is(Flags.Transparent)
      )
    )

    result.put(
      "isSynthetic",
      bool(
        symbol.flags.is(Flags.Synthetic)
      )
    )

    result


  // ----------------------------------------------------------
  // vals / vars / fields
  // ----------------------------------------------------------

  private def inspectValDef(using Quotes)(
      tree: quotes.reflect.ValDef
  ): JMap[String, Object] =

    import quotes.reflect.*

    val result =
      new HashMap[String, Object]()

    val symbol =
      tree.symbol

    result.put(
      "kind",
      "value"
    )

    result.put(
      "name",
      tree.name
    )

    result.put(
      "type",
      tree.tpt.tpe.show
    )

    addSymbolInformation(
      result,
      symbol
    )

    result.put(
      "mutable",
      bool(
        symbol.flags.is(Flags.Mutable)
      )
    )

    result.put(
      "given",
      bool(
        symbol.flags.is(Flags.Given)
      )
    )

    result.put(
      "lazy",
      bool(
        symbol.flags.is(Flags.Lazy)
      )
    )

    result


  // ----------------------------------------------------------
  // Type aliases / opaque types / nested types
  // ----------------------------------------------------------

  private def inspectTypeDef(using Quotes)(
      tree: quotes.reflect.TypeDef
  ): JMap[String, Object] =

    import quotes.reflect.*

    val result =
      new HashMap[String, Object]()

    val symbol =
      tree.symbol

    result.put(
      "kind",
      "type"
    )

    result.put(
      "name",
      tree.name
    )

    addSymbolInformation(
      result,
      symbol
    )

    result.put(
      "type",
      tree.show
    )

    result.put(
      "opaque",
      bool(
        symbol.flags.is(Flags.Opaque)
      )
    )

    result


  // ----------------------------------------------------------
  // Gemeinsame Symbolinformationen
  // ----------------------------------------------------------

  private def addSymbolInformation(using Quotes)(
      result: JMap[String, Object],
      symbol: quotes.reflect.Symbol
  ): Unit =

    import quotes.reflect.*

    result.put(
      "fullName",
      symbol.fullName
    )

    result.put(
      "flags",
      flags(symbol)
    )

    result.put(
      "private",
      bool(
        symbol.flags.is(Flags.Private)
      )
    )

    result.put(
      "protected",
      bool(
        symbol.flags.is(Flags.Protected)
      )
    )

    result.put(
      "final",
      bool(
        symbol.flags.is(Flags.Final)
      )
    )

    result.put(
      "sealed",
      bool(
        symbol.flags.is(Flags.Sealed)
      )
    )

    result.put(
      "abstract",
      bool(
        symbol.flags.is(Flags.Abstract)
      )
    )

    result.put(
      "case",
      bool(
        symbol.flags.is(Flags.Case)
      )
    )

    result.put(
      "enum",
      bool(
        symbol.flags.is(Flags.Enum)
      )
    )

    result.put(
      "trait",
      bool(
        symbol.flags.is(Flags.Trait)
      )
    )

    result.put(
      "module",
      bool(
        symbol.flags.is(Flags.Module)
      )
    )

    result.put(
      "synthetic",
      bool(
        symbol.flags.is(Flags.Synthetic)
      )
    )

    val owner =
      symbol.owner

    if owner != Symbol.noSymbol then
      result.put(
        "owner",
        owner.fullName
      )

    val pos =
      symbol.pos

    pos.foreach { position =>

      result.put(
        "sourceFile",
        position.sourceFile.path
      )

      result.put(
        "startLine",
        Integer.valueOf(
          position.startLine
        )
      )

      result.put(
        "endLine",
        Integer.valueOf(
          position.endLine
        )
      )
    }


  private def flags(using Quotes)(
      symbol: quotes.reflect.Symbol
  ): JList[String] =

    import quotes.reflect.*

    val result =
      new ArrayList[String]()

    val checks =
      List(
        "Private" ->
          Flags.Private,

        "Protected" ->
          Flags.Protected,

        "Final" ->
          Flags.Final,

        "Sealed" ->
          Flags.Sealed,

        "Case" ->
          Flags.Case,

        "Implicit" ->
          Flags.Implicit,

        "Given" ->
          Flags.Given,

        "Inline" ->
          Flags.Inline,

        "Transparent" ->
          Flags.Transparent,

        "Lazy" ->
          Flags.Lazy,

        "Override" ->
          Flags.Override,

        "Mutable" ->
          Flags.Mutable,

        "Trait" ->
          Flags.Trait,

        "Enum" ->
          Flags.Enum,

        "Module" ->
          Flags.Module,

        "Synthetic" ->
          Flags.Synthetic,

        "ExtensionMethod" ->
          Flags.ExtensionMethod,

        "Opaque" ->
          Flags.Opaque
      )

    checks.foreach {
      case (name, flag) =>
        if symbol.flags.is(flag) then
          result.add(name)
    }

    result


  private def bool(
      value: Boolean
  ): java.lang.Boolean =
    java.lang.Boolean.valueOf(value)


}
