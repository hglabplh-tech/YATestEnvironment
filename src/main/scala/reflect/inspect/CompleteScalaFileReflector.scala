package reflect.inspect

/**
 * Target definitions to be scanned by our reflection engine
 */
@deprecated("Use CloudNode instead", "v2.0")
class DataEngine(var engineId: String, val clusterSize: Int) {
  def computeWeights(factor: Double, metrics: List[Long]): Map[String, Double] = {
    Map("efficiency" -> (factor * clusterSize))
  }
}

object DataEngine {
  val GlobalVersion: String = "4.0.0-RC1"
  var currentLoad: Double = 0.42
}

/**
 * The Reflection Tool Engine using Scala 3 TASTy Inspectors
 */
object CompleteScalaFileReflector extends TastyInspector {

  def main(args: Array[String]): Unit = {
    // In a production environment, point this to your compiled .tasty or .class directory
    val classpath = List(System.getProperty("java.class.path"))
    val tastyFiles = List("target/scala-3.3.0/classes/com/analyzer/DataEngine.tasty")
    
    println("=== Starting Full File Structural Reflection Pipeline ===")
    // Try-catch block to prevent crash if running as a standalone script without target paths compiled
    try {
      inspectTastyFiles(classpath, tastyFiles, Nil)
    } catch {
      case e: Exception => 
        println(s"INFO: Provide valid compiled target paths to execute on raw bytecode: ${e.getMessage}")
    }
  }

  /**
   * Universal AST processing hook that traverses the entire structural matrix of the file
   */
  override def processCompilationUnit(using: Quotes)(root: quotes.reflect.Tree): Unit = {

    val fileTraverser = new TreeAccumulator[Unit] {
      override def foldTree(x: Unit, tree: Tree)(using: Quotes): Unit = {
        tree match {

          // 1. Inspect Classes and Objects
          case ClassDef(className, constructor, parents, selfOpt, body) =>
            if (!className.contains("$")) {
              val structureType = if (tree.symbol.flags.is(Flags.Module)) "OBJECT (Singleton)" else "CLASS"
              println(s"\n[Found $structureType Structure]: $className")
              
              // Extract Class Level Annotations
              val annotations = tree.symbol.annotations.map(_.show)
              if (annotations.nonEmpty) {
                println(s"   Annotations: ${annotations.mkString(", ")}")
              }
            }
            super.foldTree(x, tree)

          // 2. Inspect Variables, Constants, and Fields (ValDef)
          case ValDef(fieldName, typeTree, rhsOpt) =>
            if (!fieldName.contains("$") && fieldName != "MODULE$") {
              val isMutable = tree.symbol.flags.is(Flags.Mutable)
              val keyword = if (isMutable) "var" else "val"
              println(s"   -> Field definition: $keyword $fieldName: ${typeTree.show}")
            }
            super.foldTree(x, tree)

          // 3. Inspect Functions and Methods (DefDef)
          case DefDef(functionName, paramLists, returnTypeTree, rhsOpt) =>
            val excludedMethods = List("<init>", "hashCode", "toString", "equals")
            if (!functionName.contains("$") && !excludedMethods.contains(functionName)) {
              
              // Parse complex method signature arguments containing generic collections
              val parametersText = paramLists.map {
                case TermParamClause(params) => 
                  params.map(p => s"${p.name}: ${p.tpt.show}").mkString(", ")
                case TypeParamClause(tparams) => 
                  tparams.map(_.name).mkString("[", ", ", "]")
              }.mkString(")(")

              println(s"   -> Function definition: def $functionName($parametersText): ${returnTypeTree.show}")
            }
            super.foldTree(x, tree)

          case _ =>
            super.foldTree(x, tree)
        }
      }
    }
    
    fileTraverser.foldTree((), root)
  }
}
