# Chenile Intro Video Slide Deck

This document is a slide-deck version of the Chenile introduction. It is meant to help you build actual presentation slides quickly.

For each slide, this document gives:

- title
- exact or near-final bullet text
- optional visual suggestion
- presenter intent

This is optimized for an 8 to 12 minute intro.

## Deck structure

1. Title
2. Why Chenile
3. What Chenile is
4. Core runtime model
5. Service definition model
6. Interceptor chain
7. Transformation and ServiceInvoker
8. Events and trajectories
9. STM module
10. Why it matters
11. Next steps

---

## Slide 1

### Title

`Introducing Chenile`

### Bullets

- A service execution framework for Java and Spring
- REST, interception, transformation, events, trajectories, STM

### Visual suggestion

- project name
- repo link or logo
- very light architecture diagram teaser

### Presenter intent

Start broad. Do not go deep yet.

---

## Slide 2

### Title

`Why Chenile?`

### Bullets

- Service code often mixes business logic with plumbing
- Cross-cutting concerns get repeated across services
- HTTP, events, and workflows often evolve separately

### Visual suggestion

- split screen:
  business logic on one side
  plumbing concerns on the other

### Presenter intent

Frame the problem before describing the framework.

---

## Slide 3

### Title

`What Chenile Is`

### Bullets

- Open source Java and Spring microservice framework
- More than HTTP: a common service execution model
- Metadata-driven and interceptor-oriented architecture

### Visual suggestion

- simple capability map:
  REST
  events
  transformation
  trajectories
  STM

### Presenter intent

Make clear that Chenile is broader than request mapping.

---

## Slide 4

### Title

`The Core Runtime Model`

### Bullets

- Request or event becomes `ChenileExchange`
- Exchange goes through interceptor chain
- Target service is resolved and invoked
- Response is written back into the exchange

### Visual suggestion

Use a simple pipeline diagram:

`HTTP/Event -> ChenileExchange -> Interceptors -> ServiceInvoker -> Response`

### Presenter intent

This is the anchor slide. Spend the most time here.

---

## Slide 5

### Title

`How Services Are Defined`

### Bullets

- JSON service definitions
- Annotation-based controller definitions
- Same runtime model either way
- Internal model: `ChenileServiceDefinition` and `OperationDefinition`

### Visual suggestion

- two arrows converging into one box:
  JSON -> runtime model
  annotations -> runtime model

### Presenter intent

Show convergence, not choice anxiety.

---

## Slide 6

### Title

`The Interceptor Chain`

### Bullets

- Fixed execution skeleton
- Dynamic expansion per service and operation
- Pre-processors, post-processors, exception handling
- Response normalization and dispatch

### Visual suggestion

- simplified chain list:
  validate
  transform
  resolve service
  invoke
  normalize response

### Presenter intent

Show that Chenile centralizes cross-cutting runtime behavior.

---

## Slide 7

### Title

`Transformation And ServiceInvoker`

### Bullets

- Raw payload becomes a typed domain object
- Body type can be selected dynamically
- `ServiceInvoker` binds exchange data into method arguments
- Domain-oriented service methods stay clean

### Visual suggestion

- example:
  raw JSON -> `User`
  header `userId` + body `User` -> `updateUser(String userId, User user)`

### Presenter intent

Connect the architecture to Domain Driven Design.

---

## Slide 8

### Title

`Events And Trajectories`

### Bullets

- Events reuse the same Chenile pipeline
- Operations can subscribe through JSON or annotations
- Trajectories switch implementation beans at runtime
- Logical service contract stays the same

### Visual suggestion

- event arrow into exchange
- trajectory override arrow to alternate implementation

### Presenter intent

Show that Chenile handles multiple runtime styles without changing the model.

---

## Slide 9

### Title

`The STM Module`

### Bullets

- Separate declarative state machine subsystem
- Manual states and automatic states
- Entry actions, exit actions, transitions, XML flows
- Useful for approvals and business workflows

### Visual suggestion

- very small state diagram
  new -> pendingApproval -> fulfilled

### Presenter intent

Position STM as a broader platform capability, not the main topic of the intro.

---

## Slide 10

### Title

`Why Chenile Matters`

### Bullets

- Less repeated plumbing
- Cleaner domain service interfaces
- One runtime path across HTTP and events
- Extensible architecture for policies and workflows

### Visual suggestion

- concise summary diagram or four-value grid

### Presenter intent

Translate architecture into practical value.

---

## Slide 11

### Title

`Next Steps`

### Bullets

- Explore the repo docs and tests
- Start with `ChenileExchange` and the interceptor chain
- Then go deeper into JSON, annotations, transformation, events, trajectories, and STM

### Visual suggestion

- docs reading path
- or list of follow-up videos

### Presenter intent

End with a clear continuation path.

---

## Optional appendix slides

Use these only if you want a longer talk.

### Appendix A

`JSON vs Annotations`

- Same internal runtime model
- Different authoring styles
- Good follow-up topic

### Appendix B

`Runtime Pipeline`

- `ChenileExchange`
- transformation
- `ServiceInvoker`
- generic response builder

### Appendix C

`STM Example`

- one workflow XML sample
- one test case result

## Suggested design guidance

- Keep slides minimal. Speak the architecture; do not write paragraphs.
- Use one diagram for the core pipeline.
- Use one convergence diagram for JSON vs annotations.
- Use one domain-binding example for `ServiceInvoker`.
- Use one state diagram for STM.

## Fast-build version

If you want to build the deck quickly, use only these slide titles and bullets:

### Slide 1

`Introducing Chenile`

- Common service execution model
- Java and Spring
- REST, events, transformation, STM

### Slide 2

`Why Chenile?`

- Too much service plumbing
- Repeated cross-cutting concerns
- Inconsistent execution paths

### Slide 3

`Core Runtime Model`

- Input becomes `ChenileExchange`
- Interceptor chain processes it
- Service is invoked
- Response goes back into the exchange

### Slide 4

`Service Definitions`

- JSON
- Annotations
- Same runtime model

### Slide 5

`Interceptors`

- Fixed skeleton
- Dynamic expansion
- Cross-cutting behavior in one place

### Slide 6

`Transformation And ServiceInvoker`

- JSON to domain object
- Exchange to method arguments
- Cleaner service interfaces

### Slide 7

`Events And Trajectories`

- Same pipeline for events
- Runtime implementation overrides

### Slide 8

`STM Module`

- Declarative workflows
- Manual and automatic states

### Slide 9

`Why It Matters`

- Cleaner architecture
- Reusable runtime model
- More domain-focused services

### Slide 10

`Next Steps`

- Read the docs
- Explore the tests
- Follow-up deep dives
