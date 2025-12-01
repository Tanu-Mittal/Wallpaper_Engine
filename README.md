Wallpaper Engine (simple) — Windows Video & Animated Wallpapers
==============================================================

Overview
--------
This project provides a lightweight Windows application for applying video, GIF, and image wallpapers. It includes a simple UI, preview generation for various formats, and scripts to run and manage animated/video wallpapers.

Highlights
----------
- Apply video wallpapers on Windows
- Preview images, GIFs and videos
- Simple, cross-language components (Python and a Java UI)
- Scripts to run, stop and manage wallpaper processes

Quick Start (Windows)
---------------------
1. From File Explorer: double-click `build_scripts\run_app.bat`.
2. From PowerShell (project root):

```powershell
.\build_scripts\run_app.bat
```

Requirements
------------
- Windows (tested on Windows 10/11)
- Python 3.8+ (for the helper scripts in `python_scripts`)
- Java (if you want to build or modify the `java_app` UI)
- Optional: ffmpeg (if you plan to extend video preprocessing)

Repository Layout
-----------------
- `build_scripts/` — Contains `run_app.bat` used to launch the application.
- `java_app/` — Java UI source (contains `MainWindow.java`).
- `python_scripts/` — Python helper scripts:
	- `animated_wallpaper.py` — wallpaper animation runner
	- `video_wallpaper.py` — play video wallpaper
	- `set_static_wallpaper.py` — set a static wallpaper
	- `generate_preview.py` — create previews for a wallpaper
	- `fix_previews.py` — repair or regenerate previews
	- `stop_animation.py`, `stop_video.py` — stop running wallpaper processes
	- `video_pid.txt` — file used to store a running process id
- `assets/` — (optional) store sample wallpapers and preview assets
- `README.md` — this file
- `requirements.txt` — Python dependencies (if any)

How it works (high level)
-------------------------
The project uses small helper scripts to launch and control wallpaper playback (videos or animated GIFs). The Java UI provides a simple front end for browsing and selecting wallpapers. Scripts write/read `video_pid.txt` to coordinate running processes and allow stopping animations.

Common Tasks
------------
- Run the app: `build_scripts\run_app.bat` (as above)
- Start an animated wallpaper (example): run the relevant Python script from `python_scripts`.
- Stop a running wallpaper: run `python_scripts\stop_animation.py` or `stop_video.py`.

Python development
------------------
1. Create and activate a virtual environment (optional but recommended):

```powershell
python -m venv .venv; .\.venv\Scripts\Activate.ps1
```

2. Install dependencies:

```powershell
pip install -r requirements.txt
```

3. Run helper scripts from the project root, for example:

```powershell
python .\python_scripts\generate_preview.py --input path\to\wallpaper
```

Notes & Troubleshooting
-----------------------
- If a wallpaper doesn't start, check `video_pid.txt` for a stale PID and remove it if needed.
- If previews look wrong, run `python_scripts\fix_previews.py`.
- If the Java UI fails to run, ensure you have a compatible JDK and compile `java_app` as needed.

Suggested Next Steps
--------------------
- Add screenshots or an animated GIF to show the UI and preview features.
- Add a `LICENSE` and `CONTRIBUTING.md` if you plan to publish or accept contributions.
- Add a packaged installer or a single-step launcher for ease of use.

Contact & Credits
-----------------
This repository is a small personal utility-style project. If you want improvements or help, open an issue or contact the repo owner.

ChangeLog
---------
Version 1.0
- Initial working application to apply video wallpapers on Windows
- Previews for images, GIFs and videos
- Basic UI and browsing ability

Enjoy — and let me know if you want a nicer installer, screenshots, or a polished UI skin!