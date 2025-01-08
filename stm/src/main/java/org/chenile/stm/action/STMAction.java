package org.chenile.stm.action;

import org.chenile.stm.StateEntity;


/**
 * Implement this interface to become an STM entry or exit action.<br/>
 * This can be used for both entry and exit actions.
 *  The entry action is called BEFORE the state is entered. The exit method is called at the time the state is exited out of.
 *  In an entry action, if the start state is null then the end state would be the initial state for the particular flow.
 *  This can be used for making decisions around inserting a new record etc.
 *  <p>Please see {@link org.chenile.stm.impl.STMImpl#proceed(StateEntity, String, Object)} for more documentation around the
 *  entry and exit actions.
 * @author Raja Shankar Kolluru
 *
 * 
 */
public interface STMAction<StateEntityType extends StateEntity> {
	public void execute(StateEntityType stateEntity) throws Exception;
}
