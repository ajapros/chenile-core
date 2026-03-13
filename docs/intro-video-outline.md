# Chenile Intro Video Outline

This document is a practical outline for a short introductory video about Chenile. It is structured as:

- target outcome
- recommended video flow
- section-by-section talking points
- a rough transcript you can follow and adapt

The goal is not to read this word-for-word. The goal is to give you a clear structure and enough phrasing that recording is easy.

## Recommended video length

Aim for:

- 8 to 12 minutes for a concise intro
- 15 to 20 minutes if you want a light architecture walkthrough

If this is the first Chenile video, keep it short and focus on:

- what Chenile is
- what problem it solves
- how it is different
- the main runtime model

## Target audience

This outline assumes the audience is:

- Java and Spring developers
- architects evaluating service frameworks
- developers curious about interception, eventing, orchestration, and state machines

## Core message of the video

If viewers remember only three things, they should remember:

1. Chenile lets you define services once and use them across multiple entry points and execution styles.
2. Chenile separates infrastructure flow from domain logic through `ChenileExchange`, interceptors, and metadata.
3. Chenile includes more than HTTP: it also supports events, trajectories, transformation, and an STM/orchestration model.

## Suggested video structure

1. Opening hook
2. What Chenile is
3. The problem Chenile solves
4. The core runtime model
5. How services are defined
6. Interceptors and cross-cutting behavior
7. Events, trajectories, and transformation
8. STM and orchestration
9. Why this matters for real applications
10. Closing and next steps

---

## 1. Opening Hook

### Goal

Get the viewer interested quickly.

### Talking points

- Most microservice code mixes business logic with plumbing.
- Chenile tries to reduce that repetition.
- It gives you a service execution model that works across HTTP, events, and more.

### Rough transcript

"If you build Java microservices with Spring, a lot of code usually goes into the same kinds of concerns again and again: request mapping, transformation, interception, error handling, cross-cutting policies, event handling, and routing. Chenile is a framework that tries to simplify that by giving you a common execution model for services. In this video, I’ll show what Chenile is, how it works, and why its architecture is interesting."

---

## 2. What Chenile Is

### Goal

Define Chenile in one clear sentence.

### Talking points

- Open source Java and Spring framework
- focused on microservices
- supports REST, events, interception, orchestration, and state machines
- service metadata can come from JSON or annotations

### Rough transcript

"Chenile is an open source framework for building microservices in Java and Spring Boot. At one level, it helps you build REST services. But that is only part of the picture. Chenile also gives you an interception framework, an event processing model, a transformation layer, trajectory-based service overrides, and even a separate state machine module. So it is better to think of Chenile as a service execution framework, not just an HTTP helper."

---

## 3. The Problem Chenile Solves

### Goal

Explain why Chenile exists.

### Talking points

- business logic often gets mixed with infrastructure concerns
- cross-cutting concerns become repetitive
- teams need a consistent runtime model
- services should stay domain-oriented

### Rough transcript

"The problem Chenile is trying to solve is that service code often gets tightly coupled to transport and infrastructure concerns. You start with clean business logic, but over time request parsing, header handling, error formatting, body conversion, security checks, interceptors, and event plumbing start leaking everywhere. Chenile tries to centralize those concerns into a reusable pipeline, so your actual service methods can stay closer to domain logic."

---

## 4. The Core Runtime Model

### Goal

Introduce the most important concept: `ChenileExchange`.

### Talking points

- incoming requests become `ChenileExchange`
- exchange goes through interceptor chain
- service is resolved and invoked
- response goes back into the same exchange

### Suggested visual

- simple diagram:
  HTTP or event -> `ChenileExchange` -> interceptor chain -> service -> response

### Rough transcript

"The core runtime idea in Chenile is the `ChenileExchange`. This is the transport-neutral context object that carries headers, body, service metadata, operation metadata, response, exceptions, and other runtime state. Whether the request started as HTTP or as an event, Chenile normalizes it into a `ChenileExchange`. That exchange then moves through an interceptor chain, the target service is resolved, the method is invoked, and the response is written back into the same exchange."

---

## 5. How Services Are Defined

### Goal

Show the two authoring styles without going too deep.

### Talking points

- JSON-based service definitions
- annotation-based service definitions
- both become the same internal model
- `ChenileServiceDefinition` and `OperationDefinition`

### Rough transcript

"One nice design choice in Chenile is that services can be defined in two ways. You can define them using JSON metadata, or you can define them using annotations on Spring controllers. But the important part is that both approaches converge to the same internal model. Chenile turns both into service definitions and operation definitions. After that point, the runtime pipeline behaves the same way regardless of how the metadata was authored."

### Optional example line

"So whether you prefer externalized service metadata or annotation-driven configuration, Chenile gives you one execution model underneath."

---

## 6. Interceptors And Cross-Cutting Behavior

### Goal

Explain why the interceptor chain is a big deal.

### Talking points

- OWIZ-based execution chain
- fixed skeleton plus dynamic interpolation
- pre-processors, post-processors, service-specific and operation-specific interceptors
- response normalization and exception handling

### Rough transcript

"A major part of Chenile is the interceptor chain. Internally, this is driven by an orchestration engine called OWIZ. Chenile defines a fixed execution skeleton in XML, and then expands parts of that chain dynamically based on the current service and operation. That gives you a clean place for pre-processors, post-processors, exception handlers, service-specific interceptors, and operation-specific interceptors. This is one of the key ways Chenile removes repeated plumbing from application code."

---

## 7. Transformation, Domain Binding, Events, And Trajectories

### Goal

Explain the runtime features that make Chenile more than a REST wrapper.

### Talking points

- transformation converts raw body into domain object
- `ServiceInvoker` binds exchange data to domain-friendly method arguments
- events reuse the same pipeline
- trajectories can swap implementation beans at runtime

### Rough transcript

"Chenile also has a transformation layer. Instead of leaving your service methods to deal with raw payloads, Chenile can determine the correct body type, transform JSON into domain objects, and then bind those objects into domain-oriented service method signatures. That is where the `ServiceInvoker` becomes important. It takes the exchange and assembles the final Java arguments for the target method. On top of that, Chenile supports event subscriptions that reuse the same invocation pipeline, and trajectories, which let you override service implementations dynamically for selected requests."

### Optional DDD-focused line

"This is where Chenile aligns well with domain-driven design, because your core service methods can operate on domain objects rather than transport details."

---

## 8. STM And Orchestration

### Goal

Mention the larger platform story.

### Talking points

- Chenile includes orchestration support
- separate STM module for declarative workflows
- manual states and automatic states
- useful for approval and workflow scenarios

### Rough transcript

"Chenile also includes a state machine module, called STM. This is a separate subsystem in the repository, but it is an important part of the overall platform. It lets you define flows declaratively, with manual states, automatic states, transition actions, entry and exit actions, and even a small XML DSL. So if your application includes approval flows, routing logic, or stateful workflows, Chenile already has a model for that."

---

## 9. Why This Matters In Real Applications

### Goal

Translate architecture into practical value.

### Talking points

- less repeated plumbing
- consistent runtime behavior
- cleaner domain service methods
- reusable cross-cutting policies
- one model across HTTP and events

### Rough transcript

"Why does this matter in practice? Because a framework is only useful if it improves the shape of real code. Chenile gives you a consistent runtime path for service execution, centralizes cross-cutting concerns, supports cleaner service signatures, and lets you reuse the same concepts across HTTP, events, and workflow-style processing. So instead of solving these concerns differently in different parts of the system, you get one model that scales across them."

---

## 10. Closing And Next Steps

### Goal

End with a clear continuation path.

### Talking points

- this video is just an introduction
- next videos can go deeper into service definitions, interceptors, events, trajectories, STM
- point to repository and docs

### Rough transcript

"This was a high-level introduction to Chenile. If you want to go deeper, the next useful topics are service-definition JSON, annotation-based registration, the interceptor chain, transformation and ServiceInvoker, event subscriptions, trajectories, and the STM module. If you are exploring the repository, I recommend starting with the codebase overview and interceptor-chain documentation and then reading the tests alongside the runtime code."

---

## Optional 60-Second Version

If you want a very short intro video, use this:

"Chenile is an open source Java and Spring framework for building microservices around a common service execution model. It normalizes requests and events into a `ChenileExchange`, runs them through an interceptor pipeline, transforms payloads into domain objects, invokes domain-oriented service methods, and standardizes the response. Services can be defined either with JSON metadata or annotations, and Chenile also supports event subscriptions, trajectory-based routing overrides, orchestration, and a separate state machine module. So the key value of Chenile is that it centralizes service plumbing while letting your business logic stay cleaner and more domain-focused."

---

## Recording Tips

- Keep the first minute fast. Do not start with too much repository detail.
- Introduce `ChenileExchange` early. It is the easiest anchor concept.
- Use one visual diagram for the runtime pipeline.
- Use one example service to explain JSON vs annotations.
- Mention `ServiceInvoker` and transformation together, because that is where the DDD value becomes concrete.
- Mention STM, but do not let it dominate the intro video unless the video is specifically about workflows.
- End with a roadmap for follow-up videos.

## Suggested follow-up videos

1. Chenile service-definition JSON walkthrough
2. Chenile annotations and how they map to the same runtime model
3. The interceptor chain in detail
4. Transformation and `ServiceInvoker`
5. Trajectories and event processing
6. STM workflow modeling
