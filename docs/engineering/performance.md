# Performance

## Purpose

This document defines the performance principles and engineering practices for Curalis.

Performance should support usability, reliability, and battery efficiency without sacrificing maintainability or correctness.

Performance optimization should be deliberate, measurable, and justified.

---

# Goals

Curalis should be:

- Responsive
- Efficient
- Predictable
- Battery-friendly
- Scalable

Performance should remain consistent across both modern and older supported devices.

---

# Principles

## User Experience First

Perceived performance is as important as measured performance.

The application should always feel responsive.

---

## Correctness Before Speed

Correct behavior always takes priority over optimization.

Never sacrifice reliability for small performance gains.

---

## Measure Before Optimizing

Performance problems should be identified using profiling and measurement.

Avoid speculative optimization.

---

## Simplicity

Prefer simple, maintainable solutions.

Complex optimizations should only be introduced when supported by measurable evidence.

---

# Startup

Application startup should be as lightweight as possible.

Avoid expensive operations during launch.

Load only what is necessary for the initial screen.

---

# UI Performance

User interactions should remain smooth.

Avoid blocking the main thread.

Long-running operations must execute asynchronously.

---

# Database Performance

Database operations should:

- Use indexes where appropriate.
- Avoid unnecessary queries.
- Minimize repeated lookups.
- Retrieve only the required data.

Avoid full database scans whenever practical.

---

# Search Performance

Medication search should remain responsive.

Search operations should:

- Minimize unnecessary provider requests.
- Prioritize local results.
- Use cached information when available.

---

# Network Performance

Reduce unnecessary network traffic.

Prefer:

- caching
- incremental updates
- request deduplication

Avoid repeated requests for unchanged information.

---

# Memory Usage

Memory should be managed efficiently.

Avoid:

- unnecessary object retention
- memory leaks
- oversized caches

Release resources as soon as they are no longer needed.

---

# Battery Usage

Battery efficiency is a core requirement.

Avoid:

- unnecessary background work
- excessive polling
- frequent wake-ups

Background operations should respect platform recommendations.

---

# Notifications

Reminder scheduling should remain efficient.

The application should avoid redundant scheduling operations.

---

# Images

Images should be optimized for mobile devices.

Avoid loading unnecessarily large assets.

Release image resources when no longer required.

---

# Attachments

Large attachments should not block normal application usage.

File operations should execute asynchronously.

---

# Background Tasks

Background work should:

- be interruptible
- recover gracefully
- avoid duplicate execution

Background tasks should never degrade the user experience.

---

# Scalability

Performance should remain acceptable as user data grows.

The architecture should support:

- thousands of reminder records
- large medication histories
- extensive inventories

without significant degradation.

---

# Caching

Caching should improve performance without compromising correctness.

Cached data should:

- expire appropriately
- remain replaceable
- never become the sole source of truth

---

# Logging

Logging should remain lightweight.

Avoid excessive logging in production builds.

---

# Resource Usage

The application should minimize usage of:

- CPU
- Memory
- Storage
- Battery
- Network

Efficiency should be considered during feature development.

---

# Monitoring

Performance regressions should be detected during development.

Major changes should be evaluated using profiling tools.

---

# Anti-Patterns

Avoid:

- premature optimization
- blocking the UI thread
- duplicate background jobs
- unnecessary database queries
- excessive network requests
- oversized caches
- unnecessary object allocations

---

# References

- architecture.md
- database.md
- reminders.md
- sync.md