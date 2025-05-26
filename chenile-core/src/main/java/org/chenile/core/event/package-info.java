/**
 * Event processing and logging classes.
 * The Chenile event processing package defines the concept of a Chenile Event. A Chenile event is a string
 * that maps to multiple subscribers (a combination of operation + service). Chenile internal events can be used
 * in the following scenarios:<br/>
 * <ul>
 * <li>
 *     When you write a new Chenile entry point that accepts an external event and maps it to multiple Chenile
 * subscribers. For example, the Chenile file watcher uses this to map a file watch event to multiple subscribers.
 * </li>
 * <li>
 *     When you want to avoid circular dependency between two Chenile services. Service A calls Service B. Service B
 *     needs to invoke back Service A. You break the circular dependency by making Service B send a Chenile event
 *     that is subscribed to by Service A
 * </li>
 * </ul>
 */
package org.chenile.core.event;