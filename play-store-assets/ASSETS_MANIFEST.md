# POCKET APP - ASSETS MANIFEST

Generated: March 17, 2026
Package: com.faheemlabs.pocketapp
App Version: 1.0.0

## Directory Structure Overview

```
play-store-assets/
├── app-icons/                          # Application icons for Play Store
│   ├── ic_launcher_108x108.png         # Adaptive icon (108x108)
│   ├── ic_launcher_192x192.png         # High-res icon (192x192)
│   └── ic_launcher_512x512.png         # Store listing icon (512x512) ⭐
├── screenshots/                        # Phone screenshots for listing
│   ├── screenshot_1_phone.png          # Tasks Management (1080x1920)
│   ├── screenshot_2_phone.png          # Expense Tracking (1080x1920)
│   ├── screenshot_3_phone.png          # Event Planning (1080x1920)
│   └── screenshot_4_phone.png          # Payment Schedule (1080x1920)
├── feature-graphics/                   # Feature banner (top of listing)
│   └── feature_graphic_1024x500.png    # Header graphic (1024x500) ⭐
├── promotional-graphics/               # Additional marketing materials
│   ├── landscape_banner_1200x628.png   # Social media landscape
│   └── promo_square_500x500.png        # Social media square
├── icon-packs/                         # Reserved for future use
├── README.md                           # Quick start guide
├── UPLOAD_GUIDE.txt                    # Detailed Play Store upload steps ⭐
├── BRAND_GUIDELINES.md                 # Branding and design standards
├── ASSETS_MANIFEST.md                  # This file
└── QUICK_REFERENCE.txt                 # Quick reference card

```

## Files Required for Play Store Upload (PRIORITY)

⭐ **ESSENTIAL FILES** (Must Upload):
1. **ic_launcher_512x512.png**
   - Location: app-icons/
   - Use for: App icon in Play Store listing
   - Size: 512 × 512 pixels
   - Format: PNG (with transparency)

2. **Feature Graphics (1024x500)**
   - Location: feature-graphics/feature_graphic_1024x500.png
   - Use for: Header banner on app listing page
   - Size: 1024 × 500 pixels
   - Format: PNG or JPEG

3. **Phone Screenshots (minimum 2, maximum 8)**
   - Location: screenshots/screenshot_*_phone.png
   - Use for: Show app features and interface
   - Size: 1080 × 1920 pixels each
   - Format: PNG or JPEG
   - Provided: 4 screenshots

## Optional Assets

📱 **RECOMMENDED** (Enhance Listing):
- landscape_banner_1200x628.png - For marketing/social media
- promo_square_500x500.png - For social media promotion

## File Specifications

### App Icon (512x512)
```
File: app-icons/ic_launcher_512x512.png
Size: Exactly 512 × 512 pixels
Format: PNG-24 or PNG-32 with transparency
Max File Size: 1 MB
Color Space: RGB or RGBA
Corners: Do NOT round (Play Store adds them automatically)
Margins: Include ~5% safe zone from edges
```

### Feature Graphic
```
File: feature-graphics/feature_graphic_1024x500.png
Size: Exactly 1024 × 500 pixels
Format: PNG or JPEG
Max File Size: 1 MB
Orientation: Landscape (must be horizontal)
Text Safe Zone: Center 874 × 500 (avoid edges)
Uses: Header of app listing on Play Store
```

### Screenshots
```
Files: screenshots/screenshot_*_phone.png
Size: Exactly 1080 × 1920 pixels
Format: PNG or JPEG
Aspect Ratio: 9:16 (portrait orientation)
Max File Size: 8 MB each
Count: Minimum 2, Maximum 8 (provided: 4)
Order: Arrange in logical feature order
Language: Can be localized for different countries
```

## Upload Sequence

### Step 1: Login to Google Play Console
- URL: https://play.google.com/console
- App: Pocket App (com.faheemlabs.pocketapp)
- Section: Store listing

### Step 2: Upload App Icon
- File: app-icons/ic_launcher_512x512.png
- Click: App icon section
- Action: Upload PNG file

### Step 3: Upload Screenshots
- Files: screenshots/screenshot_*.png (all 4)
- Click: Add screenshots
- Action: Drag and drop all screenshot files

### Step 4: Upload Feature Graphic
- File: feature-graphics/feature_graphic_1024x500.png
- Click: Feature graphic section
- Action: Upload PNG/JPEG file

### Step 5: Add Text Content
- Short description (80 chars max)
- Full description (4000 chars max)
- See README.md for sample text

### Step 6: Configure Settings
- Category: Productivity
- Content rating: Complete questionnaire
- Privacy policy: Add your privacy policy URL
- Contact email: Add support email

### Step 7: Upload App Bundle
- File: app/build/outputs/bundle/release/app-release.aab
- Build: Run `./gradlew bundleRelease`
- Upload: In "Releases" section

## Quality Checklist

### Design Quality
- [ ] Icon clearly represents app purpose
- [ ] Feature graphic is professional and clear
- [ ] Screenshots showcase main features
- [ ] Consistent branding across all assets
- [ ] Text is readable and well-positioned
- [ ] No offensive or inappropriate content

### Technical Requirements
- [ ] All images correct dimensions
- [ ] PNG/JPEG files not corrupted
- [ ] File sizes within limits
- [ ] No watermarks or external logos
- [ ] Screenshots from actual app (not mockups)

### Store Policy Compliance
- [ ] No fake reviews or ratings mentioned
- [ ] No misleading claims about features
- [ ] No adult content or violence
- [ ] Screenshots reflect actual app experience
- [ ] Branding consistent with privacy policy

## Customization Tips

To enhance these assets for your specific market:

### Localization
1. Create language-specific versions of screenshots
2. Translate text visible in feature graphic
3. Adapt colors if needed for cultural preferences

### Market-Specific
1. Adjust promotional graphics for regional events
2. Create seasonal promotional materials
3. Add localized testimonials if available

### Ongoing Updates
1. Update screenshots with new features quarterly
2. Refresh promotional graphics for campaigns
3. Update feature graphic for special promotions

## Support & Resources

### Documentation Included
- README.md - Quick start guide
- UPLOAD_GUIDE.txt - Step-by-step instructions
- BRAND_GUIDELINES.md - Design standards
- This file - Complete manifest

### External Resources
- Google Play Console: https://play.google.com/console
- Developer Policy Center: https://play.google.com/about/developer-content-policy/
- Material Design 3: https://m3.material.io
- Android Developers: https://developer.android.com

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-03-17 | Initial asset generation |

---

**Note**: Keep this manifest for reference when updating assets in the future.
All assets are generated following Google Play Store best practices and Material Design 3 guidelines.

