# Chenile Exchange Lifecycle

`ChenileExchange` is the central runtime object in Chenile. Nearly every important piece of framework behavior is expressed by reading from it or writing to it.

This document explains:

- what `ChenileExchange` holds
- how it is created
- how its fields change across the request lifecycle
- how those fields are used by the interceptor chain

## What `ChenileExchange` is

The class lives in `chenile-core/src/main/java/org/chenile/core/context/ChenileExchange.java`.

It is a bidirectional execution context shared by:

- HTTP entry points
- event processors
- interceptors
- transformers
- the final service invocation

It starts as an incoming request context and ends as an outgoing response context.

## Important fields

The most important `ChenileExchange` fields are:

- `headers`
- `multiPartMap`
- `body`
- `bodyType`
- `serviceDefinition`
- `operationDefinition`
- `apiInvocation`
- `serviceReference`
- `method`
- `response`
- `exception`
- `httpResponseStatusCode`
- `responseMessages`
- `locale`

You can think of them in four groups.

### Incoming request metadata

- `headers`
- `multiPartMap`
- `locale`
- `body`

These are usually populated by a transport adapter such as `HttpEntryPoint`.

### Routing and contract metadata

- `serviceDefinition`
- `operationDefinition`
- `bodyType`

These tell Chenile which logical service and operation is being invoked and how to interpret the input.

### Invocation metadata

- `serviceReference`
- `method`
- `apiInvocation`

These are the fields needed to perform the actual reflective method call.

### Outgoing response state

- `response`
- `exception`
- `httpResponseStatusCode`
- `responseMessages`

These are populated as the chain unwinds after invocation.

## How an exchange is created

There are two common creation paths.

### HTTP path

`chenile-http/src/main/java/org/chenile/http/handler/HttpEntryPoint.java` creates a fresh `ChenileExchange` per request and sets:

- service definition
- operation definition
- headers
- multipart data
- locale
- raw request body

Then it sends the exchange to `ChenileEntryPoint`.

### Programmatic path

`chenile-core/src/main/java/org/chenile/core/context/ChenileExchangeBuilder.java` creates exchanges when code already knows the service name and operation name.

That builder:

- looks up the `ChenileServiceDefinition`
- finds the matching `OperationDefinition`
- creates the exchange
- optionally copies framework headers from a `HeaderCopier`

This is the path typically used by `ControllerSupport` and some internal framework integrations.

## Exchange lifecycle across one request

### Stage 1: transport initialization

At the start of request processing, the exchange usually contains:

- service definition
- operation definition
- incoming headers
- raw body as a string
- locale

At this point it does not yet contain:

- typed body
- resolved target bean
- reflective method
- response
- error

### Stage 2: header validation and context propagation

Early in the interceptor chain, `ValidateCopyHeaders` reads incoming headers and copies framework headers into `ContextContainer`.

Effects on the exchange:

- ensures request id exists
- may add generated request id header
- makes framework metadata available to downstream logic through `ContextContainer`

### Stage 3: body-type selection and transformation

The transformation step uses:

- `serviceDefinition`
- `operationDefinition`
- selector commands
- headers and other exchange state

to determine `bodyType`, then converts the raw request body into the Java object expected by the service.

After this stage:

- `body` usually becomes a typed Java object instead of a raw string
- `bodyType` has been resolved

For the detailed transformation subsystem, see `docs/transformation.md`.

### Stage 4: service resolution

`ConstructServiceReference` decides which actual Spring bean should handle the request.

After this stage, the exchange contains:

- `serviceReference`
- `serviceReferenceId`
- `method`

This may differ from the default service if:

- the request is marked as mock
- the trajectory overrides the service

### Stage 5: method-argument construction

`ServiceInvoker` builds `apiInvocation` from the `OperationDefinition` parameters.

It uses `ParamDefinition` entries from `OperationDefinition` and maps them from:

- `body`
- individual headers
- all headers
- multipart files

After this stage:

- `apiInvocation` contains the actual Java argument list for reflection

### Stage 6: service invocation

`ServiceInvoker` reflectively calls the target method:

- target object comes from `serviceReference`
- method comes from `method`
- arguments come from `apiInvocation`

After success:

- `response` is set

After failure:

- `exception` is set

### Stage 7: response normalization

As the chain unwinds, `GenericResponseBuilder` standardizes the result.

After this stage:

- `response` usually becomes a `GenericResponse`
- `httpResponseStatusCode` is set
- warnings and response messages may be copied into the generic response envelope

### Stage 8: transport emission

Finally the transport adapter reads:

- `response`
- `exception`
- `httpResponseStatusCode`
- `responseMessages`

and converts that into protocol output such as an HTTP response.

## How parameter binding works

Operation parameter binding is driven by `chenile-core/src/main/java/org/chenile/core/model/ParamDefinition.java`.

Each parameter has:

- a name
- a binding type
- a Java class

`ServiceInvoker` interprets binding types like this:

- `BODY` -> use `exchange.getBody()`
- `HEADER` -> use one named header from `exchange.getHeaders()`
- `HEADERS` -> use the full headers map
- `MULTI_PART` -> use a named multipart file from `exchange.getMultiPartMap()`

That makes the `OperationDefinition` effectively the runtime binding contract between transport-level request data and the Java service method signature.

## Why the exchange is bidirectional

`ChenileExchange` is not just an input DTO. It supports both directions of the pipeline.

Forward path uses it for:

- request metadata
- routing metadata
- typed body
- invocation state

Return path uses it for:

- result
- exception
- warnings
- HTTP status

That design is why around-interceptors work cleanly in Chenile: they can inspect the same object before and after the downstream call.

## Mental model

When reading code, use this simplified model:

- `ChenileExchange` starts as a transport request.
- the chain enriches it until it contains enough information to invoke a Java method.
- the service call writes success or failure back into the same object.
- the chain then converts that final state into a transport response.

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/context/ChenileExchange.java`
- `chenile-core/src/main/java/org/chenile/core/context/ChenileExchangeBuilder.java`
- `chenile-http/src/main/java/org/chenile/http/handler/HttpEntryPoint.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ValidateCopyHeaders.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/GenericResponseBuilder.java`
- `chenile-core/src/main/java/org/chenile/core/model/OperationDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/ParamDefinition.java`
