/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package io.github.hglabplh_tech.reflect.java.api.example.annot_rec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MethodDescriberBad {
    Category category() default Category.APIS;

    String shortDescription() default "::to-describe::";

    String[] execSteps() default {};

    public class BadIdea {
        int counter = 0;

        BadIdea(int count) {
            this.counter = count;
        }

        public int increment() {
            return this.counter++;
        }

        public int decrement() {
            return this.counter--;
        }

        public int counter() {
            return this.counter;
        }
    }
}
