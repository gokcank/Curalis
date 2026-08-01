# Medication Providers

## Purpose

This document defines how Curalis interacts with external medication data providers.

The provider architecture must remain modular, replaceable, and independent from the rest of the application.

No business logic should depend on a specific provider.

---

# Goals

The provider system should be:

- Modular
- Replaceable
- Reliable
- Testable
- Scalable
- Offline-friendly

---

# Design Principles

## Provider Independence

No provider should become a core dependency of Curalis.

Providers may be added, removed, or replaced without affecting the application's business logic.

---

## Single Responsibility

Each provider is responsible only for communicating with one external source.

A provider should never coordinate with another provider.

---

## Repository Ownership

Repositories decide:

- when providers are used
- which provider should be queried
- how provider results are merged
- how provider failures are handled

Providers never make these decisions themselves.

---

# Provider Pipeline

```
User

↓

Repository

↓

Local Database

↓

Provider Manager

↓

Provider

↓

External API
```

The Presentation Layer must never communicate with providers directly.

---

# Supported Providers

The provider system should support any number of external medication databases.

Examples include:

- OpenFDA
- RxNorm
- DrugBank
- Custom Backend
- Future providers

The application must not assume that any provider will always be available.

---

# Provider Priority

Repositories determine provider priority.

Example:

1. Local Database
2. Custom Backend
3. OpenFDA
4. RxNorm
5. Other Providers

Priority must remain configurable.

---

# Provider Responsibilities

A provider may:

- search medications
- search ingredients
- retrieve medication details
- retrieve product metadata
- retrieve manufacturer information
- retrieve barcode information

A provider should never:

- store user data
- schedule reminders
- modify local entities
- make UI decisions

---

# Data Mapping

Provider responses should never be exposed directly.

Every provider response must be mapped into the application's internal models.

Internal models must remain stable even if provider APIs change.

---

# Missing Data

Providers may return incomplete information.

Missing fields must never cause application failures.

Partial information should remain usable.

---

# Multiple Provider Results

Different providers may return different values.

Repositories are responsible for resolving conflicts.

Conflict resolution rules should be documented separately when necessary.

---

# Active Ingredient Search

Medication lookup follows the canonical strategy defined in ADR-008-medication-data-source.md.

Ingredient information should be clearly identified as ingredient-level information.

---

# Manual Medications

If every provider fails:

Users must always be allowed to create medications manually.

Manual entries are considered valid application data.

---

# Rate Limits

Providers may have request limits.

Repositories should:

- minimize requests
- cache responses
- avoid duplicate searches
- retry only when appropriate

---

# Network Failures

Provider failures should never interrupt medication management.

Possible failures include:

- no internet
- timeout
- rate limiting
- unavailable service
- invalid response

The application should continue using locally available data.

---

# Caching

Provider responses may be cached.

Cached information should:

- reduce network usage
- improve startup speed
- improve search performance

Users should be able to clear cached provider data.

---

# Versioning

Provider implementations should tolerate API evolution.

Avoid depending on unstable response structures.

Mapping layers should isolate provider-specific changes.

---

# Security

API credentials must never be hardcoded.

Secrets should remain outside source control.

Providers must follow the rules defined in:

- security.md
- privacy.md

---

# Testing

Every provider should be independently testable.

Repositories should be testable without real network requests.

Mock providers should be supported.

---

# Anti-Patterns

Avoid:

- business logic inside providers
- provider-specific models outside provider modules
- direct API access from UI
- hardcoded provider priority
- duplicated provider implementations

---

# References

- architecture.md
- database.md
- security.md
- privacy.md
- testing.md
- ADR-003-provider-abstraction.md