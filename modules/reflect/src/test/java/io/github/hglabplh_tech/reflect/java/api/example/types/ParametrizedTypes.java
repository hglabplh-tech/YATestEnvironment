package io.github.hglabplh_tech.reflect.java.api.example.types;

import java.util.*;

public class ParametrizedTypes {

    private final List<String> parmList = new ArrayList<>();

    private final Map<Integer, String> parmMap = new HashMap<>();

    private  Tuple<Integer, String> myTuple = null;

    private List<String> l_int_str = testTheWest(3, "u", "p", "s");
    private List<Double> l_str_double = testTheWest("hallo", 3.98, 56.98, 896.9);

    public void setTuple (Tuple<Integer, String> testIn) {
        this.myTuple = testIn;
    }

    public  Tuple<Integer, String> setTuple () {

        return this.myTuple;
    }

    public <Z,P> List<Z> testTheWest(P param, Z... inputs) {
        List<Z> result = Arrays.asList(inputs);
        return result;
    }

    public static class Tuple<K extends Integer, E> {
        private final K key;

        private final E value;

        public Tuple(K key, E value) {
            this.key = key;
            this.value = value;
        }

        public K key() {
            return key;
        }

        public E value() {
            return value;
        }
    }

}
