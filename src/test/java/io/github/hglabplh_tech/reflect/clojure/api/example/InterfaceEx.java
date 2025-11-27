package io.github.hglabplh_tech.reflect.clojure.api.example;

import io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.Category;
import io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec.MethodDescriber;

public interface InterfaceEx {

    @MethodDescriber(category = Category.PROJECT,
            shortDescription = "make a new document",
            execSteps = {"create in stor", "add attachments", "submit store REST req"} )

    void newDocument(DocumentExample doc) throws DocumentException;

    void retrieveDocument(DocumentExample doc) throws DocumentException;

    void updateDocument(DocumentExample doc) throws DocumentException;

    void deleteDocument(DocumentExample doc) throws DocumentException;

    default boolean testDocument () throws IllegalArgumentException {
        return true;
    }
}
