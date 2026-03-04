#!/bin/bash

# Pocket App - GitHub Upload Script
# This script automates pushing your Pocket App to GitHub

echo "=========================================="
echo "Pocket App - GitHub Upload Script"
echo "=========================================="
echo ""

# Get GitHub username
read -p "Enter your GitHub username: " GITHUB_USERNAME

if [ -z "$GITHUB_USERNAME" ]; then
    echo "❌ GitHub username is required!"
    exit 1
fi

# Get repository name
read -p "Enter repository name (default: pocket-app): " REPO_NAME
REPO_NAME=${REPO_NAME:-pocket-app}

# Set remote URL
REMOTE_URL="https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"

echo ""
echo "Repository URL: $REMOTE_URL"
echo ""

# Navigate to project directory
cd /Users/faheemabaas/AndroidStudioProjects/MyApplication

# Check if git is initialized
if [ ! -d ".git" ]; then
    echo "Git not initialized. Initializing..."
    git init
    git config user.name "Faheem Abaas"
    git config user.email "kit.faheem@gmail.com"
    git add -A
    git commit -m "Initial commit: Pocket App with Firebase Auth and Firestore backup"
fi

# Add remote
echo "Adding remote repository..."
git remote remove origin 2>/dev/null || true
git remote add origin $REMOTE_URL

# Rename branch to main
echo "Setting up main branch..."
git branch -M main

# Push to GitHub
echo ""
echo "Pushing to GitHub..."
echo "📝 Note: You'll be prompted for authentication."
echo "   Use your GitHub Personal Access Token as password."
echo ""

git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "✅ SUCCESS!"
    echo "=========================================="
    echo "Your Pocket App is now on GitHub!"
    echo ""
    echo "Repository URL:"
    echo "  $REMOTE_URL"
    echo ""
    echo "View your repository:"
    echo "  https://github.com/$GITHUB_USERNAME/$REPO_NAME"
    echo ""
else
    echo ""
    echo "❌ Error pushing to GitHub"
    echo "Please check your internet connection and try again."
    exit 1
fi

