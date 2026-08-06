# AGENTS.md

# Curalis AI Development Guide

Welcome to the Curalis project.

This repository is developed using AI-assisted engineering. Before making any changes, you must understand and follow the project's documentation.

Do not begin implementation until the relevant documentation has been reviewed.

---

# Primary Rule

Documentation is the source of truth.

If code conflicts with documentation, the documentation is considered correct until intentionally updated.

Do not silently change documented behavior.

---

# Documentation Order

Read documentation in the following order.

1. docs/index.md
2. docs/foundation/philosophy.md
3. docs/product/product.md
4. docs/architecture/architecture.md

Then read any topic-specific documents related to the task.

Examples:

Medication Providers

→ docs/architecture/providers.md

Reminder Engine

→ docs/product/reminders.md

Medical Logic

→ docs/compliance/medical-safety.md

Database

→ docs/architecture/database.md

UI

→ docs/design/ui.md

---

# Before Writing Code

Before implementing a feature, always:

- Understand the request.
- Read the relevant documentation.
- Check project decisions.
- Check ADRs.
- Avoid assumptions.

If documentation is missing, propose documentation before implementation.

---

# Documentation-Driven Development

Prefer updating documentation before writing implementation.

Architecture should lead implementation.

Implementation should not redefine architecture.

---

# Project Philosophy

Always follow:

- Medical Safety
- User Privacy
- User Ownership
- Reliability
- Simplicity
- Accessibility
- Transparency
- Maintainability
- Performance

These principles are defined in:

docs/foundation/philosophy.md

---

# Architecture Rules

Never violate architectural boundaries.

Follow:

UI

↓

ViewModel

↓

Use Case

↓

Repository

↓

Provider / Database

Business logic must remain framework-independent.

---

# Medical Safety

Never implement:

- Medical advice
- Dosage recommendations
- Diagnosis
- Treatment recommendations

Medication information must originate from trusted providers or explicit user input.

Follow:

docs/compliance/medical-safety.md

---

# Privacy

Never introduce unnecessary data collection.

Cloud functionality must remain optional.

User data belongs to the user.

Follow:

docs/compliance/privacy.md

---

# Security

Never:

- Hardcode secrets.
- Disable certificate validation.
- Log sensitive information.
- Trust external input.

Follow:

docs/engineering/security.md

---

# Coding Standards

Follow all coding conventions defined in:

docs/engineering/coding.md

Avoid introducing alternative patterns unless documentation is updated first.

---

# Documentation Updates

Whenever behavior changes:

- Update documentation.
- Update related references.
- Update roadmap if necessary.
- Update glossary if terminology changes.

Implementation is not complete until documentation is updated.

---

# Dependencies

Before introducing a dependency, verify:

- necessity
- maintenance
- license
- long-term viability

Avoid unnecessary dependencies.

---

# Testing

Every implementation should include appropriate tests.

Follow:

docs/engineering/testing.md

Do not rely on manual verification alone.

---

# Decision Making

When multiple valid solutions exist, follow the decision priorities defined in:

docs/foundation/philosophy.md

Do not optimize prematurely.

---

# When Documentation Conflicts

Priority order:

1. philosophy.md
2. product.md
3. architecture.md
4. Topic-specific documentation
5. ADRs
6. Code

If uncertainty remains, stop and request clarification instead of making assumptions.

---

# Pull Requests

Every meaningful implementation should:

- respect architecture
- update documentation
- preserve backward compatibility where practical
- include testing

---

# AI Behavior

Do not:

- invent requirements
- invent architecture
- invent APIs
- invent database schema
- ignore documentation
- silently change documented behavior

If information is missing, explicitly state what is missing.

Never guess.

---

# Development Environment & Tooling

The user develops strictly using **Antigravity IDE** powered by **Gemini** (NOT Android Studio GUI).

Never refer to Android Studio GUI features, menus, or IDE buttons.

Always use CLI tools (`./gradlew`, `adb`, shell scripts) and Antigravity IDE capabilities for building, testing, running, and managing the project.

---

# Goal

Produce software that remains:

- Safe
- Reliable
- Maintainable
- Predictable
- Privacy-respecting

Long-term quality is always more important than short-term implementation speed.

---

# Architecture Decision Records

AI should determine whether a change requires an Architecture Decision Record (ADR) before implementation.

An ADR should be proposed when a change affects:

- System architecture
- Project structure
- Data model
- Public APIs
- Provider integrations
- Security
- Privacy
- Synchronization
- Design System
- Development workflow
- Long-term technical direction

AI must not silently introduce long-term architectural changes.

If an architectural decision is required, implementation should pause until:

1. A new ADR is proposed.
2. The ADR is reviewed.
3. The ADR is accepted.
4. Implementation continues.

---

# Strict Anti-Bundling and Git Guardrails

> [!CAUTION]
> **AI BEHAVIORAL OVERRIDE:** The agent's natural tendency to bundle Git commands is strictly forbidden.

1. **NEVER bundle `git push`:** If the user commands a commit (e.g., "commit it", "commiti sen yap"), the agent must execute ONLY the commit operation. Do not chain `&& git push`. The word "commit" NEVER grants permission to push.
2. **Push requires explicit command:** The agent must only execute a push if the user explicitly types the word "push" in their request.
3. **Do not skip review phases:** If an implementation plan defines a "Pause for Review" step, the agent must completely stop execution. A user command regarding the *mechanics* of the next step (e.g., "you do the commit") does NOT constitute approval of the code. The agent must still wait for explicit code approval (e.g., "code looks good, commit it") before executing the commit.

---

# Medisafe Architecture & Feature Benchmarking & Anonymity Guardrails

1. **Internal Benchmark Only:** Whenever proposing features, UI layouts, database models, or workflows in internal developer chat, reference Medisafe's decompiled architecture as an engineering benchmark ("Bak Medisafe'de de böyleydi...").
2. **Zero External Attribution in Code & UI (Strict Anonymity):** NEVER include "Medisafe" or any third-party app name/brand in codebase files (`.kt`, `.xml`, `build.gradle.kts`), Javadocs, code comments, UI strings, commit messages, or public app documentation. All Curalis implementations, designs, and code structures must remain 100% original, unique, clean, and proprietary to Curalis.

---

# Package Execution & Commitment Workflow

0. **Mandatory Package & Item Structure:** All future implementation plans MUST be explicitly structured into numbered **Packages (Paketler)** and distinct sub-task **Items (Maddeler)**.
1. **Item-by-Item Local Commit:** When working on an approved plan separated into Packages and Items:
   - Implement one single item at a time.
   - Verify the build cleanly (`./gradlew assembleDebug` or test).
   - Perform ONLY the local `git commit` for that completed item (NEVER `git push`).
2. **Package Completion, Emulator Testing & User Push:** At the end of completing ALL items in a Package:
   - Perform a full commit and build audit (`git log`, `./gradlew test assembleDebug`).
   - Install the debug APK onto the Android Emulator (`./gradlew installDebug`), launch the app, and run live visual/functional emulator tests.
   - Report the completed Package and test results to the user so the user can review and execute `git push` for the entire package.