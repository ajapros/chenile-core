package org.chenile.stm.model;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.chenile.stm.*;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.StateEntityRetrievalStrategy;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.STMFlowStoreImpl;

public class FlowDescriptor implements TransientActionsAwareDescriptor{
	private String id;
	private boolean isDefault = false;
	private Map<String, StateDescriptor> states =  new HashMap<String, StateDescriptor>();
	private String initialState;
	private  STMAction<?> entryAction;
	// by default don't execute entry or exit actions for auto states.
	private boolean skipEntryExitActionsForAutoStates = true;
	private STMSecurityStrategy stmSecurityStrategy;
	
	public boolean isSkipEntryExitActionsForAutoStates() {
		return skipEntryExitActionsForAutoStates;
	}

	public void setSkipEntryExitActionsForAutoStates(
			boolean skipEntryExitActionsForAutoStates) {
		this.skipEntryExitActionsForAutoStates = skipEntryExitActionsForAutoStates;
	}
	
	public FlowDescriptor skipEntryExitActionsForAutoStates(
			boolean skipEntryExitActionsForAutoStates) {
		this.skipEntryExitActionsForAutoStates = skipEntryExitActionsForAutoStates;
		return this;
	}

	public STMAction<?> getEntryAction() {
		return entryAction;
	}

	public void setEntryAction(STMAction<?> entryAction) {
		this.entryAction = entryAction;
	}
	
	public FlowDescriptor entryAction(STMAction<?> entryAction) {
		this.entryAction = entryAction;
		return this;
	}

	public STMAction<?> getExitAction() {
		return exitAction;
	}

	public void setExitAction(STMAction<?> exitAction) {
		this.exitAction = exitAction;
	}
	
	public FlowDescriptor exitAction(STMAction<?> exitAction) {
		this.exitAction = exitAction;
		return this;
	}


	private  STMAction<?> exitAction;

	private StateEntityRetrievalStrategy<? extends StateEntity> retrievalStrategy;
	private STMFlowStoreImpl flowStore;

	public void setId(String id) {
		this.id = id;
	}
	
	public FlowDescriptor id(String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	public FlowDescriptor makeDefault() {
		this.isDefault = true;
		if (this.flowStore != null)
			flowStore.setDefaultFlow(this);
		return this;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setStates(Map<String, StateDescriptor> states) {
		this.states = states;
	}

	public Map<String, StateDescriptor> getStates() {
		Map<String,StateDescriptor> map = new HashMap<>(states);
		STMFlowStoreImpl flowStore = getFlowStore();
		if (flowStore == null) return map;
		final EnablementStrategy enablementStrategy = flowStore.getEnablementStrategy();

		map = states.entrySet()
				.stream().
				filter((sd) -> {
					if (enablementStrategy == null) return true;
					return enablementStrategy.isStateEnabled(sd.getValue());
				}).peek((sd)-> {
					if (enablementStrategy != null){
						enablementStrategy.addMetadataToState(sd.getValue());
					}
				}) .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		;

		Collection<StateDescriptor> list = obtainDynamicStates(enablementStrategy);
		if (list == null) return map;
		for (StateDescriptor sd: list){
			map.put(sd.getId(),sd);
		}
		return map;
	}

	private Collection<StateDescriptor> obtainDynamicStates(EnablementStrategy enablementStrategy){
		if (enablementStrategy == null) return null;
		return enablementStrategy.addDynamicStates(this);
	}

	
	public void addsd(StateDescriptor sd) throws STMException {
		StateDescriptor currSd = states.get(sd.getId());
		sd.setFlowId(this.id);
		sd.setFlow(this);
		if (currSd != null){
			currSd.merge(sd);
			sd = currSd;
		}else{
			states.put(sd.getId(), sd);
			sd.setFlowId(this.id);
		}
		if (sd.isInitialState())
			initialState = sd.getId();
		sd.validate();
		
	}
	
	public FlowDescriptor addState(StateDescriptor sd) throws STMException{
		addsd(sd);
		return this;
	}

	public void setInitialState(String initialState) {
		this.initialState = initialState;
	}
	
	public FlowDescriptor initialState(String initialState) {
		this.initialState = initialState;
		return this;
	}

	public String getInitialState() {
		return initialState;
	}
	
	public void validate(STMFlowStore flowStore) throws Exception {
		if (getId() == null) {
			System.out.println("Warning: Flow Id is null. By default the value of the ID is " + State.DEFAULT_FLOW_ID);
		}
		for ( StateDescriptor sd : states.values()){
			sd.validate();
		}
	}

	@Override
	public String toString() {
		return "FlowDescriptor [id=" + id + ", isDefault=" + isDefault + ", states=" + states + ", initialState="
				+ initialState + ", entryAction=" + entryAction + ", skipEntryExitActionsForAutoStates="
				+ skipEntryExitActionsForAutoStates + ", exitAction=" + exitAction + ", retrievalStrategy="
				+ retrievalStrategy + "]";
	}
	
	public String toXml(){
		StringBuilder sb = new StringBuilder();
		sb.append("<flow>");
		sb.append("<id>").append(id).append("</id>\n");
		sb.append("<isDefault>").append(isDefault).append("</isDefault>\n");
		sb.append("<initialState>").append(initialState).append("</initialState>\n");
		sb.append("<entryAction>").append(entryAction).append("</entryAction>\n");
		sb.append("<exitAction>").append(exitAction).append("</exitAction>\n");
		sb.append("<retrievalStrategy>").append(retrievalStrategy).append("</retrievalStrategy>\n");
		sb.append("<skipEntryExitActionsForAutoStates>").append(skipEntryExitActionsForAutoStates).append("</skipEntryExitActionsForAutoStates>\n");
		sb.append("<states>\n");
		for(StateDescriptor sd: getStates().values()){
			sb.append(sd.toXml());
		}
		sb.append("</states>\n");
		sb.append("</flow>");
		return sb.toString();
	}
	
	public void setRetrievalStrategy(StateEntityRetrievalStrategy<? extends StateEntity> retrievalStrategy){
		this.retrievalStrategy = retrievalStrategy;
	}
	
	public FlowDescriptor retrievalStrategy(StateEntityRetrievalStrategy<? extends StateEntity> retrievalStrategy){
		this.retrievalStrategy = retrievalStrategy;
		return this;
	}

	public StateEntityRetrievalStrategy<? extends StateEntity> getRetrievalStrategy() {
		return retrievalStrategy;
	}
	
	public List<Map<String,Object>> getStatesInfo(){
		List<Map<String,Object>> statesInfo = new ArrayList<Map<String,Object>>();
		for(Entry<String, StateDescriptor> stateId : states.entrySet()){
			
			if(!stateId.getValue().isManualState()){
				continue;
			}
			
			Map<String,Object> stateInfo = new HashMap<String, Object>();
			stateInfo.put("state_id", stateId.getKey());
			List<Map<String,Object>> transitionsMeta = new ArrayList<Map<String,Object>>();
			for(Transition transition : stateId.getValue().getTransitions().values()){
				Map<String,Object> transitionDetails = new HashMap<String, Object>();
				transitionDetails.put("meta-data", transition.getMetadata());
				transitionDetails.put("eventId", transition.getEventId());
				transitionDetails.put("acls", transition.getAcls());
				transitionsMeta.add(transitionDetails);
			}
			stateInfo.put("transitions_allowed",transitionsMeta);
			stateInfo.put("meta-data",stateId.getValue().getMetadata());
			statesInfo.add(stateInfo);
		}
		return statesInfo;
	}

	public STMSecurityStrategy getStmSecurityStrategy() {
		return stmSecurityStrategy;
	}

	public void setStmSecurityStrategy(STMSecurityStrategy stmSecurityStrategy) {
		this.stmSecurityStrategy = stmSecurityStrategy;
	}
	
	public FlowDescriptor securityStrategy(STMSecurityStrategy stmSecurityStrategy) {
		this.stmSecurityStrategy = stmSecurityStrategy;
		return this;
	}
	
	public ManualStateDescriptor manualState(String id)  throws STMException{
		return manualState(id,false);
	}
	
	public ManualStateDescriptor manualState(String id, boolean initialState) throws STMException {
		ManualStateDescriptor msd = new ManualStateDescriptor();
		msd.setId(id);
		msd.setFlowId(this.id);
		msd.setFlow(this);
		msd.setManualState(true);
		if (initialState) {
			this.initialState = id;
		}
		this.addsd(msd);
		return msd;
	}
	
	public AutomaticStateDescriptor autoState(String id)  throws STMException {
		return autoState(id,false);
	}
	public AutomaticStateDescriptor autoState(String id, boolean initialState) throws STMException{
		AutomaticStateDescriptor asd = new AutomaticStateDescriptor();
		asd.setId(id);
		asd.setFlowId(this.id);
		asd.setFlow(this);
		if (initialState) {
			this.initialState = id;
		}
		this.addsd(asd);
		return asd;
	}

	public STMFlowStoreImpl getFlowStore() {
		return flowStore;
	}

	public void setFlowStore(STMFlowStoreImpl flowStore) {
		this.flowStore = flowStore;
	}

	public void setInitialState(StateDescriptor stateDescriptor) {
		this.initialState = stateDescriptor.getId();		
	}

	public static class EventI {
		public String eventId;
		public boolean isManualTransition;
		public EventI(String eventId,boolean isManualTransition){
			this.eventId = eventId;
			this.isManualTransition = isManualTransition;
		}
		public String toString(){
			return toJson();
		}
		public String toJson(){
			return """
					{
					"eventId": "%s",
					"manualTransition": %s
					}
					""".formatted(this.eventId,this.isManualTransition);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof EventI eventI)) return false;
            return Objects.equals(eventId, eventI.eventId);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(eventId);
		}
	}
	public String toJson() {
		StringBuilder stringBuilder = new StringBuilder("{\n");
		stringBuilder.append("\"id\":\"").append(this.id).append("\",\n");
		stringBuilder.append("\"default\":").append(this.isDefault).append(",\n");
		stringBuilder.append("\"states\": [\n");
		boolean first = true;
		Set<EventI> events = new HashSet<>();
		for (StateDescriptor sd: getStates().values()){
			if (!first) stringBuilder.append(",");
			else first = false;
			stringBuilder.append(sd.toJson());
			events.addAll(sd.getTransitions().values().stream().map(
					t -> new EventI(t.getEventId(),sd.isManualState())).toList());
		}
		stringBuilder.append("],\n");
		stringBuilder.append("\"events\": [\n");
		first = true;
		for (EventI t: events){
			if (!first) stringBuilder.append(",");
			else first = false;
			stringBuilder.append(t.toJson());
		}
		stringBuilder.append("]}\n");

		return stringBuilder.toString();
	}

    public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap<>();
		map.put("id",this.id);
		map.put("default",this.isDefault);
		List<Map<String,Object>> list = new ArrayList<>();
		Set<EventI> events = new HashSet<>();
		for (StateDescriptor sd: getStates().values()){
			list.add(sd.toMap());
			events.addAll(sd.getTransitions().values().stream().map(
				t -> new EventI(t.getEventId(),sd.isManualState()
				)).toList());
		}
		map.put("events",events);
		map.put("states",list);
		return map;
    }
}
