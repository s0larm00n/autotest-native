package com.epam.drill.auto.test.agent.penetration.testng;

import com.epam.drill.auto.test.agent.AgentClassTransformer;
import com.epam.drill.auto.test.agent.penetration.Strategy;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestNGPenetration extends Strategy {

    private Set<String> supportedAnnotations = new HashSet<>();
    private ThreadLocal<List<CtMethod>> lastScannedMethods = new ThreadLocal<>();

    public TestNGPenetration() {
        supportedAnnotations.add("@org.testng.annotations.Test");
    }

    public boolean permit(CtClass ctClass) {
        lastScannedMethods.set(scanMethods(ctClass));
        return !lastScannedMethods.get().isEmpty();
    }

    public byte[] instrument(CtClass ctClass) throws CannotCompileException, IOException {
        for (CtMethod method : lastScannedMethods.get()) {
            method.insertBefore(AgentClassTransformer.GLOBAL_SPY + ".setTestName(\"" + method.getName() + "\");");
        }
        return ctClass.toBytecode();
    }

    private List<CtMethod> scanMethods(CtClass ctClass) {
        ArrayList<CtMethod> ctMethods = new ArrayList<>();
        for (CtMethod m : ctClass.getMethods()) {
            try {
                for (Object an : m.getAnnotations()) {
                    if (annotationSupported(an.toString())) {
                        ctMethods.add(m);
                        break;
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                //TODO: process if needed
            }
        }
        return ctMethods;
    }

    private Boolean annotationSupported(String annotation) {
        for (String supported : supportedAnnotations) {
            if (annotation.startsWith(supported)) return true;
        }
        return false;
    }

}
