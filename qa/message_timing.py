"""How long did a message stay on screen? Read from screenshots taken back to back, about 8 a second (#56).

    python3 -I qa/message_timing.py <frames-dir> <tap-time>

<frames-dir> holds PNG screenshots named by the epoch time they were taken (`1761400000.52.png`), the first taken right
after the tap at <tap-time> and the last after the message has surely gone. Messages appear at the top (in-app-messages),
so each frame is compared with the last one, on two bands (status bar left out):
- the screen below the messages: it matches the last frame once the screen the message belongs to is up;
- the band where messages show: with that screen up, it differs from the last frame exactly while the message shows.
The message is on from the first frame where the screen is up and the band differs, to the first later frame where the
band matches. Prints "appeared A s after the tap, stayed D s (±E s)", where E is the gap between frames, or a reason
and exit 1.
"""
import subprocess
import sys
from pathlib import Path

WIDTH, HEIGHT = 108, 240
BAND = range(12, 70)
BELOW = range(100, 230)
SAME = 3.0


def grey(path):
    out = subprocess.run(["ffmpeg", "-v", "error", "-i", str(path), "-vf", f"scale={WIDTH}:{HEIGHT},format=gray",
                          "-f", "rawvideo", "-"], capture_output=True)
    if out.returncode != 0 or len(out.stdout) != WIDTH * HEIGHT:
        sys.exit(f"cannot read {path}: {out.stderr.decode().strip()}")
    return out.stdout


def difference(a, b, rows):
    total = sum(abs(a[r * WIDTH + x] - b[r * WIDTH + x]) for r in rows for x in range(WIDTH))
    return total / (len(rows) * WIDTH)


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    frames = sorted(Path(sys.argv[1]).glob("*.png"), key=lambda p: float(p.stem))
    tap = float(sys.argv[2])
    if len(frames) < 4:
        sys.exit("too few frames")
    last = grey(frames[-1])
    on = off = None
    gaps = []
    for previous, frame in zip([None] + frames, frames):
        image = grey(frame)
        screen_up = difference(image, last, BELOW) < SAME
        shown = difference(image, last, BAND) >= SAME
        if on is None and screen_up and shown:
            on = frame
            gaps.append(float(frame.stem) - float(previous.stem) if previous else 0.0)
        elif on is not None and not shown:
            off = frame
            gaps.append(float(frame.stem) - float(previous.stem))
            break
    if on is None:
        print("FAIL: no frame shows a message over the final screen")
        sys.exit(1)
    if off is None:
        print("FAIL: the message was still on in the last frame but one")
        sys.exit(1)
    start, end = float(on.stem), float(off.stem)
    print(f"appeared {start - tap:.1f} s after the tap, stayed {end - start:.1f} s (±{max(gaps):.1f} s)")


if __name__ == "__main__":
    main()
