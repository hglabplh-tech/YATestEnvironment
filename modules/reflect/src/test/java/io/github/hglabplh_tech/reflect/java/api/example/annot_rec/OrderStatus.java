package io.github.hglabplh_tech.reflect.java.api.example.annot_rec;

/**
 * Bad use of a inner definition in a enum. The inner class has an own logic and state
 */
public enum OrderStatus {
    PENDING, SHIPPED, DELIVERED;

    // BAD DESIGN: A mutable helper class living inside a fixed enum
    public static class OrderMetricsTracker {
        private int totalProcessed = 0; // State mutation inside an Enum file!
        public void increment() { totalProcessed++; }
    }
}

