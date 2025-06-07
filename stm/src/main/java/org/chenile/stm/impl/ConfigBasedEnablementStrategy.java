package org.chenile.stm.impl;

import org.chenile.stm.ConfigProvider;
import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.State;
import org.chenile.stm.model.EventInformation;
import org.chenile.stm.model.FlowDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.*;

/**
 * Provides an enablement strategy based on configuration. This requires a configuration provider.
 *
 */
public class ConfigBasedEnablementStrategy implements EnablementStrategy {
    public static final String ENABLED_PROPERTY = "enabled";
    public static final String ADD_TRANSITION_PROPERTY = "transition.add";
    public static final String ADD_STATE_PROPERTY = "state.add";
    String enabledProperty = ENABLED_PROPERTY;
    String addTransitionProperty = ADD_TRANSITION_PROPERTY;
    String addStateProperty = ADD_STATE_PROPERTY;
    static final String ENABLEMENT = "enablement";
    String prefix = "";
    ConfigProvider configProvider;
    public ConfigBasedEnablementStrategy(ConfigProvider configProvider){
        this.configProvider = configProvider;
    }
    public ConfigBasedEnablementStrategy(ConfigProvider configProvider, String prefix){
        this.configProvider = configProvider;
        if (prefix != null && !prefix.endsWith("."))
            this.prefix = prefix + "." ;
        else
            this.prefix = prefix;
    }
    public void setEnabledProperty(String enabledProperty){
        this.enabledProperty = enabledProperty;
    }
    public void setAddTransitionProperty(String addTransitionProperty){
        this.addTransitionProperty = addTransitionProperty;
    }
    public void setAddStateProperty(String p){
        this.addStateProperty = p;
    }

    @Override
    public boolean isStateEnabled(StateDescriptor sd) {
        String prop = prefix + sd.getFlowId() + "." + sd.getId() + "." + enabledProperty;
        return propEnabled(prop);
    }

    /**
     *
     * @param sd state descriptor
     * @param eventInformation - event information
     * @return
     * <p></p>For every state or event it looks for a property in the configuration which is named after the
     *  * state or event. For example to enable state State1 on entity Entity1, we will need Entity1.State1.enabled = true
     *  * If this config is missing or set to true then the State is enabled. If it is explicitly set to false
     *  * then it is disabled.
     *  * <p>Similarly, for an event Event1 that belongs to state State1 of entity Entity1 the property
     *  * Entity1.State1.Event1.enabled must be explicitly set to false. Else if it is missing or set to true,
     *  * then the event is considered enabled.
     */
    @Override
    public boolean isEventEnabled(StateDescriptor sd, EventInformation eventInformation) {
        String prop = prefix +  sd.getFlowId() + "." + sd.getId() + "." + eventInformation.getEventId() + "." + enabledProperty;
        return propEnabled(prop);
    }

    /**
     *
     * @param sd state descriptor
     * @return
     * Allows for the addition of transitions in the property files.
     */
    @Override
    public Map<String,Transition> addDynamicTransitions(StateDescriptor sd) {
        Map<String,Transition> transitions = new HashMap<>();
        String pfx = prefix +  sd.getFlowId() + "." + sd.getId() + "." + addTransitionProperty + ".";
        Map<String, String> props = configProvider.getProperties(pfx);
        for (Map.Entry<String,String> prop: props.entrySet()){
            String propName = prop.getKey();
            String propValue = prop.getValue();
            String eventId = propName.substring(pfx.length());
            Transition transition = new Transition();
            transition.setStateId(sd.getId());
            transition.setFlowId(sd.getFlowId());
            transition.setEventId(eventId);
            transition.setNewStateId(propValue);
            transition.setNewFlowId(sd.getFlowId());
            transitions.put(eventId,transition);
            addEventInformationToTransition(transition,sd);
            transition.addMetaData(ENABLEMENT,"true");
            addMetadataToTransition(transition,sd);
        }
        return transitions;
    }

    /**
     *
     * @param transition - the transition which needs to be enhanced
     * @param sd the state descriptor to which the transition is attached
     *
     */
    private void addEventInformationToTransition(Transition transition, StateDescriptor sd){
        if (sd.getFlow() == null) return; // shouldn't happen
        EventInformation eventInformation = sd.getFlow().getFlowStore().
                getEventInformation(transition.getEventId());
        if (eventInformation == null) return;
        for(Map.Entry<String,String> entry: eventInformation.getMetadata().entrySet()){
            transition.addMetaData(entry.getKey(),entry.getValue());
        }
        transition.setTransitionAction(eventInformation.getTransitionAction());
    }

    /**
     *
     * @param t - the transition
     * @param sd - the state descriptor
     * Allows for metadata to be added to a transition in property file.
     */
    public void addMetadataToTransition(Transition t,StateDescriptor sd){
        String p = prefix + sd.getFlowId() + "." + sd.getId() + "." + t.getEventId() + ".meta." ;
        Map<String,String> props = configProvider.getProperties(p);
        for (Map.Entry<String,String> prop: props.entrySet()){
            String name = prop.getKey();
            String value = prop.getValue();
            name = name.substring(p.length());
            t.addMetaData(name,value);
            t.addMetaData(ENABLEMENT,"true");
        }
        p = prefix + sd.getFlowId() + "." + sd.getId() + "." + t.getEventId() + ".newStateId" ;
        String value = configProvider.valueOf(p);
        if (value == null) return;
        String[] s = value.split("\\.");
        if (s.length <= 1){
            t.setNewStateId(value);
            return;
        }
        if (s.length > 2) return; // ignore if there are more than dots in the value of the property
        t.setNewFlowId(s[0]);
        t.setNewStateId(s[1]);
    }

    @Override
    public StateDescriptor getStateInfo(State state, FlowDescriptor flowDescriptor) {
        String flowId = state.getFlowId();
        String stateId = state.getStateId();
        if (flowId == null || stateId == null) return null;
        String propName = prefix + flowId + "." + addStateProperty + "." + stateId ;
        String val = configProvider.valueOf(propName);
        if (val == null) return null;

        StateDescriptor sd = new StateDescriptor();
        sd.setManualState(true);
        sd.setId(stateId);
        sd.setFlowId(flowId);
        sd.setFlow(flowDescriptor);
        addMetadataToState(sd);
        sd.addMetaData(ENABLEMENT,"true");
        return sd;
    }

    @Override
    public Collection<StateDescriptor> addDynamicStates(FlowDescriptor flowInfo) {
        List<StateDescriptor> list = new ArrayList<>();
        String pfx = prefix + flowInfo.getId() + "." + addStateProperty + "." ;
        Map<String, String> props = configProvider.getProperties(pfx);
        for (Map.Entry<String,String> prop: props.entrySet()){
            String propName = prop.getKey();
            String stateId = propName.substring(pfx.length());
            StateDescriptor sd = new StateDescriptor();
            list.add(sd);
            sd.setId(stateId);
            sd.setFlowId(flowInfo.getId());
            sd.setFlow(flowInfo);
            sd.setManualState(true);
            sd.addMetaData(ENABLEMENT,"true");
            addMetadataToState(sd);
        }

        return list;
    }

    public void addMetadataToState(StateDescriptor sd){
        String p = prefix + sd.getFlowId() + "." + sd.getId() + ".meta." ;
        Map<String,String> props = configProvider.getProperties(p);
        for (Map.Entry<String,String> prop: props.entrySet()){
            String name = prop.getKey();
            String value = prop.getValue();
            name = name.substring(p.length());
            sd.addMetaData(name,value);
            sd.addMetaData(ENABLEMENT,"true");
        }
    }

    private boolean propEnabled(String prop){
        String val = configProvider.valueOf(prop);
        return val == null || Boolean.parseBoolean(val);
    }
}
