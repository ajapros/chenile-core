# Chenile Intro Google Slides Presentation

This document is a Google Slides-ready presentation plan for the Chenile intro video.

It is structured so you can copy each slide directly into Google Slides with:

- slide title
- subtitle if needed
- exact on-slide text
- speaker goal
- visual guidance
- suggested diagram reference

Use this together with:

- `intro-video-speaker-notes.md`
- `intro-video-slide-deck.md`
- `intro-video-mermaid-diagrams.md`

## Suggested design direction

### Theme

Keep it technical and clean.

Recommended style:

- white or near-white background
- deep blue or slate accent
- one accent color for emphasis, such as teal
- minimal animation
- code screenshots only when they add value

### Typography

Suggested Google Slides fonts:

- Titles: `Space Grotesk` or `IBM Plex Sans`
- Body: `Inter`, `IBM Plex Sans`, or `Source Sans 3`
- Code labels: `IBM Plex Mono`

### Layout rules

- keep each slide to 3 or 4 bullets maximum
- use diagrams more than dense text
- do not paste Java code unless the code is very small
- use one consistent color for Chenile runtime objects such as `ChenileExchange`

## Slide 1

### Type

Title slide

### Title

`Introducing Chenile`

### Subtitle

`A common service execution model for Java and Spring`

### On-slide text

- REST
- Interceptors
- Transformation
- Events
- Trajectories
- STM

### Speaker goal

Set scope quickly.

### Visual guidance

- Put the framework name large in the center
- Put the six capability words in a clean grid below

---

## Slide 2

### Type

Problem framing

### Title

`Why Chenile?`

### On-slide text

- Business logic gets mixed with service plumbing
- Cross-cutting concerns get repeated
- HTTP and events often evolve as separate stacks

### Speaker goal

Explain the pain before introducing the architecture.

### Visual guidance

Two-column layout:

- left: business logic
- right: transport, mapping, headers, errors, policies, events

---

## Slide 3

### Type

Definition slide

### Title

`What Chenile Is`

### On-slide text

- Open source Java and Spring framework
- More than HTTP
- A service execution framework

### Speaker goal

Define Chenile in one sentence.

### Visual guidance

Use a simple capability cluster:

- service definitions
- interception
- transformation
- events
- trajectories
- STM

---

## Slide 4

### Type

Architecture anchor slide

### Title

`Core Runtime Model`

### On-slide text

- Input becomes `ChenileExchange`
- Exchange moves through interceptor chain
- Target service is invoked
- Response returns through the same model

### Speaker goal

Introduce the central mental model.

### Visual guidance

Use Mermaid Diagram 1 from `intro-video-mermaid-diagrams.md`.

---

## Slide 5

### Type

Convergence slide

### Title

`Two Ways To Define Services`

### On-slide text

- JSON service definitions
- Annotation-based controllers
- Same internal runtime model

### Speaker goal

Explain that authoring style is flexible, but execution is unified.

### Visual guidance

Use Mermaid Diagram 2.

---

## Slide 6

### Type

Runtime behavior slide

### Title

`The Interceptor Chain`

### On-slide text

- Fixed execution skeleton
- Dynamic expansion per service and operation
- Cross-cutting behavior in one place

### Speaker goal

Show that Chenile centralizes runtime plumbing.

### Visual guidance

Use Mermaid Diagram 3.

---

## Slide 7

### Type

DDD-value slide

### Title

`Transformation And ServiceInvoker`

### On-slide text

- Raw payload becomes a domain object
- Headers and body become method arguments
- Domain-oriented services stay cleaner

### Speaker goal

Connect architecture to domain-focused service design.

### Visual guidance

Use Mermaid Diagram 4.

---

## Slide 8

### Type

Beyond-HTTP slide

### Title

`Events And Trajectories`

### On-slide text

- Events reuse the same pipeline
- Trajectories swap implementation beans at runtime
- Logical service contract stays the same

### Speaker goal

Show that Chenile is not only a request framework.

### Visual guidance

Use Mermaid Diagram 5.

---

## Slide 9

### Type

Subsystem overview

### Title

`The STM Module`

### On-slide text

- Declarative workflows
- Manual and automatic states
- Entry, exit, and transition actions

### Speaker goal

Position STM as a broader platform feature.

### Visual guidance

Use Mermaid Diagram 6.

---

## Slide 10

### Type

Value summary

### Title

`Why Chenile Matters`

### On-slide text

- Less repeated plumbing
- Cleaner service interfaces
- One model across HTTP and events
- Extensible architecture

### Speaker goal

Translate mechanics into value.

### Visual guidance

Use a 2x2 value grid.

Grid labels:

- Cleaner services
- Reusable policies
- Consistent runtime
- Workflow support

---

## Slide 11

### Type

Close

### Title

`Next Steps`

### On-slide text

- Start with `ChenileExchange`
- Then the interceptor chain
- Then transformation, events, trajectories, and STM

### Speaker goal

Give the viewer a path forward.

### Visual guidance

Use a reading path arrow:

`Exchange -> Interceptors -> ServiceInvoker -> Events/Trajectories -> STM`

## Suggested build sequence in Google Slides

1. Create an 11-slide deck
2. Apply one clean theme across all slides
3. Build title and problem slides first
4. Insert Mermaid diagrams as images for slides 4 through 9
5. Add speaker notes from `intro-video-speaker-notes.md`
6. Rehearse once with timing

## Suggested speaker-note mapping

Use:

- `intro-video-speaker-notes.md` for spoken delivery
- `intro-video-script.md` if you want fuller narration

## Export guidance

For recording:

- use Presenter View if possible
- keep slides static while you speak
- if using screen recording, zoom in slightly so diagrams are readable
