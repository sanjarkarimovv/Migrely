"""Check Migrely's exported files and write files to import (D200).

    python3 -I qa/backup_files.py json <file.json> [attacks medications day_marks]
    python3 -I qa/backup_files.py csv <file.csv> <separator , or ;> [--rows N] [--formula NOTE ...] [--text TEXT]
    python3 -I qa/backup_files.py fixtures <real-export.json> <out-dir>

json: the file is a Migrely backup (format "migrely-backup", schemaVersion 6, an id on every entry, times in epoch
milliseconds) and, when given, holds exactly that many attacks, medications and day marks (spec data-backup, D71).
csv: UTF-8 with a byte-order mark, CRLF line ends outside quoted cells, the expected header and separator, N rows
(one per attack), each --formula note written with a ' in front (so a spreadsheet shows it as text), and --text
present as written, for example non-Latin letters (D72).
fixtures: from a real export, writes
  broken.json  not a backup (EC-BAK-01);
  newer.json   schemaVersion 99 (EC-BAK-02);
  older.json   schemaVersion 5, with the optional attack fields left out, which must take their defaults (EC-BAK-03);
  add.json     the export's first attack again under another ID (it overlaps, so Add skips it), a new attack three
               days earlier, and "ibuprofen" under another ID with an intake on that new day (Add maps it to the
               phone's Ibuprofen).
Prints what it checked and exits 1 on the first mismatch.
"""
import argparse
import copy
import csv
import io
import json
import sys
import uuid
from pathlib import Path

FORMAT = "migrely-backup"
SCHEMA_VERSION = 6
HEADER = ["Start", "End", "Duration (minutes)", "Time zone", "Peak pain", "Pain level", "Where it hurts", "Symptoms",
          "Triggers", "Medications", "Impact", "Note"]
EPOCH_MILLIS = range(10 ** 12, 10 ** 14)
DAY = 86_400_000


def fail(message):
    print(f"FAIL: {message}")
    sys.exit(1)


def check_json(path, counts):
    data = json.loads(Path(path).read_text(encoding="utf-8"))
    if data.get("format") != FORMAT:
        fail(f"format is {data.get('format')!r}, not {FORMAT!r}")
    if data.get("schemaVersion") != SCHEMA_VERSION:
        fail(f"schemaVersion is {data.get('schemaVersion')!r}, not {SCHEMA_VERSION}")
    for kind in ("attacks", "customTags", "medications", "intakes"):
        for entry in data.get(kind, []):
            if not entry.get("id"):
                fail(f"an entry of {kind} without an id: {entry}")
            for key, value in entry.items():
                if key.endswith("EpochMillis") and value is not None and value not in EPOCH_MILLIS:
                    fail(f"{kind} {key}={value} is not epoch milliseconds")
    found = (len(data.get("attacks", [])), len(data.get("medications", [])), len(data.get("dayMarks", [])))
    if counts and list(found) != [int(n) for n in counts]:
        fail(f"holds {found[0]} attacks, {found[1]} medications, {found[2]} day marks; expected {' '.join(counts)}")
    print(f"JSON ok: {FORMAT} v{SCHEMA_VERSION}, {found[0]} attacks, {found[1]} medications, {found[2]} day marks")


def bare_line_feed(text):
    # A line feed outside a quoted cell must follow a carriage return; inside quotes it is part of a note.
    quoted, previous = False, ""
    for char in text:
        if char == '"':
            quoted = not quoted
        elif char == "\n" and not quoted and previous != "\r":
            return True
        previous = char
    return False


def check_csv(path, separator, rows, formulas, text):
    raw = Path(path).read_bytes()
    if not raw.startswith(b"\xef\xbb\xbf"):
        fail("no UTF-8 byte-order mark")
    body = raw[3:].decode("utf-8")
    if bare_line_feed(body):
        fail("a line ends with LF only, not CRLF")
    table = list(csv.reader(io.StringIO(body, newline=""), delimiter=separator))
    if table and table[-1] == []:
        table.pop()
    if table[0] != HEADER:
        fail(f"header {table[0]} with separator {separator!r}; expected {HEADER}")
    if rows is not None and len(table) - 1 != rows:
        fail(f"{len(table) - 1} row(s), expected {rows} (one per attack)")
    cells = [cell for row in table[1:] for cell in row]
    for formula in formulas:
        if ("'" + formula) not in cells:
            fail(f"no cell reads '{formula} (a note that looks like a formula)")
        if formula in cells:
            fail(f"the note {formula} is written as is, so a spreadsheet would run it")
    if text and not any(text in cell for cell in cells):
        fail(f"no cell holds {text!r} as written")
    print(f"CSV ok: BOM, CRLF, header with {separator!r}, {len(table) - 1} row(s)"
          + "".join(f", '{f}" for f in formulas) + (f", {text!r} kept" if text else ""))


def write_fixtures(real, out):
    out = Path(out)
    out.mkdir(parents=True, exist_ok=True)
    data = json.loads(Path(real).read_text(encoding="utf-8"))
    (out / "broken.json").write_text("This is a shopping list, not a backup.\n", encoding="utf-8")
    (out / "newer.json").write_text(json.dumps(dict(data, schemaVersion=99)), encoding="utf-8")
    older = copy.deepcopy(data)
    older["schemaVersion"] = 5
    for attack in older.get("attacks", []):
        for optional in ("longCheckDisabled", "functionalImpact", "tags", "intensityPoints"):
            attack.pop(optional, None)
    (out / "older.json").write_text(json.dumps(older), encoding="utf-8")
    add = copy.deepcopy(data)
    first = data["attacks"][0]
    overlap = dict(copy.deepcopy(first), id=str(uuid.uuid4()))
    new = dict(copy.deepcopy(first), id=str(uuid.uuid4()), note=None)
    for key in ("startEpochMillis", "endEpochMillis", "createdEpochMillis", "updatedEpochMillis"):
        if new.get(key) is not None:
            new[key] -= 3 * DAY
    new["intensityPoints"] = [dict(p, atEpochMillis=p["atEpochMillis"] - 3 * DAY) for p in new.get("intensityPoints", [])]
    medication = dict(copy.deepcopy(data["medications"][0]), id=str(uuid.uuid4()), name="ibuprofen")
    intake = {"id": str(uuid.uuid4()), "medicationId": medication["id"], "attackId": new["id"],
              "takenEpochMillis": new["startEpochMillis"] + 600_000, "takenZoneId": new["startZoneId"],
              "createdEpochMillis": new["createdEpochMillis"]}
    add["attacks"] = [overlap, new]
    add["medications"] = [medication]
    add["intakes"] = [intake]
    add["dayMarks"] = []
    (out / "add.json").write_text(json.dumps(add), encoding="utf-8")
    print(f"fixtures in {out}: broken.json, newer.json (v99), older.json (v5, optional fields left out), add.json")


def main():
    if len(sys.argv) < 3:
        sys.exit(__doc__)
    mode = sys.argv[1]
    if mode == "json":
        check_json(sys.argv[2], sys.argv[3:6])
    elif mode == "csv":
        parser = argparse.ArgumentParser()
        parser.add_argument("path")
        parser.add_argument("separator")
        parser.add_argument("--rows", type=int)
        parser.add_argument("--formula", nargs="*", default=[])
        parser.add_argument("--text", default="")
        args = parser.parse_args(sys.argv[2:])
        check_csv(args.path, args.separator, args.rows, args.formula, args.text)
    elif mode == "fixtures":
        write_fixtures(sys.argv[2], sys.argv[3])
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main()
