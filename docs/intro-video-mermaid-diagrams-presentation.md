# Chenile Intro Mermaid Diagrams For Presentation

These are presentation-tuned Mermaid diagrams with simpler labels and cleaner shapes for exporting into SVG or PNG.

Use these when the default diagrams feel too text-heavy on slides.

## Export tips

- Use Mermaid Live Editor
- Prefer SVG export for slides
- Increase spacing before export
- If labels wrap awkwardly, shorten them before exporting

## Diagram 1: Core Runtime Pipeline

```mermaid
flowchart LR
    A[HTTP or Event] --> B[ChenileExchange]
    B --> C[Interceptor Chain]
    C --> D[Transform Body]
    D --> E[Resolve Service]
    E --> F[Invoke Method]
    F --> G[Build Response]
```

## Diagram 2: Service Definition Convergence

```mermaid
flowchart LR
    A[JSON] --> C[Service Definition Model]
    B[Annotations] --> C
    C --> D[Common Runtime Pipeline]
```

## Diagram 3: Interceptor Skeleton

```mermaid
flowchart LR
    A[Validate] --> B[Pre-Processors]
    B --> C[Select Body Type]
    C --> D[Transform]
    D --> E[Resolve Bean]
    E --> F[Post-Processors]
    F --> G[Invoke]
    G --> H[Normalize Response]
```

## Diagram 4: Domain Binding

```mermaid
flowchart LR
    A[Headers] --> D[ChenileExchange]
    B[Body JSON] --> D
    D --> E[Transformation]
    E --> F[Domain Object]
    D --> G[Header Values]
    F --> H[ServiceInvoker]
    G --> H
    H --> I[Domain Method]
```

## Diagram 5: Events And Trajectories

```mermaid
flowchart TD
    A[Event] --> B[EventProcessor]
    B --> C[ChenileExchange]
    C --> D[Same Pipeline]
    D --> E[Logical Service]
    E --> F{Trajectory?}
    F -- No --> G[Default Bean]
    F -- Yes --> H[Alternate Bean]
```

## Diagram 6: STM Snapshot

```mermaid
stateDiagram-v2
    [*] --> New
    New --> Pending: requiresApproval
    New --> Fulfilled: autoApproval
    Pending --> Fulfilled: approved
    Pending --> Rejected: rejected
```

## Diagram 7: Chenile Capability Map

```mermaid
mindmap
  root((Chenile))
    Runtime Model
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
    Workflows
      STM
      Orchestration
```

## Diagram 8: Learning Path

```mermaid
flowchart LR
    A[Overview] --> B[Exchange]
    B --> C[Interceptors]
    C --> D[Transformation]
    D --> E[ServiceInvoker]
    E --> F[Events and Trajectories]
    F --> G[STM]
```

## Suggested usage

- Slide 4: Diagram 1
- Slide 5: Diagram 2
- Slide 6: Diagram 3
- Slide 7: Diagram 4
- Slide 8: Diagram 5
- Slide 9: Diagram 6
- Optional appendix: Diagram 7 or 8
