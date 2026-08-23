# Changelog

All notable changes to Curalis are documented in this file.

## [1.1.0] — versionCode 3

A large feature round based on a detailed feature-by-feature comparison against
Medisafe — every item below was designed and implemented from scratch against
Curalis's own architecture, not ported or copied.

### Highlights

- **Home screen widget** — a "Bugünün Kutusu" (Today's Pillbox) widget you can add
  to your home screen, showing today's doses and letting you mark them taken with
  one tap, without opening the app (built with Jetpack Glance).
- **Local database encryption** — the on-device Room database is now encrypted
  (SQLCipher), with the passphrase generated at runtime and stored via the Android
  Keystore. Previously only the optional Google Drive backup was encrypted.
- **Symptom tracking** — log Pain, Nausea, and Fatigue on a 0–10 subjective severity
  scale, separate from objective Vitals measurements.
- **Daily Notes** — a free-form health journal, one entry per day, with an optional
  5-point mood indicator.

### Medication management

- Simplified the Add Medication form — optional fields are now tucked behind an
  "Almost done!" checklist instead of one long form.
- Expanded the medication form and dosage-unit dictionaries (injection pen, spray,
  suppository, gel, lozenge, sachet, and more).
- Editing or deleting a medication's reminder time now asks whether the change
  applies to just that dose or to all future doses.
- Added a "Suspend" state (pause a treatment without deleting or archiving it), a
  treatment-duration field, and separate Active/Archive tabs.
- Added an Rx (prescription) number field.
- Added placebo-pill support to cyclic dosing schedules (e.g. 21 active days / 7
  rest days) — a reminder still fires on rest days, but those doses are excluded
  from adherence percentages.
- Added a large, live-updating icon/color preview to the add/edit medication form.

### Reminders & notifications

- "Mark as taken" now asks whether you took it now, on time, or at a manually
  chosen time, and a taken dose can be reverted (Un-Take).
- Added bulk actions (Take All / Skip All / Snooze All) to the Daily Timeline.
- Expanded skip reasons from 4 to 7 (cost concerns, side effects, etc.).
- Added a notification popup mode (always / never / only when screen is on) with a
  forced-override on the final missed-dose attempt.
- Expanded the Reminder Troubleshooting wizard with battery-optimization and
  manufacturer-specific (Samsung/Xiaomi/Huawei) step-by-step guidance.
- Added a proactive "take your meds with you" morning reminder with a separate
  Weekend Mode time.
- Timeline slot boundaries (Morning/Afternoon/Evening/Night) are now configurable
  in Settings.
- Reminders now automatically reschedule to the new local time when the device's
  timezone changes (e.g. after a flight), with a notification explaining why.
- Each Vital type (blood pressure, weight, blood sugar, ...) can now have its own
  independent "don't forget to measure this" reminder, with a custom time and days.

### Calendar & appointments

- Added a weekly navigation strip to the Daily Timeline.
- Added separate "Upcoming"/"Completed" tabs to the appointment list.
- Added 8-hour and 12-hour options to appointment reminder lead time.
- Added an appointments tab to the doctor detail screen.
- Added a simple "Emergency Contact" person type, selectable alongside doctors when
  assigning an appointment.

### Vitals

- Expanded the number of vital types to 13 (added A1C, HDL/LDL Cholesterol,
  Triglycerides, Body Fat, and Step Count).
- Added a kg/lb toggle for weight entries.

### Reports

- Added a medication filter to the PDF health report.
- The shared PDF's content (adherence summary / medication list / vitals) is now
  selectable.
- Added a summary percentage badge to the report preview.
- Added a "Yenileme Geçmişi" (Refill History) screen showing stock-change history
  across all medications, previously only visible per-medication inside the edit
  screen.

### Support & discoverability

- Added a Help Center / FAQ screen to Settings.
- Added a "Share App" action.
- Made the existing Google Drive backup feature more visible in Settings.

---

Under the hood: the Room database schema went from version 18 to version 22 across
this round (each step ships its own migration, so no user data is lost), and every
item above was verified with real device interaction before being merged.

## [1.0.0] — versionCode 2

Initial public release.
