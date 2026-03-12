# Chenile Core Codebase Overview

This repository is a multi-module Java/Spring framework, not a single application. The parent build in `pom.xml` assembles a set of libraries for building microservices around a common execution model: requests and events are normalized into a `ChenileExchange`, passed through an interceptor/orchestration pipeline, and finally dispatched to service methods.

## Core architecture

The center of the system is `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`. It reads `chenile.properties`, creates the global `ChenileConfiguration`, loads service/event/trajectory definitions, and wires the execution beans: entry point, interceptor chain, transformer, event processor, exception handler, and the orchestration executor.

The runtime registry lives in `chenile-core/src/main/java/org/chenile/core/model/ChenileConfiguration.java`. It stores:

- service definitions
- event definitions
- trajectory overrides
- global pre/post processors resolved from Spring

A service in Chenile is metadata plus an implementation bean.

- `chenile-core/src/main/java/org/chenile/core/model/ChenileServiceDefinition.java` describes a service-level contract.
- `chenile-core/src/main/java/org/chenile/core/model/OperationDefinition.java` describes each operation: URL, HTTP method, input/output types, interceptors, security flags, events, and parameter bindings.

Those definitions are created either from JSON resources by the initializers in `chenile-core`, or from annotated Spring controllers by `chenile-http/src/main/java/org/chenile/http/init/AnnotationChenileServiceInitializer.java`.

For the JSON path specifically, see `docs/service-definition-json.md`.
For the annotation path specifically, see `docs/chenile-annotations.md`.

## Request flow

At runtime, every transport feeds the same pipeline. HTTP is the clearest example.

`chenile-http/src/main/java/org/chenile/http/handler/HttpEntryPoint.java` converts an incoming servlet request into a `ChenileExchange`. It attaches the selected service and operation, copies headers, query params, path variables, locale, multipart files, and raw body, then calls the shared `ChenileEntryPoint`.

`chenile-core/src/main/java/org/chenile/core/entrypoint/ChenileEntryPoint.java` delegates to the orchestration engine, which executes the interceptor chain.

The orchestration layer comes from the `owiz` module.

- `owiz/src/main/java/org/chenile/owiz/impl/Chain.java` is the basic ordered command chain.
- `owiz/src/main/java/org/chenile/owiz/impl/OrchExecutorImpl.java` starts execution from the first configured command in a flow.

Chenile uses that orchestration layer as its interceptor highway.

- `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java` resolves the actual Spring bean and method to invoke, including mock and trajectory overrides.
- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java` builds the Java argument list from the exchange and reflectively invokes the target method.
- `chenile-core/src/main/java/org/chenile/core/interceptors/GenericResponseBuilder.java` wraps the result or error into the framework response shape and sets the HTTP status on the exchange.

The shared data object throughout that pipeline is `chenile-core/src/main/java/org/chenile/core/context/ChenileExchange.java`. It carries:

- incoming headers
- request body
- multipart files
- service definition
- operation definition
- resolved service bean and method
- response
- exception
- warnings and response messages
- HTTP status

For the body-type selection and JSON-to-object conversion path between request intake and invocation, see `docs/transformation.md`.

## Annotation-based HTTP path

The annotation-based HTTP path is a convenience layer over the same runtime pipeline.

- Controllers can extend `chenile-http/src/main/java/org/chenile/http/handler/ControllerSupport.java`.
- They call `process(...)`.
- Chenile builds a `ChenileExchange` and sends it through the same interceptor chain.

## Event flow

Events are handled by `chenile-core/src/main/java/org/chenile/core/event/EventProcessor.java`.

It looks up subscribers for an event in `ChenileConfiguration`, creates a `ChenileExchange` for each subscriber, and sends each exchange through the same `ChenileEntryPoint`. That means HTTP calls and in-process event subscribers share the same execution model.

For a detailed walkthrough, see `docs/events.md`.

## Dynamic behavior: trajectories and interpolation

Two parts of the framework make the execution dynamic without changing the basic flow shape.

### Trajectories

Trajectory definitions allow Chenile to switch the target service implementation for the same logical service. `ConstructServiceReference` checks trajectory headers on the exchange and can replace the default service bean with an alternate one before invocation.

This is the mechanism used for:

- route deflection
- alternate implementations
- mocks selected by runtime headers

For a detailed walkthrough, see `docs/trajectories.md`.

### Interpolation commands

The interceptor XML does not list every concrete interceptor up front. Instead, some XML entries are interpolation commands that resolve to real command lists at runtime.

Examples:

- `preProcessorsInterpolation` resolves deployable-wide pre-processors from `ChenileConfiguration`
- `postProcessorsInterpolation` resolves deployable-wide post-processors
- `operationSpecificProcessorsInterpolation` resolves interceptors defined on the selected operation
- `serviceSpecificProcessorsInterpolation` resolves interceptors defined on the selected service

That lets Chenile keep one stable XML skeleton while still varying behavior by service and operation metadata.

## Modules

### `chenile-base`

Common exceptions and generic response model.

### `chenile-core`

Service registry, initializers, exchange model, interceptors, transformation, and event handling.

### `chenile-http`

HTTP binding, controller annotations, URL mapping, and request/response adaptation.

### `owiz`

The generic orchestration and command-chain engine used internally by Chenile.

### `stm`

A separate state machine library. The main abstraction is `stm/src/main/java/org/chenile/stm/STM.java`, which supports auto and manual states driven by configured actions and flow definitions.

For a focused walkthrough of the STM subsystem, see `docs/stm.md`.

### `utils`

Supporting helpers such as region-to-trajectory conversion and streaming utilities.

### `jpa-utils`

JPA entity base classes and related persistence helpers.

### `multi-datasource-utils`

Tenant-aware datasource routing. `multi-datasource-utils/src/main/java/org/chenile/configuration/multids/MultiTenantDataSourceConfiguration.java` routes datasource selection using `ContextContainer.getTenant()`.

### `cucumber-utils`

Testing helpers for MockMvc and Cucumber.

## How to read the repository

Read these files in order:

1. `README.md`
2. `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`
3. `chenile-core/src/main/java/org/chenile/core/model/ChenileConfiguration.java`
4. `chenile-http/src/main/java/org/chenile/http/handler/HttpEntryPoint.java`
5. `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java`
6. `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`
7. `chenile-http/src/main/java/org/chenile/http/init/AnnotationChenileServiceInitializer.java`
8. `chenile-core/src/main/resources/org/chenile/core/chenile-core.xml`
9. `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/PreProcessorsInterpolation.java`
10. `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/ServiceSpecificProcessorsInterpolation.java`

Then use the tests as executable examples:

- `chenile-http/src/test/java/org/chenile/http/test/TestChenileHttp.java`
- `chenile-http/src/test/java/org/chenile/http/test/TestAnnotationController.java`
- `chenile-core/src/test/java/org/chenile/core/test/TestChenileCore.java`
