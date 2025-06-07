package org.chenile.stm.test.enablement.approval;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.State;
import org.chenile.stm.impl.ConfigProviderImpl;
import org.chenile.stm.model.FlowDescriptor;
import org.chenile.stm.model.ManualStateDescriptor;
import org.chenile.stm.model.StateDescriptor;
import org.chenile.stm.model.Transition;

import java.util.Map;

public class STMFlowEnricher extends ConfigProviderImpl {
    public static final String APPROVAL_METADATA = "approvalRequired";
    public static final String APPROVAL_PENDING = "_APPROVAL_PENDING";
    public static final String APPROVE_TRANSITION_NAME = "approve";

    public void enrich(STMFlowStore stmFlowStore){
        for (FlowDescriptor fd: stmFlowStore.getAllFlows()){
            for (StateDescriptor sd: fd.getStates().values()){
                for (Transition transition: sd.getTransitions().values()){
                    if (transitionNeedsApproval(transition)){
                        addApproval(stmFlowStore,transition,sd);
                    }
                }
            }
        }
    }

    private void addApproval(STMFlowStore stmFlowStore,Transition transition, StateDescriptor sd) {
        FlowDescriptor fd = sd.getFlow();
        StateDescriptor approvalStateDescriptor = addApprovalStateIfRequired(stmFlowStore,fd,transition);
        if(approvalStateDescriptor == null) return;
        transition.setNewStateId(approvalStateDescriptor.getId());
        transition.setNewFlowId(approvalStateDescriptor.getFlowId());
    }

    private StateDescriptor addApprovalStateIfRequired(STMFlowStore stmFlowStore,
               FlowDescriptor fd,Transition transition){
        String newStateId = transition.getNewStateId();
        String approvalPendingStateId = newStateId + APPROVAL_PENDING;
        State approvalPendingState = new State(approvalPendingStateId,transition.getNewFlowId());
        StateDescriptor approvalPendingStateDescriptor = stmFlowStore.getStateInfo(approvalPendingState);
        if (approvalPendingStateDescriptor == null){
            approvalPendingStateDescriptor = new ManualStateDescriptor();
            approvalPendingStateDescriptor.setFlow(fd);
            approvalPendingStateDescriptor.setManualState(true);
            approvalPendingStateDescriptor.setId(approvalPendingStateId);
            try {
                fd.addsd(approvalPendingStateDescriptor);
            }catch(Exception e){
                return null;
            }
            // add approve transition
            Transition approveTransition = new Transition();
            approveTransition.setEventId(APPROVE_TRANSITION_NAME);
            approveTransition.setFlowId(fd.getId());
            approveTransition.setStateId(approvalPendingStateId);
            approveTransition.setNewStateId(transition.getNewStateId());
            approveTransition.setNewFlowId(transition.getNewFlowId());
            try {
                approvalPendingStateDescriptor.addTransition(approveTransition);
            }catch (Exception e){
                return null;
            }
        }
        return approvalPendingStateDescriptor;
    }

    private boolean transitionNeedsApproval(Transition transition) {
        Map<String, String> md = transition.getMetadata();
        String val = md.get(APPROVAL_METADATA);
        return Boolean.parseBoolean(val);
    }



}
