/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package java.io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.IPersistentVector;

import javax.annotation.Nonnull;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.getArrayAsLazyVector;


/**
 * This s a utility class for conversion from Java Types to  clojure vectors
 * or maps representing the content
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class FieldsUtil {

    /**
     * Private constructor for all methods are static
     */
    private FieldsUtil () {

    }

    /**
     * Get the fields name
     * @param field the field
     * @return the field name
     */
    public static @Nonnull String getFieldName (
            @Nonnull Field field) {
        return field.getName();
    }

    /**
     * Get the type of the field as Class
     * @param field the field
     * @return the class representing the field type
     */
    public static @Nonnull Class<?> getFieldType (@Nonnull Field field) {
        return field.getType();
    }

    /**
     * Get the field modifiers integer
     * @param field the field
     * @return the field modifiers integer
     */
    public static @Nonnull Integer getFieldModifier (@Nonnull Field field) {
        return field.getModifiers();
    }

    /**
     * Get all annotations of a field
     * @param field the field
     * @return all annotations of the field
     */
    public static @Nonnull IPersistentVector getAllAnnotations(@Nonnull Field field) {
        return getArrayAsLazyVector(field.getDeclaredAnnotations());
    }

    /**
     * Get the generic type of the field
     * @param field the field
     * @return the generic type
     */
    public static @Nonnull Type getGenericType(@Nonnull Field field) {
        return field.getGenericType();
    }


    /**
     * Get the annotated type of the field
     * @param field the field
     * @return the annotated type
     */
    public static @Nonnull AnnotatedType getAnnotType(@Nonnull Field field) {
        return field.getAnnotatedType();
    }

    /**
     * Ask if the field is synthetic
     * @param field the field
     * @return true if it is synthetic / false otherwise
     */
    public static boolean isSynthetic(@Nonnull Field field) {
        return field.isSynthetic();
    }

    /**
     * Ask if the field is a enum constant
     * @param field the field
     * @return true if it is a enum contant / false otherwise
     */
    public static boolean isEnumConst(@Nonnull Field field) {
        return field.isEnumConstant();
    }




}
