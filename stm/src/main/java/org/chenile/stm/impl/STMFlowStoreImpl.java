package org.chenile.stm.impl;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.chenile.stm.*;
import org.chenile.stm.action.*;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.model.*;

/**
 * 
 * <p>
 * A base implementation of STMFlowStore FlowStoreReaders parse various
 * representations of the flow (such as in an xml or in a db) and call the
 * addflow() method in this class. Look at {@link XmlFlowReader} for an example.
 * <p>
 * All of the functionality of FlowConfigurator interface is handled by this
 * class. The only part that is missing is the parsing part which is expected to
 * be provided by the parsing sub class. Besides flow configurations, other
 * things such as {@link ScriptingStrategy}, entry actions, exit actions and
 * {@link ComponentPropertiesHelper} are handled by this base class.
 * <p>
 * 
 * @author Raja Shankar Kolluru
 */
public class STMFlowStoreImpl implements STMFlowStore, TransientActionsAwareDescriptor {

	private final static Logger LOGGER = Logger.getLogger(STMFlowStoreImpl.class.getName());

	public static final String PATH_TO_DEFAULT_STM_CONFIGURATION = "webcommercebean/stm-conf.xml";

	protected Map<String, StateTagDescriptor> actionTagsMap = new HashMap<>();
	
	protected Map<String, FlowDescriptor> flows = new HashMap<>();
	
	protected Map<String, EventInformation> eventInfos = new HashMap<String, EventInformation>();
	// transition action across flows. This avoids having to repeat the same for
	// all flows.
	// however individual flows have the ability to override this at the flow
	// level.
	protected STMActionsInfoProvider stmActionsInfoProvider = new STMActionsInfoProvider(this);
	protected STMAction<?> entryAction;
	protected STMAction<?> exitAction;
	protected String defaultFlowId = State.DEFAULT_FLOW_ID;
	private BeanFactoryAdapter beanFactory;
	protected StateEntityRetrievalStrategy<? extends StateEntity> retrievalStrategy;

	private ScriptingStrategy scriptingStrategy;
	private ComponentPropertiesHelper componentPropertiesHelper;

	protected STMTransitionAction<?> defaultTransitionAction;
	private EnablementStrategy enablementStrategy;
	private STMAutomaticStateComputation<?> defaultAutomaticStateComputation;

	/**
	 * Always initialize the action tags first
	 */
	public STMFlowStoreImpl() {

		XmlFlowReader xmlFlowReader = new XmlFlowReader(this);
		try {
			xmlFlowReader.setFilename(PATH_TO_DEFAULT_STM_CONFIGURATION);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "WARNING: cannot read the default file " + PATH_TO_DEFAULT_STM_CONFIGURATION
					+ ". Problems possible with custom tags.", e);
		} 
	}

	public void include(String file) throws Exception{
		if (file == null) return;
		XmlFlowReader xmlFlowReader = new XmlFlowReader(this);
		if (!file.endsWith(".xml"))file = file + ".xml";
		xmlFlowReader.setFilename(file);
	}

	public StateDescriptor getStateInfo(String flowId, String stateId) {
		return flows.get(flowId).getStates().get(stateId);
	}

	public FlowDescriptor getFlow(String flowId) {
		return flows.get(flowId);
	}

	public void addStateTag(StateTagDescriptor std) {
		actionTagsMap.put(std.getTag(), std);
	}

	/**
	 * This method is called during STD parsing. Every flow would result in a
	 * call to this so that the flow gets added to the configuration.
	 * 
	 * @param fd flow descriptor
	 */
	public void addFlow(FlowDescriptor fd) {
		String flowId = fd.getId();
		if (flowId == null)
			flowId = State.DEFAULT_FLOW_ID;
		fd.setFlowStore(this);
		flows.put(flowId, fd);
		if (fd.isDefault())
			defaultFlowId = fd.getId();
	}
	
	public void setDefaultFlow(FlowDescriptor fd) {
		this.defaultFlowId = fd.getId();
	}
	
	public Object makeComponent(String componentName) throws STMException {
		if (componentName == null)
			return null;
		if (componentName.startsWith("com.") || componentName.startsWith("org.")) {
			try {   
				return Class.forName(componentName).getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
				throw new STMException("Cannot instantiate a class of name " + componentName, 
						STMException.UNABLE_TO_CREATE_COMPONENT);
			}
		}
		Object bean = (beanFactory != null) ? beanFactory.getBean(componentName) : null;
		if (bean == null)
			throw new STMException("Cannot create a bean of name " + componentName, 
					STMException.UNABLE_TO_CREATE_COMPONENT);
		return bean;
	}

	public Object makeTransitionAction(String componentName,boolean enableInlineScriptsInProperties)
			throws STMException {
		return makeAction(componentName,enableInlineScriptsInProperties);
	}

	public Object makeAction(String componentName, boolean enableInlineScriptsInProperties) 
			throws STMException {
		Object stmaction = makeComponent(componentName); 
		if (stmaction == null)
			return stmaction;
		if (stmaction instanceof ScriptingStrategyAware) {
			((ScriptingStrategyAware) stmaction).setScriptingStrategy(scriptingStrategy);
		}
		if (stmaction instanceof ComponentPropertiesAware) {
			ComponentPropertiesAware cpa = (ComponentPropertiesAware) stmaction;
			cpa.setComponentPropertiesHelper(componentPropertiesHelper);
			cpa.setEnableInlineScriptsInProperties(enableInlineScriptsInProperties);
		}
		return stmaction;
	}

	public Object makeAutomaticStateComputation(String componentName, boolean enableInlineScriptsInProperties)
				throws STMException{
		return makeAction(componentName,enableInlineScriptsInProperties);
	}

	/**
	 * Make sure that the state machine is set up right.
	 * 
	 * @throws Exception
	 */
	public void validate() throws Exception {
		for (FlowDescriptor fd : flows.values()) {
			fd.validate(this);
		}
	}


	public FlowDescriptor getFlowInfo() {
		return getFlowInfo(defaultFlowId);
	}

	public FlowDescriptor getFlowInfo(String flowId) {
		return flows.get(flowId);
	}

	public void setEntryAction(String componentName, boolean enableInlineScriptsInProperties) 
			throws STMException {
		this.entryAction = (STMAction<?>) makeAction(componentName, enableInlineScriptsInProperties);
	}

	public void setExitAction(String componentName, boolean enableInlineScriptsInProperties) 
		throws STMException {
		this.exitAction = (STMAction<?>) makeAction(componentName, enableInlineScriptsInProperties);
	}
	
	public void setDefaultTransitionAction(String componentName) 
			throws STMException {
			setDefaultTransitionAction((STMTransitionAction<?>) makeTransitionAction(componentName, false));
	}

	public void setDefaultTransitionAction(STMTransitionAction<?> transitionAction){
		this.defaultTransitionAction = transitionAction;
	}

	public String getDefaultFlowId() {
		return defaultFlowId;
	}

	/**
	 * Called during parsing time when the scripting-strategy is defined in the
	 * configuration. In case of XML configuration, this method is called when
	 * the &lt;scripting-strategy ...&gt; xml element is encountered during
	 * parsing
	 * 
	 * @param componentName
	 */
	public void setScriptingStrategy(String componentName) throws STMException{
		this.scriptingStrategy = (ScriptingStrategy) makeScriptingStrategy(componentName);
		componentPropertiesHelper = new ComponentPropertiesHelper();
		componentPropertiesHelper.setScriptingStrategy(scriptingStrategy);
		componentPropertiesHelper.setFlowConfigurator(this);
	}

	public Object makeScriptingStrategy(String componentName) throws STMException {
		return makeComponent(componentName);
	}

	public Object makeSecurityStrategy(String componentName) throws STMException {
		return makeComponent(componentName);
	}

	public Object makeRetrievalStrategy(String componentName) throws STMException {
		return makeComponent(componentName);
	}

	public void setScriptingStrategy(ScriptingStrategy scriptingStrategy) {
		this.scriptingStrategy = scriptingStrategy;
		componentPropertiesHelper = new ComponentPropertiesHelper();
		componentPropertiesHelper.setScriptingStrategy(scriptingStrategy);
		componentPropertiesHelper.setFlowConfigurator(this);
	}

	public EnablementStrategy makeEnablementStrategy(String componentName) throws STMException {
		return (EnablementStrategy)makeComponent(componentName);
	}
	public void setEnablementStrategy(String componentName) throws STMException {
		setEnablementStrategy(makeEnablementStrategy(componentName));
	}
	public void setEnablementStrategy(EnablementStrategy enablementStrategy) {
		this.enablementStrategy = enablementStrategy;
	}

	/**
	 * 
	 */
	public STMAction<?> getEntryAction(State state) {
		state = correctState(state);
		if (state == null)
			return null;
		FlowDescriptor flow = flows.get(state.getFlowId());
		StateDescriptor sd = getStateInfo(state);
		if (sd == null)
			throw new RuntimeException("State " + state + " not defined in the STD.");
		LOGGER.fine(
				":::::::::::::::::::" + flow.isSkipEntryExitActionsForAutoStates() + "for state::::::::" + sd.getId());

		if (flow.isSkipEntryExitActionsForAutoStates() && !sd.isManualState())
			return null;
		// start with the most specific one and cascade up.

		LOGGER.fine(
				":::::::::::::::::::" + flow.isSkipEntryExitActionsForAutoStates() + "for state::::::::" + sd.getId());

		if (sd.getEntryAction() != null)
			return sd.getEntryAction();

		STMAction<?> entryAction = flow.getEntryAction();
		return (entryAction == null) ? this.entryAction : entryAction;
	}

	public STMAction<?> getExitAction(State state) {
		state = correctState(state);
		if (state == null)
			return null;

		FlowDescriptor flow = flows.get(state.getFlowId());
		StateDescriptor sd = getStateInfo(state);
		if (flow.isSkipEntryExitActionsForAutoStates() && !sd.isManualState())
			return null;
		// start with the most specific one and cascade up.

		if (sd != null && sd.getExitAction() != null)
			return sd.getExitAction();

		STMAction<?> exitAction = flows.get(state.getFlowId()).getExitAction();
		return (exitAction == null) ? this.exitAction : exitAction;
	}
	
	@Override
	public STMTransitionAction<?> getTransitionAction(Transition transition) {

		if (transition != null && transition.getTransitionAction() != null)
			return transition.getTransitionAction();
		
		return defaultTransitionAction;
	}

	@Override
	public STMAutomaticStateComputation<?> getAutomaticStateComputation(AutomaticStateDescriptor sd) {
		STMAutomaticStateComputation<?> action = sd.getComponent();
		return (action == null)? defaultAutomaticStateComputation : action;
	}

	private State correctState(State state) {
		State retState = state;
		
		if (retState == null)
			retState = new State(null, getDefaultFlowId());
		
		FlowDescriptor fd = flows.get(retState.getFlowId());
		
		if (fd == null)
			return null;
		
		if (retState.getStateId() == null) {
			retState.setStateId(fd.getInitialState());
		}
		return retState;
	}

	public StateDescriptor getStateInfo(State state) {
		state = correctState(state);
		if (state == null)
			return null;
		StateDescriptor sd = flows.get(state.getFlowId()).getStates().get(state.getStateId());
		if (enablementStrategy != null) {
			if (sd == null) {
				sd = enablementStrategy.getStateInfo(state, flows.get(state.getFlowId()));
			}else {
				enablementStrategy.addMetadataToState(sd);
			}
		}
		return sd;
	}

	public State getInitialState(State state) throws STMException {
		state = correctState(state);
		
		if (state == null || state.getFlowId() == null)
			throw new STMException(
					"Undefined default flow. Did you make sure that you have at least one flow with default = true? ",
					STMException.UNDEFINED_INITIAL_STATE_OR_FLOW);
		if (state.getStateId() == null)
			throw new STMException("Undefined initial state for the flow " + state.getFlowId(),
					STMException.UNDEFINED_INITIAL_STATE_OR_FLOW);
		return state;
	}

	public void setEntryAction(STMAction<?> entryAction) {
		this.entryAction = entryAction;
	}

	public void setExitAction(STMAction<?> exitAction) {
		this.exitAction = exitAction;
	}

	public BeanFactoryAdapter getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactoryAdapter beanFactory) {
		this.beanFactory = beanFactory;
	}

	public StateEntityRetrievalStrategy<? extends StateEntity> getDefaultRetrievalStrategy() {
		FlowDescriptor defFlow = flows.get(defaultFlowId);
		if (defFlow == null)
			return null;
		return defFlow.getRetrievalStrategy();
	}

	@Override
	public STMSecurityStrategy getSecurityStrategy(String flowId) {
		FlowDescriptor fd = flows.get(flowId);
		if (fd == null)
			return null;
		return fd.getStmSecurityStrategy();
	}
	
	@Override
	public String toString() {
		return flows.toString();
	}

	public String toJson(){
		StringBuilder stringBuilder = new StringBuilder("{\"flows\": [\n");
		boolean first = true;
		for (FlowDescriptor fd: flows.values()){
			if (!first) stringBuilder.append(",");
			else first = false;
			stringBuilder.append(fd.toJson());
		}
		return stringBuilder.append("]}\n").toString();
	}

	public Map<String,Object> toMap(){
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		for (FlowDescriptor fd: flows.values()){
			list.add(fd.toMap());
		}
		map.put("flows",list);
		return map;
	}

	public void addEventInformation(EventInformation eventInformation) {
		if (eventInformation.tagDefinition)
			eventInfos.put(eventInformation.getTag(),eventInformation);
		else
			eventInfos.put(eventInformation.getEventId(), eventInformation);
	}
	
	@Override
	public EventInformation getEventInformation(String eventId) {
		return eventInfos.get(eventId);
	}

	@Override
	public Collection<StateDescriptor> getAllStates() {
		return new ArrayList<>(getFlowInfo().getStates().values());
	}


	@Override
	public String getDefaultFlow(){
		return defaultFlowId;
	}

	@Override
	public Collection<FlowDescriptor> getAllFlows() {
		return flows.values();
	}


	public EnablementStrategy getEnablementStrategy() {
		return enablementStrategy;
	}

	public STMAutomaticStateComputation<?> getDefaultAutomaticStateComputation() {
		return defaultAutomaticStateComputation;
	}

	public void setDefaultAutomaticStateComputation(STMAutomaticStateComputation<?> defaultAutomaticStateComputation) {
		this.defaultAutomaticStateComputation = defaultAutomaticStateComputation;
	}
}