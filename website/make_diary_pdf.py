"""Prints the 8-week headache diary template to PDF, A4 and US Letter (BRD D139, D140).

Run from the repo root when the template changes, then commit the PDFs: python3 website/make_diary_pdf.py
It needs Google Chrome. The site build only checks that the PDFs exist, so it needs no Chrome itself.
"""
import html
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FONTS = ROOT / 'website' / 'public' / 'assets' / 'fonts'
OUT = ROOT / 'website' / 'public' / 'headache-diary'
CHROME = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
SIZES = {'a4': 'A4 landscape', 'letter': 'letter landscape'}
PAGES = 6
FOOTER = 'Free from migrely.health · A diary, not a medical device'

# The daily log's columns: heading, hint under it, width in percent.
COLUMNS = [
    ('Date', '', 7), ('Headache?', 'Y / N', 7), ('Started', 'time', 6), ('Ended', 'time', 6),
    ('Worst pain', '0–10', 6), ('Symptoms', 'N L S A', 7), ('Medicine taken', 'name, dose, time', 17),
    ('Kind', 'T C P O', 5), ('Helped?', 'Y / S / N', 6), ('Possible triggers', '', 14), ('Period', '✓', 5),
    ('Notes', '', 14),
]
SUMMARY = ['Headache days', 'Migraine days (if known)', 'Days with simple painkillers (P)', 'Days with triptans (T)',
           'Days with combination or other (C, O)', 'Days with any acute medicine', 'Worst pain']

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
h2 { font: 600 13pt/1.2 Fraunces, Georgia, serif; margin: 0 0 2mm; }
p { margin: 0 0 2mm; }
.top { display: flex; justify-content: space-between; align-items: baseline; gap: 8mm; margin-bottom: 3mm; }
.fill { display: flex; gap: 8mm; font-size: 9pt; }
.fill span { display: inline-block; min-width: 45mm; border-bottom: 0.75pt solid #888; }
.cols { display: grid; grid-template-columns: 1fr 1fr; gap: 8mm; flex: 1; }
ol, ul { margin: 0 0 3mm; padding-left: 5mm; }
li { margin-bottom: 1.2mm; }
.box { border: 1.2pt solid #000; border-radius: 2mm; padding: 3mm 4mm; margin-top: 2mm; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th, td { border: 0.75pt solid #888; padding: 1mm 1.5mm; vertical-align: top; }
th { font-weight: 600; font-size: 8.5pt; text-align: left; line-height: 1.15; }
th small { display: block; font-weight: 400; color: #333; font-size: 7.5pt; }
.log { flex: 1; }
.log tbody tr { height: calc((100vh - 46mm) / 14); }
.summary td { height: 12mm; }
.lines div { border-bottom: 0.75pt solid #888; height: 8mm; }
.foot { display: flex; justify-content: space-between; font-size: 8pt; color: #333; margin-top: 2mm; }
.key td, .key th { border: 0; padding: 0.6mm 2mm 0.6mm 0; }
.key th { width: 30%; font-size: 9.5pt; }
.intro { font-size: 10.5pt; }
'''


def foot(n):
    return f'<div class="foot"><span>{html.escape(FOOTER)}</span><span>Page {n} of {PAGES}</span></div>'


def how_to():
    return f'''<section class="page">
<div class="top"><h1>Headache diary · 8 weeks</h1>
  <div class="fill">Name <span></span> Start date <span></span></div></div>
<div class="cols intro"><div>
  <h2>How to use it</h2>
  <ol><li>Fill in one row every day, including days without headache: write N. Pain-free days count too.</li>
    <li>Note when a headache started and ended, and the worst pain from 0 (none) to 10 (worst imaginable).</li>
    <li>Write every medicine you take for headache, prescribed or not, with the dose and time, and whether it
      helped.</li>
    <li>After each 4 weeks, fill in the summary on page 6. Bring the diary to your appointment.</li></ol>
  <p>UK guidance (NICE CG150) suggests keeping a headache diary for at least 8 weeks: how often, how long and how bad;
    other symptoms; all medicines; possible triggers; and any link to your period.</p>
  <div class="box"><h2>Don't wait: call your emergency number</h2>
    <p>for a sudden, extremely painful headache, or a headache with weakness or numbness on one side, trouble speaking,
      loss of vision, confusion, a seizure, or a high temperature or stiff neck.</p></div>
</div><div>
  <h2>Key</h2>
  <table class="key"><tbody>
    <tr><th>Symptoms</th><td>N nausea or vomiting · L light bothers you · S sound bothers you · A aura</td></tr>
    <tr><th>Kind of medicine</th><td>T triptan<br>C combination painkiller, such as one with caffeine or codeine<br>
      P simple painkiller: paracetamol/acetaminophen, aspirin, ibuprofen or another NSAID<br>
      O other: opioid, ergot or anything else</td></tr>
    <tr><th>Helped?</th><td>Y yes · S somewhat · N no</td></tr>
    <tr><th>Period</th><td>tick days you have your period</td></tr></tbody></table>
  <h2>Why count medicine days?</h2>
  <p>Taking acute medicine on many days a month can make headaches more frequent. The international classification
    (ICHD-3) counts 10 or more days a month for triptans, ergots, opioids or combination painkillers, or for any mix
    of acute medicines (simple painkillers included), and 15 or more for simple painkillers alone. These are not safe limits: talk to your doctor, and don't stop or
    change medicines on your own.</p>
</div></div>
{foot(1)}</section>'''


def log(n):
    head = ''.join(f'<th style="width:{w}%">{html.escape(t)}<small>{html.escape(h)}</small></th>' for t, h, w in COLUMNS)
    rows = ''.join('<tr>' + '<td></td>' * len(COLUMNS) + '</tr>' for _ in range(14))
    weeks = f'Weeks {2 * n - 1}–{2 * n}'
    return f'''<section class="page"><div class="top"><h2>{weeks}</h2><div class="fill">From <span></span></div></div>
<table class="log"><thead><tr>{head}</tr></thead><tbody>{rows}</tbody></table>
{foot(n + 1)}</section>'''


def summary():
    head = '<th style="width:16%"></th>' + ''.join(f'<th>{html.escape(t)}</th>' for t in SUMMARY)
    rows = ''.join(f'<tr><th>{w}</th>' + '<td></td>' * len(SUMMARY) + '</tr>' for w in ['Weeks 1–4', 'Weeks 5–8'])
    lines = '<div class="lines">' + '<div></div>' * 6 + '</div>'
    return f'''<section class="page"><h1>Summary for your doctor</h1>
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
            target = OUT / f'migrely-headache-diary-{name}.pdf'
            subprocess.run([CHROME, '--headless=new', '--disable-gpu', '--no-first-run', f'--user-data-dir={tmp}/p',
                            '--no-pdf-header-footer', f'--print-to-pdf={target}', source.as_uri()],
                           check=True, capture_output=True)
            print('wrote', target.relative_to(ROOT))


if __name__ == '__main__':
    main()
