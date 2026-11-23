# Reminders

## Purpose

This document defines how reminders and notifications behave throughout Curalis.

Notifications are one of the application's core features and must remain reliable, predictable, and respectful of user preferences.

Reminder reliability takes priority over visual presentation.

---

# Goals

Notifications should be:

- Reliable
- Timely
- Configurable
- Respectful
- Accessible
- Battery-efficient

---

# Principles

## Reliability First

Medication reminders should trigger as reliably as the operating system allows.

Missed reminders caused by application logic are unacceptable.

---

## User Control

Users control notification behavior.

The application should never make assumptions about reminder preferences.

Every configurable behavior should remain optional.

---

## Predictability

Notifications should always behave consistently.

Users should never wonder why a reminder appeared—or failed to appear.

---

## Reminder Lifecycle

Every reminder follows the same lifecycle.

```
Scheduled

↓

Pending

↓

Delivered

↓

Acknowledged
        │
        ├── Taken
        ├── Skipped
        ├── Snoozed
        └── Missed
```

---

# Reminder Types

The system may support:

- One-time reminders
- Daily reminders
- Weekly reminders
- Monthly reminders
- Interval reminders
- As-needed (PRN) reminders
- Treatment plans

Each reminder type should share the same interaction model whenever possible.

---

# Reminder States

A reminder may exist in one of the following states:

- Scheduled
- Delivered
- Taken
- Skipped
- Missed
- Snoozed
- Cancelled

State transitions should be explicit.

---

# Notification Content

Notifications should include only the information necessary for the user to identify the reminder.

Users should be able to choose between:

- Full content
- Limited content
- Hidden content

Sensitive information should remain protected on the lock screen.

How Curalis applies the levels to medication reminders:

- **Full:** notifications and the full-screen reminder show the medication name.
- **Limited:** the reminder notification never contains the medication name, only "Medication Time" and the dose time. This holds whether the phone is locked or not. Android hides "private" notification content on the lock screen only when the phone's own sensitive-content setting is off, and on most phones it is on, so the name is left out of the notification itself.
- **Hidden:** with a screen lock (PIN, pattern, password) the reminder notification does not appear on the lock screen at all; once unlocked it shows the name.
- **Limited and Hidden:** while the phone is locked, the full-screen reminder shows only the dose time, with no medication name or dose. The name appears as soon as the phone is unlocked. Take, Snooze and Skip work at every level.
- **Previous setting:** users who had the old "hide medication name on lock screen" switch on move to Limited.
- **Low-stock notifications** follow the same levels: Limited leaves the medication name out ("One of your medications is running out"), Hidden keeps the notification off a secure lock screen.

---

# Confirmation

Displaying a reminder does not imply medication was taken.

Medication intake must always require explicit confirmation by the user.

---

# Snooze

Users may temporarily postpone reminders.

The snooze duration should be configurable.

Snoozing must never permanently modify the original schedule.

---

# Missed Reminders

If a reminder expires without user interaction, it should become **Missed**.

The application must never automatically mark it as **Taken**.

Before that, an unanswered reminder is shown again. In the notification settings the user chooses how many times the reminder is shown in total, the first one included (1–5, default 3), and the time between them (5, 10, 15 or 30 minutes, default 10). One interval after the last reminder, a still unmarked dose becomes **Missed**; with the defaults that is 30 minutes after the first reminder. The settings screen states this time. Changing the settings affects doses already in progress at their next check; a dose that is past the new count becomes Missed at that check. The late-dose threshold and adherence rules do not depend on these settings.

The repeats read differently from the first reminder: a repeat says the dose is not marked yet, and the last one says that, if it stays unmarked, the dose will count as missed. They follow the lock-screen privacy level like the first reminder.

## Evening Summary

An optional evening summary (off by default, time chosen by the user, 21:00 unless changed) posts one notification if any of today's doses are still unmarked at that time: missed, rung but unanswered, or never delivered. Taken, skipped and snoozed doses do not count; doses scheduled after the summary time do not count yet. Placebo days, as-needed medications and suspended or archived medications are left out. If every dose is marked, nothing is posted.

The summary uses the same dose list as the timeline, has its own notification category, shows only the count (never medication names), opens the timeline when tapped, follows the lock-screen privacy level and stays silent during quiet hours. It is rescheduled after a reboot, an app update and a time-zone change.

---

# Repeating Reminders

Repeating reminders should continue independently of previous reminder states unless the schedule explicitly depends on user confirmation.

---

# Inventory Integration

When enabled, inventory should only decrease after the user confirms medication intake.

Skipped or missed reminders must not affect inventory.

The same rule applies on every path where a dose can be marked as taken (notification, full-screen reminder, timeline, home screen, widget, "take all"):

- Amount: the dose written for the reminder's time when it is a positive whole number (e.g. "2"); otherwise (fraction, text, empty) one unit. Inventory is stored as a whole number.
- Inventory never goes below zero; an empty inventory is left unchanged.
- Undoing a taken dose restores the same amount and records it in the inventory history ("Dose undone").
- When refill alerts are enabled and the new inventory is at or below the threshold (default 5), a low-inventory notification is shown.

---

# Notification Channels

Notification categories should remain separate.

Examples:

- Medication reminders
- Refill reminders
- Low inventory alerts
- Treatment completion
- Informational notifications

Users should be able to configure each category independently.

## Reminder Sound

In the notification settings the user can choose the reminder sound with the phone's own sound picker (notification sounds; "silent" is not offered, quiet hours cover that). The phone's default notification sound is used until a sound is chosen. A "Use the default sound" button returns to it, because not every phone's sound picker offers that option. The chosen sound is also played by the full-screen alarm, repeated until the user responds; without a chosen sound the full-screen alarm keeps the phone's default alarm sound.

Android does not let an app change a category's sound after creating it. So each sound change opens a new medication-reminder category with the chosen sound and deletes the previous one: the system settings always show exactly one medication-reminder category. Changes the user made to the old category in the system settings (for example vibration) do not carry over, and reminders still shown from the old category are removed. The quiet-hours category stays silent and is not affected.

## Spoken Reminder

An optional spoken reminder (off by default) makes the phone say "Time for your medication" in the app language when a dose reminder or one of its repeats arrives, using the phone's own text-to-speech engine. Saying the medication name is a separate option, also off by default, and the name is never said while the lock-screen privacy level is Limited or Hidden. The phone does not speak during quiet hours, or when it is on silent, vibrate or do not disturb. Several doses due at the same moment are announced once when names are not said. The full-screen alarm waits for the speech before playing its sound, and answering the reminder stops the speech.

If the phone has no text-to-speech engine or no voice for the app language, the setting says so and links to the phone's voice settings; reminders are then shown and sound as usual, without speech. A "Play a sample" button lets the user hear it.

---

# Quiet Hours

Users may define quiet hours.

If enabled, reminder behavior during quiet hours should follow user preferences.

Critical reminder handling should remain configurable.

---

# Time Zones

Reminder schedules should remain consistent when the device time zone changes.

The scheduling strategy should be documented and applied consistently.

Strategy: reminder times follow the local clock. A dose set for 08:00 goes off at 08:00 in whatever time zone the device is in. When the time zone changes, future reminders and alarms are rebuilt for the new local time, and a notification tells the user.

The home screen then shows an explanation once. It lists the previous and new time zone, the time difference, and the reminder times of each medication that sets alarms. Nothing is changed by it. The system does not report the previous zone, so the app remembers the last zone the user saw. If the zone changes again before the explanation is seen, it still starts from that remembered zone. If the user is back in the starting zone, there is nothing to explain and the notification is removed.

---

# Daylight Saving Time

Recurring reminders should handle daylight saving transitions predictably.

Unexpected duplicate or skipped reminders should be avoided whenever possible.

---

# Device Reboot

Scheduled reminders should survive device restarts whenever the platform allows.

The application should restore reminder schedules automatically.

A restart clears every alarm, so a dose whose time passed while the device was off never got its reminder. On restart such doses (still scheduled, time or rescheduled time in the past) are marked **Missed**. If any were marked (placebo doses are not counted), one notification says how many doses were missed while the phone was off. It names no medication, and tapping it opens the daily timeline, where the doses can still be corrected. An app update does the same clean-up without the notification, because the device was not off.

---

# Battery Optimization

The application should minimize battery usage without compromising reminder reliability.

Platform-recommended scheduling APIs should be preferred.

---

# Offline Behavior

Reminder functionality must not depend on internet connectivity.

Medication reminders should continue functioning while offline.

---

# Accessibility

Notifications should remain compatible with assistive technologies.

Actions should be understandable through screen readers.

---

# Error Recovery

If reminder scheduling fails, the application should attempt recovery where possible.

Users should be informed when critical reminder scheduling cannot be guaranteed.

The home screen shows a warning banner while any of these is true:

- A dose's reminder never went off: its time (or its rescheduled time) passed more than 15 minutes ago, within the last 24 hours, and the alarm never fired. Doses that have no alarm are not counted (placebo days, as-needed medications, suspended or archived medications, doses after the end of a fixed-length treatment).
- Notifications, or the medication reminder category, are turned off.
- The exact alarm permission is off.
- Battery optimization is on for the app.

Permission problems are shown only while at least one medication sets reminder alarms. Tapping the banner opens Reminder Troubleshooting. The banner changes no dose or setting, and it disappears by itself once the problem is resolved (the dose is answered, the permission is granted, or 24 hours pass).

---

# Future Features

Future versions may support:

- Wearable notifications
- Smartwatch actions
- Smart speakers
- Caregiver notifications
- Shared reminder schedules

These features must remain optional.

---

# Anti-Patterns

Never:

- Assume medication was taken.
- Dismiss reminders automatically without user intent.
- Reveal sensitive medical information unnecessarily.
- Require internet access for reminders.
- Permanently modify schedules because of a snooze action.
- Merge unrelated reminder categories.

---

# References

- product.md
- medical-safety.md
- privacy.md
- accessibility.md