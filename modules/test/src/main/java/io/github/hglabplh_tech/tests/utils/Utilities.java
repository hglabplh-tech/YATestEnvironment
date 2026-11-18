package io.github.hglabplh_tech.tests.utils;

public class Utilities {

    public static String getThreadIdentifierPrefix() {
        Thread actThread = Thread.currentThread();
        return actThread.getName() + "-" + Long.toString(actThread.getId()) + ":";
    }
}
