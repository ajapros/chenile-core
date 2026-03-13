# Chenile Intro Google Slides Copy-Paste Outline

This document is optimized for fast deck creation.

Use one section per slide. Copy the title into the slide title box, and copy the bullets into the body box.

## Slide 1

### Title

Introducing Chenile

### Body

- A common service execution model for Java and Spring
- REST, interceptors, transformation, events, trajectories, STM

### Speaker reminder

Open broad. Do not go deep yet.

---

## Slide 2

### Title

Why Chenile?

### Body

- Business logic gets mixed with service plumbing
- Cross-cutting concerns get repeated
- HTTP and event flows often evolve separately

### Speaker reminder

Frame the problem first.

---

## Slide 3

### Title

What Chenile Is

### Body

- Open source Java and Spring framework
- More than HTTP
- A service execution framework

### Speaker reminder

Stress "service execution framework."

---

## Slide 4

### Title

Core Runtime Model

### Body

- Input becomes `ChenileExchange`
- Exchange moves through an interceptor chain
- Target service is resolved and invoked
- Response returns through the same model

### Speaker reminder

This is the anchor concept.

---

## Slide 5

### Title

Two Ways To Define Services

### Body

- JSON service definitions
- Annotation-based controllers
- Same internal runtime model
- Same execution pipeline

### Speaker reminder

Highlight convergence, not difference.

---

## Slide 6

### Title

The Interceptor Chain

### Body

- Fixed execution skeleton
- Dynamic expansion per service and operation
- Cross-cutting behavior in one place
- Validation, transformation, dispatch, response handling

### Speaker reminder

Keep it high-level.

---

## Slide 7

### Title

Transformation And ServiceInvoker

### Body

- Raw payload becomes a typed domain object
- Headers and body become method arguments
- Domain-oriented service methods stay cleaner

### Speaker reminder

Connect to Domain Driven Design.

---

## Slide 8

### Title

Events And Trajectories

### Body

- Events reuse the same Chenile pipeline
- Operations can subscribe through JSON or annotations
- Trajectories swap implementation beans at runtime

### Speaker reminder

Show that Chenile is broader than HTTP.

---

## Slide 9

### Title

The STM Module

### Body

- Declarative workflow subsystem
- Manual states and automatic states
- Entry actions, exit actions, and transitions

### Speaker reminder

Position STM as a broader capability.

---

## Slide 10

### Title

Why Chenile Matters

### Body

- Less repeated plumbing
- Cleaner service interfaces
- One runtime model across multiple execution styles
- Extensible architecture

### Speaker reminder

Translate mechanics into practical value.

---

## Slide 11

### Title

Next Steps

### Body

- Start with `ChenileExchange`
- Then the interceptor chain
- Then transformation, events, trajectories, and STM

### Speaker reminder

End with a clear path forward.

---

## Optional Slide 12

### Title

Follow-Up Deep Dives

### Body

- Service-definition JSON
- Chenile annotations
- Interceptor chain
- Transformation and ServiceInvoker
- Events and trajectories
- STM workflows

### Speaker reminder

Use only if the talk is part of a series.
