# ADR-002

# Clean Architecture

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis is expected to grow over time with additional providers, synchronization options, analytics, and platform support.

The project requires an architecture that remains maintainable, testable, and independent of UI frameworks and external services.

---

## Problem

How should the application's architecture be organized to support long-term maintainability?

---

## Options Considered

### Option A

Feature-oriented architecture

### Option B

MVVM only

### Option C

Clean Architecture with MVVM

---

## Decision

Curalis adopts Clean Architecture using MVVM as the presentation pattern.

The application is organized into distinct layers with clear responsibilities.

Typical dependency flow:

Presentation

↓

Domain

↓

Data

Dependencies must always point inward.

---

## Consequences

### Advantages

- Improved maintainability
- High testability
- Framework independence
- Easier provider replacement
- Better scalability

### Trade-offs

- More project structure
- Additional abstraction
- Higher initial development effort

---

## Rules

Business rules belong to the Domain layer.

UI must never contain business logic.

Repositories abstract data sources.

Providers must never be accessed directly by the UI.

---

## Future

Future technologies should integrate without changing the Domain layer.

Architecture should remain stable even if UI frameworks or providers evolve.

---

## References

- architecture.md
- coding.md
- providers.md