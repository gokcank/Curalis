# Architecture

## Purpose

This document defines the architectural foundation of Curalis.

It describes how the application is organized, how components interact, and which engineering principles must always be respected.

This document is the primary architectural reference for the entire project.

---

# Goals

The architecture should provide:

- Simplicity
- Maintainability
- Scalability
- Testability
- Reliability
- Offline capability
- Privacy by default
- Future cloud readiness

The architecture should evolve without requiring major rewrites.

---

# Design Philosophy

Curalis follows a pragmatic architecture.

Every architectural decision should prioritize:

1. Correctness
2. Simplicity
3. Maintainability
4. Extensibility
5. Performance

Avoid introducing complexity until it is justified.

---

# Core Principles

## Separation of Concerns

Each layer has a single responsibility.

Business logic must never exist inside UI components.

API implementations must never leak into presentation code.

Database details must remain hidden behind repositories.

---

## Single Responsibility

Every class, module, and component should have one reason to change.

Avoid "God Objects."

---

## Dependency Direction

Dependencies always point inward.

Outer layers depend on inner layers.

Core business logic must never depend on UI frameworks or API implementations.

---

## Composition Over Inheritance

Prefer composition whenever possible.

Inheritance should only be used when there is a clear "is-a" relationship.

---

## Interface-Driven Design

Application layers communicate through interfaces.

Concrete implementations should remain replaceable.

---

## Local-First

The application should always prefer local data.

Network access is considered an enhancement, not a requirement.

Users must retain access to their medication information without internet connectivity.

---

## Cloud Ready

Although cloud synchronization is not part of the MVP, the architecture must support future synchronization without significant refactoring.

---

# Architectural Layers

The application is organized into independent layers.

```
Presentation
↓

Application

↓

Domain

↓

Data

↓

Infrastructure
```

---

## Presentation Layer

Responsible for:

- UI
- Navigation
- State rendering
- User interactions

The presentation layer must not contain business logic.

---

## Application Layer

Responsible for:

- Use cases
- Application workflows
- Coordination between components

This layer orchestrates operations but does not contain infrastructure details.

---

## Domain Layer

The heart of the application.

Contains:

- Business rules
- Domain models
- Validation
- Core logic

The domain layer must remain independent of frameworks.

---

## Data Layer

Responsible for:

- Repositories
- Local database
- Remote providers
- Cache

The rest of the application should never know where data comes from.

---

## Infrastructure Layer

Responsible for:

- SQLite / Room
- HTTP clients
- Notifications
- Logging
- File storage
- Dependency Injection

Infrastructure details must never leak into business logic.

---

# Repository Pattern

Repositories are the only entry point for application data.

Repositories decide:

- Local or remote
- Cache usage
- Synchronization
- Data merging

Presentation must never communicate directly with APIs.

---

# Provider Pattern

External medication providers must remain isolated.

Examples:

- OpenFDA
- RxNorm
- DrugBank
- Future backend

Providers must be replaceable without affecting business logic.

---

# Local Database

The local database is the primary data source.

Remote providers enrich local data.

Downloaded information should be cached whenever appropriate.

---

# Manual Medications

Manually created medications are first-class entities.

They must behave exactly like imported medications.

Future provider matches may enrich manual entries but must never overwrite user data without consent.

---

# Active Ingredient Strategy

Medication lookup follows the strategy defined in ADR-008-medication-data-source.md.

If only the active ingredient is found, the application must clearly indicate that the information applies to the ingredient rather than a specific commercial product.

---

# Error Handling

Errors should be recoverable whenever possible.

The application must never crash because of:

- Missing network
- Missing providers
- Missing cache
- Missing optional data

Graceful degradation is preferred.

---

# State Management

Application state should be predictable.

Avoid hidden side effects.

State should flow in one direction.

---

# Background Work

Long-running tasks should execute asynchronously.

Examples:

- Provider synchronization
- Reminder scheduling
- Cache cleanup
- Backup
- Data refresh

The UI must remain responsive.

---

# Modularity

Major features should remain isolated.

Examples:

- Medications
- Reminders
- Inventory
- Reports
- Settings
- Notifications

Modules should communicate through clearly defined interfaces.

---

# Scalability

The architecture should support future features without redesign.

Potential future capabilities include:

- Cloud synchronization
- Multi-device support
- Wearables
- OCR
- Barcode scanning
- Prescription import
- Family sharing
- Healthcare integrations

---

# Architectural Constraints

The architecture must never:

- Depend on a single API provider
- Require permanent internet access
- Mix business logic with UI
- Expose database implementation details
- Couple business logic to Android-specific APIs

---

# References

- database.md
- providers.md
- sync.md
- testing.md
- ADR-001-local-first.md
- ADR-002-clean-architecture.md
- ADR-003-provider-abstraction.md
- ADR-008-medication-data-source.md