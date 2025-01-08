package org.chenile.stm.test.basicflow;

import org.chenile.stm.STMInternalTransitionInvoker;
import org.chenile.stm.State;
import org.chenile.stm.action.STMTransitionAction;
import org.chenile.stm.model.Transition;

public class ApproveCart implements STMTransitionAction<Cart> {

	public static final String LOGMESSAGE = "ApproveCart";
	@Override
	public void doTransition(Cart cart, Object transitionParam,
			State startState, String eventId, State endState,
			STMInternalTransitionInvoker<?> stm,Transition transition) throws Exception {
		cart.approved = true;
		cart.log(LOGMESSAGE);
	}

}
