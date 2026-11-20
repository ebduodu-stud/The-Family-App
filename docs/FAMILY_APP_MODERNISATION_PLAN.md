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
