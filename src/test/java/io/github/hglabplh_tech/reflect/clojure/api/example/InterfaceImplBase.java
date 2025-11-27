package io.github.hglabplh_tech.reflect.clojure.api.example;

import io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.TransportSpec;

import java.util.Objects;

public class InterfaceImplBase extends InterfaceImplBaseAbstr
                                 implements InterfaceEx {

    private InterfaceImplBase() {

    }

    public static InterfaceImplBase instance() {
        return new InterfaceImplBase();
    }

    @Override
    public void newDocument(DocumentExample doc) throws DocumentException {
            Runnable perform = new Runnable() {
                @Override
                public void run() {

                }
            };
    }

    @Override
    public void retrieveDocument(DocumentExample doc) throws DocumentException {

    }

    @Override
    public void updateDocument(DocumentExample doc) throws DocumentException {

    }

    @Override
    public void deleteDocument(DocumentExample doc) throws DocumentException {

    }

    public static class TestInner {

        @TransportSpec
        private final String firstName;

        @TransportSpec
        private final String lastName;

        public TestInner(String firtName, String lastName) {
            this.firstName = firtName;
            this.lastName = lastName;
        }

        public String firstName() {
            return firstName;
        }

        public String lastName() {
            return lastName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestInner testInner = (TestInner) o;
            return Objects.equals(firstName, testInner.firstName) && Objects.equals(lastName, testInner.lastName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstName, lastName);
        }



        public byte[] getBytes() {
            return firstName.getBytes();
        }

        @Override
        public String toString() {
            return "TestInner{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    '}';
        }
    }
}
