package com.epam.drill.auto.test.agent;

@SuppressWarnings("ALL")
public class GlobalSpy {
    private static GlobalSpy inst = new GlobalSpy();

    public native void memorizeTestName(String testName);

    public static GlobalSpy self() {
        return inst;
    }

    public void setTestName(String name) {
        memorizeTestName(name);
    }

}
