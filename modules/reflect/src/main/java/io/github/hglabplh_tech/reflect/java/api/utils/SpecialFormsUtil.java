package io.github.hglabplh_tech.reflect.java.api.utils;

import clojure.lang.*;

import java.lang.Record;
import java.lang.constant.ClassDesc;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.*;

/**
 * This s a utility class for the types enum, records and 'lambda'
 *
 * @author Harald Glab-Plhak
 */
public class SpecialFormsUtil {

    /**
     * private contructor for all methods are static
     */
    private SpecialFormsUtil() {

    }

    /**
     * This method analyzes the structure and attributes and constants of a enum and
     * returns it as a map
     *
     * @param theClass the class (it is checked to be an enum class)
     * @return the enum specification
     */
    public static IPersistentMap
    getEnumSpec(Class<?> theClass) {
        Class<? extends Enum> theEnum = null;
        if (theClass.isEnum()) {
            theEnum = (Class<? extends Enum>) theClass;
        } else {
            return PersistentArrayMap.create(PersistentArrayMap.EMPTY)
                    .assocEx(
                            retrieveKeywordForJavaID("e-info-empty", ObjType.CLASS),
                            "empty");
        }
        Enum[] enumConsts = theEnum.getEnumConstants();
        IPersistentMap enumMap = PersistentArrayMap.EMPTY;
        IPersistentMap enumValuesMap = PersistentArrayMap.EMPTY;
        for (Enum enumC : enumConsts) {
            String name = enumC.name();
            Integer ordinal = enumC.ordinal();
            Optional<Enum.EnumDesc> enumDescrOpt = enumC.describeConstable();
            IPersistentMap enumAttrMap = PersistentArrayMap
                    .create(PersistentArrayMap.EMPTY)
                    .assoc(retrieveKeywordForJavaID("ordinal", ObjType.NONE)
                            , ordinal);
            if (enumDescrOpt.isPresent()) { // TODO: complete this
                Enum.EnumDesc description = enumDescrOpt.get();
                ClassDesc classDescription = description.constantType();
                String classDescrString = classDescription.descriptorString();
                enumAttrMap = enumAttrMap
                        .assoc(retrieveKeywordForJavaID("class-descr", ObjType.NONE)
                                , classDescrString)
                        .assoc(retrieveKeywordForJavaID("enum-descr", ObjType.NONE),
                                description.toString());
            }
            enumValuesMap = enumValuesMap.assocEx(name, enumAttrMap);
        }
        enumMap = enumMap.assocEx(retrieveKeywordFromString(null, "enum-values-def"), enumValuesMap);
        enumMap = enumMap.assocEx(retrieveKeywordFromString(null, "design-issues"),
                getEnumDesignMap(theEnum, PersistentArrayMap.EMPTY));
        return enumMap;

    }

    private static IPersistentMap getEnumDesignMap(Class<? extends Enum> theEnumClass, IPersistentMap enumMap) {
        boolean classesAllowed = true;
        boolean fieldsAllowed = true;
        for (Class<?> subClass : theEnumClass.getDeclaredClasses()) {
            if (!(subClass.isInterface() && Modifier.isPrivate(subClass.getModifiers()))) {
                classesAllowed = false;
                break;
            }
        }
        for (Field field: theEnumClass.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
                fieldsAllowed = false;
                break;
            }
        }
        Keyword okFieldKey = retrieveKeywordFromString(null, "only-final-fields");
        Keyword nokFieldKey = retrieveKeywordFromString(null, "none-final-fields");
        Keyword okSubclassKey = retrieveKeywordFromString(null, "only-private-interfaces");
        Keyword nokSubclassKey = retrieveKeywordFromString(null, "none-private-interfaces");
        enumMap = enumMap.assocEx(retrieveKeywordFromString(null, "final-fields"),
                fieldsAllowed ? okFieldKey : nokFieldKey);
        enumMap = enumMap.assocEx(retrieveKeywordFromString(null, "enum-classes"),
                classesAllowed ? okSubclassKey : nokSubclassKey);
        return enumMap;
    }

    /**
     * This method analyzes the structure and attributes and constants of a record and
     * returns it as a map
     *
     * @param thePotentialRecordClass the class of the record
     * @return the record specification
     */
    public static IPersistentMap
    getRecordSpec(Class<?> thePotentialRecordClass) {
        IPersistentMap recordMap = PersistentHashMap.create(PersistentArrayMap.EMPTY);
        Class<? extends Record> theRecordClass;
        if (thePotentialRecordClass.isRecord()) {
            theRecordClass = (Class<? extends Record>) thePotentialRecordClass;
        } else {
            return PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        }
        Type[] parmTypes = null;
        Constructor<?>[] ctors = theRecordClass.getDeclaredConstructors();
        if (ctors.length > 0) {
            parmTypes = ctors[0].getGenericParameterTypes();
                    recordMap = recordMap.assocEx(
                    retrieveKeywordForJavaID("ctorParmType", ObjType.NONE),
                    getArrayAsVector(parmTypes));
        }

        if (parmTypes != null) {
            Field[] fields = theRecordClass.getDeclaredFields();
            Map<String, Type> nameTypeMap = Arrays.stream(fields)
                    .collect(Collectors.toMap(Field::getName,
                            Field::getGenericType, (a, b) -> b));
            for (Map.Entry<String, Type> nameTypeEntry : nameTypeMap.entrySet()) {
                Type[] typeArrTemp = {nameTypeEntry.getValue()};
                recordMap =
                        recordMap.assocEx(
                                retrieveKeywordForJavaID(
                                        nameTypeEntry.getKey(),
                                        ObjType.NONE),
                                getArrayAsVector(typeArrTemp));
            }
            Method[] methods = theRecordClass.getMethods();
            IPersistentMap methodMap = PersistentHashMap.create(PersistentArrayMap.EMPTY);
            Integer index = 0;
            for (Method meth: methods) {
                IPersistentMap allMethodsParms = PersistentHashMap.EMPTY;
                IPersistentVector result = MethsCtorsUtil.getMethParametersAndParmAttribs(meth);
                allMethodsParms = allMethodsParms.assocEx(retrieveKeywordFromString(null, "parameters"),
                        result);
                allMethodsParms = allMethodsParms.assocEx(retrieveKeywordFromString(null, "return-type"),
                        MethsCtorsUtil.getMethodReturnType(meth));
                methodMap = methodMap.assocEx(
                        retrieveKeywordFromString("meth_" + index++ + "_", MethsCtorsUtil.getMethodName(meth)),
                        allMethodsParms);
            }
            recordMap = recordMap.assocEx(retrieveKeywordFromString(null, "methods"),
                    methodMap);
            recordMap = recordMap.assocEx(retrieveKeywordFromString(null, "design-issues"),
                    getRecDesignMap(theRecordClass, PersistentArrayMap.EMPTY));
        }
        return recordMap;
    }

    private static IPersistentMap getRecDesignMap(Class<? extends Record> theRecordClass, IPersistentMap recordMap) {
        boolean classesAllowed = true;
        boolean fieldsAllowed = true;
        for (Class<?> subClass : theRecordClass.getDeclaredClasses()) {
            if (!subClass.isRecord()) {
                classesAllowed = false;
                break;
            }
        }
        for (Field field: theRecordClass.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers())) {
                fieldsAllowed = false;
                break;
            }
        }
        Keyword okFieldKey = retrieveKeywordFromString(null, "only-final-fields");
        Keyword nokFieldKey = retrieveKeywordFromString(null, "none-final-fields");
        Keyword okSubclassKey = retrieveKeywordFromString(null, "only-record-classes");
        Keyword nokSubclassKey = retrieveKeywordFromString(null, "none-record-classes");
        recordMap = recordMap.assocEx(retrieveKeywordFromString(null, "final-fields"),
                fieldsAllowed ? okFieldKey : nokFieldKey);
        recordMap = recordMap.assocEx(retrieveKeywordFromString(null, "record-classes"),
                classesAllowed ? okSubclassKey : nokSubclassKey);
        return recordMap;
    }


    /**
     * This method analyzes a 'lambda' and returns the attributes and the specification
     * !!!EXPERIMENTAL UP TO NOW!!!
     *
     * @param theFunction the object (it is checked to be a 'lambda')
     * @return the lambda expression specification
     */
    public static IPersistentMap
    getGeneralLambdaExprSpec(Class<?> theFunction) {
        IPersistentMap totalResult = PersistentArrayMap.EMPTY;
        Constructor<?>[] ctors = theFunction
                .getDeclaredConstructors();
        /* look for the ctors*/
       for (Constructor<?> construct: ctors) {
           IPersistentMap theResult = PersistentArrayMap.EMPTY;
            Parameter[] params = construct.getParameters();
            theResult = theResult.assocEx(
                    retrieveKeywordForJavaID("paramTypes", ObjType.NONE),
                    getArrayAsVector(params));
            theResult = theResult.assocEx(
                    retrieveKeywordForJavaID("returnType", ObjType.NONE),
                    construct.getGenericParameterTypes());
            totalResult = totalResult.assocEx(
                    retrieveKeywordForJavaID("ctor", ObjType.NONE),
                    theResult);

        }
        /* look for the methods */
        Method[] methods = theFunction.getDeclaredMethods();
        for (Method foundMethod : methods) {
            boolean isStatic =
                    ((foundMethod.getModifiers()
                            & Modifier.STATIC) != 0);
            if (isStatic) {
                IPersistentMap theResult = PersistentArrayMap.EMPTY;
                Type[] parmTypes = foundMethod.getParameterTypes();
                Type returnType = foundMethod.getReturnType();
                Type[] exceptionTypes = foundMethod.getExceptionTypes();
                theResult = theResult.assocEx(
                        retrieveKeywordForJavaID("returnType", ObjType.NONE),
                        returnType);
                theResult = theResult.assocEx(
                        retrieveKeywordForJavaID("paramTypes", ObjType.NONE),
                        parmTypes);
                theResult = theResult.assocEx(
                        retrieveKeywordForJavaID("exceptTypes", ObjType.NONE),
                        exceptionTypes);
                totalResult = totalResult.assocEx(
                        retrieveKeywordForJavaID(foundMethod.getName(), ObjType.NONE),
                        theResult);
            }
        }
        return totalResult;
    }
}
