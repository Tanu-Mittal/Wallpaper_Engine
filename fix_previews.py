import os
import sys

# Try importing for both old (v1) and new (v2) versions of MoviePy
try:
    # New version (v2.0+)
    from moviepy import VideoFileClip
except ImportError:
    try:
        # Old version (v1.x)
        from moviepy.editor import VideoFileClip
    except ImportError:
        print("Error: MoviePy library not found. Please run: pip install moviepy")
        sys.exit(1)

# POINT THIS TO YOUR ASSETS FOLDER
# Using raw string (r"...") to handle backslashes correctly
ASSETS_DIR = r"C:\Users\tanum\OneDrive\Desktop\DynamicWallpaperEngine\assets"

def generate_previews():
    if not os.path.exists(ASSETS_DIR):
        print(f"Error: Folder not found: {ASSETS_DIR}")
        return

    print(f"Scanning folder: {ASSETS_DIR}")

    files = os.listdir(ASSETS_DIR)
    # Find all mp4/avi files
    videos = [f for f in files if f.lower().endswith(('.mp4', '.avi', '.mov', '.mkv'))]

    print(f"Found {len(videos)} video files.")
    print("-" * 30)

    for filename in videos:
        video_path = os.path.join(ASSETS_DIR, filename)
        
        # Create the GIF filename (e.g., video.mp4 -> video.gif)
        gif_path = os.path.splitext(video_path)[0] + ".gif"
        
        # Only generate if it doesn't exist yet
        if not os.path.exists(gif_path):
            print(f"Generating GIF for: {filename} ...")
            try:
                # Load video, take first 2 seconds, resize width to 300px
                # We use a context manager (with) to ensure file is closed properly
                with VideoFileClip(video_path) as clip:
                    subclip = clip.subclipped(0, 2).resized(width=300)
                    subclip.write_gif(gif_path, fps=10, logger=None) # logger=None hides the progress bar spam
                print(" -> Done!")
            except Exception as e:
                print(f" -> FAILED: {e}")
                # Try fallback for older moviepy method names if above fails
                try:
                    clip = VideoFileClip(video_path).subclip(0, 2).resize(width=300)
                    clip.write_gif(gif_path, fps=10)
                    print(" -> Done (Fallback method)")
                except:
                    pass
        else:
            print(f" -> Skipped (GIF exists): {filename}")

    print("-" * 30)
    print("All Finished. You can restart your Java app now.")

if __name__ == "__main__":
    generate_previews()
