package org.chenile.trigger.test.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Subscribes to the test events and records what each delivery carried. */
public class OrderListener {
  public record Delivery(String eventId, String orderId, Map<String, Object> headers) {}

  public final List<Delivery> deliveries = new CopyOnWriteArrayList<>();

  public void orderPlaced(OrderPlaced order, Map<String, Object> headers) {
    deliveries.add(new Delivery("orderPlaced", order.orderId, new HashMap<>(headers)));
  }

  public void orderRejected(OrderPlaced order, Map<String, Object> headers) {
    deliveries.add(new Delivery("orderRejected", order.orderId, new HashMap<>(headers)));
    throw new IllegalStateException("Order " + order.orderId + " was rejected");
  }
}
