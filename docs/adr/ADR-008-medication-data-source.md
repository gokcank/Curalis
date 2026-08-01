# ADR-008

# Medication Data Source Strategy

## Status

Accepted

---

## Date

2026-08-01

---

## Context

Curalis retrieves medication information from external providers to reduce manual data entry. However, provider availability, regional coverage, and data quality cannot be guaranteed.

Users must always be able to register their medications, regardless of provider limitations.

---

## Problem

How should Curalis obtain medication information while remaining reliable and provider-independent?

---

## Options Considered

### Option A

Single Provider

### Option B

Multiple Providers Without Priority

### Option C

Prioritized Provider Chain with Manual Fallback

---

## Decision

Curalis adopts a prioritized provider strategy.

Medication lookup follows this order:

1. Primary Provider
2. Secondary Provider
3. Active Ingredient Lookup
4. Manual Entry

The application must never prevent users from adding medications due to provider failures.

---

## Lookup Strategy

Medication lookup follows this order:

1. Search by exact medication name
2. Search by barcode
3. Search by normalized medication name
4. Search by active ingredient
5. Allow manual creation

This order is canonical. All other documents should reference this ADR.

---

## Rules

Provider data should always be normalized into Curalis domain models.

Manual entries should support later enrichment if provider data becomes available.

Users remain the final authority over their own medication records.

---

## Consequences

### Advantages

- Higher availability
- Reduced vendor lock-in
- Better international support
- Reliable fallback behavior

### Trade-offs

- Increased provider abstraction complexity
- Additional maintenance for multiple integrations

---

## References

- providers.md
- api.md
- database.md