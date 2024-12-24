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
    public static final String ADD_PROPERTY = "transition.add.to";
    String enabledProperty = ENABLED_PROPERTY;
    String addTransitionProperty = ADD_PROPERTY;
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

    @Override
    public boolean isStateEnabled(StateDescriptor sd) {
        String prop = prefix + sd.getFlowId() + "." + sd.getId() + "." + enabledProperty;
        return propEnabled(prop);
    }

    /**
     *
     * @param sd
     * @param eventInformation
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
     * @param sd
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
            String toState = propName.substring(pfx.length());
            Transition transition = new Transition();
            transition.setStateId(sd.getId());
            transition.setFlowId(sd.getFlowId());
            transition.setEventId(propValue);
            transition.setNewStateId(toState);
            transition.setNewFlowId(sd.getFlowId());
            transitions.put(propValue,transition);
            addEventInformationToTransition(transition,sd);
            addMetadataToTransition(transition,sd);
        }
        return transitions;
    }

    /**
     *
     * @param transition
     * @param sd
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
     * @param t
     * @param sd
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
        }
    }

    @Override
    public StateDescriptor getStateInfo(State state, FlowDescriptor flowDescriptor) {
        String flowId = state.getFlowId();
        String stateId = state.getStateId();
        if (flowId == null || stateId == null) return null;
        String propName = prefix + "state.add." + stateId +".in" ;
        String val = configProvider.valueOf(propName);
        if (val == null) return null;
        if (val.equals(flowId)){
            StateDescriptor sd = new StateDescriptor();
            sd.setManualState(true);
            sd.setId(stateId);
            sd.setFlowId(flowId);
            sd.setFlow(flowDescriptor);
            addMetadataToState(sd);
            return sd;
        }
        return null;
    }

    @Override
    public Collection<StateDescriptor> addDynamicStates(FlowDescriptor flowInfo) {
        List<StateDescriptor> list = new ArrayList<>();
        String pfx = prefix + "state.add." ;
        Map<String, String> props = configProvider.getProperties(pfx);
        for (Map.Entry<String,String> prop: props.entrySet()){
            String propName = prop.getKey();
            String propValue = prop.getValue();
            String stateId = propName.substring(pfx.length());
            stateId = stateId.substring(0,stateId.indexOf('.'));
            StateDescriptor sd = new StateDescriptor();
            list.add(sd);
            sd.setId(stateId);
            sd.setFlowId(propValue);
            sd.setFlow(flowInfo);
            sd.setManualState(true);
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
        }
    }

    private boolean propEnabled(String prop){
        String val = configProvider.valueOf(prop);
        return val == null || Boolean.parseBoolean(val);
    }
}
