"""Which string resource names are defined in more than one module?

Android merges every module's resources into one app, so two modules that both define <string name="picker_title">
end up with one of them, and a screen of the other module shows the wrong text. That is how Log medication's date and
time dialogs once read "Reminder time" (a duplicate in :features:reminders:ui, fixed in e74055e).

The check fails (exit 1) when two modules define a resource of the same type (string, plurals or array) and name, in
any resource folder: one module's values-de copy overrides the other's values copy in German, too. With different
text, one module already shows the other's text; with the same text, the first edit or translation of one copy breaks
the other. Each module prefixes its names (#41, PR #44). ALLOWED lets a name repeat, with a reason.

    python3 -I qa/resource_names.py
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
# name -> why it may repeat (for example, the same text on purpose in two modules).
ALLOWED = {}


def module_of(path):
    # ".../features/medication/ui/src/main/res/values/strings.xml" -> "features/medication/ui"
    return path.relative_to(ROOT).as_posix().split("/src/")[0]


TYPES = {"string": "string", "plurals": "plurals", "string-array": "array"}


def read_names():
    # (type, name) -> module -> folder -> text
    names = {}
    for path in sorted(ROOT.glob("**/src/*/res/values*/*.xml")):
        posix = path.relative_to(ROOT).as_posix()
        if "/build/" in posix or posix.startswith(".") or "/." in posix:
            continue
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError as error:
            sys.exit(f"Cannot read {posix}: {error}")
        for node in root:
            if node.tag in TYPES and node.get("name"):
                text = re.sub(r"\s+", " ", "".join(node.itertext())).strip()
                modules = names.setdefault((TYPES[node.tag], node.get("name")), {})
                modules.setdefault(module_of(path), {})[path.parent.name] = text
    return names


def main():
    names = read_names()
    duplicates = []
    for (kind, name), modules in sorted(names.items()):
        if name in ALLOWED or len(modules) < 2:
            continue
        texts = {text for folders in modules.values() for text in folders.values()}
        where = "; ".join(f"{m} ({', '.join(sorted(f))})" for m, f in sorted(modules.items()))
        duplicates.append(f"{kind} {name} in {where}" + ("" if len(texts) == 1 else ", with different text"))
    print(f"{len(names)} string resource names")
    for row in duplicates:
        print(f"FAIL: {row}")
    if duplicates:
        print(f"{len(duplicates)} name(s) defined in more than one module; give each a module prefix.")
        return 1
    print("Resource name check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
