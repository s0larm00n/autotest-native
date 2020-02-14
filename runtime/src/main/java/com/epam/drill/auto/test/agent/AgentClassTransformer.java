package com.epam.drill.auto.test.agent;

import com.epam.drill.auto.test.agent.penetration.StrategyManager;
import javassist.*;

import java.io.IOException;

public class AgentClassTransformer {
    public static final String GLOBAL_SPY = "com.epam.drill.auto.test.agent.GlobalSpy.self()";

    public static byte[] transform(String className, ClassLoader classLoader) {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(classLoader));
            CtClass ctClass = null;
            try {
                ctClass = pool.get(formatClassName(className));

            } catch (NotFoundException nfe) {
                System.out.println("Class " + className + " not found by given class loader!");
            }
            return insertTestNames(ctClass);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            return null;
        }
    }

    private static byte[] insertTestNames(CtClass ctClass) {
        try {
            StrategyManager.process(ctClass);
        } catch (CannotCompileException | IOException | NotFoundException e) {
            System.out.println("Error while instrumenting class: " + ctClass.getName() + "\n Message: " + e.getMessage());
        }
        return null;
    }

    private static String formatClassName(String className) {
        return className.replace("/", ".");
    }

}
