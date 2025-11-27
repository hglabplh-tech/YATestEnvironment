package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MethodDescriber {
    Category category() default Category.APIS;
    String shortDescription() default "::to-describe::";
    String [] execSteps()  default {};

}
