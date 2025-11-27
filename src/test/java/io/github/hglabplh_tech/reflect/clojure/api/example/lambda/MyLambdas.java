package io.github.hglabplh_tech.reflect.clojure.api.example.lambda;

import java.util.function.Function;

public class MyLambdas {



    private MyLambdas () {

    }

    public String testIt() {
        String input = "Hallo here I am";
        String result = dealWith(input);
        return result;
    }

    public static <T, R> R dealWith(T berta) {
       FirstOrder<T, R> fun = new FirstOrder<T, R>();
        return fun.apply(berta);
    }

    public static class FirstOrder<T, R> implements Function<T, R> {

        @Override
        public R apply(T t) {
            R test = (R)t;
            System.out.println("try " + test);
            return test;
        }

        @Override
        public <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
            return Function.super.compose(before);
        }

        @Override
        public <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
            return Function.super.andThen(after);
        }
    }
}
