import win32gui
import win32con
import time
import psutil

def close_gif_overlay():
    window_title = "GIF Wallpaper Overlay"
    hwnd = win32gui.FindWindow(None, window_title)
    if hwnd:
        win32gui.PostMessage(hwnd, win32con.WM_CLOSE, 0, 0)
        time.sleep(0.5)
        print(f"Closed overlay: {window_title}")
    else:
        print("Overlay not found. It may already be closed.")

def kill_gif_script():
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            if 'animated_wallpaper.py' in ' '.join(proc.info.get('cmdline', [])):
                proc.kill()
                print(f"Killed process: {proc.info['pid']}")
        except Exception:
            pass

if __name__ == "__main__":
    close_gif_overlay()
    kill_gif_script()
