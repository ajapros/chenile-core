package org.chenile.stm.test.dsl;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

public class AssemblyAction implements STMAction<AssemblyLineModel>, STMTransitionAction<AssemblyLineModel> {
    private String logMessage = null;
    public AssemblyAction(){}
    public AssemblyAction(String logMessage){
        this.logMessage = logMessage;
    }
    @Override
    public void execute(AssemblyLineModel assemblyLine) throws Exception {
        assemblyLine.log.add(logMessage);
    }
    @Override
    public void doTransition(AssemblyLineModel assemblyLine, Object transitionParam, State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        assemblyLine.log.add(eventId);
    }
}
