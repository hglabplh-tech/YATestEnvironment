package io.github.hglabplh_tech.reflect.java.api.example;

public class SwitchIfExample {
    static void action() {}
    static void fallthrough(int x) {
        switch (x) {
            case 1: action();
            case 2: action(); break;
            default: action();
        }
    }
    static void grouped(int x) {
        switch (x) {
            case 1:
            case 2: action(); break;
            default: action();
        }
    }
    static void arrows(int x) {
        switch (x) {
            case 1 -> action();
            case 2 -> action();
            default -> action();
        }
    }
    static void nested(boolean a, boolean b, boolean c, boolean d) {
        if (a) { if (b) { if (c) { if (d) action(); } } }
    }
    static void flat(int x) {
        if (x == 1) action();
        else if (x == 2) action();
        else if (x == 3) action();
        else if (x == 4) action();
    }
    static void guards(boolean a, boolean b, boolean c, boolean d) {
        if (!a) return;
        if (!b) return;
        if (!c) return;
        if (!d) return;
        action();
    }
}
