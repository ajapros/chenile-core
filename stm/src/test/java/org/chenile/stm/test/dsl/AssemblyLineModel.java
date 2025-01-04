package org.chenile.stm.test.dsl;

import org.chenile.stm.State;
import org.chenile.stm.StateEntity;

import java.util.ArrayList;
import java.util.List;

public class AssemblyLineModel implements StateEntity {
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
