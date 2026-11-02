package io.github.hglabplh_tech.reflect.java.api.utils;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Set;

import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.*;

/**
 * This s a utility class for getting the definition data of
 * either a constructor or a method
 *
 * @author Harald Glab-Plhak
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
    public static IPersistentVector getCtorParamTypes(Constructor<?> ctor) {
        return getArrayAsVector(ctor.getParameterTypes());
    }

    public static IPersistentVector getCtorGenericParmTypes(Constructor<?> ctor) {
        return getArrayAsVector(ctor.getGenericParameterTypes());
    }
    public static IPersistentVector getCtorParametersAndParmAttribs(Constructor<?> ctor) {
        IPersistentVector result = PersistentVector.EMPTY;
        Parameter[] params = ctor.getParameters();
        for (Parameter parameter: params) {
            IPersistentMap paramDefMap = getParameterDefs(parameter);
            result = result.cons(paramDefMap);
        }
        return result;
    }

    public static IPersistentMap getParameterDefs(Parameter parameter) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        String theName = parameter.getName();
        Class<?> theType = parameter.getType();
        AnnotatedType annotType = parameter.getAnnotatedType();
        Annotation[] annots = parameter.getAnnotations();
        Type type = parameter.getParameterizedType();

        String paramTypeString = type.getTypeName();
        IPersistentVector annotsVector = getArrayAsVector(annots);
        int modifiers = parameter.getModifiers();
        String modifiersString = Modifier.toString(modifiers);
        boolean isVarArg =parameter.isVarArgs();
        boolean isImplicit = parameter.isImplicit();
        boolean isNamePresent = parameter.isNamePresent();
        boolean isSynthetic = parameter.isSynthetic();
        result = result.assocEx(retrieveKeywordFromString(null, "param-name"), theName);
        result = result.assocEx(retrieveKeywordFromString(null, "param-type"), readParametrizedType(theType));
        result = result.assocEx(retrieveKeywordFromString(null, "param-annot-type"), annotType);
        result = result.assocEx(retrieveKeywordFromString(null, "param-parametrized-type"),
                readParametrizedType(type));
        result = result.assocEx(retrieveKeywordFromString(null, "param-annots"), annotsVector);
        result = result.assocEx(retrieveKeywordFromString(null, "param-modifiers"), modifiersString);
        IPersistentVector flags = PersistentVector.EMPTY;

        if (isVarArg) {
            flags = flags.cons(retrieveKeywordFromString(null, "vararg"));
        }
        if (isImplicit) {
            flags = flags.cons(retrieveKeywordFromString(null, "implicit"));
        }
        if (isNamePresent) {
            flags = flags.cons(retrieveKeywordFromString(null, "name-present"));
        }
        if (isSynthetic) {
            flags = flags.cons(retrieveKeywordFromString(null, "synthetic"));
        }
        result = result.assocEx(retrieveKeywordFromString(null, "param-flags"), flags);
        return result;
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
     * Get the methods parameter types
     * @param ctor the constructor definition
     * @return the methods parameter types
     */
    public static  IPersistentMap getCtorParamAnnots (
            Constructor<?> ctor) {
        return getAsMap(ctor.getParameterAnnotations());
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
    public static  IPersistentVector getMethodParamTypes (Method method) {
        return getArrayAsVector(method.getParameterTypes());
    }

    /**
     * Get the methods parameter types
     * @param method the method definition
     * @return the methods parameter types
     */
    public static IPersistentMap getMethodParamAnnots (
            Method method) {
        return getAsMap(method.getParameterAnnotations());
    }

    public static IPersistentVector getMethParametersAndParmAttribs(Method method) {
        IPersistentVector result = PersistentVector.EMPTY;
        Parameter[] params = method.getParameters();
        for (Parameter parameter: params) {
            IPersistentMap paramDefMap = getParameterDefs(parameter);
            result = result.cons(paramDefMap);
        }
        return result;
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
     * Get the methods annotated return type
     * @param method the method definition
     * @return the methods annotated return type
     */
    public static AnnotatedType getMethodAnnotReturnType(Method method) {
        return method.getAnnotatedReturnType();
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
        return getArrayAsVector(method.getExceptionTypes());
    }

    /**
     * Get the methods generic exception types
     * @param method the method definition
     * @return the methods generic exception types
     */
    public static  IPersistentVector getMethGenericExTypes(Method method) {
        return getArrayAsVector(method.getGenericExceptionTypes());
    }

    /**
     * Get the methods generic parameter types
     * @param method the method definition
     * @return the methods generic parameter types
     */
    public static  IPersistentVector getMethGenericParmTypes(Method method){
        return getArrayAsVector(method.getGenericParameterTypes());
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
        return getArrayAsVector(ctor.getExceptionTypes());
    }

    /**
     * Get the generic exception types of a constructor
     * @param ctor the constructor definition
     * @return the generic exception types
     */
    public static IPersistentVector
    getCtorGenericExTypes(Constructor<?> ctor) {
        return getArrayAsVector(ctor.getGenericExceptionTypes());
    }

    /**
     * Get the annotations of a method
     * @param meth the method definition
     * @return the methods annotations
     */
    public static IPersistentVector
    getDeclAnnotsMethod(Method meth) {
        return getArrayAsVector(meth.getDeclaredAnnotations());
    }

    /**
     * Get the annotations of a constructor
     * @param ctor the constructor definition
     * @return the constructors annotations
     */
    public static IPersistentVector
    getDeclAnnotsCtor(Constructor<?> ctor) {
        return getArrayAsVector(ctor.getDeclaredAnnotations());
    }

    /**
     * Get the type parameters of a method
     * @param meth the method definition
     * @return the methods type parameters
     */
    public static IPersistentVector
    getTypeParamsMethod(Method meth) {
        return getArrayAsVector(meth.getTypeParameters());
    }

    /**
     * Get the type parameters of a constructor
     * @param ctor the constructor definition
     * @return the constructors type parameters
     */
    public static IPersistentVector
    getTypeParamsCtor(Constructor<?> ctor) {
        return getArrayAsVector(ctor.getTypeParameters());
    }
}
