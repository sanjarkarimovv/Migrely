"""Write Migrely backups with known content, and print what the Doctor report and Insights must show for them (QA).

    python3 -I qa/seed_backup.py <seed> <out.json> --today YYYY-MM-DD [--zone Asia/Tashkent]
    python3 -I qa/seed_backup.py --list [--today YYYY-MM-DD] [--zone Asia/Tashkent]
    python3 -I qa/seed_backup.py --self-test

<seed> writes a backup (format "migrely-backup", schemaVersion 6) whose dates count back from --today, the emulator's
date in --zone, the emulator's zone, and prints what the app must show once it is imported with "Replace" on that
date. Every attack ended before --today, none overlaps another, every time is epoch milliseconds, an attack's created
and updated times are its start and end, and IDs are uuid5 of the seed, the kind and an index, so the same --today
always writes the same file.
--list prints each seed's description and values; --today defaults to the host's date in --zone.
--self-test writes every seed for several dates into a temporary folder, checks each file with backup_files.py json,
writes it twice to compare the bytes, then counts the values again from the written JSON in a plainer way (dates hour
by hour, whole-number rounding) and compares them with what --list prints and with the spec's examples. Exits 1 on a
mismatch.

The values follow the app's rules (release/1.0.0): an attack's days and hours in the zone it was logged in, a
medication's day in the zone it was taken in, "the last N months" from the day after today minus N months through
today, a month's average as days x 30 / the period's days rounded half up to one decimal, shares as whole percents
rounded half up. Times are printed 24-hour; a 12-hour phone shows 22:30 as 10:30 PM.

Seeds: report-three-months, report-partial-month, report-tokyo, report-heavy, insights-patterns, insights-three,
five-old, five-new, four-old.
"""
import argparse
import calendar
import json
import math
import subprocess
import sys
import tempfile
import uuid
from collections import Counter
from datetime import date, datetime, timedelta, timezone
from decimal import ROUND_HALF_UP, Decimal
from functools import partial
from pathlib import Path
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

FORMAT = "migrely-backup"
SCHEMA_VERSION = 6
DEFAULT_ZONE = "Asia/Tashkent"
TOKYO = "Asia/Tokyo"
NAMESPACE = uuid.uuid5(uuid.NAMESPACE_URL, "https://migrely.health/qa/seed_backup")
DAY = timedelta(days=1)
MINUTE = 60_000
HOUR = 60 * MINUTE
BED_REST = 3
FIELDS = {
    "attacks": {"id", "startEpochMillis", "startZoneId", "endEpochMillis", "intensityPoints",
                "longCheckConfirmedEpochMillis", "longCheckDisabled", "tags", "functionalImpact", "note",
                "createdEpochMillis", "updatedEpochMillis"},
    "medications": {"id", "name", "kind", "category", "defaultDose", "archived", "createdEpochMillis",
                    "updatedEpochMillis"},
    "intakes": {"id", "medicationId", "attackId", "takenEpochMillis", "takenZoneId", "dose", "effect", "effectClosed",
                "createdEpochMillis"},
    "dayMarks": {"date", "period", "painFree", "updatedEpochMillis"},
}
TRIGGERS = {"stress": "Stress", "too_little_sleep": "Too little sleep", "too_much_sleep": "Too much sleep",
            "skipped_meal": "Skipped meal", "dehydration": "Dehydration", "alcohol": "Alcohol", "caffeine": "Caffeine",
            "period": "Period", "weather": "Weather", "bright_light_or_screens": "Bright light or screens",
            "smell": "Smell", "physical_effort": "Physical effort", "travel": "Travel"}
TAG_KEYS = {
    "LOCATION": {"left", "right", "both_sides", "forehead", "back_of_head", "around_eyes", "whole_head"},
    "SYMPTOM": {"aura", "nausea", "vomiting", "light_sensitivity", "sound_sensitivity", "smell_sensitivity",
                "dizziness", "neck_pain"},
    "TRIGGER": set(TRIGGERS),
}
EFFECTS = ("A_LOT", "COMPLETELY", "A_LITTLE", "NOT_AT_ALL")
HELPED = {"A_LOT", "COMPLETELY"}
IMPACTS = ("No impact", "Less active", "Had to stop", "Bed rest")
LEVELS = ("Mild", "Moderate", "Severe")
CATEGORIES = {"SIMPLE": "Simple painkiller", "TRIPTAN": "Triptan", "COMBINATION_OTHER": "Combination or other"}
OVERUSE = ("SIMPLE", "TRIPTAN", "COMBINATION_OTHER", "MULTIPLE")
LIMITS = {"SIMPLE": 15, "TRIPTAN": 10, "COMBINATION_OTHER": 10, "MULTIPLE": 10}
CARD_TITLES = {"SIMPLE": "Painkillers", "TRIPTAN": "Triptans", "COMBINATION_OTHER": "Combination or other medications",
               "MULTIPLE": "Acute medications"}
INDICATOR_ROWS = {"SIMPLE": "Simple painkillers", "TRIPTAN": "Triptans", "COMBINATION_OTHER": "Combination or other",
                  "MULTIPLE": "All attack medication"}
SOFT_BODY = "Taking them on {} days or more a month can lead to more headaches. Worth keeping an eye on."
DOCTOR_BODY = "Taking them this often can make headaches more frequent. It's worth talking to your doctor."
REACHED = "At this many days, medication can make headaches more frequent. Talk to your doctor about it."
WEEKDAYS = ("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
TIMES_OF_DAY = (("Night, 00–06", "at night"), ("Morning, 06–12", "in the morning"),
                ("Afternoon, 12–18", "in the afternoon"), ("Evening, 18–24", "in the evening"))
PAYWALL_READY = ('Paywall by itself: once, on the next return to home in normal mode, headline "See your patterns", '
                 "if the user is free, no paywall showed itself in the last 7 days (the onboarding one counts), this "
                 "install never showed the 5th-migraine paywall, Google Play billing is available and no attack is in "
                 "progress")
PAYWALL_NOT_READY = "Paywall by itself: none, as Insights cannot show patterns yet; a tap on Insights still opens it"
TEST_DATES = (date(2026, 10, 25), date(2026, 3, 1), date(2026, 12, 31), date(2028, 2, 29))

DESCRIPTIONS = {
    "report-three-months": "3-month Doctor report: 18 migraine days, 4 at bed rest, notes NOTE-ALPHA-7 and NOTE-BETA-9",
    "report-partial-month": "1-month Doctor report: Sumatriptan on 11 days across the month change, at the triptan "
                            "limit",
    "report-tokyo": "an attack logged in Asia/Tokyo whose Tokyo date is the day after the phone's, and one at home",
    "report-heavy": "900 short migraines over 12 months, 2 or 3 a day, each with a note: more than 50 pages hold",
    "insights-patterns": "10 migraines in 6 months: Stress with 6, 3 on Mondays, Ibuprofen helped on 7 of 9 answers",
    "insights-three": "3 migraines in 6 months, the first 45 days ago: 2 more are needed for patterns",
    "five-old": "5 migraines in 6 months, the first 40 days ago: patterns ready, the paywall shows itself",
    "five-new": "5 migraines, the first 10 days ago: patterns not ready yet",
    "four-old": "4 migraines, the first 40 days ago: log the 5th in the app",
}

THREE_MONTHS = (  # days before today, start, minutes, impact, intensity points, note, Ibuprofen (minutes in, answer)
    (84, "09:00", 300, 3, (2, 3), None, ()),
    (78, "21:30", 360, 1, (), None, ()),
    (71, "14:00", 180, None, (), "NOTE-ALPHA-7", ()),
    (65, "07:00", 480, 3, (4, 7, 5), None, ((30, "A_LOT"),)),
    (58, "23:00", 300, 2, (), None, ((30, "COMPLETELY"), (180, None))),
    (50, "16:00", 480, 0, (), None, ()),
    (43, "10:00", 360, 3, (8,), None, ()),
    (36, "22:00", 240, 1, (), None, ()),
    (29, "13:00", 120, 2, (), None, ()),
    (22, "08:00", 600, 3, (6, 10), None, ((30, "A_LITTLE"), (360, "A_LOT"))),
    (15, "20:00", 360, 1, (), "NOTE-BETA-9", ((30, "A_LOT"),)),
    (8, "12:00", 180, None, (), None, ()),
    (3, "06:30", 240, 2, (), None, ()),
)
PATTERNS = (  # weekday (Monday 0), start, minutes, triggers, impact, Ibuprofen's answer (None: not asked again)
    (0, "07:30", 240, ("stress", "too_little_sleep"), 2, "COMPLETELY"),
    (1, "06:00", 360, ("stress", "too_little_sleep", "weather"), 2, "A_LOT"),
    (6, "21:00", 270, (), 1, "NOT_AT_ALL"),
    (0, "09:00", 300, ("stress", "skipped_meal"), 3, "COMPLETELY"),
    (3, "11:45", 120, ("weather", "travel"), 1, "A_LITTLE"),
    (2, "05:30", 180, ("stress",), 3, "A_LOT"),
    (1, "12:00", 240, ("too_little_sleep", "caffeine"), 0, None),
    (4, "16:30", 180, ("stress", "skipped_meal"), 2, "COMPLETELY"),
    (0, "14:00", 180, ("stress",), 1, "A_LOT"),
    (5, "18:00", 180, ("too_little_sleep",), None, "COMPLETELY"),
)
FEW = {"insights-three": (45, 20, 6), "five-old": (40, 31, 22, 13, 4), "five-new": (10, 8, 6, 4, 2),
       "four-old": (40, 28, 15, 5)}
FEW_SPEC = {
    "insights-three": {"card": ["2 more migraines to see your patterns"], "ready": False},
    "five-old": {"card": [], "ready": True},
    "five-new": {"card": ["Patterns show after 30 days of entries: 20 more days to go."], "ready": False},
    "four-old": {"card": ["1 more migraine to see your patterns"], "ready": False, "ready_after_one_more": True},
}
HEAVY_ATTACKS = 900
HEAVY_CLOCKS = ("07:00", "13:00", "19:00")
HEAVY_TAGS = (("LOCATION", "left"), ("LOCATION", "around_eyes"), ("SYMPTOM", "nausea"),
              ("SYMPTOM", "light_sensitivity"), ("SYMPTOM", "sound_sensitivity"), ("SYMPTOM", "dizziness"),
              ("SYMPTOM", "neck_pain"), ("TRIGGER", "stress"), ("TRIGGER", "too_little_sleep"),
              ("TRIGGER", "bright_light_or_screens"))


def fail(message):
    print(f"FAIL: {message}")
    sys.exit(1)


def millis(day, clock, zone):
    hour, minute = (int(part) for part in clock.split(":"))
    return int(datetime(day.year, day.month, day.day, hour, minute, tzinfo=ZoneInfo(zone)).timestamp()) * 1000


def moment(epoch_millis, zone):
    return datetime.fromtimestamp(epoch_millis // 1000, ZoneInfo(zone)) + timedelta(milliseconds=epoch_millis % 1000)


def instant(epoch_millis):
    return datetime.fromtimestamp(epoch_millis // 1000, timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def minus_months(day, months):
    year, month = divmod(day.year * 12 + day.month - 1 - months, 12)
    return date(year, month + 1, min(day.day, calendar.monthrange(year, month + 1)[1]))


def last_months(months, today):
    return minus_months(today, months) + DAY, today


def within(day, span):
    return span[0] <= day <= span[1]


def month_of(day):
    return day.year, day.month


def pdf_date(day):
    return f"{day.day} {calendar.month_abbr[day.month]} {day.year}"


def counted(count, one, many=None):
    return f"{count} {one if count == 1 else many or one + 's'}"


class Backup:
    """One seed's records, written the way the app exports them."""

    def __init__(self, seed, zone):
        self.seed, self.zone = seed, zone
        self.attacks, self.medications, self.intakes, self.day_marks = [], [], [], []

    def uid(self, kind, index):
        return str(uuid.uuid5(NAMESPACE, f"{self.seed}/{kind}/{index}"))

    def attack(self, day, clock, minutes, logged_in=None, impact=None, peaks=(), tags=(), note=None):
        start = millis(day, clock, self.zone)
        end = start + minutes * MINUTE
        self.attacks.append({
            "id": self.uid("attack", len(self.attacks)),
            "startEpochMillis": start,
            "startZoneId": logged_in or self.zone,
            "endEpochMillis": end,
            "intensityPoints": [{"atEpochMillis": start + index * (end - start) // len(peaks), "value": value}
                                for index, value in enumerate(peaks)],
            "longCheckConfirmedEpochMillis": None,
            "longCheckDisabled": False,
            "tags": [{"kind": kind, "key": key} for kind, key in tags],
            "functionalImpact": impact,
            "note": note,
            "createdEpochMillis": start,
            "updatedEpochMillis": end,
        })
        return self.attacks[-1]

    def medication(self, name, kind, category, dose):
        self.medications.append({"id": self.uid("medication", len(self.medications)), "name": name, "kind": kind,
                                 "category": category, "defaultDose": dose, "archived": False,
                                 "createdEpochMillis": None, "updatedEpochMillis": None})
        return self.medications[-1]

    def intake(self, medication, taken, attack=None, effect=None, closed=None):
        self.intakes.append({
            "id": self.uid("intake", len(self.intakes)),
            "medicationId": medication["id"],
            "attackId": attack and attack["id"],
            "takenEpochMillis": taken,
            "takenZoneId": attack["startZoneId"] if attack else self.zone,
            "dose": medication["defaultDose"],
            "effect": effect,
            "effectClosed": effect is not None if closed is None else closed,
            "createdEpochMillis": taken + MINUTE,
        })

    def day_mark(self, day, pain_free=False, period=False):
        self.day_marks.append({"date": day.isoformat(), "period": period, "painFree": pain_free,
                               "updatedEpochMillis": millis(day, "21:00", self.zone)})

    def to_json(self):
        for medication in self.medications:
            first = min(i["takenEpochMillis"] for i in self.intakes if i["medicationId"] == medication["id"])
            medication["createdEpochMillis"] = medication["updatedEpochMillis"] = first - 10 * MINUTE
        records = {
            "attacks": sorted(self.attacks, key=lambda a: a["startEpochMillis"]),
            "customTags": [],
            "medications": self.medications,
            "intakes": sorted(self.intakes, key=lambda i: i["takenEpochMillis"]),
            "dayMarks": sorted(self.day_marks, key=lambda m: m["date"]),
        }
        return dict({"format": FORMAT, "schemaVersion": SCHEMA_VERSION,
                     "exportedAt": instant(latest_millis(records) + 10 * MINUTE)}, **records)


def latest_millis(records):
    return max(value for kind in FIELDS for entry in records[kind] for key, value in entry.items()
               if key.endswith("EpochMillis") and value is not None)


def three_months(today, zone):
    backup = Backup("report-three-months", zone)
    ibuprofen = backup.medication("Ibuprofen", "ACUTE", "SIMPLE", "400 mg")
    backup.attack(last_months(3, today)[0] - DAY, "22:30", 240, impact=2)
    for days, clock, minutes, impact, peaks, note, doses in THREE_MONTHS:
        attack = backup.attack(today - days * DAY, clock, minutes, impact=impact, peaks=peaks, note=note)
        for after, effect in doses:
            backup.intake(ibuprofen, attack["startEpochMillis"] + after * MINUTE, attack, effect)
    for days in (40, 10):
        backup.day_mark(today - days * DAY, pain_free=True)
    return backup


def spread(window, around, count):
    """The window's first day, then every other day centred on [around], the nearest free days filling in."""
    first, last = window
    wanted = [first] + [around + (2 * index - count + 1) * DAY for index in range(count)]
    chosen = list(dict.fromkeys(day for day in wanted if first <= day <= last))
    free = sorted((first + index * DAY for index in range((last - first).days + 1)),
                  key=lambda day: (abs((day - around).days), day))
    return sorted((chosen + [day for day in free if day not in chosen])[:count])


def partial_month(today, zone):
    backup = Backup("report-partial-month", zone)
    sumatriptan = backup.medication("Sumatriptan", "ACUTE", "TRIPTAN", "50 mg")
    start = last_months(1, today)[0]
    home = today - 29 * DAY
    days = [min(start, home) - DAY] + spread((max(start, home), today - DAY), today.replace(day=1), 11)
    for index, day in enumerate(days):
        attack = backup.attack(day, "15:00", 240, impact=index % 3)
        backup.intake(sumatriptan, attack["startEpochMillis"] + 30 * MINUTE, attack, EFFECTS[index % 3])
    return backup


def tokyo(today, zone):
    backup = Backup("report-tokyo", zone)
    backup.attack(today - 10 * DAY, "20:30", 180, logged_in=TOKYO, impact=1, peaks=(6,))
    backup.attack(today - 5 * DAY, "14:00", 180, impact=2, peaks=(4,))
    return backup


def heavy(today, zone):
    backup = Backup("report-heavy", zone)
    ibuprofen = backup.medication("Ibuprofen", "ACUTE", "SIMPLE", "400 mg")
    first = last_months(12, today)[0]
    days = (today - first).days
    extra = HEAVY_ATTACKS - 2 * days
    starts = [(first + index * DAY, clock) for index in range(days)
              for clock in HEAVY_CLOCKS[:2 + (index + 1) * extra // days - index * extra // days]]
    for index, (day, clock) in enumerate(starts):
        rank = len(starts) - index
        attack = backup.attack(day, clock, 60, impact=BED_REST if rank % 10 == 0 else rank % 3,
                               peaks=(2 + rank % 9,), tags=HEAVY_TAGS, note=f"HEAVY-{rank:04d}")
        if rank % 7 == 0:
            backup.intake(ibuprofen, attack["startEpochMillis"] + 15 * MINUTE, attack, EFFECTS[rank % 4])
    return backup


def insights_patterns(today, zone):
    backup = Backup("insights-patterns", zone)
    ibuprofen = backup.medication("Ibuprofen", "ACUTE", "SIMPLE", "400 mg")
    sumatriptan = backup.medication("Sumatriptan", "ACUTE", "TRIPTAN", "50 mg")
    topiramate = backup.medication("Topiramate", "PREVENTIVE", None, "25 mg")
    for index, (weekday, clock, minutes, triggers, impact, answer) in enumerate(PATTERNS):
        anchor = today - (4 + 11 * index) * DAY
        day = anchor - (anchor.weekday() - weekday) % 7 * DAY
        attack = backup.attack(day, clock, minutes, impact=impact, tags=[("TRIGGER", key) for key in triggers])
        backup.intake(ibuprofen, attack["startEpochMillis"] + 30 * MINUTE, attack, answer, closed=True)
        if index in (1, 3):
            backup.intake(sumatriptan, attack["startEpochMillis"] + 120 * MINUTE, attack, EFFECTS[index // 2])
    for days in (4, 3, 2):
        backup.intake(topiramate, millis(today - days * DAY, "08:00", zone))
    return backup


def few_migraines(name, today, zone):
    backup = Backup(name, zone)
    for index, days in enumerate(FEW[name]):
        backup.attack(today - days * DAY, ("08:30", "14:00", "19:30")[index % 3], 180, impact=index % 4,
                      peaks=(4 + index,))
    return backup


def start_date(attack):
    return moment(attack["startEpochMillis"], attack["startZoneId"]).date()


def touched(attack):
    first = start_date(attack)
    last = moment(attack["endEpochMillis"] - 1, attack["startZoneId"]).date()
    return [first + index * DAY for index in range((max(first, last) - first).days + 1)]


def taken_date(intake):
    return moment(intake["takenEpochMillis"], intake["takenZoneId"]).date()


def with_medication(data):
    medications = {m["id"]: m for m in data["medications"]}
    return [(intake, medications[intake["medicationId"]]) for intake in data["intakes"]]


def per_month(count, days):
    return str(Decimal(repr(count * 30.0 / days)).quantize(Decimal("0.1"), ROUND_HALF_UP))


def level_of(peak):
    return 1 if peak <= 3 else 2 if peak <= 6 else 3


def average_level(peaks):
    if not peaks:
        return None
    mean = sum(level_of(peak) for peak in peaks) / len(peaks)
    return LEVELS[int(Decimal(repr(mean)).quantize(Decimal(1), ROUND_HALF_UP)) - 1]


def spans_of(period):
    ends = [period[1]]
    while ends[-1] - 30 * DAY >= period[0]:
        ends.append(ends[-1] - 30 * DAY)
    return [(end - 29 * DAY, end) for end in reversed(ends)]


def months_of(period):
    months = [month_of(period[0])]
    while months[-1] < month_of(period[1]):
        year, month = months[-1]
        months.append((year + month // 12, month % 12 + 1))
    return months


def overused(intakes):
    dates = {}
    for intake, medication in intakes:
        dates.setdefault(medication["category"] or "COMBINATION_OTHER", set()).add(taken_date(intake))
    return (any(len(days) >= LIMITS[category] for category, days in dates.items())
            or len(dates) >= 2 and len(set().union(*dates.values())) >= LIMITS["MULTIPLE"])


def duration_of(minutes):
    return f"{minutes} min" if minutes < 60 else f"{minutes // 60} h {minutes % 60} min"


def row_of(attack, zone, first, doses):
    began = moment(attack["startEpochMillis"], attack["startZoneId"])
    start = (f"{pdf_date(began.date())}, {began:%H:%M}"
             + (f" ({attack['startZoneId']})" if attack["startZoneId"] != zone else "")
             + (", began before the period" if began.date() < first else ""))
    peak = max((p["value"] for p in attack["intensityPoints"]), default=None)
    impact = attack["functionalImpact"]
    return [start, duration_of((attack["endEpochMillis"] - attack["startEpochMillis"]) // MINUTE),
            f"{peak} · {LEVELS[level_of(peak) - 1]}" if peak is not None else "Not logged",
            IMPACTS[impact] if impact is not None else "", ", ".join(doses.get(attack["id"], [])) or "—"]


def medication_rows(intakes):
    groups = {}
    for intake, medication in intakes:
        groups.setdefault(medication["name"], []).append((intake, medication))
    rows = []
    for name, group in sorted(groups.items(), key=lambda item: (-len(item[1]), item[0])):
        medication = group[0][1]
        answered = sum(intake["effect"] is not None for intake, _ in group)
        helped = sum(intake["effect"] in HELPED for intake, _ in group)
        kind = ("Preventive" if medication["kind"] == "PREVENTIVE"
                else CATEGORIES.get(medication["category"], "For attacks, type not sure"))
        rows.append([name, kind, str(len(group)), str(len({taken_date(intake) for intake, _ in group})),
                     f"{helped} of {answered} answers" if answered else "Not asked"])
    return rows


def report(data, months, today, zone):
    period = last_months(months, today)
    days = (period[1] - period[0]).days + 1
    attacks = sorted((a for a in data["attacks"] if any(within(day, period) for day in touched(a))),
                     key=lambda a: a["startEpochMillis"], reverse=True)
    started = [a for a in attacks if within(start_date(a), period)]
    migraine_days = {day for a in attacks for day in touched(a) if within(day, period)}
    intakes = [(i, m) for i, m in with_medication(data) if within(taken_date(i), period)]
    acute = [(i, m) for i, m in with_medication(data) if m["kind"] == "ACUTE"]
    medication_days = {taken_date(i) for i, m in intakes if m["kind"] == "ACUTE"}
    spans = [span for span in spans_of(period) if overused([(i, m) for i, m in acute if within(taken_date(i), span)])]
    every_day = {day for a in data["attacks"] for day in touched(a)}
    doses = {}
    for intake, medication in sorted(with_medication(data), key=lambda pair: pair[0]["takenEpochMillis"]):
        doses.setdefault(intake["attackId"], []).append(" ".join(filter(None, (medication["name"], intake["dose"]))))
    return {
        "months": months,
        "period": f"{pdf_date(period[0])} to {pdf_date(period[1])} ({days} days). "
                  f"Made with Migrely on {pdf_date(today)}.",
        "days": days,
        "migraine_days": len(migraine_days),
        "per_month": per_month(len(migraine_days), days),
        "started": len(started),
        "medication_days": len(medication_days),
        "medication_per_month": per_month(len(medication_days), days),
        "spans": [f"{pdf_date(span[0])} to {pdf_date(span[1])}" for span in spans],
        "bed_rest": sum(a["functionalImpact"] == BED_REST for a in started),
        "average_peak": average_level([max(p["value"] for p in a["intensityPoints"])
                                       for a in started if a["intensityPoints"]]),
        "chronic": "Yes" if any(sum(month_of(day) == month for day in every_day) >= 15
                                for month in months_of(period)) else "No",
        "row_count": len(attacks),
        "rows": [row_of(a, zone, period[0], doses) for a in attacks],
        "medications": medication_rows(intakes),
        "notes": [(pdf_date(start_date(a)), a["note"].strip()) for a in attacks if (a["note"] or "").strip()],
    }


def percent(count, total):
    whole = math.floor(count * 100.0 / total + 0.5) if total else 0
    return "<1%" if whole == 0 < count else ">99%" if whole == 100 and count < total else f"{whole}%"


def busiest(counts, total, names, none):
    most = max(counts)
    return f"{names[counts.index(most)]}: {most} of {total}" if counts.count(most) == 1 else none


def progress_card(started, since):
    missing_migraines, missing_days = max(0, 5 - started), max(0, 30 - since)
    return ([f"{counted(missing_migraines, 'more migraine')} to see your patterns"] if missing_migraines else []) + (
        [f"Patterns show after 30 days of entries: {counted(missing_days, 'more day')} to go."] if missing_days else [])


def helped_lines(data, period):
    groups = {}
    for intake, medication in with_medication(data):
        if medication["kind"] == "ACUTE" and within(taken_date(intake), period):
            groups.setdefault(medication["name"], []).append(intake)
    lines = []
    for name, intakes in sorted(groups.items(), key=lambda item: (-len(item[1]), item[0])):
        answered = sum(intake["effect"] is not None for intake in intakes)
        helped = sum(intake["effect"] in HELPED for intake in intakes)
        lines.append(f"{name}: " + (f"Needs 3 answers (has {answered})" if answered < 3 else
                                    f"Helped a lot or completely: {helped} of {counted(answered, 'answer')} "
                                    f"({percent(helped, answered)})"))
    return lines


def patterns(data, started, period):
    total = len(started)
    counts = Counter(key for a in started for key in {t["key"] for t in a["tags"] if t["kind"] == "TRIGGER"})
    top = sorted(counts.items(), key=lambda item: (-item[1], list(TRIGGERS).index(item[0])))[:5]
    starts = [moment(a["startEpochMillis"], a["startZoneId"]) for a in started]
    weekdays = [sum(start.weekday() == day for start in starts) for day in range(7)]
    times = [sum(start.hour // 6 == part for start in starts) for part in range(4)]
    return {
        "triggers": [f"{TRIGGERS[key]} — {count} of {counted(total, 'migraine')} ({percent(count, total)})"
                     for key, count in top],
        "weekdays": weekdays,
        "busiest_day": busiest(weekdays, total, [f"Most often on {day}s" for day in WEEKDAYS],
                               "No single day stands out"),
        "times": times,
        "busiest_time": busiest(times, total, [f"Most often {words}" for _, words in TIMES_OF_DAY],
                                "No single time of day stands out"),
        "medications": helped_lines(data, period),
    }


def trends(data, today):
    migraine_days = {day for a in data["attacks"] for day in touched(a)}
    medication_days = {taken_date(i) for i, m in with_medication(data) if m["kind"] == "ACUTE"}
    lines = []
    for back in range(11, -1, -1):
        year, index = divmod(today.year * 12 + today.month - 1 - back, 12)
        month = (year, index + 1)
        started = [a for a in data["attacks"] if month_of(start_date(a)) == month]
        lines.append(f"{calendar.month_name[month[1]]} {year}: "
                     f"{counted(sum(month_of(day) == month for day in migraine_days), 'migraine day')}, "
                     f"{counted(len(started), 'migraine')}, "
                     f"{counted(sum(month_of(day) == month for day in medication_days), 'day')} with attack "
                     f"medication, "
                     f"{counted(sum(a['functionalImpact'] == BED_REST for a in started), 'bed-rest migraine')}")
    return lines


def insights(data, months, today):
    period = last_months(months, today)
    started = [a for a in data["attacks"] if within(start_date(a), period)]
    earliest = min(data["attacks"], key=lambda a: a["startEpochMillis"], default=None)
    since = (today - start_date(earliest)).days if earliest else 0
    view = {"months": months, "period": period, "started": len(started),
            "card": progress_card(len(started), since), "trends": trends(data, today)}
    if not view["card"]:
        view.update(patterns(data, started, period))
    return view


def overuse(data, today):
    window = (today - 29 * DAY, today)
    dates = {}
    for intake, medication in with_medication(data):
        if medication["kind"] == "ACUTE" and within(taken_date(intake), window):
            dates.setdefault(medication["category"] or "COMBINATION_OTHER", set()).add(taken_date(intake))
    days = {category: len(dates[category]) for category in OVERUSE if category in dates}
    if len(dates) >= 2:
        days["MULTIPLE"] = len(set().union(*dates.values()))
    return days


def overuse_card(days):
    def level(category):
        return 2 if days[category] >= LIMITS[category] else 1 if days[category] >= LIMITS[category] - 2 else 0

    singles = [category for category in OVERUSE[:3] if category in days and level(category)]
    top_single = max((level(category) for category in singles), default=0)
    candidates = singles + [c for c in OVERUSE[3:] if c in days and level(c) > top_single]
    if not candidates:
        return ["no overuse card"]
    top = max(level(category) for category in candidates)
    chosen = max((c for c in candidates if level(c) == top), key=lambda c: (days[c], -OVERUSE.index(c)))
    return [f"{CARD_TITLES[chosen]} on {days[chosen]} of the last 30 days",
            DOCTOR_BODY if top == 2 else SOFT_BODY.format(LIMITS[chosen]), "Got it"]


def indicator(days):
    lines = []
    for category in OVERUSE:
        if category in days:
            row = f"{INDICATOR_ROWS[category]}: {counted(days[category], 'day')} (limit {LIMITS[category]})"
            if days[category] >= LIMITS[category]:
                row += f", {'at' if days[category] == LIMITS[category] else 'over'} the limit"
            lines.append(row)
    return lines + ([REACHED] if any(days[c] >= LIMITS[c] for c in days) else [])


def patterns_ready(starts, today):
    period = last_months(6, today)
    return bool(starts) and sum(within(day, period) for day in starts) >= 5 and (today - min(starts)).days >= 30


def report_lines(summary):
    short = summary["days"] < 28

    def average(value, total):
        return f"{total} days in all" if short else f"{value} ({total} days in all)"

    facts = [
        ("Migraine days" if short else "Migraine days a month, on average",
         average(summary["per_month"], summary["migraine_days"])),
        ("Migraines started", summary["started"]),
        ("Days with attack medication" if short else "Days with attack medication a month, on average",
         average(summary["medication_per_month"], summary["medication_days"])),
        ("30-day spans at the medication-overuse limit", ", ".join(summary["spans"]) or "None"),
        ("Migraines with bed rest", summary["bed_rest"]),
        ("Average peak pain", summary["average_peak"] or "Not logged"),
        ("A month with 15 or more migraine days", summary["chronic"]),
    ]
    return ([f"Doctor report, {counted(summary['months'], 'month')}, page 1: {summary['period']}"]
            + [f"  {label} | {value}" for label, value in facts])


def migraine_rows(summary):
    return ([f"Migraines table, {counted(summary['row_count'], 'row')}, newest first "
             "(Start | Duration | Peak | Impact | Medications):"] + [f"  {' | '.join(row)}" for row in summary["rows"]])


def medication_lines(summary):
    return (["Medications table (Medication | Type | Intakes | Days used | Helped a lot or completely):"]
            + ([f"  {' | '.join(row)}" for row in summary["medications"]]
               or ["  No medication was logged in this period."]))


def insights_lines(view, with_trends):
    head = (f"Insights, {counted(view['months'], 'month')} ({pdf_date(view['period'][0])} to "
            f"{pdf_date(view['period'][1])}), {counted(view['started'], 'migraine')} started:")
    if view["card"]:
        body = ["  The card reads: " + " / ".join(view["card"]),
                "  No other section shows yet; the last 12 months do"]
    else:
        total = view["started"]
        triggers = view["triggers"] or ["No triggers were logged in this period."]
        medications = view["medications"] or ["No attack medication was logged in this period."]
        body = (["  Often logged with your migraines:"] + [f"    {line}" for line in triggers]
                + ["  When migraines come, by day of the week:", f"    {view['busiest_day']}",
                   "    " + ", ".join(f"{day} {n} of {total}" for day, n in zip(WEEKDAYS, view["weekdays"])),
                   "  By time of day:", f"    {view['busiest_time']}",
                   "    " + ", ".join(f"{label} {n} of {total}" for (label, _), n in zip(TIMES_OF_DAY, view["times"])),
                   "  What helped:"] + [f"    {line}" for line in medications]
                + ['  No "Around your period" section: no day is marked as a period'])
    trend = (["  The last 12 months, as TalkBack reads each row (month names as on an English phone):"]
             + [f"    {line}" for line in view["trends"]]) if with_trends else []
    return [head] + body + trend


def paywall(data, today):
    starts = [start_date(a) for a in data["attacks"]]
    return {"ready": patterns_ready(starts, today), "ready_after_one_more": patterns_ready(starts + [today], today)}


def expect_three_months(data, today, zone):
    summary = report(data, 3, today, zone)
    marks = ", ".join(pdf_date(date.fromisoformat(mark["date"])) for mark in data["dayMarks"])
    midnight = [moment(a["endEpochMillis"], a["startZoneId"]).date() for a in data["attacks"]
                if moment(a["endEpochMillis"], a["startZoneId"]).strftime("%H:%M") == "00:00"]
    lines = report_lines(summary) + migraine_rows(summary) + medication_lines(summary) + [
        f"Calendar: {marks} read Pain-free; {', '.join(map(pdf_date, midnight))} is not a migraine day, as the "
        "migraine before it ends at exactly 00:00",
        "Include notes on, Notes table, newest first: "
        + "; ".join(f"{day} | {note}" for day, note in summary["notes"]),
        "Include notes off: neither NOTE-ALPHA-7 nor NOTE-BETA-9 appears anywhere in the PDF",
    ]
    spec = [("migraine days", summary["migraine_days"], 18), ("bed-rest migraines", summary["bed_rest"], 4),
            ("average peak, Mild 3 and Severe 7, 8, 10 halved up", summary["average_peak"], "Severe"),
            ("notes", sorted(note for _, note in summary["notes"]), ["NOTE-ALPHA-7", "NOTE-BETA-9"])]
    if summary["days"] == 92:
        spec.append(("migraine days a month", summary["per_month"], "5.9"))
    else:
        lines.append(f"The spec's 5.9 is for 92 days; on this date the 3 months are {summary['days']} days")
    return {"report": summary}, lines, spec


def expect_partial_month(data, today, zone):
    summary = report(data, 1, today, zone)
    days = overuse(data, today)
    taken = sorted({taken_date(i) for i, m in with_medication(data) if m["category"] == "TRIPTAN"})
    home = today - 29 * DAY
    counted_days = [day for day in taken if within(day, (home, today))]
    lines = ["Home in normal mode, the medication card: " + " / ".join(overuse_card(days)),
             f"History, {calendar.month_name[today.month]} so far, Last 30 days: " + " / ".join(indicator(days))]
    lines += report_lines(summary) + medication_lines(summary) + [
        "Sumatriptan days counted: " + ", ".join(pdf_date(day) for day in counted_days),
        f"Not counted: {pdf_date(taken[0])}, before both the period and home's last 30 days",
    ]
    first = max(last_months(1, today)[0], home)
    spec = [("home's triptan days", days.get("TRIPTAN"), 11),
            ("triptan days in the period", summary["medication_days"], 11),
            ("span ending today listed", f"{pdf_date(home)} to {pdf_date(today)}" in summary["spans"], True)]
    if first < today.replace(day=1) <= today - DAY:
        spec.append(("months the triptan days fall in", len({month_of(day) for day in counted_days}), 2))
    else:
        lines.append("On this date no month change falls inside both the period and home's last 30 days")
    return {"report": summary, "home": {"days": days}}, lines, spec


def expect_tokyo(data, today, zone):
    summary = report(data, 3, today, zone)
    attack = next(a for a in data["attacks"] if a["startZoneId"] == TOKYO)
    here, there = moment(attack["startEpochMillis"], zone), moment(attack["startEpochMillis"], TOKYO)
    days = sorted({day for a in data["attacks"] for day in touched(a)})
    lines = [
        f"The {TOKYO} attack starts at {here:%H:%M} on {pdf_date(here.date())} in {zone}, "
        f"which is {there:%H:%M} on {pdf_date(there.date())} in {TOKYO}",
        f"The app counts it on {pdf_date(there.date())}, its date in {TOKYO}, the zone it was logged in"
        + (f", and not on {pdf_date(here.date())}" if here.date() != there.date() else ""),
        "History, home and the report's calendar: migraine days on " + ", ".join(pdf_date(day) for day in days),
    ] + report_lines(summary) + migraine_rows(summary)
    spec = [("migraine days", summary["migraine_days"], 2)]
    if zone == DEFAULT_ZONE:
        spec += [("counted on", there.date(), today - 9 * DAY),
                 ("Tokyo date after the phone's", there.date() - here.date(), DAY)]
    return {"report": summary, "tokyo": {"counted_on": there.date(), "phone_date": here.date()}}, lines, spec


def expect_heavy(data, today, zone):
    summary = report(data, 12, today, zone)
    total = summary["row_count"]
    lines = report_lines(summary) + [
        f'Migraines table: "Showing the latest N of {total} migraines. The summary counts all of them." above it, then '
        f"N rows, newest first, from {summary['rows'][0][0]}; the PDF has at most 50 pages",
        f"N is known only once the PDF exists: the writer keeps as many rows as fit in 50 pages, measured with "
        f"Android's text layout; read it from the PDF, below {total}",
        f"Include notes on: the migraines table is cut first so that every note fits, so N is smaller than with notes "
        f'off; the Notes table starts with HEAVY-0001 and, if it says "Showing the latest K of {total} notes.", '
        "ends with HEAVY-K",
    ] + medication_lines(summary)
    per_day = Counter(start_date(a) for a in data["attacks"])
    spec = [("migraines touching the period", total, HEAVY_ATTACKS), ("started", summary["started"], HEAVY_ATTACKS),
            ("notes", len(summary["notes"]), HEAVY_ATTACKS), ("attacks a day", sorted(set(per_day.values())), [2, 3]),
            ("days with attacks", len(per_day), (today - last_months(12, today)[0]).days)]
    return {"report": summary}, lines, spec


def expect_insights_patterns(data, today, zone):
    view = insights(data, 6, today)
    gate = paywall(data, today)
    lines = insights_lines(view, True) + [
        f"Other periods: 3 months {counted(insights(data, 3, today)['started'], 'migraine')} started, "
        f"12 months {counted(insights(data, 12, today)['started'], 'migraine')}",
        PAYWALL_READY if gate["ready"] else PAYWALL_NOT_READY,
    ]
    spec = [("started", view["started"], 10),
            ("first trigger", view["triggers"][0], "Stress — 6 of 10 migraines (60%)"),
            ("busiest day", view["busiest_day"], "Most often on Mondays: 3 of 10"),
            ("night, morning, afternoon, evening", view["times"], [1, 4, 3, 2]),
            ("Ibuprofen", "Ibuprofen: Helped a lot or completely: 7 of 9 answers (78%)" in view["medications"], True),
            ("period day marks", sum(mark["period"] for mark in data["dayMarks"]), 0), ("ready", gate["ready"], True)]
    return {"insights": view, "paywall": gate}, lines, spec


def expect_few(name, data, today, zone):
    view = insights(data, 6, today)
    gate = paywall(data, today)
    lines = insights_lines(view, False) + [PAYWALL_READY if gate["ready"] else PAYWALL_NOT_READY]
    if name == "four-old":
        lines.append("Log a 5th migraine today and end it: Insights then show patterns, and on returning to home the "
                     "paywall shows itself once, by the same conditions")
    facts = {"card": view["card"], "started": view["started"], **gate}
    spec = [(key, facts[key], expected) for key, expected in FEW_SPEC[name].items()]
    return {"insights": view, "paywall": gate}, lines, spec + [("started", view["started"], len(FEW[name]))]


SEEDS = {
    "report-three-months": (three_months, expect_three_months),
    "report-partial-month": (partial_month, expect_partial_month),
    "report-tokyo": (tokyo, expect_tokyo),
    "report-heavy": (heavy, expect_heavy),
    "insights-patterns": (insights_patterns, expect_insights_patterns),
    **{name: (partial(few_migraines, name), partial(expect_few, name)) for name in FEW},
}


def backup_text(name, today, zone):
    return json.dumps(SEEDS[name][0](today, zone).to_json(), ensure_ascii=False, separators=(",", ":"))


def print_seed(name, data, today, zone):
    print(f"\n{name}: {DESCRIPTIONS[name]}")
    for line in SEEDS[name][1](data, today, zone)[1]:
        print(f"  {line}")


def header(today, zone):
    print(f"Values for --today {today} in {zone}, after Your data > Import a backup > Replace on a phone set to that "
          "date and zone. Report times are 24-hour here.")


def write_seed(name, out, today, zone):
    text = backup_text(name, today, zone)
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(text, encoding="utf-8")
    data = json.loads(text)
    print(f"wrote {out}: {len(data['attacks'])} attacks, {len(data['medications'])} medications, "
          f"{len(data['intakes'])} intakes, {len(data['dayMarks'])} day marks")
    header(today, zone)
    print_seed(name, data, today, zone)


def list_seeds(today, zone):
    header(today, zone)
    for name in SEEDS:
        print_seed(name, json.loads(backup_text(name, today, zone)), today, zone)


def plain_local(epoch_millis, zone):
    return datetime.fromtimestamp(epoch_millis / 1000, ZoneInfo(zone))


def plain_days(attack):
    stamps = list(range(attack["startEpochMillis"], attack["endEpochMillis"], HOUR)) + [attack["endEpochMillis"] - 1]
    return {plain_local(stamp, attack["startZoneId"]).date() for stamp in stamps}


def plain_start(attack):
    return plain_local(attack["startEpochMillis"], attack["startZoneId"])


def plain_first(months, today):
    year, month = today.year, today.month
    for _ in range(months):
        year, month = (year, month - 1) if month > 1 else (year - 1, 12)
    return date(year, month, min(today.day, calendar.monthrange(year, month)[1])) + timedelta(days=1)


def half_up(numerator, denominator):
    return (2 * numerator + denominator) // (2 * denominator)


def plain_tenths(count, days):
    tenths = half_up(count * 300, days)
    return f"{tenths // 10}.{tenths % 10}"


def plain_percent(count, total):
    whole = half_up(100 * count, total)
    if whole == 0 and count:
        return "<1%"
    return ">99%" if whole == 100 and count < total else f"{whole}%"


def acute_dates(data):
    kinds = {m["id"]: m["kind"] for m in data["medications"]}
    return {plain_local(i["takenEpochMillis"], i["takenZoneId"]).date()
            for i in data["intakes"] if kinds[i["medicationId"]] == "ACUTE"}


def recount_report(data, months, today):
    first = plain_first(months, today)
    days = (today - first).days + 1
    touching = [a for a in data["attacks"] if any(first <= day <= today for day in plain_days(a))]
    started = [a for a in touching if first <= plain_start(a).date() <= today]
    migraine = {day for a in touching for day in plain_days(a) if first <= day <= today}
    medication = {day for day in acute_dates(data) if first <= day <= today}
    levels = [min(3, (max(p["value"] for p in a["intensityPoints"]) + 2) // 3) for a in started if a["intensityPoints"]]
    newest = sorted(touching, key=lambda a: -a["startEpochMillis"])
    return {
        "days": days, "migraine_days": len(migraine), "per_month": plain_tenths(len(migraine), days),
        "started": len(started), "bed_rest": sum(a["functionalImpact"] == BED_REST for a in started),
        "medication_days": len(medication), "medication_per_month": plain_tenths(len(medication), days),
        "average_peak": LEVELS[half_up(sum(levels), len(levels)) - 1] if levels else None,
        "row_count": len(touching),
        "notes": [(pdf_date(plain_start(a).date()), a["note"].strip()) for a in newest if (a["note"] or "").strip()],
    }


def plain_helped(data, first, today):
    medications = {m["id"]: m for m in data["medications"]}
    groups = {}
    for intake in data["intakes"]:
        medication = medications[intake["medicationId"]]
        day = plain_local(intake["takenEpochMillis"], intake["takenZoneId"]).date()
        if medication["kind"] == "ACUTE" and first <= day <= today:
            groups.setdefault(medication["name"], []).append(intake["effect"])
    lines = []
    for name in sorted(groups, key=lambda name: (-len(groups[name]), name)):
        answers = [effect for effect in groups[name] if effect]
        helped = sum(effect in HELPED for effect in answers)
        lines.append(f"{name}: Needs 3 answers (has {len(answers)})" if len(answers) < 3 else
                     f"{name}: Helped a lot or completely: {helped} of {len(answers)} answers "
                     f"({plain_percent(helped, len(answers))})")
    return lines


def plain_trends(data, today):
    medication = acute_dates(data)
    lines = []
    year, month = today.year, today.month
    for _ in range(12):
        started = [a for a in data["attacks"] if (plain_start(a).year, plain_start(a).month) == (year, month)]
        days = len({day for a in data["attacks"] for day in plain_days(a) if (day.year, day.month) == (year, month)})
        taken = sum((day.year, day.month) == (year, month) for day in medication)
        bed_rest = sum(a["functionalImpact"] == BED_REST for a in started)
        lines.insert(0, f"{calendar.month_name[month]} {year}: {counted(days, 'migraine day')}, "
                        f"{counted(len(started), 'migraine')}, {counted(taken, 'day')} with attack medication, "
                        f"{counted(bed_rest, 'bed-rest migraine')}")
        year, month = (year, month - 1) if month > 1 else (year - 1, 12)
    return lines


def recount_insights(data, months, today):
    first = plain_first(months, today)
    started = [plain_start(a) for a in data["attacks"] if first <= plain_start(a).date() <= today]
    attacks = [a for a in data["attacks"] if first <= plain_start(a).date() <= today]
    earliest = plain_start(min(data["attacks"], key=lambda a: a["startEpochMillis"])).date()
    total, since = len(started), (today - earliest).days
    card = []
    if total < 5:
        card.append(f"{5 - total} more migraine{'' if 5 - total == 1 else 's'} to see your patterns")
    if since < 30:
        card.append(f"Patterns show after 30 days of entries: {30 - since} more day{'' if 30 - since == 1 else 's'} "
                    "to go.")
    plain = {"started": total, "card": card, "trends": plain_trends(data, today)}
    if card:
        return plain
    counts = sorted(((sum(any(t == {"kind": "TRIGGER", "key": key} for t in a["tags"]) for a in attacks), key)
                     for key in TRIGGERS), key=lambda pair: -pair[0])
    weekdays = [sum(start.isoweekday() == day for start in started) for day in range(1, 8)]
    times = [sum(sum(start.hour >= hour for hour in (6, 12, 18)) == part for start in started) for part in range(4)]
    leaders = [day for day in range(7) if weekdays[day] == max(weekdays)]
    peaks = [part for part in range(4) if times[part] == max(times)]
    plain.update({
        "triggers": [f"{TRIGGERS[key]} — {n} of {total} migraines ({plain_percent(n, total)})"
                     for n, key in counts if n][:5],
        "weekdays": weekdays,
        "busiest_day": f"Most often on {WEEKDAYS[leaders[0]]}s: {max(weekdays)} of {total}" if len(leaders) == 1
        else "No single day stands out",
        "times": times,
        "busiest_time": f"Most often {TIMES_OF_DAY[peaks[0]][1]}: {max(times)} of {total}" if len(peaks) == 1
        else "No single time of day stands out",
        "medications": plain_helped(data, first, today),
    })
    return plain


def recount_overuse(data, today):
    medications = {m["id"]: m for m in data["medications"]}
    dates = {}
    for intake in data["intakes"]:
        medication = medications[intake["medicationId"]]
        day = plain_local(intake["takenEpochMillis"], intake["takenZoneId"]).date()
        if medication["kind"] == "ACUTE" and 0 <= (today - day).days < 30:
            dates.setdefault(medication["category"] or "COMBINATION_OTHER", set()).add(day)
    days = {category: len(found) for category, found in dates.items()}
    if len(dates) > 1:
        days["MULTIPLE"] = len(set.union(*dates.values()))
    return {"days": days}


def recount_paywall(data, today):
    first = plain_first(6, today)
    starts = sorted(plain_start(a).date() for a in data["attacks"])
    ready = len([day for day in starts if first <= day <= today]) >= 5 and (today - starts[0]).days >= 30
    more = len([day for day in starts + [today] if first <= day <= today]) >= 5 and (today - starts[0]).days >= 30
    return {"ready": ready, "ready_after_one_more": more}


def recount(data, today, zone, facts):
    plain = {}
    if "report" in facts:
        plain["report"] = recount_report(data, facts["report"]["months"], today)
    if "insights" in facts:
        plain["insights"] = recount_insights(data, facts["insights"]["months"], today)
    if "home" in facts:
        plain["home"] = recount_overuse(data, today)
    if "paywall" in facts:
        plain["paywall"] = recount_paywall(data, today)
    if "tokyo" in facts:
        attack = next(a for a in data["attacks"] if a["startZoneId"] == TOKYO)
        plain["tokyo"] = {"counted_on": plain_start(attack).date(),
                          "phone_date": plain_local(attack["startEpochMillis"], zone).date()}
    return plain


def problems(data, today, zone):
    found = [f"{kind} entry with fields off: {sorted(set(entry) ^ fields)}"
             for kind, fields in FIELDS.items() for entry in data[kind] if set(entry) != fields]
    ids = [entry["id"] for kind in ("attacks", "medications", "intakes") for entry in data[kind]]
    if len(set(ids)) != len(ids) or any(str(uuid.UUID(value)) != value for value in ids):
        found.append("IDs are not unique UUIDs")
    midnight = millis(today, "00:00", zone)
    attacks = sorted(data["attacks"], key=lambda a: a["startEpochMillis"])
    for attack in attacks:
        start, end = attack["startEpochMillis"], attack["endEpochMillis"]
        if end is None or not start < end <= midnight:
            found.append(f"attack {attack['id']} is in progress, empty or not over before {today}")
            continue
        if (attack["createdEpochMillis"], attack["updatedEpochMillis"]) != (start, end):
            found.append(f"attack {attack['id']} is not created at its start and updated at its end")
        if not all(start <= p["atEpochMillis"] <= end and 1 <= p["value"] <= 10 for p in attack["intensityPoints"]):
            found.append(f"attack {attack['id']} has an intensity point outside it or out of 1-10")
        if attack["functionalImpact"] not in (None, 0, 1, 2, 3):
            found.append(f"attack {attack['id']} has impact {attack['functionalImpact']}")
        if any(tag["key"] not in TAG_KEYS.get(tag["kind"], ()) for tag in attack["tags"]):
            found.append(f"attack {attack['id']} has a tag this app does not know")
        ZoneInfo(attack["startZoneId"])
    found += [f"attacks {a['id']} and {b['id']} overlap"
              for a, b in zip(attacks, attacks[1:]) if a["endEpochMillis"] > b["startEpochMillis"]]
    by_id = {attack["id"]: attack for attack in attacks}
    medications = {m["id"]: m for m in data["medications"]}
    for medication in data["medications"]:
        if medication["kind"] not in ("ACUTE", "PREVENTIVE") or medication["category"] not in (None, *CATEGORIES):
            found.append(f"medication {medication['name']} has kind or category this app does not know")
    for intake in data["intakes"]:
        medication, attack = medications.get(intake["medicationId"]), by_id.get(intake["attackId"])
        if medication is None or intake["attackId"] is not None and attack is None:
            found.append(f"intake {intake['id']} points at a record not in the file")
            continue
        if attack and not attack["startEpochMillis"] <= intake["takenEpochMillis"] <= attack["endEpochMillis"]:
            found.append(f"intake {intake['id']} is taken outside its attack")
        if not medication["createdEpochMillis"] <= intake["takenEpochMillis"] <= intake["createdEpochMillis"]:
            found.append(f"intake {intake['id']} is taken before its medication existed or logged before it was taken")
        if intake["effect"] not in (None, *EFFECTS):
            found.append(f"intake {intake['id']} has effect {intake['effect']}")
    found += [f"day mark {mark['date']} is neither a period nor pain-free"
              for mark in data["dayMarks"] if not (mark["period"] or mark["painFree"])]
    exported = datetime.strptime(data["exportedAt"], "%Y-%m-%dT%H:%M:%SZ").replace(tzinfo=timezone.utc)
    if exported.timestamp() * 1000 < latest_millis(data):
        found.append("exportedAt is before a record")
    return found


def run_backup_check(path, data):
    counts = [str(len(data[kind])) for kind in ("attacks", "medications", "dayMarks")]
    result = subprocess.run([sys.executable, "-I", str(Path(__file__).with_name("backup_files.py")), "json",
                             str(path), *counts], capture_output=True, text=True)
    return [] if result.returncode == 0 and result.stdout.startswith("JSON ok") else [result.stdout.strip()]


def check_seed(name, today, zone, folder):
    text = backup_text(name, today, zone)
    if text != backup_text(name, today, zone):
        return [f"{name} {today}: two runs wrote different files"]
    path = folder / f"{name}-{today}.json"
    path.write_text(text, encoding="utf-8")
    data = json.loads(path.read_text(encoding="utf-8"))
    failures = problems(data, today, zone) + run_backup_check(path, data)
    facts, _, spec = SEEDS[name][1](json.loads(text), today, zone)
    plain = recount(data, today, zone, facts)
    compared = 0
    for section, values in plain.items():
        for key, value in values.items():
            compared += 1
            if facts[section][key] != value:
                failures.append(f"{section} {key}: --list says {facts[section][key]!r}, counted again {value!r}")
    failures += [f"spec: {label} is {actual!r}, expected {expected!r}" for label, actual, expected in spec
                 if actual != expected]
    if not failures:
        print(f"ok {name} {today}: JSON ok, {compared} values agree when counted again, {len(spec)} spec values hold")
    return [f"{name} {today}: {failure}" for failure in failures]


def self_test():
    zone = DEFAULT_ZONE
    dates = list(dict.fromkeys(TEST_DATES + (datetime.now(ZoneInfo(zone)).date(),)))
    with tempfile.TemporaryDirectory() as folder:
        failures = [failure for today in dates for name in SEEDS
                    for failure in check_seed(name, today, zone, Path(folder))]
    for failure in failures:
        print(f"FAIL: {failure}")
    if failures:
        sys.exit(1)
    print(f"self-test ok: {len(SEEDS)} seeds on {len(dates)} dates in {zone}")


def known_zone(zone):
    try:
        ZoneInfo(zone)
    except (ZoneInfoNotFoundError, ValueError):
        fail(f"{zone!r} is not a time zone this computer knows")
    return zone


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    parser = argparse.ArgumentParser(usage=__doc__)
    parser.add_argument("seed", nargs="?")
    parser.add_argument("out", nargs="?")
    parser.add_argument("--today", type=date.fromisoformat)
    parser.add_argument("--zone", default=DEFAULT_ZONE)
    parser.add_argument("--list", action="store_true")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    zone = known_zone(args.zone)
    if args.self_test:
        self_test()
    elif args.list:
        list_seeds(args.today or datetime.now(ZoneInfo(zone)).date(), zone)
    elif args.seed in SEEDS and args.out and args.today:
        write_seed(args.seed, Path(args.out), args.today, zone)
    else:
        sys.exit(__doc__)


if __name__ == "__main__":
    main()
