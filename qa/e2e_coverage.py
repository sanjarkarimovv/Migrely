"""Which OpenSpec scenarios have an end-to-end Maestro flow?

Reads every scenario from openspec/specs/*/spec.md and every "# Covers: <capability> › <requirement> › <scenario>"
line from .maestro/**/*.yaml, then prints the coverage per capability. It fails (exit 1) on a "Covers" line that names a
scenario that no longer exists (renamed or removed) and on a "Covers" line that is not in that three-part form.

    python3 qa/e2e_coverage.py            # summary
    python3 qa/e2e_coverage.py --missing  # also list the scenarios without a flow
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COVERS = re.compile(r"^#\s*Covers:\s*(.+?)\s*›\s*(.+?)(?:\s*›\s*(.+?))?\s*$")


def norm(text):
    # "Describe a long attack (opening part)" covers "Describe a long attack".
    return re.sub(r"\s*\(.*?\)\s*$", "", text).strip().lower()


def read_specs():
    scenarios = {}
    for spec in sorted((ROOT / "openspec" / "specs").glob("*/spec.md")):
        capability, requirement = spec.parent.name, None
        for line in spec.read_text(encoding="utf-8").splitlines():
            if line.startswith("### Requirement:"):
                requirement = line.split(":", 1)[1].strip()
            elif line.startswith("#### Scenario:"):
                scenario = line.split(":", 1)[1].strip()
                scenarios[(capability, norm(requirement), norm(scenario))] = (requirement, scenario)
    return scenarios


def read_flows():
    covered, malformed = {}, []
    for flow in sorted((ROOT / ".maestro").rglob("*.yaml")):
        for line in flow.read_text(encoding="utf-8").splitlines():
            if not line.startswith("# Covers:"):
                continue
            match = COVERS.match(line)
            if not (match and match.group(3)):
                malformed.append(f"{flow.relative_to(ROOT).as_posix()}: {line}")
                continue
            key = (match.group(1).strip(), norm(match.group(2)), norm(match.group(3)))
            covered.setdefault(key, []).append(flow.relative_to(ROOT).as_posix())
    return covered, malformed


def main():
    scenarios = read_specs()
    covered, malformed = read_flows()
    capabilities = sorted({key[0] for key in scenarios})
    total_hit = 0
    print(f"{'capability':<18} {'covered':>8} {'total':>6}")
    for capability in capabilities:
        keys = [key for key in scenarios if key[0] == capability]
        hit = sum(1 for key in keys if key in covered)
        total_hit += hit
        print(f"{capability:<18} {hit:>8} {len(keys):>6}")
    print(f"{'all':<18} {total_hit:>8} {len(scenarios):>6}  ({100 * total_hit / len(scenarios):.0f}%)")

    stale = {key: flows for key, flows in covered.items() if key not in scenarios}
    if stale:
        print("\nCovers lines that match no scenario (renamed or removed?):")
        for key, flows in stale.items():
            print(f"  {' › '.join(key)}  <- {', '.join(flows)}")
    if malformed:
        print("\nCovers lines without capability › requirement › scenario:")
        for line in malformed:
            print(f"  {line}")
    if "--missing" in sys.argv:
        print("\nScenarios without an end-to-end flow:")
        for key, (requirement, scenario) in scenarios.items():
            if key not in covered:
                print(f"  {key[0]} › {requirement} › {scenario}")
    return 1 if stale or malformed else 0


if __name__ == "__main__":
    sys.exit(main())
