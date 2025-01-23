package org.chenile.stm.impl;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.digester.Rule;
import org.chenile.stm.STMSecurityStrategy;
import org.chenile.stm.StateEntity;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMAutomaticStateComputation;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.action.StateEntityRetrievalStrategy;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.model.*;
import org.xml.sax.Attributes;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;

/**
 * 
 * Provides an xml implementation of the FlowStoreReader. Supports flexible
 * custom xml tags for specific action state components. Custom properties are
 * made available to actions registered. These two features provide a powerful
 * extension making this parser support an XML based DSL.
 * 
 * @author Raja Shankar Kolluru
 */
public class XmlFlowReader extends FlowReaderBase {

	private static final String STATES_ADD_STATE_TAG = "states/add-state-tag";
	private static final String STATES_ADD_TRANSITION_TAG = "states/add-transition-tag";
	public static final String ENTRY_ACTION = "/entry-action";
	public static final String EXIT_ACTION = "/exit-action";
	String EVENT_ID_TAG = "eventIdTag";
	String COMPONENT_NAME_TAG = "componentNameTag";
	private static final String COMPONENT_NAME = "componentName";
	private static final String EVENT_ID = "eventId";
	private static final String STATES_FLOW_TAG = "states/flow";
	private static final String STATES_FLOW = "states/flow/";
	private static final String COMPONENT_PROPERTIES = "/component-properties/?";
	private Digester digester;
	public static final String META_PREFIX = "meta-";
	public static final String NEW_FLOW_ID = "newFlowId";
	public static final String NEW_STATE_ID = "newStateId";
	public static final String NEW_FLOW_ID_TAG = "newFlowIdTag";
	public static final String NEW_STATE_ID_TAG = "newStateIdTag";
	/**
	 * 
	 * @param stmFlowStoreImpl the store
	 */
	public XmlFlowReader(STMFlowStoreImpl stmFlowStoreImpl) {
		super(stmFlowStoreImpl);
		setUpDigester(); 
		setUpDigesterRules();
	}
	
	private void setUpDigesterRules() {
		for (StateTagDescriptor std: stmFlowStoreImpl.actionTagsMap.values()){
			setRulesForStateTag(std.getTag());
		}
	}

	/**
	 * 
	 * @param filename the XML file name
	 * @throws Exception if it cannot read the file
	 */
	public void setFilename(String filename) throws Exception {
		// discover all the streams that correspond to this filename.
		Enumeration<URL> urls = getClass().getClassLoader().getResources(
				filename);
		while (urls.hasMoreElements()) {
			URL u = urls.nextElement();
			parse(u.openStream()); 
		}
	}

	/**
	 * 
	 * @param inputStream the input stream to parse
	 * @throws Exception if any error occurs in parsing the stream
	 */
	public void parse(InputStream inputStream) throws Exception {
		digester.push(stmFlowStoreImpl);
		digester.parse(inputStream);
		digester.clear();
	}
	
	public class AddStateRule extends Rule {
		@SuppressWarnings("unchecked")
		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {

			StateTagDescriptor std = new StateTagDescriptor();
			std.setId(attributes.getValue("id"));
			std.setComponentName(attributes.getValue("componentName"));
			String x = attributes.getValue("manualState");
			boolean manualState = Boolean.parseBoolean(x);
			std.setManualState(manualState);
			x = attributes.getValue("initialState");
			if (x != null)
				std.setInitialState(Boolean.parseBoolean(x));
			String dc = attributes.getValue("descriptorClass");
			if (dc == null){
				dc = manualState?"org.chenile.stm.model.ManualStateDescriptor":
						"org.chenile.stm.model.AutomaticStateDescriptor";
			}
			std.setDescriptorClass((Class<? extends StateDescriptor>)Class.forName(dc));
			std.setTag(attributes.getValue("tag"));
			addOtherAttributes(std,attributes);
			XmlFlowReader.this.stmFlowStoreImpl.addStateTag(std);
			setRulesForStateTag(std.getTag());
		}

		private void addOtherAttributes(StateTagDescriptor std,Attributes attributes){
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getLocalName(i);
				if ("".equals(name)) {
					name = attributes.getQName(i);
				}
				String value = attributes.getValue(i);
				if (name.equals(COMPONENT_NAME) || name.equals("initialState") ||
					name.equals("id")) continue;
				std.props.put(name, value);
			}
		}
	}

	public void setUpDigester() {
		digester = new Digester();
		digester.setRules(new ExtendedBaseRules());
		// to make sure that we match wild cards such as ? and *


		digester.addRule(STATES_ADD_STATE_TAG, new AddStateRule());
		digester.addRule(STATES_ADD_TRANSITION_TAG, new AddEventInformationRule());

		digester.addCallMethod("states/include",
				"include", 1, new Class<?>[] { String.class });
		digester.addCallParam("states/include", 0, "file");

		digester.addCallMethod("states/scripting-strategy",
				"setScriptingStrategy", 1, new Class<?>[] { String.class });
		digester.addCallParam("states/scripting-strategy", 0, COMPONENT_NAME);

		digester.addCallMethod("states/enablement-strategy",
				"setEnablementStrategy", 1, new Class<?>[] { String.class });
		digester.addCallParam("states/enablement-strategy", 0, COMPONENT_NAME);

		digester.addRule("states" + ENTRY_ACTION,
				new AddTransitionActionToTransientActionAwareDescriptorRule());
		digester.addRule("states" + EXIT_ACTION,
				new AddTransitionActionToTransientActionAwareDescriptorRule());
		digester.addCallMethod("states/default-transition-action", "setDefaultTransitionAction",1);
		digester.addCallParam("states/default-transition-action", 0, "componentName");
		digester.addRule("states/event-information", new AddEventInformationRule());
		digester.addRule(STATES_FLOW_TAG,
				new CreateOrUseExistingObjectRule<FlowDescriptor>(
						FlowDescriptor.class, "getFlow"));
		digester.addSetProperties(STATES_FLOW_TAG);

		digester.addRule("states/flow/entry-action",
				new AddTransitionActionToTransientActionAwareDescriptorRule());
		digester.addRule("states/flow/exit-action",
				new AddTransitionActionToTransientActionAwareDescriptorRule());
		digester.addRule("states/flow/retrieval-strategy",
				new AddRetrievalStrategy());
		digester.addRule("states/flow/security-strategy",
				new AddSecurityStrategy());
		
		digester.addSetNext(STATES_FLOW_TAG, "addFlow");
	}

	private void setRulesForStateTag(String tag) {

		digester.addRule(STATES_FLOW + tag,
				new StateDescriptorAttributesRule());

		digester.addRule(STATES_FLOW + tag + ENTRY_ACTION,
				new AddTransitionActionToTransientActionAwareDescriptorRule());
		digester.addRule(STATES_FLOW + tag + EXIT_ACTION,
				new AddTransitionActionToTransientActionAwareDescriptorRule());

		digester.addCallMethod(
				STATES_FLOW + tag + COMPONENT_PROPERTIES,
				"addXmlComponentProperty", 2, new Class[] { String.class,
						String.class });
		digester.addCallParamPath(STATES_FLOW + tag
				+ COMPONENT_PROPERTIES, 0);
		digester.addCallParam(STATES_FLOW + tag + COMPONENT_PROPERTIES,
				1);

		digester.addRule(STATES_FLOW + tag + "/*", new AddTransitionRule());

		digester.addSetNext(STATES_FLOW + tag, "addsd");

	}

	public class AddEventInformationRule extends Rule{
		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {
			EventInformation eventInformation = new EventInformation();
			String tag = attributes.getValue("tag");
			if (tag != null)
				eventInformation.tagDefinition = true;
			eventInformation.eventIdTag = attributes.getValue(EVENT_ID_TAG);
			if (eventInformation.eventIdTag == null) eventInformation.eventIdTag = EVENT_ID;
			eventInformation.componentNameTag = attributes.getValue(COMPONENT_NAME_TAG);
			if (eventInformation.componentNameTag == null)eventInformation.componentNameTag = COMPONENT_NAME;
			eventInformation.newFlowIdTag = attributes.getValue(NEW_FLOW_ID_TAG);
			if (eventInformation.newFlowIdTag == null)eventInformation.newFlowIdTag = NEW_FLOW_ID;
			eventInformation.newStateIdTag = attributes.getValue(NEW_STATE_ID_TAG);
			if (eventInformation.newStateIdTag == null)eventInformation.newStateIdTag = NEW_STATE_ID;
			String eventId = attributes.getValue(eventInformation.eventIdTag);
			if(eventId == null){
				eventId = transformTag(tag);
			}
			eventInformation.setTag(tag);
			if (eventId == null){
				throw new STMException("Invalid event-information or add-transition-tag: " +
						"eventId or tag name is mandatory",STMException.INVALID_CONFIGURATION);
			}
			eventInformation.setEventId(eventId);
			eventInformation.newFlowId = attributes.getValue(eventInformation.newFlowIdTag);
			eventInformation.newStateId = attributes.getValue(eventInformation.newStateIdTag);
			processTransitionAction(eventInformation,attributes);
			processMetaAttributes(eventInformation, attributes);
			stmFlowStoreImpl.addEventInformation(eventInformation);
		}
		
		protected void processTransitionAction(EventInformation eventInformation, Attributes attributes) throws Exception {
			if (attributes.getValue(eventInformation.componentNameTag) == null) return;
			eventInformation.setTransitionAction((STMTransitionAction<?>) stmFlowStoreImpl.
					                 makeTransitionAction(attributes.getValue(eventInformation.componentNameTag),true));
		}

		protected void processMetaAttributes(EventInformation eventInformation,
				Attributes attributes) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getLocalName(i);
				if ("".equals(name)) {
					name = attributes.getQName(i);
				}
				String value = attributes.getValue(i);

				if (name.startsWith(META_PREFIX)) {
					String n = name.substring(META_PREFIX.length());
					eventInformation.addMetaData(n, value);
				}
			}
		}
	}

	public class AddTransitionRule extends AddEventInformationRule{
		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {
			EventInformation transitionTagDefinition = stmFlowStoreImpl.getEventInformation(xmlElementName);
			if (transitionTagDefinition == null) {
				// no need to process this tag since it is not a registered transition tag.
				return;
			}
			// Event ID can either be set when the tag is used. It will be set as an attribute
			// of the tag. If not set, it can be picked up from the tag definition.
			String eventId = attributes.getValue(transitionTagDefinition.eventIdTag);
			if(eventId == null) {
				eventId = transitionTagDefinition.getEventId();
			}
			EventInformation eventInfo = stmFlowStoreImpl.getEventInformation(eventId);
			EventInformation mergedEventInfo = transitionTagDefinition;
			if (eventInfo != null) {
				mergedEventInfo = transitionTagDefinition.mergeTagDefinitionWithEventInformation(eventInfo);
			}
			StateDescriptor sd = (StateDescriptor) digester.peek();
			Transition transition = new Transition(mergedEventInfo);
			transition.setEventId(eventId);
			transition.setFlowId(sd.getFlowId());
			transition.setStateId(sd.getId());
			if (attributes.getValue(mergedEventInfo.newFlowIdTag) != null)
				transition.setNewFlowId(attributes.getValue(mergedEventInfo.newFlowIdTag));
			if (attributes.getValue(mergedEventInfo.newStateIdTag) != null)
				transition.setNewStateId(attributes.getValue(mergedEventInfo.newStateIdTag));
			String invokableOnlyFromStm = attributes.getValue("invokableOnlyFromStm");
			if (invokableOnlyFromStm != null)
				transition.setInvokableOnlyFromStm(Boolean.parseBoolean(invokableOnlyFromStm));
			String rt = attributes.getValue("retrievalTransition");
			if (rt != null)
				transition.setRetrievalTransition(Boolean.parseBoolean(rt));
			processTransitionAction(transition,attributes);
			processMetaAttributes(transition, attributes);
			sd.addTransition(transition);
		}
	}

	/**
	 * Custom digester rule to process Action State Descriptor.
	 * 
	 * @author Raja Shankar Kolluru
	 *
	 */

	public class StateDescriptorAttributesRule extends Rule {

		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {
			FlowDescriptor fd = (FlowDescriptor) digester.peek();
			StateDescriptor sd;
			StateTagDescriptor tagDescriptor = stmFlowStoreImpl.actionTagsMap.get(xmlElementName);
			boolean isManualState = tagDescriptor.isManualState();
			sd = tagDescriptor.getDescriptorClass().getDeclaredConstructor().newInstance();
			sd.setManualState(isManualState);
			digester.push(sd);

			// id and initialState need to be injected into sd as well.
			String id = tagDescriptor.getId();
			if (attributes.getValue("id") != null){
				id = attributes.getValue("id");
			};
			sd.setId(id);
			sd.setFlowId(fd.getId());

			sd.setInitialState(tagDescriptor.isInitialState());
			String initialState = attributes.getValue("initialState");
			if (initialState != null)
				sd.setInitialState(Boolean.parseBoolean(initialState));
			if (sd instanceof AutomaticStateDescriptor)
				setActionDescriptorProperties((AutomaticStateDescriptor) sd,
						attributes, tagDescriptor);
			processMetaAttributes(tagDescriptor,sd, attributes);
		}

		private void processMetaAttributes(StateTagDescriptor tagDescriptor,
					    StateDescriptor sd,
						Attributes attributes) {
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getLocalName(i);
				if ("".equals(name)) {
					name = attributes.getQName(i);
				}
				String value = attributes.getValue(i);

				if (name.startsWith(META_PREFIX)) {
					String n = name.substring(META_PREFIX.length());
					sd.addMetaData(n, value);
				}
			}
			for (Map.Entry<String,String> entry: tagDescriptor.props.entrySet()){
				String name = entry.getKey();
				String value = entry.getValue();
				if (name.startsWith(META_PREFIX)) {
					String n = name.substring(META_PREFIX.length());
					sd.addMetaData(n, value);
				}
			}

		}

		private void setActionDescriptorProperties(
				AutomaticStateDescriptor asd, Attributes attributes,
				StateTagDescriptor tagDescriptor) throws STMException {

			// process componentName either from the attribute or from the
			// actionTagsMap
			String componentName = attributes.getValue(COMPONENT_NAME);
			if (componentName == null) {
				componentName = tagDescriptor.getComponentName();
			}
			String enableInlineScripts = attributes
					.getValue("enableInlineScripts");
			boolean enable = true;
			if (enableInlineScripts != null)
				enable = Boolean.getBoolean(enableInlineScripts);

			asd.setComponentName(componentName);
			asd.setComponent((STMAutomaticStateComputation<?>) stmFlowStoreImpl
					.makeAutomaticStateComputation(asd.getComponentName(), enable));

			// add the rest of the attributes to the componentProperties
			for (int i = 0; i < attributes.getLength(); i++) {
				String name = attributes.getLocalName(i);
				if ("".equals(name)) {
					name = attributes.getQName(i);
				}
				String value = attributes.getValue(i);

				if (COMPONENT_NAME.equals(name) || "id".equals(name)
						|| "initialState".equals(name))
					continue; // processed these already.
				else {
					asd.addXmlComponentProperty(name, value);
				}
			}
			for (Map.Entry<String,String> entry: tagDescriptor.props.entrySet()){
				String name = entry.getKey();
				String value = entry.getValue();
				if (!name.startsWith(META_PREFIX)) {
					asd.addXmlComponentProperty(name, value);
				}
			}
		} 

		/**
		 * 
		 */
		public void end() throws Exception {
			digester.pop(); // pop the state descriptor from the stack.			
		}

	}

	public class AddTransitionActionToTransientActionAwareDescriptorRule extends Rule {

		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {

			TransientActionsAwareDescriptor taad = (TransientActionsAwareDescriptor) digester
					.peek();
			String componentName = attributes.getValue(COMPONENT_NAME);
			String enableInlineScripts = attributes
					.getValue("enableInlineScripts");
			boolean enable = true;
			if (enableInlineScripts != null)
				enable = Boolean.getBoolean(enableInlineScripts);
			STMAction<?> transitionAction = (STMAction<?>) stmFlowStoreImpl
					.makeAction(componentName, enable);
			if ( "entry-action".equals(xmlElementName))
				taad.setEntryAction(transitionAction);
			else if ( ("exit-action").equals(xmlElementName))
				taad.setExitAction(transitionAction);
		}
	}

	
	public class AddRetrievalStrategy extends Rule {

		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {

			FlowDescriptor fd = (FlowDescriptor) digester.peek();
			String componentName = attributes.getValue(COMPONENT_NAME);

			@SuppressWarnings("unchecked")
			StateEntityRetrievalStrategy<StateEntity> retrievalStrategy = (StateEntityRetrievalStrategy<StateEntity>) stmFlowStoreImpl
					.makeRetrievalStrategy(componentName);
			fd.setRetrievalStrategy(retrievalStrategy);
		}
	}
	
	/**
	 * 
	 * @author Raja
	 *
	 */
	public class AddSecurityStrategy extends Rule {

		@Override
		public void begin(String namespace, String xmlElementName,
				Attributes attributes) throws Exception {

			FlowDescriptor fd = (FlowDescriptor) digester.peek();
			String componentName = attributes.getValue(COMPONENT_NAME);

			STMSecurityStrategy securityStrategy = (STMSecurityStrategy) stmFlowStoreImpl
					.makeSecurityStrategy(componentName);
			fd.setStmSecurityStrategy(securityStrategy);
		}
	}

	public static String transformTag(String tagName){
		if (tagName == null)return null;
		int index = tagName.indexOf('-');
		while (index != -1){
			if (index == (tagName.length()-1)){
				tagName = tagName.substring(0,index);
			}else {
				tagName = tagName.substring(0, index) + tagName.substring(index + 1, index + 2).toUpperCase()
						+ tagName.substring(index + 2);
			}
			index = tagName.indexOf('-');
		}
		return tagName;
	}

	public static void main(String[] args){
		String tag = "event-id";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
		tag = "abc";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
		tag = "abc-";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
		tag = "abc-def-ghi";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
		tag = "-abc-def-ghi";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
		tag = "-abc-def-ghi-";
		System.out.println("tag = " + tag + " transformed event id is " + transformTag(tag));
	}

}
