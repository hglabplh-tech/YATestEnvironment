package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

public interface HumanResource {

    String getEmployeeName(String team, EmployeeType type);

    default Stats retrieveStatistics(String name, EmployeeType type) {
        Stats test = new Stats(Statistics.COUNT_OF_FUNS_PH, 8.0, 9.9);
        Double theAvg = test.average();
        return test;
    }


    record Stats(Statistics statsType, Double average, Double total) {

    }

    enum Statistics {
        SALARY_PH,
        PERFORMACE_PH,
        COUNT_OF_FUNS_PH,
        LINES_OF_CODE_PH,
        NONE,;
    }

}
