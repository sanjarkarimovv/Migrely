#!/usr/bin/env python3
"""Check History's "Last 30 days" section on screen against the pairing that passed the medical check (BRD D151).

    adb exec-out cat dump.xml | python3 -I qa/overuse_rows.py
    python3 -I qa/overuse_rows.py --self-test

Reads a uiautomator dump on stdin and exits 1 when any of these does not hold:
- every row is exactly "<category>: N day(s)", or that with ". Doctors watch for L or more days a month.";
- a row names its number exactly when it has reached it (N >= L, with 15 for simple painkillers and 10 for the rest);
- a reached row always comes with the line, once, below the rows; with no reached row the line is not there;
- no row uses a word that reads as a safe amount, and neither the old rows nor the old line are left.

Only what is on screen counts, so the caller must have the section in view: a dump with no row at all fails.
--self-test feeds it the right states and every wrong one it must catch.
"""
import re
import sys
import xml.etree.ElementTree as ET

LIMITS = {"Simple painkillers": 15, "Triptans": 10, "Combination or other": 10, "All attack medication": 10}
ROW = re.compile(r"(?P<name>" + "|".join(LIMITS) + r"): (?P<days>\d+) (?P<unit>days?)"
                 r"(?:\. Doctors watch for (?P<number>\d+) or more days a month\.)?")
LINE = "Taking medication this often, month after month, can make headaches more frequent. " \
       "It's worth talking to your doctor."
OLD_LINE = "At this many days"
FORBIDDEN = re.compile(r"\b(limit|safe|allowed|under|within|maximum)\b", re.I)


def problems(texts):
    """texts: the on-screen texts, top to bottom."""
    found, rows, lines = [], [], []
    for index, text in enumerate(texts):
        if text == LINE:
            lines.append(index)
        elif OLD_LINE in text:
            found.append(f"the old line is on screen: {text!r}")
        elif any(text.startswith(name + ":") for name in LIMITS):
            rows.append(index)
            match = ROW.fullmatch(text)
            if FORBIDDEN.search(text):
                found.append(f"a forbidden word in the row: {text!r}")
            if not match:
                found.append(f"not a row of the final text: {text!r}")
                continue
            days, limit = int(match["days"]), LIMITS[match["name"]]
            if match["unit"] != ("day" if days == 1 else "days"):
                found.append(f"wrong plural: {text!r}")
            if days >= limit and match["number"] is None:
                found.append(f"reached ({days} of {limit}), but the row does not say so: {text!r}")
            if days < limit and match["number"] is not None:
                found.append(f"not reached ({days} of {limit}), but the row shows a number: {text!r}")
            if match["number"] is not None and int(match["number"]) != limit:
                found.append(f"the row names {match['number']}, the number is {limit}: {text!r}")
    if not rows:
        found.append("no row of the section is on screen")
    reached = any((m := ROW.fullmatch(texts[i])) and m["number"] is not None for i in rows)
    if reached and len(lines) != 1:
        found.append(f"a reached row needs the line once, it shows {len(lines)} times")
    if not reached and lines:
        found.append("the line shows with no reached row")
    if lines and rows and lines[0] < rows[-1]:
        found.append("the line is above a row")
    return found


def on_screen(xml):
    nodes = ET.fromstring(xml).iter("node")
    return [text for node in nodes if (text := node.get("text") or node.get("content-desc"))]


def self_test():
    reached = "Triptans: 10 days. Doctors watch for 10 or more days a month."
    right = [
        ["Simple painkillers: 6 days"],
        ["Triptans: 1 day"],
        ["Simple painkillers: 14 days"],
        [reached, LINE],
        ["Simple painkillers: 16 days. Doctors watch for 15 or more days a month.", LINE],
        ["Simple painkillers: 7 days", "Triptans: 5 days",
         "All attack medication: 12 days. Doctors watch for 10 or more days a month.", LINE],
    ]
    wrong = {
        "the old row": ["Simple painkillers: 6 days (limit 15)"],
        "the old reached row": ["Simple painkillers: 15 days (limit 15), at the limit",
                                "At this many days, medication can make headaches more frequent."],
        "the old line alone": ["Triptans: 3 days", "At this many days, medication can make headaches more frequent."],
        "a reached row without the line": [reached],
        "a reached row with the line twice": [reached, LINE, LINE],
        "the line with no reached row": ["Triptans: 9 days", LINE],
        "a number before it is reached": ["Triptans: 9 days. Doctors watch for 10 or more days a month.", LINE],
        "a reached row not saying so": ["Triptans: 10 days", LINE],
        "the wrong number": ["Simple painkillers: 15 days. Doctors watch for 10 or more days a month.", LINE],
        "the wrong plural": ["Triptans: 1 days"],
        "the line above a row": [reached, LINE, "Simple painkillers: 3 days"],
        "no row": ["Last 30 days"],
        "a forbidden word": ["Triptans: 3 days, within reason"],
    }
    failed = 0
    for texts in right:
        if problems(["Last 30 days", *texts]):
            print(f"FAILED: a right state is refused: {texts}: {problems(['Last 30 days', *texts])}")
            failed += 1
    for name, texts in wrong.items():
        if not problems(["Last 30 days", *texts]):
            print(f"FAILED: not caught: {name}")
            failed += 1
    print(f"overuse_rows self-test: {len(right)} right states, {len(wrong)} wrong states, {failed} failed")
    return 1 if failed else 0


def main():
    if sys.argv[1:] == ["--self-test"]:
        return self_test()
    found = problems(on_screen(sys.stdin.buffer.read()))
    for problem in found:
        print(f"  FAILED: {problem}")
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main())
