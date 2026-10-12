# Changelog

All notable changes to Curalis are documented in this file.

## [1.3.1] — versionCode 8

- Timeline slot start hours (Settings → Zaman Çizelgesi Dilimleri) are now picked from
  dropdowns instead of rows of chips; the six evening options no longer squeeze the
  hours onto two lines.

## [1.3.0] — versionCode 7

Changes since 1.2.2 (versionCode 6).

### Upgrade note

- **On-device database encryption (SQLCipher) has been removed.** Installing 1.3.0
  over a 1.2.x install starts with an empty database, so take a backup in 1.2.x first
  and restore it after a clean install. Backups themselves are still encrypted with
  your password. The APK shrank from about 14 MB to 6 MB.

### New

- **Fill a medication by scanning its box** — the scan button in the medication name
  field reads the barcode or the İTS karekod (DataMatrix) and looks the product up in
  the official TİTCK list. A match fills the name and active ingredient and locks them
  until the barcode is cleared; if a different name is already entered, Curalis asks
  first. Codes not in the list only keep the number.
- **Daily Timeline from the home screen** — a "Bugünün Özeti" link on the Today's
  Pillbox card opens it again.

### Fixes

- Backups now include every kind of data (symptoms, daily notes, emergency contacts,
  stock history and measurement reminders were missing). Older backups still restore
  without wiping the tables they never contained.
- Alarms are rescheduled right after a restore, and deleted, archived or suspended
  medications no longer get reminders.
- Changing a dose time "from tomorrow" keeps today's dose at its old time, with its own
  alarm.
- TİTCK list updates now actually reach installed apps; previously the first copied
  list stayed in place while the About screen showed the new date.
- The onboarding screen now shows the exact-alarm permission as granted after returning
  from system settings.
- The medication name label no longer reads "İlaç Adı * (API'de ara) *", and the
  medication list search box has its own hint.

### Other

- TİTCK medication list updated to 09.10.2026, with the generator script added under
  `tools/`.
- Removed the "Share the App" row from Settings and the encrypted database export.
- Tests for every database migration path (version 7 → 23) and for barcode parsing.
- Removed dead code and replaced deprecated APIs.

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
