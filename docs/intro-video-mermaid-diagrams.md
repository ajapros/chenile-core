# Chenile Intro Mermaid Diagrams

These Mermaid diagrams are designed for the intro slide deck.

Use them in one of these ways:

- paste into Mermaid Live Editor and export as SVG or PNG
- paste into Markdown tools that support Mermaid
- convert to images and place into Google Slides

## Diagram 1: Core Runtime Model

```mermaid
flowchart LR
    A[HTTP Request or Event] --> B[ChenileExchange]
    B --> C[Interceptor Chain]
    C --> D[Transformation]
    D --> E[Service Resolution]
    E --> F[ServiceInvoker]
    F --> G[Domain Service Method]
    G --> H[Generic Response]
    H --> I[HTTP Response or Event Outcome]
```

## Diagram 2: JSON And Annotations Converge

```mermaid
flowchart LR
    A[JSON Service Definitions] --> C[ChenileServiceDefinition]
    B[Annotated Controllers] --> C
    C --> D[OperationDefinition]
    D --> E[Common Runtime Pipeline]
```

## Diagram 3: Interceptor Chain

```mermaid
flowchart LR
    A[Validate Headers] --> B[Pre-Processors]
    B --> C[Transformation Class Selector]
    C --> D[Transformer]
    D --> E[Construct Service Reference]
    E --> F[Post-Processors]
    F --> G[Operation Interceptors]
    G --> H[Service Interceptors]
    H --> I[ServiceInvoker]
    I --> J[Response Builder]
```

## Diagram 4: Domain Binding

```mermaid
flowchart LR
    A[Request Headers] --> D[ChenileExchange]
    B[Request Body JSON] --> D
    D --> E[Transformation]
    E --> F[User / Order / Command Object]
    D --> G[Header Values]
    F --> H[ServiceInvoker]
    G --> H
    H --> I["Domain Method e.g. updateUser(String userId, User user)"]
```

## Diagram 5: Events And Trajectories

```mermaid
flowchart TD
    A[Named Event] --> B[EventProcessor]
    B --> C[ChenileExchange]
    C --> D[Same Interceptor Chain]
    D --> E[Logical Service]
    E --> F{Trajectory Active?}
    F -- No --> G[Default Service Bean]
    F -- Yes --> H[Alternate Service Bean]
```

## Diagram 6: STM Overview

```mermaid
stateDiagram-v2
    [*] --> New
    New --> PendingApproval: requiresApproval
    New --> Fulfilled: autoApproval
    PendingApproval --> Fulfilled: approved
    PendingApproval --> Discarded: rejected
```

## Diagram 7: Broader Chenile Capability Map

```mermaid
mindmap
  root((Chenile))
    Service Execution Model
      ChenileExchange
      Interceptor Chain
      ServiceInvoker
    Service Definitions
      JSON
      Annotations
    Runtime Features
      Transformation
      Events
      Trajectories
    Workflow
      STM
      Orchestration
```

## Diagram 8: Suggested Reading Path

```mermaid
flowchart LR
    A[Codebase Overview] --> B[ChenileExchange]
    B --> C[Interceptor Chain]
    C --> D[Transformation]
    D --> E[ServiceInvoker]
    E --> F[Events and Trajectories]
    F --> G[STM]
```
