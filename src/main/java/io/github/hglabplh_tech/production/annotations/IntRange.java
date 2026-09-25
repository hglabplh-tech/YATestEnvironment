package io.github.hglabplh_tech.production.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.interfaces.ECKey;

@Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
    int from() default Integer.MIN_VALUE;
    int to() default Integer.MAX_VALUE;
}
