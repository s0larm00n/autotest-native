package com.epam.drill.auto.test.agent;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentClassTransformer {
    private static final String GLOBAL_SPY = "com.epam.drill.auto.test.agent.GlobalSpy.self()";

    public static JByteArray transform(String className, ClassLoader classLoader) {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(classLoader));
            CtClass ctClass = null;
            try {
                ctClass = pool.get(formatClassName(className));

            } catch (NotFoundException nfe) {
                System.out.println("Class " + className + " not found by given class loader!");
            }
            byte[] result = insertTestNames(ctClass);
            if (result != null) {
                return new JByteArray(result);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            return null;
        }
    }

    private static byte[] insertTestNames(CtClass cc) {
        List<CtMethod> ctMethods = filterMethods(cc);
        if (ctMethods.isEmpty()) return null;
        byte[] result = null;
        try {
            for (CtMethod method : ctMethods) {
                method.insertBefore(GLOBAL_SPY + ".setTestName(\"" + method.getName() + "\");");
            }
            result = cc.toBytecode();
        } catch (CannotCompileException | IOException cce) {
            System.out.println("Could not compile class: " + cc.getName());
        }
        return result;
    }

    private static List<CtMethod> filterMethods(CtClass ctClass) {
        ArrayList<CtMethod> ctMethods = new ArrayList<>();
        for (CtMethod m : ctClass.getMethods()) {
            try {
                for (Object an : m.getAnnotations()) {
                    if (an.toString().startsWith("@org.junit.jupiter.api.Test") ||
                            an.toString().startsWith("@org.junit.jupiter.params.ParameterizedTest")) {
                        ctMethods.add(m);
                        break;
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                //System.out.println("Could not find annotations for " + m.getLongName());
            }
        }
        return ctMethods;
    }

    private static String formatClassName(String className) {
        return className.replace("/", ".");
    }

}
