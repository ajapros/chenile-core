package org.chenile.stm.model;

import java.util.Collections;

import java.util.HashMap;
import java.util.Map;

import org.chenile.stm.action.STMTransitionAction;

/**
 * An abstraction to extract actions and their meta data and share them across transitions.
 * An event and a transition are two different things. 
 * Different transitions can potentially generate the same event.
 * This captures all the event information in one place. In this way, the same event information
 * does not have to be repeated for every transition.
 * @author Raja Shankar Kolluru
 *
 */
public class EventInformation {
	public static final String GENERIC_EVENT_ID = "__GENERIC_EVENT__";
	/**
	 * Specifies if this information belongs to tag definition or in from event-information
	 */
	public boolean tagDefinition = false;
	protected String tag;
	protected String eventId;
	public String eventIdTag;
	public String componentNameTag;
	public String newStateIdTag;
	public String newFlowIdTag;
	protected STMTransitionAction<?> transitionAction;
	public String newFlowId;
	public String newStateId;

	public String getTag(){
		if (tag == null) return eventId;
		return tag;
	}
	public void setTag(String tag){
		this.tag = tag;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
	public EventInformation id(String eventId) {
		this.eventId = eventId;
		return this;
	}

	public STMTransitionAction<?> getTransitionAction() {
		return transitionAction;
	}

	public void setTransitionAction(STMTransitionAction<?> transitionAction) {
		this.transitionAction = transitionAction;
	}
	
	public EventInformation transitionAction(STMTransitionAction<?> transitionAction) {
		this.transitionAction = transitionAction;
		return this;
	}

	public EventInformation addMetaData(String name, String value) {
		metadata.put(name, value);
		return this;
	}

	public Map<String, String> getMetadata() {
		return Collections.unmodifiableMap(metadata);
	}

	protected Map<String,String> metadata = new HashMap<String, String>();

	public EventInformation() {
		super();
	}

	/**
	 * Merges tag definition as defined in the add-transition-tag <br/>
	 * (which is contained in this event information) with the event
	 * information defined in the "event-information" tag. <br/>
	 * Tag information can be overridden by event-information
	 * @param other the event information to merge with this is from &lt;event-information&gt; tag
	 * @return the merged information as a copy
	 */
	public EventInformation mergeTagDefinitionWithEventInformation(EventInformation other) {
		EventInformation eventInformation = new EventInformation();
		eventInformation.tag = this.tag;
		eventInformation.eventIdTag = this.eventIdTag;
		eventInformation.componentNameTag = this.componentNameTag;
		eventInformation.newFlowIdTag = this.newFlowIdTag;
		eventInformation.newStateIdTag = this.newStateIdTag;
		eventInformation.eventId = this.eventId;
		eventInformation.newStateId = this.newStateId;
		eventInformation.newFlowId = this.newFlowId;
		eventInformation.transitionAction = this.transitionAction;
		if (other.getEventId() != null)
			eventInformation.eventId = other.eventId;
		if (other.newFlowId != null)
			eventInformation.newFlowId = other.newFlowId;
		if (other.newStateId != null)
			eventInformation.newStateId = other.newStateId;
		eventInformation.metadata = new HashMap<>(this.metadata);
		eventInformation.metadata.putAll(other.metadata);
		if (other.transitionAction != null)
			eventInformation.transitionAction = other.transitionAction;
		return eventInformation;
	}
}