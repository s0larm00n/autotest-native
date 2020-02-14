package com.epam.drill.auto.test.agent.penetration;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;

public abstract class Strategy {

    public Strategy(){
        StrategyManager.self.strategies.add(this);
    }

    public abstract boolean permits(CtClass ctClass);

    public abstract byte[] instrument() throws CannotCompileException, IOException, NotFoundException;

}
