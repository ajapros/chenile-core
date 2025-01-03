package org.chenile.stm.dummy;

import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.STMFlowStoreImpl;

public class DummyStore extends STMFlowStoreImpl {
    DummyTransitionAction dummyTransitionAction = new DummyTransitionAction();
    DummyAction dummyAction = new DummyAction();
    DummyScriptingStrategy dummyScriptingStrategy = new DummyScriptingStrategy();
    DummyAutomaticStateComputation dummyAutomaticStateComputation = new DummyAutomaticStateComputation();
    DummySecurityStrategy dummySecurityStrategy = new DummySecurityStrategy();
    DummyRetrievalStrategy dummyRetrievalStrategy = new DummyRetrievalStrategy();
    DummyEnablementStrategy dummyEnablementStrategy = new DummyEnablementStrategy();
    @Override
    public Object makeTransitionAction(String componentName,boolean enable) throws STMException {
        return  dummyTransitionAction;
    }

    @Override
    public Object makeAction(String componentName, boolean enableInlineScriptsInProperties) throws STMException {
        return dummyAction;
    }

    @Override
    public Object makeAutomaticStateComputation(String componentName, boolean enableInlineScriptsInProperties) throws STMException {
        return dummyAutomaticStateComputation;
    }

    @Override
    public Object makeScriptingStrategy(String componentName) throws STMException {
        return dummyScriptingStrategy;
    }

    @Override
    public Object makeRetrievalStrategy(String componentName) throws STMException {
        return dummyRetrievalStrategy;
    }

    @Override
    public Object makeSecurityStrategy(String componentName) throws STMException {
        return dummySecurityStrategy;
    }

    @Override
    public EnablementStrategy makeEnablementStrategy(String componentName) throws STMException {
        return dummyEnablementStrategy;
    }
}