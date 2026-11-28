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

Current implementation (ADR-011, accepted 2026-11-25; destinations and password changed by ADR-013, accepted
2026-11-28):

- Encrypted backups use one format: the export is encrypted with the user's backup password (PBKDF2-HMAC-SHA256 key
  derivation, AES-GCM, which also verifies integrity, prefix `CURALIS_ENC_V1:`). A wrong password changes nothing.
- The manual file export always asks for a password and is always encrypted (the file can go anywhere).
- Google Drive backups (automatic and manual) are **unencrypted by default** (ADR-013): the plain export in Drive's
  hidden `appDataFolder` (`drive.appdata` scope only), protected by the user's Google account but readable on Google's
  servers. The app says so next to the "Encrypt backups" option. With that option on, they use the encrypted format
  with the stored password and no prompt; restoring tries the stored password first and asks only when it is missing
  or does not open the backup (the correct password is then stored and encryption stays on).
- A restore runs in one database transaction: if anything fails, the existing data stays as it was.
- The backup password is stored on the device, encrypted with a non-exportable Android Keystore key, in a preferences
  file that is excluded from Android Auto Backup and device transfer. It is never logged or shown.
- Before a corrupt database is deleted, or before the user resets an unreadable one, its files are copied to
  app-private storage.
- A forgotten app PIN can be reset only through the phone's own screen lock ("Forgot PIN?" on the lock screen): on
  Android 11+ the system prompt accepts the screen lock or strong biometrics, on Android 8–10 the system's own confirm
  screen is shown. After success the app opens the new-PIN entry. Without a phone screen lock there is no other way in
  (only clearing the app's data), and the app says so.
- Android Auto Backup and device transfer never include the database, the database rescue copies, the automatic
  backup settings or the stored backup password (`res/xml/backup_rules.xml`, `data_extraction_rules.xml`).
- Automatic backup goes to Google Drive only and can be turned on only while Drive is connected; disconnecting Drive
  turns it off. Backups are dated files (`curalis_auto_<date>_<time>.json`); only these are pruned to the 30 newest.
  The Drive permission is requested in the background with the connected account and never shows a dialog there (if
  Google asks for consent, the user is notified instead). Manual Drive backups are dated files too
  (`curalis_drive_manual_<date>_<time>.json`) and are never overwritten; the older single file
  `curalis_drive_backup.json` stays readable. Restoring lists every backup, newest first.

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