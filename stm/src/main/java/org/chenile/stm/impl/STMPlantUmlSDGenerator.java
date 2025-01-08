package org.chenile.stm.impl;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.model.AutomaticStateDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates a PLANT UML state diagram for the State Transition Diagram
 */
public class STMPlantUmlSDGenerator {

    private final STMFlowStore stmFlowStore ;
    private final Map<String,Boolean> notOrphaned = new HashMap<>();
    public TransitionStyler transitionStyler = new TransitionStyler();
    public STMPlantUmlSDGenerator(STMFlowStore flowStore){
        this.stmFlowStore = flowStore;
        findIncomingForAllStates();
    }
    private static final String STARTUML = "@startuml\n";
    private static final String ENDUML = "@enduml\n";
    public static final String STATE = "state ";
    private static final String AUTO_STATE = " <<choice>> ";
    private static final String MAIN_PATH_STEREOTYPE = " <<MAIN_PATH>> ";
    public static final String NOTE_RIGHT_OF_ = "note right of ";
    private static final String TERMINAL = "[*]";
    private static final String MAIN_PATH = "mainPath";
    private static final String MAIN_PATH_LINE_STYLE = "[thickness=4,#Peru]";

    public String toStateDiagram(){
        return new StateStringBuilder().
            printStyles().
            renderStates().
            renderTransitions().
            printLegend().
            end().
            toString();
    }

    private class StateStringBuilder {
        StringBuilder stringBuilder;
        public StateStringBuilder(){
            this.stringBuilder = new StringBuilder(STARTUML);
        }
        public StateStringBuilder end(){
            stringBuilder.append(ENDUML);
            return this;
        }
        public String toString(){
            return stringBuilder.toString();
        }
        public  StateStringBuilder printStyles(){
            stringBuilder.append(""" 
               <style>
                    diamond {
                    BackgroundColor #palegreen
                    LineColor #green
                    LineThickness 2.5
                    }
                 </style>
                 skinparam state  {
                  BorderThickness<<MAIN_PATH>> 4
                  BorderColor<<MAIN_PATH>> Peru
                  BackgroundColor<<MAIN_PATH>> Bisque
                 }
                 skinparam state {
                    BackgroundColor<<orphaned>> OrangeRed
                 }
               """);
            return this;
        }
        public StateStringBuilder renderStates(){
            for(StateDescriptor sd: stmFlowStore.getAllStates()) {
                stringBuilder.append(STATE).append(sd.getId());
                if (!notOrphaned.get(sd.getId())){
                    stringBuilder.append(" <<orphaned>> ").append("\n");
                }
                else if (!sd.isManualState()){
                    stringBuilder.append(AUTO_STATE).append("\n");
                    stringBuilder.append(NOTE_RIGHT_OF_).append(sd.getId())
                            .append(" : **").append(sd.getId()).append("**");
                    Map<String, String> metadata = sd.getMetadata();
                    if (metadata != null && !metadata.isEmpty()){
                        for (Map.Entry<String,String> md: metadata.entrySet()){
                            stringBuilder.append("\\n").append(md.getKey()).append(":").append(md.getValue());
                        }
                    }
                    printComponentProperties(sd);
                }
                else if (isInMainPath(sd)){
                    stringBuilder.append(MAIN_PATH_STEREOTYPE);
                }
                stringBuilder.append("\n");
            }
            return this;
        }
        private StateStringBuilder renderTransitions(){
            for(StateDescriptor sd: stmFlowStore.getAllStates()) {
                Map<String, Transition> transitions = sd.getTransitions();
                if (sd.isInitialState()){
                    stringBuilder.append(TERMINAL);
                    paintTerminal(sd).append(sd.getId()).append("\n");
                }
                if (transitions.isEmpty()){
                    stringBuilder.append(sd.getId());
                    paintTerminal(sd).append(TERMINAL).append("\n");
                }
                StringBuilder selfNote = new StringBuilder();
                for(Transition t: transitions.values()){
                    if(t.getNewStateId().equals(sd.getId())){
                        selfNote.append(t.getEventId()).append("\n");
                    }else {
                        stringBuilder.append(sd.getId());
                        paintConnection(t).append(t.getNewStateId()).append(" : ");
                        if (isInMainPath(t)){
                            stringBuilder.append("<color:Peru>**").append(t.getEventId()).append("**");
                        }else {
                            stringBuilder.append(t.getEventId());
                        }
                        stringBuilder.append("\n");
                    }
                }
                if (!selfNote.isEmpty()){
                    stringBuilder.append(sd.getId()).append(" --> ").append(sd.getId()).append("\n");
                    stringBuilder.append("note on link #LightBlue\n").append(selfNote).append("end note\n");
                }
            }
            return this;
        }

        public StateStringBuilder printLegend(){
            stringBuilder.append("""
                legend right
                <#GhostWhite,#GhostWhite>|        |= __Legend__ |
                |<#OrangeRed>   | Orphaned State|
                |<#Peru>   | Main Path|
                |<#LightBlue> |Transitions without state change|
                |<#PaleGreen> |Automatic State Computations|
                endlegend
                """);
            return this;
        }

        private StringBuilder paintTerminal(StateDescriptor sd) {
            stringBuilder.append(" -");
            if (isInMainPath(sd)){
                stringBuilder.append(MAIN_PATH_LINE_STYLE);
            }

            return stringBuilder.append("-> ");
        }

        private StringBuilder paintConnection(Transition t){
            stringBuilder.append(" -");
            if (isInMainPath(t)) {
                stringBuilder.append(MAIN_PATH_LINE_STYLE);
            }else {
                stringBuilder.append(getStyle(t));
            }
            return stringBuilder.append("-> ");
        }

        private void printComponentProperties(StateDescriptor sd){
            if( sd instanceof AutomaticStateDescriptor asd){
                Map<String, Object> props = asd.getComponentProperties();
                for (Map.Entry<String,Object> prop: props.entrySet()){
                    stringBuilder.append("\\n**").append(prop.getKey()).append(":**").append(prop.getValue());
                }
            }
        }
    }
    private void findIncomingForAllStates(){

        for(StateDescriptor sd: stmFlowStore.getAllStates()) {
            if (sd.isInitialState())
                notOrphaned.put(sd.getId(),true);
            else
                notOrphaned.put(sd.getId(),false);
        }
        for(StateDescriptor sd: stmFlowStore.getAllStates()) {
            for(Transition t: sd.getTransitions().values()){
                if (!t.getNewStateId().equals(sd.getId())){
                    notOrphaned.put(t.getNewStateId(),true);
                }
            }
        }
    }

    private boolean isInMainPath(StateDescriptor sd){
        if (sd == null) return false;
        String mainPath = sd.getMetadata().get(MAIN_PATH);
        return Boolean.parseBoolean(mainPath);
    }

    private boolean isInMainPath(Transition t){
        String mainPath = t.getMetadata().get(MAIN_PATH);
        if (mainPath == null) return checkForStates(t);
        return Boolean.parseBoolean(mainPath);
    }

    private String getStyle(Transition t) {
        return transitionStyler.getStyle(t);
    }

    private boolean checkForStates(Transition t){
        State fromState = new State(t.getStateId(), t.getFlowId());
        State toState = new State(t.getNewStateId(), t.getNewFlowId());
        StateDescriptor fromSd = stmFlowStore.getStateInfo(fromState);
        StateDescriptor toSd = stmFlowStore.getStateInfo(toState);
        return isInMainPath(fromSd) && isInMainPath(toSd);
    }
}