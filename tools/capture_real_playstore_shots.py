#!/usr/bin/env python3
import subprocess
import time
from pathlib import Path

ADB_PREFIX = ["adb", "-s", "emulator-5554"]
ROOT = Path("/Users/faheemabaas/AndroidStudioProjects/MyApplication/play-store-assets")

OUT = {
    "phone": ROOT / "screenshots-phone",
    "tablet7": ROOT / "screenshots-tablet-7inch",
    "tablet10": ROOT / "screenshots-tablet-10inch",
    "chromebook": ROOT / "screenshots-chromebook",
}

for d in OUT.values():
    d.mkdir(parents=True, exist_ok=True)


def adb(*args: str, check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run(ADB_PREFIX + list(args), check=check, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


def set_profile(size: str, density: int, rotation: int) -> None:
    adb("shell", "wm", "size", size)
    adb("shell", "wm", "density", str(density))
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", str(rotation))
    time.sleep(1.5)


def launch_app() -> None:
    adb("shell", "am", "start", "-n", "com.faheemlabs.pocketapp/.MainActivity")
    time.sleep(4.5)


def tap(x: int, y: int) -> None:
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(1.0)


def screencap(file_path: Path) -> None:
    p = subprocess.Popen(ADB_PREFIX + ["exec-out", "screencap", "-p"], stdout=subprocess.PIPE)
    data, _ = p.communicate(timeout=15)
    file_path.write_bytes(data)


def capture_tabs(width: int, height: int, out_dir: Path, suffix: str) -> None:
    y = int(height * 0.965)
    # First 4 tabs: Tasks, Expenses, Events, Payments
    xs = [int(width * 0.10), int(width * 0.30), int(width * 0.50), int(width * 0.70)]
    for idx, x in enumerate(xs, start=1):
        tap(x, y)
        time.sleep(0.5)
        screencap(out_dir / f"screenshot_{idx}_{suffix}.png")


def main() -> None:
    adb("start-server")
    adb("shell", "input", "keyevent", "KEYCODE_WAKEUP", check=False)
    adb("shell", "input", "keyevent", "82", check=False)

    # Real captures per required listing class
    set_profile("1080x1920", 420, 0)
    launch_app()
    capture_tabs(1080, 1920, OUT["phone"], "phone")

    set_profile("1200x2133", 280, 0)
    launch_app()
    capture_tabs(1200, 2133, OUT["tablet7"], "7inch")

    set_profile("1440x2560", 280, 0)
    launch_app()
    capture_tabs(1440, 2560, OUT["tablet10"], "10inch")

    set_profile("1920x1080", 200, 1)
    launch_app()
    capture_tabs(1920, 1080, OUT["chromebook"], "chromebook")

    # Restore emulator display defaults
    adb("shell", "wm", "size", "reset")
    adb("shell", "wm", "density", "reset")
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "1")


if __name__ == "__main__":
    main()

