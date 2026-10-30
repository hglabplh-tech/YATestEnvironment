package io.github.hglabplh_tech.reflect.java.api.annotations;

import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import io.github.hglabplh_tech.reflect.java.api.utils.ClassUtil;


import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.*;
import static io.github.hglabplh_tech.reflect.general.DataTypeTransformer.*;
import static io.github.hglabplh_tech.reflect.general.DataTypeTransformer.transformTypeValuesFromMethods;

/**
 * This class is a utility class for analyzing the content and the attributes of
 * a given annotation.
 *
 * @param <T> T is the type of the class and this type extends the annotation class
 * @author Harald Glab-Plhak
 */
public class AnnotationTool<T extends Annotation> {

    /**
     * the annotation to be analyzed
     */
    private final Annotation theAnnotation;

    /**
     * Here the Utility is constructed with an annotation to be analyzed
     * if the input class is not an annotation an Illegal Argument Exception
     * is thrown
     *
     * @param annot a annotation to analyze
     */
    public AnnotationTool(Annotation annot) {
        this.theAnnotation = annot;
    }


    /**
     * With this the attributes of a annotation are retrieved (Target, Retention)
     *
     * @return the map of the annotation attributes
     */
    public IPersistentMap getAnnotationAttributes() {
        Annotation[] annots = this.theAnnotation.annotationType().getAnnotations();
        IPersistentMap theMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        for (Annotation annot : annots) {
            if (annot.getClass()
                    .isAssignableFrom(Target.class)) {
                IPersistentMap typeMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);

                Target theTarget = (Target) annot;
                typeMap = fillTargetAnnotAttribs(theTarget, typeMap);
                theMap.assocEx(
                        Keyword.intern("Target"),
                        typeMap);
            } else if (annot.getClass()
                    .isAssignableFrom(Retention.class)) {
                IPersistentMap typeMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
                Retention theRetention = (Retention) annot;
                RetentionPolicy theRetPolicy = theRetention.value();
                theMap.assocEx(
                        Keyword.intern("Retention"),
                        typeMap);
                typeMap.assocEx(
                        retrieveKeywordForJavaID(theRetPolicy.name(), ObjType.FIELD),
                        theRetPolicy);
            }
        }
        return theMap;
    }

    public Annotation theAnnotation() {
        return theAnnotation;
    }

    public IPersistentMap retrieveAnnotationValuesAndAttributes() {
        IPersistentMap theMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        IPersistentMap policies = getAnnotationAttributes();
        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-attr", ObjType.ANNOTATION_POLICY),
                policies);
        Class<? extends Annotation> type = this.theAnnotation().annotationType();
        theMap = theMap.assoc(retrieveKeywordForJavaID("annotation-type", ObjType.NONE),
                type.getCanonicalName());
        Field[] fields = type.getDeclaredFields();
        Method[] methods = type.getDeclaredMethods();
        IPersistentMap fldMap = transformTypeValuesFromFields(fields, this.theAnnotation());
        theMap = theMap.assoc(retrieveKeywordForJavaID("fields-values", ObjType.NONE),
        fldMap);
        IPersistentMap methMap = transformTypeValuesFromMethods(methods, this.theAnnotation());
        theMap = theMap.assoc(retrieveKeywordForJavaID("methods-values", ObjType.NONE),
                methMap);
        return theMap;
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
            Keyword elementKey = retrieveKeywordForJavaID(eType.name(), ObjType.FIELD);
            typeMap = typeMap.assoc(elementKey, eType);
        }
        return typeMap;
    }

    public IPersistentMap getAllAnnotationInfos() {
        IPersistentMap annotMap = PersistentArrayMap.EMPTY;
        Keyword elementKey = retrieveKeywordForJavaID(theAnnotation().toString(), ObjType.ANNOTATION);
        annotMap = annotMap.assoc(elementKey, retrieveAnnotationValuesAndAttributes());
        return annotMap;
    }
}