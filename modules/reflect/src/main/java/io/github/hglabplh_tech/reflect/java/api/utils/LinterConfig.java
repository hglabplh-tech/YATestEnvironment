package io.github.hglabplh_tech.reflect.java.api.utils;

public record LinterConfig(
        int maxJumpDepth,
        int maxIfNestingDepth,
        int maxElseIfChainLength,
        int maxCyclomaticComplexity) {

    public static LinterConfig defaults() {
        return new LinterConfig(2, 3, 4, 8);
    }
}

