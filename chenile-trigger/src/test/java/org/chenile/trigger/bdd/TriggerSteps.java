package org.chenile.trigger.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.chenile.core.context.ContextContainer;
import org.chenile.trigger.DirectTriggerService;
import org.chenile.trigger.TriggerHeaders;
import org.chenile.trigger.TriggerService;
import org.chenile.trigger.model.TriggerInput;
import org.chenile.trigger.model.TriggerStatus;
import org.chenile.trigger.test.TriggerTestApplication;
import org.chenile.trigger.test.service.OrderListener;
import org.chenile.trigger.test.service.OrderListener.Delivery;
import org.chenile.trigger.test.service.OrderPlaced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = TriggerTestApplication.class)
public class TriggerSteps {
  @Autowired TriggerService triggers;
  @Autowired OrderListener listener;

  private final List<String> acceptedIds = new ArrayList<>();
  private RuntimeException failure;

  @Before
  public void reset() {
    ContextContainer.getInstance().clear();
    listener.deliveries.clear();
    acceptedIds.clear();
    failure = null;
  }

  @When("code triggers {string} for order {string} from source {string} with key {string}")
  public void triggerWithKey(String eventId, String orderId, String source, String key) {
    TriggerInput input = input(eventId, orderId);
    input.source = source;
    input.idempotencyKey = key;
    fire(input);
  }

  @When("code triggers {string} for order {string} from source {string} without a key")
  public void triggerWithoutKey(String eventId, String orderId, String source) {
    TriggerInput input = input(eventId, orderId);
    input.source = source;
    fire(input);
  }

  @When("tenant {string} triggers {string} for order {string} with key {string}")
  public void tenantTriggers(String tenant, String eventId, String orderId, String key) {
    TriggerInput input = input(eventId, orderId);
    input.idempotencyKey = key;
    input.headers.put(TriggerHeaders.TENANT_ID, tenant);
    fire(input);
  }

  @When("code triggers {string} for order {string} with header {string} set to {string}")
  public void triggerWithHeader(String eventId, String orderId, String header, String value) {
    TriggerInput input = input(eventId, orderId);
    input.headers.put(header, value);
    fire(input);
  }

  @Then("the listener received {int} deliver(y)(ies) of order {string}")
  public void received(int count, String orderId) {
    assertEquals(count, deliveriesOf(orderId).size());
  }

  @Then("the delivery has header {string} equal to {string}")
  public void deliveryHeader(String header, String value) {
    assertEquals(value, lastDelivery().headers().get(header));
  }

  @Then("the delivery has the accepted trigger id in header {string}")
  public void deliveryCarriesTriggerId(String header) {
    assertEquals(1, acceptedIds.size());
    assertEquals(acceptedIds.get(0), lastDelivery().headers().get(header));
  }

  @Then("the trigger status is {string}")
  public void triggerStatus(String status) {
    TriggerStatus reported = triggers.status(acceptedIds.get(acceptedIds.size() - 1));
    assertEquals(status, reported.status());
    assertNotNull(reported.finishedAt());
  }

  @Then("both triggers were accepted with the same id")
  public void sameId() {
    assertEquals(2, acceptedIds.size());
    assertEquals(acceptedIds.get(0), acceptedIds.get(1));
  }

  @Then("the deliveries carry tenants {string}")
  public void tenants(String tenants) {
    List<Object> seen =
        listener.deliveries.stream().map(d -> d.headers().get(TriggerHeaders.TENANT_ID)).toList();
    assertEquals(Arrays.asList((Object[]) tenants.split(",")), seen);
    assertNotEquals(acceptedIds.get(0), acceptedIds.get(1));
  }

  @Then("the trigger is refused with {string}")
  public void refused(String message) {
    assertNotNull(failure, "the trigger should have been refused");
    assertTrue(failure.getMessage().contains(message), failure.getMessage());
  }

  @Then("the trigger fails with {string}")
  public void fails(String message) {
    assertNotNull(failure, "the trigger should have failed");
    assertTrue(failure.getMessage().contains(message), failure.getMessage());
  }

  @Then("the status of the delivered trigger is {string} with error {string}")
  public void deliveredStatus(String status, String error) {
    String id = (String) lastDelivery().headers().get(TriggerHeaders.TRIGGER_ID);
    TriggerStatus reported = triggers.status(id);
    assertEquals(status, reported.status());
    assertEquals(error, reported.error());
    assertEquals(DirectTriggerService.FAILED, reported.status());
  }

  private TriggerInput input(String eventId, String orderId) {
    TriggerInput input = new TriggerInput();
    input.eventId = eventId;
    input.payload = new OrderPlaced(orderId);
    return input;
  }

  private void fire(TriggerInput input) {
    try {
      acceptedIds.add(triggers.trigger(input).triggerId());
    } catch (RuntimeException e) {
      failure = e;
    }
  }

  private List<Delivery> deliveriesOf(String orderId) {
    return listener.deliveries.stream().filter(d -> orderId.equals(d.orderId())).toList();
  }

  private Delivery lastDelivery() {
    assertTrue(!listener.deliveries.isEmpty(), "the listener received nothing");
    return listener.deliveries.get(listener.deliveries.size() - 1);
  }
}
