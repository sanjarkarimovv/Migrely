"""Which OpenSpec scenarios have an end-to-end Maestro flow?

Reads every scenario from openspec/specs/*/spec.md and from the delta specs of open changes, and every
"# Covers: <capability> › <requirement> › <scenario>" line from .maestro/**/*.yaml, from qa/device_scenarios.sh and
from qa/split_apks.sh (for checks the scripts make themselves, such as key events). Then it prints the coverage per
capability. It fails (exit 1) on a "Covers" line that names a scenario that no longer exists (renamed or removed) and
on a "Covers" line that is not in that three-part form.

Limits while a change is open: the old name of a RENAMED requirement still counts, and a scenario that a MODIFIED
delta drops stays in the main spec until the change is archived, so a Covers line on it is not reported as stale yet.

    python3 qa/e2e_coverage.py            # summary
    python3 qa/e2e_coverage.py --missing  # also list the scenarios without a flow
    python3 qa/e2e_coverage.py --below 50 # also list the capabilities with under 50% of their scenarios covered, for
                                          # the residual risks of a Go/No-Go record (D204)
    python3 qa/e2e_coverage.py --register docs/qa/test-strategy.md
                                          # also fail when the test strategy is out of date (D204): a capability
                                          # without a row in its risk register (section 2), a row whose E2E column
                                          # is not "<covered>/<total> (<share>%)" as printed here, or an End-to-end
                                          # level row (section 1) without the overall "<covered> of <total>
                                          # scenarios (<share>%)"
"""
import argparse
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COVERS = re.compile(r"^#\s*Covers:\s*(.+?)\s*›\s*(.+?)(?:\s*›\s*(.+?))?\s*$")


def norm(text):
    # "Describe a long attack (opening part)" covers "Describe a long attack".
    return re.sub(r"\s*\(.*?\)\s*$", "", text).strip().lower()


def read_specs():
    # The main specs, plus the delta specs of changes still in progress (not the archive), so a flow can cover a scenario
    # that its own change adds. A REMOVED requirement of an open change stays listed until the change is archived.
    scenarios = {}
    specs = sorted((ROOT / "openspec" / "specs").glob("*/spec.md"))
    specs += sorted(p for p in (ROOT / "openspec" / "changes").glob("*/specs/*/spec.md") if "archive" not in p.parts)
    for spec in specs:
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
    scripts = [ROOT / "qa" / "device_scenarios.sh", ROOT / "qa" / "split_apks.sh"]
    for flow in sorted((ROOT / ".maestro").rglob("*.yaml")) + scripts:
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


def share(hit, total):
    return f"{100 * hit / total:.0f}%"


def section(text, number):
    # The body of the "## <number>. ..." section, up to the next "## " heading.
    match = re.search(rf"^## {number}\. .*?$(.*?)(?=^## |\Z)", text, re.M | re.S)
    return match.group(1) if match else ""


def check_register(path, shares, total_hit, total):
    text = Path(path).read_text(encoding="utf-8")
    register, levels, problems = section(text, 2), section(text, 1), []
    for capability, (hit, count) in shares.items():
        row = re.search(rf"^\| {re.escape(capability)} \| ([^|]*)\|", register, re.M)
        want = f"{hit}/{count} ({share(hit, count)})"
        if not row:
            problems.append(f"{capability}: no row in the risk register")
        elif row.group(1).strip() != want:
            problems.append(f"{capability}: the register says {row.group(1).strip()}, the flows give {want}")
    level = re.search(r"^\| End-to-end \|.*$", levels, re.M)
    want = f"{total_hit} of {total} scenarios ({share(total_hit, total)})"
    if not level or want not in level.group(0):
        problems.append(f"the End-to-end level row does not say {want}")
    return problems


def main():
    parser = argparse.ArgumentParser(description="Which OpenSpec scenarios have an end-to-end Maestro flow?")
    parser.add_argument("--missing", action="store_true", help="list the scenarios without a flow")
    parser.add_argument("--below", type=float, metavar="PERCENT",
                        help="list the capabilities with under PERCENT of their scenarios covered")
    parser.add_argument("--register", metavar="FILE", help="check the test strategy's risk register against the flows")
    args = parser.parse_args()
    scenarios = read_specs()
    covered, malformed = read_flows()
    capabilities = sorted({key[0] for key in scenarios})
    total_hit = 0
    shares = {}
    print(f"{'capability':<18} {'covered':>8} {'total':>6}")
    for capability in capabilities:
        keys = [key for key in scenarios if key[0] == capability]
        hit = sum(1 for key in keys if key in covered)
        total_hit += hit
        shares[capability] = (hit, len(keys))
        print(f"{capability:<18} {hit:>8} {len(keys):>6}")
    print(f"{'all':<18} {total_hit:>8} {len(scenarios):>6}  ({share(total_hit, len(scenarios))})")

    stale = {key: flows for key, flows in covered.items() if key not in scenarios}
    if stale:
        print("\nCovers lines that match no scenario (renamed or removed?):")
        for key, flows in stale.items():
            print(f"  {' › '.join(key)}  <- {', '.join(flows)}")
    if malformed:
        print("\nCovers lines without capability › requirement › scenario:")
        for line in malformed:
            print(f"  {line}")
    if args.missing:
        print("\nScenarios without an end-to-end flow:")
        for key, (requirement, scenario) in scenarios.items():
            if key not in covered:
                print(f"  {key[0]} › {requirement} › {scenario}")
    if args.below is not None:
        print(f"\nCapabilities with under {args.below:.0f}% of their scenarios covered:")
        for capability, (hit, total) in shares.items():
            if 100 * hit < args.below * total:
                print(f"  {capability}: {hit} of {total} ({share(hit, total)})")
    out_of_date = []
    if args.register:
        out_of_date = check_register(args.register, shares, total_hit, len(scenarios))
        if out_of_date:
            print(f"\nThe test strategy {args.register} is out of date:")
            for problem in out_of_date:
                print(f"  {problem}")
    return 1 if stale or malformed or out_of_date else 0


if __name__ == "__main__":
    sys.exit(main())
