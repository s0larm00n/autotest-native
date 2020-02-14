package com.epam.drill.auto.test.agent;

import java.util.ArrayList;
import java.util.List;

public enum SupportedAnnotation {
    JUNIT_TEST("@org.junit.jupiter.api.Test"),
    JUNIT_PARAM_TEST("@org.junit.jupiter.params.ParameterizedTest"),
    TEST_NG_TEST("@org.testng.annotations.Test");

    public String asString;

    SupportedAnnotation(String name) {
        this.asString = name;
    }

    public static List<String> stringValues() {
        List<String> result = new ArrayList<>();
        for (SupportedAnnotation annotation : SupportedAnnotation.values()) {
            result.add(annotation.asString);
        }
        return result;
    }

}