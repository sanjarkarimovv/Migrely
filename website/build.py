"""Builds migrely.health into website/public (BRD D119).

The site is static, fast and sends nothing anywhere: no scripts, no cookies, no analytics, and fonts and images are
served from the site itself, as Migrely's promise of privacy deserves. The privacy policy is generated from
docs/release/privacy-policy.md, so the store, the app and the site always say the same thing.

Run from the repo root: python3 website/build.py
It needs Pillow and the cwebp tool for the images. It checks /privacy-check/ against the release build's permission
report, so run ./gradlew :app:checkReleasePermissions first (BRD EC-WEB-01). It updates website/lastmod.json, which is
committed with the site (BRD D136).
"""
import hashlib
import html
import json
import re
import subprocess
from datetime import date
from pathlib import Path
from urllib.parse import quote

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SITE = ROOT / 'website' / 'public'
ASSETS = SITE / 'assets'
STORE_ART = ROOT / 'docs' / 'release' / 'store'
LASTMOD = ROOT / 'website' / 'lastmod.json'
PERMISSION_REPORT = ROOT / 'app' / 'build' / 'reports' / 'permissions' / 'release.txt'
# _headers caches /assets/ for a year, so the stylesheet's address changes whenever it does.
CSS_VERSION = hashlib.sha256((ASSETS / 'site.css').read_bytes()).hexdigest()[:10]
BASE = 'https://migrely.health'
PLAY = 'https://play.google.com/store/apps/details?id=com.migrely.app'
SUPPORT = 'support@migrely.health'
PRIVACY_MAIL = 'privacy@migrely.health'
DISCLAIMER = ('Migrely is a diary. It is not a medical device and does not diagnose, treat or prevent any condition. '
              'Talk to a doctor about your headaches.')
# /headache-diary/ (BRD D138): the date its facts were last checked against the sources, as ISO and as text.
DIARY_REVIEWED = ('2026-10-29', '29 October 2026')
# The printable template, made by website/make_diary_pdf.py (BRD D140).
DIARY_PDFS = {'a4': 'migrely-headache-diary-a4.pdf', 'letter': 'migrely-headache-diary-letter.pdf'}
DIARY_TITLE = 'Headache diary: what to write down, with a free printable template'
ICHD_MOH = ('https://ichd-3.org/8-headache-attributed-to-a-substance-or-its-withdrawal/'
            '8-2-medication-overuse-headache-moh/')
NHS_MIGRAINE = 'https://www.nhs.uk/conditions/migraine/'
MOH_REVIEWED = ('2026-10-29', '29 October 2026')
MOH_TITLE = 'Medication-overuse headache: the 10- and 15-day rules'
NICE_CG150 = 'https://www.nice.org.uk/guidance/cg150/chapter/Recommendations'
DIARY_SOURCES = [
    ('NICE CG150, Headaches in over 12s: recommendations 1.1.3, 1.1.4, 1.2.6, 1.2.7 and 1.3.1',
     NICE_CG150),
    ('ICHD-3, Definition of terms: headache days', 'https://ichd-3.org/definition-of-terms/'),
    ('ICHD-3, 1.3 Chronic migraine', 'https://ichd-3.org/1-migraine/1-3-chronic-migraine/'),
    ('ICHD-3, 8.2 Medication-overuse headache', ICHD_MOH),
    ('NHS, Migraine', NHS_MIGRAINE),
    ('The Migraine Trust, Keeping a headache diary',
     'https://migrainetrust.org/live-with-migraine/self-management/keeping-a-migraine-diary/'),
    ('The Migraine Trust, Menstrual migraine',
     'https://migrainetrust.org/understand-migraine/types-of-migraine/menstrual-migraine/'),
]
# One object for the home page and /about/ (BRD D135). sameAs comes with the social profiles (EC-WEB-06).
ORGANIZATION = {'@type': 'Organization', '@id': f'{BASE}/#organization', 'name': 'Migrely', 'url': f'{BASE}/',
                'logo': f'{BASE}/assets/img/icon-512.png', 'email': SUPPORT,
                'contactPoint': {'@type': 'ContactPoint', 'contactType': 'customer support', 'email': SUPPORT}}
# Every permission the release build asks for, and what Migrely uses it for (BRD D134). Titles in quotes are the
# names Android 16 shows in Settings → Apps → Migrely → Permissions → All permissions; the emulator, without Google
# Play, did not list the other two. check_permissions() keeps this list equal to the release build's.
PERMISSIONS = [
    ('“show notifications”', 'android.permission.POST_NOTIFICATIONS',
     'Reminders and the “Did it help?” question. On Android 13 and later, Android asks you first. On any version '
     'you can turn notifications off in Settings.'),
    ('“run at startup”', 'android.permission.RECEIVE_BOOT_COMPLETED',
     'Puts your reminders back after the phone restarts. It comes with WorkManager, the Android library Migrely '
     'uses to schedule reminders.'),
    ('“prevent phone from sleeping”', 'android.permission.WAKE_LOCK',
     'Lets WorkManager keep the phone awake for the moments it takes to run a reminder.'),
    ('Google Play billing', 'com.android.vending.BILLING',
     'Lets Migrely ask the Google Play app on your phone whether Premium is on, and open Google Play\'s purchase '
     'screen. Migrely talks only to the Google Play app, never to the internet; Google Play itself goes online to '
     'complete a purchase.'),
    ('An internal AndroidX permission', 'com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION',
     'AndroidX, Google\'s standard library for Android apps, adds it so that on older Android versions only Migrely '
     'can send messages to its own parts. Settings does not list it.'),
]
NUMBER_WORDS = ['no', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine']


def esc(text):
    return html.escape(text, quote=True)


def in_words(number):
    return NUMBER_WORDS[number] if number < len(NUMBER_WORDS) else str(number)


def breakable(name):
    """A permission name that wraps after its dots and underscores instead of inside a word."""
    return re.sub(r'([._])', r'\1<wbr>', esc(name))


def json_ld(data):
    # '</' would end the script element early; JSON may write it as '<\/'.
    text = json.dumps(data).replace('</', '<\\/')
    return f'<script type="application/ld+json">{text}</script>'


def campaign(path):
    """The page a Play link sits on, as its UTM campaign: home, or the path's name (BRD D137)."""
    return path.strip('/').removesuffix('.html') or 'home'


def play_link(page_campaign):
    """The Play listing with the site's UTM tags in referrer, the Play URL builder's format (BRD D137). Play Console
    counts the visits; nothing here tracks the visitor."""
    tags = f'utm_source=migrely.health&utm_medium=website&utm_campaign={page_campaign}'
    return f'{PLAY}&referrer={quote(tags, safe="")}'


def page(path, title, description, body, extra_head=''):
    canonical = f'{BASE}{path}'
    current = ' aria-current="page"'
    nav = ''.join(
        f'<a href="{href}"{current if href == path else ""}>{label}</a>'
        for href, label in [('/', 'Home'), ('/about/', 'About'), ('/support/', 'Help'), ('/privacy/', 'Privacy')])
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
<link rel="stylesheet" href="/assets/site.css?v={CSS_VERSION}">
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
  <p class="links"><a href="/support/">Help</a><a href="/about/">About</a><a href="/privacy/">Privacy policy</a>
    <a href="/privacy-check/">Check our privacy</a><a href="/headache-diary/">Free headache diary</a>
    <a href="/delete-data/">Delete your data</a><a href="{esc(play_link(campaign(path)))}">Google Play</a></p>
  <p>© 2026 Migrely, published by Sulton UzDev · <a href="mailto:{SUPPORT}">{SUPPORT}</a></p>
</div></footer>
</body>
</html>
'''


def play_button(path, label='Get it on Google Play'):
    return (f'<a class="play" href="{esc(play_link(campaign(path)))}">'
            '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">'
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
    <div class="cta">{play_button('/')}<a class="quiet" href="/privacy/">How your data stays private</a></div>
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
  <p><a class="quiet" href="/privacy-check/">Check it yourself in two minutes</a></p>
</div></section>

<section class="wrap block two">
  <div><h2>Free, and Premium when you want more</h2>
    <p>Logging, your whole history, medications, reminders, export and every safety warning are free, for good.
      Premium adds the doctor report and Insights, with a 7-day free trial on the yearly plan.</p></div>
  <div><h2>Safety first</h2>
    <p>The warning signs that need urgent care are always in Settings. Migrely is a diary, not a medical device: it
      helps you keep a record and talk to your doctor.</p></div>
</section>

<section class="wrap block final"><h2>Start your diary today</h2>{play_button('/')}</section>
'''
    app = {'@type': 'MobileApplication', 'name': 'Migrely: Migraine Tracker',
          'operatingSystem': 'Android', 'applicationCategory': 'HealthApplication', 'url': f'{BASE}/',
          'downloadUrl': PLAY, 'image': f'{BASE}/assets/img/icon-512.png',
          'description': 'A calm, private migraine diary: log in seconds, see your patterns, and share a report with '
                         'your doctor.',
          'offers': {'@type': 'Offer', 'price': '0', 'priceCurrency': 'USD'},
          'publisher': {'@type': 'Organization', 'name': 'Sulton UzDev'}}
    website = {'@type': 'WebSite', '@id': f'{BASE}/#website', 'name': 'Migrely', 'url': f'{BASE}/',
               'publisher': {'@id': ORGANIZATION['@id']}}
    extra = json_ld({'@context': 'https://schema.org', '@graph': [ORGANIZATION, website, app]})
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
                'deleting your data.', body, json_ld(ld))


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
    return page('/delete-data/', 'Delete your data · Migrely', 'How to delete everything Migrely keeps on your phone, '
                'and Android\'s backup of it. We never receive your entries.', body)


def about():
    path = '/about/'
    body = f'''<section class="wrap block narrow prose"><h1>About Migrely</h1>
  <p class="lead">Migrely is a calm, private migraine diary for Android. Here is who makes it, how it is paid for, and
    what it will never do.</p>
  <h2>Who makes Migrely?</h2>
  <p>Migrely is published on Google Play by Sulton UzDev. Questions and ideas reach us at
    <a href="mailto:{SUPPORT}">{SUPPORT}</a>.</p>
  <h2>Why does Migrely keep everything on the phone?</h2>
  <p>A headache diary holds some of the most personal things a phone can know: when you were in pain, what you took
    and your cycle. Migrely keeps all of it on your phone and has no internet access, so it cannot send it to us or
    to anyone else. <a href="/privacy-check/">You can check that yourself</a>, and see what Android's own backup and
    the files you choose to share do.</p>
  <p>It is built for the moment a migraine starts: one tap to log, a dark screen, and the details later.</p>
  <h2>How does Migrely make money?</h2>
  <p>From Migrely Premium, and nothing else. Premium adds the doctor report and Insights. The yearly plan starts with
    a 7-day free trial for new subscribers; there is also a monthly plan and a one-time lifetime purchase. Google Play
    handles the payment, and we never see your card, name or email.</p>
  <p>There are no ads, and no data is sold or shared: your entries never reach us, so there is nothing to sell.</p>
  <h2>What stays free?</h2>
  <p>Logging, your whole history, medications, reminders, backup and spreadsheet export, and every safety warning
    are free, for good.</p>
  <h2>What will Migrely never do?</h2>
  <ul><li>Ask for internet access.</li><li>Ask you to make an account.</li>
    <li>Show ads, or carry analytics or tracking code of its own.</li>
    <li>Put a safety warning behind Premium.</li></ul>
  <h2>Where do the medication limits come from?</h2>
  <p>Migrely is a diary, not a medical device. Its medication note uses the limits for
    <a href="/medication-overuse-headache/">medication-overuse headache</a> in the International Classification of
    Headache Disorders (ICHD-3): 15 or more days a month
    with simple painkillers, or 10 or more with triptans, combination painkillers or other medicines for attacks.
    Migrely counts your days over the last 30 and suggests talking to your doctor. It does not diagnose.</p>
  <h2>How can I contact you?</h2>
  <p>Help and questions: <a href="mailto:{SUPPORT}">{SUPPORT}</a>. Privacy:
    <a href="mailto:{PRIVACY_MAIL}">{PRIVACY_MAIL}</a>. Answers to common questions are on the
    <a href="/support/">help page</a>.</p>
  <p>{play_button(path)}</p></section>'''
    ld = {'@context': 'https://schema.org', '@type': 'AboutPage', 'url': f'{BASE}{path}', 'name': 'About Migrely',
          'about': ORGANIZATION}
    return page(path, 'About Migrely: who makes it and how it is paid for',
                'Migrely is a migraine diary paid for by Premium, not by your data: no ads, no account and no internet '
                'access. Who makes it, and why.', body, json_ld(ld))


def privacy_check():
    path = '/privacy-check/'
    count = in_words(len(PERMISSIONS))
    cards = ''.join(f'<article><h3>{esc(title)}</h3><p><code>{breakable(name)}</code></p><p>{esc(why)}</p></article>'
                    for title, name, why in PERMISSIONS)
    body = f'''<section class="wrap block narrow prose"><h1>Check Migrely's privacy yourself</h1>
  <p class="lead">Many apps promise privacy. With Migrely you can check it: the app has no internet permission, so
    Android itself stops it from connecting to the internet. It takes about two minutes.</p>
  <h2>Does Migrely connect to the internet?</h2>
  <p>No. An Android app can reach the internet only if it asks for the internet permission, and Migrely does not ask
    for it. Android then blocks every connection the app tries, so nothing you enter can leave your phone through
    Migrely.</p>
  <h2>How can I check it on my phone?</h2>
  <ol><li>Open your phone's Settings, then Apps, then Migrely.</li><li>Tap Permissions.</li>
    <li>Tap the ⋮ menu at the top, then All permissions.</li>
    <li>Look for “have full network access”. Apps that use the internet list it. Migrely does not.</li></ol>
  <p>The names can differ a little between phone makers. The check for developers below is the same on every
    phone.</p>
  <h2>Does airplane mode prove it?</h2>
  <p>It shows half of it. Turn on airplane mode and Migrely works as usual: logging, your history, reminders and export
    all work offline. That shows Migrely does not need the internet; the permission list shows it cannot use it.
    Buying or restoring Premium, and rating Migrely, need Google Play to be online.</p>
  <h2>What are Migrely's {count} permissions for?</h2>
  <p>These are all the permissions Migrely asks for. Those in quotes appear in the list under those names. On
    phones with Google Play, billing may be listed too.</p>
  <div class="perms">{cards}</div>
  <h2>What can still leave my phone?</h2>
  <p>Without the internet permission, Migrely itself sends nothing. Three things are up to Android, Google Play or
    you:</p>
  <ul><li><strong>Android's own backup.</strong> Android may back up Migrely's entries to your Google account, as it
      does for other apps. On Android 9 and later Migrely allows this only end-to-end encrypted, which needs a screen
      lock; on Android 8 it is backed up like other apps' data. You can turn backup off in your phone's
      settings.</li>
    <li><strong>Google Play.</strong> When you buy Premium or rate Migrely, Google Play's own window handles it, under
      Google's privacy policy. Migrely never sees your payment details or your rating.</li>
    <li><strong>Files you share.</strong> A backup, spreadsheet or PDF report goes wherever you choose to send
      it.</li></ul>
  <p>The <a href="/privacy/">privacy policy</a> has the details.</p>
  <h2>How can a developer check it?</h2>
  <p>With USB debugging on and Migrely installed, run:</p>
  <pre><code>adb shell dumpsys package com.migrely.app | grep -A{len(PERMISSIONS)} "requested permissions"</code></pre>
  <p>It prints the same {count} permissions. To look for the internet permission alone, run:</p>
  <pre><code>adb shell dumpsys package com.migrely.app | grep INTERNET</code></pre>
  <p>It prints nothing.</p>
  <p>{play_button(path)}</p></section>'''
    return page(path, 'Check Migrely\'s privacy yourself · Migrely',
                'Migrely has no internet permission. Check it on your own phone in two minutes, and see what each of '
                f'its {count} permissions is for.', body)


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


def urgent_help():
    """The same urgent-help section on every medical page."""
    return f'''<h2>When should I get help straight away?</h2>
  <p>Call your emergency number if a headache comes on suddenly and is extremely painful, or comes with weakness on one
    side, problems speaking or loss of vision. The <a href="{NHS_MIGRAINE}">NHS migraine page</a> lists every warning
    sign.</p>'''


def reviewed_block(sources, reviewed):
    """A medical page's sources and the date its facts were last checked against them."""
    items = ''.join(f'<li><a href="{esc(url)}">{esc(name)}</a></li>' for name, url in sources)
    return f'''<h2>Sources</h2>
  <ul>{items}</ul>
  <p class="meta">Last reviewed: {reviewed[1]}. Written by Migrely from the sources above; not reviewed by a
    clinician. Educational only, not medical advice.</p>'''


def medical_ld(path, name, reviewed, about):
    return json_ld({'@context': 'https://schema.org', '@type': 'MedicalWebPage', 'url': f'{BASE}{path}',
                    'name': name, 'inLanguage': 'en', 'lastReviewed': reviewed[0], 'about': about,
                    'publisher': ORGANIZATION})


def headache_diary():
    path = '/headache-diary/'
    downloads = ''.join(f'<a class="play alt" href="{path}{DIARY_PDFS[size]}">{label}</a>'
                        for size, label in [('a4', 'Download the template (A4, PDF)'),
                                            ('letter', 'Download US Letter (PDF)')])
    body = f'''<section class="wrap block narrow prose"><h1>{esc(DIARY_TITLE)}</h1>
  <p class="lead">A headache diary turns hazy weeks into facts your doctor can use. Here is what UK guidance suggests
    recording, for how long, and a free 8-week template to print.</p>
  <div class="cta">{downloads}</div>
  <h2>Why keep a headache diary?</h2>
  <p>NICE, which writes guidance for doctors in England, suggests considering a headache diary to help diagnose
    headaches. A diary also shows whether a treatment is working, and gives you and your doctor something concrete to
    talk about.</p>
  <h2>What should I write down?</h2>
  <p>NICE suggests recording:</p>
  <ul><li>how often headaches come, how long they last and how bad they get;</li>
    <li>other symptoms, such as nausea, or light or sound bothering you;</li>
    <li>every medicine you take for them, prescribed and over the counter;</li>
    <li>possible triggers;</li><li>any link to your period, if you have periods.</li></ul>
  <p>The template also asks whether each medicine helped.</p>
  <h2>How long should I keep it?</h2>
  <p>At least 8 weeks, NICE suggests. If headaches seem linked to your period, keep it for at least 2 menstrual cycles;
    the Migraine Trust suggests 3 months. For frequent headaches, the international classification (ICHD-3) says a
    day-by-day diary for at least a month is generally needed.</p>
  <h2>What counts as a headache day?</h2>
  <p>Any day with headache, for any part of the day (ICHD-3). Fill in a line on pain-free days too: they are part of
    the picture, and they make the count reliable.</p>
  <h2>Why count medicine days?</h2>
  <p>Taking medicine for attacks on many days a month can make headaches more frequent. This is called
    medication-overuse headache. ICHD-3 describes it in people who have headache on 15 or more days a month after more
    than 3 months of taking:</p>
  <ul><li>triptans, ergots, opioids or combination painkillers (such as those with caffeine or codeine) on 10 or more
      days a month;</li>
    <li>a mix of these kinds, or of these and simple painkillers, on 10 or more days a month in total;</li>
    <li>simple painkillers alone, such as paracetamol, aspirin or ibuprofen, one or several, on 15 or more days a
      month.</li></ul>
  <p>These are diagnostic thresholds, not safe limits: the NHS suggests not taking painkillers on more than 2 days a
    week. Don't stop or change medicines without talking to your doctor.
    <a href="/medication-overuse-headache/">More on medication-overuse headache</a>.</p>
  {urgent_help()}
  <h2>What's in the template?</h2>
  <ul><li>Page 1: how to use it, a key, and the warning signs that need urgent care.</li>
    <li>Pages 2 to 5: two weeks each, one row a day, for 8 weeks.</li>
    <li>Page 6: a summary for your doctor, with medicine days by kind.</li></ul>
  <p>It prints well in black and white, on A4 or US Letter.</p>
  <h2>Would you rather use your phone?</h2>
  <p>Migrely, our app, keeps the same diary on Android, with no account and no internet access. Logging, your whole
    history and spreadsheet export are free, and it counts medicine days for you. Its doctor PDF report is part of
    Premium.</p>
  <p>{play_button(path)}</p>
  {reviewed_block(DIARY_SOURCES, DIARY_REVIEWED)}</section>'''
    ld = medical_ld(path, DIARY_TITLE, DIARY_REVIEWED, {'@type': 'MedicalCondition', 'name': 'Headache'})
    return page(path, 'Headache diary: free printable template · Migrely',
                'What to write in a headache or migraine diary, for how long, and how to count headache and medicine '
                'days. A free printable 8-week template, A4 and US Letter.', body, ld)


# The medication-overuse table: medicine kind, days a month for more than 3 months (ICHD-3 8.2, NICE 1.2.7).
MOH_DAYS = [
    ('Triptans', '10 or more'),
    ('Ergots (ergotamine)', '10 or more'),
    ('Opioids, such as codeine', '10 or more'),
    ('Combination painkillers: drugs of two or more kinds in one tablet, such as paracetamol with codeine, or a '
     'painkiller with caffeine', '10 or more'),
    ('A mix of the kinds above, or of these and simple painkillers', '10 or more in total'),
    ('Simple painkillers alone, one or several: paracetamol (acetaminophen), aspirin, ibuprofen and other NSAIDs',
     '15 or more'),
]
MOH_SOURCES = [
    ('ICHD-3, 8.2 Medication-overuse headache, with 8.2.3, 8.2.5 and 8.2.6', ICHD_MOH),
    ('NICE CG150, Headaches in over 12s: recommendations 1.2.7, 1.3.6 and 1.3.42 to 1.3.48', NICE_CG150),
    ('NHS, Migraine', NHS_MIGRAINE),
]


def medication_overuse():
    path = '/medication-overuse-headache/'
    rows = ''.join(f'<tr><td>{esc(kind)}</td><td>{esc(days)}</td></tr>' for kind, days in MOH_DAYS)
    body = f'''<section class="wrap block narrow prose"><h1>{esc(MOH_TITLE)}</h1>
  <p class="lead">Medication-overuse headache is headache on 15 or more days a month that comes from taking medicine
    for headache too often for more than 3 months: on 10 or more days a month for triptans, ergots, opioids,
    combination painkillers or a mix of kinds, or on 15 or more days a month for simple painkillers alone.</p>
  <h2>What is medication-overuse headache?</h2>
  <p>It is a headache disorder in its own right, in people who already have headaches such as migraine, and it was
    once called rebound headache. Medicine that eases an attack, taken on many days, can make headaches more frequent.
    It can come with chronic migraine, and a doctor may diagnose both.</p>
  <h2>How many days count as overuse?</h2>
  <p>The international classification (ICHD-3) counts days a month, kept up for more than 3 months:</p>
  <table><thead><tr><th>Medicine for the attack</th><th>Days a month</th></tr></thead><tbody>{rows}</tbody></table>
  <p>Simple painkillers are one kind, so paracetamol on some days and ibuprofen on others still count toward 15, not
    10.</p>
  <h2>Does it count doses or days?</h2>
  <p>Days. A day with two doses counts once. A day with a triptan and paracetamol counts for both kinds, and once in
    the total.</p>
  <h2>Are these safe limits?</h2>
  <p>No. They are the counts doctors use to diagnose it, not a safe amount. The NHS suggests not taking painkillers on
    more than 2 days a week, and NICE asks doctors to explain the risk to anyone using medicine for headache
    attacks.</p>
  <h2>How is it treated?</h2>
  <p>UK guidance (NICE) says doctors treat it by stopping the overused medicine, usually for at least a month. Headaches
    may get worse for a while before they improve, so it comes with close follow-up, sometimes with a preventive
    treatment, and a review after 4 to 8 weeks. ICHD-3 says it usually, but not always, gets better once the overuse
    stops.</p>
  <p>Plan this with your doctor, especially if you take opioids, and don't stop or change medicines on your own.</p>
  <h2>How can I tell if I'm getting close?</h2>
  <p>Count your medicine days by kind. The <a href="/headache-diary/">free printable headache diary</a> has a column
    for it. Migrely, our app, counts them for you over the last 30 days: it shows a gentle note 2 days before these
    counts and a "talk to your doctor" note when you reach one. The note is free, and it is a heads-up, not a
    diagnosis: the diagnosis looks at more than 3 months, and only a doctor makes it.</p>
  <p>{play_button(path)}</p>
  {urgent_help()}
  {reviewed_block(MOH_SOURCES, MOH_REVIEWED)}</section>'''
    ld = medical_ld(path, MOH_TITLE, MOH_REVIEWED, {'@type': 'MedicalCondition', 'name': 'Medication-overuse headache',
                                                    'alternateName': 'Rebound headache'})
    return page(path, 'Medication-overuse headache: 10 and 15 days · Migrely',
                'Medication-overuse (rebound) headache: the ICHD-3 day counts for triptans and painkillers, why they '
                'aren\'t safe limits, and what to ask your doctor.', body, ld)


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


def check_permissions():
    """Stops the build when /privacy-check/ no longer lists what the release build asks for (BRD EC-WEB-01)."""
    if not PERMISSION_REPORT.exists():
        print('note: no release permission report, so the list on /privacy-check/ is not checked; run '
              './gradlew :app:checkReleasePermissions first')
        return
    shipped = set(PERMISSION_REPORT.read_text().split())
    listed = {name for _, name, _ in PERMISSIONS}
    if shipped != listed:
        raise SystemExit(f'/privacy-check/ lists {sorted(listed)}, but the release build asks for {sorted(shipped)}. '
                         'Update PERMISSIONS in website/build.py.')


def lastmods(pages):
    """Each page's last change date, kept with its HTML's hash in website/lastmod.json (BRD D136).

    Google trusts lastmod only when it is always right, so a page keeps its date until its HTML changes.
    """
    known = json.loads(LASTMOD.read_text()) if LASTMOD.exists() else {}
    today = date.today().isoformat()
    marks = {}
    for path, content in pages.items():
        digest = hashlib.sha256(content.encode()).hexdigest()
        mark = known.get(path)
        marks[path] = mark if mark and mark['sha256'] == digest else {'sha256': digest, 'lastmod': today}
    LASTMOD.write_text(json.dumps(marks, indent=2, sort_keys=True) + '\n')
    return {path: mark['lastmod'] for path, mark in marks.items()}


# The pages in the sitemap, in its order; the 404 page stays out of it.
PAGES = [('/', 'index.html', home), ('/about/', 'about/index.html', about),
         ('/privacy-check/', 'privacy-check/index.html', privacy_check),
         ('/headache-diary/', 'headache-diary/index.html', headache_diary),
         ('/medication-overuse-headache/', 'medication-overuse-headache/index.html', medication_overuse),
         ('/support/', 'support/index.html', support),
         ('/privacy/', 'privacy/index.html', privacy), ('/delete-data/', 'delete-data/index.html', delete_data)]


def check_diary_pdfs():
    """Stops the build when a download on /headache-diary/ would be missing (BRD D140)."""
    missing = [name for name in DIARY_PDFS.values() if not (SITE / 'headache-diary' / name).exists()]
    if missing:
        raise SystemExit(f'Missing {", ".join(missing)}: run python3 website/make_diary_pdf.py first.')


def main():
    check_permissions()
    check_diary_pdfs()
    built = {path: (rel, build()) for path, rel, build in PAGES}
    for rel, content in [*built.values(), ('404.html', not_found())]:
        target = SITE / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content)
    images()
    dates = lastmods({path: content for path, (_, content) in built.items()})
    (SITE / 'sitemap.xml').write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + ''.join(f'  <url><loc>{BASE}{path}</loc><lastmod>{dates[path]}</lastmod></url>\n' for path in built)
        + '</urlset>\n')
    (SITE / 'robots.txt').write_text(f'User-agent: *\nAllow: /\nSitemap: {BASE}/sitemap.xml\n')
    print('built', ', '.join(built), 'and 404.html')


if __name__ == '__main__':
    main()
