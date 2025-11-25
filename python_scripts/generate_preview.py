import sys
import os
from moviepy.editor import VideoFileClip

def create_gif(video_path):
    try:
        if not os.path.exists(video_path):
            print("Error: Video file not found.")
            return

        # Define output GIF path (same name as video, but .gif)
        base_name = os.path.splitext(video_path)[0]
        gif_path = base_name + ".gif"

        print(f"Generating preview for: {video_path}...")

        # Load video, cut first 2 seconds, resize to width 300 (for speed), save as GIF
        clip = VideoFileClip(video_path).subclip(0, 2).resize(width=300)
        clip.write_gif(gif_path, fps=10, program='ffmpeg')

        print("Success")
        
    except Exception as e:
        print(f"Error: {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        create_gif(sys.argv[1])
    else:
        print("Please provide a video file path.")
