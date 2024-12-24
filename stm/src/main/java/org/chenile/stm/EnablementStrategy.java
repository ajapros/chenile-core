package org.chenile.stm;

import org.chenile.stm.model.EventInformation;
import org.chenile.stm.model.FlowDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.Collection;
import java.util.Map;

/**
 * Implementations of this interface enable/disable states and events based out of various criteria.
 * This allows the state transition diagram to be customized using configurations.
 * {@link org.chenile.stm.impl.ConfigBasedEnablementStrategy} is the easiest way to achieve this.
 * But there can be alternative implementations possible for selectively enabling or disabling states
 * and events/transitions based on various criteria.
 */
public interface EnablementStrategy {
    public boolean isStateEnabled(StateDescriptor sd);

    public boolean isEventEnabled(StateDescriptor sd, EventInformation eventInformation);

    public Map<String,Transition> addDynamicTransitions(StateDescriptor sd);

    public StateDescriptor getStateInfo(State state, FlowDescriptor flowDescriptor);

    public Collection<StateDescriptor> addDynamicStates(FlowDescriptor flowInfo);

    public void addMetadataToTransition(Transition t, StateDescriptor stateDescriptor);

    public void addMetadataToState(StateDescriptor sd);
}
