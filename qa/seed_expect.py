"""The texts a seed's import must make the app show, read from what qa/seed_backup.py printed (D202).

    python3 -I qa/seed_expect.py pdf <seed.expected>        one text per line, to find in the report's PDF
    python3 -I qa/seed_expect.py insights <seed.expected>   one text per line, to find on the Insights screen

pdf: the period line of page 1 ("27 Jul 2026 to 26 Oct 2026 (92 days)"), each summary row as "Label Value", each
medications row with its cells joined by spaces, and the start of each migraines row. The start cell wraps in the PDF
and its time is written as an en-US phone writes it (12-hour), so a start is a Python regex, marked "re:".
insights: the "N more migraines" card, each trigger share, the busiest day and time of day, each medication's "What
helped" line, and the 12 months as TalkBack reads them.
"""
import re
import sys
from datetime import datetime
from pathlib import Path


def sections(lines):
    # The indented blocks under each "Doctor report, ...:" or "Insights, ...:" heading, by kind.
    current, found = None, {}
    for line in lines:
        if re.match(r"^\s{2}Doctor report, .*page 1: ", line):
            current = "pdf"
        elif re.match(r"^\s{2}Insights, ", line):
            current = "insights"
        elif re.match(r"^\s{2}\S", line):
            current = current if line.lstrip().startswith(("Migraines table", "Medications table")) else None
        if current:
            found.setdefault(current, []).append(line)
    return found


def start_pattern(cell):
    # "17 Oct 2026, 00:30 (Asia/Tokyo)" -> the date, other cells of the row maybe, "12:30 AM", the zone broken anywhere.
    match = re.match(r"^(\d{1,2} \w{3} \d{4}), (\d{2}:\d{2})(?: \((.+?)\))?", cell)
    date, time, zone = match.groups()
    twelve = datetime.strptime(time, "%H:%M").strftime("%I:%M %p").lstrip("0")
    pattern = re.escape(date) + r",.{0,120}?" + re.escape(twelve)
    if zone:
        pattern += r"\s*\(" + r"\s?".join(re.escape(c) for c in zone) + r"\)"
    return pattern


def pdf_texts(lines):
    out, table = [], None
    for line in lines:
        period = re.match(r"^\s{2}Doctor report, .*page 1: (.+?)\. Made with Migrely", line)
        if period:
            out.append(period.group(1))
            table = "summary"
            continue
        stripped = line.strip()
        if stripped.startswith("Migraines table"):
            table = "migraines"
            continue
        if stripped.startswith("Medications table"):
            table = "medications"
            continue
        if " | " not in stripped:
            continue
        cells = [c.strip() for c in stripped.split(" | ")]
        if table == "summary":
            out.append(f"{cells[0]} {cells[1]}")
        elif table == "medications":
            out.append(" ".join(c for c in cells if c))
        elif table == "migraines":
            out.append("re:" + start_pattern(cells[0]))
    return out


def insights_texts(lines):
    out = []
    for line in lines:
        stripped = line.strip()
        card = re.match(r"^The card reads: (.+)$", stripped)
        if card:
            out.append(card.group(1))
        elif re.match(r"^.+ — \d+ of \d+ migraines? \(.+\)$", stripped):
            out.append(stripped)
        elif re.match(r"^(Most often .+: \d+ of \d+|No single (day|time of day) stands out)$", stripped):
            out.append(stripped)
        elif re.match(r"^[A-Z][^:]*: (Helped a lot or completely: .+|Needs \d+ answers? \(has \d+\))$", stripped):
            name, text = stripped.split(": ", 1)
            out.extend([name, text])
        elif re.match(r"^[A-Z][a-z]+ \d{4}: \d+ migraine days?, ", stripped):
            out.append(stripped)
    return out


def main():
    if len(sys.argv) != 3 or sys.argv[1] not in ("pdf", "insights"):
        sys.exit(__doc__)
    found = sections(Path(sys.argv[2]).read_text(encoding="utf-8").splitlines())
    texts = (pdf_texts if sys.argv[1] == "pdf" else insights_texts)(found.get(sys.argv[1], []))
    if not texts:
        sys.exit(f"no {sys.argv[1]} texts in {sys.argv[2]}")
    print("\n".join(dict.fromkeys(texts)))


if __name__ == "__main__":
    main()
