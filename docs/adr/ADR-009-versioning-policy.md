# ADR-009

# Semantic Versioning Policy

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis will evolve over time with new features, architectural improvements, and provider integrations.

A predictable versioning strategy is required.

---

## Problem

How should application versions be managed?

---

## Decision

Curalis follows Semantic Versioning.

Version format:

MAJOR.MINOR.PATCH

Examples:

1.0.0

1.2.0

1.2.3

---

## Rules

MAJOR

Breaking changes.

MINOR

Backward-compatible new features.

PATCH

Bug fixes and small improvements.

---

## Documentation

Breaking architectural changes should reference an ADR.

Release notes should summarize all user-visible changes.

---

## Future

Pre-release versions may use identifiers such as:

-alpha

-beta

-rc

---

## References

- roadmap.md
- decisions.md