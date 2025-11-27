package io.github.hglabplh_tech.reflect.clojure.api.example.special;

import io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.Category;

public class CaseInner {

    public static final String test;
    static { // how can this be reflected ???
        test = "hello";
    }
    public final Runnable theRunner = new Runnable() {
        @Override
        public void run() {
            System.out.print(8);
        }
    };

    public void synth_inner_class (Category category) {
        switch (category) {
            case APIS -> System.out.println("Apis" + category.getName());
            case TESTS -> System.out.println("Test" + category.getName());
            case PROJECT -> System.out.println("Project" + category.getName());
            case NONE -> System.out.println("Nothing selected");
            default -> System.out.println("Not handled category");
        }

        Thread t = new Thread(this.theRunner);
    }
}
