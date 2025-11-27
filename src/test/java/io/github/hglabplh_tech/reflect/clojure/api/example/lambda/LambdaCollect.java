package io.github.hglabplh_tech.reflect.clojure.api.example.lambda;

import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LambdaCollect {
    private static Function supply;

    /*TODO: make method calling the definitions
       below enhance example to get a better test */

    public static void displayExpression(Supplier<Double> supplier) {
        System.out.println(supplier);
        System.out.println("Anonymous class: " + supplier.getClass().isAnonymousClass());
    }

    public static void displaySecExpression(Supplier<?> supplier) {
        supplier.get();
    }

    public  void example() {
        // Lambda expression with method reference
        displayThirdExpression(LambdaCollect::getDoubleValue);
    }
    public static Double getDoubleValue() {
        return 50.0;
    }

    public static void displayThirdExpression(Supplier<Double> supplier) {
        supplier.get();
    }

    public void secondExample() {
        // Lambda expression
        transformToUppercase(String::toUpperCase);
    }

    public static void transformToUppercase(Function<String,String> transformer) {
        System.out.println(transformer);
    }
    static Supplier<String> theThing = new Supplier<String>() {
        @Override
        public String get() {
            return "try";
        }};
    public List<String> tryTheThing() {
        String[] array = {"one", "two", "three", "four"};


        return Arrays.stream(array).toList();
        }




    public static Function<String[],
            Collector<String[],
                    Supplier<String>,
                    List<String>>>
    doSomething(Function<String[],
                                   Collector<String[],
                                           Supplier<String>,
                                           List<String>>> supply) {
        LambdaCollect.supply = supply;
        return supply;
    }
}
