"""Which string resource names are defined in more than one module?

Android merges every module's resources into one app, so two modules that both define <string name="picker_title">
end up with one of them, and a screen of the other module shows the wrong text. That is how Log medication's date and
time dialogs once read "Reminder time" (a duplicate in :features:reminders:ui, fixed in e74055e).

The check fails (exit 1) when two modules define the same <string>, <plurals> or <string-array> name with different
text in the same resource folder (values, values-de, ...), because one module then shows the other's text. The same
name with the same text is a warning: nothing is wrong yet, but the first edit or translation of one copy breaks the
other. ALLOWED silences a name, with a reason.

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
        print(f"warning: same text, {row}")
    if different:
        print(f"{len(different)} name(s) with different text in different modules: one module shows the other's text.")
        return 1
    print(f"Resource name check passed ({len(same)} warning(s): rename with a module prefix or share one resource)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
