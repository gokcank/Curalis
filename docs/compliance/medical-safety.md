# Medical Safety

## Purpose

This document defines the medical safety requirements that govern the design, implementation, and behavior of Curalis.

Medical safety takes precedence over usability, convenience, feature completeness, and engineering preferences.

Every contributor and AI agent must follow these rules without exception.

This document defines all medical safety requirements. When implementing features that may affect medical functionality, this document becomes mandatory.

---

# Scope

These rules apply to:

- User interface
- Business logic
- Notifications
- Medication information
- AI-generated content
- External providers
- Future cloud services
- Future AI integrations

---

# Safety Priority

Medical safety is the highest-priority document in the project.

If any implementation conflicts with this document, the implementation must be changed.

---

# Core Philosophy

Curalis helps users manage medications.

Curalis does not make medical decisions.

The application exists to improve organization, not to replace healthcare professionals.

---

# Fundamental Rule

Never present medical assumptions as facts.

Whenever certainty cannot be guaranteed, communicate uncertainty clearly.

---

# What Curalis May Do

The application may:

- Store medications.
- Display medication information.
- Display provider information.
- Display reminder schedules.
- Record medication history.
- Track inventory.
- Display official medication documentation.
- Show provider-reported side effects.
- Show provider-reported contraindications.
- Display provider-reported interactions.
- Organize personal medication records.

---

# What Curalis Must Never Do

The application must never:

- Diagnose diseases.
- Recommend treatments.
- Recommend medications.
- Recommend dosage changes.
- Recommend stopping medications.
- Recommend starting medications.
- Recommend replacing medications.
- Interpret laboratory results.
- Predict medical outcomes.
- Guarantee medication safety.
- Override healthcare professionals.

---

# Dosage Safety

Dosage recommendations are prohibited.

The application may display:

- Manufacturer information
- Official prescribing information
- Provider data

The application must never calculate or recommend personalized doses.

---

# Medication Changes

The application must never encourage users to:

- skip medication
- double medication
- stop medication
- change medication
- replace medication

without explicit direction from a qualified healthcare professional.

---

# Drug Interactions

If interaction data becomes available:

The application may display:

- official interaction information
- severity classifications
- provider references

The application must never determine whether a medication combination is safe.

---

# Side Effects

Side effects may only originate from trusted providers.

Never generate side effects.

Never estimate side effects.

Never rank side effects by probability unless supplied by the provider.

---

# Allergies

The application may allow users to record allergies.

The application must never determine allergy risks automatically.

---

# Pregnancy

The application must never provide pregnancy-related recommendations.

Provider information may be displayed without interpretation.

---

# Children

The application must never provide pediatric dosage recommendations.

---

# Elderly Patients

The application must never recommend dosage adjustments based on age.

---

# Chronic Conditions

The application must never alter recommendations based on user conditions.

---

# Emergency Situations

The application must never attempt to determine whether an emergency exists.

If the user explicitly reports a possible medical emergency, the application should encourage immediate contact with local emergency medical services or an appropriate healthcare professional.

---

# AI Usage

AI may:

- summarize official documentation
- simplify medical terminology
- improve readability

AI must never:

- generate medical advice
- invent medication information
- invent contraindications
- invent interactions
- invent dosage recommendations

---

# Data Sources

Medication information should originate from:

- Official providers
- Trusted medical databases
- User-entered information

Every medication record should clearly indicate its source.

---

# Manual Medications

User-created medications are valid records.

However, manually entered medical information must never be presented as verified medical information.

---

# Missing Information

Missing information should never be replaced with generated information.

Display:

"Information unavailable."

instead of guessing.

---

# Medical Terminology

Medical terminology should remain accurate.

Simplification is allowed.

Distortion is prohibited.

---

# Reminder Safety

Reminders should never imply that medication was taken.

Only the user can confirm medication intake.

---

# Medication History

History should distinguish between:

- Scheduled
- Medication Intake
- Missed
- Skipped

The application must never assume medication adherence.

---

# Transparency

Users should always know:

- where information comes from
- whether information is verified
- whether information was manually entered
- whether information was imported

---

# User Control

Users remain responsible for:

- medication decisions
- treatment plans
- dosage
- healthcare choices

Curalis only assists with organization.

---

# Error Handling

Medical information should fail safely.

If provider data cannot be verified:

Display limited information instead of potentially incorrect information.

---

# Future AI Features

Future AI features must remain assistive.

They must never become decision-making systems.

---

# Anti-Patterns

Never implement:

- AI diagnoses
- AI prescriptions
- AI dosage calculations
- automatic medication substitutions
- automatic interaction conclusions
- personalized treatment recommendations
- medical certainty without authoritative sources

---

# References

- product.md
- architecture.md
- providers.md
- privacy.md
- security.md