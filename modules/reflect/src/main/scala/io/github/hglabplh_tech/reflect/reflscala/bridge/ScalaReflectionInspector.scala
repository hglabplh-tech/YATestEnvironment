package io.github.hglabplh_tech.reflect.reflscala.bridge

import scala.quoted.*
import scala.tasty.inspector.*

import java.util.{
  ArrayList,
  HashMap,
  List => JList,
  Map => JMap
}

/**
 * Scala 3.8.4 TASTy inspector.
 *
 * This implementation intentionally avoids APIs that are documented as
 * experimental or deprecated in Scala 3.8.4.
 *
 * In particular it does NOT use:
 *   - Symbol.info
 *   - Tree.tpe on generic Tree
 *   - deprecated TermParamClause.isErased
 *   - experimental erased-parameter helpers
 *
 * The Clojure/JVM boundary is kept to java.util.List / java.util.Map.
 */
final class ScalaReflectionInspector(
    result: JList[JMap[String, Object]]
) extends Inspector {

  private var lambdaCounter: Long = 0L

  override def inspect(using Quotes)(
      tastys: List[Tasty[quotes.type]]
  ): Unit = {

    import quotes.reflect.*

    object Traverser extends TreeTraverser {

      override def traverseTree(
          tree: Tree
      )(owner: Symbol): Unit = {

        tree match {

          // ------------------------------------------------------------
          // CLASS / TRAIT / ENUM
          // ------------------------------------------------------------

          case cls: ClassDef =>
            inspectClassDef(cls)

          // ------------------------------------------------------------
          // METHOD / TOP-LEVEL FUNCTION
          // ------------------------------------------------------------

          case d: DefDef =>
            inspectDefDef(d)

          // ------------------------------------------------------------
          // LAMBDA
          //
          // Lambda.unapply in Scala 3.8.4 accepts Block, therefore
          // we first refine Tree -> Block and only then use Lambda.
          // ------------------------------------------------------------

          case block: Block =>
            block match {
              case Lambda(params, body) =>
                inspectLambda(
                  params,
                  body,
                  block,
                  owner
                )

              case _ =>
                ()
            }

          // ------------------------------------------------------------
          // VAL / VAR
          // ------------------------------------------------------------

          case v: ValDef =>
            inspectValDef(v)

          case _ =>
            ()
        }

        super.traverseTree(tree)(owner)
      }
    }

    tastys.foreach { tasty =>
      Traverser.traverseTree(
        tasty.ast
      )(Symbol.noSymbol)
    }
  }

  // ==================================================================
  // CLASS / TRAIT / ENUM
  // ==================================================================

  private def inspectClassDef(using Quotes)(
      cls: quotes.reflect.ClassDef
  ): Unit = {

    import quotes.reflect.*

    val sym = cls.symbol

    val kind =
      if sym.flags.is(Flags.Enum) then
        "enum"
      else if sym.flags.is(Flags.Trait) then
        "trait"
      else
        "class"

    val m = baseMap(kind, sym)

    val parents = new ArrayList[String]()

    cls.parents.foreach { parent =>
      parents.add(
        safeTreeType(parent)
      )
    }

    m.put(
      "parents",
      parents
    )

    result.add(m)
  }

  // ==================================================================
  // METHOD / TOP-LEVEL FUNCTION
  // ==================================================================

  private def inspectDefDef(using Quotes)(
      d: quotes.reflect.DefDef
  ): Unit = {

    import quotes.reflect.*

    val sym = d.symbol

    // The compiler can emit implementation methods for lambdas.
    // They are filtered here because the source-level lambda is recorded
    // independently by the Lambda extractor.
    if !isSyntheticLambdaMethod(sym) then {

      val topLevel =
        isTopLevel(sym)

      val m =
        baseMap(
          if topLevel then
            "top-level-function"
          else
            "method",
          sym
        )

      m.put(
        "return-type",
        safeTypeRepr(
          d.returnTpt.tpe
        )
      )

      m.put(
        "parameters",
        parameters(d)
      )

      m.put(
        "top-level",
        Boolean.box(topLevel)
      )

      m.put(
        "has-body",
        Boolean.box(
          d.rhs.nonEmpty
        )
      )

      result.add(m)
    }
  }

  // ==================================================================
  // LAMBDA
  // ==================================================================

  private def inspectLambda(using Quotes)(
      params: List[quotes.reflect.ValDef],
      body: quotes.reflect.Term,
      lambdaBlock: quotes.reflect.Block,
      owner: quotes.reflect.Symbol
  ): Unit = {

    import quotes.reflect.*

    lambdaCounter += 1

    val m =
      new HashMap[String, Object]()

    m.put(
      "kind",
      "lambda"
    )

    m.put(
      "name",
      s"$$lambda$$$lambdaCounter"
    )

    m.put(
      "generated-name",
      Boolean.box(true)
    )

    m.put(
      "owner",
      safeFullName(owner)
    )

    val ps =
      new ArrayList[
        JMap[String, Object]
      ]()

    params.foreach { p =>

      val pm =
        new HashMap[
          String,
          Object
        ]()

      pm.put(
        "name",
        p.name
      )

      pm.put(
        "type",
        safeTypeRepr(
          p.tpt.tpe
        )
      )

      ps.add(pm)
    }

    m.put(
      "parameters",
      ps
    )

    m.put(
      "return-type",
      safeTypeRepr(
        body.tpe
      )
    )

    m.put(
      "type",
      safeTreeType(lambdaBlock)
    )

    result.add(m)
  }

  // ==================================================================
  // VAL / VAR
  // ==================================================================

  private def inspectValDef(using Quotes)(
      v: quotes.reflect.ValDef
  ): Unit = {

    import quotes.reflect.*

    val sym = v.symbol

    if !sym.flags.is(Flags.Synthetic) then {

      val kind =
        if sym.flags.is(Flags.Mutable) then
          "var"
        else
          "val"

      val m =
        baseMap(
          kind,
          sym
        )

      m.put(
        "type",
        safeTypeRepr(
          v.tpt.tpe
        )
      )

      m.put(
        "has-rhs",
        Boolean.box(
          v.rhs.nonEmpty
        )
      )

      result.add(m)
    }
  }

  // ==================================================================
  // COMMON MAP
  // ==================================================================

  private def baseMap(using Quotes)(
      kind: String,
      sym: quotes.reflect.Symbol
  ): JMap[String, Object] = {

    import quotes.reflect.*

    val m =
      new HashMap[String, Object]()

    m.put(
      "kind",
      kind
    )

    m.put(
      "name",
      safeName(sym)
    )

    m.put(
      "full-name",
      safeFullName(sym)
    )

    m.put(
      "owner",
      safeFullName(
        sym.owner
      )
    )

    m.put(
      "private",
      Boolean.box(
        sym.flags.is(
          Flags.Private
        )
      )
    )

    m.put(
      "protected",
      Boolean.box(
        sym.flags.is(
          Flags.Protected
        )
      )
    )

    m.put(
      "abstract",
      Boolean.box(
        sym.flags.is(
          Flags.Deferred
        )
      )
    )

    m.put(
      "final",
      Boolean.box(
        sym.flags.is(
          Flags.Final
        )
      )
    )

    m.put(
      "synthetic",
      Boolean.box(
        sym.flags.is(
          Flags.Synthetic
        )
      )
    )

    m
  }

  // ==================================================================
  // PARAMETERS
  //
  // Only stable TermParamClause functionality is used here.
  // No erased-parameter API is referenced.
  // ==================================================================

  private def parameters(using Quotes)(
      d: quotes.reflect.DefDef
  ): JList[JMap[String, Object]] = {

    import quotes.reflect.*

    val result =
      new ArrayList[
        JMap[String, Object]
      ]()

    d.paramss.foreach {

      case clause: TermParamClause =>

        clause.params.foreach { p =>

          val m =
            new HashMap[
              String,
              Object
            ]()

          m.put(
            "name",
            p.name
          )

          m.put(
            "type",
            safeTypeRepr(
              p.tpt.tpe
            )
          )

          m.put(
            "given",
            Boolean.box(
              clause.isGiven
            )
          )

          m.put(
            "implicit",
            Boolean.box(
              clause.isImplicit
            )
          )

          result.add(m)
        }

      case _ =>
        ()
    }

    result
  }

  // ==================================================================
  // CLASSIFICATION
  // ==================================================================

  private def isSyntheticLambdaMethod(using Quotes)(
      sym: quotes.reflect.Symbol
  ): Boolean = {

    import quotes.reflect.*

    sym.flags.is(
      Flags.Synthetic
    ) &&
    (
      sym.name.contains(
        "$anonfun"
      ) ||
      sym.name.contains(
        "$lambda"
      )
    )
  }

  private def isTopLevel(using Quotes)(
      sym: quotes.reflect.Symbol
  ): Boolean = {

    import quotes.reflect.*

    val owner =
      sym.owner

    if owner == Symbol.noSymbol then
      false
    else {

      val ownerName =
        safeFullName(owner)

      owner.flags.is(
        Flags.Package
      ) ||
      ownerName.endsWith(
        "$package"
      ) ||
      owner.name.endsWith(
        "$package"
      )
    }
  }

  // ==================================================================
  // SAFE SYMBOL ACCESS
  //
  // Symbol.info is intentionally not used.
  // ==================================================================

  private def safeName(using Quotes)(
      sym: quotes.reflect.Symbol
  ): String = {

    import quotes.reflect.*

    if sym == Symbol.noSymbol then
      ""
    else
      try
        sym.name
      catch
        case _: Throwable =>
          ""
  }

  private def safeFullName(using Quotes)(
      sym: quotes.reflect.Symbol
  ): String = {

    import quotes.reflect.*

    if sym == Symbol.noSymbol then
      ""
    else
      try
        sym.fullName
      catch
        case _: Throwable =>
          safeName(sym)
  }

  // ==================================================================
  // SAFE TYPE ACCESS
  //
  // Type information is obtained only from concrete trees:
  //   DefDef.returnTpt
  //   ValDef.tpt
  //   parameter ValDef.tpt
  //   Term.tpe
  //   TypeTree.tpe
  //
  // Generic Tree.tpe and Symbol.info are not used.
  // ==================================================================

  private def safeTypeRepr(using Quotes)(
      tpe: quotes.reflect.TypeRepr
  ): String = {

    try
      tpe.show
    catch
      case _: Throwable =>
        ""
  }

  private def safeTreeType(using Quotes)(
      tree: quotes.reflect.Tree
  ): String = {

    import quotes.reflect.*

    tree match {

      case term: Term =>
        safeTypeRepr(
          term.tpe
        )

      case typeTree: TypeTree =>
        safeTypeRepr(
          typeTree.tpe
        )

      case _ =>
        ""
    }
  }
}
