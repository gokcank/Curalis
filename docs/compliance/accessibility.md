# Accessibility

## Purpose

This document defines the accessibility standards for Curalis.

Accessibility is a core product requirement and must be considered throughout the design, development, and testing process.

Every user should be able to use Curalis regardless of age, ability, or assistive technology.

---

# Goals

Curalis should be:

- Perceivable
- Operable
- Understandable
- Robust

The application should align with the principles of the Web Content Accessibility Guidelines (WCAG) where applicable to native mobile applications.

---

# Principles

## Accessibility by Design

Accessibility should be built into every feature from the beginning.

It must never be treated as an optional enhancement.

---

## Equal Experience

Core functionality should remain available to all users.

Accessibility features should not provide a reduced experience.

---

## Simplicity

Interfaces should reduce cognitive load.

Medication management should remain straightforward even for users with limited technical experience.

---

# Text

Text should always remain readable.

Guidelines:

- Support system font scaling.
- Avoid fixed text sizes.
- Avoid truncating essential information.
- Maintain sufficient spacing between text elements.

---

# Contrast

Text and important interface elements should provide sufficient contrast against their backgrounds.

Color alone must never communicate essential information.

Always combine color with text, icons, or other visual indicators.

---

# Touch Targets

Interactive elements should provide comfortable touch areas.

Avoid placing interactive elements too close together.

---

# Screen Readers

Every interactive component should provide meaningful accessibility labels.

Examples include:

- Buttons
- Icons
- Medication cards
- Reminder actions
- Navigation items

Decorative elements should not be announced.

---

# Navigation

Navigation order should remain logical and predictable.

Keyboard navigation should be supported where the platform provides it.

---

# Motion

Animations should never interfere with usability.

Where supported by the platform, reduced motion preferences should be respected.

---

# Notifications

Notifications should be understandable without requiring visual context.

Important reminder information should remain concise and clear.

---

# Forms

Forms should:

- Clearly identify required fields.
- Explain validation errors.
- Preserve user input after validation failures.
- Avoid relying solely on placeholder text.

---

# Error Messages

Error messages should:

- Explain what happened.
- Explain how to recover.
- Avoid technical terminology.

---

# Icons

Icons should support, not replace, text.

Where meaning could become ambiguous, labels should be displayed.

---

# Feedback

Important actions should provide accessible feedback.

Feedback may include:

- Visual confirmation
- Haptic feedback (where available)
- Accessible announcements
- Sound (where appropriate)

No single feedback method should be required.

---

# Time-Sensitive Actions

Users should have sufficient time to respond to important actions.

Avoid unnecessary countdowns.

Critical reminders should remain visible until acknowledged or dismissed according to user preferences.

---

# Language

Use clear, concise language.

Avoid unnecessary medical jargon when simpler terminology conveys the same meaning.

When medical terminology is required, explanations should be provided where appropriate.

---

# Cognitive Accessibility

Reduce unnecessary complexity.

Avoid:

- Deep navigation hierarchies
- Overloaded screens
- Excessive choices
- Hidden functionality

Each screen should have a clear primary purpose.

---

# Internationalization

Accessibility should remain compatible with future localization efforts.

Layouts should adapt to longer translated text.

Avoid hardcoded text sizes or fixed-width layouts.

---

# Testing

Accessibility should be verified throughout development.

Testing should include:

- Screen reader support
- Font scaling
- High contrast
- Keyboard navigation (where applicable)
- Landscape orientation (where applicable)

Accessibility testing should be part of the release process.

---

# Anti-Patterns

Avoid:

- Tiny touch targets
- Low-contrast text
- Icon-only actions without labels
- Fixed font sizes
- Hidden navigation
- Color-only status indicators
- Automatically disappearing critical information

---

# References

- design-system.md
- ui.md
- product.md
- medical-safety.md