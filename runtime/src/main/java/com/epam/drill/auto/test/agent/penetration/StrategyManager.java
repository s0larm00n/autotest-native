package com.epam.drill.auto.test.agent.penetration;

import com.epam.drill.auto.test.agent.penetration.jmeter.JMeterPenetration;
import com.epam.drill.auto.test.agent.penetration.junit.JUnitPenetration;
import com.epam.drill.auto.test.agent.penetration.testng.TestNGPenetration;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StrategyManager {

    public static Set<Strategy> strategies = new HashSet<>();

    public static void initialize() {
        strategies.add(new JMeterPenetration());
        strategies.add(new JUnitPenetration());
        strategies.add(new TestNGPenetration());
    }

    public static byte[] process(CtClass ctClass) throws NotFoundException, CannotCompileException, IOException {
        for (Strategy strategy : strategies) {
            if (strategy.permit(ctClass)) return strategy.instrument(ctClass);
        }
        return null;
    }

}
