# Architecture Decision Records

## Purpose

Architecture Decision Records (ADRs) document important technical and architectural decisions made during the development of Curalis.

An ADR explains:

- the problem
- the available options
- the chosen solution
- the reasoning behind the decision
- the expected consequences

ADRs provide historical context for future contributors and AI development agents.

---

# Principles

An ADR should be created whenever a decision is expected to have long-term impact.

Examples include:

- architecture
- database design
- synchronization strategy
- dependency selection
- security model
- offline strategy
- provider architecture
- design system changes

Minor implementation details do not require ADRs.

When in doubt, create an ADR. It is preferable to document an unnecessary architectural decision than to lose the reasoning behind an important one.

---

# Naming

Use sequential numbering.

Examples:

ADR-001-local-first.md

ADR-002-clean-architecture.md

ADR-003-provider-abstraction.md

Numbers should never be reused.

---

# Status

Every ADR must define its current status.

Possible values:

- Proposed
- Accepted
- Superseded
- Deprecated

---

# Template

Each ADR should contain:

- Title
- Status
- Date
- Context
- Problem
- Options Considered
- Decision
- Consequences
- Alternatives Rejected
- References

---

# Updating ADRs

Accepted ADRs should not be rewritten.

If a decision changes:

- create a new ADR
- reference the previous one
- mark the previous ADR as Superseded

History should remain preserved.

---

# AI Guidelines

AI agents should read relevant ADRs before proposing architectural changes.

AI must never silently contradict an accepted ADR.

If an ADR appears outdated, propose a new ADR instead of modifying history.

---

# References

- architecture.md
- philosophy.md
- AGENTS.md