# Chenile Trajectories

Trajectories are Chenile’s runtime service-override mechanism. They allow the framework to keep the same logical service id and operation contract while switching the actual service implementation for selected requests.

This document explains:

- what a trajectory is
- how trajectory JSON is registered
- how requests activate a trajectory
- how the interceptor chain uses trajectory data during dispatch

## What a trajectory is

A trajectory is a named set of overrides for one or more logical Chenile services.

The core model classes are:

- `chenile-core/src/main/java/org/chenile/core/model/TrajectoryDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/TrajectoryOverride.java`

A `TrajectoryDefinition` contains:

- an id such as `t1`
- a map of service ids to `TrajectoryOverride`

Each `TrajectoryOverride` can specify:

- `newServiceReferenceId`
- `newHealthCheckerReferenceId`

Those fields are Spring bean names, not class names.

## Why trajectories exist

The intended use cases include:

- A/B testing
- tenant-specific behavior
- region-specific behavior
- experimental service implementations
- alternate health-check behavior for selected traffic

The important point is that trajectories change the concrete bean used at runtime without changing the logical Chenile service definition.

## How trajectory JSON is loaded

Trajectory JSON is loaded by `chenile-core/src/main/java/org/chenile/core/init/ChenileTrajectoryInitializer.java`.

The resource pattern comes from:

- `chenile.trajectory.json.package`

Example from the tests:

```properties
chenile.trajectory.json.package=classpath*:org/chenile/core/test/trajectories/*.json
```

## Example trajectory JSON

From `chenile-core/src/test/resources/org/chenile/core/test/trajectories/t1.json`:

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

This means:

- trajectory id is `t1`
- if logical service `mockService` is invoked under trajectory `t1`
- use Spring bean `t1MockService` instead of the default service bean

## Registration flow

During initialization, `ChenileTrajectoryInitializer.registerTrajectoryDefinition(...)`:

1. iterates over each `TrajectoryOverride`
2. resolves `newServiceReferenceId` to a Spring bean
3. resolves `newHealthCheckerReferenceId` to a `HealthChecker` bean if present
4. stores the fully wired trajectory in `ChenileConfiguration`

This means trajectory JSON is not passive configuration. It is resolved against the live Spring context at startup.

## How requests activate a trajectory

The request activates a trajectory through the header:

- `x-chenile-trajectory-id`

This constant is defined in `chenile-core/src/main/java/org/chenile/core/context/HeaderUtils.java`.

Example:

```java
exchange.setHeader("x-chenile-trajectory-id", "t1");
```

That is exactly what the test does in `chenile-core/src/test/java/org/chenile/core/test/TestChenileCore.java`.

## Where the trajectory is applied in the chain

The trajectory is applied in:

- `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java`

`ConstructServiceReference` checks the exchange in this order:

1. if a service reference is already set, leave it alone
2. if mock mode is enabled, use the mock bean
3. otherwise check the trajectory id from headers
4. if the trajectory overrides the current service id, use the override bean
5. otherwise use the normal service bean from `ChenileServiceDefinition`

This is why trajectories fit naturally into the interceptor chain: they modify dispatch just before invocation.

## What changes and what stays the same

When a trajectory is active:

- the logical `serviceDefinition` stays the same
- the `operationDefinition` stays the same
- the runtime `serviceReference` changes
- the reflected `method` may be recomputed on the new bean class

So trajectories do not redefine the contract. They only swap the implementation behind the contract.

## Relationship to service ids

Trajectory overrides are keyed by Chenile service id, not by controller class or URL.

That means:

- JSON-defined services can be overridden
- annotation-defined services can also be overridden
- the same override mechanism works once both are registered into `ChenileConfiguration`

## Health checker overrides

`TrajectoryOverride` can also carry:

- `newHealthCheckerReferenceId`

That allows health checks for a service to vary by trajectory as well.

The registration logic resolves this to a `HealthChecker` bean during startup.

## Test example

The test `testTrajectoryRouting()` in `chenile-core/src/test/java/org/chenile/core/test/TestChenileCore.java` shows the full pattern:

1. create an exchange for logical service `mockService`
2. set header `x-chenile-trajectory-id` to `t1`
3. invoke Chenile
4. observe that the result comes from `t1MockService` rather than the default service

That is the clearest practical example in the repository.

## Relationship to `@ConditionalOnTrajectory`

Chenile also provides:

- `@ConditionalOnTrajectory`
- `@ConditionalHealthCheckOnTrajectory`

These annotations document the idea of trajectory-specific replacement at the Spring configuration level.

The runtime dispatch decision still uses the same trajectory concepts:

- trajectory id
- logical service id
- alternate bean reference

## Mental model

Use this model:

- service definitions describe the stable logical contract
- trajectories choose which implementation bean serves that contract for a request
- `ConstructServiceReference` is the point where that choice is applied

## Most relevant source files

- `chenile-core/src/main/java/org/chenile/core/model/TrajectoryDefinition.java`
- `chenile-core/src/main/java/org/chenile/core/model/TrajectoryOverride.java`
- `chenile-core/src/main/java/org/chenile/core/init/ChenileTrajectoryInitializer.java`
- `chenile-core/src/main/java/org/chenile/core/model/ChenileConfiguration.java`
- `chenile-core/src/main/java/org/chenile/core/interceptors/ConstructServiceReference.java`
- `chenile-core/src/main/java/org/chenile/core/context/HeaderUtils.java`
- `chenile-core/src/test/resources/org/chenile/core/test/trajectories/t1.json`
- `chenile-core/src/test/java/org/chenile/core/test/TestChenileCore.java`
