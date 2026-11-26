# ADR-012

# License: All Rights Reserved

## Status

Accepted (2026-11-27)

---

## Date

2026-11-27

---

## Context

- The repository is public on GitHub. Its `LICENSE` file has been the MIT License since 2026-08-01 (commit `6f7ae1a`,
  which replaced Apache 2.0 on the first day). GitHub shows the repository as MIT-licensed.
- `README.md` says the opposite: "All rights reserved. Proprietary software".
- `docs/compliance/legal.md` says the license is documented separately and does not name one.
- Curalis is distributed only as signed APKs on GitHub releases (not on Google Play).
- The owner wants to keep all rights to the code while keeping the repository public.

---

## Problem

The project states two incompatible licenses. A reader cannot tell what they may do with the code, and the
owner's intent (all rights reserved) is not what the binding file (`LICENSE`, MIT) grants.

---

## Options Considered

### Option 1: Keep MIT and fix the README

Anyone may use, copy, modify and redistribute the code, including in closed or commercial apps.

Rejected: not the owner's intent.

### Option 2: All rights reserved, repository stays public (source-available)

The code can be read on GitHub; using, copying, modifying or redistributing it needs the owner's written
permission. GitHub's terms still let users view and fork the repository on GitHub.

Chosen.

### Option 3: Make the repository private

Rejected: releases are published from this repository, and the public code supports trust in a health app's
privacy claims.

### Option 4: A source-available license with conditions (e.g. non-commercial)

Rejected for now: more text to maintain and enforce; can be revisited later.

---

## Decision

1. `LICENSE` is replaced with an "All rights reserved" notice: copyright holder and year, viewing on GitHub allowed,
   any other use, copying, modification or distribution only with prior written permission, no warranty.
2. `README.md` (and the Turkish README) state the same in their license section.
3. `docs/compliance/legal.md` names the project license and points to `LICENSE`.
4. The change applies from the commit that replaces `LICENSE`. Versions published before it (up to and including
   1.8.0) were published under MIT; this ADR does not and cannot withdraw that grant for those copies.

---

## Consequences

- The repository is no longer open source in the OSI sense; it is source-available.
- Outside contributions would need a separate agreement; none are expected.
- Third-party dependency licenses are unaffected; their notices must still be respected (`legal.md`).
- GitHub will show the license as "Other" or none instead of MIT.
- Copies of the code taken before the change keep their MIT rights.

---

## Alternatives Rejected

- Keeping MIT (Option 1), making the repository private (Option 3), a conditional source-available license
  (Option 4): see above.

---

## References

- `LICENSE`, `README.md`, `docs/compliance/legal.md`
- GitHub Terms of Service, section D.5 (license granted to other GitHub users to view and fork public repositories)
