package org.chenile.stm.test.enablement;

import junit.framework.TestCase;
import org.chenile.stm.STM;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;
import org.chenile.stm.model.StateDescriptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestEnablement extends TestCase{
	
	protected STM<MfgModel> stm = new STMImpl<>();
	protected ConfigProviderImpl configProvider = new ConfigProviderImpl();
	protected STMFlowStoreImpl stmFlowStore = null;
	private final Map<String, Object> compMap = new HashMap<String, Object>();
	protected static final String FLOW_DEFINITION_FILE = "org/chenile/stm/test/enablement/mfg.xml";
	private static final String ENTRY_ACTION = "ENTRY_ACTION";
	private static final String EXIT_ACTION = "EXIT_ACTION";
	public class MyStore extends STMFlowStoreImpl {
		// Provide a hook to a local component factory to facilitate mocking
		@Override
		public Object makeComponent(String componentName) throws STMException{
			Object action =  compMap.get(componentName);
			if (action == null) {
				action = super.makeComponent(componentName);
			}
			return action;
		}
	}

	private  void createComponents() throws Exception{
		STMAction<MfgModel> entryAction = new MfgAction(ENTRY_ACTION);
		compMap.put("entryAction",entryAction);
		STMAction<MfgModel> exitAction = new MfgAction(EXIT_ACTION);
		compMap.put("exitAction",exitAction);
		STMTransitionAction<MfgModel> transitionAction = new MfgAction();
		compMap.put("defaultTransitionAction",transitionAction);
		ConfigBasedEnablementStrategy configBasedEnablementStrategy = new ConfigBasedEnablementStrategy(configProvider);
		compMap.put("configBasedEnablementStrategy",configBasedEnablementStrategy);
	}

	public void setUp() throws Exception{
		createComponents();
		stmFlowStore = new MyStore();
		XmlFlowReader flowReader = new XmlFlowReader(stmFlowStore);
		flowReader.setFilename(FLOW_DEFINITION_FILE);
		stm.setStmFlowStore(stmFlowStore);
	}
	
	public void testNoEnablementProperties() throws Exception{
		configProvider.clear();
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS1",null);
		assertEquals(new State("S1","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS2",null);
		assertEquals(new State("S2","MFG_FLOW"),mfgModel.getCurrentState());
		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS1",
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS2",
				ENTRY_ACTION,
				EXIT_ACTION
		};
		
		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testHideState() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				MFG_FLOW.S1.enabled=false
				# Add transition doS2 to CREATED state that will lead to state S2
				MFG_FLOW.CREATED.transition.add.doS2=S2
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS2",null);
		assertEquals(new State("S2","MFG_FLOW"),mfgModel.getCurrentState());
		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS2",
				ENTRY_ACTION,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testAddState() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				# Add new State S3 to the MFG_FLOW
				MFG_FLOW.state.add.S3=
				# Add transition doS3 to S2 state that will lead to state S3
				MFG_FLOW.S2.transition.add.doS3=S3
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS1",null);
		assertEquals(new State("S1","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS2",null);
		assertEquals(new State("S2","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS3",null);
		assertEquals(new State("S3","MFG_FLOW"),mfgModel.getCurrentState());
		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS1",
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS2",
				ENTRY_ACTION,
				EXIT_ACTION,
				"doS3",
				ENTRY_ACTION,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testAddStateMetadata() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				# Add new State S3 to the MFG_FLOW
				MFG_FLOW.state.add.S3=
				# Add meta data to State S3
				MFG_FLOW.S3.meta.key=value
				""");
		StateDescriptor sd = stmFlowStore.getStateInfo(new State("S3","MFG_FLOW"));
		assertEquals("Meta data not set","value",sd.getMetadata().get("key"));
	}

	public void testAddMultipleStates() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				# Add new States S3 and S4 to the MFG_FLOW
				MFG_FLOW.state.add.S3=
				MFG_FLOW.state.add.S4=
				# Add transitions from S2 to S3(doS3) and S3 to S4(doS4)
				MFG_FLOW.S2.transition.add.doS3=S3
				MFG_FLOW.S3.transition.add.doS4=S4
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS1",null);
		assertEquals(new State("S1","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS2",null);
		assertEquals(new State("S2","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS3",null);
		assertEquals(new State("S3","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS4",null);
		assertEquals(new State("S4","MFG_FLOW"),mfgModel.getCurrentState());
		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS1",
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS2",
				ENTRY_ACTION,
				EXIT_ACTION,
				"doS3",
				ENTRY_ACTION,
				EXIT_ACTION,
				"doS4",
				ENTRY_ACTION,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testChangeNewStateId() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				# Change transitions e1 to goto state S2
				MFG_FLOW.CREATED.e1.newStateId=S2
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"e1",null);
		assertEquals(new State("S2","MFG_FLOW"),mfgModel.getCurrentState());
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"e1",
				ENTRY_ACTION ,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testDisableTransition() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				MFG_FLOW.S1.doS2.enabled=false
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS1",null);
		assertEquals(new State("S1","MFG_FLOW"),mfgModel.getCurrentState());
		try{
			stm.proceed(mfgModel,"doS2",null);
			fail("The transition doS2 is disabled. So it should have failed");
		}catch(STMException e){
			assertTrue("Error code must be either 550 or 580",
					e.getMessageId() == 550 || e.getMessageId() == 580);
		}

		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS1",
				ENTRY_ACTION ,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

	public void testDisableState() throws Exception{
		configProvider.clear();
		configProvider.setProperties("""
				MFG_FLOW.S2.enabled=false
				""");
		MfgModel mfgModel = new MfgModel();
		stm.proceed(mfgModel);
		assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
		stm.proceed(mfgModel,"doS1",null);
		assertEquals(new State("S1","MFG_FLOW"),mfgModel.getCurrentState());
		try{
			stm.proceed(mfgModel,"doS2",null);
			fail("The state S1 is disabled. So transition doS2 should have failed");
		}catch(STMException e){
			assertTrue("Error code must be either 550 or 580",
					e.getMessageId() == 550 || e.getMessageId() == 580);
		}

		// Expected calling sequence as documented in the log.
		List<String> log = mfgModel.log;

		String[] expectedLog = {
				ENTRY_ACTION ,
				EXIT_ACTION ,
				"doS1",
				ENTRY_ACTION ,
				EXIT_ACTION
		};

		assertEquals(Arrays.asList(expectedLog), log);
	}

}