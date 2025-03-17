package org.chenile.stm.model;

import org.chenile.stm.action.STMTransitionAction;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Transition extends EventInformation {
	private String tag;
	public String getTag(){
		return tag;
	}
	public void setTag(String tag){
		this.tag = tag;
	}
	public Transition(EventInformation eventInformation) {
		fromEventInformation(eventInformation);
	}
	
	private void fromEventInformation(EventInformation eventInformation) {
		this.tag = eventInformation.tag;
		this.eventIdTag = eventInformation.eventIdTag;
		this.componentNameTag = eventInformation.componentNameTag;
		this.eventId = eventInformation.eventId;
		this.newFlowId = eventInformation.newFlowId;
		this.newStateId = eventInformation.newStateId;
		this.metadata.putAll(eventInformation.metadata);
		this.transitionAction = eventInformation.transitionAction;

		if (eventInformation.getMetadata().get("acls") != null){
			String acls = eventInformation.getMetadata().get("acls");
			setAclString(acls);
		}
	}
	
	public Transition() {}
	
	public String getNewStateId() {
		return newStateId;
	}
	public void setNewStateId(String newStateId) {
		this.newStateId = newStateId;
	}
	
	public Transition transitionAction(STMTransitionAction<?> transitionAction) {
		this.transitionAction = transitionAction;
		return this;
	}
	
	public Transition newStateId(String newStateId) {
		this.newStateId = newStateId;
		return this;
	}
	public void setNewFlowId(String newFlowId) {
		this.newFlowId = newFlowId;
	}
	
	public Transition newFlowId(String newFlowId) {
		this.newFlowId = newFlowId;
		return this;
	}
	public String getNewFlowId() {
		return newFlowId;
	}
	public boolean isRetrievalTransition() {
		return retrievalTransition;
	}
	public void setRetrievalTransition(boolean retrievalTransition) {
		this.retrievalTransition = retrievalTransition;
	}

	public String getStateId() {
		return stateId;
	}
	public void setStateId(String stateId) {
		this.stateId = stateId;
	}
	
	public Transition id(String stateId) {
		this.stateId = stateId;
		return this;
	}
	
	private String[] acls;
	private boolean isInvokableOnlyFromStm = false;
	private String newStateId;
	private String newFlowId;
	private boolean retrievalTransition;
	private String stateId;
	private String flowId;
	private StateDescriptor parentState;


	public StateDescriptor getParentState() {
		return parentState;
	}

	public void setParentState(StateDescriptor parentState) {
		this.parentState = parentState;
	}

	public String getFlowId() {
		return flowId;
	}

	public void setFlowId(String flowId) {
		this.flowId = flowId;
	}

	@Override
	public String toString() {
		return "Transition{" +
				"tag='" + tag + '\'' +
				", eventId ='" + eventId + '\'' +
				", stateId='" + stateId + '\'' +
				", flowId='" + flowId + '\'' +
				", acls=" + Arrays.toString(acls) +
				", isInvokableOnlyFromStm=" + isInvokableOnlyFromStm +
				", newStateId='" + newStateId + '\'' +
				", newFlowId='" + newFlowId + '\'' +
				", retrievalTransition=" + retrievalTransition +
				", transitionAction= " + transitionAction +
				'}';
	}

	public String[] getAcls() {
		return acls;
	}
	
	public void setAclString(String acl){
		if (acl == null) return;
		setAcls(acl.split(","));
	}

	/**
	 * This is to support fluent API.
	 * @param acl the acls which are comma separated
	 * @return the Transition
	 */
	public Transition acl(String acl){
		if (acl == null) return this;
		setAcls(acl.split(","));
		return this;
	}
	
	
	public void setAcls(String[] acls) {
		this.acls = acls;
	}
	
	public boolean isInvokableOnlyFromStm() {
		return isInvokableOnlyFromStm;
	}
	public void setInvokableOnlyFromStm(boolean isInvokableOnlyFromStm) {
		this.isInvokableOnlyFromStm = isInvokableOnlyFromStm;
	}
	
	public Transition makeInvokableOnlyFromStm() {
		this.isInvokableOnlyFromStm = true;
		return this;
	}
	
	public String toXml(){
		return "<transition eventId='" + eventId + "' >\n" +
                "<newStateId>" + newStateId + "</newStateId>\n" +
                "<newFlowId>" + newFlowId + "</newFlowId>\n" +
                "<retrievalTransition>" + retrievalTransition + "</retrievalTransition>\n" +
                "<transitionAction>" + transitionAction + "</transitionAction>\n" +
                "</transition>\n";

	}

	public Transition transitionTo(String stateId, String... flowId) {
		String newFlowId = this.flowId;
		if (flowId != null && flowId.length > 0 && flowId[0] != null) {
			newFlowId = flowId[0];
		}
		this.newFlowId = newFlowId;
		this.newStateId = stateId;	
		return this;
	}
	
	public StateDescriptor state() {
		return parentState;
	}


	public String toJson() {
		return """
				{
					"eventId": "%s",
					"newFlowId": "%s",
					"newStateId" : "%s"
				}
				""".formatted(this.getEventId(),
				this.getNewFlowId(),this.getNewStateId());
	}

    public Map<String, Object> toMap() {
		Map<String,Object> map = new HashMap<>();
		map.put("eventId",this.getEventId());
		map.put("newFlowId",this.getNewFlowId());
		map.put("newStateId",this.getNewStateId());
		return map;
    }
}
