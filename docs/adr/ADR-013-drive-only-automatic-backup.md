# ADR-013

# Drive-Only Automatic Backup, Optional Backup Password

## Status

Accepted (2026-11-28)

Partly supersedes ADR-011 (destinations and the backup password). ADR-011's database rescue copy, startup alarm repair
and PIN recovery stay as decided.

---

## Date

2026-11-28

---

## Context

ADR-011 (accepted 2026-11-25) introduced the daily automatic backup, released in 1.8.0:

- It always writes to a folder the user picks on the phone; Google Drive is an extra opt-in on top of it and only
  appears after a folder was chosen.
- Every backup, manual or automatic, local or on Drive, is encrypted with a backup password. The manual Drive backup
  and the Drive restore ask for the password every time.

When setting up the app for daily use on 2026-11-28, the owner found this heavier than common apps: with apps such as
WhatsApp, connecting a Google account is enough to back up and restore, with an end-to-end encryption password
available as an option. Picking a folder and a password before the first automatic backup is extra friction, and a
forgotten password makes every backup useless on a new phone.

---

## Problem

How should automatic and Drive backups work so that a single step (connecting Google Drive) is enough for daily
protection, while the user still knows what is uploaded and can choose stronger protection?

---

## Options Considered

### Destination of the automatic backup

1. **Folder required, Drive optional (status quo, ADR-011).** Rejected: two setup steps; the folder copy stays on the
   same phone unless the folder is synced elsewhere.
2. **User chooses: folder, Drive or both.** Rejected by the owner: more settings and code to maintain for little gain.
3. **Drive only.** Chosen. The manual file export remains for anyone who wants a copy outside Google.

### Password on Drive backups

1. **Always encrypted, password typed each time (status quo).** Rejected: friction.
2. **Always encrypted, password stored and never typed on the same phone.** Rejected by the owner: a new phone still
   needs the password; forgetting it loses the backups.
3. **Never encrypted.** Rejected: users who want backups unreadable on Google's servers have no way to get that.
4. **Unencrypted by default, optional backup password (as in WhatsApp).** Chosen.

---

## Decision

### Automatic backup

- Off by default. It can be turned on only while Google Drive is connected. Turning it on asks for nothing else
  (no folder, no password).
- One backup per day (periodic background work), skipped when the data has not changed since the last backup.
- Written to Drive's hidden app folder (`appDataFolder`) as dated files; the 30 newest are kept. Nothing is written
  to the phone's storage any more.
- A failure posts one notification; a network problem is retried later.
- Disconnecting Drive turns automatic backup off.

### Password ("Encrypt backups", off by default)

- Off: Drive backups (automatic and manual) are the plain JSON export. Restoring needs no password, also on a new
  phone.
- On: the user sets a password once. It is stored as in ADR-011 (Android Keystore key, dedicated preferences file
  excluded from Android backup) and used without asking: automatic and manual Drive backups are encrypted with it
  (`BackupCrypto`, `CURALIS_ENC_V1:`).
- Restoring an encrypted Drive backup first tries the stored password; if there is none (new phone, reinstall) or it
  does not open that backup, the user is asked once and the correct password is stored.
- Turning the option off makes future backups plain; the stored password is kept so older encrypted backups still
  open.
- Existing encrypted Drive backups keep working.

### Unchanged

- The manual file export and the restore from a file keep asking for a password: the file can go anywhere.
- The legacy single Drive file and dated manual Drive backups stay readable.

### Upgrade from 1.8.0

- Automatic backup on and going to Drive: it continues on Drive, with "Encrypt backups" on (that user already set a
  password).
- Automatic backup on but only to a folder: it is turned off; the card offers to turn it on with Drive. Backup files
  already in the folder are left untouched.

---

## Consequences

### Positive

- One step (connect Drive, turn on automatic backup) gives daily off-phone backups that restore on a new phone without
  a password to remember.
- Users who want stronger protection keep it, without typing the password each time.
- Less code: the folder picker, folder permission handling and folder failure notification are removed.

### Negative and accepted risks

- **By default, backups on Drive are readable plain files.** The app folder is hidden from the user and from other
  apps, and access needs the user's Google account; but Google can technically read the content, and anyone who takes
  over the Google account can read the medication list. This reverses ADR-011's rejection of unencrypted backups for
  the Drive case, by the owner's choice, and is stated in the app next to the option.
- Automatic backup needs a Google account and Drive. Without them Curalis still works fully, but only manual backups
  are available (the app must stay usable without cloud services, `docs/architecture/sync.md`).
- With encryption on, a forgotten password still makes encrypted backups unusable on a new phone.

### Privacy

- Data leaves the device only after two explicit choices: connecting Drive and turning on automatic backup (or
  tapping the manual Drive backup).
- The automatic backup card and the Drive card say whether backups are encrypted and that unencrypted backups can be
  read through the Google account.
- `docs/compliance/privacy.md`, `docs/engineering/security.md` and the README are updated with the implementation.
  The in-app privacy policy update is on hold by the owner; this change is noted for it.

---

## References

- ADR-011 Automatic Backups, Stored Backup Password and Recovery (partly superseded)
- `docs/compliance/privacy.md` (Cloud Synchronization), `docs/architecture/sync.md` (cloud never required)
- `docs/engineering/security.md` (Backup Security)
