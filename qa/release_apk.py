"""Is a release APK free of the debug build's simulated store? (D113: release builds always use Google Play)

    python3 -I qa/release_apk.py <app-release.apk> [<app-debug.apk>]

It looks for the simulated store in the APK's code (every classes*.dex), resources and manifest: its broadcast action,
its preferences file, its class names and its sheet's title. These are fixed strings, so R8's renaming does not hide
them. The release APK must hold none and must not be debuggable. When a debug APK is given, it must hold each kind, as a
control that the search still finds what it looks for.
Prints the counts and exits 1 on a mismatch.
"""
import re
import subprocess
import sys
import zipfile
from pathlib import Path

ANDROID_HOME = Path.home() / "Library/Android/sdk"
CODE = re.compile(rb"SIMULATED_STORE|migrely_simulated_store|SimulatedBilling")
SHEET = b"Simulated Google Play"


def aapt2():
    tools = sorted((ANDROID_HOME / "build-tools").glob("*/aapt2"),
                   key=lambda p: [int(n) for n in re.findall(r"\d+", p.parent.name)])
    if not tools:
        sys.exit(f"no aapt2 under {ANDROID_HOME / 'build-tools'}")
    return str(tools[-1])


def counts(apk):
    with zipfile.ZipFile(apk) as archive:
        dex = b"".join(archive.read(n) for n in archive.namelist() if re.fullmatch(r"classes\d*\.dex", n))
        resources = archive.read("resources.arsc")
    manifest = subprocess.run([aapt2(), "dump", "xmltree", "--file", "AndroidManifest.xml", apk],
                              capture_output=True, text=True, check=True).stdout
    return {
        "code": len(CODE.findall(dex)),
        "resources": resources.count(SHEET),
        "manifest": len(re.findall(r"SimulatedBillingReceiver|SIMULATED_STORE", manifest)),
        "debuggable": bool(re.search(r":debuggable\(0x[0-9a-f]+\)=(true|\(type 0x12\)0xffffffff)", manifest)),
    }


def main():
    if len(sys.argv) not in (2, 3):
        sys.exit(__doc__)
    release = counts(sys.argv[1])
    print(f"release: code {release['code']}, resources {release['resources']}, manifest {release['manifest']}, "
          f"debuggable {release['debuggable']}")
    failed = release["debuggable"] or any(release[k] for k in ("code", "resources", "manifest"))
    if len(sys.argv) == 3:
        control = counts(sys.argv[2])
        print(f"debug control: code {control['code']}, resources {control['resources']}, manifest {control['manifest']}")
        if not all(control[k] for k in ("code", "resources", "manifest")):
            print("FAIL: the debug control lacks the simulated store, so the search no longer finds it")
            sys.exit(1)
    if failed:
        print("FAIL: the release APK holds the simulated store or is debuggable")
        sys.exit(1)
    print("Release APK ok: no simulated store, not debuggable")


if __name__ == "__main__":
    main()
