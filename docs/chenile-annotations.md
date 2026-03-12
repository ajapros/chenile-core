# Chenile Annotations

Chenile supports an annotation-based registration path that produces the same internal runtime model as the service-definition JSON path.

This document explains:

- which annotations matter
- how annotated controllers are turned into `ChenileServiceDefinition` and `OperationDefinition`
- how that matches the JSON registration path
- when annotations are more convenient than JSON

## The core idea

The JSON path and the annotation path are two ways of producing the same internal metadata objects:

- `ChenileServiceDefinition`
- `OperationDefinition`

Once those objects are created and registered, the interceptor chain and invocation pipeline do not care where they came from.

That is the key equivalence to understand.

## The annotation registration path

The main registration class is `chenile-http/src/main/java/org/chenile/http/init/AnnotationChenileServiceInitializer.java`.

At application startup it:

1. scans the Spring context for beans annotated with `@ChenileController`
2. creates a `ChenileServiceDefinition` for each controller
3. scans the controller methods for Spring mapping annotations like `@GetMapping` and `@PostMapping`
4. uses mapping producers to convert those methods into `OperationDefinition` objects
5. resolves service bean, mock bean, and health checker bean references
6. registers the service into `ChenileConfiguration`

This is the annotation equivalent of `ChenileServiceInitializer` loading a service JSON file.

## Main annotations

### `@ChenileController`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/ChenileController.java`.

Example:

```java
@RestController
@ChenileController(value = "jsonController", serviceName = "jsonService")
public class JsonController extends ControllerSupport {
}
```

Important fields:

- `value`
- `serviceName`
- `healthCheckerName`
- `mockName`
- `interfaceClass`

What they mean:

- `value` is the Chenile service id
- `serviceName` is the Spring bean name of the actual service implementation
- `healthCheckerName` and `mockName` match the equivalent JSON fields

Equivalent JSON:

```json
{
  "id": "jsonController",
  "name": "jsonService"
}
```

One practical note: in annotation-based setups the controller bean and the service bean are different things. The controller is used to derive metadata and receive HTTP traffic, while the service bean is the actual target invoked by `ServiceInvoker`.

### Spring mapping annotations

The annotation path uses ordinary Spring MVC annotations:

- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@PatchMapping`
- `@DeleteMapping`

These are converted into Chenile operation metadata by the mapping producers in `chenile-http/src/main/java/org/chenile/http/init/od`.

For example:

```java
@PostMapping("/c/save")
public ResponseEntity<GenericResponse<JsonData>> save(
        HttpServletRequest request, @RequestBody JsonData jsonData) {
    return process("save", request, jsonData);
}
```

becomes an `OperationDefinition` roughly equivalent to:

```json
{
  "name": "save",
  "url": "/c/save",
  "httpMethod": "POST",
  "input": "org.chenile.http.test.service.JsonData",
  "params": [
    {
      "name": "jsonData",
      "type": "BODY"
    }
  ]
}
```

### `@InterceptedBy`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/InterceptedBy.java`.

Example:

```java
@PostMapping("/c/ping")
@InterceptedBy({"jsonInterceptor", "jsonInterceptor1"})
public ResponseEntity<GenericResponse<JsonData>> ping(
        HttpServletRequest request, @RequestBody JsonData jsonData) {
    return process("ping", request, jsonData);
}
```

Equivalent JSON:

```json
"interceptorComponentNames": ["jsonInterceptor", "jsonInterceptor1"]
```

At registration time, `MappingProducerBase.processInterceptedBy(...)` resolves those bean names to interceptor commands and stores them on the `OperationDefinition`.

That means `operationSpecificProcessorsInterpolation` sees the same data whether it came from JSON or annotations.

### `@EventsSubscribedTo`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/EventsSubscribedTo.java`.

Example:

```java
@PostMapping("/c/save")
@EventsSubscribedTo({"event1", "event2"})
public ResponseEntity<GenericResponse<JsonData>> save(
        HttpServletRequest request, @RequestBody JsonData jsonData) {
    return process("save", request, jsonData);
}
```

Equivalent JSON:

```json
"eventSubscribedTo": ["event1", "event2"]
```

This ends up on the same `OperationDefinition.eventSubscribedTo` field used by the event subsystem.

For the full runtime event-registration and dispatch story, see `docs/events.md`.

### `@BodyTypeSelector`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/BodyTypeSelector.java`.

Example from the tests:

```java
@PostMapping("/add-capacity")
@BodyTypeSelector("subclassBodyTypeSelector")
public ResponseEntity<GenericResponse<Capacity>> addCapacity(
        HttpServletRequest request,
        @ChenileParamType(Vehicle.class) @RequestBody String vehicle) {
    return process(request, vehicle);
}
```

Equivalent JSON:

```json
"bodyTypeSelectorComponentNames": ["subclassBodyTypeSelector"]
```

This is the annotation equivalent of saying: the request body type is not fixed up front and must be selected dynamically.

### `@ChenileParamType`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/ChenileParamType.java`.

This annotation is used when the controller method signature is more specific or more transport-oriented than the actual service signature.

Example:

```java
@ChenileParamType(Vehicle.class) @RequestBody String vehicle
```

This tells Chenile:

- the controller receives a `String`
- but the underlying service contract should be treated as `Vehicle`

There is no exact one-field JSON equivalent because JSON already describes the service contract directly through `input` and `paramClass`. `@ChenileParamType` exists mainly to bridge the gap between controller signatures and service signatures.

### `@ChenileResponseCodes`

Defined in `chenile-http/src/main/java/org/chenile/http/annotation/ChenileResponseCodes.java`.

Example:

```java
@ChenileResponseCodes(success = 201, warning = 200)
```

This populates the same `OperationDefinition.successHttpStatus` and `warningHttpStatus` fields that a JSON-based registration path would need to set in metadata.

### `@ChenileAnnotation`

Defined in `chenile-core/src/main/java/org/chenile/core/annotation/ChenileAnnotation.java`.

This is a meta-annotation. Any annotation marked with `@ChenileAnnotation` is automatically stored as extension metadata on the service or operation definition.

That lets Chenile or downstream modules attach policy metadata without changing the core model.

`@ChenileResponseCodes` is one example.

## How controller methods become `OperationDefinition`

The conversion happens in `chenile-http/src/main/java/org/chenile/http/init/od/MappingProducerBase.java`.

For each mapped controller method, it derives:

- operation name from the Java method name
- URL from the Spring mapping annotation
- HTTP method from the producer subclass
- consumes and produces from the Spring annotation
- output type from the method return type
- parameter bindings from the method arguments
- Chenile-specific metadata from annotations such as `@InterceptedBy`, `@EventsSubscribedTo`, and `@BodyTypeSelector`

The parameter binding rules are especially important:

- first parameter must be `HttpServletRequest`
- parameters with `@RequestBody` become Chenile `BODY` params
- other controller parameters default to Chenile `HEADER` params

That is how a controller method signature becomes the same kind of param metadata that JSON would have described explicitly.

## Side-by-side example

### JSON version

From `chenile-http/src/test/resources/org/chenile/http/test/service/service.json`:

```json
{
  "name": "jsonService",
  "id": "jsonService",
  "operations": [
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
          "type": "BODY"
        }
      ]
    }
  ]
}
```

### Annotation version

From `chenile-http/src/test/java/org/chenile/http/test/controller/JsonController.java`:

```java
@RestController
@ChenileController(value = "jsonController", serviceName = "jsonService")
public class JsonController extends ControllerSupport {

    @PostMapping("/c/save")
    @InterceptedBy("jsonInterceptor")
    @EventsSubscribedTo({"event1", "event2"})
    public ResponseEntity<GenericResponse<JsonData>> save(
            HttpServletRequest request, @RequestBody JsonData jsonData) {
        return process("save", request, jsonData);
    }
}
```

### Same result internally

These two paths differ in how metadata is expressed, but they converge on the same internal ideas:

- logical service metadata
- operation URL and HTTP method
- typed body input
- interceptor list
- event subscriptions
- parameter binding contract

After registration, both produce `OperationDefinition` and `ChenileServiceDefinition` objects that drive:

- URL mapping
- interceptor interpolation
- body transformation
- service invocation
- event subscription wiring

## Another side-by-side example: body type selection

### JSON version

```json
{
  "name": "s6",
  "bodyTypeSelectorComponentNames": ["s6BodyTypeSelector"],
  "params": [
    {
      "name": "param",
      "type": "BODY",
      "paramClass": "java.lang.Object"
    }
  ]
}
```

### Annotation version

From `CapacityController`:

```java
@PostMapping("/add-capacity")
@BodyTypeSelector("subclassBodyTypeSelector")
public ResponseEntity<GenericResponse<Capacity>> addCapacity(
        HttpServletRequest request,
        @ChenileParamType(Vehicle.class) @RequestBody String vehicle) {
    return process(request, vehicle);
}
```

Both tell Chenile that:

- the body type is dynamic
- a selector bean determines the effective type before transformation
- invocation should use the service contract type rather than the raw controller transport type

## Why annotations can achieve the same result

They achieve the same result because the annotation path does not bypass Chenile internals. It only changes how metadata is authored.

The annotation path still ends up filling the same fields on the same model objects:

- service id
- service bean name
- URL
- HTTP method
- input type
- output type
- parameter list
- interceptor list
- event subscriptions
- response codes

That is why the interceptor chain works identically for both paths.

## When to prefer annotations vs JSON

Annotations are usually better when:

- the team prefers code-local configuration
- controller and service evolution happen together
- Spring MVC annotations already define most of the needed metadata

JSON is usually better when:

- you want service metadata externalized
- you want to define services without dedicated controllers
- you want to reason about service contracts as configuration artifacts

In both cases, the runtime model is the same.

## Most relevant source files

- `chenile-http/src/main/java/org/chenile/http/annotation/ChenileController.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/InterceptedBy.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/EventsSubscribedTo.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/BodyTypeSelector.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/ChenileParamType.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/ChenileResponseCodes.java`
- `chenile-http/src/main/java/org/chenile/http/init/AnnotationChenileServiceInitializer.java`
- `chenile-http/src/main/java/org/chenile/http/init/od/MappingProducerBase.java`
- `chenile-http/src/test/java/org/chenile/http/test/controller/JsonController.java`
- `chenile-http/src/test/java/org/chenile/http/test/controller/CapacityController.java`
- `chenile-http/src/test/resources/org/chenile/http/test/service/service.json`
