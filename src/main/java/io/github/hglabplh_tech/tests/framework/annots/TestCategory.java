package io.github.hglabplh_tech.tests.framework.annots;

public enum TestCategory {
    UNIT_TEST("unit-test"),
    FUN_TEST("fun-test"),
    SMOKE_TEST("smoke-test"),
    BLACKBOX("blackbox-test"),
    WHITEBOX("whitebox-test"),
    ;

    private final String categoryName;
    private final String userDef;

    TestCategory(String name, String userDef) {
        this.categoryName = name;
        this.userDef = userDef;
    }

    public TestCategory categoryName() {
        return TestCategory.valueOf(this.categoryName);
    }

    public String userDef() {
        return this.userDef;
    }
}
