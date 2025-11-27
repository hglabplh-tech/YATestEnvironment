package io.github.hglabplh_tech.reflect.clojure.api.example;

public class MyTuple<T extends TheTupleTypeIfc> {
    private final String name;

    private final T value;

    public MyTuple(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public T value() {
        return this.value;
    }

    public String key() {
        return this.name;
    }

}
