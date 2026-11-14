package io.github.hglabplh_tech.reflect.java.api.utils;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ControlFlowAstLinter {

    /**
     * Analyzes a Java file using configuration loaded via LinterConfigLoader
     * (external env/system property -> classpath linter.properties -> defaults).
     */
    public static List<String> analyzeFile(Path sourceFile) throws Exception {
        return analyzeFile(sourceFile, LinterConfigLoader.load());
    }

    /**
     * Analyzes a Java file with an explicit LinterConfig instance.
     */
    public static List<String> analyzeFile(Path sourceFile, LinterConfig config) throws Exception {
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        CompilationUnit cu = StaticJavaParser.parse(sourceFile);
        List<String> violations = new ArrayList<>();

        analyzeCyclomaticComplexity(cu, config, violations);
        analyzeJumpStatements(cu, config, violations);
        analyzeSwitches(cu, violations);
        analyzeIfNesting(cu, config, violations);

        return violations;
    }


    // =========================================================================
    // 1. CYCLOMATIC COMPLEXITY PER METHOD / CONSTRUCTOR
    // =========================================================================

    private static void analyzeCyclomaticComplexity(CompilationUnit cu, LinterConfig config, List<String> violations) {
        List<CallableDeclaration<?>> callables = new ArrayList<>();
        callables.addAll(cu.findAll(MethodDeclaration.class));
        callables.addAll(cu.findAll(ConstructorDeclaration.class));

        for (CallableDeclaration<?> callable : callables) {
            int complexity = calculateCyclomaticComplexity(callable);
            if (complexity > config.maxCyclomaticComplexity()) {
                violations.add(String.format(
                        "Line %d: '%s' has high Cyclomatic Complexity (%d > %d). Split into smaller focused methods.",
                        getLine(callable),
                        callable.getDeclarationAsString(false, false, false),
                        complexity,
                        config.maxCyclomaticComplexity()
                ));
            }
        }
    }

    public static int calculateCyclomaticComplexity(CallableDeclaration<?> callable) {
        int complexity = 1;

        complexity += callable.findAll(IfStmt.class).size();
        complexity += callable.findAll(ForStmt.class).size();
        complexity += callable.findAll(ForEachStmt.class).size();
        complexity += callable.findAll(WhileStmt.class).size();
        complexity += callable.findAll(DoStmt.class).size();

        long caseBranches = callable.findAll(SwitchEntry.class).stream()
                .filter(entry -> !entry.getLabels().isEmpty() && !entry.isDefault())
                .count();
        complexity += (int) caseBranches;

        complexity += callable.findAll(ConditionalExpr.class).size();
        complexity += callable.findAll(CatchClause.class).size();

        long booleanBranches = callable.findAll(BinaryExpr.class).stream()
                .filter(bin -> bin.getOperator() == BinaryExpr.Operator.AND
                        || bin.getOperator() == BinaryExpr.Operator.OR)
                .count();
        complexity += (int) booleanBranches;

        return complexity;
    }

    // =========================================================================
    // 2. JUMP ANALYSIS (Labeled & Deeply Buried break / continue)
    // =========================================================================

    private static void analyzeJumpStatements(CompilationUnit cu, LinterConfig config, List<String> violations) {
        cu.findAll(BreakStmt.class).forEach(brk -> {
            int line = getLine(brk);
            brk.getLabel().ifPresent(label -> violations.add(String.format(
                    "Line %d: Labeled 'break %s;' acts like a goto. Extract nested loop into a helper method.",
                    line, label.asString()
            )));

            int depth = calculateControlNestingDepth(brk);
            if (depth > config.maxJumpDepth()) {
                violations.add(String.format(
                        "Line %d: Deeply nested 'break' (depth %d) obscures loop exit flow.",
                        line, depth
                ));
            }
        });

        cu.findAll(ContinueStmt.class).forEach(cont -> {
            int line = getLine(cont);
            cont.getLabel().ifPresent(label -> violations.add(String.format(
                    "Line %d: Labeled 'continue %s;' creates confusing multi-loop jumps.",
                    line, label.asString()
            )));

            int depth = calculateControlNestingDepth(cont);
            if (depth > config.maxJumpDepth()) {
                violations.add(String.format(
                        "Line %d: Deeply nested 'continue' (depth %d) risks silently skipping end-of-loop logic.",
                        line, depth
                ));
            }
        });
    }

    // =========================================================================
    // 3. SWITCH ANALYSIS (Fallthrough, Missing Default, Nested Switches)
    // =========================================================================

    private static void analyzeSwitches(CompilationUnit cu, List<String> violations) {
        cu.findAll(SwitchStmt.class).forEach(sw -> {
            int line = getLine(sw);
            analyzeSwitchEntries(sw.getEntries(), line, true, violations);

            if (isNestedSwitch(sw)) {
                violations.add(String.format(
                        "Line %d: Nested 'switch' statement detected. Extract inner switch to a separate method.",
                        line
                ));
            }
        });

        cu.findAll(SwitchExpr.class).forEach(swExpr -> {
            int line = getLine(swExpr);
            analyzeSwitchEntries(swExpr.getEntries(), line, false, violations);

            if (isNestedSwitch(swExpr)) {
                violations.add(String.format(
                        "Line %d: Nested 'switch' expression detected. Extract inner switch to a separate method.",
                        line
                ));
            }
        });
    }

    private static boolean isNestedSwitch(Node switchNode) {
        return switchNode.findAncestor(SwitchStmt.class).isPresent()
                || switchNode.findAncestor(SwitchExpr.class).isPresent();
    }

    private static void analyzeSwitchEntries(
            List<SwitchEntry> entries,
            int switchLine,
            boolean isStatement,
            List<String> violations) {

        boolean hasDefault = false;

        for (int i = 0; i < entries.size(); i++) {
            SwitchEntry entry = entries.get(i);
            int entryLine = getLine(entry);

            if (entry.getLabels().isEmpty() || entry.isDefault()) {
                hasDefault = true;
            }

            if (entry.getType() == SwitchEntry.Type.STATEMENT_GROUP) {
                List<Statement> stmts = entry.getStatements();
                if (!stmts.isEmpty()) {
                    Statement lastStmt = stmts.get(stmts.size() - 1);
                    boolean isLastCase = (i == entries.size() - 1);

                    if (!isLastCase && !terminatesControlFlow(lastStmt)) {
                        violations.add(String.format(
                                "Line %d: Accidental fallthrough in classic 'switch' case (missing 'break', 'return', or 'throw'). Consider modern '->' syntax.",
                                entryLine
                        ));
                    }
                }
            }
        }

        if (isStatement && !hasDefault) {
            violations.add(String.format(
                    "Line %d: 'switch' statement is missing a 'default' branch, risking silent unhandled cases.",
                    switchLine
            ));
        }
    }

    private static boolean terminatesControlFlow(Statement stmt) {
        return stmt instanceof BreakStmt
                || stmt instanceof ReturnStmt
                || stmt instanceof ThrowStmt
                || stmt instanceof ContinueStmt;
    }

    // =========================================================================
    // 4. IF-THEN-ELSE NESTING & LONG ELSE-IF CHAIN ANALYSIS
    // =========================================================================

    private static void analyzeIfNesting(CompilationUnit cu, LinterConfig config, List<String> violations) {
        cu.findAll(IfStmt.class).forEach(ifStmt -> {
            if (isPartOfElseIfChain(ifStmt)) {
                return;
            }

            int line = getLine(ifStmt);

            int trueNestingDepth = calculateTrueIfNestingDepth(ifStmt);
            if (trueNestingDepth > config.maxElseIfChainLength()) {
                violations.add(String.format(
                        "Line %d: Extensive 'if-then-else' nesting (depth %d > %d). Refactor using guard clauses or extract methods.",
                        line, trueNestingDepth, config.maxIfNestingDepth()
                ));
            }
            int chainLength = calculateElseIfChainLength(ifStmt);
            if (chainLength > config.maxElseIfChainLength()) {
                violations.add(String.format(
                        "Line %d: Long 'else-if' chain (%d branches). Consider replacing with a modern 'switch' expression or Strategy pattern.",
                        line, chainLength
                ));
            }
        });
    }
    private static boolean isPartOfElseIfChain(IfStmt ifStmt) {
        return ifStmt.getParentNode()
                .filter(parent -> parent instanceof IfStmt)
                .map(parent -> ((IfStmt) parent).getElseStmt().orElse(null) == ifStmt)
                .orElse(false);
    }
    private static int calculateTrueIfNestingDepth(IfStmt targetIf) {
        int depth = 1;
        Node current = targetIf;
        Node parent = current.getParentNode().orElse(null);
        while (parent != null) {
            if (parent instanceof IfStmt parentIf && parentIf.getElseStmt().orElse(null) != current) {
                depth++;
            }
            current = parent;
            parent = current.getParentNode().orElse(null);
        }
        return depth;
    }
    private static int calculateElseIfChainLength(IfStmt rootIf) {
        int count = 1;
        Statement elseBranch = rootIf.getElseStmt().orElse(null);
        while (elseBranch instanceof IfStmt nextIf) {
            count++;
            elseBranch = nextIf.getElseStmt().orElse(null);
        }
        return count;
    }
    // =========================================================================
// SHARED HELPERS
// =========================================================================
    private static int calculateControlNestingDepth(Node jumpNode) {
        int depth = 0;
        Node current = jumpNode.getParentNode().orElse(null);
        while (current != null) {
            if (current instanceof IfStmt || current instanceof SwitchStmt || current instanceof SwitchExpr) {
                depth++;
            } else if (isLoopNode(current)) {
                break;
            }
            current = current.getParentNode().orElse(null);
        }
        return depth;
    }
    private static boolean isLoopNode(Node node) {
        return node instanceof ForStmt
                || node instanceof ForEachStmt
                || node instanceof WhileStmt
                || node instanceof DoStmt;
    }
    private static int getLine(Node node) {
        return node.getBegin().map(p -> p.line).orElse(-1);
    }
}

