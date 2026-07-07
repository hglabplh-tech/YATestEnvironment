/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class AnnotTestClass extends AnnotTestBase implements HumanResource {

    @CheckForNull
    private final Category theCategory;

    @CheckForNull
    private final EmployeeType emplType;

    @CheckForNull
    private final String name;

    public AnnotTestClass(@Nonnull Category category,
                          @Nonnull EmployeeType emplType, @Nonnull String name) {
        this.theCategory = category;
        this.emplType = emplType;
        this.name = name;
    }

    @SuppressWarnings({"test", "is", "well"})
    public Category theCategory() {
        return theCategory;
    }

    @io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.TransportSpec(transportType = io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.TransportSpec.TransportType.PIPE,
    sourceType = Category.class, targetType = StringBuilder.class)
    public EmployeeType emplType() {
        return emplType;
    }

    @io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.TransportSpec(transportType = TransportSpec.TransportType.HTTP)
    public String name() {
        return name;
    }
}
