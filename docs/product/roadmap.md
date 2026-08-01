# Roadmap

## Purpose

This document defines the long-term development roadmap of Curalis.

It provides a structured view of planned milestones, priorities, and future evolution while remaining flexible enough to adapt as the project grows.

The roadmap is a planning document rather than a strict commitment.

---

# Product Strategy

Development follows an incremental approach.

Each milestone should produce a stable, usable application.

Every release should improve the product without requiring major architectural rewrites.

---

# Development Principles

Every milestone should:

- Deliver user value.
- Preserve architectural integrity.
- Improve overall quality.
- Maintain backward compatibility whenever practical.
- Avoid unnecessary technical debt.

---

# Milestone 1 — Project Foundation

## Goal

Establish the technical foundation of the project.

### Deliverables

- Project setup
- Build configuration
- Dependency injection
- Navigation
- Local database
- Basic architecture
- Documentation
- CI setup

### Exit Criteria

- Application builds successfully.
- Documentation is established.
- Architecture is implemented.
- Development workflow is functional.

---

# Milestone 2 — Medication Management

## Goal

Allow users to manage medications.

### Deliverables

- Medication CRUD
- Medication search
- Manual medications
- Medication details
- Active ingredient support

### Exit Criteria

Users can fully manage medications locally.

---

# Milestone 3 — Reminder System

## Goal

Implement reliable medication reminders.

### Deliverables

- Reminder scheduling
- Repeating reminders
- Snooze
- Reminder history
- Reminder confirmation

### Exit Criteria

Medication reminders operate reliably without internet access.

---

# Milestone 4 — Inventory Management

## Goal

Track medication quantities.

### Deliverables

- Inventory tracking
- Low inventory alerts
- Refill reminders
- Inventory history

### Exit Criteria

Inventory updates automatically after confirmed medication intake.

---

# Milestone 5 — Provider Integration

## Goal

Integrate external medication databases.

### Deliverables

- Provider abstraction
- Medication lookup
- Active ingredient lookup
- Metadata retrieval
- Provider caching

### Exit Criteria

Medication information can be imported from supported providers.

---

# Milestone 6 — History & Insights

## Goal

Provide users with medication history.

### Deliverables

- Medication history
- Treatment history
- Statistics
- Timeline

### Exit Criteria

Users can review historical medication activity.

---

# Milestone 7 — Polish

## Goal

Improve overall quality.

### Deliverables

- Performance improvements
- Accessibility improvements
- UI refinement
- Animations
- Error handling
- Additional testing

### Exit Criteria

Application quality is suitable for public release.

---

# Milestone 8 — Cloud Synchronization

## Goal

Introduce optional synchronization.

### Deliverables

- User accounts
- Secure synchronization
- Conflict resolution
- Multi-device support

### Exit Criteria

Synchronization remains optional and follows Local-First principles.

---

# Future Possibilities

Potential future features include:

- Barcode scanning
- OCR
- Prescription import
- AI assistance
- Medication interaction lookup
- Family sharing
- Wearables
- Health platform integration
- Backup and restore
- Tablet optimization

Inclusion depends on user needs and project direction.

---

# Technical Debt

Technical debt should be:

- Documented
- Prioritized
- Resolved incrementally

Technical debt should never accumulate without visibility.

---

# Release Philosophy

Every release should be:

- Stable
- Tested
- Documented
- Reversible when possible

Incomplete features should remain behind feature flags or be postponed.

---

# Prioritization

When choosing between features, follow the decision priorities defined in:

docs/foundation/philosophy.md

---

# Definition of Done

A milestone is complete only when:

- Implementation is finished.
- Tests pass.
- Documentation is updated.
- Code review is completed.
- No critical issues remain.
- Release criteria are satisfied.

---

# References

- product.md
- architecture.md
- testing.md
- medical-safety.md