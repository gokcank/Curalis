# ADR-006

# Room as Local Database

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Medication records, reminders, history, inventory, and user preferences require reliable local persistence.

---

## Problem

Which local database technology should Curalis adopt?

---

## Options Considered

### Option A

SQLite

### Option B

Room

### Option C

Realm

### Option D

ObjectBox

---

## Decision

Curalis adopts Room as the primary local database.

Room provides a stable abstraction over SQLite while integrating naturally with the Android ecosystem.

---

## Consequences

### Advantages

- Mature ecosystem
- Compile-time validation
- Excellent tooling
- Offline support
- Strong integration with Jetpack

### Trade-offs

- Android-specific implementation
- SQL schema migrations require maintenance

---

## Rules

Room entities are persistence models only.

Business logic must never exist inside database entities.

Repositories are responsible for mapping persistence models to domain models.

---

## References

- database.md
- architecture.md