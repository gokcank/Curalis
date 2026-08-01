# Contributing

## Purpose

This document defines the contribution workflow for Curalis.

Its goal is to ensure that every contribution maintains the project's quality, consistency, and long-term maintainability.

These guidelines apply equally to human contributors and AI-assisted development.

---

# Guiding Principles

Every contribution should:

- Improve the project.
- Preserve architectural integrity.
- Respect existing conventions.
- Avoid unnecessary complexity.
- Include appropriate documentation.

The objective is long-term maintainability rather than short-term speed.

---

# Before You Start

Before implementing a feature:

- Read the relevant documentation.
- Check existing Architectural Decision Records (ADRs).
- Review previous project decisions.
- Verify that no similar work already exists.

Never implement features based on assumptions.

---

# Development Workflow

The recommended workflow is:

1. Understand the problem.
2. Review documentation.
3. Design the solution.
4. Implement.
5. Test.
6. Update documentation.
7. Submit for review.

Skipping documentation updates is not acceptable.

---

# Branch Strategy

Every feature should be developed in its own branch.

Examples:

feature/medication-search

feature/reminder-engine

bugfix/timezone-reminders

refactor/provider-layer

docs/update-roadmap

Branch names should be short, descriptive, and lowercase.

---

## Commit Policy

Development should progress in small, self-contained increments.

After completing a meaningful unit of work, AI should automatically create a Git commit and then recommend pushing before proceeding. AI must NEVER automatically execute `git push`.

Examples include:

- Completing a project phase
- Finishing a feature
- Refactoring without behavior changes
- Resolving a bug
- Updating documentation

AI should never perform large, unrelated changes without creating intermediate commits.

---

# Commit Messages

Commits should describe **what changed**, not how much work was done.

Examples:

```
Add medication search repository

Fix reminder scheduling after reboot

Refactor provider abstraction

Update database documentation
```

Avoid vague messages such as:

```
Update

Fix

Changes

Work

Final
```

---

# Pull Requests

Each pull request should focus on a single logical change.

Pull requests should include:

- Summary
- Motivation
- Testing performed
- Documentation changes
- Known limitations (if any)

Large unrelated changes should be split into multiple pull requests.

---

# Code Reviews

Code reviews should evaluate:

- Correctness
- Simplicity
- Maintainability
- Security
- Privacy
- Medical safety
- Testability

Personal coding preferences should not override documented standards.

---

# Documentation

Documentation is part of the implementation.

Every architectural or behavioral change should update the relevant documentation.

Do not duplicate information across documents.

---

# Testing

Every contribution should include appropriate testing.

Examples:

- Unit tests
- Integration tests
- UI tests
- Manual verification

Testing requirements are defined in `testing.md`.

---

# Dependencies

Before introducing a new dependency, evaluate:

- Maintenance status
- Community support
- License compatibility
- Security history
- Long-term viability

Avoid adding dependencies for trivial functionality.

---

# Refactoring

Refactoring is encouraged when it:

- Improves readability.
- Reduces duplication.
- Simplifies architecture.
- Improves maintainability.

Avoid unrelated refactoring during feature development.

---

# Breaking Changes

Breaking architectural changes require:

- Updated documentation
- New ADR (if applicable)
- Migration strategy
- Team agreement

---

# AI-Assisted Development

AI-generated code must:

- Follow project architecture.
- Respect all documentation.
- Be reviewed before merging.
- Pass the same quality standards as human-written code.

AI should accelerate development, not replace engineering judgment.

---

# Definition of Ready

Work should begin only when:

- Requirements are understood.
- Scope is defined.
- Documentation has been reviewed.
- Dependencies are identified.

---

# Definition of Done

A task is complete only when:

- Implementation is finished.
- Tests pass.
- Documentation is updated.
- No known critical issues remain.
- Code review requirements are satisfied.

---

# Project Values

Follow the decision priorities defined in:

docs/foundation/philosophy.md

Feature count is never a measure of project quality.

---

# References

- architecture.md
- coding.md
- testing.md
- roadmap.md
- medical-safety.md