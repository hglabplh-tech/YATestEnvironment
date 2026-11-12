package io.github.hglabplh_tech.reflect.java.api.annotations;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;


import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.*;
import static io.github.hglabplh_tech.reflect.general.DataTypeTransformer.*;
import static io.github.hglabplh_tech.reflect.general.DataTypeTransformer.transformTypeValuesFromMethods;

/**
 * This class is a utility class for analyzing the content and the attributes of
 * a given annotation.
 *
 * @author Harald Glab-Plhak
 */
public class AnnotationTool {



    private AnnotationTool() {
    }


    /**
     * With this the attributes of a annotation are retrieved (Target, Retention)
     *
     * @return the map of the annotation attributes
     */
    public static IPersistentMap getAnnotationAttributes(Annotation[] annots) {
        IPersistentMap theMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        for (Annotation annot : annots) {
            if (annot instanceof  Target) {
                IPersistentMap typeMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);

                Target theTarget = (Target) annot;
                typeMap = fillTargetAnnotAttribs(theTarget, typeMap);
                theMap = theMap.assocEx(
                        Keyword.intern("Target"),
                        typeMap);
            } else if (annot instanceof Retention) {
                IPersistentMap typeMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
                Retention theRetention = (Retention) annot;
                RetentionPolicy theRetPolicy = theRetention.value();
                typeMap = typeMap.assocEx(
                        retrieveKeywordForJavaID(theRetPolicy.name(), ObjType.FIELD),
                        theRetPolicy);
                theMap = theMap.assocEx(
                        Keyword.intern("Retention"),
                        typeMap);

            }
        }
        return theMap;
    }


    public static IPersistentMap retrieveAnnotationValuesAndAttributes(Annotation theAnnotation) {
        IPersistentMap theMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        IPersistentMap policies = getAnnotationAttributes(theAnnotation.annotationType().getAnnotations());
        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-attr", ObjType.ANNOTATION_POLICY),
                policies);
        Class<? extends Annotation> type = theAnnotation.annotationType();
        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-type", ObjType.NONE),
                type.getCanonicalName());
        Field[] fields = type.getDeclaredFields();
        Method[] methods = type.getDeclaredMethods();
        IPersistentMap fldMap = transformTypeValuesFromFields(fields, theAnnotation);
        theMap = theMap.assoc(retrieveKeywordForJavaID("fields-values", ObjType.NONE),
        fldMap);
        IPersistentMap methMap = transformTypeValuesFromMethods(methods);
        theMap = theMap.assoc(retrieveKeywordForJavaID("methods-values", ObjType.NONE),
                methMap);
        return theMap;
    }

    public static IPersistentMap retrieveAnnotationClassValuesAndAttributes(Class<?> theAnnotationClass) {
        IPersistentMap theMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        IPersistentMap policies = getAnnotationAttributes(theAnnotationClass.getDeclaredAnnotations());
        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-attr", ObjType.ANNOTATION_POLICY),
                policies);

        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-type", ObjType.NONE),
                theAnnotationClass.getCanonicalName());
        Field[] fields = theAnnotationClass.getDeclaredFields();
        Method[] methods = theAnnotationClass.getDeclaredMethods();
        IPersistentMap fldMap = transformTypeValuesFromFields(fields, theAnnotationClass);
        theMap = theMap.assoc(retrieveKeywordForJavaID("fields-values", ObjType.NONE),
                fldMap);
        IPersistentMap methMap = transformTypeValuesFromMethods(methods);
        theMap = theMap.assoc(retrieveKeywordForJavaID("methods-values", ObjType.NONE),
                methMap);
        theMap = theMap.assocEx(retrieveKeywordFromString(null, "design-issues"),
                getAnnotDesignMap(theAnnotationClass.getDeclaredClasses(), methods));
        return theMap;
    }
    private static IPersistentMap getAnnotDesignMap(Class<?>[] subclasses, Method[] methods) {
        int badCountMeths = checkMethodsDesignFail(methods);
        int badCountSubclasses = checkSubclassesDesignFail(subclasses);
        Keyword okMethodsKey = retrieveKeywordFromString(null, "all-methods-valid");
        Keyword nokMethodsKey = retrieveKeywordFromString(null, "invalid-methods");
        Keyword okSubclassKey = retrieveKeywordFromString(null, "all-classes-valid");
        Keyword nokSubclassKey = retrieveKeywordFromString(null, "invalid-classes");
        IPersistentMap designMap = PersistentArrayMap.EMPTY;
        designMap = designMap.assocEx(retrieveKeywordFromString(null, "subclasses"),
                (badCountSubclasses == 0) ? okSubclassKey : nokSubclassKey);
        designMap = designMap.assocEx(retrieveKeywordFromString(null, "methods"),
                (badCountMeths == 0) ? okMethodsKey : nokMethodsKey);
        return designMap;
    }

    private static int checkMethodsDesignFail(Method [] methods) {
        int badCounter = 0;
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers())) {
                badCounter++;
            }
        }
        return badCounter;
    }

    private static int checkSubclassesDesignFail(Class<?> [] classes) {
        int badCounter = 0;
        for (Class<?> nested : classes) {
            Method [] methods = nested.getMethods();
            for (Method method : methods) {
                if (!method.isSynthetic() && !Modifier.isAbstract(method.getModifiers())) {
                    badCounter++;
                }
            }
        }
        return badCounter;
    }



    /**
     * Put the element types of the annotation '@Target' to a map
     *
     * @param theAnnotation the annotation
     * @param typeMap       the map containing the element types of the @Target
     *                      annotation
     */
    private static IPersistentMap
    fillTargetAnnotAttribs(Target theAnnotation, IPersistentMap typeMap) {
        ElementType[] elementTypes = theAnnotation.value();
        for (ElementType eType : elementTypes) {
            Keyword elementKey = retrieveKeywordForJavaID(eType.name(), ObjType.NONE);
            typeMap = typeMap.assoc(elementKey, eType);
        }
        return typeMap;
    }

    public static IPersistentMap getAllAnnotationInfos(Annotation theAnnotation) {
        IPersistentMap annotMap = PersistentArrayMap.EMPTY;
        Keyword elementKey = retrieveKeywordForJavaID(theAnnotation.toString(), ObjType.ANNOTATION);
        annotMap = annotMap.assoc(elementKey, retrieveAnnotationValuesAndAttributes(theAnnotation));
        return annotMap;
    }

    public static IPersistentMap getAllAnnotationClassInfos(Class<?> theAnnotationClass) {
        IPersistentMap annotMap = PersistentArrayMap.EMPTY;
        if (theAnnotationClass.isAnnotation()) {
            Keyword elementKey = retrieveKeywordForJavaID(theAnnotationClass.getCanonicalName(), ObjType.ANNOTATION);
            annotMap = annotMap.assoc(elementKey, retrieveAnnotationClassValuesAndAttributes(theAnnotationClass));
        }


        return annotMap;
    }
}