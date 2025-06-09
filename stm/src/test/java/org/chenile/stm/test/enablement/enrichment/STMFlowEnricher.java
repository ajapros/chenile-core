package org.chenile.stm.test.enablement.enrichment;

import org.chenile.stm.STMFlowStore;
import org.chenile.stm.model.*;

/**
 * Enriches the STM flow store by using the STM Fluent API. It provides an approval flow for some
 * transitions that have been annotated with metadata  "approvalRequired = true".
 * <p>For these transitions, the flow is altered to transition to an auto-state named as
 * {eventId}_IS_APPROVAL_REQUIRED unless overridden with a metadata property called approvalRequiredState
 * The auto state dynamically determines if an approval is required. If approval is required, then the entity is
 * moved into a newly created manual state. This new manual state is by default called
 * "${transitionEndState}_APPROVAL_PENDING where transitionEndState is the newStateId of the annotated transition.
 * But it is possible to override the default name by providing "approvalPendingState" as the metadata in the
 * transition.</p>
 */
public class STMFlowEnricher {
    public static final String APPROVAL_METADATA = "approvalRequired";
    public static final String APPROVAL_PENDING_SUFFIX = "_APPROVAL_PENDING";
    public static final String APPROVE_TRANSITION_NAME = "approve";
    public static final String APPROVAL_PENDING_STATE_NAME_METADATA = "approvalPendingState";
    public static final String IS_APPROVAL_REQUIRED_SUFFIX = "_IS_APPROVAL_REQUIRED";
    public static final String APPROVAL_REQUIRED_STATE_NAME_METADATA = "approvalRequiredState";

    public void enrich(STMFlowStore stmFlowStore) throws Exception{
        for (FlowDescriptor fd: stmFlowStore.getAllFlows()){
            for (StateDescriptor sd: fd.getStates().values()){
                for (Transition transition: sd.getTransitions().values()){
                    if (transitionNeedsApproval(transition)){
                        addApproval(transition);
                    }
                }
            }
        }
    }

    /**
     * Modify the flow from<br/>
     * transition -> endState <br/>
     * to <br/>
     * <pre>
     * transition -> isApprovalRequiredState
     *                 -> yes
     *                     approvalPendingState
     *                     -> approve
     *                        endState
     *                 -> no
     *                     endState
     * </pre>
     * @param transition current transition
     * @throws Exception exception if flow cannot be modified
     */
    private void addApproval(Transition transition) throws  Exception{
        String approvalPendingStateId = obtainApprovalPendingStateId(transition);
        String approvalRequiredStateId = obtainApprovalRequiredStateId(transition);
        String endStateId = transition.getNewStateId();
        transition.
                newStateId(approvalRequiredStateId).
            state().
            flow().
                autoState(approvalRequiredStateId)
                .on("yes")
                .newStateId(approvalPendingStateId)
            .state()
                .on("no")
                .newStateId(endStateId)
            .state()
            .flow()
                .manualState(approvalPendingStateId)
                .on(APPROVE_TRANSITION_NAME)
                .newStateId(endStateId);
    }

    private String obtainApprovalRequiredStateId(Transition transition){
        String isApprovalRequiredStateId = transition.getMetadata().get(APPROVAL_REQUIRED_STATE_NAME_METADATA);
        return (isApprovalRequiredStateId == null) ?
            transition.getEventId() + IS_APPROVAL_REQUIRED_SUFFIX
            : isApprovalRequiredStateId;
    }

    private String obtainApprovalPendingStateId(Transition transition){
        String approvalPendingStateId = transition.getMetadata().get(APPROVAL_PENDING_STATE_NAME_METADATA);
        return (approvalPendingStateId == null)?
            transition.getNewStateId() + APPROVAL_PENDING_SUFFIX
            : approvalPendingStateId;
    }

    private boolean transitionNeedsApproval(Transition transition) {
        return Boolean.parseBoolean(transition.getMetadata().get(APPROVAL_METADATA));
    }
}
