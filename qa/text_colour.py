"""The colour of a text on a screenshot: warm (green), amber or neutral (grey).

    python3 -I qa/text_colour.py <screenshot.png> <x1> <y1> <x2> <y2>

Inside the box, the most common colour is the background; the pixels far from it are the glyphs, and their average
decides. It prints "<kind> rgb(r, g, b)". Used by qa/device_scenarios.sh for the progress message (EC-UX-14: warm when
better, neutral otherwise).
"""
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from a11y_audit import read_png  # noqa: E402


def main():
    path, x1, y1, x2, y2 = Path(sys.argv[1]), *map(int, sys.argv[2:6])
    rows, bpp = read_png(path)
    pixels = [tuple(rows[y][x * bpp:x * bpp + 3]) for y in range(y1, y2) for x in range(x1, x2)]
    background = Counter(pixels).most_common(1)[0][0]
    glyphs = [p for p in pixels if sum(abs(a - b) for a, b in zip(p, background)) > 120]
    if not glyphs:
        print("none rgb(-)")
        return
    r, g, b = (sum(p[i] for p in glyphs) // len(glyphs) for i in range(3))
    if g > r + 20 and g > b:
        kind = "warm"
    elif r > b + 40 and g > b + 15:
        kind = "amber"
    elif max(r, g, b) - min(r, g, b) < 30:
        kind = "neutral"
    else:
        kind = "other"
    print(f"{kind} rgb({r}, {g}, {b})")


if __name__ == "__main__":
    main()
