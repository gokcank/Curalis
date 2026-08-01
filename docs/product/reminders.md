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