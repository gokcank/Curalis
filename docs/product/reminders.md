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

---

# Quiet Hours

Users may define quiet hours.

If enabled, reminder behavior during quiet hours should follow user preferences.

Critical reminder handling should remain configurable.

---

# Time Zones

Reminder schedules should remain consistent when the device time zone changes.

The scheduling strategy should be documented and applied consistently.

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