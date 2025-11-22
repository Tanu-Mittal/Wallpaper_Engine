import sys
from PIL import Image, ImageTk, ImageSequence
import tkinter as tk

def play_gif(gif_path):
    root = tk.Tk()
    root.title("GIF Wallpaper Overlay")
    # Only fullscreen (not topmost)
    root.attributes("-fullscreen", True)
    canvas = tk.Canvas(root, width=root.winfo_screenwidth(),
                       height=root.winfo_screenheight(), highlightthickness=0)
    canvas.pack(fill="both", expand=True)
    
    # Exit overlay when ESC pressed
    root.bind("<Escape>", lambda e: root.destroy())

    try:
        gif = Image.open(gif_path)
        frames = [ImageTk.PhotoImage(f.copy().resize((
            root.winfo_screenwidth(), root.winfo_screenheight())))
            for f in ImageSequence.Iterator(gif)]
        img_item = canvas.create_image(0, 0, anchor='nw', image=frames[0])
        def animate(i=0):
            canvas.itemconfig(img_item, image=frames[i % len(frames)])
            root.after(gif.info.get('duration', 40), animate, i+1)
        animate()
        print(f"GIF wallpaper applied successfully: {gif_path}")
        root.mainloop()
    except Exception as e:
        print(f"ERROR: {str(e)}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python animated_wallpaper.py path-to-gif.gif")
    else:
        play_gif(sys.argv[1])
