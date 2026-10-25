"""Draws Migrely's icon (BRD D103, D116) and writes every file made from it.

The icon is one bold colour and one simple glyph: a white calendar page with a navy band and a navy crescent moon, on
Migrely violet. It is drawn on the 108-unit adaptive-icon grid and stays inside the 66-unit safe circle (radius 33), so
no launcher mask and no splash screen cuts it.

Run from the repo root: python3 docs/release/icon/make_icon.py
It writes the three launcher drawables into app/src/main/res/drawable, the SVG sources next to this script, and the
512×512 Play icon as a 32-bit PNG (rendered with rsvg-convert, then given an alpha channel with Pillow).
"""
import math
import subprocess
from pathlib import Path

from PIL import Image

ICON = Path(__file__).parent
RES = ICON.parents[2] / 'app' / 'src' / 'main' / 'res' / 'drawable'

VIOLET = '#5B50D6'
NAVY = '#1C1F4A'
WHITE = '#FFFFFF'


def num(value):
    return f'{value:.2f}'.rstrip('0').rstrip('.')


def crescent(cx, cy, big, dx, dy, small):
    """A disc of radius [big] at (cx, cy) with a disc of radius [small], offset by (dx, dy), bitten out of it."""
    d = math.hypot(dx, dy)
    a = (big * big - small * small + d * d) / (2 * d)
    h = math.sqrt(big * big - a * a)
    xm, ym = cx + a * dx / d, cy + a * dy / d
    p = (xm + h * dy / d, ym - h * dx / d)
    q = (xm - h * dy / d, ym + h * dx / d)
    return (f'M{num(p[0])},{num(p[1])} A{num(big)},{num(big)} 0 1,0 {num(q[0])},{num(q[1])} '
            f'A{num(small)},{num(small)} 0 0,1 {num(p[0])},{num(p[1])} Z')


def rounded_rect(x, y, w, h, r):
    return (f'M{num(x + r)},{num(y)} H{num(x + w - r)} A{num(r)},{num(r)} 0 0 1 {num(x + w)},{num(y + r)} '
            f'V{num(y + h - r)} A{num(r)},{num(r)} 0 0 1 {num(x + w - r)},{num(y + h)} H{num(x + r)} '
            f'A{num(r)},{num(r)} 0 0 1 {num(x)},{num(y + h - r)} V{num(y + r)} A{num(r)},{num(r)} 0 0 1 '
            f'{num(x + r)},{num(y)} Z')


PAGE = rounded_rect(30, 35, 48, 44, 10)
BAND = 'M30,45 A10,10 0 0 1 40,35 H68 A10,10 0 0 1 78,45 V48 H30 Z'
RINGS = [rounded_rect(39.5, 28.5, 5.5, 11, 2.75), rounded_rect(63, 28.5, 5.5, 11, 2.75)]
MOON = crescent(55, 64, 10.5, 5.6, -4.8, 8.8)

SHAPES = [(PAGE, WHITE), (BAND, NAVY)] + [(ring, WHITE) for ring in RINGS] + [(MOON, NAVY)]


def vector(comment, paths):
    body = ''.join(f'''    <path
        android:fillColor="{color}"
        android:pathData="{data}"{rule} />
''' for data, color, rule in paths)
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- {comment} Source: docs/release/icon/make_icon.py (BRD D116). -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
{body}</vector>
'''


def svg(view_box, size, background, foreground):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{view_box}" width="{size}" height="{size}">'
            f'{background}{foreground}</svg>\n')


if __name__ == '__main__':
    foreground = [(data, color, '') for data, color in SHAPES]
    (RES / 'ic_launcher_foreground.xml').write_text(
        vector("Migrely's icon: a white calendar page with a navy band and crescent moon.", foreground))
    (RES / 'ic_launcher_background.xml').write_text(
        vector('Migrely violet, one flat colour.', [('M0,0h108v108h-108z', VIOLET, '')]))
    # Themed icons tint one shape: the page with the moon cut out of it, and its two rings.
    mono = ([(f'{PAGE} {MOON}', '#FFFFFFFF', '\n        android:fillType="evenOdd"')] +
            [(ring, '#FFFFFFFF', '') for ring in RINGS])
    (RES / 'ic_launcher_monochrome.xml').write_text(
        vector('The icon in one colour, for themed icons: the page with the moon cut out, and its rings.', mono))

    glyph = ''.join(f'<path d="{data}" fill="{color}"/>' for data, color in SHAPES)
    flat = f'<rect width="108" height="108" fill="{VIOLET}"/>'
    (ICON / 'migrely-icon.svg').write_text(svg('0 0 108 108', 108, flat, glyph))
    (ICON / 'migrely-play-icon.svg').write_text(svg('18 18 72 72', 512, flat, glyph))
    play = ICON / 'migrely-play-icon-512.png'
    subprocess.run(['rsvg-convert', '-w', '512', '-h', '512', str(ICON / 'migrely-play-icon.svg'), '-o', str(play)],
                   check=True)
    Image.open(play).convert('RGBA').save(play)
    print('written')
