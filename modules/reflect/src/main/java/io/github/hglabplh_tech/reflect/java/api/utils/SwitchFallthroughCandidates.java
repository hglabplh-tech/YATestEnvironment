package io.github.hglabplh_tech.reflect.java.api.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.nio.file.*;
import java.util.*;

/** Bytecode evidence only: each result is UNKNOWN until source/policy confirms it. */
public final class SwitchFallthroughCandidates implements Opcodes {
    public record Candidate(String method, int switchInstruction,
                            int fromEntry, int toEntry, int boundaryInstruction) {}

    public static List<Candidate> inspect(byte[] bytecode) {
        ClassNode owner = new ClassNode();
        new ClassReader(bytecode).accept(owner, ClassReader.SKIP_FRAMES);
        List<Candidate> findings = new ArrayList<>();
        for (MethodNode method : owner.methods) {
            List<AbstractInsnNode> code = new ArrayList<>();
            IdentityHashMap<AbstractInsnNode, Integer> positions = new IdentityHashMap<>();
            for (AbstractInsnNode node : method.instructions) {
                if (node.getOpcode() >= 0) {
                    positions.put(node, code.size());
                    code.add(node);
                }
            }
            // JSR/RET need subroutine expansion before this simplified analysis.
            if (code.stream().anyMatch(n -> n.getOpcode() == JSR || n.getOpcode() == RET)) {
                throw new UnsupportedOperationException("Expand legacy JSR/RET: " + method.name);
            }
            for (int dispatch = 0; dispatch < code.size(); dispatch++) {
                AbstractInsnNode instruction = code.get(dispatch);
                Set<Integer> entries = new TreeSet<>();
                if (instruction instanceof TableSwitchInsnNode s) {
                    entries.add(target(s.dflt, positions));
                    for (LabelNode label : s.labels) entries.add(target(label, positions));
                } else if (instruction instanceof LookupSwitchInsnNode s) {
                    entries.add(target(s.dflt, positions));
                    for (LabelNode label : s.labels) entries.add(target(label, positions));
                } else continue;

                // Labels sharing the same executable target are grouped, not fallthrough.
                for (int entry : entries) {
                    BitSet seen = new BitSet(code.size());
                    Deque<Integer> work = new ArrayDeque<>();
                    work.add(entry);
                    while (!work.isEmpty()) {
                        int at = work.removeFirst();
                        if (at < 0 || at >= code.size() || seen.get(at)) continue;
                        if (at == dispatch || (at != entry && entries.contains(at))) continue;
                        seen.set(at);
                        AbstractInsnNode current = code.get(at);
                        for (int next : successors(current, at, code.size(), positions)) {
                            if (next != entry && entries.contains(next)) {
                                // Require a sequential boundary crossing, not merely a GOTO.
                                if (next == at + 1 && completesSequentially(current)) {
                                    findings.add(new Candidate(owner.name + "." + method.name
                                            + method.desc, dispatch, entry, next, at));
                                }
                            } else work.add(next);
                        }
                    }
                }
            }
        }
        return findings;
    }

    private static int target(LabelNode label,
                              IdentityHashMap<AbstractInsnNode, Integer> positions) {
        AbstractInsnNode node = label;
        while (node != null && node.getOpcode() < 0) node = node.getNext();
        Integer index = positions.get(node);
        if (index == null) throw new IllegalArgumentException("Invalid executable target");
        return index;
    }

    private static boolean completesSequentially(AbstractInsnNode node) {
        int op = node.getOpcode();
        return op != GOTO && op != JSR && op != RET && op != ATHROW
                && !(op >= IRETURN && op <= RETURN)
                && !(node instanceof TableSwitchInsnNode)
                && !(node instanceof LookupSwitchInsnNode);
    }

    private static List<Integer> successors(AbstractInsnNode node, int at, int size,
                             IdentityHashMap<AbstractInsnNode, Integer> positions) {
        List<Integer> result = new ArrayList<>();
        if (node instanceof JumpInsnNode jump) {
            result.add(target(jump.label, positions));
        } else if (node instanceof TableSwitchInsnNode s) {
            result.add(target(s.dflt, positions));
            for (LabelNode label : s.labels) result.add(target(label, positions));
        } else if (node instanceof LookupSwitchInsnNode s) {
            result.add(target(s.dflt, positions));
            for (LabelNode label : s.labels) result.add(target(label, positions));
        }
        if (completesSequentially(node) && at + 1 < size) result.add(at + 1);
        return result;
    }

    public static void main(String[] args) throws Exception {
        for (Candidate candidate : inspect(Files.readAllBytes(Path.of(args[0])))) {
            System.out.println("COMMON.SWITCH_FALLTHROUGH UNKNOWN " + candidate);
        }
    }
}
