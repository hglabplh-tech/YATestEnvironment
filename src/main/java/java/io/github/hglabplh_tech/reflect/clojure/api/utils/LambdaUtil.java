/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package java.io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.PersistentArrayMap;

import javax.annotation.Nonnull;
import java.lang.reflect.*;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.*;

/**
 * This is a utility class for getting reflection information about lambda
 * classes and classes that expose functional interface members.
 * <p>
 * The Java reflection API does not expose lambda source expressions directly.
 * This utility therefore reports the runtime shape that can be observed safely:
 * generated lambda classes, implemented functional interfaces, captured state,
 * synthetic lambda body methods, and ordinary members whose type is a functional
 * interface.
 * </p>
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class LambdaUtil {

    /**
     * Private constructor for all methods are static.
     */
    private LambdaUtil() {

    }

    /**
     * Returns true if the given class looks like a generated lambda class.
     * A generated lambda class is normally synthetic and contains the marker
     * {@code $$Lambda} in its VM name.
     *
     * @param classToCheck the class which is checked
     * @return true if this is a synthetic lambda class otherwise false
     */
    public static boolean isLambdaClass(@Nonnull Class<?> classToCheck) {
        return classToCheck.isSynthetic()
                && classToCheck.getName().contains("$$Lambda");
    }

    /**
     * Returns true if the given class is a functional interface.
     * The check is structural: an interface is accepted when it has exactly one
     * abstract method after ignoring methods declared by {@link Object}.
     *
     * @param classToCheck the class which is checked
     * @return true if the class is a functional interface otherwise false
     */
    public static boolean isFunctionalInterface(@Nonnull Class<?> classToCheck) {
        if (!classToCheck.isInterface()) {
            return false;
        }
        int abstractMethodCount = 0;
        for (Method method : classToCheck.getMethods()) {
            if (isObjectMethod(method)) {
                continue;
            }
            if (Modifier.isAbstract(method.getModifiers())) {
                abstractMethodCount++;
            }
        }
        return abstractMethodCount == 1;
    }

    /**
     * Get the complete lambda reflection specification of a class.
     * The returned map is intended for the pseudo AST and contains observable
     * lambda-related facts under stable Clojure keyword names.
     *
     * @param theClass the class which is analyzed
     * @return the lambda reflection specification as Clojure map
     */
    public static @Nonnull IPersistentMap getLambdaSpec(@Nonnull Class<?> theClass) {
        return PersistentArrayMap.create(PersistentArrayMap.EMPTY)
                .assocEx(retrieveKeywordForJavaID("lambdaClass", ObjType.NONE),
                        isLambdaClass(theClass))
                .assocEx(retrieveKeywordForJavaID("functionalInterfaces", ObjType.NONE),
                        getFunctionalInterfaces(theClass))
                .assocEx(retrieveKeywordForJavaID("captureFields", ObjType.NONE),
                        getCaptureFields(theClass))
                .assocEx(retrieveKeywordForJavaID("constructors", ObjType.NONE),
                        getConstructorSpecs(theClass))
                .assocEx(retrieveKeywordForJavaID("syntheticLambdaMethods", ObjType.NONE),
                        getSyntheticLambdaMethods(theClass))
                .assocEx(retrieveKeywordForJavaID("functionalFields", ObjType.NONE),
                        getFunctionalFields(theClass))
                .assocEx(retrieveKeywordForJavaID("functionalMethodParameters", ObjType.NONE),
                        getFunctionalMethodParameters(theClass));
    }

    /**
     * Get all functional interfaces implemented by the class.
     * This is especially useful when {@code theClass} is itself a generated
     * lambda class.
     *
     * @param theClass the class which is analyzed
     * @return a vector with the functional interface classes
     */
    public static @Nonnull IPersistentVector getFunctionalInterfaces(@Nonnull Class<?> theClass) {
        IPersistentVector interfaces = getArrayAsLazyVector(theClass.getInterfaces());
        IPersistentVector result = getArrayAsLazyVector(new Class<?>[]{});
        for (int index = 0; index < interfaces.length(); index++) {
            Class<?> interfaceClass = (Class<?>) interfaces.nth(index);
            if (isFunctionalInterface(interfaceClass)) {
                result = result.cons(interfaceClass);
            }
        }
        return result;
    }

    /**
     * Get fields that may capture lambda state.
     * For generated lambda classes all declared fields are treated as capture
     * candidates. For ordinary classes only synthetic fields are returned.
     *
     * @param theClass the class which is analyzed
     * @return a map with field names and field types
     */
    public static @Nonnull IPersistentMap getCaptureFields(@Nonnull Class<?> theClass) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Field[] fields = theClass.getDeclaredFields();
        for (Field field : fields) {
            if (isLambdaClass(theClass) || field.isSynthetic()) {
                result = result.assocEx(
                        retrieveKeywordForJavaID(field.getName(), ObjType.FIELD),
                        field.getGenericType());
            }
        }
        return result;
    }

    /**
     * Get constructors of the class in a Clojure processable form.
     *
     * @param theClass the class which is analyzed
     * @return a map with constructor parameter and exception types
     */
    public static @Nonnull IPersistentMap getConstructorSpecs(@Nonnull Class<?> theClass) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Constructor<?>[] constructors = theClass.getDeclaredConstructors();
        for (int index = 0; index < constructors.length; index++) {
            Constructor<?> constructor = constructors[index];
            IPersistentMap ctorResult = PersistentArrayMap.EMPTY
                    .assocEx(retrieveKeywordForJavaID("ctorName", ObjType.NONE),
                            constructor.getName())
                    .assocEx(retrieveKeywordForJavaID("paramTypes", ObjType.NONE),
                            getArrayAsLazyVector(constructor.getGenericParameterTypes()))
                    .assocEx(retrieveKeywordForJavaID("exceptTypes", ObjType.NONE),
                            getArrayAsLazyVector(constructor.getGenericExceptionTypes()));
            result = result.assocEx(index, ctorResult);
        }
        return result;
    }

    /**
     * Get synthetic methods generated for lambda expression bodies.
     * Java compilers commonly name these methods with the {@code lambda$}
     * prefix.
     *
     * @param theClass the class which is analyzed
     * @return a map with the synthetic lambda method descriptions
     */
    public static @Nonnull IPersistentMap getSyntheticLambdaMethods(@Nonnull Class<?> theClass) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Method[] methods = theClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isSynthetic() && method.getName().contains("lambda$")) {
                result = result.assocEx(
                        retrieveKeywordForJavaID(method.getName(), ObjType.METHOD),
                        getMethodSpec(method));
            }
        }
        return result;
    }

    /**
     * Get fields that are declared as functional interface types.
     * These fields are lambda-capable slots even when their runtime value is not
     * available through class reflection.
     *
     * @param theClass the class which is analyzed
     * @return a map with field names and functional field descriptions
     */
    public static @Nonnull IPersistentMap getFunctionalFields(@Nonnull Class<?> theClass) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Field[] fields = theClass.getDeclaredFields();
        for (Field field : fields) {
            if (isFunctionalInterface(field.getType())) {
                IPersistentMap fieldResult = PersistentArrayMap.EMPTY
                        .assocEx(retrieveKeywordForJavaID("fieldType", ObjType.NONE),
                                field.getType())
                        .assocEx(retrieveKeywordForJavaID("genericType", ObjType.NONE),
                                field.getGenericType());
                result = result.assocEx(
                        retrieveKeywordForJavaID(field.getName(), ObjType.FIELD),
                        fieldResult);
            }
        }
        return result;
    }

    /**
     * Get method parameters that are functional interface types.
     * These parameters mark methods that accept lambdas, method references, or
     * other functional interface implementations.
     *
     * @param theClass the class which is analyzed
     * @return a map with method names and functional parameter descriptions
     */
    public static @Nonnull IPersistentMap getFunctionalMethodParameters(@Nonnull Class<?> theClass) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Method[] methods = theClass.getDeclaredMethods();
        for (int index = 0; index < methods.length; index++) {
            Method method = methods[index];
            IPersistentMap paramResult = getFunctionalParameters(method);
            if (paramResult.count() > 0) {
                IPersistentMap methodResult = PersistentArrayMap.EMPTY
                        .assocEx(retrieveKeywordForJavaID("methodName", ObjType.NONE),
                                method.getName())
                        .assocEx(retrieveKeywordForJavaID("params", ObjType.NONE),
                                paramResult);
                result = result.assocEx(index, methodResult);
            }
        }
        return result;
    }

    /**
     * Get functional interface parameters of the method.
     *
     * @param method the method which is analyzed
     * @return a map with parameter indexes and parameter types
     */
    public static @Nonnull IPersistentMap getFunctionalParameters(@Nonnull Method method) {
        IPersistentMap result = PersistentArrayMap.EMPTY;
        Class<?>[] paramTypes = method.getParameterTypes();
        Type[] genericParamTypes = method.getGenericParameterTypes();
        for (int index = 0; index < paramTypes.length; index++) {
            Class<?> paramType = paramTypes[index];
            if (isFunctionalInterface(paramType)) {
                IPersistentMap paramResult = PersistentArrayMap.EMPTY
                        .assocEx(retrieveKeywordForJavaID("paramType", ObjType.NONE),
                                paramType)
                        .assocEx(retrieveKeywordForJavaID("genericParamType", ObjType.NONE),
                                genericParamTypes[index]);
                result = result.assocEx(index, paramResult);
            }
        }
        return result;
    }

    /**
     * Build a method specification with parameters, return type and exceptions.
     *
     * @param method the method which is analyzed
     * @return a method specification as Clojure map
     */
    private static @Nonnull IPersistentMap getMethodSpec(@Nonnull Method method) {
        return PersistentArrayMap.EMPTY
                .assocEx(retrieveKeywordForJavaID("returnType", ObjType.NONE),
                        method.getGenericReturnType())
                .assocEx(retrieveKeywordForJavaID("paramTypes", ObjType.NONE),
                        getArrayAsLazyVector(method.getGenericParameterTypes()))
                .assocEx(retrieveKeywordForJavaID("exceptTypes", ObjType.NONE),
                        getArrayAsLazyVector(method.getGenericExceptionTypes()));
    }

    /**
     * Returns true if the given method is declared by java.lang.Object.
     *
     * @param method the method which is checked
     * @return true if the method is declared by Object otherwise false
     */
    private static boolean isObjectMethod(@Nonnull Method method) {
        try {
            Object.class.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        }
    }
}
