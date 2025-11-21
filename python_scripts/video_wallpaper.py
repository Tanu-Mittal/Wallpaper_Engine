# video_wallpaper.py
# Usage: python video_wallpaper.py "C:\path\to\video.mp4"

import sys
import os
import vlc
import win32gui
import win32con
import win32api
import time

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PID_FILE = os.path.join(BASE_DIR, "video_pid.txt")

def find_workerw():
    progman = win32gui.FindWindow("Progman", None)
    win32gui.SendMessageTimeout(progman, 0x052C, 0, 0,
                                win32con.SMTO_NORMAL, 1000)

    workerw = None

    def enum_handler(hwnd, _):
        nonlocal workerw
        if win32gui.GetClassName(hwnd) == "WorkerW":
            child = win32gui.FindWindowEx(hwnd, 0, "SHELLDLL_DefView", None)
            if child == 0:
                workerw = hwnd

    win32gui.EnumWindows(enum_handler, None)
    return workerw

def play_video(video_path):
    # Store PID for stopping later
    with open(PID_FILE, "w") as f:
        f.write(str(os.getpid()))

    # Screen size
    width = win32api.GetSystemMetrics(0)
    height = win32api.GetSystemMetrics(1)

    # Create a borderless fullscreen window
    import tkinter as tk
    root = tk.Tk()
    root.overrideredirect(True)
    root.geometry(f"{width}x{height}+0+0")

    # Embed frame inside Tk window
    frame = tk.Frame(root, width=width, height=height)
    frame.pack()
    root.update()

    hwnd = frame.winfo_id()

    # Attach to WorkerW to go behind desktop icons
    workerw = find_workerw()
    if workerw:
        win32gui.SetParent(hwnd, workerw)

    # VLC Player
        # VLC Player (stable config)
    instance = vlc.Instance([
        "--no-video-title-show",
        "--no-osd",
        "--quiet",
        "--no-sub-autodetect-file",
        "--loop",
        "--no-xlib",
        "--vout=win32"      # 100% stable for wallpapers
    ])



    player = instance.media_player_new()

    media = instance.media_new(video_path)
    player.set_media(media)

    # Assign video output to Tk window
    player.set_hwnd(hwnd)
    time.sleep(0.5)   # allow VLC to attach to window


    # Start playing
    player.play()
    # Force resize/stretch to fill screen
    player.video_set_scale(0)
    player.video_set_aspect_ratio(f"{width}:{height}")


    # Loop forever
    while True:
        time.sleep(0.1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python video_wallpaper.py <video-file>")
        sys.exit(1)

    video = sys.argv[1]

    if not os.path.exists(video):
        print("Video not found!")
        sys.exit(1)

    play_video(video)
