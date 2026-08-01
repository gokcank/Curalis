# Design System

## Purpose

This document defines the visual language, interaction principles, and reusable design foundation of Curalis.

Its purpose is to ensure that every screen, component, and interaction feels like part of a single cohesive product.

The Design System defines how the application looks, behaves, and communicates visually.

Content and writing standards are defined separately in `content-guidelines.md`.

---

# Philosophy

The Curalis Design System is based on the belief that healthcare software should reduce stress rather than increase it.

The interface should feel:

- Calm
- Predictable
- Trustworthy
- Accessible
- Professional

Visual design should never compete with information.

Instead, it should help users focus on the information that matters.

---

# Design Goals

The Design System prioritizes:

- Clarity
- Consistency
- Accessibility
- Simplicity
- Readability
- Predictability

Decoration should never reduce usability.

---

# Foundation

Material Design 3 provides the technical foundation.

Curalis provides the visual identity.

Material components may be customized when doing so improves consistency with the project's design philosophy.

Material Design is a tool—not the product identity.

---

# Visual Personality

Curalis should feel:

- Calm
- Minimal
- Friendly
- Modern
- Clinical without feeling cold
- Professional
- Trustworthy

The interface should avoid looking playful or entertainment-focused.

---

# Design Principles

## Clarity First

Users should immediately understand:

- what they are looking at
- what actions are available
- what requires attention

---

## Information Before Decoration

Information always has higher priority than visual effects.

Avoid unnecessary gradients, shadows, animations, and decorative elements.

---

## Consistency

Similar components should behave identically.

Users should never need to relearn familiar interactions.

---

## Calm Design

The interface should avoid visual overload.

Prefer whitespace over density.

Reduce unnecessary visual noise.

---

## Progressive Disclosure

Show only the information needed for the current task.

Reveal advanced options only when appropriate.

Avoid overwhelming users with rarely used settings.

---

## Accessibility by Default

Accessibility is a core design requirement.

Every component should remain usable without relying on vision, color perception, or precise touch input.

---

# Design Language

The design language is built around:

- Cards
- Lists
- Sections
- Meaningful spacing
- Strong typography
- Clear hierarchy

Complex layouts should be avoided whenever practical.

---

# User Attention

Visual attention should follow this order:

1. Critical medical information
2. Current reminders
3. Required user actions
4. Active treatments
5. Supporting information

Less important information should never visually compete with higher-priority content.

---

# Medical UI Principles

Medical software should communicate confidence without creating anxiety.

Avoid unnecessary warning colors.

Avoid excessive alerts.

Avoid visual clutter.

Medical information should appear organized, stable, and trustworthy.

---

# Color Philosophy

Colors communicate meaning.

Color should never exist purely for decoration.

Every semantic color must have a defined purpose.

Color must never be the only way information is communicated.

---

# Typography Philosophy

Typography is the primary communication tool.

Readable text is more important than decorative typography.

Hierarchy should come from typography before color.

---

# Component Philosophy

Components should be:

- Predictable
- Reusable
- Accessible
- Self-explanatory

Components should solve one problem well rather than many problems poorly.

---

# Motion Philosophy

Animation exists to improve understanding.

Animation should never exist solely for decoration.

Motion should explain:

- transitions
- hierarchy
- cause and effect
- state changes

---

# References

- philosophy.md
- ui.md
- accessibility.md
- content-guidelines.md

---

# Color System

## Philosophy

Colors communicate meaning before aesthetics.

Every semantic color must have a clearly defined purpose.

Avoid decorative color usage.

The interface should remain calm, readable, and medically appropriate.

---

## Semantic Colors

The design system defines semantic colors rather than fixed hexadecimal values.

Color values may evolve while their meanings remain stable.

Core semantic colors include:

- Primary
- Secondary
- Tertiary
- Surface
- Surface Variant
- Background
- Error
- Warning
- Success
- Information
- Outline

---

## Primary

Represents the application's primary identity.

Used for:

- Primary actions
- Active navigation
- Important emphasis

Should not be overused.

---

## Secondary

Supports the primary color.

Used sparingly for secondary emphasis.

---

## Tertiary

Reserved for optional visual differentiation.

Should never replace semantic colors.

---

## Surface

Surfaces define structure.

Different elevation levels should be distinguished primarily through tonal elevation rather than strong shadows.

---

## Error

Reserved exclusively for:

- Errors
- Validation failures
- Dangerous actions

Never use Error colors for ordinary attention.

---

## Warning

Represents situations requiring user attention but not immediate danger.

Examples:

- Low medication inventory
- Upcoming refill
- Missing optional information

---

## Success

Represents completed user actions.

Examples:

- Medication successfully added
- Reminder confirmed
- Backup completed

Success should never imply medical success.

Example:

"Reminder confirmed"

NOT

"Treatment successful"

---

## Information

Used for neutral informational content.

Examples:

- Tips
- Updates
- Help messages

---

## Color Independence

No information should depend solely on color.

Every color-based state must also include:

- text
- icon
- layout
- or another visual indicator

---

## Dynamic Color

Dynamic Color may be supported.

If enabled:

- semantic meaning must remain unchanged
- accessibility requirements must still be satisfied

---

## Dark Theme

Dark mode should not simply invert colors.

It should remain:

- comfortable
- readable
- low contrast where appropriate
- medically calm

Pure black should generally be avoided.

---

# Typography

## Philosophy

Typography is the primary communication tool.

Hierarchy should be created through typography before introducing additional visual styling.

---

## Font Family

Use a clean, highly legible typeface.

Preference should be given to platform-native typography whenever practical.

---

## Hierarchy

Typography should clearly distinguish:

- Display
- Headline
- Title
- Body
- Label

Each level should have a unique purpose.

---

## Readability

Long text should prioritize readability over density.

Avoid unnecessarily small font sizes.

---

## Numeric Typography

Numbers are especially important within medical applications.

Medication quantities

Times

Dates

Dosages

Inventory

should remain easy to scan.

Prefer tabular numbers where supported.

---

# Layout

## Layout Philosophy

Whitespace is a design tool.

Spacing improves comprehension.

Avoid filling every available area.

---

## Grid

Use a consistent spacing system throughout the application.

Avoid arbitrary spacing values.

---

## Spacing Scale

Spacing should be based on a single scale.

Example:

4

8

12

16

24

32

48

64

Avoid inconsistent spacing.

---

## Alignment

Visual alignment should always appear intentional.

Components should align to shared layout boundaries.

---

## Density

The interface should prioritize readability over maximum information density.

Avoid overly compact layouts.

---

# Shapes

## Philosophy

Rounded corners should communicate friendliness without appearing playful.

Shape consistency is more important than shape variety.

---

## Radius

Corner radii should follow a limited scale.

Avoid assigning custom radius values to individual components.

---

## Cards

Cards are the primary content container.

Cards should:

- group related information
- maintain consistent padding
- avoid excessive nesting

---

## Buttons

Buttons should maintain consistent corner radius and sizing across the application.

---

## Dialogs

Dialogs should appear distinct from ordinary surfaces while remaining visually connected to the design language.

---

## Bottom Sheets

Bottom Sheets are preferred over dialogs for complex interactions on mobile devices.

---

# Elevation

## Philosophy

Elevation communicates hierarchy.

Avoid dramatic shadows.

Prefer tonal elevation.

---

## Levels

Each elevation level should have a defined semantic purpose.

Elevation should never be decorative.

---

# References

- ui.md
- accessibility.md

---

# Iconography

## Philosophy

Icons support comprehension.

They should reinforce meaning rather than replace text.

Icons must remain recognizable at small sizes.

---

## Style

Use a single icon family throughout the application.

Avoid mixing outlined, filled, rounded, and sharp styles within the same interface.

Maintain visual consistency.

---

## Usage

Icons should:

- support labels
- improve scanning
- communicate actions
- indicate status

Icons should not replace clear wording.

---

## Medical Icons

Medical icons should remain neutral and universally understandable.

Avoid overly decorative or emotionally charged medical imagery.

Examples:

- Pill
- Capsule
- Syringe
- Calendar
- Clock
- Bell
- Inventory
- History

Avoid unnecessary symbolism.

---

## Icon Sizes

Use a limited set of standard sizes.

Examples:

- Small
- Medium
- Large

Avoid arbitrary sizing.

---

## Status Icons

Critical states should combine:

- icon
- text
- semantic color

Never rely on icons alone.

---

# Motion

## Philosophy

Motion should explain interface behavior.

Animation is functional, not decorative.

---

## Goals

Motion should communicate:

- navigation
- hierarchy
- state changes
- feedback
- continuity

---

## Principles

Motion should be:

- fast
- smooth
- predictable
- subtle

Avoid exaggerated animations.

---

## Duration

Animations should complete quickly enough to avoid interrupting user workflows.

Long animations should be avoided.

---

## Easing

Transitions should feel natural.

Abrupt movement should be reserved for exceptional circumstances.

---

## Navigation

Screen transitions should reinforce navigation direction.

Users should never feel lost during navigation.

---

## Component Motion

Components should animate only when animation improves understanding.

Examples:

- expanding cards
- bottom sheets
- dialogs
- snackbars

---

## Loading Motion

Loading animations should reassure users that work is in progress.

Avoid distracting or repetitive animations.

---

## Success Motion

Success animations should remain subtle.

Avoid celebration-style animations.

Medication management is a productivity workflow, not a game.

---

## Error Motion

Errors should draw attention without causing unnecessary stress.

Use restrained motion.

---

## Reduced Motion

Respect the user's reduced motion preference.

Motion should remain optional whenever practical.

---

# Components

## Philosophy

Components are reusable building blocks.

Every component should solve one problem consistently.

Avoid creating multiple components with overlapping responsibilities.

---

## Component Principles

Every component should be:

- reusable
- accessible
- predictable
- testable
- documented

---

## Variants

Component variants should exist only when they represent genuinely different purposes.

Avoid creating variants solely for visual differences.

---

## Composition

Prefer composing simple components over creating large, specialized components.

Composition improves maintainability.

---

## States

Every interactive component should define its supported states.

Typical states include:

- Default
- Hover
- Focus
- Pressed
- Disabled
- Loading
- Error
- Selected

Behavior should remain consistent across all components.

---

## Responsiveness

Components should adapt gracefully to different screen sizes without changing their interaction model.

---

## Accessibility

Every component must satisfy the accessibility requirements defined in `accessibility.md`.

Accessibility is a requirement, not an enhancement.

---

# References

- ui.md
- accessibility.md
- content-guidelines.md

---

# Component Library

## App Bar

The App Bar provides context for the current screen.

Responsibilities:

- Display screen title
- Provide contextual actions
- Support navigation
- Maintain orientation

The App Bar should remain visually lightweight.

Avoid placing more than three primary actions in the top app bar.

---

## Bottom Navigation

Bottom Navigation represents the application's primary destinations.

Guidelines:

- Use only for top-level destinations.
- Avoid nested navigation.
- Keep labels visible.
- Prefer five or fewer destinations.

Navigation should remain stable throughout the application.

---

## Navigation Rail

Navigation Rail may be used on larger screens.

It should mirror Bottom Navigation functionality.

Navigation patterns should remain consistent across screen sizes.

---

## Navigation Drawer

Navigation Drawer is reserved for secondary destinations.

Frequently used functionality should never be hidden exclusively inside the drawer.

---

## Floating Action Button (FAB)

The FAB represents the primary action of the current screen.

Examples:

Medication Screen

→ Add Medication

Reminder Screen

→ Add Reminder

Inventory Screen

→ Add Inventory Entry

Only one primary FAB should exist per screen.

Extended FAB may be used when additional context improves clarity.

---

## Cards

Cards are the primary information container.

Cards should:

- group related information
- maintain consistent padding
- avoid unnecessary nesting
- remain visually lightweight

Cards should not become miniature screens.

---

## Medication Card

Purpose

Display a medication summary.

Typical content:

- Medication name
- Active ingredient
- Strength
- Schedule summary
- Reminder status
- Inventory status

Actions should remain secondary to information.

---

## Reminder Card

Purpose

Present upcoming or active reminders.

Typical content:

- Medication
- Time
- Status
- Primary action

Critical reminders should receive higher visual emphasis without overwhelming the interface.

---

## Inventory Card

Purpose

Display medication availability.

Typical content:

- Remaining quantity
- Estimated remaining days
- Refill status

Inventory warnings should remain informative rather than alarming.

---

## List Items

Lists should support efficient scanning.

Each row should communicate one primary piece of information.

Avoid placing excessive controls inside list items.

---

## Buttons

Buttons communicate intent.

Hierarchy:

- Filled
- Filled Tonal
- Outlined
- Text

Primary actions should always be visually identifiable.

Avoid multiple competing primary buttons.

---

## Icon Buttons

Use icon buttons only when their meaning is universally understood.

Otherwise include text.

---

## Segmented Buttons

Use segmented buttons for switching between closely related views.

Avoid using them for navigation.

---

## Chips

Use chips for:

- filters
- categories
- quick selections
- tags

Avoid replacing navigation with chips.

---

## Search Bar

Search should remain immediately discoverable.

Support:

- incremental search
- clear action
- empty states

Search should tolerate partial input.

---

## Text Fields

Forms should minimize typing whenever practical.

Text fields should provide:

- labels
- helper text
- validation
- clear error messages

Placeholder text should never replace labels.

---

## Dropdown Menus

Dropdowns should be used only when users already know the available choices.

Large datasets should use searchable selection instead.

---

## Date Picker

Dates should be easy to understand.

Respect regional formatting preferences.

---

## Time Picker

Medication scheduling should prioritize speed and readability.

Frequently used times should require minimal interaction.

---

## Dialogs

Dialogs interrupt workflow.

Use only when user attention is required.

Dialogs should answer:

- What happened?
- Why?
- What are the available actions?

---

## Bottom Sheets

Bottom Sheets are preferred for:

- multi-step actions
- contextual editing
- additional options

Complex workflows should favor Bottom Sheets over dialogs.

---

## Snackbars

Snackbars provide lightweight feedback.

Use them for:

- confirmations
- undo actions
- short informational messages

Snackbars should disappear automatically.

---

## Progress Indicators

Progress should always communicate:

- current state
- remaining work when possible

Indeterminate indicators should be used only when progress cannot be measured.

---

## Switches

Switches represent immediate on/off preferences.

Avoid using switches for destructive actions.

---

## Checkboxes

Checkboxes support multiple selections.

They should not replace switches.

---

## Radio Buttons

Radio buttons support mutually exclusive choices.

Users should immediately understand the available options.

---

## Badges

Badges communicate counts or status.

Avoid excessive badge usage.

Only meaningful information should receive badges.

---

## Tabs

Tabs divide closely related content.

Tabs should not replace primary navigation.

---

## Dividers

Dividers should separate content—not decorate it.

Prefer whitespace whenever separation can be achieved without visible lines.

---

## Tooltips

Tooltips provide optional clarification.

They should never contain essential information.

Users should be able to complete tasks without relying on tooltips.

---

# Component Consistency

Every component should define:

- Purpose
- Usage
- States
- Accessibility
- Responsiveness
- Interaction behavior

New components should follow the same documentation structure.

---

# References

- ui.md
- accessibility.md
- content-guidelines.md

---

# Interaction Patterns

## Philosophy

Interaction patterns define how components work together to create consistent user experiences.

Users should recognize familiar workflows throughout the application.

Patterns should reduce learning, minimize mistakes, and improve efficiency.

---

# CRUD Pattern

All Create, Read, Update, and Delete workflows should follow a consistent structure.

Users should always know:

- where they are
- what they are editing
- how to save
- how to cancel

Unsaved changes should be communicated clearly.

---

# Create Flow

Creating a new item should require the minimum amount of information necessary.

Advanced options should remain hidden until needed.

Examples:

- Add Medication
- Add Reminder
- Add Inventory Entry

---

# Edit Flow

Editing should preserve existing values.

Changes should be reversible until saved.

Users should never lose edits unexpectedly.

---

# Delete Flow

Deletion should require confirmation only when recovery is difficult or impossible.

Whenever practical, prefer soft delete or Undo over permanent deletion.

---

# Search Pattern

Search should be available for all large collections.

Search should support:

- incremental filtering
- partial matches
- typo tolerance where practical
- immediate feedback

Empty search results should explain what happened.

---

# Filtering

Filtering narrows existing results.

Filtering should never permanently modify data.

Users should easily reset all filters.

---

# Sorting

Sorting changes presentation only.

Sorting should not alter underlying data.

The active sort order should always be visible.

---

# Selection Pattern

Selection should be visually obvious.

Bulk actions should appear only after one or more items have been selected.

Users should always know how many items are selected.

---

# Confirmation Pattern

Confirmation dialogs should be used only when necessary.

Good candidates include:

- permanent deletion
- stopping an active treatment
- clearing history
- resetting settings

Routine actions should not require confirmation.

---

# Undo Pattern

Undo is preferred over confirmation whenever practical.

Undo should remain available for an appropriate amount of time.

Undo should clearly describe the reverted action.

---

# Empty States

Every empty state should answer:

- Why is nothing here?
- Is this expected?
- What should I do next?

Empty states should encourage meaningful action.

---

# Loading Pattern

Loading indicators should communicate progress without blocking unrelated interactions.

Skeleton loading is preferred over large blocking spinners when appropriate.

Avoid sudden layout shifts.

---

# Error Recovery

Errors should always provide a recovery path.

Examples:

- Retry
- Edit input
- Check connection
- Continue offline

Users should never reach a dead end.

---

# Offline Pattern

Offline functionality should degrade gracefully.

Unavailable features should explain why they are unavailable.

Previously available local information should remain accessible.

---

# Provider Pattern

When provider information is unavailable:

1. Use cached data if available.
2. Allow manual medication creation.
3. Explain the limitation without technical jargon.

Users should never be blocked from using the application.

---

# Reminder Pattern

Reminder interactions should be fast.

Common actions should require minimal interaction.

Typical actions include:

- Confirm
- Snooze
- Skip
- View Medication

---

# Form Pattern

Forms should:

- validate continuously
- group related fields
- minimize scrolling
- reduce typing

Required fields should be clearly identified.

---

# Navigation Pattern

Users should always know:

- current location
- previous location
- available destinations

Navigation should remain predictable.

---

# Progressive Disclosure

Present only the information required for the current task.

Advanced options should remain hidden until requested.

---

# Feedback Pattern

Every meaningful action should produce appropriate feedback.

Feedback should be:

- immediate
- understandable
- proportional

---

# Recovery Pattern

Mistakes should be easy to recover from.

Avoid irreversible actions whenever practical.

The interface should help users correct errors rather than punish them.

---

# References

- ui.md
- content-guidelines.md
- accessibility.md

---

# Data Visualization

## Philosophy

Data visualization should improve understanding, not decorate the interface.

Visualizations should help users answer questions quickly while remaining accurate, accessible, and easy to interpret.

Every visualization must communicate meaningful information.

---

# Principles

Data visualizations should be:

- Accurate
- Readable
- Accessible
- Consistent
- Minimal

Avoid unnecessary visual complexity.

---

# Purpose

Visualizations should help users understand:

- medication adherence
- reminder activity
- medication inventory
- treatment duration
- long-term trends

Charts should support decision-making rather than entertainment.

---

# Chart Selection

Choose the simplest visualization capable of communicating the data.

Avoid complex charts when simpler alternatives provide equal clarity.

---

# Supported Visualization Types

Preferred chart types include:

- Line Charts
- Bar Charts
- Progress Indicators
- Timelines
- Calendar Views
- Distribution Charts

Avoid 3D charts.

Avoid decorative chart effects.

---

# Progress Indicators

Progress indicators should represent measurable progress.

Examples include:

- Remaining medication
- Treatment completion
- Daily reminder completion

Progress should never imply treatment effectiveness.

---

# Timelines

Timelines are preferred for chronological information.

Examples include:

- Medication history
- Reminder history
- Treatment duration

Time should always progress in a predictable direction.

---

# Calendar Views

Calendar views should emphasize consistency rather than density.

Users should quickly identify:

- completed days
- missed reminders
- scheduled medications

Calendar cells should remain readable.

---

# Inventory Visualization

Inventory should communicate:

- remaining quantity
- estimated remaining duration
- refill threshold

Low inventory should become increasingly noticeable without appearing alarming.

---

# Statistics

Statistics should summarize user activity.

Examples include:

- active medications
- completed reminders
- missed reminders
- upcoming reminders

Statistics should remain descriptive rather than evaluative.

---

# Trend Visualization

Long-term trends should emphasize changes over time.

Avoid implying medical conclusions from visual trends.

---

# Color Usage

Charts should follow the semantic color system.

Colors should communicate meaning consistently across the application.

Color should never be the sole information carrier.

---

# Labels

Charts should include clear labels.

Avoid requiring users to infer meaning.

Axes, legends, and values should remain understandable without additional explanation.

---

# Empty Visualizations

If insufficient data exists:

Explain why.

Encourage future data collection naturally.

Avoid displaying misleading placeholder charts.

---

# Accessibility

Every visualization should remain understandable without relying solely on color.

Support:

- screen readers
- high contrast
- large text
- reduced motion

Charts should provide textual summaries whenever practical.

---

# Interaction

Interactive charts should remain simple.

Users should never require gestures to understand essential information.

Interactions may include:

- tap
- long press
- tooltip
- highlight

Avoid hidden interactions.

---

# Performance

Visualizations should remain responsive even with large datasets.

Prefer incremental rendering where practical.

Avoid unnecessary animations.

---

# Medical Responsibility

Charts describe recorded information.

Charts must never:

- diagnose
- predict
- recommend treatment
- evaluate medical outcomes

Medical interpretation belongs to qualified healthcare professionals.

---

# References

- ui.md
- accessibility.md
- medical-safety.md

---

# Interface States

## Philosophy

Every screen should clearly communicate its current state.

Users should never wonder:

- What is happening?
- Why is nothing visible?
- What should I do next?

State transitions should feel predictable and intentional.

---

# State Hierarchy

Screens may transition between the following states:

- Initial
- Loading
- Success
- Empty
- Error
- Offline
- Permission Required
- Searching
- No Results
- Refreshing

States should never overlap in confusing ways.

---

# Initial State

The initial state represents the screen before data becomes available.

Avoid displaying blank interfaces.

Provide immediate visual structure whenever practical.

---

# Loading State

Loading should reassure users that work is in progress.

Preferred techniques:

- Skeleton placeholders
- Progressive loading
- Partial rendering

Avoid blocking the entire interface unless absolutely necessary.

---

# Refreshing State

Refreshing differs from loading.

Existing content should remain visible whenever practical.

Users should continue interacting with unaffected content.

Refresh indicators should remain subtle.

---

# Success State

Success confirms that an action completed successfully.

Examples:

- Medication added
- Reminder updated
- Inventory adjusted
- Backup completed

Success feedback should be brief and non-disruptive.

Avoid excessive animations.

---

# Empty State

An empty state is an opportunity to guide the user.

Every empty state should explain:

- Why nothing is shown
- Whether this is expected
- What the next step should be

Empty states should encourage meaningful action.

---

# Error State

Errors should explain:

- What happened
- What the user can do
- Whether recovery is possible

Avoid technical terminology.

Provide actionable recovery whenever practical.

---

# Offline State

Offline mode should remain fully usable whenever possible.

Unavailable functionality should explain:

- why it is unavailable
- what still works
- what will resume after reconnecting

Never imply that user data has been lost solely because connectivity is unavailable.

---

# Permission Required

If a permission is required:

Explain:

- why it is needed
- what feature depends on it
- how to enable it

Do not repeatedly interrupt users after denial.

Respect user decisions.

---

# Searching State

While searching:

Keep previous results visible whenever practical.

Provide immediate feedback.

Avoid clearing results before new results arrive.

---

# No Results

No Results differs from Empty State.

The collection exists.

The current query simply produced no matches.

Offer helpful recovery actions:

- Clear filters
- Change search terms
- Add a new medication

---

# First Launch

The first launch experience should be welcoming but brief.

Introduce only essential concepts.

Avoid overwhelming new users.

The application should become usable as quickly as possible.

---

# Maintenance State

If future maintenance modes exist:

Clearly explain:

- what is unavailable
- expected duration if known
- what users can still do

---

# Partial Failure

Some operations may succeed while others fail.

Communicate partial success honestly.

Do not present mixed outcomes as complete failures.

---

# Long Running Operations

Operations expected to take noticeable time should:

- communicate progress
- allow cancellation where appropriate
- avoid freezing the interface

---

# Recovery

Every recoverable state should provide a clear next action.

Examples:

- Retry
- Continue Offline
- Edit
- Refresh
- Open Settings

Recovery should never require guesswork.

---

# Consistency

Equivalent states should appear consistently throughout the application.

Users should recognize familiar patterns immediately.

---

# Accessibility

State changes should be communicated through multiple channels when appropriate:

- text
- icons
- semantic colors
- accessibility announcements

State should never rely solely on animation or color.

---

# References

- ui.md
- accessibility.md
- content-guidelines.md
- medical-safety.md

---

# Responsive Design

## Philosophy

Curalis should provide a consistent experience across supported device types.

Layouts may change.

Workflows should not.

Users should never need to relearn the application because of screen size.

---

# Supported Devices

The design system should support:

- Phones
- Foldables
- Tablets
- Large Tablets
- Desktop (future)

Every supported device should receive an intentionally designed experience.

---

# Adaptive Design

Adapt layouts—not behavior.

Interaction patterns should remain consistent across devices.

---

# Layout Expansion

Additional screen space should reveal more information rather than enlarging existing elements.

Prefer:

- additional panels
- side-by-side layouts
- persistent navigation

Avoid excessive whitespace.

---

# Orientation

Portrait should remain the primary orientation.

Landscape should improve productivity rather than simply rearranging content.

---

# Foldable Devices

Foldable devices should use available space intelligently.

Examples include:

- dual-pane layouts
- persistent detail panels
- expanded medication information

Avoid treating foldables as oversized phones.

---

# Tablet Experience

Tablets should prioritize information density without sacrificing readability.

Use larger layouts to reduce unnecessary navigation.

---

# Window Size Classes

Responsive behavior should follow Android Window Size Classes where practical.

Layout changes should occur at defined breakpoints rather than arbitrary dimensions.

---

# Navigation Adaptation

Navigation should adapt naturally.

Typical progression:

Phone

Bottom Navigation

↓

Tablet

Navigation Rail

↓

Large Screen

Navigation Drawer

Navigation destinations should remain identical.

---

# Multi-Pane Layouts

Large screens may display multiple related views simultaneously.

Examples:

Medication List + Details

Reminder List + Schedule

History + Detail View

Panels should complement—not duplicate—each other.

---

# Dialog Adaptation

Dialogs should scale appropriately.

Large screens should avoid excessively narrow dialogs.

Complex workflows may become side panels instead.

---

# Bottom Sheets

Bottom Sheets are optimized for phones.

Large screens may replace them with dialogs or side panels where appropriate.

---

# Responsive Forms

Forms should use available space efficiently.

Wide layouts may display related fields in multiple columns.

Field order should remain logical.

---

# Responsive Lists

Lists should adapt spacing and column count based on available width.

Scanning efficiency should remain consistent.

---

# Responsive Cards

Cards may grow horizontally.

Avoid stretching cards vertically without purpose.

Maintain comfortable reading widths.

---

# Images & Illustrations

Images should scale proportionally.

Avoid decorative imagery occupying excessive screen space.

---

# Performance

Responsive layouts should avoid unnecessary recomposition or expensive layout calculations.

Adaptation should remain performant.

---

# Accessibility

Responsive layouts must preserve:

- touch targets
- contrast
- readability
- keyboard navigation
- screen reader order

Larger screens should not reduce accessibility.

---

# Design Tokens

## Philosophy

Design Tokens define the reusable values that create visual consistency throughout the application.

Components should consume tokens rather than hardcoded values.

---

## Token Categories

The design system defines tokens for:

- Colors
- Typography
- Spacing
- Radius
- Elevation
- Motion
- Opacity
- Borders
- Icon Sizes

---

## Color Tokens

Colors should be referenced semantically.

Examples:

Primary

Surface

Error

Warning

Success

Avoid direct color values in components.

---

## Typography Tokens

Typography tokens define:

- font size
- weight
- line height
- letter spacing

Components should never define typography independently.

---

## Spacing Tokens

Spacing should follow the project's spacing scale.

Consistent spacing improves rhythm and readability.

---

## Radius Tokens

Corner radius should use predefined tokens.

Avoid arbitrary radius values.

---

## Elevation Tokens

Elevation tokens communicate hierarchy.

Components should reference semantic elevation levels.

---

## Motion Tokens

Motion tokens define:

- duration
- easing
- delay

Animation values should remain consistent throughout the application.

---

## Border Tokens

Borders should follow a consistent thickness scale.

Avoid component-specific border values.

---

## Opacity Tokens

Opacity should communicate state.

Examples:

Disabled

Pressed

Dragged

Avoid decorative transparency.

---

## Asset Guidelines

## Philosophy

Visual assets should support usability.

Decorative assets should never compete with important medical information.

---

## Illustrations

Illustrations should:

- reduce intimidation
- explain concepts
- improve onboarding
- enhance empty states

Avoid excessive decoration.

---

## Images

Images should serve a functional purpose.

Avoid stock photography unless it clearly improves understanding.

---

## Icons

Icons should follow the project's iconography guidelines.

Maintain a single visual style.

---

## Logos

Brand identity should remain simple and professional.

Logo usage should remain consistent across platforms.

---

## Avatars

User avatars are optional.

Medical functionality should never depend on profile images.

---

## Medical Imagery

Medical imagery should remain:

- respectful
- neutral
- culturally appropriate

Avoid graphic medical illustrations.

---

## Animations

Animated assets should support communication rather than entertainment.

Subtlety is preferred.

---

## Asset Quality

All assets should remain sharp across supported screen densities.

Raster assets should be minimized where scalable alternatives exist.

---

## Localization

Assets containing text should be avoided whenever practical.

Illustrations should remain language-independent.

---

# References

- ui.md
- accessibility.md
- content-guidelines.md
- illustrations.md
