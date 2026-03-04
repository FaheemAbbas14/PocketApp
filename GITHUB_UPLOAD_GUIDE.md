# GitHub Upload Instructions for Pocket App

## Step 1: Create a New Repository on GitHub

1. Go to https://github.com/new
2. Sign in with your GitHub account (kit.faheem@gmail.com)
3. Fill in the repository details:
   - **Repository name:** `pocket-app` (or your preferred name)
   - **Description:** `Pocket App - Firebase Backup Storage with Authentication`
   - **Visibility:** Select `Public` (or `Private` if preferred)
   - **Initialize with:** Leave unchecked (don't initialize README, .gitignore, license)
   - Click **Create repository**

## Step 2: Add Remote and Push Code

After creating the repository, you'll see instructions. Run these commands:

```bash
cd /Users/faheemabaas/AndroidStudioProjects/MyApplication

# Add remote repository (replace USERNAME with your GitHub username)
git remote add origin https://github.com/USERNAME/pocket-app.git

# Rename branch to main (if needed)
git branch -M main

# Push code to GitHub
git push -u origin main
```

### Example with Actual Username
If your GitHub username is `faheemabaas`, the command would be:
```bash
git remote add origin https://github.com/faheemabaas/pocket-app.git
git branch -M main
git push -u origin main
```

## Step 3: Authenticate with GitHub

When you run `git push`, you'll be prompted to authenticate:

### Option A: Personal Access Token (Recommended)
1. Go to https://github.com/settings/tokens
2. Click **Generate new token** → **Generate new token (classic)**
3. Give it a name (e.g., "Pocket App Upload")
4. Select scopes: `repo` (Full control of private repositories)
5. Click **Generate token**
6. Copy the token and use it as your password when prompted
7. Save the token in a safe place

### Option B: GitHub CLI
```bash
# If you have GitHub CLI installed
gh auth login
gh repo create pocket-app --source=. --remote=origin --push
```

## Step 4: Verify Upload

After pushing, verify on GitHub:
1. Go to `https://github.com/YOUR-USERNAME/pocket-app`
2. You should see all your project files
3. Confirm these are present:
   - `app/` folder with source code
   - `README.md`
   - `RENAME_COMPLETE.md`
   - `SETUP_COMPLETE.md`
   - `firestore.rules`
   - `firebase.json`
   - `build.gradle.kts` and other Gradle files

## Important Notes

⚠️ **Before Pushing:**
- The `app/google-services.json` file is included. Consider if you want to:
  - **Keep it:** Easy setup but exposes Firebase config (API keys visible but not secrets)
  - **Remove it:** Add to `.gitignore` and have users generate their own
  - **Recommendation:** Keep it for now (API key in config file is considered public)

## Step 5: Share Repository Link

Once uploaded, your repository will be at:
```
https://github.com/YOUR-USERNAME/pocket-app
```

Share this link for others to clone and use the project.

## Commands to Copy-Paste

```bash
cd /Users/faheemabaas/AndroidStudioProjects/MyApplication
git remote add origin https://github.com/YOUR-USERNAME/pocket-app.git
git branch -M main
git push -u origin main
```

---

**Need Help?**
- GitHub Docs: https://docs.github.com/en/get-started/importing-your-projects-to-github/importing-a-repository-with-github-cli
- Git Docs: https://git-scm.com/doc

