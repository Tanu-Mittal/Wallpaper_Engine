import os
import sys

# Try importing MoviePy (Handles v1 and v2)
try:
    from moviepy import VideoFileClip
except ImportError:
    try:
        from moviepy.editor import VideoFileClip
    except ImportError:
        print("Error: MoviePy library not found. Run: pip install moviepy")
        sys.exit(1)

def generate_previews(target_dir):
    if not os.path.exists(target_dir):
        print(f"Error: Folder not found: {target_dir}")
        return

    print(f"Scanning folder: {target_dir}")
    files = os.listdir(target_dir)
    videos = [f for f in files if f.lower().endswith(('.mp4', '.avi', '.mov', '.mkv'))]

    if not videos:
        print("No videos found.")
        return

    print(f"Found {len(videos)} videos.")

    for filename in videos:
        video_path = os.path.join(target_dir, filename)
        gif_path = os.path.splitext(video_path)[0] + ".gif"
        
        if not os.path.exists(gif_path):
            print(f"Generating GIF for: {filename} ...")
            try:
                # Generate 2-second preview
                with VideoFileClip(video_path) as clip:
                    subclip = clip.subclipped(0, 2).resized(width=300)
                    subclip.write_gif(gif_path, fps=10, logger=None)
                print(" -> Done!")
            except Exception as e:
                print(f" -> FAILED: {e}")
                # Fallback for older moviepy versions
                try:
                    clip = VideoFileClip(video_path).subclip(0, 2).resize(width=300)
                    clip.write_gif(gif_path, fps=10)
                except:
                    pass
        else:
            print(f" -> Skipped (Exists): {filename}")

if __name__ == "__main__":
    # Use command line argument if provided, otherwise default to 'assets'
    folder = sys.argv[1] if len(sys.argv) > 1 else "assets"
    generate_previews(folder)
