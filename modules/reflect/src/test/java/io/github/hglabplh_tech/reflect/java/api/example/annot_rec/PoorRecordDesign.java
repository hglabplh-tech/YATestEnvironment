package io.github.hglabplh_tech.reflect.java.api.example.annot_rec;

public record PoorRecordDesign(String username, SecurityContext security) {

    // 1. A traditional static nested class (allowed)
    // Note: Writing 'static' is optional; it is static by default.
    public static class SecurityContext {
        private String token;

        public SecurityContext(String token) { this.token = token; }
        public String getToken() { return token; }
    }

    // 2. An inner enum (allowed & implicitly static)
    public enum AccountStatus {
        ACTIVE, SUSPENDED, DELETED
    }

    // 3. An inner interface (allowed & implicitly static)
    public interface Validator {
        boolean validate(PoorRecordDesign profile);
    }
}