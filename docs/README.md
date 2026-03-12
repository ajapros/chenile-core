# Chenile Core Documentation

This folder contains repository-focused documentation for the main runtime subsystems in `chenile-core`.

## Recommended reading order

If you are new to the repository, read in this order:

1. `codebase-overview.md`
2. `exchange-lifecycle.md`
3. `interceptor-chain.md`
4. `service-definition-json.md`
5. `chenile-annotations.md`
6. `transformation.md`
7. `service-invoker.md`
8. `trajectories.md`
9. `events.md`
10. `stm.md`

## By topic

### Core runtime

- `codebase-overview.md`: top-level architecture and module map
- `exchange-lifecycle.md`: what `ChenileExchange` is and how it changes during a request
- `interceptor-chain.md`: OWIZ flow, interceptor ordering, and runtime expansion

### Service registration

- `service-definition-json.md`: JSON-based service and operation metadata
- `chenile-annotations.md`: annotation-based registration and its equivalence to JSON

### Request shaping and dispatch

- `transformation.md`: body-type selection, JSON deserialization, subclass handling
- `service-invoker.md`: final argument binding from `ChenileExchange` to domain-oriented service methods
- `trajectories.md`: runtime service overrides based on trajectory id

### Eventing

- `events.md`: event definitions, subscriptions, and `EventProcessor`

### State machine subsystem

- `stm.md`: STM model, XML flow definitions, DSL features, and execution behavior

## Mental model for the repository

The shortest path to understanding the framework is:

1. service metadata is registered from JSON or annotations
2. requests and events become `ChenileExchange`
3. the OWIZ interceptor chain enriches the exchange and invokes a target service
4. transformation, trajectories, and event subscriptions are runtime variations on that same core path
5. the `stm` module is a separate but related subsystem for declarative stateful workflows
