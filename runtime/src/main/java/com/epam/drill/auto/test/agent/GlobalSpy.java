package com.epam.drill.auto.test.agent;

@SuppressWarnings("ALL")
public class GlobalSpy {
    private static GlobalSpy inst = new GlobalSpy();
    public ThreadLocal<String> testNameStorage = new ThreadLocal<String>();

    private native void memorizeTestName(String testName);

    public static GlobalSpy self() {
        return inst;
    }

    public void setTestName(String name) {
        memorizeTestName(name);
        testNameStorage.set(name);
    }

}
