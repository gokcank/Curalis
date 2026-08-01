# Versioning

## Purpose

This document defines the versioning strategy for Curalis.

Consistent versioning helps maintain compatibility, simplify releases, and reduce upgrade risks throughout the project's lifecycle.

---

# Principles

Versioning should be:

- Predictable
- Consistent
- Traceable
- Backward-aware

Every released version should be uniquely identifiable.

---

# Semantic Versioning

Curalis follows Semantic Versioning (SemVer).

```
MAJOR.MINOR.PATCH
```

Example:

```
1.4.2
```

---

# Major Version

Increment the major version when introducing breaking changes.

Examples:

- Incompatible database changes
- Major architectural redesign
- Breaking API changes
- Removal of supported functionality

Example:

```
1.x.x → 2.0.0
```

---

# Minor Version

Increment the minor version when adding backward-compatible functionality.

Examples:

- New features
- Additional provider support
- New reminder types
- New export formats

Example:

```
1.3.0 → 1.4.0
```

---

# Patch Version

Increment the patch version for backward-compatible fixes.

Examples:

- Bug fixes
- Security fixes
- UI improvements
- Performance improvements
- Documentation corrections

Example:

```
1.4.2 → 1.4.3
```

---

# Development Versions

Pre-release identifiers may be used during development.

Examples:

```
1.0.0-alpha.1

1.0.0-beta.2

1.0.0-rc.1
```

Definitions:

- Alpha: Early development
- Beta: Feature complete, testing phase
- RC (Release Candidate): Expected release build

---

# Database Versioning

Database schema versions should be managed independently from application versions.

Every schema change should be implemented through migrations.

Schema versions should never be edited manually after release.

---

# Database Migrations

Every migration should be:

- Reproducible
- Idempotent where applicable
- Tested
- Documented

Avoid destructive migrations whenever possible.

---

# Migration Policy

Database upgrades should preserve existing user data.

If a migration cannot preserve data, the impact must be clearly documented before release.

---

# Rollback Strategy

Where practical, migrations should support rollback.

If rollback is impossible, document the reason.

---

# Data Compatibility

New application versions should handle older user data whenever practical.

Compatibility should only be broken when there is a clear long-term benefit.

---

# Provider Versioning

Provider integrations should remain independent of the application version.

Changes in external providers should be isolated within the provider layer.

---

# API Versioning

If future public APIs are introduced, they should use explicit versioning.

Example:

```
/v1/

v2/
```

Breaking API changes should always require a new major API version.

---

# File Format Versioning

Exported files should include a format version when appropriate.

Example metadata:

```
{
  "formatVersion": 1,
  "exportedAt": "...",
  "applicationVersion": "1.2.0"
}
```

This simplifies future import compatibility.

---

# Release Process

Every release should include:

- Version number
- Changelog
- Migration notes (if applicable)
- Known issues
- Upgrade instructions (if required)

---

# Changelog

Every released version should have a corresponding changelog entry.

Entries should describe:

- Added
- Changed
- Fixed
- Removed
- Deprecated
- Security

---

# Deprecation Policy

Deprecated functionality should remain available for a reasonable period whenever practical.

Deprecation should be documented before removal.

---

# Backward Compatibility

Preserve backward compatibility unless:

- Security requires otherwise.
- Long-term maintenance becomes impractical.
- The architectural benefit clearly outweighs the migration cost.

---

# Build Metadata

Build metadata may be appended when necessary.

Example:

```
1.2.0+42

1.2.0+20260801
```

Build metadata should not affect compatibility.

---

# References

- roadmap.md
- database.md
- architecture.md
- decisions.md