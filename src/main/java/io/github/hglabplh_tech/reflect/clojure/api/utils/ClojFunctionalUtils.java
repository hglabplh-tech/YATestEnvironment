package io.github.hglabplh_tech.reflect.clojure.api.utils;

import clojure.lang.*;


import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * This s a utility class for conversion from Java Types to  clojure vectors
 * or maps representing the content
 *
 * @author Harald Glab-Plhak (Harald G.P. IT-Consulting / Projéct Support)
 */
public class ClojFunctionalUtils {

    /**
     * Private constructor class has only statics
     */
    private ClojFunctionalUtils () {

    }

    /**
     *
     * @param arrayContent the array to transform
     * @return the persistence vector with the array-content
     */
    public static <T> IPersistentVector getArrayAsLazyVector(T [] arrayContent) {
        IPersistentVector newVector = LazilyPersistentVector.create(arrayContent);
        return newVector;
    }

    /**
     *
     * @param value
     * @return
     */
    public static Consumer<IFn> buildFunConsumer(IFn clojFun) {
        return (a) -> clojFun.invoke(a);
    }

    /**
     * transform a java.util.List to a Clojure vector
     * @param listContent the list to be transformed
     * @return thevector with the list content
     */
    public static IPersistentVector getListAsLazyVector(List<?> listContent) {
        IPersistentVector newVector = LazilyPersistentVector.create(listContent.toArray());
        return newVector;
    }

    /**
     * transform a generic param to a key value
     * @param paramVect the vector of parameters
     * @return the key value map (name, type-name)
     */
    public static IPersistentMap
        retrieveGenericParamTypesAsMeta(IPersistentVector paramVect) {
        IPersistentMap resultMap = PersistentArrayMap.EMPTY;
        for (int index = 0;
             index < paramVect.length();
             index++) {
            TypeVariable<?> typeVar = (TypeVariable<?>)paramVect.nth(index);
            String varName = typeVar.getName();
            String typeName = typeVar.getTypeName();
            resultMap = resultMap.assocEx(index, Tuple.create(varName, typeName));

        }
        return resultMap;
    }

    /**
     * Make a Clojure keyword out of a Java Name String
     * the name string is converted to Clojure name convention e.g. K gets k-
     * (No CamelCase)
     * @param id the id(Java Name) to be converted
     * @param objectType the type of the object to generate a prefix if wanted
     *                   (meth-(Method) fld-(Field) cls-(Class) ...
     * @return the K
     */
    public static Keyword retrieveKeywordForJavaID(String id,
                                                            ObjType objectType) {
        char[] idCharArray = id.toCharArray();
        StringBuilder resultBuffer = new StringBuilder(objectType.prefix());
        for (char character : idCharArray) {
            if (Character.isUpperCase(character)) {
                char charLower = Character.toLowerCase(character);
                resultBuffer.append('-').append(charLower);
            } else {
                resultBuffer.append(character);
            }

        }

        return Keyword.intern(resultBuffer.toString());
    }

    /**
     * The enum class for the different types when translating class names to
     * Clojure keyword convention
     * @author Harald Glab-Plhak*/
    public static enum ObjType {
        CLASS("cls-"),
        INTERFACE("ifc-"),
        CTOR("ctor-"),
        FIELD("fld-"),
        METHOD("meth-"),
        ANNOTATION("annot-"),
        RECORD("rec-"),
        NONE(""),;


        private final String prefix;

        ObjType(String prefix) {
            this.prefix = prefix;
        }

        public String prefix() {
            return prefix;
        }
    }
}
