# Curalis

Local-first medication and health tracker for Android (Kotlin, Jetpack Compose, Material 3, Hilt, Room).

## Build & test
- Verify every change: `./gradlew testDebugUnitTest lintDebug` — all unit tests pass, lint clean
  (fix new warnings, e.g. PluralsCandidate → `<plurals>`; do not add them to `app/lint-baseline.xml`).
- Release APK: `./gradlew assembleRelease` (signed from `local.properties`; builds without it).
- CI (`.github/workflows/android.yml`): JDK 17, Gradle wrapper validation, `./gradlew test assembleDebug`.
- Test UI on the **release** build (R8 is on). Install with `adb -s <serial> install -r app/build/outputs/apk/release/app-release.apk`.
- **Never run `./gradlew install*` or `connected*`**: they target every attached device, including a personal phone.
  Use `assemble*` + `adb -s <serial>`.

## Layout
- Code root `app/src/main/java/com/gokcank/curalis/`: `core/` (notification = reminder engine, work, theme,
  security, utils/PdfReportGenerator, widget), `domain/` (model, usecase), `data/` (Room DB, backup, provider),
  `presentation/` (one Compose screen + HiltViewModel per folder).
- Room schemas in `app/schemas/`; migration chain in `data/local/DatabaseMigrations.kt` (`CURALIS_MIGRATIONS`).
  Every schema change needs a migration + `DatabaseMigrationsTest` coverage.
- Drug dictionary: `app/src/main/assets/database/medications.db`, rebuilt by `tools/build_titck_dictionary.py`.
- Tests use Robolectric; new DAO/repository methods need overrides in the private fake repositories inside test files (e.g. `FakeReminderRepository`).
- `CHANGELOG.md` is English, one entry per version (`## [x.y.z] — versionCode N`).

## Documentation is the source of truth
- If code conflicts with `docs/`, the docs win until intentionally updated. Do not silently change documented behavior.
- Before implementing, read: `docs/index.md` → `docs/foundation/philosophy.md` → `docs/product/product.md` →
  `docs/architecture/architecture.md`.
- Read on demand, by topic:
  - Reminders: `docs/product/reminders.md` · Database: `docs/architecture/database.md`
  - Providers: `docs/architecture/providers.md` · Sync: `docs/architecture/sync.md`
  - Medical logic: `docs/compliance/medical-safety.md` · Privacy: `docs/compliance/privacy.md`
  - UI: `docs/design/ui.md`, `docs/design/design-system.md` · Security: `docs/engineering/security.md`
  - Coding: `docs/engineering/coding.md` · Testing: `docs/engineering/testing.md` · ADRs: `docs/adr/`
- When behavior changes, update the docs (and roadmap/glossary if needed); the change is not done until then.
  If docs are missing, propose them before implementing.
- Conflict priority: philosophy → product → architecture → topic docs → ADRs → code.
- **ADR rule**: propose an ADR and pause implementation until it is accepted when a change affects architecture,
  project structure, data model, public APIs, provider integrations, security, privacy, sync, the design system,
  development workflow or long-term direction.

## Project rules
- **Architecture boundaries**: UI → ViewModel → UseCase → Repository → Provider/DB. Business logic stays
  framework-independent.
- **Medical safety**: never implement medical advice, dosage or treatment recommendations, or diagnosis. Medication
  data comes only from the trusted provider (TİTCK dictionary) or explicit user input.
- **Privacy**: no unnecessary data collection; cloud features stay optional; user data belongs to the user.
  Never log sensitive information or disable certificate validation.
- **Dependencies**: check necessity, maintenance, license and long-term viability first.
- **Reference app anonymity**: internal reference apps (more than one) are used as benchmarks in chat and local,
  git-ignored review files only. Their names must never appear in code, comments, UI strings, commit messages or
  public docs; say "a reference app". Never copy their code, text, images or sounds. All work stays original.
- **Agreed reminder/adherence behavior** (time changes "from today/tomorrow", late dose = half weight, binding
  treatment duration, single-dose reschedule, taken-time rules): do not change without asking. Adherence math lives
  only in `DoseAdherence`; DST-safe day math only via `FrequencyCalculator.calendarDaysBetween`.
- **Design identity**: list/hub screens use full section color, add/edit forms accent only, settings/utility screens
  `colorScheme.primary`; PIN lock screen untouched. Use `core/theme/SectionColors.kt` +
  `rememberSectionThemeMode()`; no per-screen color constants. Allow text wrapping (large fonts).
- **Distribution**: sideloaded APKs via GitHub releases only (not on Google Play). Do not suggest Play uploads, AABs,
  store listings or Play policy work unless asked. Keep `USE_EXACT_ALARM`.
- Release notes and store-facing text: **Turkish only** by default.

## Status (as of 2026-11-22)
- Project status: **active** (set 2026-11-18; source of truth: the brain page).
- Latest release 1.4.1 (versionCode 11), 2026-10-14; Room DB version 24; CI green at that commit.
  No app-code change since; 1.4.1 was installed on the phone as a first install on 2026-11-14.
- Reference-app scan finished (2026-11-22). A 22-step revision plan exists locally (bug fixes → 1.4.2, then
  feature groups → 1.5.0 / 1.6.0 / 1.7.0); not approved or started yet. Bottom nav, add-medication form, easy mode and
  profiles wait for a separate design session together with the menu topic.

## Open items
- Remove the reference app's name from source comments and `CHANGELOG.md`. It is also in 3 pushed commit messages;
  cleaning those needs a history rewrite — ask first.
- Remove the unused `WEB_CLIENT_ID` BuildConfig field from `app/build.gradle.kts`.
- README version badge is stuck at v1.1.0.
- PDF: preview card lacks the late-dose count; archived medications still appear in the medicine-cabinet list.
- Stock bug (static reading, not reproduced yet): stock decrement and the low-stock warning run only in
  `ReminderActionReceiver` (notification + full-screen). Taking a dose from the timeline, home or widget changes no
  stock; un-taking never refunds. Violates `docs/product/reminders.md` "Inventory Integration".
- Notification channel bug (static reading): `proactive_reminders_channel` is built but never registered, so the
  morning summary, vital reminders and the time-zone notification are probably dropped.

## More context
Background, decisions and history are in the second brain: `/home/gokcank/ikinci-beyin/projects/curalis.md` (read on demand).
