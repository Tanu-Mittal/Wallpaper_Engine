# -*- coding: utf-8 -*-

import sys
import os
import cv2
import win32gui
import win32con
import win32api
import numpy as np
import ctypes
from ctypes import wintypes
import sys
sys.stdout.reconfigure(encoding='utf-8')


print("Script started")
with open("video_pid.txt", "w") as f:
    import os
    f.write(str(os.getpid()))


class BITMAPINFOHEADER(ctypes.Structure):
    _fields_ = [
        ('biSize', wintypes.DWORD),
        ('biWidth', wintypes.LONG),
        ('biHeight', wintypes.LONG),
        ('biPlanes', wintypes.WORD),
        ('biBitCount', wintypes.WORD),
        ('biCompression', wintypes.DWORD),
        ('biSizeImage', wintypes.DWORD),
        ('biXPelsPerMeter', wintypes.LONG),
        ('biYPelsPerMeter', wintypes.LONG),
        ('biClrUsed', wintypes.DWORD),
        ('biClrImportant', wintypes.DWORD)
    ]

class BITMAPINFO(ctypes.Structure):
    _fields_ = [
        ('bmiHeader', BITMAPINFOHEADER),
        ('bmiColors', wintypes.DWORD * 3)
    ]

gdi32 = ctypes.WinDLL('gdi32')
CreateDIBitmap = gdi32.CreateDIBitmap
CreateDIBitmap.argtypes = [
    wintypes.HDC,
    ctypes.POINTER(BITMAPINFOHEADER),
    wintypes.DWORD,
    ctypes.c_void_p,
    ctypes.POINTER(BITMAPINFO),
    wintypes.UINT,
]
CreateDIBitmap.restype = wintypes.HBITMAP

def get_workerw():
    progman = win32gui.FindWindow("Progman", None)
    win32gui.SendMessageTimeout(progman, 0x052C, 0, 0, 0, 1000)
    workerw = [None]
    def enum_windows(hwnd, lParam):
        shell = win32gui.FindWindowEx(hwnd, 0, "SHELLDLL_DefView", None)
        if shell != 0:
            workerw[0] = win32gui.FindWindowEx(0, hwnd, "WorkerW", None)
        return True
    win32gui.EnumWindows(enum_windows, None)
    return workerw[0]

def draw_frame(hwnd, frame):
    h, w = frame.shape[:2]
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    hdc = win32gui.GetDC(hwnd)
    hdc_mem = win32gui.CreateCompatibleDC(hdc)
    bmi = BITMAPINFO()
    bmi.bmiHeader.biSize = ctypes.sizeof(BITMAPINFOHEADER)
    bmi.bmiHeader.biWidth = w
    bmi.bmiHeader.biHeight = -h  # top-down bitmap
    bmi.bmiHeader.biPlanes = 1
    bmi.bmiHeader.biBitCount = 24
    bmi.bmiHeader.biCompression = win32con.BI_RGB
    bmi.bmiHeader.biSizeImage = 0
    bmi.bmiHeader.biXPelsPerMeter = 0
    bmi.bmiHeader.biYPelsPerMeter = 0
    bmi.bmiHeader.biClrUsed = 0
    bmi.bmiHeader.biClrImportant = 0
    dib = CreateDIBitmap(
        hdc,
        ctypes.byref(bmi.bmiHeader),
        win32con.CBM_INIT,
        frame.ctypes.data_as(ctypes.c_void_p),
        ctypes.byref(bmi),
        win32con.DIB_RGB_COLORS,
    )
    old_bmp = win32gui.SelectObject(hdc_mem, dib)
    win32gui.BitBlt(hdc, 0, 0, w, h, hdc_mem, 0, 0, win32con.SRCCOPY)
    win32gui.SelectObject(hdc_mem, old_bmp)
    win32gui.DeleteObject(dib)
    win32gui.DeleteDC(hdc_mem)
    win32gui.ReleaseDC(hwnd, hdc)

def add_to_startup(script_path, video_path):
    startup_folder = os.path.join(os.getenv('APPDATA'), 
        'Microsoft\\Windows\\Start Menu\\Programs\\Startup')
    shortcut_path = os.path.join(startup_folder, 'VideoWallpaper.bat')
    with open(shortcut_path, 'w') as file:
        file.write(f'python "{script_path}" "{video_path}"\n')

def play_wallpaper(video_path, persist=True):
    print("Requested video:", video_path)
    cap = cv2.VideoCapture(video_path)
    print("Video opened?", cap.isOpened())
    if not cap.isOpened():
        print("Cannot open video", video_path)
        return
    workerw = get_workerw()
    print("WorkerW:", workerw)
    if workerw is None:
        print("WorkerW not found")
        return
    print("Creating window...")

    wnd_class = win32gui.WNDCLASS()
    wnd_class.hInstance = win32api.GetModuleHandle(None)
    wnd_class.lpszClassName = "VideoWallpaper"
    wnd_class.style = win32con.CS_HREDRAW | win32con.CS_VREDRAW
    wnd_class.hCursor = win32gui.LoadCursor(0, win32con.IDC_ARROW)
    wnd_class.hbrBackground = win32con.COLOR_BACKGROUND + 1
    wnd_class.lpfnWndProc = win32gui.DefWindowProc
    classAtom = win32gui.RegisterClass(wnd_class)
    hwnd = win32gui.CreateWindowEx(
        win32con.WS_EX_TOOLWINDOW | win32con.WS_EX_LAYERED | win32con.WS_EX_TRANSPARENT,
        classAtom,
        None,
        win32con.WS_POPUP | win32con.WS_VISIBLE,
        0, 0,
        win32api.GetSystemMetrics(0),
        win32api.GetSystemMetrics(1),
        workerw,
        0,
        wnd_class.hInstance,
        None
    )
    win32gui.SetLayeredWindowAttributes(hwnd, 0, 255, win32con.LWA_ALPHA)
    # Z-order fix so icons stay on top
    win32gui.SetWindowPos(hwnd, win32con.HWND_BOTTOM, 0, 0, 
        win32api.GetSystemMetrics(0), win32api.GetSystemMetrics(1), win32con.SWP_NOACTIVATE)
    print(" Video wallpaper running... CTRL+C to stop")
    if persist:
        add_to_startup(os.path.abspath(__file__), video_path)
    while True:
        ret, frame = cap.read()
        if not ret:
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)
            continue
        draw_frame(hwnd, frame)
        cv2.waitKey(30)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python video_wallpaper.py path-to-video")
    else:
        play_wallpaper(sys.argv[1], persist=True)
