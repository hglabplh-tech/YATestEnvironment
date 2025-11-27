package io.github.hglabplh_tech.reflect.clojure.api.example.annot_rec;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EmployeeType {
    MANAGER("manager", ContractType.PARTNER),
    DEVELOPER("developer", ContractType.EMPLOYEE),
    TEST_MANAGER("Test Manager", ContractType.SUBCONTRACTOR),
    NONE("NONE", ContractType.NONE);

    private final String displayName;

    private final ContractType contractType;

    EmployeeType(String displayName, ContractType contractType) {
        this.displayName = displayName;
        this.contractType = contractType;
    }

    public static @Nonnull EmployeeType findByDisplayName(@Nonnull String displayName) {
        return Arrays.stream(EmployeeType.values())
                .filter(actVal -> actVal.displayName().equals(displayName))
                .findFirst()
                .orElse(EmployeeType.NONE);
    }


    public static @Nonnull List<EmployeeType>
        findAllByContractType(@Nonnull ContractType contractType) {
        return Arrays.stream(EmployeeType.values())
                .filter(actVal -> actVal.contractType().equals(contractType))
                .collect(Collectors.toList());
    }
    public String displayName() {
        return displayName;
    }

    public ContractType contractType() {
        return contractType;
    }


    public enum ContractType {
        EMPLOYEE,
        SUBCONTRACTOR,
        PARTNER,
        NONE,
        ;
    }
}
