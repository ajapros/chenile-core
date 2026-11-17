package org.chenile.trigger;

import org.chenile.trigger.model.TriggerInput;
import org.chenile.trigger.model.TriggerResult;
import org.chenile.trigger.model.TriggerStatus;

/**
 * Raises a Chenile event on behalf of a source (the scheduler, file watch, an API call or code).
 * Every subscriber of the event receives the payload and the standard {@link TriggerHeaders}.
 *
 * <p>{@link DirectTriggerService} raises the event at once, in the caller's thread. Process
 * management supplies a durable implementation that records the trigger in its outbox first.
 */
public interface TriggerService {
  TriggerResult trigger(TriggerInput input);

  TriggerStatus status(String triggerId);
}
