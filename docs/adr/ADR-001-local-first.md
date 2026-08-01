# ADR-001

# Local-First Architecture

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis stores personal medication records, reminder schedules, treatment history, and inventory information.

This data should remain available regardless of network connectivity.

Users should retain ownership of their personal health information.

---

## Problem

Should Curalis require cloud infrastructure for normal operation?

---

## Options Considered

### Option A

Cloud-first

### Option B

Hybrid

### Option C

Local-first

---

## Decision

Curalis adopts a Local-First architecture.

Core functionality must remain available without internet access.

Cloud synchronization is optional and may be added in future versions.

---

## Consequences

### Advantages

- Better privacy
- Faster response times
- Offline support
- User ownership
- Reduced infrastructure costs

### Trade-offs

- Device backup becomes the user's responsibility unless cloud sync is enabled.
- Multi-device synchronization requires additional infrastructure.

---

## Future

Optional cloud synchronization must never replace local storage.

The local database remains the primary source of truth.

---

## References

- philosophy.md
- architecture.md