# 🚀 GitHub Upload Instructions

## Prerequisites
1. You have a GitHub account: `faheemabbas14`
2. You have generated a GitHub Personal Access Token (PAT)

## Step 1: Create Repository on GitHub

1. Go to https://github.com/new
2. Fill in the details:
   - **Repository name**: `PocketApp` (or `pocket-app`)
   - **Description**: `A full-featured Android app for managing tasks, expenses, and events with Firebase integration and real-time sync`
   - **Visibility**: Public
   - **Initialize with**: Do NOT add README, .gitignore, or license (we have our own)
3. Click "Create repository"

## Step 2: Connect Local Repo to GitHub

Once your repository is created, you'll see a page with commands. Use these exact commands:

```bash
cd /Users/faheemabaas/AndroidStudioProjects/MyApplication

# Set the main branch
git branch -M main

# Add GitHub remote
git remote add origin https://github.com/faheemabbas14/PocketApp.git

# Push to GitHub
git push -u origin main
```

**Note**: When prompted for authentication, use:
- Username: `faheemabbas14`
- Password: Use your GitHub Personal Access Token (not your password)

## Step 3: Generate GitHub Personal Access Token

If you haven't created a token:

1. Go to GitHub Settings: https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Name: `MyApplicationToken`
4. Select scopes:
   - ☑️ repo (full control of private repositories)
   - ☑️ workflow
5. Click "Generate token"
6. **Copy the token immediately** (you won't see it again!)
7. Use this token as your password when git prompts

## Step 4: Verify Upload

After pushing, verify on GitHub:
```
https://github.com/faheemabbas14/PocketApp
```

---

## Alternative: Using HTTPS with Stored Credentials

After first push, macOS will offer to save credentials in Keychain. Accept this to avoid re-entering token each time.

---

## Troubleshooting

**Q: "fatal: destination path already exists and is not an empty directory"**
- The origin is already set. Use: `git remote set-url origin https://github.com/faheemabbas14/PocketApp.git`

**Q: "fatal: 'origin' does not appear to be a 'git' repository"**
- Add remote again: `git remote add origin https://github.com/faheemabbas14/PocketApp.git`

**Q: "Authentication failed"**
- Make sure you're using a Personal Access Token, not your GitHub password
- Token needs `repo` and `workflow` scopes

**Q: "Permission denied (publickey)"**
- You're trying SSH. Use HTTPS URL instead

---

## Repository Structure

Your uploaded repository will contain:

```
PocketApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/faheem/pocketapp/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MainViewModel.kt
│   │   │   │   ├── AlarmScheduler.kt
│   │   │   │   ├── AuthCache.kt
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Theme.kt
│   │   │   │       ├── Color.kt
│   │   │   │       └── Type.kt
│   │   │   └── res/
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle.kts
│   ├── google-services.json
│   └── proguard-rules.pro
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── gradlew & gradlew.bat
├── settings.gradle.kts
├── .gitignore
├── README.md
├── LICENSE
└── firebase.json

```

---

## What Gets Uploaded

✅ All source code (Kotlin, XML, Compose)
✅ Gradle configuration
✅ Firebase configuration (google-services.json)
✅ Build configuration
✅ Documentation

❌ Build artifacts (build/ folder)
❌ IDE configuration (.idea/ folder)
❌ Gradle cache (.gradle/ folder)

---

## After Upload

1. Your repo will be at: `https://github.com/faheemabbas14/PocketApp`
2. You can share this link with others
3. Others can clone with: `git clone https://github.com/faheemabbas14/PocketApp.git`

---

**Let me know once you've created the GitHub repo, and I'll automatically push your code!**

