# ADR-011

# Automatic Backups, Stored Backup Password and Recovery

## Status

Accepted (2026-11-25)

Partly superseded by ADR-013 (2026-11-28): automatic backup goes to Google Drive only, and the backup password
becomes optional. The rescue copy, startup alarm repair and PIN recovery below are unchanged.

---

## Date

2026-11-25

---

## Context

Curalis is moving from testing to everyday use with real medication data. A data-safety review on 2026-11-25 found:

- Upgrades are safe: an emulator test upgraded 1.3.2 → 1.4.1 → 1.5.0 → 1.6.0 → 1.7.0 and 1.4.1 → 1.7.0 directly
  without losing medications, stock or dose history; the migration chain 7 → 24 is tested; Android refuses to install
  an older version over a newer one.
- Backups are manual only. Uninstalling the app or losing the phone loses everything after the last manual backup.
  Android Auto Backup deliberately excludes the database (`res/xml/backup_rules.xml`).
- The Google Drive backup overwrites a single file, so one bad backup replaces the last good one.
- When SQLite reports a corrupt database file, the default `SupportSQLiteOpenHelper.Callback.onCorruption` deletes the
  file (verified in the androidx.sqlite 2.6.2 bytecode).
- The database recovery screen deletes the database with one tap, without confirmation and without a copy.
- A forgotten app PIN has no way back except clearing the app's data.

---

## Problem

How can a user's medication history survive phone loss, uninstalling, file corruption and a forgotten PIN, without
weakening privacy (ADR-001, local-first) and without asking for a password every day?

---

## Options Considered

### Backups

1. **Manual backups only (status quo).** Depends on the user remembering; rejected.
2. **Android Auto Backup of the database.** A raw database file may come back with a schema older than the migration
   chain, it is not encrypted with the user's password and its timing is not under the app's control; rejected (as
   already decided in `backup_rules.xml`).
3. **Unencrypted automatic backup.** Anyone who gets the file can read the medication list; rejected.
4. **Backup password derived from the app PIN.** A 4–6 digit PIN can be brute-forced offline against a backup file;
   rejected.
5. **Automatic encrypted backup with a password stored on the device, written to a user-chosen folder and to Google
   Drive.** Chosen.

### Forgotten PIN

1. **No recovery (status quo).** Locks the owner out of their own data; rejected.
2. **Security question or e-mail reset.** Needs an account or adds weak secrets; rejected.
3. **The phone's own screen lock (device credential).** Chosen.

### Corrupt database

1. **Library default (delete).** Loses all data silently; rejected.
2. **Keep failing until fixed.** The app becomes unusable; rejected.
3. **Copy the files, then let the library continue.** Chosen.

---

## Decision

### Automatic backup

- Off by default. The user turns it on, chooses a folder with the phone's own folder picker and sets a backup password
  once.
- One backup per day (periodic background work). It is skipped when the data has not changed since the last backup
  (hash of the plain export).
- Same encrypted format as the manual backup (`BackupCrypto`: PBKDF2-HMAC-SHA256 + AES-GCM, `CURALIS_ENC_V1:`), so the
  existing restore screen opens it, on any phone, with the password.
- Destinations:
  - the chosen folder, which survives uninstalling the app;
  - Google Drive's hidden app folder (`appDataFolder`), only when Drive is connected **and** automatic Drive backup is
    turned on (a separate opt-in, off by default).
- Each destination keeps the 30 newest backups as dated files; older ones are deleted. The legacy single Drive file
  stays readable.
- A failure (lost folder permission, Drive authorization needing the user) posts one notification; backups never fail
  silently.

### Stored backup password

- Encrypted with an AES-GCM key held in the Android Keystore (non-exportable, no user authentication required, so the
  daily backup can run while the phone is locked) and kept in a dedicated preferences file.
- That file is excluded from Android Auto Backup and device transfer: the Keystore key does not move to a new phone,
  so the copy would be useless there.
- Never logged or shown. Restoring on a new phone needs the password itself; the app tells the user to note it.

### Corrupt database

- The SQLite open helper is wrapped: before the library's default deletion, the database, `-wal` and `-shm` files are
  copied to app-private storage (`db-rescue/<timestamp>/`). A small number of rescue copies is kept.
- The recovery screen asks for confirmation, copies the files the same way before resetting, and offers to export the
  copy to a file the user chooses.

### Forgotten PIN

- The lock screen offers "Forgot PIN". The phone's own screen lock (PIN, pattern, password or biometrics) unlocks the
  app and leads to setting a new PIN. If the phone has no screen lock, the app says so; there is no other bypass.

---

## Consequences

### Positive

- Losing the phone, uninstalling or a corrupt file costs at most one day of records, and restoring works on a new phone.
- A bad backup can no longer replace every good one.
- The owner can always get back into the app.

### Negative and accepted risks

- The backup password is on the device. Someone with the unlocked phone cannot read it, but malware with root access
  could use the Keystore key on that device. This matches the threat model of the unencrypted local database.
- Whoever knows the phone's screen lock can open the app through "Forgot PIN". The app PIN protects against casual
  access on an unlocked phone, not against the phone's owner.
- Rescue copies are as protected as the database itself (app-private storage), not more.
- Up to 30 small files per destination use storage (each backup is a few tens of kilobytes).

### Privacy

- Data leaves the device only to Google Drive, and only after the user connected Drive and turned on automatic Drive
  backup. Both are explicit, separate choices.
- Backups stay encrypted with the user's password in every destination.

---

## References

- ADR-001 Local-First Architecture
- ADR-006 Room Database
- `docs/engineering/security.md` (Backup Security)
- `docs/compliance/privacy.md` (Local Storage, Cloud Synchronization, Encryption)
- `app/src/main/res/xml/backup_rules.xml`, `data_extraction_rules.xml`
