# ADR-005

# Material Design 3 as Foundation

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis requires a modern, accessible, and maintainable design foundation while preserving its own visual identity.

---

## Problem

Should Curalis create a completely custom UI framework or build upon an existing design system?

---

## Options Considered

### Option A

Fully custom design system

### Option B

Material Design 3

### Option C

Material Design 3 with Curalis Design System

---

## Decision

Curalis adopts Material Design 3 as its technical foundation while maintaining a project-specific Design System.

Material components may be customized when necessary, but consistency with the Curalis Design System always takes priority.

---

## Consequences

### Advantages

- Mature component library
- Excellent accessibility support
- Native Android experience
- Lower maintenance cost

### Trade-offs

- Some Material conventions may constrain customization.
- Designers and developers must understand both Material 3 and the Curalis Design System.

---

## Rules

Material Design defines implementation.

The Curalis Design System defines identity.

Project documentation takes precedence over default Material behavior.

---

## References

- design-system.md
- ui.md