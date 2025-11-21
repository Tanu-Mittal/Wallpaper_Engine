# stop_video.py

import os
import signal
import time
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PID_FILE = os.path.join(BASE_DIR, "video_pid.txt")

def stop_pid(pid):
    try:
        os.kill(pid, signal.SIGTERM)
        time.sleep(0.5)
        os.kill(pid, signal.SIGKILL)
        return True
    except:
        return False

if __name__ == "__main__":
    if not os.path.exists(PID_FILE):
        print("No video wallpaper running.")
        sys.exit(0)

    with open(PID_FILE, "r") as f:
        pid = int(f.read().strip())

    if stop_pid(pid):
        print("Video wallpaper stopped.")
    else:
        print("Failed to stop video.")

    os.remove(PID_FILE)
