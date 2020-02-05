package com.epam.drill.auto.test.agent;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentClassTransformer {
    private static final String GLOBAL_SPY = "com.epam.drill.auto.test.agent.GlobalSpy.self()";

    public static JByteArray transform(String className, ClassLoader classLoader) {
        if (className != null && classLoader != null) {
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(classLoader));
            CtClass ctClass = null;
            try {
                ctClass = pool.get(formatClassName(className));

            } catch (NotFoundException nfe) {
                System.out.println("Class not found by given class loader!");
            }
            byte[] result = insertTestNames(filterMethods(ctClass), ctClass);
            if (result != null) {
                return new JByteArray(result);
            }
        }
        return null;
    }

    private static byte[] insertTestNames(List<CtMethod> ctMethods, CtClass cc) {
        byte[] result = null;
        if (!ctMethods.isEmpty()) {
            try {
                for (CtMethod method : ctMethods) {
                    method.insertBefore(GLOBAL_SPY + ".setTestName(\"" + method.getName() + "\");");
                }
                result = cc.toBytecode();
            } catch (CannotCompileException | IOException cce) {
                System.out.println("Could not compile class: " + cc.getName());
            }

        }
        return result;
    }

    private static List<CtMethod> filterMethods(CtClass ctClass) {
        ArrayList<CtMethod> ctMethods = new ArrayList<>();
        for (CtMethod m : ctClass.getMethods()) {
            System.out.println("METHOD: " + m.getLongName());
            try {
                for (Object an : m.getAnnotations()) {
                    System.out.println("ANNOTATION: " + an.toString());
                    if (an.toString().startsWith("@org.junit.jupiter.api.Test") ||
                            an.toString().startsWith("@org.junit.jupiter.params.ParameterizedTest")) {
                        ctMethods.add(m);
                        break;
                    }
                }
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Could not find annotations for " + m.getLongName());
            }
        }
        return ctMethods;
    }

    private static String formatClassName(String className) {
        return className.replace("/", ".");
    }

}
