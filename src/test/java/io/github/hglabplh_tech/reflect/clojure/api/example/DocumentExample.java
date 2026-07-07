/*
 * Copyright (c) 2026 Harald Glab-Plhak
 */

package io.github.hglabplh_tech.reflect.clojure.api.example;

import java.util.HashMap;

public record DocumentExample() {
    static Integer docNo;
    static String docName;
    static HashMap<Integer, String> content;
}
