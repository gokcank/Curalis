# Privacy

## Purpose

This document defines how Curalis collects, stores, processes, and protects user data.

Privacy is a core product value, not an optional feature.

Every design and engineering decision must consider its impact on user privacy.

---

# Principles

Curalis follows these privacy principles:

- Privacy by Design
- Privacy by Default
- Data Minimization
- User Ownership
- Transparency
- Explicit Consent

---

# Privacy Philosophy

Users own their medical information.

Curalis exists to organize that information—not to monetize it.

The application should require the minimum amount of personal information necessary to function.

---

# Data Ownership

All user-generated data belongs to the user.

Examples include:

- Medications
- Reminders
- Medication history
- Inventory
- Notes
- Attachments
- Settings

Curalis must never claim ownership of user data.

---

# Data Collection

Collect only the information required for the feature being used.

Never collect information "just in case."

Avoid optional data collection unless it provides clear user value.

---

# Personal Information

The application should function without requiring personally identifiable information whenever possible.

Examples:

- Name
- Email address
- Phone number
- Address
- Date of birth

These should not be required for the MVP.

---

# Medical Information

Medical information is considered highly sensitive.

Examples:

- Medication lists
- Treatment schedules
- Medication history
- Medical notes
- Attachments
- Reminder history

This information must always receive the highest level of protection.

---

# Local Storage

User data should remain on the user's device.

Local storage is the default behavior.

Cloud storage is optional and must never be enabled automatically.

---

# Cloud Synchronization

Future synchronization features must require explicit user consent.

Users should always know:

- what data is uploaded
- where it is stored
- why it is stored

---

# Analytics

Medical information must never be included in analytics.

Examples that must never be transmitted:

- Medication names
- Dosages
- Reminder schedules
- Medical history
- Notes

Anonymous usage metrics may be collected only with explicit user consent.

---

# Telemetry

Hidden telemetry is prohibited.

Background data collection must never occur without informing the user.

---

# Third-Party Services

Third-party services should be minimized.

Before integrating any external service, evaluate:

- privacy impact
- data sharing
- long-term reliability

Services requiring unnecessary user data should be avoided.

---

# API Requests

Only the information required to perform a medication lookup should be transmitted.

User-specific medical history must never be sent to medication providers unless explicitly required and approved by the user.

---

# Attachments

Medical attachments should remain private.

Examples:

- Prescriptions
- Photos
- PDFs
- Medical documents

Attachments should not leave the device without explicit user action.

---

# Notifications

Notifications should minimize sensitive information.

Users should be able to choose:

- full notification content
- limited notification content
- hidden notification content

---

# Logging

Application logs must comply with the logging restrictions defined in engineering/security.md.

---

# Export

Users should always be able to export their own data.

Future export formats may include:

- JSON
- CSV
- PDF

Exports belong to the user.

---

# Deletion

Users should always be able to permanently delete their data.

Deletion should remove:

- local records
- cached provider data
- attachments
- backups (when applicable)

---

# Consent

Privacy-sensitive features require explicit consent.

Examples:

- cloud sync
- analytics
- crash reporting
- AI services

Consent should be:

- informed
- reversible
- specific

---

# Transparency

Users should always know:

- what data exists
- where it is stored
- how it is used
- when it leaves the device

---

# Compliance

The architecture should remain compatible with modern privacy regulations, including:

- GDPR
- KVKK

Compliance should be considered during feature design rather than added later.

---

# Anti-Patterns

Never:

- sell user data
- share medical data without consent
- enable analytics by default
- upload attachments automatically
- collect unnecessary personal information
- store secrets insecurely

---

# References

- product.md
- medical-safety.md
- security.md
- sync.md