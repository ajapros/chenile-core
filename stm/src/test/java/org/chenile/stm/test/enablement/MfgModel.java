package org.chenile.stm.test.enablement;

import org.chenile.stm.State;
import org.chenile.stm.StateEntity;

import java.util.ArrayList;
import java.util.List;

public class MfgModel implements StateEntity {
    State currentState;
    @Override
    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    @Override
    public State getCurrentState() {
        return currentState;
    }
    public List<String> log = new ArrayList<String>();
}
