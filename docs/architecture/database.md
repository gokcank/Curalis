# Database

## Purpose

This document defines the data architecture of Curalis.

It describes how data is stored, retrieved, updated, cached, and synchronized throughout the application.

This document focuses on data architecture rather than database implementation details.

---

# Goals

The database should be:

- Reliable
- Fast
- Offline-first
- Privacy-focused
- Extensible
- Easy to migrate
- Easy to synchronize in the future

---

# Principles

## Local First

The local database is always considered the primary source of truth.

Remote providers enrich local data.

The application must continue functioning without internet access.

---

## Cache Before Network

Never request information that already exists locally unless it has expired or requires refreshing.

---

## Single Source of Truth

Every piece of data should have one authoritative owner.

Avoid storing duplicate information whenever possible.

---

## Normalization

Avoid unnecessary duplication.

Relationships should be represented explicitly whenever practical.

---

## Data Ownership

Curalis owns:

- User medications
- Reminder schedules
- Inventory
- History
- Notes
- Settings

External providers own:

- Medication metadata
- Ingredient information
- Commercial product information

---

# Data Sources

The application may retrieve medication data from multiple sources.

Example priority:

1. Local Database
2. Custom Backend
3. OpenFDA
4. RxNorm
5. Other providers

Provider priority must remain configurable.

---

# Medication Lifecycle

Medication data follows this lifecycle:

```
Search

↓

Local Database

↓

Provider Search

↓

Cache

↓

User
```

Downloaded medications should be stored locally for future use.

---

# Manual Medications

Users must always be able to create medications manually.

Manual medications are first-class entities.

They should support:

- reminders
- inventory
- notes
- schedules
- attachments

exactly like imported medications.

---

# Automatic Enrichment

If a manually created medication later becomes available from an official provider:

- detect the match
- notify the user
- request confirmation
- merge missing information

Never overwrite user-generated information automatically.

---

# Active Ingredient Fallback

Medication lookup follows the canonical strategy defined in ADR-008-medication-data-source.md.

If only ingredient data is available:

- display ingredient information
- clearly indicate that commercial product information is unavailable

---

# Data Synchronization

Synchronization is not part of the MVP.

However, every entity should be designed with synchronization in mind.

Possible future metadata:

- createdAt
- updatedAt
- deletedAt
- syncStatus
- version

These fields should not dictate the current implementation but future support should remain possible.

---

# Entity Identity

Every entity should have a stable unique identifier.

External provider identifiers should never replace local identifiers.

One entity may contain:

- local identifier
- provider identifier
- barcode
- normalized name

---

# Relationships

The architecture should support relationships between:

Medication

↓

Ingredient

↓

Reminder

↓

Inventory

↓

History

↓

Attachments

Relationships should remain loosely coupled.

---

# Attachments

Users may attach:

- prescription PDFs
- medication photos
- package images
- doctor's instructions

Attachments should remain separate from structured medical data.

---

# Cache Strategy

Downloaded provider data should be cached.

The cache should reduce unnecessary API requests.

The application may refresh cached data periodically.

Users should be able to clear cached provider data without affecting personal information.

---

# Data Integrity

The application should protect against:

- duplicate medications
- orphaned reminders
- invalid references
- corrupted cache
- incomplete imports

Validation should occur before data is committed.

---

# Migrations

Database migrations must:

- preserve user data
- avoid destructive changes
- be backward compatible whenever possible

Breaking migrations require documented justification.

---

# Backup

Future versions may support:

- encrypted backup
- local export
- cloud backup

Backup architecture should remain independent of provider architecture.

---

# Security

Sensitive information must remain encrypted whenever appropriate.

Medical history must never be exposed through logs or temporary storage.

Refer to:

- privacy.md
- security.md

---

# Performance

The database should optimize for:

- fast startup
- fast medication search
- low memory usage
- efficient indexing
- incremental updates

Avoid unnecessary full-table scans.

---

# Anti-Patterns

Avoid:

- duplicate medication records
- storing identical provider data multiple times
- hardcoding provider-specific schemas
- tightly coupling entities
- mixing cache data with user-owned data
- using provider IDs as primary keys

---

# References

- architecture.md
- providers.md
- privacy.md
- security.md
- sync.md
- ADR-001-local-first.md
- ADR-006-room-database.md
- ADR-008-medication-data-source.md