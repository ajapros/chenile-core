package org.chenile.stm.dummy;

import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMAction;

public class DummyAction implements STMAction<StateEntity> {

    @Override
    public void execute(State startState, State endState, StateEntity stateEntity) throws Exception {

    }
}
