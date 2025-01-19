package org.chenile.stm.test.dsl;

import junit.framework.TestCase;
import org.chenile.stm.STM;
import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.exception.STMException;
import org.chenile.stm.impl.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDsl extends TestCase {
    protected STM<AssemblyLineModel> stm = new STMImpl<>();
    protected ConfigProviderImpl configProvider = new ConfigProviderImpl();
    protected STMFlowStoreImpl stmFlowStore = null;
    private final Map<String, Object> compMap = new HashMap<String, Object>();
    protected static final String FLOW_DEFINITION_FILE = "org/chenile/stm/test/dsl/dsl.xml";
    private static final String ENTRY_ACTION = "ENTRY_ACTION";
    private static final String EXIT_ACTION = "EXIT_ACTION";
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
        STMAction<AssemblyLineModel> entryAction = new AssemblyAction(ENTRY_ACTION);
        compMap.put("entryAction",entryAction);
        STMAction<AssemblyLineModel> exitAction = new AssemblyAction(EXIT_ACTION);
        compMap.put("exitAction",exitAction);
        STMTransitionAction<AssemblyLineModel> transitionAction = new AssemblyAction();
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
        AssemblyLineModel AssemblyLineModel = new AssemblyLineModel();
        stm.proceed(AssemblyLineModel);
        assertEquals(new State("CREATED","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        stm.proceed(AssemblyLineModel,"doS1",null);
        assertEquals(new State("S1","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        stm.proceed(AssemblyLineModel,"doS2",null);
        assertEquals(new State("S2","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        stm.proceed(AssemblyLineModel,"reject",null);
        assertEquals(new State("REJECTED","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        // Expected calling sequence as documented in the log.
        List<String> log = AssemblyLineModel.log;

        String[] expectedLog = {
                ENTRY_ACTION ,
                EXIT_ACTION ,
                "doS1",
                ENTRY_ACTION ,
                EXIT_ACTION ,
                "doS2",
                ENTRY_ACTION,
                EXIT_ACTION,
                "reject",
                ENTRY_ACTION,
                EXIT_ACTION
        };

        assertEquals(Arrays.asList(expectedLog), log);
    }

    public void testHideState() throws Exception{
        configProvider.clear();
        configProvider.setProperties("""
				AssemblyLineFlow.S1.enabled=false
				# Add transition doS2 to CREATED state that will lead to state S2
				AssemblyLineFlow.CREATED.transition.add.doS2=S2
				""");
        AssemblyLineModel AssemblyLineModel = new AssemblyLineModel();
        stm.proceed(AssemblyLineModel);
        assertEquals(new State("CREATED","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        stm.proceed(AssemblyLineModel,"doS2",null);
        assertEquals(new State("S2","AssemblyLineFlow"),AssemblyLineModel.getCurrentState());
        // Expected calling sequence as documented in the log.
        List<String> log = AssemblyLineModel.log;

        String[] expectedLog = {
                ENTRY_ACTION ,
                EXIT_ACTION ,
                "doS2",
                ENTRY_ACTION
        };

        assertEquals(Arrays.asList(expectedLog), log);
    }

}
