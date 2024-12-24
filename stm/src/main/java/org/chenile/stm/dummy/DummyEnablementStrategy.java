package org.chenile.stm.dummy;

import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.State;
import org.chenile.stm.model.EventInformation;
import org.chenile.stm.model.FlowDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DummyEnablementStrategy implements EnablementStrategy {
    @Override
    public boolean isStateEnabled(StateDescriptor sd) {
        return true;
    }

    @Override
    public boolean isEventEnabled(StateDescriptor sd, EventInformation eventInformation) {
        return true;
    }

    @Override
    public Map<String, Transition> addDynamicTransitions(StateDescriptor sd) {
        return Map.of();
    }

    @Override
    public StateDescriptor getStateInfo(State state, FlowDescriptor flowDescriptor) {
        return null;
    }

    @Override
    public Collection<StateDescriptor> addDynamicStates(FlowDescriptor flowInfo) {
        return List.of();
    }

    @Override
    public void addMetadataToTransition(Transition t, StateDescriptor stateDescriptor) {

    }

    @Override
    public void addMetadataToState(StateDescriptor sd) {

    }
}
