# Chenile Interceptor Chain

This document explains:

- where the interceptor chain is defined
- how OWIZ turns the XML into executable commands
- how a request moves through the chain at runtime
- how interpolation commands expand dynamically

## Where the chain is defined

The concrete Chenile interceptor flow is in `chenile-core/src/main/resources/org/chenile/core/chenile-core.xml`.

It defines one default OWIZ flow, `chenile-highway`, whose first command is another command also named `chenile-highway`, and under that a nested `chenile-interceptor-chain` with the actual interceptor sequence.

```xml
<?xml version="1.0" encoding="UTF-8"?>

<flows>
  <flow id='chenile-highway' defaultFlow="true">
    <chenile-highway first="true">
      <chenile-interceptor-chain>
        <log-output/>
        <generic-response-builder/>
        <exception-handler-interpolation/>
        <validate-copy-headers/>
        <pre-processors-interpolation/>
        <transformation-class-selector/>
        <transformer/>
        <construct-service-reference/>
        <post-processors-interpolation/>
        <operation-specific-processors-interpolation/>
        <service-specific-processors-interpolation/>
        <service-invoker/>
      </chenile-interceptor-chain>
    </chenile-highway>
  </flow>
</flows>
```

That file is loaded by `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`, where `chenileOrchExecutor()` creates an `XmlOrchConfigurator`, points it at `chenile.interceptors.path`, and supplies a Spring bean lookup adapter.

## How the XML becomes commands

OWIZ parses the XML in `owiz/src/main/java/org/chenile/owiz/config/impl/XmlOrchConfigurator.java`.

The important behavior is:

- XML tags become `CommandDescriptor` objects.
- Tag names are converted from kebab-case to camelCase.
- That camelCase name is used as the component name if none is explicitly supplied.
- OWIZ first looks up a Spring bean by that component name.
- If Spring does not contain it, OWIZ tries to instantiate a class by that name.

That means these XML tags map directly to Spring beans:

- `<log-output/>` -> `logOutput`
- `<generic-response-builder/>` -> `genericResponseBuilder`
- `<construct-service-reference/>` -> `constructServiceReference`
- `<service-invoker/>` -> `serviceInvoker`

Those beans are all created in `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`.

## Why the XML can be so short

Nested commands are automatically attached to their parent command. OWIZ infers the attachment from XML structure.

For example:

```xml
<chenile-highway first="true">
  <chenile-interceptor-chain>
    <log-output/>
    <generic-response-builder/>
  </chenile-interceptor-chain>
</chenile-highway>
```

is interpreted roughly as:

- `chenileHighway` is the first command in the flow
- `chenileInterceptorChain` is attached to `chenileHighway`
- `logOutput` is attached to `chenileInterceptorChain`
- `genericResponseBuilder` is attached to `chenileInterceptorChain`

The chain parent is an OWIZ `Chain`, so the attachment order becomes execution order.

## Fixed commands vs interpolation commands

Some entries in `chenile-core.xml` are regular commands. Some are interpolation commands.

Regular commands do work themselves:

- `validateCopyHeaders`
- `transformationClassSelector`
- `transformer`
- `constructServiceReference`
- `serviceInvoker`
- `genericResponseBuilder`
- `logOutput`

Interpolation commands are placeholders that expand to a list of commands at runtime for the current `ChenileExchange`.

- `exceptionHandlerInterpolation`
- `preProcessorsInterpolation`
- `postProcessorsInterpolation`
- `operationSpecificProcessorsInterpolation`
- `serviceSpecificProcessorsInterpolation`

OWIZ detects this in `owiz/src/main/java/org/chenile/owiz/impl/Chain.java`. If a command is an `InterpolationCommand`, the chain asks it for the actual commands to execute for the current exchange and inserts those commands into the runtime sequence.

This is how Chenile mixes:

- a fixed pipeline shape from XML
- dynamic per-module, per-service, and per-operation interceptors from metadata

## Where each interpolation command gets its list

The interpolation command classes are in `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations`.

### `ExceptionHandlerInterpolation`

Returns a one-element list containing the configured exception handler bean from `ChenileConfiguration`.

Source:

- `chenileConfiguration.getChenileExceptionHandler()`

### `PreProcessorsInterpolation`

Returns deployable-wide pre-processors configured in `chenile.properties` and resolved through Spring bean lookup.

Source:

- `chenileConfiguration.getPreProcessorCommands()`

### `PostProcessorsInterpolation`

Returns deployable-wide post-processors configured in `chenile.properties`.

Source:

- `chenileConfiguration.getPostProcessorCommands()`

### `OperationSpecificProcessorsInterpolation`

Returns the interceptor list attached directly to the selected `OperationDefinition`.

Source:

- `exchange.getOperationDefinition().getInterceptorCommands()`

### `ServiceSpecificProcessorsInterpolation`

Returns the interceptor list attached directly to the selected `ChenileServiceDefinition`.

Source:

- `exchange.getServiceDefinition().getInterceptorCommands()`

This is the main reason the framework can keep one common XML skeleton while allowing each service and operation to contribute custom behavior.

Those service and operation interceptor lists typically originate in service-definition JSON or annotation-based registration. For the JSON format, see `docs/service-definition-json.md`.
For the detailed transformation flow behind `transformationClassSelector`, `transformer`, `BodyTypeSelector`, and subclass selection, see `docs/transformation.md`.

## Runtime walkthrough for one HTTP request

An HTTP request enters at `chenile-http/src/main/java/org/chenile/http/handler/HttpEntryPoint.java`.

`HttpEntryPoint` builds a `ChenileExchange` by setting:

- service definition
- operation definition
- headers
- multipart files
- locale
- raw request body

It then calls `chenileEntryPoint.execute(exchange)`.

`chenile-core/src/main/java/org/chenile/core/entrypoint/ChenileEntryPoint.java` hands that exchange to the OWIZ executor, which starts the `chenile-highway` flow.

### Forward path

The request moves through the chain in this order:

1. `logOutput`
2. `genericResponseBuilder`
3. `exceptionHandlerInterpolation`
4. `validateCopyHeaders`
5. `preProcessorsInterpolation`
6. `transformationClassSelector`
7. `transformer`
8. `constructServiceReference`
9. `postProcessorsInterpolation`
10. `operationSpecificProcessorsInterpolation`
11. `serviceSpecificProcessorsInterpolation`
12. `serviceInvoker`

What each stage is doing:

1. `logOutput` enters and delegates.
2. `genericResponseBuilder` enters and delegates.
3. `exceptionHandlerInterpolation` expands into configured exception-handling commands.
4. `validateCopyHeaders` validates protected headers and copies allowed values into the context container.
5. `preProcessorsInterpolation` inserts deployable-wide pre-processors from `ChenileConfiguration`.
6. `transformationClassSelector` determines the Java body type.
7. `transformer` converts the request body into that type.
8. `constructServiceReference` chooses the target Spring bean and method, considering mock mode and trajectory overrides.
9. `postProcessorsInterpolation` inserts deployable-wide post-processors.
10. `operationSpecificProcessorsInterpolation` inserts interceptors configured on the selected operation.
11. `serviceSpecificProcessorsInterpolation` inserts interceptors configured on the service.
12. `serviceInvoker` constructs the Java argument list and invokes the method reflectively.

## Around-interceptor behavior

Most Chenile interceptors extend `chenile-core/src/main/java/org/chenile/core/interceptors/BaseChenileInterceptor.java`.

That base class implements a standard pattern:

1. run `doPreProcessing(exchange)`
2. continue the rest of the chain
3. capture downstream exceptions into `exchange`
4. run `doPostProcessing(exchange)` in `finally`

That means many interceptors are not simple one-way filters. They behave like wrappers around the downstream chain.

This matters when reading `chenile-core.xml`:

- an interceptor near the top can still do important work after the service returns
- `genericResponseBuilder` is intentionally near the top because it wraps almost the whole flow
- `logOutput` can log the final response state even though it appears before the service invocation

## Why the response builder appears near the top

Several Chenile interceptors are around-interceptors. They execute logic before delegating, then execute more logic when control returns.

So the XML order is not a one-way pipeline. It is a nested call stack.

That is why `generic-response-builder` is near the top of the XML. It needs to wrap almost everything beneath it so it can convert the final success or failure into Chenile’s generic response format.

## Return path

After `serviceInvoker` returns or throws, control unwinds back up the stack in reverse order.

The return path is conceptually:

1. service-specific interceptors finish
2. operation-specific interceptors finish
3. global post-processors finish
4. `constructServiceReference` completes
5. `transformer` completes
6. global pre-processors complete
7. exception-handling wrapper sees any failure and normalizes it
8. `genericResponseBuilder` wraps success or error into the standard response and sets HTTP status
9. `logOutput` logs final result state

Finally control returns to `HttpEntryPoint`, which reads `exchange.getResponse()`, `exchange.getException()`, and `exchange.getHttpResponseStatusCode()` and writes the HTTP response.

## Header and context handling

One early interceptor in the chain deserves special attention: `validateCopyHeaders`.

`chenile-core/src/main/java/org/chenile/core/interceptors/ValidateCopyHeaders.java` does two things:

- ensures a request id exists
- copies all external `x-...` headers into `ContextContainer`

It also blocks incoming `x-p-...` headers. Those are treated as protected internal headers, and attempts to pass them from outside result in an error.

This is how Chenile propagates internal metadata across the rest of the pipeline while preserving a distinction between user-supplied headers and framework-internal headers.

The related helper `chenile-core/src/main/java/org/chenile/core/context/PopulateContextContainer.java` can copy values between `ChenileExchange` and `ContextContainer` when an exchange is built programmatically.

## Practical mental model

Use this model when reading Chenile:

- `ChenileExchange` is the shared mutable request/response context.
- `chenile-core.xml` defines the skeleton of execution.
- interpolation commands inject dynamic interceptors based on the current service and operation metadata.
- `constructServiceReference` picks the target bean.
- `serviceInvoker` performs the actual method call.
- `genericResponseBuilder` standardizes the output on the way back.
- `ContextContainer` is where framework-level request metadata is propagated after header validation.

## Most relevant source files

- `chenile-core/src/main/resources/org/chenile/core/chenile-core.xml`
- `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`
- `chenile-core/src/main/java/org/chenile/core/entrypoint/ChenileEntryPoint.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/BaseChenileInterceptor.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/GenericResponseBuilder.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ValidateCopyHeaders.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/PreProcessorsInterpolation.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/PostProcessorsInterpolation.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/OperationSpecificProcessorsInterpolation.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/interpolations/ServiceSpecificProcessorsInterpolation.java`
- `owiz/src/main/java/org/chenile/owiz/config/impl/XmlOrchConfigurator.java`
- `owiz/src/main/java/org/chenile/owiz/config/impl/HandleCommand.java`
- `owiz/src/main/java/org/chenile/owiz/config/impl/HandleAttachment.java`
- `owiz/src/main/java/org/chenile/owiz/impl/Chain.java`
