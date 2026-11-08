package io.github.hglabplh_tech.reflect.java.api.example.annot_rec;

/**
 * This is a example for a good ussage of inner classes in ENUMS the inner logic is strictly bound to enum definition
 */
public enum PaymentProcessor {
    CREDIT_CARD(payload -> "Processing credit card token: " + payload),
    PAYPAL(payload -> "Redirecting to PayPal for: " + payload),
    CRYPTO(payload -> "Verifying blockchain ledger for: " + payload);

    // 1. NESTED INTERFACE: Defines the behavior strategy
    // Highly acceptable because it is tightly bound ONLY to this enum's domain
    @FunctionalInterface
    private interface ActionStrategy {
        String execute(String payload);
    }

    private final ActionStrategy strategy;

    // Constructor assigns the strategy to the constant
    PaymentProcessor(ActionStrategy strategy) {
        this.strategy = strategy;
    }

    // Public method exposes the execution cleanly
    public String process(String data) {
        return strategy.execute(data);
    }
}
