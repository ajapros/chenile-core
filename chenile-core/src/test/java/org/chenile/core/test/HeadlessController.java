package org.chenile.core.test;

import org.chenile.core.annotation.ChenileOperation;
import org.chenile.core.annotation.ChenileBody;
import org.chenile.core.event.SubscribeTo;
import org.chenile.http.annotation.ChenileController;

/** A Chenile controller deliberately not exposed through Spring MVC. */
@ChenileController(value = "headlessController", registerInServiceRegistry = false)
public class HeadlessController {
	private Foo lastEvent;

	@ChenileOperation
	@SubscribeTo("headless-event")
	public Foo consume(@ChenileBody Foo event) {
		lastEvent = event;
		return event;
	}

	public Foo getLastEvent() {
		return lastEvent;
	}
}
