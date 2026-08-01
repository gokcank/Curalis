# ADR-004

# Offline-First Synchronization

## Status

Accepted

---

## Date

2026-08-01

---

## Scope

This ADR defines the synchronization architecture for future implementations.

Synchronization is not part of the current MVP.

When synchronization is introduced, it must follow the principles defined in this ADR.

---

## Context

Medication management must remain reliable without continuous internet access.

Users should never lose access to essential functionality due to network conditions.

---

## Problem

How should synchronization behave?

---

## Decision

The local database is always the source of truth.

Remote synchronization is an optional enhancement.

Synchronization must never overwrite local data without conflict resolution.

---

## Principles

Local changes occur immediately.

Synchronization happens asynchronously.

Temporary network failures should not interrupt user workflows.

---

## Conflict Resolution

Conflicts should be detected explicitly.

Automatic data loss is unacceptable.

Whenever practical, users should be informed about conflicts.

---

## Future

Support for cloud synchronization may be added without changing the Local-First architecture.

---

## References

- sync.md
- database.md
- philosophy.md