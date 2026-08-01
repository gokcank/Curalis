# Coding Standards

## Purpose

This document defines the coding standards, conventions, and engineering practices used throughout the Curalis project.

Its goal is to ensure consistency, readability, maintainability, and long-term sustainability.

All contributors, including AI agents, must follow these standards.

---

# Principles

## Readability First

Code is read far more often than it is written.

Prefer code that is immediately understandable over code that is clever.

---

## Simplicity

Choose the simplest solution that satisfies the requirements.

Avoid unnecessary abstractions.

Avoid premature optimization.

---

## Consistency

When multiple valid approaches exist, choose the one already used in the project.

Consistency is more valuable than personal preference.

---

## Explicitness

Code should clearly express its intent.

Avoid hidden behavior.

Avoid surprising side effects.

---

# General Rules

- Keep functions focused.
- Keep classes focused.
- Avoid deeply nested code.
- Prefer early returns.
- Prefer immutable data whenever practical.
- Eliminate duplicated logic.

---

# Naming

Names should clearly describe purpose.

Good examples:

```
MedicationRepository
ReminderScheduler
MedicationSearchUseCase
NotificationManager
```

Avoid vague names:

```
Manager
Helper
Utils
Data
Object
Item
```

---

# Functions

Functions should:

- Do one thing.
- Be small.
- Have descriptive names.
- Avoid unnecessary parameters.
- Return predictable results.

Prefer:

```
calculateNextDose()
```

Instead of:

```
doCalculation()
```

---

# Classes

Every class should have one responsibility.

Large classes should be split into smaller components.

Avoid "God Classes."

---

# Comments

Write comments only when necessary.

Prefer improving code over explaining confusing code.

Good comments explain:

- Why
- Constraints
- Non-obvious decisions

Bad comments explain:

- What the code already says

---

# Documentation

Every public component should be documented.

Business rules should always be documented.

Complex algorithms should include implementation notes.

---

# Error Handling

Never silently ignore errors.

Handle expected failures gracefully.

Unexpected failures should be logged.

---

# Logging

Logs should help debugging.

Follow the logging restrictions defined in engineering/security.md.

Debug logs must also follow these rules.

---

# Null Safety

Avoid nullable values whenever possible.

Prefer explicit handling instead of unsafe assumptions.

---

# Magic Numbers

Avoid hardcoded values.

Extract meaningful constants.

Example:

```
const val MAX_RETRY_COUNT = 3
```

Instead of:

```
retry(3)
```

---

# Dependency Injection

Dependencies should be injected.

Avoid directly constructing dependencies inside business logic.

Bad:

```
val repository = MedicationRepository()
```

Good:

```
class SearchMedicationUseCase(
    private val repository: MedicationRepository
)
```

---

# Architecture Boundaries

Presentation must not access:

- Database
- HTTP clients
- Provider implementations

Always communicate through repositories and use cases.

---

# Asynchronous Code

Long-running work should never block the UI.

Use asynchronous execution where appropriate.

---

# Code Duplication

Avoid copy-paste.

If the same logic appears multiple times, extract it.

---

# Configuration

Configuration values should not be hardcoded.

Examples:

- URLs
- Timeouts
- Cache duration
- Feature flags

---

# Feature Development

Every new feature should include:

- Business logic
- Error handling
- Tests
- Documentation updates

No feature is complete without documentation.

---

# Refactoring

Refactor continuously.

Do not postpone obvious improvements.

However:

Never refactor unrelated code while implementing a feature.

---

# Code Reviews

Every contribution should improve at least one of:

- Readability
- Reliability
- Performance
- Maintainability

---

# AI Development Guidelines

AI-generated code is treated exactly like human-written code.

Generated code must:

- Follow project architecture.
- Follow naming conventions.
- Respect module boundaries.
- Avoid unnecessary complexity.
- Be reviewed before acceptance.

---

# Anti-Patterns

Avoid:

- God Objects
- Massive ViewModels
- Massive Activities
- Business logic inside UI
- Circular dependencies
- Deep inheritance hierarchies
- Static global state
- Hidden side effects
- Duplicate business rules

---

# References

- architecture.md
- database.md
- providers.md
- testing.md