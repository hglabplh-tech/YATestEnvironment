package io.github.hglabplh_tech.reflect.java.api.utils;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.file.*;
import java.util.*;

/** Exact source nesting for method/constructor bodies; lambdas are separate scopes. */
public final class ConditionalNesting {
    private static final class Depth extends TreeScanner<Void, Integer> {
        int maximum;
        Tree deepest;
        @Override public Void visitIf(IfTree tree, Integer enclosing) {
            int depth = enclosing + 1;
            if (depth > maximum) { maximum = depth; deepest = tree; }
            scan(tree.getCondition(), enclosing);
            scan(tree.getThenStatement(), depth);
            // A syntactic else-if chain stays at the same conditional level.
            scan(tree.getElseStatement(), tree.getElseStatement() instanceof IfTree
                    ? enclosing : depth);
            return null;
        }
        @Override public Void visitClass(ClassTree tree, Integer depth) {
            return null; // Another class is a separate scope.
        }
        @Override public Void visitLambdaExpression(LambdaExpressionTree tree, Integer depth) {
            return null; // Inspected independently by the outer scanner.
        }
    }

    public static void main(String[] args) throws Exception {
        int limit = Integer.parseInt(args[0]);
        if (limit < 0) throw new IllegalArgumentException("Negative nesting limit");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("Run with a full JDK");
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(diagnostics, null, null)) {
            var sources = files.getJavaFileObjectsFromPaths(List.of(Path.of(args[1])));
            JavacTask task = (JavacTask) compiler.getTask(null, files, diagnostics,
                    List.of("-proc:none"), null, sources);
            List<CompilationUnitTree> units = new ArrayList<>();
            task.parse().forEach(units::add);
            if (diagnostics.getDiagnostics().stream()
                    .anyMatch(d -> d.getKind() == Diagnostic.Kind.ERROR)) {
                throw new IllegalArgumentException("Parse failed: " + diagnostics.getDiagnostics());
            }
            Trees trees = Trees.instance(task);
            for (CompilationUnitTree unit : units) {
                new TreeScanner<Void, Void>() {
                    void check(Tree body, String scope) {
                        Depth depth = new Depth();
                        depth.scan(body, 0);
                        if (depth.maximum > limit) {
                            long position = trees.getSourcePositions()
                                    .getStartPosition(unit, depth.deepest);
                            long line = unit.getLineMap().getLineNumber(position);
                            System.out.printf("COMMON.MAX_CONDITIONAL_NESTING FAIL %s:%d "
                                    + "%s depth=%d limit=%d%n", unit.getSourceFile().getName(),
                                    line, scope, depth.maximum, limit);
                        }
                    }
                    @Override public Void visitMethod(MethodTree tree, Void unused) {
                        check(tree.getBody(), tree.getName().toString());
                        return super.visitMethod(tree, unused);
                    }
                    @Override public Void visitLambdaExpression(LambdaExpressionTree tree, Void unused) {
                        check(tree.getBody(), "lambda");
                        return super.visitLambdaExpression(tree, unused);
                    }
                    @Override public Void visitClass(ClassTree tree, Void unused) {
                        for (Tree member : tree.getMembers()) {
                            if (member instanceof BlockTree block) check(block, "initializer");
                        }
                        return super.visitClass(tree, unused);
                    }
                }.scan(unit, null);
            }
        }
    }
}
