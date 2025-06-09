/**
 * <p>This test case demonstrates a potential for extending the STM by enriching the State diagram based on
 * some metadata after it has been created. The enricher is created and called in the test case. The STM is not
 * aware of the fact that the enricher has been used.</p>
 * <p>An enricher has been created. This incorporates an approval process to an event if it sees a metadata
 * called approvalRequired in the event. If it sees that, the enricher creates an intermediary state to which
 * the event is guided. When an "approve" event is received from the intermediary state, the end state is
 * reached. In this way, we can incorporate the approval process without cluttering the state diagram with a
 * lot of repetitive intermediate states.
 *  </p>
 *  <p>In the test case, the event "e1" has the requisite metadata. It normally leads from S1 to S9 state.
 *  Instead, it is enhanced by the enricher to move from S1 to S9_APPROVAL_PENDING to S9 state. </p>
 *  <p>The on-approval tag illustrates a good use of the DSL to create meaningful tags.</p>
 *  <p>Note that enrichers are applied only during the creation of the STM. They do not help if the
 *  tag can be defined or enhanced dynamically using an Enablement Strategy. </p>
 */
package org.chenile.stm.test.enablement.enrichment;