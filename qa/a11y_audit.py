"""Accessibility audit of captured screens: real pixels from the device, not a simulation.

Each state is a pair <name>.xml (uiautomator dump) and <name>.png (screencap) in one folder. Only nodes of the app's
package are checked:

- contrast: WCAG 2.1 AA, 4.5:1 for normal text and 3:1 for large text. The text colour is the glyph core (the pixel
  with the highest contrast inside the node), the background is the most common colour. Text counts as large when its
  tallest line of glyphs, measured on the pixels, is at least 22 dp (about 24 sp, WCAG's 18 pt). Bold 14 pt is not
  recognised, which only makes the check stricter. Disabled controls are exempt (WCAG 1.4.3).
- touch target: every clickable node is at least 48 × 48 dp (Android and Play pre-launch report guidance).
- label: every clickable node has a text or a spoken label on itself or a descendant.
- keyboard: every clickable node is focusable, so Tab and switch access can reach it (WCAG 2.1.1).

Not judged, and listed as such:
- nodes cut off at the top or bottom edge of their scrolling container, because uiautomator reports only their
  visible part. They are checked on a capture where they are scrolled fully into view;
- text drawn inside merged nodes that expose only a spoken label (cards, calendar days, list rows). The tree has no
  text node for it to measure. Their touch target, label and keyboard reach are still checked.

    python3 qa/a11y_audit.py <folder> [--density 420]
Exit code 1 when any failure is found.
"""
import struct
import sys
import xml.etree.ElementTree as ET
import zlib
from collections import Counter
from pathlib import Path

APP_PACKAGE = "com.migrely.app"
MIN_TARGET_DP = 48
LARGE_TEXT_GLYPH_DP = 22


def read_png(path):
    data = path.read_bytes()
    pos, idat = 8, b""
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        chunk = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            width, height, depth, colour = struct.unpack(">IIBB", chunk[:10])
            if depth != 8 or colour not in (2, 6):
                raise ValueError(f"{path}: only 8-bit RGB or RGBA PNGs are supported")
            bpp = 4 if colour == 6 else 3
        elif kind == b"IDAT":
            idat += chunk
        pos += 12 + length
    raw, stride, rows, prev = zlib.decompress(idat), width * bpp, [], bytearray(width * bpp)
    for y in range(height):
        start = y * (stride + 1)
        kind, line = raw[start], bytearray(raw[start + 1:start + 1 + stride])
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if kind == 1:
                line[i] = (line[i] + a) & 255
            elif kind == 2:
                line[i] = (line[i] + b) & 255
            elif kind == 3:
                line[i] = (line[i] + (a + b) // 2) & 255
            elif kind == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(line)
        prev = line
    return rows, bpp


def luminance(rgb):
    def channel(v):
        v /= 255
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    r, g, b = rgb
    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)


def contrast(a, b):
    high, low = sorted((luminance(a), luminance(b)), reverse=True)
    return (high + 0.05) / (low + 0.05)


def bounds(node):
    x1, y1, x2, y2 = map(int, node.get("bounds").replace("][", ",").strip("[]").split(","))
    return x1, y1, x2, y2


def clipped(node, parents):
    """True when the node touches the top or bottom edge of its nearest scrolling ancestor and may be cut off there.

    Only the vertical axis counts: every scrolling container in the app scrolls vertically, and a full-width row that
    touches the container's sides is not cut off.
    """
    _, y1, _, y2 = bounds(node)
    parent = parents.get(node)
    while parent is not None and parent.tag == "node":
        if parent.get("scrollable") == "true":
            _, cy1, _, cy2 = bounds(parent)
            return y1 <= cy1 + 1 or y2 >= cy2 - 1
        parent = parents.get(parent)
    return False


def tallest_glyph_line_px(rows, bpp, box, background):
    """Height in px of the tallest run of pixel rows that hold text-coloured pixels (gaps of up to 2 rows bridged)."""
    x1, y1, x2, y2 = box
    inked = [any(contrast(tuple(rows[y][x * bpp:x * bpp + 3]), background) >= 2.0 for x in range(x1, x2))
             for y in range(y1, y2)]
    best = run = gap = 0
    for row in inked:
        if row:
            run += gap + 1
            gap = 0
        elif run:
            gap += 1
            if gap > 2:
                run = gap = 0
        best = max(best, run)
    return best


def has_label(node):
    return any((n.get("text") or n.get("content-desc")) for n in node.iter("node"))


def label_of(node):
    for n in node.iter("node"):
        if n.get("text") or n.get("content-desc"):
            return (n.get("text") or n.get("content-desc"))[:60]
    return "(no label)"


def audit_state(xml_path, png_path, density):
    rows, bpp = read_png(png_path)
    height, width = len(rows), len(rows[0]) // bpp
    scale = density / 160
    failures, warnings, clipped_labels, merged = [], [], [], 0
    root = ET.parse(xml_path).getroot()
    parents = {child: parent for parent in root.iter() for child in parent}
    for node in root.iter("node"):
        if node.get("package") != APP_PACKAGE:
            continue
        clickable = node.get("clickable") == "true"
        text = node.get("text", "")
        if not clickable and not text:
            if node.get("content-desc"):
                merged += 1
            continue
        if clipped(node, parents):
            clipped_labels.append(label_of(node))
            continue
        x1, y1, x2, y2 = bounds(node)
        x1, y1, x2, y2 = max(x1, 0), max(y1, 0), min(x2, width), min(y2, height)
        if x2 <= x1 or y2 <= y1:
            continue
        w_dp, h_dp = (x2 - x1) / scale, (y2 - y1) / scale

        if clickable and node.get("enabled") == "true":
            name = label_of(node)
            if w_dp < MIN_TARGET_DP - 0.5 or h_dp < MIN_TARGET_DP - 0.5:
                failures.append(f"touch target {w_dp:.0f}x{h_dp:.0f} dp < 48 dp: {name!r}")
            if not has_label(node):
                failures.append(f"clickable without a label at {node.get('bounds')}")
            if node.get("focusable") != "true":
                failures.append(f"clickable but not keyboard-focusable: {name!r}")
            if not text and node.get("content-desc") and not any(c.get("text") for c in node.iter("node")):
                merged += 1

        if not text:
            continue
        parent = parents.get(node)
        disabled = node.get("enabled") == "false" or (parent is not None and parent.get("enabled") == "false")
        if disabled:
            continue
        pixels = [tuple(rows[y][x * bpp:x * bpp + 3]) for y in range(y1, y2) for x in range(x1, x2)]
        background = Counter(pixels).most_common(1)[0][0]
        foreground = max(pixels, key=lambda p: contrast(p, background))
        ratio = contrast(foreground, background)
        if ratio >= 4.5:
            continue
        glyph_dp = tallest_glyph_line_px(rows, bpp, (x1, y1, x2, y2), background) / scale
        large = glyph_dp >= LARGE_TEXT_GLYPH_DP
        detail = (f"contrast {ratio:.2f}:1 (#{'%02X%02X%02X' % foreground} on #{'%02X%02X%02X' % background}, "
                  f"glyphs {glyph_dp:.0f} dp): {text[:60]!r}")
        if ratio < 3.0 or not large:
            failures.append(detail)
        else:
            warnings.append(detail + " [large text]")
    return failures, warnings, clipped_labels, merged


def main():
    folder = Path(sys.argv[1])
    density = int(sys.argv[sys.argv.index("--density") + 1]) if "--density" in sys.argv else 420
    total_failures = 0
    for xml_path in sorted(folder.glob("*.xml")):
        png_path = xml_path.with_suffix(".png")
        if not png_path.exists():
            continue
        failures, warnings, clipped_labels, merged = audit_state(xml_path, png_path, density)
        total_failures += len(failures)
        status = "FAIL" if failures else ("WARN" if warnings else "ok")
        print(f"[{status}] {xml_path.stem}")
        for line in failures:
            print(f"    fail: {line}")
        for line in warnings:
            print(f"    warn: {line}")
        if clipped_labels:
            print(f"    not judged, cut off by scrolling: {', '.join(repr(l[:30]) for l in clipped_labels)}")
        if merged:
            print(f"    text contrast not measurable in {merged} merged node(s) with only a spoken label")
    print(f"\n{total_failures} failure(s)")
    return 1 if total_failures else 0


if __name__ == "__main__":
    sys.exit(main())
