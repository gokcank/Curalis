# Synchronization

## Purpose

This document defines the synchronization architecture for future versions of Curalis.

Cloud synchronization is intentionally excluded from the MVP. However, the application's architecture should remain fully prepared for future synchronization capabilities.

Synchronization should be implemented without requiring significant architectural changes.

---

# Goals

The synchronization system should be:

- Optional
- Secure
- Reliable
- Predictable
- Conflict-resistant
- Privacy-focused

---

# Principles

## Local-First

The local database is always the primary source of truth.

Users must be able to use Curalis without creating an account or connecting to cloud services.

Cloud synchronization enhances the experience but must never become a requirement.

---

## User Control

Synchronization must always require explicit user consent.

Users should decide:

- Whether synchronization is enabled.
- Which data is synchronized.
- When synchronization occurs.

---

## Transparency

Synchronization should never happen silently.

Users should always know:

- What is being synchronized.
- When synchronization occurred.
- Whether synchronization succeeded.
- Whether conflicts were detected.

---

# Synchronization Model

Future synchronization should follow this flow:

```
Application

↓

Local Database

↓

Synchronization Engine

↓

Cloud Provider
```

Business logic should never communicate directly with cloud services.

---

# Synchronization Scope

Potential synchronized data:

- Medications
- Reminders
- Inventory
- Medication history
- Notes
- Attachments
- Settings

Provider cache should not be synchronized.

---

# Offline Support

Offline functionality remains fully supported.

Changes made while offline should synchronize automatically when connectivity returns.

---

# Conflict Resolution

Conflicts should be detected automatically.

Possible strategies include:

- Last write wins
- Manual resolution
- Field-level merge

The selected strategy should be consistent throughout the application.

---

# Deletions

Deletion events should be synchronized explicitly.

Deleted records should not silently reappear after synchronization.

---

# Entity Identity

Every synchronized entity should have:

- Local identifier
- Synchronization identifier
- Version information

Identifiers must remain stable.

---

# Versioning

Entities should support version tracking.

Potential metadata includes:

- createdAt
- updatedAt
- deletedAt
- version
- syncStatus

These fields support synchronization but should not affect business logic.

---

# Synchronization Status

Entities may have synchronization states such as:

- Local Only
- Pending Upload
- Synced
- Conflict
- Failed

These states should be visible to the synchronization engine without affecting normal application usage.

---

# Error Recovery

Synchronization failures should never result in data loss.

Failed synchronization attempts should be retryable.

Users should receive clear feedback when manual intervention is required.

---

# Attachments

Attachments should synchronize independently from structured data.

Large files should not block synchronization of smaller records.

---

# Security

All synchronized data should use secure transport.

Sensitive information should remain encrypted where appropriate.

Authentication should be required before cloud synchronization begins.

---

# Privacy

Synchronization should respect all rules defined in:

- privacy.md
- security.md

No additional data should be uploaded without explicit user consent.

---

# Future Providers

The synchronization architecture should remain independent of any cloud provider.

Potential providers may include:

- Custom backend
- Self-hosted server
- Third-party cloud services

Changing providers should not require changes to business logic.

---

# Performance

Synchronization should:

- Minimize network usage.
- Transfer only changed data.
- Avoid unnecessary uploads.
- Resume interrupted operations when possible.

---

# Anti-Patterns

Never:

- Require cloud synchronization.
- Upload data without consent.
- Lose local data because of synchronization failures.
- Block the application while synchronization is running.
- Couple business logic to a specific cloud provider.

---

# References

- architecture.md
- database.md
- privacy.md
- security.md
- ADR-001-local-first.md