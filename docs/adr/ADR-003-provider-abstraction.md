# ADR-003

# Provider Abstraction

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Medication information may originate from multiple providers.

Different countries, APIs, or datasets may become available over time.

Providers may also become unavailable or change their terms of service.

---

## Problem

How should external medication providers be integrated?

---

## Options Considered

### Option A

Direct API integration

### Option B

Single provider implementation

### Option C

Provider abstraction layer

---

## Decision

All medication providers must implement a common provider interface.

The application communicates only with the provider abstraction.

Provider implementations remain interchangeable.

---

## Consequences

### Advantages

- Easier provider replacement
- Better testing
- Country-specific providers
- Reduced vendor lock-in

### Trade-offs

- Additional abstraction layer
- Slightly increased implementation effort

---

## Rules

Providers should never expose provider-specific models.

All provider responses must be converted into Curalis domain models.

Provider failures must never prevent manual medication creation.

---

## Future

Multiple providers may coexist.

Provider priority and fallback behavior should remain configurable.

---

## References

- providers.md
- api.md
- architecture.md