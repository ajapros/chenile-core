package org.chenile.stm.test.orderapproval;

import org.chenile.stm.State;
import org.chenile.stm.action.STMAction;

public class ExitAction implements STMAction<Order>{

	@Override
	public void execute(State startState, State endState, Order order) throws Exception {
		order.addExitTransition(order.getCurrentState());

	}
}
