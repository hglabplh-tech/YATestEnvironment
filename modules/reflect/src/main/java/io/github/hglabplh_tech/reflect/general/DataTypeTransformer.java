package io.github.hglabplh_tech.reflect.general;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentArrayMap;
import io.github.hglabplh_tech.reflect.java.api.utils.SpecialFormsUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.ObjType;
import static io.github.hglabplh_tech.reflect.general.ClojFunctionalUtils.retrieveKeywordForJavaID;

/**
 * This s a utility class for transforming data from Java to a well readable
 * processable form of clojure data
 *
 * @author Harald Glab-Plhak
 */
public class DataTypeTransformer {

    /**
     * Private for all methods are static
     */
    private DataTypeTransformer () {

    }

    /**
     * This method gets a array of field definitions and
     * the instance of the corresponding object
     * the field definitions are used to retrieve the values from the fields
     * the field name (Camel Case ) is converted to Clojure conventions and a
     * keyword is created the value is then added to a map with the keyword as key
     * @param fields the field definitions
     * @param instance the field instance
     * @return the parsistant map wirh name value
     */
    public static IPersistentMap
    transformTypeValuesFromFields(Field[] fields, Object instance) { // TODO make return to a map {:type typeName :typeAttr attr :value val}
        IPersistentMap nameValueMap = PersistentArrayMap.create
                (PersistentArrayMap.EMPTY);
        for (Field field : fields) {
            if (field.getType().isAssignableFrom(Boolean.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getBoolean(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isAssignableFrom(Double.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getDouble(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isAssignableFrom(String.class)) {
                nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            instance.toString());
            } else if (field.getType().isAssignableFrom(Float.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getFloat(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isAssignableFrom(Integer.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getInt(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isAssignableFrom(Short.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getShort(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else if (field.getType().isAssignableFrom(Long.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getLong(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            } else if (field.getType().isAssignableFrom(Byte.TYPE)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getByte(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            } else if (field.getType().isAssignableFrom(char.class)) {
                try {
                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            field.getChar(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

            } else {
                try {
                    Object value = field.get(instance);
                    if (value.getClass().isArray()) {
                        value = java.util.Arrays.toString((Object[]) value);
                    }
                    if(value.getClass().isEnum()) {
                        value = SpecialFormsUtil.getEnumSpec(value.getClass());
                    }
                    else  if (value.getClass().isRecord()){
                        value = SpecialFormsUtil.getRecordSpec(value.getClass());
                    }
                    else if (value.getClass().isSynthetic()) {
                        value = SpecialFormsUtil.getGeneralLambdaExprSpec(value.getClass());
                    } else {
                        String typeName = value.getClass().getTypeName();
                        value = field.get(instance);
                    }


                    nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID(field.getName(),
                                    ObjType.FIELD),
                            value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return nameValueMap;
    }

    /**
     * This method gets a array of method definitions and
     * the instance of the corresponding object
     * the method definitions are used to get the return values from
     * calling the method name (Camel Case ) is converted to Clojure conventions and a
     * keyword is created the value is then added to a map with the keyword as key
     * Only the methods with an empty parameterlist are called for we assume them
     * to be 'getter' methods
     *
     * @param methods the field definitions
     * @return the parsistant map wirh name value
     */
    public static IPersistentMap
    transformTypeValuesFromMethods(Method[] methods) {

        IPersistentMap methValuesMap = PersistentArrayMap.create
                (PersistentArrayMap.EMPTY);
        for (Method method : methods) {
            IPersistentMap nameValueMap = PersistentArrayMap.create
                    (PersistentArrayMap.EMPTY);
            Class<?> result = null;
            Object defaultVal = null;
            String defaultValTypeName = null;
            if (method.getParameterCount() == 0) {
                result = method.getReturnType();
                defaultVal = method.getDefaultValue();
                defaultValTypeName = defaultVal.getClass().getTypeName();
                IPersistentMap resultMap = PersistentArrayMap.EMPTY;
                if(result.isEnum()) {
                    resultMap = SpecialFormsUtil.getEnumSpec(result);
                }
                if(result.isRecord()) {
                    resultMap = SpecialFormsUtil.getEnumSpec(result);
                }
                nameValueMap = nameValueMap.assoc(
                        retrieveKeywordForJavaID("method-name", ObjType.NONE),
                        resultMap);
                nameValueMap = nameValueMap.assoc(
                        retrieveKeywordForJavaID("return-type", ObjType.NONE),
                        resultMap);
                nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID("default-value", ObjType.NONE), defaultVal);
                nameValueMap = nameValueMap.assoc(retrieveKeywordForJavaID("val-type-name", ObjType.NONE), defaultValTypeName);
                methValuesMap = methValuesMap.assocEx(retrieveKeywordForJavaID(method.getName(), ObjType.METHOD), nameValueMap);
            }

        }
        return methValuesMap;
    }
}