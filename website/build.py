"""Builds migrely.health into website/public (BRD D119).

The site is static, fast and sends nothing anywhere: no scripts, no cookies, no analytics, and fonts and images are
served from the site itself, as Migrely's promise of privacy deserves. The privacy policy is generated from
docs/release/privacy-policy.md, so the store, the app and the site always say the same thing.

Run from the repo root: python3 website/build.py
It needs Pillow and the cwebp tool for the images.
"""
import html
import json
import re
import subprocess
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SITE = ROOT / 'website' / 'public'
ASSETS = SITE / 'assets'
STORE_ART = ROOT / 'docs' / 'release' / 'store'
BASE = 'https://migrely.health'
PLAY = 'https://play.google.com/store/apps/details?id=com.migrely.app'
SUPPORT = 'support@migrely.health'
PRIVACY_MAIL = 'privacy@migrely.health'
DISCLAIMER = ('Migrely is a diary. It is not a medical device and does not diagnose, treat or prevent any condition. '
              'Talk to a doctor about your headaches.')


def esc(text):
    return html.escape(text, quote=True)


def page(path, title, description, body, extra_head=''):
    canonical = f'{BASE}{path}'
    current = ' aria-current="page"'
    nav = ''.join(
        f'<a href="{href}"{current if href == path else ""}>{label}</a>'
        for href, label in [('/', 'Home'), ('/support/', 'Help'), ('/privacy/', 'Privacy')])
    return f'''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{esc(title)}</title>
<meta name="description" content="{esc(description)}">
<link rel="canonical" href="{canonical}">
<meta name="color-scheme" content="light dark">
<meta name="theme-color" content="#5B50D6">
<meta property="og:type" content="website">
<meta property="og:site_name" content="Migrely">
<meta property="og:title" content="{esc(title)}">
<meta property="og:description" content="{esc(description)}">
<meta property="og:url" content="{canonical}">
<meta property="og:image" content="{BASE}/assets/img/og.png">
<meta property="og:image:width" content="1200">
<meta property="og:image:height" content="630">
<meta name="twitter:card" content="summary_large_image">
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="icon" href="/favicon-32.png" sizes="32x32" type="image/png">
<link rel="apple-touch-icon" href="/apple-touch-icon.png">
<link rel="preload" href="/assets/fonts/fraunces-600.woff" as="font" type="font/woff" crossorigin>
<link rel="stylesheet" href="/assets/site.css">
{extra_head}
</head>
<body>
<a class="skip" href="#main">Skip to content</a>
<header class="top"><div class="wrap bar">
  <a class="brand" href="/"><img src="/favicon.svg" alt="" width="32" height="32"><span>Migrely</span></a>
  <nav aria-label="Main">{nav}</nav>
</div></header>
<main id="main">
{body}
</main>
<footer class="foot"><div class="wrap">
  <p>{esc(DISCLAIMER)}</p>
  <p class="links"><a href="/support/">Help</a><a href="/privacy/">Privacy policy</a><a href="/delete-data/">Delete your data</a>
    <a href="{PLAY}">Google Play</a></p>
  <p>© 2026 Migrely, published by Sulton UzDev · <a href="mailto:{SUPPORT}">{SUPPORT}</a></p>
</div></footer>
</body>
</html>
'''


def play_button(label='Get it on Google Play'):
    return (f'<a class="play" href="{PLAY}"><svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">'
            '<path fill="currentColor" d="M4 2.6v18.8c0 .5.5.8.9.6l16.3-9.4c.4-.2.4-.9 0-1.1L4.9 2.1c-.4-.2-.9.1-.9.5z"/>'
            f'</svg><span>{label}</span></a>')


FEATURES = [
    ('Log in seconds', 'One tap when a migraine starts, even in a dark room. Migrely keeps the time while you rest, '
     'and you can add the details later.'),
    ('Your month as a calm sky', 'Every day is drawn from what you logged: clear, hazy, cloudy or a storm. Patterns '
     'show at a glance, with period and pain-free days marked.'),
    ('Watch your medication days', 'A gentle note when painkillers or triptans reach the days a month that can make '
     'headaches more frequent, so you can talk to your doctor in time.'),
    ('A report for your doctor', 'A clear PDF with a summary, a calendar and every migraine and medication, ready to '
     'share at your appointment.'),
    ('See what your migraines share', 'Triggers often logged with your migraines, the days and hours they come, the '
     'days around your period and what helped.'),
    ('Gentle on your eyes', 'Dark by default, light if you like, and always dark during a migraine. Large text, '
     'TalkBack and keyboards work throughout.'),
]

FAQ = [
    ('Does Migrely send my data anywhere?',
     'No. Migrely has no internet access at all, so nothing you enter can leave your phone through the app. There is '
     'no account, no tracking and no ads.'),
    ('Is Migrely free?',
     'Logging, your whole history, medications, reminders, export and every safety warning are free. Premium adds the '
     'doctor report and Insights. The yearly plan starts with a 7-day free trial; there is also a monthly plan and a '
     'one-time lifetime purchase.'),
    ('How do I move my diary to a new phone?',
     'Android\'s own backup brings it to a new phone signed in to the same Google account. You can also export a '
     'backup file in Settings, Your data, and import it on the new phone.'),
    ('How do I restore Premium on a new phone?',
     'Premium belongs to your Google account. Install Migrely with the same account and it is found at the first '
     'start; Settings, Premium, Restore purchases checks again on demand.'),
    ('How do I cancel my subscription?',
     'In Google Play: Profile, Payments and subscriptions, Subscriptions, Migrely Premium, Cancel. Cancel before a '
     'free trial ends and you are not charged. Your entries are never deleted.'),
    ('How do I delete everything?',
     'In Migrely: Settings, Delete all data. Type DELETE to confirm. Everything Migrely keeps on your phone is removed '
     'at once.'),
    ('Is Migrely a medical device?',
     'No. Migrely is a diary that helps you keep a record and talk to your doctor. It does not diagnose or treat. If a '
     'headache comes with any warning sign, such as the worst pain of your life or weakness that does not pass, call '
     'your local emergency number.'),
]


def home():
    shots = ''.join(
        f'<figure><img src="/assets/img/{name}.webp" alt="{esc(alt)}" width="360" height="640" loading="lazy"></figure>'
        for name, alt in [('01-log', 'Attack mode: Rest now, we will keep time'),
                          ('02-sky', 'Your month as a calm night sky'),
                          ('03-report', 'A clear report for your doctor'),
                          ('04-insights', 'Triggers and patterns in your migraines')])
    features = ''.join(f'<article><h3>{esc(t)}</h3><p>{esc(d)}</p></article>' for t, d in FEATURES)
    body = f'''
<section class="hero"><div class="wrap hero-grid">
  <div class="hero-text">
    <p class="eyebrow">Migraine tracker for Android</p>
    <h1>A calm, private migraine diary</h1>
    <p class="lead">Log a migraine in seconds, see your months as a quiet night sky, and bring a clear report to your
      doctor. No account, no internet, no ads.</p>
    <div class="cta">{play_button()}<a class="quiet" href="/privacy/">How your data stays private</a></div>
  </div>
  <img class="hero-icon" src="/assets/img/icon-512.png" alt="The Migrely icon: a white calendar with a crescent moon on violet" width="256" height="256">
</div></section>

<section class="shots" aria-label="Screens from Migrely"><div class="wrap strip">{shots}</div></section>

<section class="wrap block"><h2>Everything a migraine diary should be</h2><div class="features">{features}</div></section>

<section class="band"><div class="wrap block">
  <h2>Private by design</h2>
  <ul class="ticks">
    <li>Migrely has no internet access. Nothing you enter can leave your phone through the app.</li>
    <li>No account to make, no tracking, no ads.</li>
    <li>Your entries stay on your phone, and in Android's own backup, end-to-end encrypted on Android 9 and later.</li>
    <li>Export a backup or a spreadsheet, or delete everything, whenever you like.</li>
  </ul>
</div></section>

<section class="wrap block two">
  <div><h2>Free, and Premium when you want more</h2>
    <p>Logging, your whole history, medications, reminders, export and every safety warning are free, for good.
      Premium adds the doctor report and Insights, with a 7-day free trial on the yearly plan.</p></div>
  <div><h2>Safety first</h2>
    <p>The warning signs that need urgent care are always in Settings. Migrely is a diary, not a medical device: it
      helps you keep a record and talk to your doctor.</p></div>
</section>

<section class="wrap block final"><h2>Start your diary today</h2>{play_button()}</section>
'''
    ld = {'@context': 'https://schema.org', '@type': 'MobileApplication', 'name': 'Migrely: Migraine Tracker',
          'operatingSystem': 'Android', 'applicationCategory': 'HealthApplication', 'url': f'{BASE}/',
          'downloadUrl': PLAY, 'image': f'{BASE}/assets/img/icon-512.png',
          'description': 'A calm, private migraine diary: log in seconds, see your patterns, and share a report with '
                         'your doctor.',
          'offers': {'@type': 'Offer', 'price': '0', 'priceCurrency': 'USD'},
          'publisher': {'@type': 'Organization', 'name': 'Sulton UzDev'}}
    extra = f'<script type="application/ld+json">{json.dumps(ld)}</script>'
    return page('/', 'Migrely: Migraine Tracker & Private Headache Diary',
                'Log migraines in seconds, see your patterns and share a doctor-ready PDF. A private migraine diary '
                'for Android: no account, no internet, no ads.', body, extra)


def support():
    items = ''.join(f'<details><summary>{esc(q)}</summary><p>{esc(a)}</p></details>' for q, a in FAQ)
    body = f'''<section class="wrap block narrow"><h1>Help</h1>
  <p class="lead">Answers to common questions. Anything else: write to <a href="mailto:{SUPPORT}">{SUPPORT}</a>.</p>
  <div class="faq">{items}</div></section>'''
    ld = {'@context': 'https://schema.org', '@type': 'FAQPage', 'mainEntity': [
        {'@type': 'Question', 'name': q, 'acceptedAnswer': {'@type': 'Answer', 'text': a}} for q, a in FAQ]}
    return page('/support/', 'Help · Migrely', 'Answers about Migrely: privacy, backups, Premium, cancelling and '
                'deleting your data.', body, f'<script type="application/ld+json">{json.dumps(ld)}</script>')


def delete_data():
    body = f'''<section class="wrap block narrow"><h1>Delete your data</h1>
  <p class="lead">Migrely keeps your entries only on your phone, so you delete them on your phone. We hold nothing
    about you that we could delete for you.</p>
  <h2>In the app</h2>
  <ol><li>Open Migrely, then Settings.</li><li>Choose Delete all data at the end of the list.</li>
    <li>Type DELETE and confirm. Every entry, setting and copy Migrely keeps on the phone is removed, and the app starts
      fresh.</li></ol>
  <h2>Android's own backup</h2>
  <p>Android's backup of Migrely is replaced the next time your phone backs up, usually within a day. You can also
    delete your phone's backup in your Google account's backup settings.</p>
  <h2>Premium</h2>
  <p>Deleting your data does not cancel a subscription. Cancel it in Google Play: Profile, Payments and subscriptions,
    Subscriptions.</p>
  <p>Questions: <a href="mailto:{PRIVACY_MAIL}">{PRIVACY_MAIL}</a></p></section>'''
    return page('/delete-data/', 'Delete your data · Migrely', 'How to delete everything Migrely keeps on your phone.',
                body)


def privacy():
    source = (ROOT / 'docs/release/privacy-policy.md').read_text()
    out, para, item = [], [], None

    def inline(text):
        text = html.escape(text, quote=False)
        text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
        return re.sub(r'(?<![\w@/])([\w.+-]+@[\w-]+(?:\.[\w-]+)+)', r'<a href="mailto:\1">\1</a>', text)

    def close():
        nonlocal item
        if item is not None:
            out.append('<li>' + inline(item) + '</li></ul>')
            item = None
        if para:
            out.append('<p>' + inline(' '.join(para)) + '</p>')
            para.clear()

    for line in source.split('\n'):
        if line.startswith('#'):
            close()
            level = 1 if line.startswith('# ') else 2
            out.append(f'<h{level}>{inline(line.lstrip("# "))}</h{level}>')
        elif line.startswith('- '):
            if para:
                out.append('<p>' + inline(' '.join(para)) + '</p>')
                para.clear()
            if item is None:
                out.append('<ul>')
            else:
                out.append('<li>' + inline(item) + '</li>')
            item = line[2:]
        elif line.startswith('  ') and item is not None:
            item += ' ' + line.strip()
        elif not line.strip():
            close()
        else:
            if item is not None:
                out.append('<li>' + inline(item) + '</li></ul>')
                item = None
            para.append(line.strip())
    close()
    body = '<section class="wrap block narrow prose">' + '\n'.join(out) + '</section>'
    return page('/privacy/', 'Privacy policy · Migrely', 'Migrely has no internet access and no account. What it '
                'keeps on your phone, Android\'s backup, and how to delete everything.', body)


def not_found():
    body = '''<section class="wrap block narrow"><h1>Page not found</h1>
  <p class="lead">The page you were looking for is not here. <a href="/">Go to the home page</a>.</p></section>'''
    return page('/404.html', 'Page not found · Migrely', 'This page does not exist.', body)


def images():
    img = ASSETS / 'img'
    img.mkdir(parents=True, exist_ok=True)
    for name in ['01-log', '02-sky', '03-report', '04-insights']:
        src = STORE_ART / 'screenshots' / f'{name}.png'
        tmp = img / f'{name}.png'
        Image.open(src).convert('RGB').resize((720, 1280), Image.LANCZOS).save(tmp)
        subprocess.run(['cwebp', '-quiet', '-q', '80', str(tmp), '-o', str(img / f'{name}.webp')], check=True)
        tmp.unlink()
    icon = Image.open(ROOT / 'docs/release/icon/migrely-play-icon-512.png').convert('RGBA')
    icon.save(img / 'icon-512.png', optimize=True)
    icon.resize((180, 180), Image.LANCZOS).save(SITE / 'apple-touch-icon.png', optimize=True)
    icon.resize((32, 32), Image.LANCZOS).save(SITE / 'favicon-32.png', optimize=True)
    svg = (ROOT / 'docs/release/icon/migrely-play-icon.svg').read_text()
    (SITE / 'favicon.svg').write_text(svg.replace('width="512" height="512"', 'width="32" height="32"'))
    # The link preview: the feature graphic centred on violet at 1200 × 630.
    feature = Image.open(STORE_ART / 'feature-graphic-1024x500.png').convert('RGB')
    og = Image.new('RGB', (1200, 630), (91, 80, 214))
    og.paste(feature, ((1200 - feature.width) // 2, (630 - feature.height) // 2))
    og.save(img / 'og.png', optimize=True)


def main():
    pages = {'index.html': home(), 'support/index.html': support(), 'privacy/index.html': privacy(),
             'delete-data/index.html': delete_data(), '404.html': not_found()}
    for rel, content in pages.items():
        target = SITE / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content)
    images()
    urls = ['/', '/support/', '/privacy/', '/delete-data/']
    (SITE / 'sitemap.xml').write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + ''.join(f'  <url><loc>{BASE}{u}</loc></url>\n' for u in urls) + '</urlset>\n')
    (SITE / 'robots.txt').write_text(f'User-agent: *\nAllow: /\nSitemap: {BASE}/sitemap.xml\n')
    print('built', ', '.join(pages))


if __name__ == '__main__':
    main()
