package io.github.hglabplh_tech.reflect.clojure.api.example;

public abstract class InterfaceImplBaseAbstr
        implements InterfaceEx {
    @Override
    public void newDocument(DocumentExample doc) throws DocumentException {

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
    public enum MyPlan {
        TEST_OK(1, "test-ok"),
        TEST_NOK(6, "test-nok"),
        TEST_OK_CLONE(7, "the-clone"),
        TEST_MAYBE_OK(8, "test-maybe-ok"),
        ;

        private final Integer codeNum;

        private final String displayName;

        MyPlan(Integer codeNum, String displayName) {
            this.codeNum = codeNum;
            this.displayName = displayName;
        }

        public Integer codeNum() {
            return codeNum;
        }

        public String displayName() {
            return displayName;
        }
    }
}
