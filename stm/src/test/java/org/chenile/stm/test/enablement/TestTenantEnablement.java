package org.chenile.stm.test.enablement;

import junit.framework.TestCase;
import org.chenile.stm.STM;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;
import org.chenile.stm.test.enablement.tenant.SecurityContextHolder;
import org.chenile.stm.test.enablement.tenant.TenantConfigProviderImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTenantEnablement extends TestCase{
	
	protected STM<MfgModel> stm = new STMImpl<>();
	protected TenantConfigProviderImpl configProvider = new TenantConfigProviderImpl();
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
		//configProvider.clear();
		noEnablement();
	}

	public void testHideStateInTwoTenantSameThread() throws Exception{
		SecurityContextHolder.getContext().setTenant("t2");
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

		SecurityContextHolder.getContext().setTenant("t1");
		noEnablement();
	}


	private void noEnablement() throws Exception {
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


}