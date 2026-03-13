# Chenile Intro Final Slide Text

This file contains only the final on-slide text, with no commentary.

## Slide 1

### Title

Introducing Chenile

### Body

- A common service execution model for Java and Spring
- REST, interceptors, transformation, events, trajectories, STM

## Slide 2

### Title

Why Chenile?

### Body

- Business logic gets mixed with service plumbing
- Cross-cutting concerns get repeated
- HTTP and event flows often evolve separately

## Slide 3

### Title

What Chenile Is

### Body

- Open source Java and Spring framework
- More than HTTP
- A service execution framework

## Slide 4

### Title

Core Runtime Model

### Body

- Input becomes `ChenileExchange`
- Exchange moves through an interceptor chain
- Target service is resolved and invoked
- Response returns through the same model

## Slide 5

### Title

Two Ways To Define Services

### Body

- JSON service definitions
- Annotation-based controllers
- Same internal runtime model
- Same execution pipeline

## Slide 6

### Title

The Interceptor Chain

### Body

- Fixed execution skeleton
- Dynamic expansion per service and operation
- Cross-cutting behavior in one place
- Validation, transformation, dispatch, response handling

## Slide 7

### Title

Transformation And ServiceInvoker

### Body

- Raw payload becomes a typed domain object
- Headers and body become method arguments
- Domain-oriented service methods stay cleaner

## Slide 8

### Title

Events And Trajectories

### Body

- Events reuse the same Chenile pipeline
- Operations can subscribe through JSON or annotations
- Trajectories swap implementation beans at runtime

## Slide 9

### Title

The STM Module

### Body

- Declarative workflow subsystem
- Manual states and automatic states
- Entry actions, exit actions, and transitions

## Slide 10

### Title

Why Chenile Matters

### Body

- Less repeated plumbing
- Cleaner service interfaces
- One runtime model across multiple execution styles
- Extensible architecture

## Slide 11

### Title

Next Steps

### Body

- Start with `ChenileExchange`
- Then the interceptor chain
- Then transformation, events, trajectories, and STM
