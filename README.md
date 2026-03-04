# Pocket App - Firebase Backup Storage

A Kotlin Jetpack Compose pocket storage app with Firebase Authentication login and Cloud Firestore per-user data backup.

## Features

- **Email/Password Authentication** — Secure login and registration
- **Pocket Notes/Tasks** — Save items with title + detailed notes
- **Cloud Backup** — Firebase Firestore auto-syncs all user data
- **Secure Access** — Only authenticated users can view their own data

## Firebase Project Setup ✅

**Project ID:** `daily-utility-faheem`  
**Package:** `com.faheem.pocketapp`  
**Auth:** Email/Password enabled  
**Firestore:** Deployed with user-scoped security rules

### Configuration Files
- `app/google-services.json` — Firebase SDK config for Pocket App
- `firestore.rules` — User-scoped read/write access control
- `firebase.json` — Firebase deployment configuration

## Build & Run

```bash
# Compile Kotlin
./gradlew :app:compileDebugKotlin

# Build APK
./gradlew :app:assembleDebug

# Install on device/emulator
./gradlew :app:installDebug
```

## Test Login

After building, test the app with:

1. **Register:** Enter any email + password (min 6 chars) → tap Register
2. **Create Entry:** Add a pocket item title and notes → tap Save
3. **Verify Backup:** Entry appears in the list (synced from Firestore)
4. **Sign Out/In:** Sign out, sign back in → entries reload from backup
5. **Delete:** Remove entry from list (deleted in Firestore)

### Example Test User
- Email: `test@example.com`
- Password: `Test123`

## Data Model

**Collection:** `users/{uid}/entries/{entryId}`

```json
{
  "title": "Shopping list",
  "note": "Milk, eggs, bread, cheese",
  "updatedAt": 1741000000000
}
```

## Architecture

- **MainActivity** — Compose UI (auth screen + pocket home screen)
- **MainViewModel** — Firebase Auth + Firestore sync logic
- **Firestore Rules** — Per-user data isolation and security

## Firestore Rules

```
rules_version = '2';
service cloud.firestore {
  match /users/{userId}/entries/{entryId} {
    allow read, write: if request.auth.uid == userId;
  }
}
```

Deployed via: `firebase deploy --only firestore:rules --project=daily-utility-faheem`

