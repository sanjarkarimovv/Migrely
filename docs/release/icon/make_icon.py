"""Draws Migrely's icon (BRD D103) and writes every file made from it.

Run from the repo root: python3 docs/release/icon/make_icon.py
It writes the three launcher drawables into app/src/main/res/drawable, the SVG sources next to this script, and the
512×512 Play icon as a 32-bit PNG (rendered with rsvg-convert, then given an alpha channel with Pillow).
"""
import subprocess
from pathlib import Path
from PIL import Image

ICON = Path(__file__).parent
RES = ICON.parents[2] / 'app' / 'src' / 'main' / 'res' / 'drawable'
import math
# 108-unit adaptive-icon grid; safe zone is a circle of radius 33 around (54, 54).
C1 = (50.0, 57.0); R = 19.0          # the moon's disc
C2 = (59.5, 48.5); r = 16.0          # the circle that bites it
def intersections(c1, r1, c2, r2):
    (x1, y1), (x2, y2) = c1, c2
    d = math.hypot(x2 - x1, y2 - y1)
    a = (r1**2 - r2**2 + d**2) / (2 * d)
    h = math.sqrt(r1**2 - a**2)
    xm, ym = x1 + a * (x2 - x1) / d, y1 + a * (y2 - y1) / d
    return ((xm + h * (y2 - y1) / d, ym - h * (x2 - x1) / d),
            (xm - h * (y2 - y1) / d, ym + h * (x2 - x1) / d))
p, q = intersections(C1, R, C2, r)
f = lambda v: f"{v:.2f}".rstrip('0').rstrip('.')
# Outer arc: the big circle's long way round from p to q; inner arc: back along the biting circle.
moon = (f"M{f(p[0])},{f(p[1])} A{f(R)},{f(R)} 0 1,0 {f(q[0])},{f(q[1])} "
        f"A{f(r)},{f(r)} 0 0,1 {f(p[0])},{f(p[1])} Z")
def sparkle(cx, cy, a):
    return (f"M{f(cx)},{f(cy - a)} Q{f(cx)},{f(cy)} {f(cx + a)},{f(cy)} Q{f(cx)},{f(cy)} {f(cx)},{f(cy + a)} "
            f"Q{f(cx)},{f(cy)} {f(cx - a)},{f(cy)} Q{f(cx)},{f(cy)} {f(cx)},{f(cy - a)} Z")
stars = [sparkle(66.5, 44.5, 4.6), sparkle(74.0, 59.5, 2.6)]
MOON = "#C8CCD2"; STAR = "#C8CCD2"; TOP = "#121A2E"; BOTTOM = "#26305A"
# Check that every point of the art stays inside the safe zone (radius 33), with margin.
pts = [(C1[0] + R * math.cos(t), C1[1] + R * math.sin(t)) for t in [i * math.pi / 90 for i in range(180)]]
pts += [(66.5, 44.5 - 4.6), (66.5 + 4.6, 44.5), (74 + 2.6, 59.5), (74, 59.5 + 2.6)]
print("max radius from centre:", f(max(math.hypot(x - 54, y - 54) for x, y in pts)), "(safe zone 33)")
svg_full = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">
  <defs><linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="{TOP}"/><stop offset="1" stop-color="{BOTTOM}"/></linearGradient></defs>
  <rect width="108" height="108" fill="url(#sky)"/>
  <path d="{moon}" fill="{MOON}"/>
  {''.join(f'<path d="{s}" fill="{STAR}"/>' for s in stars)}
</svg>
'''
open(ICON / 'migrely-icon.svg', 'w').write(svg_full)
# Play icon: the same art scaled so the moon fills more of the square (Play masks it to a rounded square itself).
svg_play = svg_full.replace('viewBox="0 0 108 108"', 'viewBox="18 18 72 72"').replace('<rect width="108" height="108"', '<rect x="18" y="18" width="72" height="72"')
open(ICON / 'migrely-play-icon.svg', 'w').write(svg_play)
fg = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Migrely's icon: a crescent moon and two stars on the home screen's night sky (BRD D103). Source:
     docs/release/icon/migrely-icon.svg. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="{MOON}"
        android:pathData="{moon}" />
''' + ''.join(f'''    <path
        android:fillColor="{STAR}"
        android:pathData="{s}" />
''' for s in stars) + '</vector>\n'
open(RES / 'ic_launcher_foreground.xml', 'w').write(fg)
mono = fg.replace(MOON, '#FFFFFFFF').replace("Migrely's icon: a crescent moon and two stars on the home screen's night sky", "The icon's shapes in one colour, for themed icons")
open(RES / 'ic_launcher_monochrome.xml', 'w').write(mono)
bg = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- The home screen's calm night sky, darker at the top (BRD D103). -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:endX="54"
                android:endY="108"
                android:startX="54"
                android:startY="0"
                android:type="linear">
                <item
                    android:color="{TOP}"
                    android:offset="0" />
                <item
                    android:color="{BOTTOM}"
                    android:offset="1" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
'''
open(RES / 'ic_launcher_background.xml', 'w').write(bg)
play = ICON / 'migrely-play-icon-512.png'
subprocess.run(['rsvg-convert', '-w', '512', '-h', '512', str(ICON / 'migrely-play-icon.svg'), '-o', str(play)], check=True)
Image.open(play).convert('RGBA').save(play)
