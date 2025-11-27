package io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.getArrayAsLazyVector;
/**
 * This is a utility class for getting the Annotations belonging to a
 * class method field
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class AnnotationsUtil {

    /**
     * get all annotations belonging to a class
     * @param clazz the class to analyze
     * @return a vector of annotation classes
     */
    public static IPersistentVector getClassAnnots(Class<?> clazz) {
        return getArrayAsLazyVector(clazz.getAnnotations());
    }

    /**
     * get all annotations belonging to a field
     * @param field the field to analyze
     * @return a vector of annotation classes
     */
    public static IPersistentVector getFieldAnnots(Field field) {
        return getArrayAsLazyVector(field.getAnnotations());
    }

    /**
     * get all annotations belonging to a method
     * @param meth the method to analyze
     * @return a vector of annotation classes
     */
    public static IPersistentVector getMethodAnnots(Method meth) {
        return getArrayAsLazyVector(meth.getAnnotations());
    }

    /**
     * get the annotated return-type of a method
     * @param meth the method to analyze
     * @return a vector of annotation classes
     */
    public static AnnotatedType getAnnotReturnType (Method meth) {
        return meth.getAnnotatedReturnType();

    }

    /**
     * get the annotations of the parameters of a method
     * @param meth the method to analyze
     * @return a map containing annotations by parameter
     */
    public static IPersistentMap getMethParamAnnots(Method meth) {
        Object[][] array =
                meth.getParameterAnnotations();
        return getParamAnnotsInternal(array);
    }

    /**
     * this function gets the annotations of a constructor
     * @param ctor the constructor to analyze
     * @return
     */
    public static IPersistentVector getCtorAnnots(Constructor<?> ctor){
        return getArrayAsLazyVector(ctor.getAnnotations());
    }



    /**
     * get the annotations of the parameters of a constructor
     * @param ctor the constructor to analyze
     * @return a map containing annotations by parameter
     */
    public static IPersistentMap getCtorParamAnnots(Constructor<?> ctor) {
        Object[][] array =
                ctor.getParameterAnnotations();
        return getParamAnnotsInternal(array);
    }

    /**
     * Here the annotations for parameters are detected and sorted out
     * @param array the 2D Array of the annotations of the parameters
     * @return the map containing the annotations per parameter
     */
    private static IPersistentMap getParamAnnotsInternal (Object[][] array ) {
        IPersistentMap resultMap =
                PersistentArrayMap.create(PersistentArrayMap.EMPTY);

        for (int i = 0; i < array.length; i++) {
            Object obj = array[i];
            IPersistentVector innerVect = LazilyPersistentVector.createOwning();
            Annotation[] objects = (Annotation[]) obj;
            for (Annotation objInner : objects) {
                RT.conj(innerVect, objInner);
            }
            resultMap.assocEx(i, innerVect);
        }

        return  resultMap;
    }


}
