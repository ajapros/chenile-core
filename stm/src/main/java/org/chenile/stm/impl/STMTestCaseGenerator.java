package org.chenile.stm.impl;

import org.chenile.stm.State;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.*;

public class STMTestCaseGenerator {
    STMFlowStoreImpl flowStore;
    public STMTestCaseGenerator(STMFlowStoreImpl flowStore){
        this.flowStore = flowStore;
    }

    public String toTestCase(){
        StringBuilder stringBuilder = new StringBuilder("[\n");
        boolean first = true;
        for(Transition t :constructFlow()){
            if (!first) stringBuilder.append(",");
            else first=false;
            stringBuilder.append("""
                    {
                        "from": "%s",
                        "event": "%s",
                        "to": "%s"
                    }
                    """.formatted(t.getStateId(),
                    t.getEventId(),t.getNewStateId()));
        }
        return stringBuilder.append("]\n").toString();
    }

    public List<Transition> constructFlow(){
        List<Transition> list = new ArrayList<>();
        StateDescriptor sd = getInitialStateDescriptor();
        if(sd == null)return null;
        Set<State> visitedStates = new HashSet<>();
        addToFlow(sd,visitedStates,list);
        return list;
    }

    private void addToFlow(StateDescriptor sd,Set<State> visitedStates,
                           List<Transition> list){
        visitedStates.add(new State(sd.getId(),sd.getFlowId()));
        for (Transition t: sd.getTransitions().values()){
            if (t.getFlowId().equals(t.getNewFlowId()) &&
                t.getStateId().equals(t.getNewStateId()))
                continue;
            State newState = new State(t.getNewStateId(),t.getNewFlowId());
            if(!visitedStates.contains(newState)){
                sd = flowStore.getStateInfo(newState);
                list.add(t);
                addToFlow(sd,visitedStates,list);
            }
        }
    }

    private StateDescriptor getInitialStateDescriptor(){
        for (StateDescriptor sd: flowStore.getAllStates()){
            if (sd.isInitialState()){
                return sd;
            }
        }
        return null;
    }
    private boolean isInMainFlow(StateDescriptor sd){
        return isInMainFlow(sd.getMetadata());
    }

    private boolean isInMainFlow(Transition t){
        return isInMainFlow(t.getMetadata());
    }

    private boolean isInMainFlow(Map<String, String> metadata) {
        String val = metadata.get("mainFlow");
        return Boolean.parseBoolean(val);
    }
}
