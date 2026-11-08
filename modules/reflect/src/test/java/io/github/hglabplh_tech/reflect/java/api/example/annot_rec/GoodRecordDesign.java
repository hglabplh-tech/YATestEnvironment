package io.github.hglabplh_tech.reflect.java.api.example.annot_rec;

public record GoodRecordDesign (String name, InnerRecord details) {
    // This nested record is implicitly static!
    public record InnerRecord(int id, String description) {}
}
