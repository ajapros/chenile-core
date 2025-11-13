package org.chenile.stm.test.basicflow;

import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;

public class EntryAction implements STMAction<Cart>{

	public static final String LOGMESSAGE = "EntryAction";

	@Override
	public void execute(State startState, State endState, Cart cart) throws Exception {
		cart.log( LOGMESSAGE + ":" + cart.getCurrentState().getStateId());
	}
}
