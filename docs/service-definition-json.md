# Chenile Service-Definition JSON

Chenile can register services from JSON files instead of relying only on controller annotations. This document explains:

- where JSON service definitions are loaded from
- how they map to `ChenileServiceDefinition` and `OperationDefinition`
- which fields matter most at runtime
- how they drive interceptor selection and method invocation

## Where the JSON is loaded from

Service-definition JSON is loaded by `chenile-core/src/main/java/org/chenile/core/init/ChenileServiceInitializer.java`.

That initializer is created in `chenile-core/src/main/java/org/chenile/configuration/core/ChenileCoreConfiguration.java`, which reads the property:

- `chenile.service.json.package`

The property usually contains one or more Spring resource patterns, for example:

```properties
chenile.service.json.package=classpath*:org/chenile/http/test/service/*.json
```

Each matching file is parsed into a `ChenileServiceDefinition` by `AbstractServiceInitializer`.

## What happens after parsing

The main logic is in `chenile-core/src/main/java/org/chenile/core/init/AbstractServiceInitializer.java`.

After a JSON file is deserialized:

1. module-level metadata is applied
2. service-level interceptor bean names are resolved to command instances
3. operation-level interceptor bean names are resolved
4. body-type selector beans are resolved
5. the service bean, mock bean, and health-checker bean are looked up from Spring
6. each operation is validated against the actual Java service implementation
7. the service is registered into `ChenileConfiguration`

The JSON is therefore not just static metadata. It is actively validated against the Spring application context and the target Java methods.

## Top-level service JSON structure

A service JSON file maps to `ChenileServiceDefinition`.

A simplified shape looks like this:

```json
{
  "name": "jsonService",
  "id": "jsonService",
  "mockName": "jsonServiceMock",
  "healthCheckerName": "jsonHealthChecker",
  "interceptorComponentNames": ["serviceInterceptor"],
  "operations": [
    {
      "name": "save",
      "url": "/system/property",
      "httpMethod": "POST",
      "produces": "JSON",
      "consumes": "JSON",
      "input": "org.example.JsonData",
      "params": [
        {
          "name": "jsonData",
          "type": "BODY"
        }
      ]
    }
  ]
}
```

## Important top-level fields

### `name`

The Spring bean name of the actual service implementation. During initialization, Chenile looks up this bean from the application context and stores it as the default service reference.

This field must match a bean that exists in Spring.

### `id`

The logical Chenile service id. This is the id used internally in the service registry and in trajectory overrides.

### `mockName`

Optional Spring bean name for the mock implementation. `ConstructServiceReference` may use this when mock invocation is enabled.

### `healthCheckerName`

Optional Spring bean name for a `HealthChecker`.

### `interceptorComponentNames`

Optional service-level interceptor bean names. These are resolved into real command instances and later returned by `serviceSpecificProcessorsInterpolation`.

### `operations`

The list of operation definitions for the service.

## Operation JSON structure

Each operation entry maps to `chenile-core/src/main/java/org/chenile/core/model/OperationDefinition.java`.

Important fields include:

- `name`
- `url`
- `httpMethod`
- `produces`
- `consumes`
- `input`
- `output`
- `interceptorComponentNames`
- `bodyTypeSelectorComponentNames`
- `eventSubscribedTo`
- `params`

## Important operation fields

### `name`

Logical operation name. This is also the method name by default unless overridden internally.

The initializer validates that the underlying service bean has a matching Java method.

### `url`

HTTP URL pattern. When `chenile-http` builds URL mappings, it uses this field to create `HttpEntryPoint` handlers.

Example:

```json
"url": "/system/property/{key}"
```

Path variables are extracted by `HttpEntryPoint` and placed into exchange headers.

### `httpMethod`

The HTTP verb for `chenile-http`.

Examples:

- `GET`
- `POST`
- `PUT`
- `PATCH`
- `DELETE`

### `produces` and `consumes`

The response and request mime types from Chenile’s enum model. These influence transformation and response handling.

Examples:

- `JSON`
- `TEXT`
- `HTML`
- `PDF`

### `input`

The Java class that the request body should be transformed into before the service is invoked.

Example:

```json
"input": "org.chenile.http.test.service.JsonData"
```

If a parameter is bound from `BODY` and no dynamic body-type selector is configured, `input` is required.

### `output`

The declared output class. This is used as metadata and documentation, and in some cases for response transformation behavior.

### `interceptorComponentNames`

Operation-specific interceptor bean names. These are resolved to command instances during initialization and later returned by `operationSpecificProcessorsInterpolation`.

### `bodyTypeSelectorComponentNames`

An optional list of bean names used to dynamically determine the effective body type at runtime.

This is used for operations where the input type depends on headers or other exchange metadata.

### `eventSubscribedTo`

Optional list of event ids that should trigger this operation when `EventProcessor` handles those events.

For the full runtime event-registration and dispatch story, see `docs/events.md`.

## Parameter definitions

Each `params` entry maps to `chenile-core/src/main/java/org/chenile/core/model/ParamDefinition.java`.

Examples:

```json
{
  "name": "jsonData",
  "type": "BODY",
  "description": "System property multi key value."
}
```

```json
{
  "name": "key",
  "type": "HEADER",
  "description": "System property key."
}
```

```json
{
  "name": "headers",
  "type": "HEADERS",
  "paramType": "java.util.Map<java.lang.String,java.lang.Object>",
  "paramClass": "java.util.Map",
  "description": "headers"
}
```

The important fields are:

- `name`
- `type`
- `paramType`
- `paramClass` (legacy/raw-class compatibility)
- `description`

### Binding types

`ServiceInvoker` interprets parameter binding types like this:

- `BODY` -> use `exchange.getBody()`
- `HEADER` -> use one named header from the exchange
- `HEADERS` -> use the whole header map
- `MULTI_PART` -> use one named multipart file

`paramType` is the public metadata field and the value external consumers should read through `ParamDefinition.getParamType()`. It can represent generic types such as `java.util.List<java.lang.String>` or `java.util.Map<java.lang.String,java.lang.Object>`.

`paramClass` remains supported only as a compatibility and internal raw-invocation field. External consumers should not use `ParamDefinition.getParamClass()` because it may intentionally differ from `paramType` when Chenile needs a different raw signature for method matching.

If both `paramType` and `paramClass` are omitted:

- body parameters default to the operation `input` type
- non-body parameters default to `String`

## Concrete examples from the repository

### Example 1: GET with a path variable

From `chenile-http/src/test/resources/org/chenile/http/test/service/service.json`:

```json
{
  "name": "getOne",
  "url": "/system/property/{key}",
  "httpMethod": "GET",
  "produces": "JSON",
  "consumes": "JSON",
  "interceptorComponentNames": ["jsonInterceptor"],
  "params": [
    {
      "name": "key",
      "type": "HEADER",
      "description": "System property key."
    }
  ]
}
```

What this means at runtime:

- `HttpEntryPoint` extracts `{key}` from the URL and stores it into exchange headers
- `ServiceInvoker` reads header `key`
- operation-specific interceptor `jsonInterceptor` is inserted by interpolation before the service call

### Example 2: POST with a typed request body

Also from `service.json`:

```json
{
  "name": "save",
  "url": "/system/property",
  "httpMethod": "POST",
  "produces": "JSON",
  "consumes": "JSON",
  "input": "org.chenile.http.test.service.JsonData",
  "interceptorComponentNames": ["jsonInterceptor"],
  "params": [
    {
      "name": "jsonData",
      "type": "BODY",
      "description": "System property multi key value."
    }
  ]
}
```

What this means at runtime:

- the raw request body is transformed into `JsonData`
- that object becomes `exchange.body`
- `ServiceInvoker` passes it as the Java argument for the method

### Example 3: dynamic body type

From `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`:

```json
{
  "name": "s6",
  "url": "/s6",
  "httpMethod": "POST",
  "bodyTypeSelectorComponentNames": ["s6BodyTypeSelector"],
  "params": [
    {
      "name": "eventId",
      "type": "HEADER"
    },
    {
      "name": "param",
      "type": "BODY",
      "paramType": "java.lang.Object",
      "paramClass": "java.lang.Object"
    }
  ]
}
```

What this means at runtime:

- the request body type is not fixed at configuration time
- `s6BodyTypeSelector` decides which type to use based on the exchange
- the body is transformed accordingly before invocation

### Example 4: whole-header-map binding

From `mockService.json`:

```json
{
  "name": "s7",
  "url": "/s7",
  "httpMethod": "POST",
  "params": [
    {
      "name": "headers",
      "type": "HEADERS",
      "paramType": "java.util.Map<java.lang.String,java.lang.Object>",
      "paramClass": "java.util.Map"
    }
  ]
}
```

This tells `ServiceInvoker` to pass the complete header map to the service method.

### Example 4a: generic body metadata

From `mockService.json`:

```json
{
  "name": "mockMethod",
  "input": "java.util.ArrayList",
  "params": [
    {
      "name": "list",
      "type": "BODY",
      "paramType": "java.util.List<java.lang.String>",
      "paramClass": "java.util.List"
    }
  ]
}
```

This preserves the generic contract for metadata consumers such as MCP schema generation, which should read `ParamDefinition.getParamType()`, while runtime method lookup still uses the internal raw class from `paramClass`.

### Example 5: event subscription

From `mockService.json`:

```json
{
  "name": "s8",
  "url": "/s8",
  "eventSubscribedTo": ["foo", "event1"],
  "input": "org.chenile.core.test.Foo",
  "output": "org.chenile.core.test.Foo",
  "params": [
    {
      "name": "foo",
      "type": "BODY",
      "paramType": "org.chenile.core.test.Foo",
      "paramClass": "org.chenile.core.test.Foo"
    }
  ]
}
```

This operation can be triggered by the event subsystem, not only by HTTP.

## Relationship to trajectories

Service-definition JSON can also participate in trajectory-based routing, because trajectory overrides target logical Chenile service ids.

Example trajectory JSON from the tests:

```json
{
  "id": "t1",
  "trajectoryOverrides": {
    "mockService": {
      "newServiceReferenceId": "t1MockService"
    }
  }
}
```

At runtime, if the request carries trajectory header `x-chenile-trajectory-id: t1`, `ConstructServiceReference` can replace the normal service bean for service id `mockService` with `t1MockService`.

For the full trajectory flow, see `docs/trajectories.md`.

## How JSON drives the interceptor chain

The JSON service definition directly affects the chain in three ways.

### Service-level interceptors

If the top-level service JSON contains `interceptorComponentNames`, those bean names are resolved during initialization and stored in `ChenileServiceDefinition`.

At runtime, `serviceSpecificProcessorsInterpolation` returns those commands.

### Operation-level interceptors

If an operation contains `interceptorComponentNames`, those become `OperationDefinition.interceptorCommands`.

At runtime, `operationSpecificProcessorsInterpolation` returns those commands.

### Body-type selectors

If an operation contains `bodyTypeSelectorComponentNames`, those selectors are resolved into a small command chain during initialization and used by transformation-related interceptors.

## Validation rules that matter

`AbstractServiceInitializer` performs important validation:

- the service bean named by `name` must exist in Spring
- `mockName`, if present, must resolve to a bean
- `healthCheckerName`, if present, must resolve to a `HealthChecker`
- each operation must map to a real Java method on the service bean
- body-bound parameters require an input type unless a body-type selector is configured

This means a malformed JSON file usually fails at startup, not only at request time.

## Relationship to annotation-based services

The annotation-based path in `chenile-http` and the JSON-based path in `chenile-core` both end up producing the same internal objects:

- `ChenileServiceDefinition`
- `OperationDefinition`

Once registration is complete, the interceptor chain and invocation pipeline do not care whether the metadata came from JSON or annotations.

For a detailed walkthrough of the annotation path and side-by-side JSON comparisons, see `docs/chenile-annotations.md`.

## JSON to annotation mental mapping

These concepts are direct equivalents:

- service JSON `id` -> `@ChenileController(value = "...")`
- service JSON `name` -> `@ChenileController(serviceName = "...")`
- operation JSON `url` + `httpMethod` -> `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`
- operation JSON `interceptorComponentNames` -> `@InterceptedBy`
- operation JSON `eventSubscribedTo` -> `@EventsSubscribedTo`
- operation JSON `bodyTypeSelectorComponentNames` -> `@BodyTypeSelector`
- operation JSON response codes -> `@ChenileResponseCodes`

The main practical difference is this:

- JSON path defines Chenile metadata explicitly in data files.
- annotation path derives most metadata from controller method signatures plus Chenile-specific annotations.

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/init/ChenileServiceInitializer.java`
- `chenile-core/src/main/java/org/chenile/core/init/AbstractServiceInitializer.java`
- `chenile-core/src/main/java/org/chenile/core/model/ChenileServiceDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/OperationDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/ParamDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`
- `chenile-http/src/test/resources/org/chenile/http/test/service/service.json`
- `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`
