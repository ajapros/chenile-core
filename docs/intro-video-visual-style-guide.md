# Chenile Intro Video Visual Style Guide

This document gives a practical visual direction for the intro deck so the slides look coherent without much iteration.

## Overall direction

The deck should feel:

- technical
- clear
- modern
- architectural, not marketing-heavy

Avoid:

- overly bright gradients
- stock-photo style slides
- crowded diagrams
- long paragraphs on slides

## Color palette

Suggested palette:

- Background: `#F7F8FA`
- Primary text: `#142033`
- Secondary text: `#4A5A70`
- Primary accent: `#0F5D91`
- Secondary accent: `#1F9D8B`
- Highlight/light fill: `#DCEAF6`
- Warning/emphasis accent: `#E59F2A`

## Typography

### Recommended Google Slides fonts

- Title font: `Space Grotesk` or `IBM Plex Sans`
- Body font: `Inter` or `IBM Plex Sans`
- Code font: `IBM Plex Mono`

### Size suggestions

- Title: 28 to 34 pt
- Subtitle: 18 to 22 pt
- Body bullets: 18 to 22 pt
- Code labels: 16 to 18 pt

## Layout system

Use these recurring layouts:

### Layout A: Title + 3 bullets

Use for:

- definition slides
- value slides
- next steps

### Layout B: Title + diagram

Use for:

- runtime model
- interceptor chain
- events and trajectories
- STM

### Layout C: Title + two-column compare

Use for:

- JSON vs annotations
- problem framing

## Slide rhythm

Try to alternate:

- one text slide
- one visual slide
- one text slide
- one visual slide

This prevents the deck from becoming monotonous.

## Diagram styling

When exporting Mermaid diagrams:

- use dark text on light background
- keep box fill very light
- keep line color medium-dark blue or gray
- avoid too many labels inside one node

If editing diagrams after export:

- make `ChenileExchange` the most visually distinct node
- use one accent color for runtime pipeline arrows
- use a second accent color for domain-oriented concepts

## Suggested slide-by-slide style

### Slide 1

- large centered title
- small capability chips or short keywords

### Slide 2

- 2-column problem framing
- subtle divider line

### Slide 3

- clean capability cluster
- minimal bullets

### Slide 4

- large diagram
- very few bullets

### Slide 5

- left-right convergence visual
- one center box for shared runtime model

### Slide 6

- horizontal pipeline diagram
- optional numbered stages

### Slide 7

- domain binding example highlighted in accent color
- use one method signature visually

### Slide 8

- event and trajectory diagram
- keep labels short

### Slide 9

- state diagram centered
- one short subtitle explaining manual vs automatic states

### Slide 10

- 2x2 value grid
- one word or phrase per quadrant

### Slide 11

- simple reading path arrow
- no more than 3 bullets

## Motion guidance

If using Google Slides animations:

- use minimal fade-ins only
- animate bullet groups, not every line individually
- avoid slide transitions stronger than dissolve

## Visual emphasis rules

Only emphasize:

- `ChenileExchange`
- `ServiceInvoker`
- “same runtime model”
- “cleaner domain services”

If everything is emphasized, nothing is emphasized.

## Suggested slide-building workflow

1. Build all titles first
2. Add bullets next
3. Export Mermaid diagrams and place them
4. Apply consistent colors and typography
5. Remove any slide with more than 4 bullets
6. Rehearse once and simplify wording further if needed

## Good visual shorthand for Chenile

Use the following recurring concepts visually:

- `ChenileExchange` as a central container box
- interceptor chain as a left-to-right pipeline
- domain methods as the “clean endpoint” of the pipeline
- JSON and annotations as two inputs feeding one shared runtime model

## Final quality checklist

Before recording, check:

- titles are consistent in style
- every slide has one main idea
- diagrams are readable at normal zoom
- no bullet exceeds two lines if possible
- slide text does not duplicate your full narration
