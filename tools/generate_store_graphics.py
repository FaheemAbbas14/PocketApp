from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path('/Users/faheemabaas/AndroidStudioProjects/MyApplication')
ASSET_ROOT = ROOT / 'play-store-assets'
ICON_DIR = ASSET_ROOT / 'app-icons'
FEATURE_DIR = ASSET_ROOT / 'feature-graphics'

ICON_512 = ICON_DIR / 'ic_launcher_512x512.png'
ICON_192 = ICON_DIR / 'ic_launcher_192x192.png'
ICON_108 = ICON_DIR / 'ic_launcher_108x108.png'
FEATURE_1024 = FEATURE_DIR / 'feature_graphic_1024x500.png'

BRAND_PRIMARY = (255, 122, 0)
BRAND_SECONDARY = (255, 162, 77)
WHITE = (255, 255, 255)


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in [
        '/System/Library/Fonts/SFNSDisplay.ttf',
        '/System/Library/Fonts/Helvetica.ttc',
    ]:
        try:
            return ImageFont.truetype(path, size)
        except Exception:
            continue
    return ImageFont.load_default()


def create_icon_master(size: int = 1024) -> Image.Image:
    """Create a clean icon matching provided foreground/background concept."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Rounded gradient background similar to icon background XML.
    for y in range(size):
        t = y / max(size - 1, 1)
        r = int(BRAND_PRIMARY[0] * (1 - t) + BRAND_SECONDARY[0] * t)
        g = int(BRAND_PRIMARY[1] * (1 - t) + BRAND_SECONDARY[1] * t)
        b = int(BRAND_PRIMARY[2] * (1 - t) + BRAND_SECONDARY[2] * t)
        draw.line((0, y, size, y), fill=(r, g, b, 255))

    radius = int(size * 0.22)
    mask = Image.new('L', (size, size), 0)
    mdraw = ImageDraw.Draw(mask)
    mdraw.rounded_rectangle((0, 0, size, size), radius=radius, fill=255)
    img.putalpha(mask)

    draw = ImageDraw.Draw(img)

    # White top circle from provided foreground vector.
    cx = int(size * 0.5)
    cy = int(size * 0.185)
    cr = int(size * 0.24)
    draw.ellipse((cx - cr, cy - cr, cx + cr, cy + cr), fill=(255, 255, 255, 255))

    # Stylized "P" stroke approximating provided vector path.
    stroke = int(size * 0.06)
    x = int(size * 0.435)
    y_top = int(size * 0.34)
    y_bottom = int(size * 0.70)
    draw.line((x, y_top, x, y_bottom), fill=BRAND_PRIMARY + (255,), width=stroke)

    x_right = int(size * 0.65)
    y_mid = int(size * 0.56)
    draw.line((x, y_top, x_right - int(stroke * 0.4), y_top), fill=BRAND_PRIMARY + (255,), width=stroke)

    arc_box = (
        int(size * 0.50),
        y_top,
        int(size * 0.76),
        y_mid,
    )
    draw.arc(arc_box, start=270, end=90, fill=BRAND_PRIMARY + (255,), width=stroke)
    draw.line((int(size * 0.63), y_mid, x, y_mid), fill=BRAND_PRIMARY + (255,), width=stroke)

    return img


def save_icons(icon_master: Image.Image) -> None:
    ICON_DIR.mkdir(parents=True, exist_ok=True)
    icon_master.resize((512, 512), Image.Resampling.LANCZOS).save(ICON_512, 'PNG', optimize=True)
    icon_master.resize((192, 192), Image.Resampling.LANCZOS).save(ICON_192, 'PNG', optimize=True)
    icon_master.resize((108, 108), Image.Resampling.LANCZOS).save(ICON_108, 'PNG', optimize=True)


def create_feature_graphic(icon_512: Image.Image) -> Image.Image:
    w, h = 1024, 500
    img = Image.new('RGB', (w, h), BRAND_PRIMARY)
    draw = ImageDraw.Draw(img)

    # Catchy orange gradient backdrop.
    for y in range(h):
        t = y / max(h - 1, 1)
        r = int(255 * (1 - t) + 248 * t)
        g = int(122 * (1 - t) + 174 * t)
        b = int(0 * (1 - t) + 88 * t)
        draw.line((0, y, w, y), fill=(r, g, b))

    # Soft decorative blobs.
    for bx, by, br, a in [(130, 90, 170, 45), (900, 90, 210, 35), (850, 420, 240, 28)]:
        overlay = Image.new('RGBA', (w, h), (0, 0, 0, 0))
        odraw = ImageDraw.Draw(overlay)
        odraw.ellipse((bx - br, by - br, bx + br, by + br), fill=(255, 255, 255, a))
        img = Image.alpha_composite(img.convert('RGBA'), overlay).convert('RGB')

    # Icon card with shadow.
    overlay = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    odraw = ImageDraw.Draw(overlay)
    odraw.rounded_rectangle((72, 88, 392, 408), radius=44, fill=(255, 255, 255, 240))
    shadow = overlay.filter(ImageFilter.GaussianBlur(10))
    base = Image.alpha_composite(img.convert('RGBA'), shadow)
    base = Image.alpha_composite(base, overlay)

    icon = icon_512.resize((220, 220), Image.Resampling.LANCZOS)
    base.alpha_composite(icon, (122, 138))

    img = base.convert('RGB')
    draw = ImageDraw.Draw(img)

    title_font = load_font(78)
    subtitle_font = load_font(34)
    chip_font = load_font(24)

    draw.text((430, 150), 'Pocket App', fill=WHITE, font=title_font)
    draw.text((430, 250), 'Tasks | Expenses | Events | Payments', fill=(255, 245, 235), font=subtitle_font)

    # Accent chips.
    chips = ['Plan', 'Track', 'Remind']
    chip_x = 430
    chip_y = 320
    for chip in chips:
        tw = draw.textlength(chip, font=chip_font)
        pad_x = 16
        draw.rounded_rectangle((chip_x, chip_y, chip_x + tw + pad_x * 2, chip_y + 42), radius=14, fill=(255, 230, 200))
        draw.text((chip_x + pad_x, chip_y + 8), chip, fill=(180, 80, 20), font=chip_font)
        chip_x += int(tw + pad_x * 2 + 12)

    return img


def main() -> None:
    icon_master = create_icon_master(1024)
    save_icons(icon_master)

    FEATURE_DIR.mkdir(parents=True, exist_ok=True)
    feature = create_feature_graphic(Image.open(ICON_512).convert('RGBA'))
    feature.save(FEATURE_1024, 'PNG', optimize=True)

    print(f'updated {ICON_512}')
    print(f'updated {FEATURE_1024}')


if __name__ == '__main__':
    main()

