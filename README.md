# Wallpaper Engine (Simple) — Windows Video & Animated Wallpapers

A lightweight Windows application for applying **video, GIF, and image wallpapers** with a simple interface, preview generation support, and helper scripts for managing animated wallpapers.

This project combines **Python automation scripts** with a **Java-based UI**, making it easy to browse, preview, and apply dynamic wallpapers on Windows.

---

## ✨ Features

- 🎥 Apply video wallpapers on Windows
- 🖼️ Support for images, GIFs, and video backgrounds
- 🔍 Generate previews for wallpapers
- ⚡ Lightweight and easy to run
- 🧩 Java UI with Python-powered helper scripts
- 🛠️ Start, stop, and manage wallpaper processes
- 📁 Simple project structure for modifications and contributions

---

## 📷 Preview

Add screenshots or demo GIFs here.

Example:

```md
![App Preview](assets/preview.png)
```

or

```md
![Demo](assets/demo.gif)
```

---

# 🚀 Quick Start (Windows)

### Option 1: File Explorer

Navigate to:

```text
build_scripts\run_app.bat
```

Double-click to launch.

### Option 2: PowerShell

Open PowerShell in the project root:

```powershell
.\build_scripts\run_app.bat
```

---

# 📋 Requirements

Required:

- Windows 10 / Windows 11
- Python 3.8+
- Java (for UI modifications or rebuilding)

Optional:

- FFmpeg (for future video preprocessing support)

---

# 📁 Repository Structure

```text
Wallpaper-Engine-Simple/
│
├── build_scripts/
│   └── run_app.bat
│
├── java_app/
│   └── MainWindow.java
│
├── python_scripts/
│   ├── animated_wallpaper.py
│   ├── video_wallpaper.py
│   ├── set_static_wallpaper.py
│   ├── generate_preview.py
│   ├── fix_previews.py
│   ├── stop_animation.py
│   ├── stop_video.py
│   └── video_pid.txt
│
├── assets/
│
├── requirements.txt
│
└── README.md
```

---

# ⚙️ How It Works

The project uses lightweight helper scripts to launch and control wallpaper playback.

### Workflow:

1. User selects wallpaper in Java UI
2. UI calls Python scripts
3. Wallpaper starts as a background process
4. Running process ID gets stored in:

```text
video_pid.txt
```

5. Stop scripts can terminate active wallpaper sessions

This enables process management without requiring a heavy background service.

---

# 🛠 Common Tasks

### Run Application

```powershell
.\build_scripts\run_app.bat
```

---

### Generate Wallpaper Preview

```powershell
python .\python_scripts\generate_preview.py --input path\to\wallpaper
```

---

### Stop Running Wallpaper

Animated wallpaper:

```powershell
python .\python_scripts\stop_animation.py
```

Video wallpaper:

```powershell
python .\python_scripts\stop_video.py
```

---

# 🧪 Python Development Setup

### Create virtual environment

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
```

### Install dependencies

```powershell
pip install -r requirements.txt
```

---

# 🔧 Troubleshooting

### Wallpaper does not start

Check:

```text
video_pid.txt
```

Remove stale process IDs if necessary.

---

### Previews are broken

Run:

```powershell
python .\python_scripts\fix_previews.py
```

---

### Java UI does not open

Make sure:

- Java JDK is installed
- JAVA_HOME is configured
- Java version is compatible

---

# 📌 Future Improvements

Planned enhancements:

- Better UI design
- Packaged installer
- Multi-monitor support
- Custom wallpaper playlists
- Better preview caching
- Wallpaper categories
- Startup integration

---

# 👥 Contributors

## Project Owner

### Shashank Mandoli

GitHub Username:

```text
shashankmandoli
```

GitHub Profile:

https://github.com/shashankmandoli

Role:

- Project architecture
- Development
- Wallpaper functionality
- Application implementation

---

## Contributor

### Tanu Mittal

GitHub Username:

```text
Tanu-Mittal
```

GitHub Profile:

https://github.com/Tanu-Mittal

Role:

- Project contribution
- Development support
- Collaboration

---

# 🤝 Contributing

Contributions, suggestions, and improvements are welcome.

To contribute:

1. Fork repository
2. Create a feature branch

```bash
git checkout -b feature-name
```

3. Commit changes

```bash
git commit -m "Added feature"
```

4. Push changes

```bash
git push origin feature-name
```

5. Open a Pull Request

---

# 📄 License

Add a LICENSE file if you intend to publish or distribute the project.

Example:

MIT License

---

# 📜 Changelog

## Version 1.0

Initial release:

- Video wallpaper support
- GIF support
- Image previews
- Wallpaper process management
- Basic Java UI

---

# ⭐ Support

If you find this project useful:

- Star the repository
- Share feedback
- Report issues
- Suggest improvements
