package com.epam.drill.auto.test.agent.penetration.jmeter;

import com.epam.drill.auto.test.agent.AgentClassTransformer;
import com.epam.drill.auto.test.agent.penetration.Strategy;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.drill.auto.test.agent.AgentClassTransformer.GLOBAL_SPY;

public class JMeterPenetration extends Strategy {

    private String testNameSourceClass = "org.apache.jmeter.protocol.http.sampler.HTTPHC4Impl";
    private CtClass lastScannedClass;

    public JMeterPenetration(){
        super();
    }

    public boolean permits(CtClass ctClass){
        lastScannedClass = ctClass;
        return lastScannedClass.getName().equals(testNameSourceClass);
    }

    public byte[] instrument() throws CannotCompileException, IOException, NotFoundException {
        CtMethod setupRequestMethod = lastScannedClass.getMethod(
                "setupRequest",
                "(Ljava/net/URL;Lorg/apache/http/client/methods/HttpRequestBase;" +
                        "Lorg/apache/jmeter/protocol/http/sampler/HTTPSampleResult;)V"
        );
        setupRequestMethod.insertBefore(
                "String drillTestName = $3.getSampleLabel();\n" +
                        GLOBAL_SPY + ".setTestName(drillTestName);\n"
        );
        return lastScannedClass.toBytecode();
    }

}
