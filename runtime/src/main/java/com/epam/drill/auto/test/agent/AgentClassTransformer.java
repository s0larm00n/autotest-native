package com.epam.drill.auto.test.agent;

import com.epam.drill.auto.test.agent.penetration.StrategyManager;
import javassist.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class AgentClassTransformer {
    public static final String GLOBAL_SPY = "com.epam.drill.auto.test.agent.GlobalSpy.self()";

    public static byte[] transform(String className, ClassLoader classLoader) {
        try {
            CtClass ctClass = getCtClass(className, classLoader);
            return insertTestNames(ctClass);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            return null;
        }
    }

    private static byte[] insertTestNames(CtClass ctClass) {
        byte[] result = null;
        try {
            result = StrategyManager.process(ctClass);
        } catch (CannotCompileException | IOException | NotFoundException e) {
            System.out.println("Error while instrumenting class: " + ctClass.getName() + "\n Message: " + e.getMessage());
        }
        return result;
    }

    @Nullable
    private static CtClass getCtClass(String className, ClassLoader classLoader) {
        ClassPool pool = ClassPool.getDefault();
        pool.appendClassPath(new LoaderClassPath(classLoader));
        CtClass ctClass = null;
        try {
            ctClass = pool.get(formatClassName(className));

        } catch (NotFoundException nfe) {
            System.out.println("Class " + className + " not found by given class loader!");
        }
        return ctClass;
    }

    private static String formatClassName(String className) {
        return className.replace("/", ".");
    }

}
