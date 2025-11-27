package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TransportSpec {
    Class<?> sourceType() default String.class;
    Class<?> targetType() default String.class;
    TransportType transportType() default TransportType.HTTPS;

    enum TransportType {
        HTTP,
        HTTPS,
        PIPE,;

    }
}
