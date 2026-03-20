# Chenile MCP

This document explains how the `chenile-mcp` module exposes Chenile services as Spring AI MCP server-side tools.

## What the module does

`chenile-mcp` scans the already-registered Chenile service metadata in `ChenileConfiguration`.

For every service or operation annotated with `@ChenileMCP`, it creates a Spring AI `ToolCallback`.

Those callbacks are then picked up by Spring AI MCP server autoconfiguration and converted into MCP server tool specifications.

The important point is that Chenile does not register tools by re-scanning raw controller methods on its own. It uses Chenile’s own runtime model:

- `ChenileServiceDefinition`
- `OperationDefinition`
- `ParamDefinition`
- the resolved service reference bean
- the resolved Java `Method`

That means MCP registration is aligned with the same metadata Chenile uses for normal request dispatch.

## Main pieces

### `@ChenileMCP`

[`ChenileMCP.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/model/ChenileMCP.java)

This annotation can be placed at:

- service/controller level
- operation/method level

It currently has only two fields:

- `name`
- `description`

If a service has `@ChenileMCP`, all of its operations are eligible for MCP exposure.

If only a specific operation has `@ChenileMCP`, only that operation is exposed.

## Registration flow

The main registration logic is in:

[`ChenileMCPInitializer.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/init/ChenileMCPInitializer.java)

At application startup it:

1. iterates through `chenileConfiguration.getServices()`
2. checks each `ChenileServiceDefinition` for service-level `@ChenileMCP`
3. checks each `OperationDefinition` for operation-level `@ChenileMCP`
4. resolves the actual target method from `OperationDefinition.getMethod()` or recomputes it from the service reference if needed
5. builds a Spring AI `ToolCallback`
6. stores those callbacks in the initializer, which itself implements `ToolCallbackProvider`

Because the initializer is a Spring bean implementing `ToolCallbackProvider`, Spring AI MCP server autoconfiguration can consume those callbacks directly.

## How a Chenile operation becomes an MCP tool

For a non-polymorphic method, one Chenile operation becomes one MCP tool.

The tool name comes from:

- `@ChenileMCP.name()` if present
- otherwise `serviceId_operationName`

The description comes from:

- `@ChenileMCP.description()`
- otherwise `OperationDefinition.description`
- otherwise a generated fallback based on service id and operation name

The tool input schema is derived from Chenile parameter metadata:

- `ParamDefinition.name`
- `ParamDefinition.type`
- `ParamDefinition.getParamType()`
- `OperationDefinition.input`

External consumers should always use `ParamDefinition.getParamType()`. `getParamClass()` is an internal raw-signature detail used by Chenile for method matching and invocation compatibility.

The actual invocation uses the service reference bean plus the resolved Java method.

## `ChenileToolCallback`

[`ChenileToolCallback.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/model/ChenileToolCallback.java)

This is the adapter between Chenile metadata and Spring AI tools.

It is responsible for:

- exposing a Spring AI `ToolDefinition`
- generating JSON schema for tool input
- converting incoming MCP JSON into Java arguments
- injecting fixed values for polymorphic registrations
- invoking the target service method reflectively
- converting the method result back into the string form expected by Spring AI tool callbacks

This keeps MCP registration aligned with Chenile’s own invocation model instead of creating a separate execution path.

## `@ChenilePolymorph`

[`ChenilePolymorph.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/model/ChenilePolymorph.java)

This is a method-level annotation.

It marks an MCP-enabled Chenile operation as polymorphic.

A polymorphic method is one where the exposed MCP tool shape depends on a combination of parameter values and parameter types.

For example:

```java
public String foo(String id, String eventId, Object eventPayload)
```

At runtime, `eventPayload` may be one of multiple concrete classes depending on `eventId`.

For MCP exposure, that means one Java method may need multiple tool registrations.

Example:

- variant 1: `eventId = "e1"`, `eventPayload = E1Payload`
- variant 2: `eventId = "e2"`, `eventPayload = E2Payload`

So the single Chenile operation becomes two MCP tools.

## Polymorph provider contract

Polymorphic expansion is driven by:

- [`ChenilePolymorphProvider.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/model/ChenilePolymorphProvider.java)
- [`ChenilePolymorphVariant.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/main/java/org/chenile/mcp/model/ChenilePolymorphVariant.java)

`@ChenilePolymorph("beanName")` points to a Spring bean implementing `ChenilePolymorphProvider`.

That provider returns a list of `ChenilePolymorphVariant` objects.

Each variant can specify:

- `nameSuffix`
- `description`
- `parameterTypes`
- `fixedParameterValues`

### `parameterTypes`

This map overrides the Java type used for a named parameter in the MCP tool schema and invocation.

It now uses `TypeReference<?>` rather than raw `Class<?>`, so variant registrations can describe generic types as well as simple classes.

Example:

```java
Map.of("eventPayload", new TypeReference<E1Payload>() {})
```

### `fixedParameterValues`

This map fixes specific parameters to a constant value for that variant.

Example:

```java
Map.of("eventId", "e1")
```

A fixed parameter is not exposed as user input in the generated MCP schema.

That is how one method can appear as multiple specialized tools.

## Example

The test module includes a concrete example:

- [`TestMcpController.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/test/java/org/chenile/mcp/test/TestMcpController.java)
- [`TestMcpService.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/test/java/org/chenile/mcp/test/TestMcpService.java)
- [`TestMcpTestConfig.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/test/java/org/chenile/mcp/test/TestMcpTestConfig.java)

The controller exposes:

- one normal MCP tool: `simpleTool`
- one polymorphic MCP operation: `fooTool`

The polymorph provider expands `fooTool` into:

- `fooTool_e1`
- `fooTool_e2`

with:

- fixed `eventId` values
- different `eventPayload` classes

## Test coverage

The integration test is:

[`TestChenileMcp.java`](/Users/rajashankarkolluru/Documents/framework/chenile-core/chenile-mcp/src/test/java/org/chenile/mcp/test/TestChenileMcp.java)

It verifies:

- normal `@ChenileMCP` registration
- `@ChenilePolymorph` expansion into multiple tools
- actual callback invocation
- correct deserialization into variant-specific payload classes

## Mental model

The shortest way to think about `chenile-mcp` is:

1. Chenile registers services and operations as usual
2. `chenile-mcp` reads those `ChenileServiceDefinition` and `OperationDefinition` objects
3. each MCP-enabled operation becomes a Spring AI `ToolCallback`
4. polymorphic operations become multiple tool callbacks
5. Spring AI MCP server support publishes those callbacks as server-side MCP tools
