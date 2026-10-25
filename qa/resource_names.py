"""Which string resource names are defined in more than one module?

Android merges every module's resources into one app, so two modules that both define <string name="picker_title">
end up with one of them, and a screen of the other module shows the wrong text. That is how Log medication's date and
time dialogs once read "Reminder time" (a duplicate in :features:reminders:ui, fixed in e74055e).

The check fails (exit 1) when two modules define the same <string>, <plurals> or <string-array> name in the same
resource folder (values, values-de, ...). With different text, one module already shows the other's text; with the
same text, the first edit or translation of one copy breaks the other. Each module prefixes its names (#41, PR #44).
ALLOWED lets a name repeat, with a reason.

    python3 -I qa/resource_names.py
"""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
KINDS = ("string", "plurals", "string-array")
# name -> why it may repeat (for example, the same text on purpose in two modules).
ALLOWED = {}


def module_of(path):
    # ".../features/medication/ui/src/main/res/values/strings.xml" -> "features/medication/ui"
    return path.relative_to(ROOT).as_posix().split("/src/")[0]


def read_names():
    # name -> resource folder (values, values-de, ...) -> module -> text
    names = {}
    for path in sorted(ROOT.glob("**/src/*/res/values*/*.xml")):
        if "/build/" in path.as_posix():
            continue
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError as error:
            sys.exit(f"Cannot read {path.relative_to(ROOT)}: {error}")
        for node in root:
            if node.tag in KINDS and node.get("name"):
                text = re.sub(r"\s+", " ", "".join(node.itertext())).strip()
                folder = names.setdefault(node.get("name"), {}).setdefault(path.parent.name, {})
                folder[module_of(path)] = text
    return names


def main():
    names = read_names()
    different, same = [], []
    for name, folders in sorted(names.items()):
        if name in ALLOWED:
            continue
        for folder, modules in sorted(folders.items()):
            if len(modules) < 2:
                continue
            row = f"{name} ({folder}) in {', '.join(sorted(modules))}"
            if len(set(modules.values())) > 1:
                different.append(row + ": " + " | ".join(f"{m}={t!r}" for m, t in sorted(modules.items())))
            else:
                same.append(row)
    print(f"{len(names)} string resource names")
    for row in different:
        print(f"FAIL: {row}")
    for row in same:
        print(f"FAIL: same text, {row}")
    if different or same:
        print(f"{len(different) + len(same)} name(s) defined in more than one module; give each a module prefix.")
        return 1
    print("Resource name check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
