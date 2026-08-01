# Security

## Purpose

This document defines the security principles, requirements, and practices that protect Curalis, its users, and their data.

Security is a fundamental requirement and must be considered throughout the entire software lifecycle.

Every feature should be designed with security in mind from the beginning.

---

# Goals

The security architecture should provide:

- Confidentiality
- Integrity
- Availability
- Reliability
- Least Privilege
- Defense in Depth

---

# Security Principles

## Security by Design

Security should be built into the architecture.

Never rely on security patches to compensate for insecure design.

---

## Least Privilege

Every component should receive only the permissions it requires.

Do not request permissions that are unnecessary.

---

## Fail Securely

When an unexpected failure occurs, the application should fail safely.

Never expose sensitive information because of an error.

---

## Zero Trust

Never assume that any external data is trustworthy.

Every external input must be validated.

Examples include:

- API responses
- User input
- Files
- QR codes
- Barcodes
- OCR results

---

# Authentication

The MVP does not require user accounts.

Future authentication systems should support:

- strong passwords
- passkeys
- biometric authentication
- multi-factor authentication

Authentication should remain optional unless cloud synchronization is enabled.

---

# Authorization

Every future cloud feature should verify user ownership before granting access.

No user should ever gain access to another user's medical information.

---

# Encryption

Sensitive data should be encrypted whenever appropriate.

Examples include:

- backups
- exported files
- cloud storage
- authentication tokens

Do not implement custom cryptography.

Use well-established platform APIs.

---

# Local Storage

Sensitive data should remain inside the application's private storage whenever possible.

Avoid unnecessary external storage.

---

# Secrets

Never hardcode:

- API keys
- tokens
- passwords
- encryption keys
- signing keys

Secrets must never be committed to version control.

---

# Network Security

All network communication should use secure transport.

Reject insecure connections whenever possible.

Certificate validation must never be disabled in production.

---

# Provider Security

External providers should be treated as untrusted systems.

Provider responses must always be:

- validated
- sanitized
- mapped

Never expose raw provider responses directly to the application.

---

# Input Validation

Validate all external input.

Examples:

- text
- numbers
- files
- URLs
- provider responses

Reject invalid data as early as possible.

---

# File Handling

User-provided files should never be trusted automatically.

Validate:

- file type
- file size
- readability

Reject unsupported files.

---

# Backup Security

Future backups should support:

- encryption
- integrity verification
- secure restoration

Backups should never be publicly accessible.

---

# Notification Security

Notifications should avoid exposing sensitive information on the lock screen unless explicitly allowed by the user.

---

# Clipboard

Avoid copying sensitive medical information to the clipboard automatically.

Clipboard usage should always require explicit user action.

---

# Logging

Sensitive information must never be logged, including but not limited to:

- Personal data
- Medication records
- Health information
- User notes
- Authentication tokens
- API keys
- Passwords
- Session identifiers

Production logs should remain minimal.

---

# Crash Reports

Crash reports must never include medical information.

Users should be able to disable crash reporting.

Crash reporting requires explicit consent.

---

# Dependency Security

All dependencies should be:

- actively maintained
- regularly updated
- reviewed before adoption

Avoid abandoned libraries.

Remove unused dependencies.

---

# Updates

Security updates should be prioritized over feature development.

Critical vulnerabilities should be addressed immediately.

---

# Secure Defaults

The safest behavior should always be the default behavior.

Users may choose less restrictive settings only through explicit configuration.

---

# Future Cloud Security

Future synchronization should include:

- authenticated communication
- encrypted transport
- encrypted storage
- conflict validation
- secure session management

---

# Future AI Security

Future AI integrations must never receive user medical data without explicit user consent.

AI requests should be minimized to only the information required.

---

# Anti-Patterns

Never:

- hardcode secrets
- disable certificate validation
- trust external input
- expose stack traces to users
- store tokens insecurely
- log sensitive information
- invent custom encryption

---

# References

- architecture.md
- database.md
- providers.md
- privacy.md
- medical-safety.md
- sync.md