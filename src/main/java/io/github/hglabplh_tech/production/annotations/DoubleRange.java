package io.github.hglabplh_tech.production.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
    double from() default Double.MIN_VALUE;
    double to() default Double.MAX_VALUE;
}
