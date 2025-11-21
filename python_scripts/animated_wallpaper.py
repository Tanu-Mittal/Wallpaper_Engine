# animated_wallpaper.py
import sys
import time
import threading
from PIL import Image, ImageTk, ImageSequence
import tkinter as tk
import win32gui, win32con, win32api

GIF = sys.argv[1]  # "C:\\Users\\...\\wall.gif"

def find_workerw():
    progman = win32gui.FindWindow("Progman", None)
    win32gui.SendMessageTimeout(progman, 0x052C, 0, 0, win32con.SMTO_NORMAL, 1000)
    workerw = None
    def enum_handler(hwnd, lParam):
        nonlocal workerw
        if win32gui.GetClassName(hwnd) == "WorkerW":
            # pick the WorkerW that has no children with SHELLDLL_DefView
            child = win32gui.FindWindowEx(hwnd, 0, "SHELLDLL_DefView", None)
            if child == 0:
                workerw = hwnd
    win32gui.EnumWindows(enum_handler, None)
    return workerw

def play_gif_on_window(gif_path):
    root = tk.Tk()
    root.overrideredirect(True)
    root.geometry("{0}x{1}+0+0".format(win32api.GetSystemMetrics(0),
                                       win32api.GetSystemMetrics(1)))
    canvas = tk.Canvas(root, highlightthickness=0)
    canvas.pack(fill="both", expand=True)

    gif = Image.open(gif_path)
    frames = [ImageTk.PhotoImage(f.copy().resize(
              (win32api.GetSystemMetrics(0), win32api.GetSystemMetrics(1)))) 
              for f in ImageSequence.Iterator(gif)]

    img_item = canvas.create_image(0,0,anchor="nw",image=frames[0])

    def animate(i=0):
        canvas.itemconfig(img_item, image=frames[i])
        root.after(gif.info.get('duration', 40), animate, (i+1)%len(frames))

    animate()

    # Re-parent window to WorkerW so it sits behind icons
    hwnd = root.winfo_id()
    workerw = find_workerw()
    if workerw:
        win32gui.SetParent(hwnd, workerw)

    root.mainloop()

if __name__ == "__main__":
    play_gif_on_window(GIF)
