"""Check a doctor report PDF by its text and its pages (D202).

    python3 -I qa/report_pdf.py <file.pdf> [--pages N] [--pages-at-most N] [--text T ...] [--regex R ...]
                                [--absent T ...] [--every-page T] [--dump]

The text comes from `pdftotext -layout`, one page per form feed, with runs of spaces and line breaks read as one space,
so a check does not depend on where a line wraps.
--text: each T is somewhere in the report; --regex: each Python regex matches somewhere; --absent: no T anywhere;
--every-page: T is on every page (the footer); --pages / --pages-at-most: the page count from `pdfinfo`.
--dump prints the normalised text of each page, to write a check against a real report.
Prints what it checked and exits 1 on the first mismatch.
"""
import argparse
import re
import subprocess
import sys


def fail(message):
    print(f"FAIL: {message}")
    sys.exit(1)


def normalise(text):
    return re.sub(r"\s+", " ", text).strip()


def page_count(path):
    info = subprocess.run(["pdfinfo", path], capture_output=True, text=True)
    if info.returncode != 0:
        fail(f"pdfinfo cannot read {path}: {info.stderr.strip()}")
    match = re.search(r"^Pages:\s+(\d+)$", info.stdout, re.M)
    if not match:
        fail("pdfinfo gives no page count")
    return int(match.group(1))


def pages_text(path):
    out = subprocess.run(["pdftotext", "-layout", "-enc", "UTF-8", path, "-"], capture_output=True, text=True)
    if out.returncode != 0:
        fail(f"pdftotext cannot read {path}: {out.stderr.strip()}")
    pages = out.stdout.split("\f")
    if pages and not pages[-1].strip():
        pages.pop()
    return [normalise(page) for page in pages]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("path")
    parser.add_argument("--pages", type=int)
    parser.add_argument("--pages-at-most", type=int)
    parser.add_argument("--text", nargs="*", default=[])
    parser.add_argument("--regex", nargs="*", default=[])
    parser.add_argument("--absent", nargs="*", default=[])
    parser.add_argument("--every-page")
    parser.add_argument("--dump", action="store_true")
    args = parser.parse_args()

    count = page_count(args.path)
    pages = pages_text(args.path)
    if len(pages) != count:
        fail(f"pdfinfo says {count} pages, pdftotext gives {len(pages)}")
    if args.dump:
        for number, page in enumerate(pages, 1):
            print(f"--- page {number}\n{page}")
    whole = " ".join(pages)
    if args.pages is not None and count != args.pages:
        fail(f"{count} pages, expected {args.pages}")
    if args.pages_at_most is not None and count > args.pages_at_most:
        fail(f"{count} pages, more than {args.pages_at_most}")
    for text in args.text:
        if normalise(text) not in whole:
            fail(f"no {text!r} in the report")
    for pattern in args.regex:
        if not re.search(pattern, whole):
            fail(f"nothing matches /{pattern}/ in the report")
    for text in args.absent:
        if normalise(text) in whole:
            fail(f"{text!r} is in the report")
    if args.every_page:
        missing = [n for n, page in enumerate(pages, 1) if normalise(args.every_page) not in page]
        if missing:
            fail(f"{args.every_page!r} is missing on page(s) {', '.join(map(str, missing))}")
    checked = [f"{count} page(s)"]
    checked += [f"{len(args.text)} text(s)"] if args.text else []
    checked += [f"{len(args.regex)} pattern(s)"] if args.regex else []
    checked += [f"{len(args.absent)} absent"] if args.absent else []
    checked += ["footer on every page"] if args.every_page else []
    print("PDF ok: " + ", ".join(checked))


if __name__ == "__main__":
    main()
