# Changelog

All notable changes to Curalis are documented in this file.

## [1.8.0] — versionCode 16

Data safety for everyday use (ADR-011).

- **Automatic backup.** Optional, off by default: once a day an encrypted backup is saved
  to a folder you choose with the phone's own folder picker, using a backup password you
  set once (kept encrypted with the phone's hardware key). Each backup is a dated file;
  the 30 newest are kept and other files in the folder are never touched. A failed backup
  shows a notification, and the backup screen shows the last backup and any problem.
- **Automatic backup to Google Drive.** When Drive is connected, the same daily backup can
  also go to Curalis's hidden folder in your Drive (a separate switch), again keeping the
  30 newest. Manual Drive backups are dated files too instead of overwriting one file, and
  restoring from Drive lets you pick which backup to load.
- **Safer recovery of an unreadable database.** If the data file is ever damaged, a copy is
  kept before anything is deleted, and you are told on the next start. The recovery screen
  lets you save the data file and asks before resetting.
- **Reminders survive "Force stop".** Opening the app after a force stop sets the reminder
  alarms again; before, they could stay missing for up to a day.
- **Forgot your app PIN?** The lock screen can unlock with the phone's own screen lock and
  then lets you set a new PIN.
- The Google Drive card no longer claims to be automatic, and backup settings, the stored
  password and database rescue copies are kept out of Android's own backup.

## [1.7.0] — versionCode 15

Reminder experience and accessibility.

- **Choose how reminders repeat.** The notification settings set how many times an
  unmarked dose is reminded, the first reminder included (1–5, default 3), and the time
  between reminders (5, 10, 15 or 30 minutes, default 10). The screen says when an unmarked
  dose will count as missed. The defaults keep the old behavior.
- **Choose the reminder sound.** The phone's own sound picker sets the sound for dose
  reminders; the full-screen reminder plays it too, repeated until answered. A button
  returns to the default sound. Quiet hours stay silent.
- **Spoken reminder.** Optional, off by default: the phone says "Time for your medication"
  in the app language when a reminder arrives. Saying the medication name is a separate
  option and never happens while the lock-screen privacy level is Limited or Hidden. It
  stays quiet during quiet hours and when the phone is silent, on vibrate or in do not
  disturb. The settings screen says when the phone has no voice for the app language.
- **Medication shapes.** The medication form offers ten shape icons next to the color
  (round tablet, oval tablet, capsule, drop, bottle, syringe, tube, inhaler, patch,
  sachet). The list, timeline, home screen, calendar and analytics show the medication with
  its shape and color. Without a choice the shape follows the medication form.
- **Screen reader.** Switches announce what they turn on, titles are no longer read twice,
  colors and shapes have names, the selected day is announced, and short buttons such as
  "10 min" on the full-screen reminder are read in full ("Snooze 10 minutes").

## [1.6.0] — versionCode 14

Small conveniences.

- **Repeat reminders say what changed.** The first reminder stays as before; a repeat says
  the dose is not marked yet, and the last one says the dose will count as missed if it
  stays unmarked. They follow the lock-screen privacy level.
- **Expiry date from the box's DataMatrix code.** Scanning the code on the box fills an
  empty expiry date. An expiry date in the past shows a warning on the form; saving is
  still allowed.
- **Search ignores Turkish letter case.** "şurup", "ŞURUP" and "Şurup" find the same
  medications, and i/İ/ı/I no longer break a search, both in the medication dictionary and
  in My Medications. When the dictionary finds nothing, close spellings are offered under
  "Did you mean?". The dictionary is rebuilt (version 3) with the same list.
- **Add a box or enter the stock from the medication list.** Tapping a medication's stock
  label offers "Add a box" (adds the starting stock) or "Enter stock"; both are recorded in
  the stock history.
- **Pick an emergency contact from the address book.** The emergency contact form has a
  "Pick from contacts" button that opens the phone's own contact picker; the app does not
  ask for contacts permission.
- **Share the medication list as text.** My Medications has a share button that sends the
  active medications (name, frequency, times, dose, meal instruction) as plain text.
  Archived and suspended medications are left out.
- **Evening summary.** Optional, off by default: at the chosen time (21:00 unless changed),
  one notification says how many of today's doses are still unmarked; tapping it opens the
  timeline. Nothing arrives when every dose is marked. It has its own notification category.

## [1.5.0] — versionCode 13

Reminder reliability and privacy.

- **Warning when reminders may not arrive.** The home screen shows a banner when a dose's
  reminder never went off in the last 24 hours, or when notifications, the exact alarm
  permission or the battery exemption are turned off. Tapping it opens Reminder
  Troubleshooting, which now also lists the notification permission.
- **Doses missed while the phone was off.** After a restart, one notification says how many
  doses were missed while the phone was off; tapping it opens the daily timeline.
- **Time zone change explained.** After a time zone change the home screen shows the
  previous and new zone, the time difference and every medication's reminder times, which
  keep following the local clock.
- **Three lock-screen privacy levels: full, limited, hidden.** Limited leaves the medication
  name out of reminder and low-stock notifications ("Medication Time" and the time only);
  hidden keeps them off a secure lock screen. In both, the full-screen reminder shows only
  the dose time while the phone is locked. The old "hide medication name" switch becomes
  limited; it used to leave the name visible on most phones.

## [1.4.2] — versionCode 12

Bug fixes.

- **Stock now follows every taken dose.** Stock used to drop only when a dose was taken
  from the notification or the full-screen reminder; taking it from the timeline, the home
  screen or the widget changed nothing and sent no low-stock warning. Every path now uses
  the same rule, and undoing a taken dose puts the amount back (shown as "Dose undone" in
  the stock history).
- **Morning summary, measurement reminders and the time-zone notice appear again.** Their
  notification category was never created, so Android dropped them silently.

## [1.4.1] — versionCode 11

Fixes found while trying 1.4.0 on a phone.

- **PDF report language** — the report options ask for Turkish or English, defaulting to
  the app's language. Frequencies and measurement types now show their names instead of
  internal codes such as DAILY.
- A dose's taken time can no longer be set in the future, which could wrongly mark it as
  taken late. "On time" is only offered once the dose is due, and for a rescheduled dose it
  means its new time.
- The home screen's monthly adherence updates while the screen is open; doses answered
  after opening it used to be ignored until the app was reopened.
- Ending the treatment, suspending or deleting a medication also closes its reminder that
  is still in the notification shade.
- With large font sizes the timeline's "Taken: …" time wraps instead of being cut off, and
  the source label on medication cards is shortened to "Verified" / "Manual".
- CI: the Gradle wrapper validation step no longer times out.

## [1.4.0] — versionCode 10

Changes since 1.3.2 (versionCode 9).

### Upgrade note

- Install over 1.3.x; your data is kept (the database moves to version 24).
- Connect Google Drive once more on the Backup screen; existing Drive backups still restore.
- Medications whose treatment duration has already passed stop reminding. If you are still
  taking one, edit its duration.

### New

- **Change a dose time by tapping it** on the medication screen.
- **Reschedule a single dose** to another day and time from the Daily Timeline. The dose
  stays in its place showing the new time, and its reminder comes at the new time.
- **The time a dose was taken** is now recorded when it is taken from the notification or
  the full-screen reminder, and can be corrected later by tapping it on the timeline.
- **The full-screen reminder lists every dose due today** that is still unanswered, each
  with Take, snooze and Skip, plus "Take all". Several medications at the same minute no
  longer replace each other.
- **Late doses** — a dose taken more than a chosen time after its reminder (30 min, 1 h by
  default, or 2 h, in Notification settings) is labelled "Taken late" and counts as half a
  dose in every adherence figure. A day with a late dose is no longer shown as perfect.
- **PDF report** — a per-medication summary of taken, late, skipped and missed doses,
  adherence and average delay. Long reports continue on new pages instead of being cut off.
- **Compact medication cards** — dose times, frequency and source fit on one line; tap a
  card for the rest and for Edit, Suspend, End treatment and Delete. "Suspended" and
  "Treatment ended" labels are always visible.
- **Treatment duration is binding** — reminders stop after its last day. "End treatment"
  moves a medication to the Archive, keeping its history.
- **Google Drive backup uses Google's current Authorization API** instead of the deprecated
  Google Sign-In. Disconnecting also revokes the permission.

### Fixes

- Moving a dose time "from today" removes today's unanswered dose at the old time, which
  used to stay and later count as missed.
- The full-screen reminder no longer asks to unlock the phone (biometric prompt); its
  actions work on the lock screen.
- Suspending or archiving a medication removes its upcoming doses. They used to stay on the
  timeline for 30 days and could be counted as missed after resuming.
- A rescheduled dose is no longer marked as missed when the phone restarts or the app is
  updated before its new time.
- Placebo days of cyclic medications beyond the next 30 days are shown as placebo.
- Editing a medication no longer resets its start date, which restarted the treatment
  duration and shifted every-X-days and cyclic schedules.

## [1.3.2] — versionCode 9

- Exact alarms now use USE_EXACT_ALARM on Android 13+, granted at install and not
  revocable, so reminders no longer depend on the user approving "Alarms & reminders"
  (Android 12 keeps the approval-based permission). Possible because Curalis is no
  longer distributed through Google Play.
- Onboarding also asks for the full-screen notification permission (Android 14+ may keep
  it off, which silently turns full-screen reminders into plain notifications) and opens
  the battery optimization exemption dialog directly; only missing permissions are listed.
- The reminder troubleshooting screen checks the full-screen permission too.

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
a reference app — every item below was designed and implemented from scratch against
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
