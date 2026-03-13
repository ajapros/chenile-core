# Chenile Intro Video Speaker Notes

These notes are meant for live delivery or a natural screen recording. Unlike the full script, this version is:

- shorter
- easier to improvise from
- written in speaking rhythm
- focused on flow, transitions, and emphasis

Use this as presenter notes, not as slide text.

## General guidance

- Speak a little slower than normal conversation.
- Pause after each major concept.
- Do not try to explain every subsystem in depth.
- Keep coming back to the same core idea: one service execution model.

## Opening

### Key message

Chenile reduces repeated service plumbing by giving you one common runtime model.

### Notes

"In this video I want to introduce Chenile.

If you build Java microservices with Spring, you usually end up rewriting the same kinds of plumbing over and over. Request mapping, payload conversion, headers, error handling, interception, event handling, routing rules, all of that.

Chenile is a framework that tries to bring those concerns into one consistent execution model.

So instead of thinking of Chenile as just another REST layer, think of it as a service execution framework."

### Delivery cue

Pause after "service execution framework."

## What Chenile is

### Key message

Chenile is broader than HTTP.

### Notes

"At a high level, Chenile is an open source Java and Spring framework for microservices.

It supports REST services, but also interception, transformation, event subscriptions, trajectory-based routing overrides, orchestration, and a separate state machine module.

So the real value is not just HTTP endpoint generation. The value is the architecture underneath."

### Delivery cue

Stress:

- "not just HTTP"
- "architecture underneath"

## The problem it solves

### Key message

Business logic gets mixed with infrastructure concerns.

### Notes

"The problem Chenile is trying to solve is that service code often gets tightly mixed with infrastructure logic.

You want your service methods to talk in business terms. User. Order. Payment. Approval.

But in many codebases, those methods gradually get coupled to request parsing, generic payloads, headers, framework types, and transport-specific logic.

Chenile tries to separate those concerns more cleanly."

### Transition line

"And the central mechanism for that is the Chenile exchange."

## `ChenileExchange`

### Key message

Everything gets normalized into `ChenileExchange`.

### Notes

"The most important concept to understand is `ChenileExchange`.

This is the transport-neutral runtime context in Chenile.

It carries the body, headers, service metadata, operation metadata, response state, exception state, and other execution details.

Whether a request starts as HTTP or as an event, Chenile turns it into a `ChenileExchange`.

Then that exchange moves through the runtime pipeline."

### Delivery cue

When saying "transport-neutral runtime context," slow down a bit.

## The runtime pipeline

### Key message

Normalize, intercept, transform, invoke, respond.

### Notes

"Once Chenile has an exchange, it sends it through an interceptor chain.

In that chain, Chenile can validate headers, apply cross-cutting policies, determine body type, transform the request body into the right Java object, resolve the actual service bean, invoke the method, and normalize the response.

So the runtime story is:

input becomes exchange,
exchange moves through interceptors,
service gets invoked,
response goes back into the exchange."

### Transition line

"That runtime model is the reason the rest of Chenile hangs together."

## Service definitions: JSON and annotations

### Key message

Two authoring styles, one runtime model.

### Notes

"One thing I like in Chenile is that you can define services in more than one way.

You can use JSON service definitions, or you can use annotations on Spring controllers.

But both of those paths converge to the same internal objects.

So from the framework’s point of view, what matters is not whether metadata came from JSON or annotations. What matters is that Chenile ends up with the same service definition and operation definition."

### Delivery cue

Stress:

- "two authoring styles"
- "same internal objects"

## Interceptors

### Key message

Cross-cutting behavior is centralized in one pipeline.

### Notes

"A big part of Chenile is the interceptor chain.

Internally, Chenile uses an orchestration layer to define a fixed execution skeleton, and then it expands parts of that chain dynamically for the current service and operation.

That gives you one place for pre-processors, post-processors, exception handling, response normalization, and service-specific or operation-specific policies.

So instead of spreading these concerns across controllers and service classes, Chenile makes them part of the runtime pipeline."

### Transition line

"And this becomes especially useful when you get to transformation and service invocation."

## Transformation and ServiceInvoker

### Key message

Chenile helps keep service methods domain-oriented.

### Notes

"This is probably one of the strongest parts of the design.

Chenile first determines what type the incoming body should become.

Then it transforms the raw payload into that Java object.

And then `ServiceInvoker` takes the exchange and builds the final method arguments for the target service.

So your service method can often look like a domain method instead of a transport method.

For example, you can think in terms of something like `updateUser(String userId, User user)` instead of dealing directly with raw JSON and request parsing."

### Important precision note

If you want to be precise while speaking:

"The transformation layer turns the raw body into the domain object, and `ServiceInvoker` binds exchange data into the final Java method call."

### Delivery cue

Pause after the example method signature.

## Events

### Key message

Events reuse the same service execution model.

### Notes

"Chenile also supports event subscriptions.

An operation can subscribe to a named event, and when that event is triggered, Chenile creates an exchange and sends it through the same pipeline.

That is an important architectural choice.

Events are not treated like a completely separate world. They reuse the same service model, the same interception model, and the same invocation model."

## Trajectories

### Key message

Trajectories swap implementations without changing logical service definitions.

### Notes

"Another interesting feature is trajectories.

Trajectories let Chenile override the actual service implementation at runtime for selected requests.

So the logical service contract stays the same, but the implementation bean can change.

That is useful for experiments, tenant-specific behavior, rollout paths, or alternate implementations."

### Delivery cue

Stress:

- "logical service contract stays the same"

## STM module

### Key message

Chenile includes a separate workflow/state-machine subsystem.

### Notes

"The repository also includes a separate STM module.

That is Chenile’s state machine subsystem.

It supports declarative flows, manual and automatic states, transitions, entry and exit actions, and XML-based workflow definitions.

So if your application includes approval flows or stateful business workflows, Chenile already has a model for that."

## Why Chenile matters

### Key message

The value is coherence, not just features.

### Notes

"The reason Chenile is interesting is not just that it has a lot of features.

The interesting part is that those features fit into one coherent runtime model.

You get a common execution path for HTTP services, events, payload transformation, cross-cutting policies, runtime routing variation, and even workflows in the broader platform.

That coherence is what makes the framework worth studying."

### Delivery cue

Stress:

- "one coherent runtime model"

## Closing

### Key message

Point viewers toward the next layer.

### Notes

"This was just a high-level introduction.

If you want to go deeper, the next useful topics are service-definition JSON, annotation-based registration, the interceptor chain, transformation and ServiceInvoker, event subscriptions, trajectories, and the STM module.

The repository documentation now covers each of those areas in more detail, and the tests are also very helpful as executable examples."

## Optional closing line

"If you are evaluating Chenile, start with the runtime model first. Once `ChenileExchange`, the interceptor chain, and `ServiceInvoker` make sense, the rest of the framework starts to fit together."

## Suggested pacing

For a 10-minute video:

- Opening and framing: 1 minute
- What Chenile is: 1 minute
- Problem and runtime model: 2 minutes
- Service definitions and interceptors: 2 minutes
- Transformation and ServiceInvoker: 2 minutes
- Events, trajectories, STM: 1.5 minutes
- Why it matters and close: 0.5 minute

## Presenter reminders

- Do not over-explain OWIZ in the intro video.
- Do not over-explain STM in the intro video.
- Spend most of the time on the common runtime model.
- If you show code, use docs first and implementation files second.
- If you feel the video becoming too broad, cut detail and return to the central sentence:

"Chenile is a service execution framework that centralizes service plumbing around a common runtime model."
