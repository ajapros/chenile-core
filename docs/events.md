# Chenile Events

Chenile supports an in-process event model where operations can subscribe to named events and be invoked through the same interceptor pipeline used for HTTP calls.

This document explains:

- how events are defined
- how operations subscribe to events
- how subscribers are registered
- how `EventProcessor` dispatches events at runtime

## Core idea

Events in Chenile are identified by event id strings such as:

- `foo`
- `event1`
- `event2`

An operation can subscribe to one or more of these event ids. When an event is handled, Chenile creates a `ChenileExchange` for each subscriber and runs the normal interceptor chain.

That means event handling is not a separate execution stack. It reuses the same entry point and interceptors as ordinary service invocation.

## Core model classes

The key event model classes are:

- `chenile-core/src/main/java/org/chenile/core/model/ChenileEventDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/SubscriberVO.java`

`ChenileEventDefinition` stores:

- event id
- topic
- payload type
- subscribed service/operation pairs

`SubscriberVO` is the pair:

- `ChenileServiceDefinition`
- `OperationDefinition`

## Event definition JSON

Event definitions can be loaded from JSON using:

- `chenile.event.json.package`

Example from the tests:

```properties
chenile.event.json.package=classpath*:org/chenile/core/test/event/*.json
```

Example event JSON from `chenile-core/src/test/resources/org/chenile/core/test/event/foo.json`:

```json
{
  "id": "foo",
  "topic": "/foo",
  "type": "org.chenile.core.test.Foo"
}
```

Another example from `chenile-http/src/test/resources/org/chenile/http/test/event/event1.json`:

```json
{
  "id": "event1",
  "topic": "event1",
  "type": "org.chenile.http.test.service.JsonData"
}
```

These JSON files are loaded by `chenile-core/src/main/java/org/chenile/core/init/ChenileEventInitializer.java`.

## How operations subscribe to events

There are two ways in this repository.

### JSON path

An operation can declare:

```json
"eventSubscribedTo": ["foo", "event1"]
```

Example from `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`.

### Annotation path

A controller method can declare:

```java
@EventsSubscribedTo({"event1", "event2"})
```

Example from `chenile-http/src/test/java/org/chenile/http/test/controller/JsonController.java`.

Both paths populate the same field:

- `OperationDefinition.eventSubscribedTo`

## Subscriber registration

Subscriber registration happens after services and events are loaded.

The main class is:

- `chenile-core/src/main/java/org/chenile/core/init/ChenileEventSubscribersInitializer.java`

At `ApplicationReadyEvent`, it:

1. iterates through all registered services in `ChenileConfiguration`
2. iterates through each service’s operations
3. reads each operation’s `eventSubscribedTo` set
4. registers that operation as a subscriber on the matching `ChenileEventDefinition`

## Type checking during registration

When an operation subscribes to an event, Chenile ensures payload type consistency.

If the event already exists, `ChenileEventSubscribersInitializer` checks:

- event definition type
- operation input type

If those types do not match, startup fails with an event type mismatch error.

If the event id does not already exist, Chenile creates a minimal `ChenileEventDefinition` automatically using the operation input type.

This behavior explains how annotation-based subscriptions to undeclared event ids like `event2` can still work in the registry.

## Runtime dispatch

The main runtime dispatcher is:

- `chenile-core/src/main/java/org/chenile/core/event/EventProcessor.java`

`EventProcessor.handleEvent(eventId, payload, headers)` works like this:

1. look up the `ChenileEventDefinition` in `ChenileConfiguration`
2. get the set of subscribed service/operation pairs
3. for each subscriber
4. create a fresh `ChenileExchange`
5. attach the subscriber’s service and operation definitions
6. set the event payload as the exchange body
7. call `ChenileEntryPoint.execute(exchange)`

So event delivery reuses the normal Chenile invocation path.

## What event dispatch reuses from normal request handling

Event dispatch still uses:

- `ChenileExchange`
- `ChenileEntryPoint`
- the OWIZ interceptor chain
- `ConstructServiceReference`
- `ServiceInvoker`
- `GenericResponseBuilder`

This is an important architectural decision. It means:

- event subscribers can use the same interceptors
- trajectory and mock behavior can still apply if headers are present
- response and error normalization stay consistent

## Concrete examples

### JSON-defined subscriber

From `mockService.json`:

```json
{
  "name": "s8",
  "eventSubscribedTo": ["foo", "event1"],
  "input": "org.chenile.core.test.Foo",
  "params": [
    {
      "name": "foo",
      "type": "BODY",
      "paramType": "org.chenile.core.test.Foo",
      "paramClass": "org.chenile.core.test.Foo"
    }
  ]
}
```

This means operation `s8` subscribes to events `foo` and `event1`.

### Annotation-defined subscriber

From `JsonController`:

```java
@PostMapping("/c/save")
@EventsSubscribedTo({"event1", "event2"})
public ResponseEntity<GenericResponse<JsonData>> save(
        HttpServletRequest request, @RequestBody JsonData jsonData) {
    return process("save", request, jsonData);
}
```

This means the logical operation behind `save` is also a subscriber for `event1` and `event2`.

## Test example

The test in `chenile-core/src/test/java/org/chenile/core/test/TestChenileCore.java` shows the basic event path:

1. create a `Foo` payload
2. call `eventProcessor.handleEvent("foo", foo)`
3. verify that the subscribed service method ran

That test is the clearest minimal example of the in-process event dispatch behavior.

## Event definitions vs subscriptions

It helps to separate these two concepts:

### Event definition

Defines:

- event id
- event topic
- event payload type

### Event subscription

Defines:

- which operation should run when that event occurs

The framework combines them during startup by attaching `SubscriberVO` entries to `ChenileEventDefinition`.

## Relationship to HTTP and annotations

Event subscriptions can come from:

- JSON service definitions
- annotation-based controllers

But after registration, both are the same to the framework because both become `OperationDefinition.eventSubscribedTo`.

That is the same convergence pattern seen elsewhere in Chenile:

- multiple authoring styles
- one runtime model

## Mental model

Use this model:

- event definition says what the event is
- operation subscription says who listens to it
- `EventProcessor` turns the event into a normal Chenile exchange and sends it through the same pipeline as any other invocation

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/model/ChenileEventDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/SubscriberVO.java`
- `chenile-core/src/main/java/org/chenile/core/init/ChenileEventInitializer.java`
- `chenile-core/src/main/java/org/chenile/core/init/ChenileEventSubscribersInitializer.java`
- `chenile-core/src/main/java/org/chenile/core/event/EventProcessor.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/EventsSubscribedTo.java`
- `chenile-core/src/test/resources/org/chenile/core/test/event/foo.json`
- `chenile-http/src/test/resources/org/chenile/http/test/event/event1.json`
- `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`
- `chenile-http/src/test/java/org/chenile/http/test/controller/JsonController.java`
