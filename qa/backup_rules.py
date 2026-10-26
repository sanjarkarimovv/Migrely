"""Does every backup rule in an APK leave out a file? (D115: the entitlement cache is never backed up)

    python3 -I qa/backup_rules.py <app.apk> <shared-preferences file name>

The rules are read from the APK itself, so the check covers the build under test, release builds with shortened
resource names included: the manifest's fullBackupContent and dataExtractionRules point at the files.
A section that takes shared preferences must exclude the file. A section takes them when it includes the
"sharedpref" or the "root" domain, or when it includes nothing at all (Android then backs up everything not excluded):
- in the data extraction rules (Android 12 and later), cloud-backup and device-transfer each, and both must be there
  (a missing section moves everything);
- in the full-backup rules (Android 11 and older), once, since an exclude there applies to every include.
Prints what it checked and exits 1 on the first rule that would back the file up.
"""
import re
import subprocess
import sys
from pathlib import Path

ANDROID_HOME = Path.home() / "Library/Android/sdk"
RULES = {"fullBackupContent": "full-backup", "dataExtractionRules": "data-extraction"}


def fail(message):
    print(f"FAIL: {message}")
    sys.exit(1)


def aapt2():
    tools = sorted((ANDROID_HOME / "build-tools").glob("*/aapt2"),
                   key=lambda p: [int(n) for n in re.findall(r"\d+", p.parent.name)])
    if not tools:
        fail(f"no aapt2 under {ANDROID_HOME / 'build-tools'}")
    return str(tools[-1])


def run(*args):
    out = subprocess.run([aapt2(), *args], capture_output=True, text=True)
    if out.returncode != 0:
        fail(f"aapt2 {' '.join(args[:2])}: {out.stderr.strip()}")
    return out.stdout


def rule_files(apk):
    # Manifest attribute -> resource id -> file in the APK.
    manifest = run("dump", "xmltree", "--file", "AndroidManifest.xml", apk)
    resources = run("dump", "resources", apk)
    files = {}
    for attribute in RULES:
        match = re.search(rf":{attribute}\(0x[0-9a-f]+\)=@(0x[0-9a-f]+)", manifest)
        if not match:
            fail(f"the manifest has no {attribute}")
        entry = re.search(rf"resource {match.group(1)} \S+\n\s+\(\) \(file\) (\S+)", resources)
        if not entry:
            fail(f"no file for {attribute} ({match.group(1)})")
        files[RULES[attribute]] = entry.group(1)
    return files


def sections(apk, path):
    # [(section tag, [(element, domain, path)])] from aapt2's indented tree.
    found, current = [], None
    element = None
    for line in run("dump", "xmltree", "--file", path, apk).splitlines():
        tag = re.match(r"\s*E: (\S+)", line)
        if tag:
            name = tag.group(1)
            if name in ("cloud-backup", "device-transfer", "full-backup-content"):
                current = (name, [])
                found.append(current)
            elif name in ("include", "exclude") and current:
                element = [name, None, None]
                current[1].append(element)
            continue
        attribute = re.match(r'\s*A: (domain|path)="([^"]*)"', line)
        if attribute and element is not None:
            element[1 if attribute.group(1) == "domain" else 2] = attribute.group(2)
    return found


def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    apk, name = sys.argv[1], sys.argv[2]
    checked = []
    for kind, path in rule_files(apk).items():
        for section, rules in sections(apk, path):
            includes = [r for r in rules if r[0] == "include"]
            takes = not includes or any(r[1] in ("sharedpref", "root") for r in includes)
            excluded = any(r[0] == "exclude" and r[1] == "sharedpref" and r[2] == name for r in rules)
            if takes and not excluded:
                fail(f"{section} in {path} takes shared preferences and does not exclude {name}")
            checked.append(f"{section} ({'excluded' if takes else 'no shared preferences'})")
    for section in ("cloud-backup", "device-transfer"):
        if not any(c.startswith(section) for c in checked):
            fail(f"the data extraction rules have no {section} section, so it would take everything")
    print(f"Backup rules ok: {name} left out of " + ", ".join(checked))


if __name__ == "__main__":
    main()
