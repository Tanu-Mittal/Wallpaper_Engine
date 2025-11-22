import psutil
import win32gui
import win32con
import time

# Find and close overlay window (update title if needed)
def close_window_by_title(window_title):
    hwnd = win32gui.FindWindow(None, window_title)
    if hwnd:
        win32gui.PostMessage(hwnd, win32con.WM_CLOSE, 0, 0)
        time.sleep(0.5)
        print(f"Closed window: {window_title}")
    else:
        print(f"No window found with title: {window_title}")

# Kill all video_wallpaper.py processes
def kill_vidwallpaper():
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            if 'video_wallpaper.py' in ' '.join(proc.info.get('cmdline', [])):
                proc.kill()
                print(f"Killed process: {proc.info['pid']}")
        except Exception:
            pass

if __name__ == "__main__":
    # Try both methods
    close_window_by_title("Video Wallpaper Overlay")  # Update to your actual window title!
    kill_vidwallpaper()
