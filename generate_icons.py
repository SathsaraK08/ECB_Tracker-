import os
from PIL import Image, ImageDraw

def create_icon(size, filename, is_round=False):
    # Create cyan background
    img = Image.new('RGBA', (size, size), (0, 212, 255, 0)) # transparent base
    draw = ImageDraw.Draw(img)
    
    # Draw background shape
    if is_round:
        draw.ellipse([0, 0, size, size], fill=(0, 212, 255, 255))
    else:
        draw.rectangle([0, 0, size, size], fill=(0, 212, 255, 255))
        
    # Draw a simple white bolt in the center
    bolt_w, bolt_h = size * 0.4, size * 0.6
    cx, cy = size / 2, size / 2
    
    # Simple polygon for a bolt
    points = [
        (cx + bolt_w*0.2, cy - bolt_h/2),
        (cx - bolt_w*0.4, cy + bolt_h*0.1),
        (cx, cy + bolt_h*0.1),
        (cx - bolt_w*0.2, cy + bolt_h/2),
        (cx + bolt_w*0.4, cy - bolt_h*0.1),
        (cx, cy - bolt_h*0.1),
    ]
    draw.polygon(points, fill=(255, 255, 255, 255))
    
    img.save(filename)

sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

base_dir = r"c:\Users\Nethm\Music\ECB_Tracker_App\app\src\main\res"

for density, size in sizes.items():
    folder = os.path.join(base_dir, f"mipmap-{density}")
    os.makedirs(folder, exist_ok=True)
    
    create_icon(size, os.path.join(folder, "ic_launcher.png"), is_round=False)
    create_icon(size, os.path.join(folder, "ic_launcher_round.png"), is_round=True)

print("Icons generated.")
