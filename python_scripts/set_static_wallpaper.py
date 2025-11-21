# set_static_wallpaper.py

import ctypes
import sys
import os

def set_wallpaper(path):
    if not os.path.exists(path):
        print("File not found:", path)
        return False

    SPI_SETDESKWALLPAPER = 20
    return ctypes.windll.user32.SystemParametersInfoW(
        SPI_SETDESKWALLPAPER, 0, path, 3
    )

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python set_static_wallpaper.py <image-path>")
        sys.exit(1)

    ok = set_wallpaper(sys.argv[1])
    print("OK" if ok else "Failed")
