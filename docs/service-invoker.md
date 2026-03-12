# ServiceInvoker And Domain Binding

`ServiceInvoker` is the final bridge between Chenile’s transport-neutral execution context and your application’s domain-oriented service methods.

This document explains:

- what `ServiceInvoker` does
- what it does not do
- how it works with the transformation subsystem
- how it supports domain-driven service interfaces
- how headers and body become domain-level method arguments

## The main idea

Chenile processes requests through a generic runtime object:

- `ChenileExchange`

That object is deliberately infrastructure-oriented. It contains:

- headers
- body
- multipart data
- service definition
- operation definition
- runtime response and exception state

Your application service methods, by contrast, usually should not accept that generic exchange object directly. In a domain-driven design style, you want methods like:

```java
public User register(User user)
```

or:

```java
public User updateEmail(String userId, User user)
```

or:

```java
public Order approve(String orderId, ApprovalCommand command)
```

`ServiceInvoker` is the component that makes this possible.

## What `ServiceInvoker` actually does

The class is:

- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`

Its responsibility is:

1. read the selected `OperationDefinition`
2. inspect that operation’s `ParamDefinition` list
3. pull values from `ChenileExchange`
4. assemble the Java argument list in the correct order
5. invoke the selected service method reflectively
6. store the return value back into the exchange

So `ServiceInvoker` is not just “call this bean method.” It is the final argument-binding step from framework context to application API.

## What `ServiceInvoker` does not do

`ServiceInvoker` does not usually parse raw JSON itself into a domain object.

That work normally happens earlier in the chain:

1. `TransformationClassSelector` decides the target body type
2. `Transformer` deserializes the raw request body string into that target type
3. `ServiceInvoker` reads the already-typed body and places it into the service method arguments

That distinction is important.

So the complete flow is:

- transport creates a generic `ChenileExchange`
- transformation turns the raw body into a domain object
- `ServiceInvoker` binds that domain object into the service method call

## Why this supports Domain Driven Design

Domain Driven Design pushes service interfaces toward domain concepts rather than transport or infrastructure concepts.

That means your core service layer should prefer:

- `User`
- `Order`
- `Payment`
- `ApprovalCommand`
- `CustomerId`

instead of:

- `HttpServletRequest`
- raw JSON strings
- generic maps
- framework-specific exchange objects

Chenile supports that style by separating:

- infrastructure concerns in `ChenileExchange`
- domain arguments in the actual service method signature

`ServiceInvoker` is the point where Chenile translates between those two worlds.

## How body becomes a domain object

Suppose a request body contains JSON for a user:

```json
{
  "id": "u1",
  "name": "Asha"
}
```

And your service method is:

```java
public User save(User user)
```

The flow is:

1. HTTP puts the raw JSON string into `exchange.body`
2. `TransformationClassSelector` sets the target type to `User`
3. `Transformer` converts the JSON string into a `User` object
4. `ServiceInvoker` sees a `BODY` parameter and adds `exchange.getBody()` to the invocation list
5. the service method receives a real `User` object

So from the domain service’s point of view, it never deals with raw HTTP payloads.

## How headers become domain-oriented method parameters

`ServiceInvoker` also supports parameters bound from headers.

If an operation declares a parameter of type `HEADER`, `ServiceInvoker` reads that named value from the exchange headers and converts it to the expected Java type.

That lets you write service methods like:

```java
public User updateEmail(String userId, User user)
```

where:

- `userId` comes from a header or extracted path variable
- `user` comes from the request body

The domain method remains clean and explicit:

- identity comes from a simple argument
- aggregate data comes from a typed domain object

## Binding rules in `ServiceInvoker`

`ServiceInvoker` reads each `ParamDefinition` and binds according to its `HttpBindingType`.

### `BODY`

For `BODY`, it adds:

- `exchange.getBody()`

By the time this happens, the body is often already a typed domain object because of the transformation subsystem.

### `HEADER`

For `HEADER`, it adds a single named header value:

- `exchange.getHeader(paramName)`

It also performs simple type conversion for primitives and wrapper types such as:

- `boolean`
- `int`
- `long`
- `double`

### `HEADERS`

For `HEADERS`, it adds the complete headers map.

This is less domain-pure than `BODY` plus explicit header params, but it is available when needed.

### `MULTI_PART`

For `MULTI_PART`, it extracts the named multipart file from the exchange.

## DDD-friendly method shapes

The Chenile model encourages method signatures like these:

```java
public User register(User user)
```

```java
public User update(String userId, User user)
```

```java
public Payment authorize(String tenantId, PaymentCommand command)
```

```java
public InventoryItem uploadImage(String itemId, MultipartFile file)
```

In all of these, the domain service stays focused on business concepts.

The framework handles:

- path/header extraction
- payload deserialization
- multipart extraction
- argument ordering
- reflective invocation

## Example: user creation

Imagine this logical Chenile operation:

- `userId` from header or path variable
- `User` from body

The effective binding looks like:

```java
public User createUser(String tenantId, User user)
```

Chenile can satisfy that as follows:

- header `tenantId` is copied into `ChenileExchange.headers`
- body JSON is transformed into `User`
- `ServiceInvoker` assembles the argument list as `[tenantId, user]`
- reflection invokes the domain service with those real objects

This is exactly the kind of separation DDD wants:

- infrastructure input is normalized early
- the domain service gets meaningful inputs

## Example from the repository

A concrete version of this pattern exists throughout the repo.

From the JSON-based test service model:

- body parameters are declared with `type = BODY`
- header parameters are declared with `type = HEADER`

From the annotation-based path:

- `@RequestBody` becomes a Chenile `BODY` parameter
- non-body controller params become header-bound params in the generated `OperationDefinition`

At runtime, `ServiceInvoker` treats both the same because both end up as `ParamDefinition` metadata.

## Why `ChenileExchange` is still useful

DDD does not mean `ChenileExchange` is bad. It means `ChenileExchange` should stay in the application-service boundary layer rather than leaking into the core domain model by default.

`ChenileExchange` is useful for:

- interceptors
- framework plumbing
- transport-neutral metadata
- error and response handling
- advanced custom behavior

But the default service style Chenile enables is:

- domain objects in
- domain objects out

That is where `ServiceInvoker` adds architectural value.

## Subtle but important point

If you say “`ServiceInvoker` converts `ChenileExchange` into a domain object,” the precise version is:

- the transformation subsystem converts raw exchange body data into a domain object
- `ServiceInvoker` converts the full `ChenileExchange` into the final Java method argument list, which may include that domain object plus header-derived values

That distinction matches the actual code and makes the design easier to reason about.

## End-to-end mental model

Use this model:

1. `ChenileExchange` is the infrastructure envelope
2. transformation turns raw body data into domain objects
3. `ServiceInvoker` binds exchange data into domain-style method arguments
4. the service method executes in business terms, not transport terms

That is how Chenile helps preserve domain-oriented service interfaces while still using a generic execution pipeline underneath.

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/interceptors/ServiceInvoker.java`
- `chenile-core/src/main/java/org/chenile/core/context/ChenileExchange.java`
- `chenile-core/src/main/java/org/chenile/core/model/OperationDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/ParamDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/transform/TransformationClassSelector.java`
- `chenile-core/src/main/java/org/chenile/core/transform/Transformer.java`
- `docs/exchange-lifecycle.md`
- `docs/transformation.md`
