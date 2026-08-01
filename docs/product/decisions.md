# Project Decisions

## Purpose

This document records significant project decisions that do not require a formal Architectural Decision Record (ADR).

Its purpose is to preserve project knowledge, reduce repeated discussions, and provide historical context for future contributors.

Major architectural decisions belong in the ADR directory.

---

# Principles

Project decisions should be:

- Documented
- Dated
- Traceable
- Reversible when appropriate

Every decision should explain both **what** was decided and **why**.

---

# Decision Template

Every decision should follow this format.

```md
## YYYY-MM-DD

### Title

Status:
Accepted | Superseded | Rejected | Deprecated

Context

Why was this decision necessary?

Decision

What was decided?

Consequences

What changes because of this decision?

Related Documents

- architecture.md
- roadmap.md
- ADR-00X
```

---

# Decisions

## 2026-08-01

### Local-First MVP

**Status**

Accepted

**Context**

Cloud synchronization significantly increases project complexity.

**Decision**

The MVP will be completely Local-First.

Cloud synchronization will be implemented in a future milestone.

**Consequences**

- No user accounts
- No backend dependency
- Faster development
- Simpler testing

**Related Documents**

- architecture.md
- roadmap.md
- ADR-001-local-first.md

---

## 2026-08-01

### Provider Abstraction

**Status**

Accepted

**Context**

Medication providers may change over time.

**Decision**

All provider implementations must remain behind a provider abstraction layer.

**Consequences**

Changing providers will not require changes to business logic.

**Related Documents**

- providers.md
- ADR-003-provider-abstraction.md

---

## 2026-08-01

### Manual Medication Support

**Status**

Accepted

**Context**

Not every medication exists in every provider.

**Decision**

Manual medication creation is a core feature.

Manual medications are first-class entities.

**Consequences**

The application remains usable even without provider coverage.

**Related Documents**

- database.md
- ADR-008-medication-data-source.md

---

## 2026-08-01

### Offline Reminder Engine

**Status**

Accepted

**Context**

Medication reminders must remain reliable.

**Decision**

Reminder scheduling must never depend on internet connectivity.

**Consequences**

Reminder reliability becomes independent of cloud availability.

**Related Documents**

- reminders.md
- architecture.md

---

## 2026-08-01

### Privacy-First Analytics

**Status**

Accepted

**Context**

Many applications collect unnecessary medical analytics.

**Decision**

Medical information will never be included in analytics.

Analytics, if implemented, must require explicit user consent.

**Consequences**

Reduced data collection.

Improved user trust.

**Related Documents**

- privacy.md
- security.md

---

# Decision Lifecycle

A decision may have one of the following statuses:

- Proposed
- Accepted
- Deprecated
- Superseded
- Rejected

Do not delete historical decisions.

If a decision changes, update its status and reference the replacing decision.

---

# When to Create an ADR

Create an ADR instead of adding an entry here when the decision:

- Changes the architecture.
- Introduces a long-term engineering principle.
- Affects multiple modules.
- Is difficult or expensive to reverse.

---

# References

- architecture.md
- roadmap.md
- adr/