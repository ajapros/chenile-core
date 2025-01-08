package org.chenile.stm.action;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.StateEntity;
import org.chenile.stm.impl.STMImpl;
import org.chenile.stm.model.Transition;

/**
 * This interface must be implemented to capture the actual logic to update the state to the new state ID.
 * STM calls this method (if set) for every state event that occurs. STM also supports the notion
 * of a default transition action if this action is not explicitly set for a transition. Use the
 * &lt;default-transition-action componentName=""/&gt; tag to define a default transition action.<br/>
 * Chenile Workflow uses this to implement more functionality.
 *
 * @param <StateEntityType>
 */
public interface STMTransitionAction<StateEntityType extends StateEntity> {
	public abstract void doTransition(StateEntityType stateEntity, Object transitionParam, 
			State startState, String eventId, State endState,STMInternalTransitionInvoker<?> stm, Transition transition) throws Exception;
}
