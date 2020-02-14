package com.epam.drill.auto.test.agent.penetration.junit;

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

public class JUnitPenetration extends Strategy {

    private Set<String> supportedAnnotations = new HashSet<>();
    private CtClass lastScannedClass;
    private List<CtMethod> lastScannedMethods;

    public JUnitPenetration(){
        super();
        supportedAnnotations.add("@org.junit.jupiter.api.Test");
        supportedAnnotations.add("@org.junit.jupiter.params.ParameterizedTest");
    }

    public boolean permits(CtClass ctClass){
        lastScannedClass = ctClass;
        lastScannedMethods = scanMethods(ctClass);
        return !lastScannedMethods.isEmpty();
    }

    public byte[] instrument() throws CannotCompileException, IOException {
        for (CtMethod method : lastScannedMethods) {
            method.insertBefore(AgentClassTransformer.GLOBAL_SPY + ".setTestName(\"" + method.getName() + "\");");
        }
        return lastScannedClass.toBytecode();
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
