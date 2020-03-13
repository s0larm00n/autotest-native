package com.epam.drill.auto.test.agent;

import com.epam.drill.auto.test.agent.penetration.StrategyManager;
import javassist.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class AgentClassTransformer {

    public static final String CLASS_NAME = "com.epam.drill.auto.test.agent.AgentClassTransformer";

    public static native void memorizeTestName(String testName);

    public static byte[] transform(String className, byte[] classBytes) {
        try {
            CtClass ctClass = getCtClass(className, classBytes);
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
    private static CtClass getCtClass(String className, byte[] classBytes) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = null;
        try {
            pool.insertClassPath(new ByteArrayClassPath(className, classBytes));
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
