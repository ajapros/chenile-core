# Chenile Intro Video Script

This document turns the intro outline into a production-ready recording script.

It includes:

- a recommended 10-minute structure
- slide-by-slide content
- rough narration you can read or adapt
- on-screen suggestions
- demo cues

This version is meant for a screen-recorded or slide-based introduction.

## Target format

- Length: 8 to 12 minutes
- Style: technical introduction
- Audience: Java and Spring developers, architects, framework evaluators

## Video flow

1. Title and hook
2. What Chenile is
3. The problem Chenile solves
4. Core runtime model
5. How services are defined
6. Interceptor chain
7. Transformation and ServiceInvoker
8. Events and trajectories
9. STM module
10. Why Chenile matters
11. Close and next steps

---

## Slide 1: Title

### Slide title

`Introducing Chenile`

### Slide bullets

- Microservices with a common execution model
- REST, events, interception, transformation, trajectories, STM

### On-screen suggestion

Show:

- project name
- repository
- a simple architecture teaser

### Narration

"In this video, I want to introduce Chenile. Chenile is an open source Java and Spring framework for building microservices around a common execution model. It supports REST services, event subscriptions, interception, transformation, runtime routing overrides called trajectories, and even a separate state machine module. The goal of this video is to give you a high-level understanding of what Chenile is, how it works, and why its architecture is useful."

---

## Slide 2: The Hook

### Slide title

`Why Another Framework?`

### Slide bullets

- Business logic gets mixed with plumbing
- Cross-cutting behavior gets repeated
- Different entry points often create inconsistent service code

### Narration

"If you build Java microservices with Spring, a lot of code usually ends up dealing with the same categories of concerns again and again. Request mapping. Header extraction. Body conversion. Error formatting. Interceptors. Policy checks. Event handling. Routing rules. Over time, your domain code starts to mix with infrastructure code. Chenile tries to reduce that by giving you one consistent service execution model underneath all of those concerns."

---

## Slide 3: What Chenile Is

### Slide title

`What Is Chenile?`

### Slide bullets

- Open source Java and Spring framework
- Service execution framework, not only HTTP
- Metadata-driven runtime model

### Narration

"The best way to think about Chenile is not just as an HTTP framework. It is really a service execution framework. Yes, it helps you expose REST services. But it also gives you an interception framework, an event processing model, a transformation layer, trajectory-based runtime overrides, orchestration support, and a state machine module. So the central idea is not just request handling. The central idea is how services are represented and executed."

---

## Slide 4: Core Runtime Model

### Slide title

`The Core Runtime Model`

### Slide bullets

- Request or event becomes `ChenileExchange`
- Exchange moves through interceptor chain
- Service is resolved and invoked
- Response is written back into the exchange

### On-screen suggestion

Show a diagram like:

`HTTP/Event -> ChenileExchange -> Interceptors -> ServiceInvoker -> Response`

### Narration

"The most important concept in Chenile is the `ChenileExchange`. This is the transport-neutral runtime context. It contains headers, body, service metadata, operation metadata, response state, exception state, and other execution data. Whether the input started as HTTP or as an event, Chenile normalizes it into a `ChenileExchange`. That exchange then moves through an interceptor chain, the correct service bean is selected, the target method is invoked, and the response is written back into the same exchange."

---

## Slide 5: Why This Model Matters

### Slide title

`Why Normalize Everything Into An Exchange?`

### Slide bullets

- Same model for HTTP and events
- Cross-cutting logic runs in one place
- Domain services can stay cleaner

### Narration

"This design matters because it gives Chenile one runtime path for multiple ways of invoking services. It also means cross-cutting concerns can be centralized in the interceptor pipeline. And it allows your core service methods to stay much closer to domain concepts instead of dealing directly with transport details."

---

## Slide 6: How Services Are Defined

### Slide title

`Two Ways To Define Services`

### Slide bullets

- JSON-based service definitions
- Annotation-based controller definitions
- Same internal model either way

### On-screen suggestion

Show:

- `service-definition-json.md`
- `chenile-annotations.md`

### Narration

"Chenile lets you define services in two different ways. One is through JSON service definitions. The other is through annotations on Spring controllers. But the important architectural point is that both approaches converge to the same internal metadata model. They both produce Chenile service definitions and operation definitions. After registration, the runtime pipeline behaves the same way regardless of whether the metadata came from JSON or annotations."

---

## Slide 7: Interceptor Chain

### Slide title

`The Interceptor Chain`

### Slide bullets

- Fixed execution skeleton
- Dynamic expansion based on service and operation
- Pre-processors, post-processors, exception handling, response normalization

### On-screen suggestion

Show:

- `chenile-core.xml`
- `interceptor-chain.md`

### Narration

"A major feature of Chenile is the interceptor chain. Internally, Chenile uses an orchestration engine called OWIZ. The framework defines a fixed execution skeleton in XML, and then expands parts of that chain dynamically for the current request. That gives you a clean runtime structure for pre-processors, post-processors, exception handlers, service-specific interceptors, operation-specific interceptors, transformation, dispatch, and response normalization."

---

## Slide 8: Transformation And ServiceInvoker

### Slide title

`From Transport Data To Domain Method Calls`

### Slide bullets

- Body type is selected before invocation
- JSON is transformed into domain objects
- `ServiceInvoker` binds exchange data into method arguments
- Supports domain-oriented service interfaces

### On-screen suggestion

Show:

- `transformation.md`
- `service-invoker.md`

### Narration

"This is one of the most important parts of Chenile. The framework first determines what Java type the incoming body should become. Then it transforms the raw payload into that object. After that, `ServiceInvoker` assembles the final Java argument list for the service method. This is how Chenile supports domain-friendly service interfaces. Instead of forcing your services to deal with raw JSON or generic transport objects, Chenile can supply meaningful arguments like a `User`, an `Order`, or a command object plus a few header-derived values."

### Optional example line

"So if your service method is something like `updateUser(String userId, User user)`, Chenile can extract the id from headers or path data, transform the body into a `User`, and invoke that method directly."

---

## Slide 9: Events And Trajectories

### Slide title

`Beyond HTTP`

### Slide bullets

- Event subscriptions reuse the same pipeline
- Trajectories allow runtime service overrides

### On-screen suggestion

Show:

- `events.md`
- `trajectories.md`

### Narration

"Chenile is not limited to HTTP entry points. It also supports event subscriptions. An operation can subscribe to a named event, and when that event is handled, Chenile creates a new exchange and sends it through the same pipeline. Chenile also supports trajectories, which are runtime service overrides. That means you can keep the same logical service definition but switch the actual implementation bean for selected requests. This is useful for experiments, tenant-specific behavior, or alternate rollout paths."

---

## Slide 10: STM Module

### Slide title

`The STM Module`

### Slide bullets

- Separate declarative state machine subsystem
- Manual states and automatic states
- Workflow and approval use cases

### On-screen suggestion

Show:

- `stm.md`
- one flow XML example

### Narration

"Chenile also includes a separate state machine module, called STM. This is useful for workflows, approvals, and stateful business processes. It supports manual states, automatic states, transitions, entry actions, exit actions, and even a small XML DSL. So the repository is broader than a simple request framework. It includes a model for stateful workflow execution as well."

---

## Slide 11: Why Chenile Matters

### Slide title

`Why This Matters In Real Systems`

### Slide bullets

- Less repeated plumbing
- Cleaner domain services
- One runtime path across multiple execution styles
- Extensible architecture

### Narration

"The value of Chenile is not just that it has many features. The value is that those features fit into one coherent execution model. That means less repeated plumbing, more centralized cross-cutting behavior, cleaner domain service methods, and a more uniform architecture across HTTP services, event-driven processing, and workflows."

---

## Slide 12: Close

### Slide title

`Next Steps`

### Slide bullets

- Explore the docs
- Read the tests as examples
- Follow-up topics: JSON, annotations, interceptor chain, transformation, STM

### Narration

"This was a high-level introduction to Chenile. If you want to go deeper, the next useful topics are service-definition JSON, annotation-based registration, the interceptor chain, transformation and ServiceInvoker, events, trajectories, and the STM module. The repository now has focused documentation for each of these subsystems, and the tests are also very useful as executable examples."

---

## Suggested screen recording plan

If you want to record with repository visuals instead of slides, use this sequence:

1. Open `README.md`
2. Open `docs/README.md`
3. Open `docs/codebase-overview.md`
4. Open `docs/interceptor-chain.md`
5. Open `docs/transformation.md`
6. Open `docs/service-invoker.md`
7. Open `docs/events.md`
8. Open `docs/trajectories.md`
9. Open `docs/stm.md`

## Teleprompter-friendly short script

Use this if you want a smoother single-take recording:

"Chenile is an open source Java and Spring framework for building microservices around a common execution model. Instead of treating HTTP, events, transformation, interception, and routing as separate concerns solved in separate ways, Chenile normalizes execution into a common runtime object called `ChenileExchange`. That exchange moves through an interceptor chain, the correct service and operation are resolved, payloads are transformed into domain objects, and the final service method is invoked. Services can be defined either through JSON metadata or annotations, but both paths converge to the same runtime model. Chenile also supports event subscriptions, trajectory-based runtime service overrides, and a separate state machine module for workflows. The main architectural value of Chenile is that it centralizes service plumbing while allowing application services to stay cleaner and more domain-focused."

## Suggested follow-up videos

1. Service-definition JSON in Chenile
2. Chenile annotations and how they map to the same runtime model
3. The interceptor chain in detail
4. Transformation and ServiceInvoker
5. Events and trajectories
6. STM workflow modeling
