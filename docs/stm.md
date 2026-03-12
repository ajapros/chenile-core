# Chenile STM Module

The `stm` module is a standalone state machine engine inside this repository. It is not just a small helper around enums. It provides:

- XML-defined flows
- manual and automatic states
- state entry and exit actions
- transition actions
- multi-flow routing
- security checks
- retrieval and merge hooks for persisted state
- a DSL mechanism for defining custom state and transition tags

This document explains:

- the core abstractions
- how STM flows are defined
- how `STMImpl` executes them
- how the XML DSL works
- how to read the tests as examples

## Core abstractions

The main public abstraction is `stm/src/main/java/org/chenile/stm/STM.java`.

At a high level:

- `STM<StateEntityType>` processes a stateful entity
- the entity implements `StateEntity`
- the current state is represented by `State`
- the state machine uses an `STMFlowStore` to know what states and transitions exist

The three main entry points are:

- `proceed(entity)`
- `proceed(entity, actionParam)`
- `proceed(entity, startingEventId, actionParam)`

These methods allow the caller to:

- start a flow from the beginning
- continue from the current state
- explicitly drive a manual transition with an event id

## Manual states vs automatic states

This is the most important STM distinction.

### Manual state

A manual state waits for an external event.

Examples:

- a user approves an order
- a correction is supplied
- a button is clicked

If the machine reaches a manual state and no event is supplied, it returns control to the caller.

### Automatic state

An automatic state computes its own next event and immediately keeps progressing.

Examples:

- branch based on a condition
- auto-approve if total is below a threshold
- auto-route into another flow

If the machine reaches an automatic state, it does not stop there. It computes the next event and recursively continues.

This is why `STMImpl` can progress through several states in one `proceed(...)` call.

## Main runtime classes

The key runtime classes are:

- `stm/src/main/java/org/chenile/stm/impl/STMImpl.java`
- `stm/src/main/java/org/chenile/stm/impl/STMFlowStoreImpl.java`
- `stm/src/main/java/org/chenile/stm/model/FlowDescriptor.java`
- `stm/src/main/java/org/chenile/stm/model/StateDescriptor.java`
- `stm/src/main/java/org/chenile/stm/model/Transition.java`

### `STMImpl`

`STMImpl` is the execution engine.

Its job is to:

1. determine the current state
2. initialize the entity into the initial state if needed
3. determine the next event
4. resolve the transition
5. run exit action on the start state
6. run the transition action
7. move the entity to the new state
8. run entry action on the end state
9. recurse again if the end state is automatic

That recursion is what makes auto-state flows feel declarative.

### `STMFlowStoreImpl`

`STMFlowStoreImpl` is the runtime registry for all STM configuration.

It stores:

- flows
- state descriptors
- event information
- entry and exit actions
- transition actions
- security strategy
- enablement strategy
- retrieval strategy
- scripting strategy

It also supports including additional XML files and loading a default configuration file for reusable tags.

### `FlowDescriptor`

A flow is a named graph of states. Each flow has:

- an id
- a set of states
- an initial state
- optional flow-level actions and strategies

### `StateDescriptor`

A state descriptor stores:

- state id
- whether it is manual
- whether it is initial
- transitions
- metadata
- entry and exit actions

### `Transition`

A transition connects:

- a source state
- an event id
- a destination state
- optional transition action
- optional destination flow
- optional ACL metadata

## How `STMImpl` executes a flow

The execution algorithm in `STMImpl` is easiest to understand in phases.

### Phase 1: initialize if needed

If the entity has no current state:

- the machine chooses the initial state of the default flow
- sets that state on the entity
- runs entry action for that initial state

### Phase 2: determine the current event

If the current state is manual:

- the caller must supply the event id

If the current state is automatic:

- the machine computes the event internally

### Phase 3: resolve transition

The machine looks up the current state descriptor and finds the transition matching the event id.

From that transition it determines:

- new state id
- new flow id if any
- transition action

### Phase 4: execute actions and move state

The execution order is:

1. start-state exit action
2. transition action
3. update entity current state
4. end-state entry action

### Phase 5: recurse for auto states

If the new state is automatic:

- compute the next event
- continue again recursively

If the new state is manual or final:

- stop and return the entity

This is the core behavior of the STM module.

## XML flow format

STM XML files use a `<states>` root.

The main elements are:

- `<flow>`
- `<manual-state>`
- auto-state tags like `<if>` and `<script>`
- transition tags like `<on>`
- `<entry-action>`
- `<exit-action>`
- `<event-information>`
- `<add-state-tag>`
- `<add-transition-tag>`

## Basic example: cart flow

From `stm/src/test/resources/org/chenile/stm/test/basicflow/cart.xml`:

```xml
<flow id='cart-flow' default='true'>
  <security-strategy componentName="org.chenile.stm.test.basicflow.MockSecurityStrategy"/>
  <entry-action componentName="org.chenile.stm.test.basicflow.EntryAction" />
  <exit-action componentName="org.chenile.stm.test.basicflow.ExitAction" />

  <manual-state id='CREATED' initialState='true'>
    <on eventId='addItem' componentName='org.chenile.stm.test.basicflow.AddItem' />
    <on eventId='initiatePayment'
        componentName='org.chenile.stm.test.basicflow.InitiatePayment'
        newStateId='PAYMENT_INITIATED' />
  </manual-state>

  <manual-state id='PAYMENT_INITIATED'>
    <on eventId="approve" componentName="org.chenile.stm.test.basicflow.ApproveCart"/>
    <on eventId="confirmPayment"
        componentName='org.chenile.stm.test.basicflow.ConfirmPayment'
        newStateId='TEST_STATE'/>
  </manual-state>

  <if id='TEST_STATE' condition='approved' then='confirm' else='reject'>
    <on eventId='confirm' newStateId='PAYMENT_CONFIRMED'/>
    <on eventId='reject' newStateId='PAYMENT_INITIATED'/>
  </if>

  <manual-state id='PAYMENT_CONFIRMED'/>
</flow>
```

This shows several important STM ideas:

- `CREATED` is a manual initial state
- `PAYMENT_INITIATED` is another manual state
- `TEST_STATE` is an automatic branching state implemented by the `<if>` tag
- the machine can go from a manual state into an auto state and then into another manual or final state in one call

## Multi-flow routing example

The order approval flow in `stm/src/test/resources/org/chenile/stm/test/orderapproval/order-flow.xml` shows:

- multiple flows in one file
- transitions from one flow to another using `newFlowId`
- script-based and conditional automatic states

Example:

```xml
<on eventId='invalid' newFlowId='invalidOrder' newStateId='invalid' />
```

This means the machine can jump to another flow while continuing with the same entity.

## The XML DSL mechanism

One of the more sophisticated STM features is the XML DSL system.

The parser is implemented in `stm/src/main/java/org/chenile/stm/impl/XmlFlowReader.java`.

It supports declaring custom tags:

- `add-state-tag`
- `add-transition-tag`

Example from `stm/src/test/resources/org/chenile/stm/test/dsl/dsl.xml`:

```xml
<add-state-tag tag="stage" manualState="true"/>
<add-transition-tag tag="step" eventIdTag="id"/>
<add-transition-tag tag="reject" newStateId="REJECTED"/>
<add-transition-tag tag="succeed" newStateId="SUCCESS"/>
```

After these declarations, the XML can use domain-specific tags:

```xml
<stage id='CREATED' initialState='true'>
  <step id='doS1' newStateId='S1'/>
  <reject/>
</stage>
```

This is equivalent to defining states and transitions with generic tags, but it makes the flow more readable for a specific domain.

## Entry, exit, and transition actions

STM supports several kinds of actions.

### Entry action

Runs after a state is entered.

### Exit action

Runs before leaving a state.

### Transition action

Runs when a specific transition fires.

These actions are regular components resolved by the flow store and invoked by `STMImpl`.

The cart-flow tests verify the ordering of these actions very explicitly by checking the log sequence.

## Security and enablement

The STM module can enforce security and configuration-based behavior.

### Security strategy

A flow can define a security strategy:

```xml
<security-strategy componentName="org.chenile.stm.test.basicflow.MockSecurityStrategy"/>
```

`STMImpl` uses this to decide whether a manual transition is authorized.

### Enablement strategy

The DSL test uses:

```xml
<enablement-strategy componentName="configBasedEnablementStrategy"/>
```

This allows configuration to:

- disable states
- add transitions
- shape the visible flow dynamically

That is what `TestDsl` demonstrates when it hides state `S1` and adds a direct transition.

## Retrieval and persistence hooks

The STM module also supports retrieving and merging persisted entities before proceeding.

`STMImpl.retrieveMergeFromPersistentStorage(...)` uses a `StateEntityRetrievalStrategy` if configured.

This supports flows where:

- the incoming entity is just a partial command object
- the current persisted entity must be loaded first
- the runtime transition should be based on stored state

## How to read the tests

The best examples are:

### `TestCartFlow`

Use this to understand:

- manual states
- automatic states via `<if>`
- action ordering
- security checks
- internal STM-only transitions

### `TestOrderFlow`

Use this to understand:

- multi-flow routing
- correction flows
- script and condition-based automatic branching

### `TestDsl`

Use this to understand:

- custom XML tags
- DSL extension points
- config-based enablement

## Mental model

Use this model:

- a flow is a graph of states
- manual states wait for external events
- automatic states compute their own next event
- `STMImpl` keeps moving until it reaches a manual or final state
- entry, exit, and transition actions are hooks around each move
- XML can be extended into a domain-specific DSL

## Most relevant source files

- `stm/src/main/java/org/chenile/stm/STM.java`
- `stm/src/main/java/org/chenile/stm/impl/STMImpl.java`
- `stm/src/main/java/org/chenile/stm/impl/STMFlowStoreImpl.java`
- `stm/src/main/java/org/chenile/stm/impl/XmlFlowReader.java`
- `stm/src/main/java/org/chenile/stm/model/FlowDescriptor.java`
- `stm/src/main/java/org/chenile/stm/model/StateDescriptor.java`
- `stm/src/main/java/org/chenile/stm/model/Transition.java`
- `stm/src/test/resources/org/chenile/stm/test/basicflow/cart.xml`
- `stm/src/test/resources/org/chenile/stm/test/orderapproval/order-flow.xml`
- `stm/src/test/resources/org/chenile/stm/test/dsl/dsl.xml`
- `stm/src/test/java/org/chenile/stm/test/basicflow/TestCartFlow.java`
- `stm/src/test/java/org/chenile/stm/test/orderapproval/TestOrderFlow.java`
- `stm/src/test/java/org/chenile/stm/test/dsl/TestDsl.java`
