package com.epam.drill.auto.test.agent;

import javassist.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgentClassTransformer {
    private static final String GLOBAL_SPY = "com.epam.drill.auto.test.agent.GlobalSpy.self()";

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
            return insertTestNames(ctClass, className);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            return null;
        }
    }

    private static byte[] insertTestNames(CtClass cc, String rawClassName) {
        try {
            switch (SupportedClass.convert(rawClassName)) {
                case JMETER_HTTP_SAMPLER: {
                    return insertJMeterTestNameGathering(cc);
                }
                case UNDEFINED: {
                    return insertTestNamesByAnnotations(cc);
                }
            }
        } catch (CannotCompileException | IOException | NotFoundException e) {
            System.out.println("Error while instrumenting class: " + cc.getName() + "\n Message: " + e.getMessage());
        }
        return null;
    }

    private static byte[] insertJMeterTestNameGathering(CtClass cc) throws NotFoundException, CannotCompileException, IOException {
        CtMethod setupRequestMethod = cc.getMethod(
                "setupRequest",
                "(Ljava/net/URL;Lorg/apache/http/client/methods/HttpRequestBase;" +
                        "Lorg/apache/jmeter/protocol/http/sampler/HTTPSampleResult;)V"
        );
        setupRequestMethod.insertBefore(
                "String drillTestName = $3.getSampleLabel();\n" +
                        GLOBAL_SPY + ".setTestName(drillTestName);\n"
        );
        return cc.toBytecode();
    }

    private static byte[] insertTestNamesByAnnotations(CtClass cc) throws IOException, CannotCompileException {
        List<CtMethod> ctMethods = filterMethods(cc);
        if (ctMethods.isEmpty()) return null;
        for (CtMethod method : ctMethods) {
            method.insertBefore(GLOBAL_SPY + ".setTestName(\"" + method.getName() + "\");");
        }
        return cc.toBytecode();
    }

    private static List<CtMethod> filterMethods(CtClass ctClass) {
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

    private static String formatClassName(String className) {
        return className.replace("/", ".");
    }

    private static Boolean annotationSupported(String annotation) {
        boolean result = false;
        for (String supported : SupportedAnnotation.stringValues()) {
            result = result || annotation.startsWith(supported);
            if (result) break;
        }
        return result;
    }

}
