/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package java.io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentArrayMap;
import javax.annotation.Nonnull;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.github.hglabplh_tech.reflect.clojure.api.utils.ClojFunctionalUtils.*;

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
    public static @Nonnull IPersistentMap
    getEnumSpec(@Nonnull Class<?> theClass) {
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
            enumMap = enumMap.assoc(name, enumAttrMap);
        }
        return enumMap;

    }

    /**
     * This method analyzes the structure and attributes and constants of a record and
     * returns it as a map
     *
     * @param thePotentialRecordClass the class of the record
     * @return the record specification
     */
    public static @Nonnull IPersistentMap
    getRecordSpec(@Nonnull Class<?> thePotentialRecordClass) {
        IPersistentMap recordMap = PersistentArrayMap.create(PersistentArrayMap.EMPTY);
        Class<? extends Record> theRecordClass = null;
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
                    getArrayAsLazyVector(parmTypes));
        }
        if (parmTypes != null) {
            Field[] fields = theRecordClass.getDeclaredFields();
            Map<String, Type> nameTypeMap = Arrays.stream(fields)
                    .collect(Collectors.toMap(Field::getName,
                            Field::getGenericType, (a, b) -> b));
            IPersistentMap fieldsTypesMap = PersistentArrayMap.EMPTY;
            for (Map.Entry<String, Type> nameTypeEntry : nameTypeMap.entrySet()) {
                Type[] typeArrTemp = {nameTypeEntry.getValue()};
                recordMap =
                        recordMap.assocEx(
                                retrieveKeywordForJavaID(
                                        nameTypeEntry.getKey(),
                                        ObjType.NONE),
                                getArrayAsLazyVector(typeArrTemp));
            }



        }
        return recordMap;
    }

    /**
     * This method analyzes a 'lambda' and returns the attributes and the specification
     *
     * @param theFunction the object (it is checked to be a 'lambda')
     * @return the lambda expression specification
     */
    public static @Nonnull IPersistentMap
    getGeneralLambdaExprSpec(@Nonnull Class<?> theFunction) {
        return LambdaUtil.getLambdaSpec(theFunction);
    }
}
