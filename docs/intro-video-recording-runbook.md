# Chenile Intro Video Recording Runbook

This document is a practical runbook for recording the Chenile intro video.

## Recording goal

Produce one clean 8 to 12 minute introductory video with:

- coherent slides
- consistent pacing
- a clear explanation of the Chenile runtime model

## What to keep open while recording

Keep these open on your machine:

1. `docs/intro-video-google-slides.md`
2. `docs/intro-video-speaker-notes.md`
3. your final Google Slides deck
4. `docs/intro-video-mermaid-diagrams-themed.md` if you still need export fixes

Optional:

5. `docs/codebase-overview.md`
6. `docs/interceptor-chain.md`
7. `docs/service-invoker.md`

## Pre-record checklist

- Slides built and visually consistent
- Mermaid diagrams exported and readable
- Speaker notes reviewed once out loud
- Screen resolution checked
- Microphone tested
- Notifications turned off
- Browser tabs and desktop cleaned up

## Recommended speaking flow

### Minute 0 to 1

Explain the problem and set up why Chenile exists.

### Minute 1 to 3

Define Chenile and introduce `ChenileExchange`.

### Minute 3 to 5

Explain the interceptor chain and service definition model.

### Minute 5 to 7

Explain transformation and `ServiceInvoker`.

### Minute 7 to 9

Explain events, trajectories, and STM at a high level.

### Minute 9 to 10

Summarize why Chenile matters and point to follow-up material.

## How to use the speaker notes

- Do not read every sentence literally
- Use the first sentence of each section as your anchor
- Paraphrase the rest naturally
- Pause after each major concept

## Slide-by-slide runbook

### Slide 1

Goal:

- introduce Chenile in one sentence

Say:

- Chenile is a service execution framework for Java and Spring

### Slide 2

Goal:

- define the pain point

Say:

- repeated service plumbing
- mixed business and infrastructure logic

### Slide 3

Goal:

- show that Chenile is broader than HTTP

Say:

- REST is one part
- runtime model is the real story

### Slide 4

Goal:

- explain `ChenileExchange`

Say:

- everything becomes a transport-neutral exchange
- the exchange moves through one runtime path

### Slide 5

Goal:

- explain JSON and annotations without going deep

Say:

- two authoring styles
- one runtime model underneath

### Slide 6

Goal:

- explain interceptors as the cross-cutting layer

Say:

- validation
- transformation
- service resolution
- invocation
- response normalization

### Slide 7

Goal:

- connect Chenile to domain-oriented service design

Say:

- transformation creates typed objects
- `ServiceInvoker` binds them to service methods
- service methods stay cleaner

### Slide 8

Goal:

- show Chenile is more than request handling

Say:

- events reuse the same pipeline
- trajectories swap implementations, not service contracts

### Slide 9

Goal:

- mention STM without letting it dominate

Say:

- separate workflow subsystem
- manual and automatic states

### Slide 10

Goal:

- convert architecture into value

Say:

- less repeated plumbing
- cleaner domain services
- one consistent runtime model

### Slide 11

Goal:

- give the viewer a next step

Say:

- start with the exchange and interceptor docs
- then explore transformation, ServiceInvoker, events, trajectories, and STM

## If recording with slides only

- Stay full screen
- Advance slides manually
- Keep cursor still
- Use the slide titles as your pacing anchors

## If recording with slides plus repo docs

Only switch to docs if:

- you want to quickly show the depth of the repository
- you are comfortable moving between slides and editor windows

Otherwise stay on slides for the intro video and save code navigation for follow-up videos.

## Mistake recovery strategy

If you miss a sentence:

- pause
- restart the current slide section
- do not restart the whole recording unless the mistake happens in the first minute

If you lose the thread:

- return to the anchor phrase:

"Chenile is a common service execution model for Java and Spring."

## Post-record checklist

- trim dead air at the beginning and end
- normalize audio if needed
- verify diagrams are readable in export
- verify text is readable on mobile
- keep the final title simple and technical

## Suggested final video title

Introducing Chenile: A Common Service Execution Model For Java And Spring

## Suggested video description

An architectural introduction to Chenile, an open source Java and Spring framework for building microservices around a common service execution model. This video covers `ChenileExchange`, the interceptor chain, service definitions, transformation, `ServiceInvoker`, events, trajectories, and the STM module.
