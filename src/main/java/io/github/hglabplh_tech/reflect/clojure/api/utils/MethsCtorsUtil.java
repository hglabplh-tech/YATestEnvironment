package io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.IPersistentVector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.getArrayAsLazyVector;

/**
 * This s a utility class for getting the definition data of
 * either a constructor or a method
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class MethsCtorsUtil {

    /**
     * private constructor all methods are static
     */
    private MethsCtorsUtil () {

    }
    /**
     * Get the constructors name
     * @param ctor the constructor definition
     * @return the name of the constructor
     */
    public static  String getCtorName (
           Constructor<?> ctor) {
        return ctor.getName();
    }

    /**
     * Get the methods name
     * @param meth the method definition
     * @return the name of the method
     */
    public static  String getMethodName (
            Method meth) {
        return meth.getName();
    }

    /**
     * Get the constructors parameter count
     * @param ctor the constructor definition
     * @return the parameter count of the constructor
     */
    public static  Integer getCtorParamCount (
            Constructor<?> ctor) {
        return ctor.getParameterCount();
    }

    /**
     * Get the constructors parameter types
     * @param ctor the constructor definition
     * @return the parameter types of the constructor
     */
    public static  IPersistentVector getCtorParamTypes(Constructor<?> ctor) {
        return getArrayAsLazyVector(ctor.getParameterTypes());
    }

    /**
     * Get the constructors modifiers integer
     * @param ctor the constructor definition
     * @return the modifiers integer of the constructor
     */
    public static Integer getCtorModifiers(Constructor<?> ctor) {
        return ctor.getModifiers();
    }

    /**
     * Get the constructors declaring class
     * @param ctor the constructor definition
     * @return the declatring class of the constructor
     */
    public static  Class<?> getCtorDeclaringClass(Constructor<?> ctor){
        return ctor.getDeclaringClass();
    }

    /**
     * Get the methods parameter count
     * @param method the method definition
     * @return the methods parameter count
     */
    public static Integer getMethodParamCount (
            Method method) {
        return method.getParameterCount();
    }

    /**
     * Get the methods parameter types
     * @param method the method definition
     * @return the methods parameter types
     */
    public static  IPersistentVector getMethodParamTypes (
             Method method) {
        return getArrayAsLazyVector(method.getParameterTypes());
    }

    /**
     * Get the methods return type
     * @param method the method definition
     * @return the methods return type
     */
    public static  Class<?> getMethodReturnType (Method method) {
        return method.getReturnType();
    }

    /**
     * Get the methods modifiers integer
     * @param method the method definition
     * @return the methods modifiers integer
     */
    public static  Integer getMethodModifiers (
                                                        Method method) {
        return method.getModifiers();
    }

    /**
     * Get the methods exception types
     * @param method the method definition
     * @return the methods exception type
     */
    public static  IPersistentVector getMethExceptionTypes(Method method) {
        return getArrayAsLazyVector(method.getExceptionTypes());
    }

    /**
     * Get the methods generic exception types
     * @param method the method definition
     * @return the methods generic exception types
     */
    public static  IPersistentVector getMethGenericExTypes(Method method) {
        return getArrayAsLazyVector(method.getGenericExceptionTypes());
    }

    /**
     * Get the methods generic parameter types
     * @param method the method definition
     * @return the methods generic parameter types
     */
    public static  IPersistentVector getMethGenericParmTypes(Method method){
        return getArrayAsLazyVector(method.getGenericParameterTypes());
    }

    /**
     * Get the methods generic return type
     * @param method the method definition
     * @return the methods generic return type
     */
    public static Type getMethGenericReturnType(Method method){
        return method.getGenericReturnType();
    }

    /**
     * Get the methods declaring class
     * @param method the method definition
     * @return the methods declaring class
     */
    public static Class<?> getMethDeclaringClass(Method method){
        return method.getDeclaringClass();
    }

    /**
     * Get info if method is a bridge true if yes / false otherwise
     * @param method the method definition
     * @return true = yes / false = no
     */
    public static  Boolean isBridgeMethod(Method method){
        return method.isBridge();
    }

    /**
     * Get info if method is a default method true if yes / false otherwise
     * @param method the method definition
     * @return true = yes / false = no
     */
    public static  Boolean isDefaultMethod(Method method){
        return method.isDefault();
    }

    /**
     * Get info if method is synthetic true if yes / false otherwise
     * @param method the method definition
     * @return true = yes / false = no
     */
    public static  Boolean isSyntheticMethod(Method method){
        return method.isSynthetic();
    }

    /**
     * Get info if method is a varargs method -  true if yes / false otherwise
     * @param method the method definition
     * @return true = yes / false = no
     */
    public static  Boolean isVarArgsMethod(Method method){
        return method.isVarArgs();
    }

    /**
     * Get info if a constructor is snthetic true if yes / false otherwise
     * @param ctor the constructor definition
     * @return true = yes / false = no
     */
    public static  Boolean isSyntheticCtor(Constructor<?> ctor){
        return ctor.isSynthetic();
    }

    /**
     * Get info if a constructor is with varargs true if yes / false otherwise
     * @param ctor the constructor definition
     * @return true = yes / false = no
     */
    public static  Boolean isVarArgsCtor(Constructor<?> ctor){
        return ctor.isVarArgs();
    }
    /**
     * Get info if a constructor has a annotation with type x true if yes / false otherwise
     * @param ctor the constructor definition
     * @param annotation the annotations class
     * @return true = yes / false = no
     */
    public static  Boolean isAnnotPresentCtor(Constructor<?> ctor,
                                              Class<? extends Annotation>
                                                      annotation){
        return ctor.isAnnotationPresent(annotation);
    }

    /**
     * Get info if a method has a annotation with type x  true if yes / false otherwise
     * @param meth the constructor definition
     * @param annotation the annotations class
     * @return true = yes / false = no
     */
    public static  boolean isAnnotPresentMethod(Method meth,
                                              Class<? extends Annotation>
                                                      annotation){
        return meth.isAnnotationPresent(annotation);
    }


    /**
     * Get the exception types of a constructor
     * @param ctor the constructor definition
     * @return the exception types
     */
    public static  IPersistentVector
    getCtorExceptionTypes(Constructor<?> ctor) {
        return getArrayAsLazyVector(ctor.getExceptionTypes());
    }

    /**
     * Get the generic exception types of a constructor
     * @param ctor the constructor definition
     * @return the generic exception types
     */
    public static IPersistentVector
    getCtorGenericExTypes(Constructor<?> ctor) {
        return getArrayAsLazyVector(ctor.getGenericExceptionTypes());
    }

    /**
     * Get the annotations of a method
     * @param meth the method definition
     * @return the methods annotations
     */
    public static IPersistentVector
    getDeclAnnotsMethod(Method meth) {
        return getArrayAsLazyVector(meth.getDeclaredAnnotations());
    }

    /**
     * Get the annotations of a constructor
     * @param ctor the constructor definition
     * @return the constructors annotations
     */
    public static IPersistentVector
    getDeclAnnotsCtor(Constructor<?> ctor) {
        return getArrayAsLazyVector(ctor.getDeclaredAnnotations());
    }

    /**
     * Get the type parameters of a method
     * @param meth the method definition
     * @return the methods type parameters
     */
    public static IPersistentVector
    getTypeParamsMethod(Method meth) {
        return getArrayAsLazyVector(meth.getTypeParameters());
    }

    /**
     * Get the type parameters of a constructor
     * @param ctor the constructor definition
     * @return the constructors type parameters
     */
    public static IPersistentVector
    getTypeParamsCtor(Constructor<?> ctor) {
        return getArrayAsLazyVector(ctor.getTypeParameters());
    }
}
