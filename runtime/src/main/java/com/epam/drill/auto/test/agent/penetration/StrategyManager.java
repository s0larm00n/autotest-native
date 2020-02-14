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

    public static StrategyManager self = new StrategyManager();
    public Set<Strategy> strategies = new HashSet<>();

    public StrategyManager() {
        new JUnitPenetration();
        new TestNGPenetration();
        new JMeterPenetration();
    }

    public static byte[] process(CtClass ctClass) throws NotFoundException, CannotCompileException, IOException {
        for (Strategy strategy : self.strategies) {
            if (strategy.permits(ctClass)) return strategy.instrument();
        }
        return null;
    }

}
