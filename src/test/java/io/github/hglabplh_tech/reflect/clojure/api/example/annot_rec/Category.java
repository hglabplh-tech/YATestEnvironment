package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

public enum Category {
    TOOLS("tools", "develop"),
    APIS("apis", "design"),
    TESTS("tests", "support"),
    PROJECT("project", "architecture board"),
    NONE("none","none"),;

    private final String name;

    private final String department;

    Category(String name, String department) {
        this.name = name;
        this.department = department;
    }


    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public static Category findByName(String theName) {
        for (Category actVal : Category.values()){
            if (actVal.getName().equals(theName)) {
                return actVal;
            }
        }
        return Category.NONE;
    }

}
