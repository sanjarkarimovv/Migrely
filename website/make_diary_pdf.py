"""Prints the 8-week headache diary template to PDF, A4 and US Letter (BRD D139, D140).

Run from the repo root when the template or DIARY_REVIEWED changes, then commit the PDFs:
python3 website/make_diary_pdf.py
It needs Google Chrome, at its macOS path unless CHROME names another. The site build only checks that the PDFs
exist, so it needs no Chrome itself.
"""
import html
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

from build import DIARY_PDFS, DIARY_REVIEWED

ROOT = Path(__file__).resolve().parents[1]
FONTS = ROOT / 'website' / 'public' / 'assets' / 'fonts'
OUT = ROOT / 'website' / 'public' / 'headache-diary'
CHROME = os.environ.get('CHROME', '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome')
SIZES = {'a4': 'A4 landscape', 'letter': 'letter landscape'}
PAGES = 6
FOOTER = f'Free from migrely.health · A diary, not a medical device · Reviewed {DIARY_REVIEWED[1]}'

# The daily log's columns: heading, hint under it, width in percent.
COLUMNS = [
    ('Date', '', 7), ('Headache?', 'Y / N', 7), ('Started', 'time', 6), ('Ended', 'time', 6),
    ('Worst pain', '0–10', 6), ('Symptoms', 'N L S A', 7), ('Medicine taken', 'name, dose, time', 17),
    ('Kind', 'T C P O', 5), ('Helped?', 'Y / S / N', 6), ('Possible triggers', '', 14), ('Period', 'tick', 5),
    ('Notes', '', 14),
]
# What the doctor sees per 4 weeks: only counts the reader can make, no self-diagnosis (review I5).
SUMMARY = ['Headache days', 'Days with N, L, S or A', 'Days with P', 'Days with T', 'Days with C', 'Days with O',
           'Days with any medicine for the attack', 'Worst pain']

STYLE = '''
@font-face { font-family: Fraunces; src: url("{fonts}/fraunces-600.woff"); font-weight: 600; }
@font-face { font-family: Figtree; src: url("{fonts}/figtree-400.woff"); font-weight: 400; }
@font-face { font-family: Figtree; src: url("{fonts}/figtree-600.woff"); font-weight: 600; }
@page { size: {size}; margin: 10mm; }
* { box-sizing: border-box; }
html, body { margin: 0; color: #000; font: 400 9.5pt/1.35 Figtree, Arial, sans-serif; }
.page { height: calc(100vh - 0.1mm); display: flex; flex-direction: column; break-after: page; }
.page:last-child { break-after: auto; }
h1 { font: 600 22pt/1.1 Fraunces, Georgia, serif; margin: 0 0 2mm; }
h2 { font: 600 12pt/1.2 Fraunces, Georgia, serif; margin: 0 0 1.5mm; }
p { margin: 0 0 1.5mm; }
.top { display: flex; justify-content: space-between; align-items: baseline; gap: 8mm; margin-bottom: 3mm; }
.fill { display: flex; gap: 8mm; font-size: 9pt; }
.fill span { display: inline-block; min-width: 45mm; border-bottom: 0.75pt solid #888; }
.cols { display: grid; grid-template-columns: 1fr 1fr; gap: 8mm; flex: 1; }
ol, ul { margin: 0 0 2mm; padding-left: 5mm; }
li { margin-bottom: 0.8mm; }
.box { border: 1.2pt solid #000; border-radius: 2mm; padding: 2.5mm 4mm; margin-top: 1.5mm; }
.box ul { margin-bottom: 1mm; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th, td { border: 0.75pt solid #888; padding: 1mm 1.5mm; vertical-align: top; }
th { font-weight: 600; font-size: 8.5pt; text-align: left; line-height: 1.15; }
th small { display: block; font-weight: 400; color: #333; font-size: 7.5pt; }
.log { flex: 1; }
.log tbody tr { height: calc((100vh - 46mm) / 14); }
.summary td { height: 12mm; }
.lines div { border-bottom: 0.75pt solid #888; height: 8mm; }
.foot { display: flex; justify-content: space-between; font-size: 8pt; color: #333; margin-top: 2mm; }
.key td, .key th { border: 0; padding: 0.5mm 2mm 0.5mm 0; }
.key th { width: 26%; font-size: 9pt; }
.intro { font-size: 9.5pt; }
'''


def foot(n):
    return f'<div class="foot"><span>{html.escape(FOOTER)}</span><span>Page {n} of {PAGES}</span></div>'


def how_to():
    # The warning signs are the NHS migraine page's, unsoftened; numbness and a stiff neck alone are ordinary migraine
    # symptoms there, so they are not listed as emergencies (review C1).
    return f'''<section class="page">
<div class="top"><h1>Headache diary · 8 weeks</h1>
  <div class="fill">Name <span></span> Start date <span></span></div></div>
<div class="cols intro"><div>
  <h2>How to use it</h2>
  <ol><li>Fill in one row every day. On a day without headache, write N under Headache? and leave the rest blank:
      pain-free days count too.</li>
    <li>Note when a headache started and ended, and the worst pain from 0 (none) to 10 (worst imaginable). If it goes
      on past midnight, write "cont." under Started on the next day's row.</li>
    <li>Write every painkiller or headache medicine you take, prescribed or not, with the dose and time, and whether it
      helped. Include painkillers taken for other pain, such as period pain.</li>
    <li>After each 4 weeks, fill in the summary on page 6. Bring the diary to your appointment. For longer, print
      another copy.</li></ol>
  <p>UK guidance (NICE CG150) suggests keeping a headache diary for at least 8 weeks: how often, how long and how bad;
    other symptoms; all medicines; possible triggers; and any link to your period.</p>
  <div class="box"><h2>Don't wait: call your emergency number</h2>
    <p>if you have a headache and:</p>
    <ul><li>it came on suddenly and is extremely painful;</li>
      <li>you cannot move, or have weakness in, an arm or leg on one side, or one side of your face;</li>
      <li>you have problems speaking or remembering things, loss of vision, or blurred or double vision;</li>
      <li>you feel drowsy or confused, or have a seizure;</li>
      <li>you have a very high temperature with a stiff neck, or a rash that does not fade under a glass;</li>
      <li>you have recently had a head injury.</li></ul>
    <p>See a doctor the same day (in the UK, NHS 111) if an attack lasts more than 72 hours, aura lasts more than
      1 hour, or you are pregnant or have just had a baby.</p></div>
</div><div>
  <h2>Key</h2>
  <table class="key"><tbody>
    <tr><th>Symptoms</th><td>N nausea or vomiting · L light bothers you · S sound bothers you<br>A aura: warning signs
      such as zigzag lines, flashing lights or pins and needles, usually less than 1 hour</td></tr>
    <tr><th>Kind of medicine</th><td>T triptan<br>C combination painkiller, such as one with caffeine or codeine<br>
      P simple painkiller: paracetamol/acetaminophen, aspirin, ibuprofen or another NSAID<br>
      O other medicine for the attack: opioid, ergot or another (write its name)</td></tr>
    <tr><th>Helped?</th><td>Y yes · S somewhat · N no</td></tr>
    <tr><th>Period</th><td>tick days you have your period</td></tr></tbody></table>
  <h2>Why count medicine days?</h2>
  <p>Taking medicine for attacks on many days a month can make headaches more frequent. The international
    classification (ICHD-3) describes medication-overuse headache in people with headache on 15 or more days a month
    who, for more than 3 months, have taken: triptans, ergots, opioids or combination painkillers on 10 or more days a
    month; a mix of these kinds, or of these and simple painkillers, on 10 or more days in total; or simple painkillers
    alone (one or several) on 15 or more days.</p>
  <p>These are diagnostic thresholds, not safe limits: the NHS advises taking painkillers on no more than 2 days a
    week. Talk to your doctor, and don't stop or change medicines on your own.</p>
</div></div>
{foot(1)}</section>'''


def log(n):
    head = ''.join(f'<th style="width:{w}%">{html.escape(t)}<small>{html.escape(h)}</small></th>'
                   for t, h, w in COLUMNS)
    rows = ''.join('<tr>' + '<td></td>' * len(COLUMNS) + '</tr>' for _ in range(14))
    weeks = f'Weeks {2 * n - 1}–{2 * n}'
    return f'''<section class="page"><div class="top"><h2>{weeks}</h2><div class="fill">From <span></span></div></div>
<table class="log"><thead><tr>{head}</tr></thead><tbody>{rows}</tbody></table>
{foot(n + 1)}</section>'''


def summary():
    head = '<th style="width:13%"></th>' + ''.join(f'<th>{html.escape(t)}</th>' for t in SUMMARY)
    rows = ''.join(f'<tr><th>{w}</th>' + '<td></td>' * len(SUMMARY) + '</tr>' for w in ['Weeks 1–4', 'Weeks 5–8'])
    lines = '<div class="lines">' + '<div></div>' * 6 + '</div>'
    return f'''<section class="page"><h1>Summary for your doctor</h1>
<p>Count days, not doses: a day with two doses counts once, and a day with T and P counts under both, and once under
  any medicine for the attack.</p>
<table class="summary"><thead><tr>{head}</tr></thead><tbody>{rows}</tbody></table>
<div class="cols" style="margin-top:6mm"><div><h2>Medicines I take to prevent headaches</h2>{lines}</div>
<div><h2>Questions for my doctor</h2>{lines}</div></div>
{foot(PAGES)}</section>'''


def document(size):
    pages = [how_to()] + [log(n) for n in range(1, 5)] + [summary()]
    style = STYLE.replace('{fonts}', FONTS.as_uri()).replace('{size}', size)
    return (f'<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Headache diary · 8 weeks · '
            f'Migrely</title><style>{style}</style></head><body>{"".join(pages)}</body></html>')


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        for name, size in SIZES.items():
            source = Path(tmp) / f'{name}.html'
            source.write_text(document(size))
            target = OUT / DIARY_PDFS[name]
            run = subprocess.run([CHROME, '--headless=new', '--disable-gpu', '--no-first-run',
                                  f'--user-data-dir={tmp}/p', '--no-pdf-header-footer', f'--print-to-pdf={target}',
                                  source.as_uri()], capture_output=True, text=True)
            if run.returncode != 0:
                sys.exit(f'Chrome failed for {name}:\n{run.stderr}')
            # A page that overflows would print as an extra page.
            pages = len(re.findall(rb'/Type\s*/Page(?!s)', target.read_bytes()))
            if pages != PAGES:
                sys.exit(f'{target.name} has {pages} pages, not {PAGES}.')
            print('wrote', target.relative_to(ROOT))


if __name__ == '__main__':
    main()
