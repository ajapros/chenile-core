package org.chenile.stm.test.enablement;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

public class MfgAction implements STMAction<MfgModel>, STMTransitionAction<MfgModel> {
    private String logMessage = null;

    public MfgAction(){

    }
    public MfgAction(String logMessage){
        this.logMessage = logMessage;
    }
    @Override
    public void execute(MfgModel mfgModel) throws Exception {
        mfgModel.log.add(logMessage);
    }

    @Override
    public void doTransition(MfgModel mfgModel, Object transitionParam, State startState, String eventId, State endState, STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception {
        mfgModel.log.add(eventId);
    }
}
