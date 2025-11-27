package io.github.hglabplh_tech.reflect.clojure.api.example;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.util.LinkedHashMap;
import java.util.Map;

public class GenArrayParametAnnotType <D extends TheTupleTypeIfc> {

    private GenArrayParametAnnotType() {

    }

    public static <T> T[] getArray(T[] input) {
        T[] array = input;
        return array;
    }

    public static <K, V> Map<K, V> dummyGen() {
        Map<K, V> result = new LinkedHashMap<>();
        return result;
    }

    @Inherited
    public @interface MyCustomAnnotation {
        String value();
    }

    @MyCustomAnnotation(value = "hallo")
    public abstract class ParentClass{
        String  annot;

        public ParentClass() {
            annot = "hallo";
        }

        public String getAnnot() {
            return annot;
        }
    }

    public class ChildClass extends ParentClass {

        @Override
        public String getAnnot() {
            return "hallo child";
        }
    }

    @Repeatable(Universities.class)
    public @interface University {
        String name();
    }

    public @interface Universities {
        University[] value();
    }
}
