# User Interface

## Purpose

This document defines the user interface principles, navigation structure, screen organization, and interaction patterns of Curalis.

It describes how users interact with the application rather than how components are visually styled.

Visual appearance is defined separately in `design-system.md`.

---

# Goals

The interface should be:

- Simple
- Predictable
- Accessible
- Calm
- Efficient
- Consistent

The UI should reduce cognitive load and support daily medication management without overwhelming the user.

---

# Design Philosophy

The interface should prioritize clarity over decoration.

Users should always know:

- Where they are.
- What they can do.
- What just happened.
- What will happen next.

---

# Navigation Principles

Navigation should remain shallow and predictable.

Users should reach frequently used functionality with as few interactions as practical.

Avoid deep navigation hierarchies.

---

# Primary Navigation

The application should expose only the most important sections through primary navigation.

Candidate sections include:

- Home
- Medications
- Reminders
- Inventory
- History
- Settings

The final navigation structure may evolve with user testing.

---

# Screen Responsibilities

Every screen should have one primary purpose.

Avoid mixing unrelated workflows on the same screen.

---

# Home Screen

The Home screen acts as the user's dashboard.

It should prioritize today's information rather than all available information.

Examples include:

- Upcoming reminders
- Missed reminders
- Low inventory
- Active treatments
- Quick actions

---

# Medication Screen

The Medication screen manages medication records.

Users should be able to:

- Browse medications
- Search medications
- Filter medications
- Add medications
- Edit medications
- Archive medications

---

# Reminder Screen

The Reminder screen focuses exclusively on reminder management.

It should not become a medication editor.

---

# Inventory Screen

Inventory management should remain independent from reminder management.

Users should immediately understand:

- Current quantity
- Remaining supply
- Refill status

---

# History Screen

History should provide chronological insight.

Historical information must remain read-only unless correction workflows exist.

---

# Settings

Settings should contain application configuration only.

Medical information should never be hidden inside settings.

---

# Information Hierarchy

Important information should receive the highest visual priority.

Recommended order:

1. Critical
2. Time-sensitive
3. Frequently used
4. Supporting information

---

# User Actions

Primary actions should always be obvious.

Secondary actions should never compete visually with primary actions.

Destructive actions should require confirmation where appropriate.

---

# Search

Search should be available wherever users manage large collections.

Search should provide immediate feedback.

Empty searches should remain informative.

---

# Forms

Forms should:

- Minimize typing
- Prefer selection when practical
- Validate continuously
- Explain errors clearly

---

# Empty States

Every empty state should explain:

- Why nothing is shown.
- What the user can do next.

Avoid blank screens.

---

# Error States

Errors should:

- Explain the problem.
- Explain possible recovery.
- Avoid technical language.

---

# Loading States

Loading indicators should communicate progress without blocking unnecessary interaction.

Avoid indefinite loading whenever possible.

---

# Feedback

Every meaningful user action should receive appropriate feedback.

Examples:

- Success
- Warning
- Error
- Information

Feedback should be immediate and understandable.

---

# Confirmation

Only ask for confirmation when the action is:

- Destructive
- Difficult to reverse
- Safety-critical

Avoid confirmation fatigue.

---

# Notifications

Notifications should support—not interrupt—the user's workflow.

Only important events should trigger attention-grabbing notifications.

---

# Accessibility

The interface must follow the principles defined in `accessibility.md`.

Accessibility should never be treated as an optional enhancement.

---

# Responsiveness

The interface should adapt gracefully to:

- Phones
- Foldables
- Tablets
- Larger screens

Layouts should scale without changing interaction patterns.

---

# Consistency

Identical actions should behave identically throughout the application.

Users should never need to relearn common interactions.

---

# Future Evolution

New features should extend the existing interaction model instead of introducing entirely new paradigms.

Consistency should be preserved as the application grows.

---

# References

- philosophy.md
- product.md
- accessibility.md
- design-system.md