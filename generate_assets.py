#!/usr/bin/env python3
"""
Generate all Play Store assets for Pocket App
"""

import os
from PIL import Image, ImageDraw, ImageFont
import textwrap

# Define colors for Pocket App (using a modern teal/blue theme)
PRIMARY_COLOR = (26, 118, 161)  # Professional blue
SECONDARY_COLOR = (76, 175, 80)  # Green accent
ACCENT_COLOR = (255, 152, 0)  # Orange accent
WHITE = (255, 255, 255)
DARK_BG = (245, 245, 245)  # Light background

BASE_PATH = "/Users/faheemabaas/AndroidStudioProjects/MyApplication/play-store-assets"

def create_app_icon(size, filename, rounded=False):
    """Create app icon with Pocket App branding"""
    img = Image.new("RGB", (size, size), color=PRIMARY_COLOR)
    draw = ImageDraw.Draw(img)

    # Create a simplified pocket icon
    margin = int(size * 0.15)

    # Draw outer pocket shape (rectangle with rounded top corners)
    pocket_left = margin
    pocket_top = int(margin * 1.5)
    pocket_right = size - margin
    pocket_bottom = size - margin

    # Main pocket rectangle
    draw.rectangle([pocket_left, pocket_top, pocket_right, pocket_bottom],
                   fill=WHITE, outline=WHITE, width=2)

    # Draw a simple "P" letter for Pocket
    try:
        # Try to use a system font, fall back to default if not available
        font_size = int(size * 0.5)
        font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", font_size)
    except:
        font = ImageFont.load_default()

    text = "P"
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]

    text_x = (size - text_width) // 2
    text_y = (size - text_height) // 2 - int(size * 0.1)

    draw.text((text_x, text_y), text, fill=PRIMARY_COLOR, font=font)

    # Add accent color at bottom
    accent_height = int(size * 0.08)
    draw.rectangle([0, size - accent_height, size, size], fill=SECONDARY_COLOR)

    if rounded:
        # Create rounded corners
        img = img.convert("RGBA")
        radius = int(size * 0.15)
        # Create a mask with rounded corners
        mask = Image.new('L', (size, size), 0)
        mask_draw = ImageDraw.Draw(mask)
        mask_draw.rounded_rectangle([0, 0, size, size], radius=radius, fill=255)
        img.putalpha(mask)
        return img

    return img

def create_screenshot(width, height, title, subtitle, screen_num):
    """Create a screenshot mockup"""
    img = Image.new("RGB", (width, height), color=DARK_BG)
    draw = ImageDraw.Draw(img)

    # Top bar with primary color
    bar_height = int(height * 0.08)
    draw.rectangle([0, 0, width, bar_height], fill=PRIMARY_COLOR)

    # Title in top bar
    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", int(height * 0.035))
        font_text = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", int(height * 0.025))
    except:
        font_title = ImageFont.load_default()
        font_text = ImageFont.load_default()

    draw.text((int(width * 0.05), int(bar_height * 0.25)), "Pocket App",
             fill=WHITE, font=font_title)

    # Main content area
    content_start = int(bar_height * 1.5)
    padding = int(width * 0.1)

    # Draw title
    draw.text((padding, content_start), title, fill=PRIMARY_COLOR, font=font_title)

    # Draw subtitle with wrapping
    subtitle_y = content_start + int(height * 0.08)
    for line in textwrap.wrap(subtitle, width=40):
        draw.text((padding, subtitle_y), line, fill=(100, 100, 100), font=font_text)
        subtitle_y += int(height * 0.035)

    # Draw some mock content boxes
    box_y = subtitle_y + int(height * 0.05)
    for i in range(3):
        # Box background
        draw.rectangle([padding, box_y, width - padding, box_y + int(height * 0.12)],
                      fill=WHITE, outline=PRIMARY_COLOR, width=2)

        # Mock content in box
        item_text = f"Item {i+1}"
        draw.text((padding + int(width * 0.02), box_y + int(height * 0.02)),
                 item_text, fill=PRIMARY_COLOR, font=font_text)

        box_y += int(height * 0.14)

    return img

def create_feature_graphic(width, height):
    """Create a 1024x500 feature graphic"""
    img = Image.new("RGB", (width, height), color=PRIMARY_COLOR)
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", int(height * 0.4))
        font_subtitle = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", int(height * 0.2))
    except:
        font_title = ImageFont.load_default()
        font_subtitle = ImageFont.load_default()

    # Draw semi-transparent overlay gradient effect with rectangles
    overlay_colors = [
        (26, 118, 161),
        (36, 140, 188),
        (46, 162, 215)
    ]

    for i, color in enumerate(overlay_colors):
        section_width = width // len(overlay_colors)
        draw.rectangle([i * section_width, 0, (i + 1) * section_width, height], fill=color)

    # Title
    title = "Pocket App"
    bbox = draw.textbbox((0, 0), title, font=font_title)
    title_width = bbox[2] - bbox[0]
    title_x = (width - title_width) // 2
    title_y = int(height * 0.15)
    draw.text((title_x, title_y), title, fill=WHITE, font=font_title)

    # Subtitle
    subtitle = "Manage Tasks • Track Expenses\nPlan Events • Schedule Payments"
    draw.text((int(width * 0.1), int(height * 0.55)), subtitle, fill=WHITE, font=font_subtitle)

    return img

def create_promotional_graphics():
    """Create various promotional graphics"""
    promotions = {}

    # 1200x628 landscape banner
    img = Image.new("RGB", (1200, 628), color=SECONDARY_COLOR)
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 80)
        font_text = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 40)
    except:
        font_title = ImageFont.load_default()
        font_text = ImageFont.load_default()

    # Left side - gradient effect
    for i in range(0, 600, 50):
        alpha = int(255 * (i / 600))
        draw.rectangle([0, 0, i, 628], fill=(76, 175, 80))

    draw.text((100, 150), "Pocket App", fill=WHITE, font=font_title)
    draw.text((100, 280), "Complete Task Management", fill=WHITE, font=font_text)
    draw.text((100, 350), "Manage your life efficiently", fill=(240, 240, 240), font=font_text)

    # Add some decorative circles
    circle_radius = 40
    for x, y in [(1100, 150), (1000, 500)]:
        draw.ellipse([x-circle_radius, y-circle_radius, x+circle_radius, y+circle_radius],
                    fill=ACCENT_COLOR, outline=WHITE, width=3)

    promotions['landscape_banner_1200x628.png'] = img

    # 500x500 promotional square
    img = Image.new("RGB", (500, 500), color=PRIMARY_COLOR)
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 60)
        font_text = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", 28)
    except:
        font_title = ImageFont.load_default()
        font_text = ImageFont.load_default()

    draw.text((50, 100), "Pocket App", fill=WHITE, font=font_title)
    draw.text((50, 200), "All-in-One", fill=ACCENT_COLOR, font=font_text)
    draw.text((50, 240), "Life Manager", fill=ACCENT_COLOR, font=font_text)
    draw.text((50, 300), "Tasks • Expenses", fill=WHITE, font=font_text)
    draw.text((50, 340), "Events • Payments", fill=WHITE, font=font_text)

    # Add corner design
    draw.rectangle([450, 0, 500, 50], fill=SECONDARY_COLOR)
    draw.rectangle([0, 450, 50, 500], fill=SECONDARY_COLOR)

    promotions['promo_square_500x500.png'] = img

    return promotions

def create_directory_structure():
    """Create text file explaining directory structure"""
    content = """# Pocket App - Play Store Assets Guide

## Directory Structure

### app-icons/
Contains all app icon variations:
- ic_launcher_108x108.png - Adaptive icon base
- ic_launcher_192x192.png - High resolution icon
- ic_launcher_512x512.png - Extra high resolution (for Play Store listing)

### screenshots/
Phone screenshots for Play Store listing (minimum 2-8 screenshots):
- screenshot_1_phone.png - 1080x1920 (9:16 aspect ratio)
- screenshot_2_phone.png
- screenshot_3_phone.png
- screenshot_4_phone.png

### feature-graphics/
- feature_graphic_1024x500.png - Required feature graphic for listing header

### promotional-graphics/
Additional promotional materials:
- landscape_banner_1200x628.png - Social media banner
- promo_square_500x500.png - Social media square graphic

### icon-packs/
Alternative icon variations for different themes

## Play Store Upload Instructions

1. Go to Google Play Console (https://play.google.com/console)
2. Select your app (com.faheemlabs.pocketapp)
3. Navigate to "Store listing"

### Icon Uploads:
- **App Icon (512x512)**: Upload in "App icon"
  - File: app-icons/ic_launcher_512x512.png
  - Requirements: PNG, RGB, exactly 512x512px

### Screenshots:
- Minimum: 2 screenshots
- Maximum: 8 screenshots
- Aspect ratio: 9:16 (Portrait) - Recommended 1080x1920px
- Files: screenshots/screenshot_*_phone.png

### Feature Graphic:
- Dimensions: 1024x500px
- Format: PNG or JPEG
- File: feature-graphics/feature_graphic_1024x500.png
- This appears at the top of your app listing

### Promotional Graphics:
- Optional but recommended
- Use for social media and marketing
- Recommended sizes:
  - 1200x628px - Facebook, LinkedIn headers
  - 500x500px - Social media square posts

## Asset Specifications

### Colors Used:
- Primary Blue: #1A76A1 (26, 118, 161)
- Secondary Green: #4CAF50 (76, 175, 80)
- Accent Orange: #FF9800 (255, 152, 0)
- White: #FFFFFF
- Dark Gray: #F5F5F5

### App Description:
**Pocket App** - Manage tasks, expenses and events all in one place

### Key Features:
- Plan and track your work with Tasks
- Control daily spending with Expense tracking
- Stay ahead of upcoming plans with Events
- Manage future payments with Payment scheduling
- Get timely reminders for everything

## Recommended Download and Upload Steps

1. Download the play-store-assets.zip file
2. Extract it locally
3. Review all graphics in the app-icons/, screenshots/, and feature-graphics/ folders
4. Customize as needed with your branding
5. Upload to Google Play Console following the specifications above

## Next Steps

1. Add App Description (500-4000 characters)
2. Add Short Description (80 characters max)
3. Upload screenshots
4. Upload feature graphic
5. Set appropriate categories (Productivity)
6. Set content rating
7. Configure app pricing and distribution
8. Request Play Store review
"""

    with open(f"{BASE_PATH}/README.md", "w") as f:
        f.write(content)

def main():
    print("Generating Play Store assets for Pocket App...")

    # Create app icons
    print("✓ Creating app icons...")
    icon_sizes = [
        (108, "app-icons/ic_launcher_108x108.png"),
        (192, "app-icons/ic_launcher_192x192.png"),
        (512, "app-icons/ic_launcher_512x512.png"),
    ]

    for size, filename in icon_sizes:
        img = create_app_icon(size, filename)
        full_path = os.path.join(BASE_PATH, filename)
        img.save(full_path, "PNG")
        print(f"  ✓ Created {filename}")

    # Create screenshots
    print("✓ Creating screenshots...")
    screenshots = [
        ("Tasks Management", "Create, organize and track all your tasks with deadlines and reminders"),
        ("Expense Tracking", "Monitor your spending and control your daily expenses"),
        ("Event Planning", "Plan upcoming events and get reminders for important dates"),
        ("Payment Schedule", "Schedule and manage your future payments in one place"),
    ]

    for i, (title, subtitle) in enumerate(screenshots, 1):
        img = create_screenshot(1080, 1920, title, subtitle, i)
        filename = f"screenshots/screenshot_{i}_phone.png"
        full_path = os.path.join(BASE_PATH, filename)
        img.save(full_path, "PNG")
        print(f"  ✓ Created screenshot_{i}_phone.png")

    # Create feature graphic
    print("✓ Creating feature graphic...")
    img = create_feature_graphic(1024, 500)
    full_path = os.path.join(BASE_PATH, "feature-graphics/feature_graphic_1024x500.png")
    img.save(full_path, "PNG")
    print(f"  ✓ Created feature_graphic_1024x500.png")

    # Create promotional graphics
    print("✓ Creating promotional graphics...")
    promos = create_promotional_graphics()
    for filename, img in promos.items():
        full_path = os.path.join(BASE_PATH, f"promotional-graphics/{filename}")
        img.save(full_path, "PNG")
        print(f"  ✓ Created {filename}")

    # Create README
    print("✓ Creating documentation...")
    create_directory_structure()
    print(f"  ✓ Created README.md")

    print("\n✅ All assets generated successfully!")
    print(f"\nAssets location: {BASE_PATH}")

if __name__ == "__main__":
    main()

