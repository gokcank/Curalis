# Documentation Loading Strategy

AI agents and contributors should load only the documentation required for the current task.

Avoid loading the entire documentation unless explicitly requested.

---

## Always Read

These documents define the project's core principles and should always be read before starting any development task.

1. foundation/philosophy.md
2. product/product.md
3. architecture/architecture.md

---

## Read When Needed

### Product

Read when implementing or modifying product behavior.

- product/reminders.md
- product/implementation-plan.md
- product/roadmap.md
- product/glossary.md
- product/decisions.md

### Architecture

Read when modifying application architecture, data flow, or integrations.

- architecture/database.md
- architecture/providers.md
- architecture/api.md
- architecture/sync.md
- architecture/versioning.md

### Design

Read when implementing or modifying the user interface or user experience.

- design/ui.md
- design/design-system.md
- design/content-guidelines.md
- design/illustrations.md

### Engineering

Read when writing, reviewing, or optimizing code.

- engineering/coding.md
- engineering/testing.md
- engineering/security.md
- engineering/performance.md
- engineering/contributing.md

### Compliance

Read when handling medical data, accessibility, privacy, or legal requirements.

- compliance/medical-safety.md
- compliance/privacy.md
- compliance/accessibility.md
- compliance/legal.md

---

## Architecture Decision Records (ADR)

Read only the ADRs relevant to the current task.

Do not load every ADR by default.

If a task affects architecture or long-term technical direction, review the relevant ADRs before implementation.

If no suitable ADR exists, propose creating a new ADR before making the change.

ADR index: adr/README.md