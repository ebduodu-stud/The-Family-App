# The Family's App — Modernisation Foundation

This document records the first agreed design decisions for the transition from **Family Morning Worship** to **The Family's App**. It is a planning foundation; it does not yet change the app's screens or connect Google Drive.

## Product direction

- App name: **The Family's App**
- Purpose: a shared family information and activity app, not an app limited to Morning Worship.
- Preserve existing Morning Worship schedules, assignments, Bible links, profile preferences, reminders, appearance settings, and offline behaviour while the wider app is developed.

## Initial Library sections

The following four sections have been supplied so far. Treat them as the initial list, not necessarily the final list:

| Section | Short code | Example file ID |
|---|---|---|
| Morning Worship Arrangements | MWA | MWA-2026-001 |
| Family Worship Arrangements | FWA | FWA-2026-001 |
| Cleaning Roster | CR | CR-2026-001 |
| Outing Plans | OP | OP-2026-001 |

IDs should be unique and stable. Replacing a document should retain its logical identity and increment a revision/version field rather than accidentally creating a second unrelated item. The app should use the category metadata, not infer a category from a filename alone.

## Library distribution and storage

- Google Drive is the proposed central document and image store.
- Drive management is administrator-controlled.
- Ordinary family members should be able to browse, open, and download Library items in the app without needing their own Google accounts.
- Administrators alone should be able to add, replace, organise, or remove Library items.
- The app should refresh the Library on launch and when a user requests refresh; periodic refresh while open may also be used.
- A new or updated item should appear for all family members after the app refreshes its central Library catalogue.
- WhatsApp is no longer required as the primary distribution mechanism if the Drive connection is implemented.

## Security and implementation constraints

- Never embed an administrator password, OAuth refresh token, or private Drive credential in the website or Android app.
- Do not expose a private administrator Drive folder directly as a public write surface.
- The final integration must use a secure administrator authorisation path and a safe read-only catalogue for family members.
- Google Drive is the planned storage provider; Firebase is not part of the agreed design.
- The exact Google Drive integration method, folder permissions, administrator sign-in, and catalogue mechanism must be selected and tested before enabling live uploads.
- Library categories should be data-driven rather than hard-coded, so an authorised administrator can add a category later, assign its short code, and have it appear in the app after refresh.
- Do not remove or replace the existing schedule data or offline fallback as part of the initial planning work.

## Pending decisions

- Final order of the four initial Library sections; future additions should be supported dynamically by category metadata.
- Whether administrators can be more than one person.
- Whether each section displays the newest item prominently on the home screen.
- Whether announcements should be shown as text posts, attached files, or both.
- Rules for archiving and deleting older documents.
- Final file ID/revision format and administrator workflow.

## Implementation progress — first phase

The first interface implementation has now been committed:

- Website title, PWA name, Android launcher label, and main home heading changed to **The Family’s App**.
- The existing Android application ID and namespace remain unchanged to preserve upgrade compatibility.
- The former Schedule Files screen is now a Library with the four initial category cards and category codes.
- The three existing Morning Worship PDF schedules are listed under MWA with stable example IDs.
- FWA, CR, and OP have ready category pages with clear empty states.
- The About section describes the broader family purpose.
- The introduction/tutorial includes the new Library.
- Existing Morning Worship assignments, personal profiles, reminders, links, and offline fallback are retained.

This is an initial Library interface, not yet a live shared Drive Library. Category cards and existing schedule files are currently app data; family-wide publishing, dynamic category management, administrator sign-in, and automatic catalogue refresh are still pending.

## Current status

First interface phase committed; web deployment and signed Android build are being generated. No Google account has been connected, no Drive folder has been configured, and no live upload or synchronisation feature is active.


## Phase 2 — private shared Library access (decision confirmed)

The family selected the private-access option:

- Family members enter one shared family access code; the app remembers the authorised device locally.
- Family access is read-only: list categories and published documents, open and download items.
- Administrators authenticate with their own authorised Google accounts for publishing and management.
- The Google Drive storage folder remains private. The app must not make files public-by-link merely to make them viewable.
- The family access code is stored only as a secure verifier on the server, not embedded in website or Android source. Use rate limiting and a revocable session token after code verification.
- Administrator authorisation must be checked server-side against an allowlist of Google account email addresses. Do not rely on a hidden button or client-side role flag.
- The public read API and administrator management interface must be separate permission paths. No Drive OAuth token, refresh token, or service credential may reach the client.
- The catalogue is data-driven: category records and document records include stable IDs, category codes, revision/version, status, timestamps, and Drive file IDs. Replacing a document increments its revision while preserving its logical ID.
- The app refreshes the catalogue on launch and on user request; it must retain a clear offline/last-synced state if the service is unavailable.

### Proposed implementation approach

Use a server-side Google Apps Script integration as the initial cost-conscious broker to a private Drive folder. A public, read-only family endpoint validates the family code and returns catalogue data; document downloads are mediated by the server rather than exposing a public Drive permission. A separate administrator interface requires Google sign-in and server-side email allowlisting. Deployments, scopes, quotas, file-size limits, and cross-origin behaviour must be tested before production use. If Apps Script cannot safely and reliably proxy the family's actual document sizes, move the broker to a server-side hosting service rather than making the Drive folder public.

### Configuration still required

- A Google account selected to own the Drive folder and Apps Script project.
- A private Drive folder for the Library (folder ID configured server-side only).
- The administrator Google account email address(es) to allowlist.
- A family access code to be set securely during setup; do not commit it to GitHub or send it in chat.
- Test uploads and downloads, including a PDF, from both the web app and Android app before enabling live Library synchronisation.

No Google account, Drive folder, administrator list, or family code has been configured yet. The live connection remains disabled until setup and security testing are complete.
