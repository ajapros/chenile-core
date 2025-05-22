package org.chenile.stm.model;

import java.util.*;
import java.util.Map.Entry;

import org.chenile.stm.EnablementStrategy;
import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.STMFlowStoreImpl;


public class StateDescriptor implements TransientActionsAwareDescriptor{

	protected String id;
	protected boolean initialState;
	protected STMAction<?> entryAction;
	protected Map<String,String> metadata = new HashMap<String, String>();
	
	public boolean isFinalState() {
		return getTransitions().isEmpty();
	}
	
	public StateDescriptor addMetaData(String name, String value){
		metadata.put(name, value);
		return this;
	}

	public Map<String, String> getMetadata(){
		return Collections.unmodifiableMap(metadata);
	}

	public STMAction<?> getEntryAction() {
		return entryAction;
	}

	public void setEntryAction(STMAction<?> entryAction) {
		this.entryAction = entryAction; 
	}
	
	public StateDescriptor entryAction(STMAction<?> entryAction) {
		this.entryAction = entryAction; 
		return this;
	}

	public STMAction<?> getExitAction() {
		return exitAction;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public void setExitAction(STMAction<?> exitAction) {
		this.exitAction = exitAction;
	}
	
	public StateDescriptor exitAction(STMAction<?> exitAction) {
		this.exitAction = exitAction;
		return this;
	}
	

	protected STMAction<?> exitAction;
	/**
	 * Is this state manual? (or a view state?)
	 */
	protected boolean manualState = false;
	public boolean isManualState() {
		return manualState;
	}
	public void setManualState(boolean manualState) {
		this.manualState = manualState;
	}

	private Map<String,Transition> transitions = new HashMap<String, Transition>();
	private String flowId;
	private FlowDescriptor flow;

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	public StateDescriptor() {
		super();
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public StateDescriptor id(String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setInitialState(boolean initialState) {
		this.initialState = initialState;
	}

	public boolean isInitialState() {
		return initialState;
	}
	
	public StateDescriptor makeInitialState() {
		this.initialState = true;
		if (flow != null)
			flow.setInitialState(this);
		return this;
	}

	public void setTransitions(Map<String,Transition> transitions) {
		this.transitions = transitions;
	}

	// @XmlTransient
	public Map<String,Transition> getTransitions() {
		EnablementStrategy enablementStrategy = null;
		STMFlowStoreImpl flowStore = getFlow().getFlowStore();
		if (flowStore != null)
			enablementStrategy = flowStore.getEnablementStrategy();
		if (enablementStrategy == null) return transitions;
		Map<String,Transition> map = new HashMap<>();
		for (Transition t: transitions.values()){
			if(!enablementStrategy.isEventEnabled(this,t))
				continue;
			// find the new state
			StateDescriptor sd = getFlow().getStates().get(t.getNewStateId());
			if (sd == null || !enablementStrategy.isStateEnabled(sd))
				continue;
			enablementStrategy.addMetadataToTransition(t,this);
			map.put(t.getEventId(),t);
		}
		addDynamicTransitions(enablementStrategy,map);
		return map;
	}

	private void addDynamicTransitions(EnablementStrategy  enablementStrategy,Map<String,Transition> map){
		Map<String, Transition> t = enablementStrategy.addDynamicTransitions(this);
		map.putAll(t);
	}

	public StateDescriptor addTransition(Transition transition) throws STMException {
		// ensure that the transitions have explicit newStateId etc.
		if(transition.getNewFlowId() == null) transition.setNewFlowId(this.flowId);
		if (transition.getNewStateId() == null) transition.setNewStateId(this.id);
		transition.setParentState(this);
		// for retrieval transitions the new state cannot be the same as the old state
		if (transition.isRetrievalTransition() && transition.getNewStateId().equals(this.id)){
			throw new STMException("Retrieval transitions must have a \"to state\" that is different from the \"from state\"",
					STMException.INVALID_TRANSITION);
		}
		transitions.put(transition.getEventId(), transition);
		return this;
	}
	
	public Transition on(String eventId) throws STMException {
		Transition transition = new Transition();
		transition.setEventId(eventId);
		transition.setParentState(this);
		transition.setFlowId(this.flowId);
		transition.setStateId(this.id);
		addTransition(transition);
		return transition;
	}

	@Override
	public String toString() {
		return "StateDescriptor [id=" + id + ", initialState=" + initialState
				+ ", transitions=" + transitions + "]";
	}
	
	public boolean checkIfonlyRetrievalTransitions() {
		for (Transition t: transitions.values()){
			if(!t.isRetrievalTransition()) return false; 
		}
		return true;
	}
	
	public void validate() throws STMException{
		for (Transition t: transitions.values()){
			if(t.isRetrievalTransition() && transitions.size() != 1) {
				throw new STMException("Invalid state definition for id " + id,STMException.INVALID_STATE); 
			}
		}
	}
	
	public void validate(STMFlowStore flowStore) throws STMException{
		validate();
		for (Transition t: transitions.values()){
			// make sure that each transition points to a new state that is defined in the state machine
			State newState = new State(t.getNewStateId(),t.getNewFlowId());
			StateDescriptor s = flowStore.getStateInfo(newState);
			if (s == null)
				throw new STMException("New State " + newState + " pointed by transition " + t.getEventId() + 
						" is not defined in the flow store.", STMException.INVALID_STATE,new State(getId(),getFlowId())); 
		}
	}
	
	public Set<String> getAllTransitionsIds(){
		return getTransitions().keySet();
	}
	
	public void merge(StateDescriptor sd) {
		if (exitAction != null && sd.getExitAction() != null){
			System.err.println("Warning: Exit action of " + sd.getId() + " seems to be duplicated!!");
		}
		if (sd.getTransitions() != null)
			// this.transitions.putAll(sd.getTransitions()); // merge the transitions
			this.transitions.putAll(sd.transitions); // merge the transitions. Use the transitions instead
		    // of the getTransitions() since the end state might not have been read yet and the getTransitions()
		    // will discard the transition if end state is not defined.
	}
	
	public String toXml(){
		StringBuilder sb = new StringBuilder();
		sb.append("<state>\n");
		sb.append("<id>" + id + "</id>\n");
		sb.append("<metadata>\n");
		for(Entry<String, String> entry:metadata.entrySet()){
			sb.append("<"+entry.getKey()+" value="+entry.getValue()+">\n");
		}
		sb.append("</metadata>\n");
		sb.append("<transitions>");
		for (Transition t: transitions.values()){
			sb.append(t.toXml());
		}
		sb.append("</transitions>");
		sb.append("</state>\n");
		return sb.toString();
	}

	public FlowDescriptor flow() {
		return this.flow;		
	}

	public FlowDescriptor getFlow() {
		return flow;
	}

	public void setFlow(FlowDescriptor flow) {
		this.flow = flow;
	}

	public String toJson() {
        return """
                {
                    "id": "%s",
                    "initialState": %s,
                    "transitions": [
                    %s
                    ]
                }
                """.formatted(this.id, this.initialState,transitionsAsJson());
	}

	private String transitionsAsJson() {
		StringBuilder stringBuilder = new StringBuilder();
		boolean first = true;
		for (Transition t: getTransitions().values()){
			if (!first) stringBuilder.append(",");
			else first = false;
			stringBuilder.append(t.toJson());
		}
		return stringBuilder.toString();
	}

    public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap<>();
		map.put("id",this.id);
		map.put("initialState",this.initialState);
		List<Map<String,Object>> list = new ArrayList<>();
		for (Transition t: getTransitions().values()){
			list.add(t.toMap());
		}
		map.put("transitions",list);
		return map;
    }
}