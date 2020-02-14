package com.epam.drill.auto.test.agent;

public enum SupportedClass {
    JMETER_HTTP_SAMPLER("org/apache/jmeter/protocol/http/sampler/HTTPHC4Impl"),
    UNDEFINED("");

    public String asString;

    SupportedClass(String name) {
        this.asString = name;
    }

    public static SupportedClass convert(String str) {
        for (SupportedClass className : SupportedClass.values()) {
            if (className.asString.equals(str)) return className;
        }
        return UNDEFINED;
    }

}