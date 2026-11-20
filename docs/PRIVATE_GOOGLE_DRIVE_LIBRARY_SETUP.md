# Private Google Drive Library — setup and security checklist

This guide is for the second phase of The Family's App. The selected arrangement is private family access with a shared family access code and Google-account-controlled administrators.

## What the finished arrangement will do

- Family members enter the family access code once per device. The app remembers a revocable device session and provides read-only access.
- Administrators use their own Google accounts. The server checks each administrator's email against an allowlist before accepting a management action.
- Library files remain in a private Google Drive folder. Do not enable “Anyone with the link” on the folder or its contents.
- Categories are catalogue records, not hard-coded app screens. Adding a category in the admin interface makes it available after the app refreshes.
- Each document has a stable ID and a revision number. Replacing a document creates a new revision for the same logical item.

## What we need from the owner

1. Select the Google account that will own the Library folder and server-side integration.
2. In that account's Google Drive, create a folder named The Family's App — Library.
3. Keep the folder restricted. Add only the intended administrator accounts as editors. Do not share it publicly.
4. Keep the folder ID available for private server configuration; do not put it in the app's public source code.
5. Decide which Google account email addresses should have administrator privileges.
6. Create the family access code privately during setup. Do not commit it to GitHub, put it in a public document, or send it in an ordinary chat message.

## Security requirements

- Never place Google passwords, OAuth refresh tokens, service-account keys, or the family access code in the website, Android assets, or public GitHub repository.
- The server stores a verifier for the family code, not the code itself; it should rate-limit failed attempts and issue revocable sessions.
- Every administrator operation must be authorised server-side using a verified Google identity and an explicit email allowlist.
- The family API is read-only. Upload, replace, archive, delete, and category-management operations are restricted to administrators.
- Do not solve file delivery by making the Drive folder or its files public. The server must mediate access to private files.
- Test realistic PDF sizes and Android downloads. If the chosen serverless platform cannot safely proxy the required file sizes, use a proper server-side file proxy rather than weakening Drive permissions.

## Deployment sequence

1. Confirm the owner account, folder, and administrator email allowlist.
2. Configure the server-side secrets and Google authorisation scopes.
3. Deploy separate family-read and administrator-management access paths.
4. Test valid and invalid family codes, rate limiting, expired/revoked sessions, unauthorised administrator accounts, and private file downloads.
5. Test a category addition and a document replacement, checking that the stable ID remains and the revision increments.
6. Connect the web and Android Library screens to the tested catalogue API.
7. Enable production synchronisation only after all tests pass.

## Current state

This is a setup specification only. No Drive folder is connected, no credentials or codes have been configured, and the live Library API is not enabled.
