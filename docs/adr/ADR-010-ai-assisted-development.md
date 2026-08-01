# ADR-010

# AI-Assisted Development

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis is developed using AI assistants alongside human developers.

AI significantly improves productivity but requires consistent guidance and documentation.

---

## Problem

How should AI participate in the development process?

---

## Decision

AI is a development assistant, not the source of truth.

Project documentation defines expected behavior.

Human review remains mandatory for architectural decisions, security, and medical functionality.

---

## Principles

AI should:

- Follow project documentation
- Respect accepted ADRs
- Preserve architectural boundaries
- Avoid unnecessary complexity
- Prefer existing patterns over inventing new ones

---

## Rules

AI must not:

- Invent requirements
- Invent APIs
- Invent database schemas
- Bypass documentation
- Change accepted architecture without proposing a new ADR

If documentation is missing, AI should recommend updating documentation before implementation.

---

## Human Responsibility

Humans remain responsible for:

- Final architecture
- Security
- Medical compliance
- Privacy
- Release approval

AI assists implementation but does not replace engineering judgment.

---

## References

- AGENTS.md
- philosophy.md
- coding.md