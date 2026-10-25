"""Check Migrely's exported files and write files to import (D200).

    python3 -I qa/backup_files.py json <file.json> [attacks medications day_marks]
    python3 -I qa/backup_files.py csv <file.csv> <separator , or ;> [note that must start with ']
    python3 -I qa/backup_files.py fixtures <real-export.json> <out-dir>

json: the file is a Migrely backup (format "migrely-backup", schemaVersion 6, epoch-millis times, an id on every entry),
and, when given, holds exactly that many attacks, medications and day marks (spec data-backup, BAK-F1, D71).
csv: UTF-8 with a byte-order mark, CRLF line ends, the expected header and separator, one row per attack, and a note
that starts with = + - @ written with a ' in front, so a spreadsheet shows it as text (BAK-F2, D72).
fixtures: from a real export, writes broken.json (not a backup), newer.json (schemaVersion 99) and older.json
(schemaVersion 5) for EC-BAK-01…03.
Prints what it checked and exits 1 on the first mismatch.
"""
import json
import sys
from pathlib import Path

FORMAT = "migrely-backup"
SCHEMA_VERSION = 6
HEADER = ["Start", "End", "Duration (minutes)", "Time zone", "Peak pain", "Pain level", "Where it hurts", "Symptoms",
          "Triggers", "Medications", "Impact", "Note"]


def fail(message):
    print(f"FAIL: {message}")
    sys.exit(1)


def check_json(path, counts):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    if data.get("format") != FORMAT:
        fail(f"format is {data.get('format')!r}, not {FORMAT!r}")
    if data.get("schemaVersion") != SCHEMA_VERSION:
        fail(f"schemaVersion is {data.get('schemaVersion')!r}, not {SCHEMA_VERSION}")
    for attack in data.get("attacks", []):
        if not attack.get("id") or not isinstance(attack.get("startEpochMillis"), int):
            fail(f"an attack without an id or epoch-millis start: {attack}")
        if attack["startEpochMillis"] < 10 ** 12:
            fail(f"a start that is not in milliseconds: {attack['startEpochMillis']}")
    found = (len(data.get("attacks", [])), len(data.get("medications", [])), len(data.get("dayMarks", [])))
    if counts and list(found) != [int(n) for n in counts]:
        fail(f"holds {found[0]} attacks, {found[1]} medications, {found[2]} day marks; expected {' '.join(counts)}")
    print(f"JSON ok: {FORMAT} v{SCHEMA_VERSION}, {found[0]} attacks, {found[1]} medications, {found[2]} day marks")


def check_csv(path, separator, formula_note):
    raw = Path(path).read_bytes()
    if not raw.startswith(b"\xef\xbb\xbf"):
        fail("no UTF-8 byte-order mark")
    text = raw[3:].decode("utf-8")
    if "\n" in text.replace("\r\n", ""):
        fail("a line ends with LF only, not CRLF")
    lines = [line for line in text.split("\r\n") if line]
    header = lines[0].split(separator)
    if header != HEADER:
        fail(f"header {header} with separator {separator!r}; expected {HEADER}")
    if formula_note:
        cells = [cell.strip('"') for line in lines[1:] for cell in line.split(separator)]
        if ("'" + formula_note) not in cells:
            fail(f"no cell reads '{formula_note} (the note that looks like a formula)")
        if formula_note in cells:
            fail(f"the note {formula_note} is written as is, so a spreadsheet would run it")
    print(f"CSV ok: BOM, CRLF, header with {separator!r}, {len(lines) - 1} row(s)"
          + (f", the note written as '{formula_note}" if formula_note else ""))


def write_fixtures(real, out):
    out = Path(out)
    out.mkdir(parents=True, exist_ok=True)
    data = json.loads(Path(real).read_text(encoding="utf-8"))
    (out / "broken.json").write_text("This is a shopping list, not a backup.\n", encoding="utf-8")
    newer = dict(data, schemaVersion=99)
    (out / "newer.json").write_text(json.dumps(newer), encoding="utf-8")
    older = dict(data, schemaVersion=5)
    (out / "older.json").write_text(json.dumps(older), encoding="utf-8")
    print(f"fixtures in {out}: broken.json, newer.json (v99), older.json (v5)")


def main():
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    mode = sys.argv[1]
    if mode == "json":
        check_json(sys.argv[2], sys.argv[3:6])
    elif mode == "csv":
        check_csv(sys.argv[2], sys.argv[3], sys.argv[4] if len(sys.argv) > 4 else "")
    elif mode == "fixtures":
        write_fixtures(sys.argv[2], sys.argv[3])
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main()
