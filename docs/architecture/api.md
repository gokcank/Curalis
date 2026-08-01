# PROVIDER (API) & Contracts

## Purpose

This document defines the contracts between application layers.

Its purpose is to establish predictable communication between modules while keeping implementations replaceable.

Business logic should depend on contracts rather than concrete implementations.

---

# Principles

All APIs should be:

- Stable
- Predictable
- Testable
- Documented
- Platform-independent

Contracts should describe behavior, not implementation.

---

# Architecture

Communication should follow the dependency rule.

```
UI

↓

ViewModel

↓

Use Case

↓

Repository

↓

Provider / Database
```

Layers should never bypass intermediate layers.

---

# Repository Contract

Repositories expose business data.

Repositories decide where data originates.

Possible sources include:

- Local database
- Provider
- Cache
- Future cloud synchronization

Business logic must never know the actual source.

---

# Provider Contract

Providers expose medication information.

Providers may represent:

- Public APIs
- Government databases
- Future custom services

Providers must never expose raw responses directly.

Every provider should map external data into internal models.

---

# Database Contract

The database stores user-owned information.

Database implementations should remain hidden behind repositories.

Business logic must never communicate directly with the database.

---

# Use Case Contract

Each use case represents one business action.

Examples:

- Add Medication
- Update Medication
- Delete Medication
- Search Medication
- Confirm Intake
- Schedule Reminder

A use case should have a single responsibility.

---

# UI Contract

The UI displays application state.

The UI should never contain business logic.

UI components should react to immutable state.

---

# Data Models

Separate models should exist for different responsibilities.

Examples:

- Entity
- DTO
- Provider Model
- UI Model

Do not reuse one model for every layer.

---

# Result Pattern

Operations should return explicit results.

Possible outcomes include:

- Success
- Failure
- Validation Error
- Network Error
- Provider Error
- Unknown Error

Avoid using exceptions for expected outcomes.

---

# Validation

Validation belongs as close as possible to business logic.

UI validation improves user experience.

Business validation guarantees correctness.

Business rules must never rely solely on UI validation.

---

# Error Handling

Errors should be meaningful.

Expose user-friendly messages to the UI.

Internal implementation details should remain hidden.

---

# Nullability

Avoid nullable values unless they represent valid business meaning.

Prefer explicit optional types where appropriate.

---

# Pagination

Future provider integrations should support pagination where available.

Pagination behavior should remain provider-independent.

---

# Search

Medication search should:

- Prefer cached results when appropriate.
- Avoid duplicate requests.
- Support cancellation.
- Handle empty results gracefully.

---

# Caching

Caching is an implementation detail.

Repositories decide when cached data is acceptable.

Business logic should remain unaware of caching.

---

# Asynchronous Operations

Long-running operations should be asynchronous.

Blocking operations should be avoided.

Cancellation should be supported where practical.

---

# Versioning

Contracts should evolve carefully.

Breaking contract changes should require:

- Documentation updates
- Version review
- Migration strategy

---

# Testing

Every contract should be independently testable.

Implementations may change.

Contract behavior must remain stable.

---

# Anti-Patterns

Avoid:

- UI calling providers directly
- UI accessing databases
- Business logic depending on frameworks
- Provider-specific models outside the provider layer
- Shared mutable state
- Leaking implementation details

---

# References

- architecture.md
- providers.md
- database.md
- coding.md
- versioning.md