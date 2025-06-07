package org.chenile.stm.test.enablement.approval;

import junit.framework.TestCase;
import org.chenile.stm.STM;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;
import org.chenile.stm.test.enablement.MfgAction;
import org.chenile.stm.test.enablement.MfgModel;

import java.util.HashMap;
import java.util.Map;

public class TestApprovalEnablement extends TestCase {

        protected STM<MfgModel> stm = new STMImpl<>();
        protected ConfigProviderImpl configProvider = new ConfigProviderImpl();
        protected STMFlowStoreImpl stmFlowStore = null;
        private final Map<String, Object> compMap = new HashMap<String, Object>();
        protected static final String FLOW_DEFINITION_FILE = "org/chenile/stm/test/enablement/approval/mfg.xml";
        private static final String ENTRY_ACTION = "ENTRY_ACTION";
        private static final String EXIT_ACTION = "EXIT_ACTION";
        protected STMFlowEnricher stmFlowEnricher = null;
        public class MyStore extends STMFlowStoreImpl {
            // Provide a hook to a local component factory to facilitate mocking
            @Override
            public Object makeComponent(String componentName) throws STMException {
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
            stmFlowEnricher = new STMFlowEnricher();
            XmlFlowReader flowReader = new XmlFlowReader(stmFlowStore);
            flowReader.setFilename(FLOW_DEFINITION_FILE);
            // do approval processing
            stmFlowEnricher.enrich(stmFlowStore);
            stm.setStmFlowStore(stmFlowStore);
        }

        public void testDummy() throws Exception{
            MfgModel mfgModel = new MfgModel();
            stm.proceed(mfgModel);
            assertEquals(new State("CREATED","MFG_FLOW"),mfgModel.getCurrentState());
            stm.proceed(mfgModel,"e1",null);
            assertEquals(new State("S9_APPROVAL_PENDING","MFG_FLOW"),mfgModel.getCurrentState());
            stm.proceed(mfgModel,"approve",null);
            assertEquals(new State("S9","MFG_FLOW"),mfgModel.getCurrentState());
        }
}
