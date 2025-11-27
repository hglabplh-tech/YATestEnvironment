package io.github.hglabplh_tech.tests.framework.annots;

public enum TestCategory {
    UNIT_TEST("unit-test"),
    FUN_TEST("fun-test"),
    SMOKE_TEST("smoke-test"),
    BLACKBOX("blackbox-test"),
    WHITEBOX("whitebox-test"),
    ;

    private final String categoryName;

    TestCategory(String name) {
        this.categoryName = name;
    }

    public String categoryName() {
        return this.categoryName;
    }
}
