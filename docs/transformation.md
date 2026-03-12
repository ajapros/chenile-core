# Chenile Transformation

Chenile’s transformation subsystem is responsible for turning an incoming raw request body, usually a JSON string, into the Java object that the target service method expects.

This document explains:

- where transformation sits in the interceptor chain
- how body type is chosen
- how JSON is converted into typed Java objects
- how subclass selection works
- how annotation and JSON-based body-type selectors feed the same runtime behavior

## Where transformation happens

In the interceptor chain defined in `chenile-core/src/main/resources/org/chenile/core/chenile-core.xml`, transformation happens in two consecutive steps:

1. `transformation-class-selector`
2. `transformer`

That ordering matters:

- first Chenile decides what Java type the body should become
- then it performs the actual JSON-to-object conversion

## The two main classes

The core classes are:

- `chenile-core/src/main/java/org/chenile/core/transform/TransformationClassSelector.java`
- `chenile-core/src/main/java/org/chenile/core/transform/Transformer.java`

`TransformationClassSelector` determines `exchange.bodyType`.

`Transformer` reads:

- `exchange.body`
- `exchange.bodyType`

and replaces the raw body string with a typed Java object.

## Why type selection is separate from transformation

Chenile separates these steps because the target type is not always fixed.

Sometimes the target type is obvious from the operation definition:

- operation input type is `JsonData`
- transform JSON into `JsonData`

Sometimes the target type depends on request context:

- a header determines the subtype
- a path variable determines whether the payload is a `Room` or a `Vehicle`
- the body itself contains a `type` discriminator

That is why Chenile first selects the target type and only then deserializes.

## Default type selection

If no body-type selector is configured, `TransformationClassSelector` uses:

- `OperationDefinition.input`

and converts that class into a `TypeReference`, then stores it into `exchange.bodyType`.

That is the default path for most ordinary typed request bodies.

## Custom body-type selectors

If a body-type selector exists, `TransformationClassSelector` executes it instead of defaulting directly to the operation input class.

The selector may come from:

- service-definition JSON via `bodyTypeSelectorComponentNames`
- annotations via `@BodyTypeSelector`

In both cases, the runtime result is the same:

- `OperationDefinition.bodyTypeSelector` contains a command
- `TransformationClassSelector` executes that command
- the command updates `exchange.bodyType`

## JSON path and annotation path converge here

### JSON path

An operation can declare:

```json
"bodyTypeSelectorComponentNames": ["s6BodyTypeSelector"]
```

During service initialization, `AbstractServiceInitializer` resolves these bean names into a command chain and stores it on the `OperationDefinition`.

### Annotation path

A controller method can declare:

```java
@BodyTypeSelector("subclassBodyTypeSelector")
```

During annotation-based registration, `MappingProducerBase.processBodyTypeSelector(...)` resolves the annotation values into the same kind of command chain and stores it on the `OperationDefinition`.

### Same result

After registration, both paths produce the same runtime structure:

- `OperationDefinition.bodyTypeSelector`
- `OperationDefinition.bodyTypeSelectorComponentNames`

So the transformation subsystem does not care whether the selector came from JSON or annotations.

## Actual deserialization

The deserialization step happens in `Transformer`.

It behaves like this:

1. read `exchange.bodyType`
2. read `exchange.body`
3. if either is missing, do nothing
4. if the body is not a `String`, do nothing
5. deserialize the JSON string into the target type using Jackson
6. replace `exchange.body` with the typed object

It also clears `exchange.apiInvocation` so method arguments can be recomputed after transformation.

If deserialization fails, `Transformer` throws a bad request exception.

## Subclass selection

The most interesting transformation feature in this repository is subclass support.

The main classes are:

- `chenile-core/src/main/java/org/chenile/core/transform/SubclassRegistry.java`
- `chenile-core/src/main/java/org/chenile/core/transform/SubclassBodyTypeSelector.java`

### `SubclassRegistry`

`SubclassRegistry` maintains mappings like:

- base class `Vehicle`
- discriminator value `car` -> subclass `Car`
- discriminator value `truck` -> subclass `Truck`

It expects the payload JSON to contain a top-level field named `type`.

### `SubclassBodyTypeSelector`

This selector:

1. starts from the operation input type or current body type
2. inspects the raw JSON body
3. asks `SubclassRegistry` whether a more specific subclass should be used
4. if found, updates `exchange.bodyType` to that subclass

This is how Chenile can accept a generic service contract like `Vehicle` but still instantiate `Car` or `Truck` before invocation.

## Concrete subclass example

The subclass example is in:

- `chenile-http/src/test/java/org/chenile/http/test/controller/CapacityController.java`
- `chenile-http/src/test/java/org/chenile/http/test/subclass/TestSubclassing.java`

The controller method:

```java
@PostMapping("/add-capacity")
@BodyTypeSelector("subclassBodyTypeSelector")
public ResponseEntity<GenericResponse<Capacity>> addCapacity(
        HttpServletRequest request,
        @ChenileParamType(Vehicle.class) @RequestBody String vehicle) {
    return process(request, vehicle);
}
```

The important pieces are:

- controller receives a raw JSON string
- `@ChenileParamType(Vehicle.class)` says the service contract should be treated as `Vehicle`
- `@BodyTypeSelector("subclassBodyTypeSelector")` says a selector should refine that type

If the JSON body contains `"type": "car"`, the selector upgrades the body type from `Vehicle` to `Car`.
If it contains `"type": "truck"`, it upgrades to `Truck`.

Then `Transformer` deserializes into the chosen subclass, and the service method receives the correct object type.

## Selector chaining

Chenile supports multiple body-type selectors in sequence.

Example from `CapacityController`:

```java
@PostMapping("/add-capacity-generic/{type}")
@BodyTypeSelector({"roomVehicleBodyTypeSelector","subclassBodyTypeSelector"})
public ResponseEntity<GenericResponse<Capacity>> addCapacityGeneric(
        HttpServletRequest request,
        @PathVariable("type") String type,
        @ChenileParamType(Object.class) @RequestBody String object) {
    return process(request, type, object);
}
```

This is a two-stage selection strategy:

1. `roomVehicleBodyTypeSelector` looks at header/path information and chooses a broad category like `Room` or `Vehicle`
2. `subclassBodyTypeSelector` refines `Vehicle` into `Car` or `Truck` using the payload’s `type` field

That allows Chenile to express complex, context-sensitive transformation logic in a composable way.

## Another custom selector example

The core tests also include a JSON-defined selector example:

- `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`
- `chenile-core/src/test/java/org/chenile/core/test/S6BodyTypeSelector.java`

That example shows an operation whose body type is determined by event or request context rather than fixed at configuration time.

## How transformation connects to service invocation

Transformation affects invocation directly because `ServiceInvoker` reads `exchange.body` when constructing method arguments.

The sequence is:

1. `TransformationClassSelector` sets `exchange.bodyType`
2. `Transformer` converts `exchange.body` from JSON string to typed object
3. `ServiceInvoker` reads `exchange.body`
4. the typed object is passed into the target Java method

So transformation is the bridge between transport-level input and method-level invocation.

## Mental model

Use this model:

- `OperationDefinition.input` is the default target type
- body-type selectors can override that target type dynamically
- `exchange.bodyType` is the effective deserialization target
- `Transformer` converts the raw body string into that type
- `ServiceInvoker` consumes the transformed object

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/transform/TransformationClassSelector.java`
- `chenile-core/src/main/java/org/chenile/core/transform/Transformer.java`
- `chenile-core/src/main/java/org/chenile/core/transform/SubclassRegistry.java`
- `chenile-core/src/main/java/org/chenile/core/transform/SubclassBodyTypeSelector.java`
- `chenile-http/src/main/java/org/chenile/http/annotation/BodyTypeSelector.java`
- `chenile-http/src/main/java/org/chenile/http/init/od/MappingProducerBase.java`
- `chenile-core/src/main/java/org/chenile/core/init/AbstractServiceInitializer.java`
- `chenile-http/src/test/java/org/chenile/http/test/controller/CapacityController.java`
- `chenile-http/src/test/java/org/chenile/http/test/subclass/TestSubclassing.java`
- `chenile-core/src/test/resources/org/chenile/core/test/service/mockService.json`
