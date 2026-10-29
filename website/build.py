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
MENSTRUAL_REVIEWED = ('2026-10-29', '29 October 2026')
MENSTRUAL_TITLE = 'Menstrual migraine: the day −2 to +3 window'
VA_REVIEWED = ('2026-10-29', '29 October 2026')
VA_TITLE = 'Keeping a migraine log for a VA claim'
COMPARE_REVIEWED = ('2026-10-29', '29 October 2026')
COMPARE_TITLE = 'Migraine diary apps for Android, compared on facts'
# The last line of a reviewed page's sources (BRD D143): the medical pages and the VA page say who did not review them.
MEDICAL_DISCLAIMER = 'not reviewed by a clinician. Educational only, not medical advice.'
VA_DISCLAIMER = 'not reviewed by a lawyer or by VA. General information, not legal or medical advice.'
NICE_CG150 = 'https://www.nice.org.uk/guidance/cg150/chapter/Recommendations'
MT_MENSTRUAL = 'https://migrainetrust.org/understand-migraine/types-of-migraine/menstrual-migraine/'
DIARY_SOURCES = [
    ('NICE CG150, Headaches in over 12s: recommendations 1.1.3, 1.1.4, 1.2.6, 1.2.7 and 1.3.1',
     NICE_CG150),
    ('ICHD-3, Definition of terms: headache days', 'https://ichd-3.org/definition-of-terms/'),
    ('ICHD-3, 1.3 Chronic migraine', 'https://ichd-3.org/1-migraine/1-3-chronic-migraine/'),
    ('ICHD-3, 8.2 Medication-overuse headache', ICHD_MOH),
    ('NHS, Migraine', NHS_MIGRAINE),
    ('The Migraine Trust, Keeping a headache diary',
     'https://migrainetrust.org/live-with-migraine/self-management/keeping-a-migraine-diary/'),
    ('The Migraine Trust, Menstrual migraine', MT_MENSTRUAL),
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
    the files you choose to share do. <a href="/compare/">See how other migraine diaries compare</a>.</p>
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
  <p>Migrely is a diary, not a medical device. Its medication note uses the day counts for
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


def urgent_help(extra=''):
    """The same urgent-help section on the medical pages and the VA page, with an optional extra line."""
    return f'''<h2>When should I get help straight away?</h2>
  <p>Call your emergency number if a headache comes on suddenly and is extremely painful, or comes with weakness on one
    side, problems speaking or loss of vision. The <a href="{NHS_MIGRAINE}">NHS migraine page</a> lists every warning
    sign.</p>{extra}'''


def reviewed_block(sources, reviewed, disclaimer=MEDICAL_DISCLAIMER):
    """A page's sources and the date its facts were last checked against them."""
    items = ''.join(f'<li><a href="{esc(url)}">{esc(name)}</a></li>' for name, url in sources)
    return f'''<h2>Sources</h2>
  <ul>{items}</ul>
  <p class="meta">Last reviewed: {reviewed[1]}. Written by Migrely from the sources above; {disclaimer}</p>'''


def medical_ld(path, name, reviewed, condition):
    """A medical page's MedicalWebPage JSON-LD, dated like its "Last reviewed" line."""
    return json_ld({'@context': 'https://schema.org', '@type': 'MedicalWebPage', 'url': f'{BASE}{path}',
                    'name': name, 'inLanguage': 'en', 'lastReviewed': reviewed[0], 'about': condition,
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
    the Migraine Trust suggests 3 months (<a href="/menstrual-migraine/">more on menstrual migraine</a>). For
    frequent headaches, the international classification (ICHD-3) says a day-by-day diary for at least a month is
    generally needed.</p>
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
    ('Combination painkillers: drugs of two or more kinds in one medicine, such as paracetamol with codeine, or a '
     'painkiller with caffeine', '10 or more'),
    ('A mix of the kinds above, or of these and simple painkillers', '10 or more in total'),
    ('Simple painkillers alone, one or several: paracetamol (acetaminophen), aspirin, ibuprofen and other NSAIDs',
     '15 or more'),
    ('Any other medicine taken for attacks', '10 or more'),
]
MOH_SOURCES = [
    ('ICHD-3, 8.2 Medication-overuse headache, with 8.2.3, 8.2.5, 8.2.6 and 8.2.8', ICHD_MOH),
    ('NICE CG150, Headaches in over 12s: recommendations 1.2.7, 1.3.6 and 1.3.42 to 1.3.48', NICE_CG150),
    ('NHS, Migraine', NHS_MIGRAINE),
]


def medication_overuse():
    path = '/medication-overuse-headache/'
    rows = ''.join(f'<tr><th scope="row">{esc(kind)}</th><td>{esc(days)}</td></tr>' for kind, days in MOH_DAYS)
    body = f'''<section class="wrap block narrow prose"><h1>{esc(MOH_TITLE)}</h1>
  <p class="lead">Medication-overuse headache is headache on 15 or more days a month that comes from taking medicine
    for headache too often for more than 3 months: on 10 or more days a month for triptans, ergots, opioids,
    combination painkillers or a mix of kinds, or on 15 or more days a month for simple painkillers alone, one or
    several.</p>
  <h2>What is medication-overuse headache?</h2>
  <p>It is a headache disorder in its own right, in people who already have headaches such as migraine, and it was
    once called rebound headache. Medicine that eases an attack, taken on many days, can make headaches more frequent.
    It can come with chronic migraine, and a doctor may diagnose both.</p>
  <h2>How many days count as overuse?</h2>
  <p>The international classification (ICHD-3) counts days a month, kept up for more than 3 months:</p>
  <table><caption>ICHD-3: days a month, kept up for more than 3 months</caption>
    <thead><tr><th scope="col">Medicine for the attack</th><th scope="col">Days a month</th></tr></thead>
    <tbody>{rows}</tbody></table>
  <p>Simple painkillers are one kind, so paracetamol on some days and ibuprofen on others still count toward 15, not
    10.</p>
  <h2>Does it count doses or days?</h2>
  <p>Days. A day with two doses counts once. A day with a triptan and paracetamol counts for both kinds, and once in
    the total. Days you take these medicines for other pain, such as period or back pain, count too.</p>
  <h2>Are these safe limits?</h2>
  <p>No. They are the counts doctors use to diagnose it, not a safe amount. The NHS suggests not taking painkillers on
    more than 2 days a week, and NICE asks doctors to explain the risk to anyone using medicine for headache
    attacks.</p>
  <h2>How is it treated?</h2>
  <p>UK guidance (NICE) says doctors treat it by stopping the overused medicine for at least a month. Headaches are
    likely to get worse for a while before they improve, and there may be withdrawal symptoms, so doctors offer close
    follow-up and support, sometimes a preventive treatment, and a review 4 to 8 weeks after stopping. People who take
    strong opioids may need a specialist. ICHD-3 says it usually, but not always, gets better once the overuse
    stops.</p>
  <p>Plan this with your doctor, especially if you take opioids, and don't stop or change medicines on your own.</p>
  <h2>How can I keep count?</h2>
  <p>Count your medicine days by kind. The <a href="/headache-diary/">free printable headache diary</a> has a column
    for it. Migrely, our app, counts them for you over the last 30 days: it shows a gentle note 2 days before these
    counts and a note suggesting you talk to your doctor when you reach one. The note is free, and it is a heads-up,
    not a diagnosis: it looks at 30 days, while the diagnosis also needs headache on 15 or more days a month for more
    than 3 months, and only a doctor makes it.</p>
  <p>{play_button(path)}</p>
  {urgent_help()}
  {reviewed_block(MOH_SOURCES, MOH_REVIEWED)}</section>'''
    ld = medical_ld(path, MOH_TITLE, MOH_REVIEWED, {'@type': 'MedicalCondition', 'name': 'Medication-overuse headache',
                                                    'alternateName': 'Rebound headache'})
    return page(path, 'Medication-overuse headache: 10 and 15 days · Migrely',
                'Medication-overuse (rebound) headache: the ICHD-3 day counts for triptans and painkillers, why they '
                'aren\'t safe limits, and how it is treated.', body, ld)


# The window around day 1 of a period (ICHD-3 A1.1.1, A1.1.2; NICE 1.2.5); there is no day 0.
MENSTRUAL_DAYS = [('Day −2', '2 days before your period starts'), ('Day −1', 'The day before your period starts'),
                  ('Day 1', 'The first day of bleeding'), ('Day 2', 'The 2nd day of your period'),
                  ('Day 3', 'The 3rd day of your period')]
MENSTRUAL_SOURCES = [
    ('ICHD-3, A1.1.1 Pure menstrual migraine without aura',
     'https://ichd-3.org/appendix/a1-migraine/a1-1-migraine-without-aura/a1-1-1-pure-menstrual-migraine-without-aura/'),
    ('ICHD-3, A1.1.2 Menstrually-related migraine without aura',
     'https://ichd-3.org/appendix/a1-migraine/a1-1-migraine-without-aura/'
     'a1-1-2-menstrually-related-migraine-without-aura/'),
    ('NICE CG150, Headaches in over 12s: recommendations 1.2.5, 1.2.6, 1.3.30 and 1.3.31', NICE_CG150),
    ('The Migraine Trust, Menstrual migraine', MT_MENSTRUAL),
    ('NHS, Migraine', NHS_MIGRAINE),
]


def menstrual_migraine():
    path = '/menstrual-migraine/'
    rows = ''.join(f'<tr><th scope="row">{esc(day)}</th><td>{esc(what)}</td></tr>' for day, what in MENSTRUAL_DAYS)
    body = f'''<section class="wrap block narrow prose"><h1>{esc(MENSTRUAL_TITLE)}</h1>
  <p class="lead">Menstrual migraine means migraine attacks that happen from 2 days before a period to its 3rd day
    (days −2 to +3, where day 1 is the first day of bleeding and there is no day 0) in at least 2 of 3 cycles. If
    attacks happen only then, it is called pure menstrual migraine. If they also happen at other times, it is
    menstrually-related migraine, which is far more common. A doctor confirms it, usually from a diary kept over a few
    cycles.</p>
  <h2>What is menstrual migraine?</h2>
  <p>It is migraine linked to periods. The international classification (ICHD-3) describes both kinds, for migraine
    without aura, in its appendix of research criteria. The Migraine Trust says fewer than 1 in 100 women with migraine
    are thought to have the pure kind, while as many as two-thirds may have attacks linked to their period.</p>
  <p>For this purpose, the withdrawal bleed in the break between packs of a combined pill, or during cyclical hormone
    replacement therapy (HRT), counts as a period too.</p>
  <h2>How do I count the days?</h2>
  <p>Day 1 is the first day of bleeding. The day before it is day −1, so there is no day 0. The window is five days:</p>
  <table><caption>The menstrual migraine window (ICHD-3)</caption>
    <thead><tr><th scope="col">Day</th><th scope="col">When</th></tr></thead><tbody>{rows}</tbody></table>
  <h2>How long should I track it?</h2>
  <p>UK guidance (NICE) suggests a headache diary for at least 2 menstrual cycles, and the Migraine Trust at least 3
    months. Mark your period days and every attack. NICE suggests suspecting migraine linked to periods (it calls this
    menstrual-related migraine) when attacks happen mostly in this window in at least 2 of 3 cycles in a row. The NHS
    suggests seeing a GP if you regularly get attacks around your period. The
    <a href="/headache-diary/">free printable headache diary</a> has a column for period days.</p>
  <h2>Does contraception matter?</h2>
  <p>It can. NICE advises doctors not to routinely offer combined hormonal contraception (the combined pill, patch or
    ring) for contraception to people who have migraine with aura, and the Migraine Trust says the same for other
    stroke risk factors. If you have aura, or get it for the first time while using one of these, tell whoever
    prescribes your contraception soon: they may suggest a different method. If you stop a method, it no longer
    protects you from pregnancy.</p>
  <h2>How is it treated?</h2>
  <p>Standard migraine medicines can be used for menstrual migraine, the Migraine Trust says. For period-related
    migraine that is predictable and does not respond well to usual acute treatment, NICE describes options a doctor
    may consider on the days migraine is expected. Days you take medicine for it count toward
    <a href="/medication-overuse-headache/">medication-overuse headache</a>. Talk to your doctor about what suits
    you.</p>
  <h2>Is my period data private in Migrely?</h2>
  <p>Migrely has no internet access and no account, so it cannot send your period days or migraines anywhere. They are
    kept on your phone, and can leave it only in two ways: in Android's own backup to your Google account, if backup
    is on (end-to-end encrypted on Android 9 and later when your phone has a screen lock), and in any file you export
    and share. Tracking periods is optional: turning it off hides the marks, and any you saved stay until you delete
    them. You can <a href="/privacy-check/">check the privacy yourself</a>.</p>
  <p>Marking period days on the calendar is free. Once the months you look at include at least 2 period starts and 5
    migraines, Insights, part of Premium, counts how many of your migraines started in these five days and shows how
    much of the time those days make up. It doesn't draw a conclusion: that is for you and your doctor.</p>
  <p>{play_button(path)}</p>
  {urgent_help()}
  {reviewed_block(MENSTRUAL_SOURCES, MENSTRUAL_REVIEWED)}</section>'''
    ld = medical_ld(path, MENSTRUAL_TITLE, MENSTRUAL_REVIEWED,
                    {'@type': 'MedicalCondition', 'name': 'Menstrual migraine'})
    return page(path, f'{MENSTRUAL_TITLE} · Migrely',
                'Migraine around your period: the window doctors use, pure and menstrually-related migraine, how long '
                'to track it, and keeping period data private.', body, ld)


ECFR_4124A = 'https://www.ecfr.gov/current/title-38/chapter-I/part-4/subpart-B/section-4.124a'
VA_DBQ = 'https://www.benefits.va.gov/compensation/docs/Headaches_Including_Migraines.pdf'
VA_FIND_REP = 'https://www.va.gov/get-help-from-accredited-representative/find-rep/'
VA_CRISIS_LINE = '''
  <p>Veterans in crisis can dial 988, then press 1, or text 838255 (Veterans Crisis Line).</p>'''
# 38 CFR 4.124a, diagnostic code 8100, in the regulation's own words (read on VA_REVIEWED).
DC_8100 = [
    ('50%', 'With very frequent completely prostrating and prolonged attacks productive of severe economic '
            'inadaptability'),
    ('30%', 'With characteristic prostrating attacks occurring on an average once a month over last several months'),
    ('10%', 'With characteristic prostrating attacks averaging one in 2 months over last several months'),
    ('0%', 'With less frequent attacks'),
]
VA_SOURCES = [
    ('38 CFR 4.124a, Schedule of ratings: neurological conditions and convulsive disorders, diagnostic code 8100 '
     '(eCFR)', ECFR_4124A),
    ('VA, Headaches (including migraine headaches) Disability Benefits Questionnaire, updated 9 July 2024', VA_DBQ),
    ('VA, VA-accredited representative FAQs, last updated 8 August 2024',
     'https://www.va.gov/resources/va-accredited-representative-faqs/'),
]


def va_migraine_log():
    path = '/va-migraine-log/'
    rows = ''.join(f'<tr><th scope="row">{esc(rating)}</th><td>{esc(words)}</td></tr>' for rating, words in DC_8100)
    body = f'''<section class="wrap block narrow prose"><h1>{esc(VA_TITLE)}</h1>
  <p class="lead">Migrely is not affiliated with or endorsed by the U.S. Department of Veterans Affairs (VA), and it is
    not a VA-accredited representative, so it cannot help you prepare or file a claim. This page is general
    information, not legal or medical advice, and no log or app can promise a rating. Accredited Veterans Service
    Organization (VSO) representatives help with VA claims for free.</p>
  <h2>How does VA rate migraine?</h2>
  <p>VA rates migraine under diagnostic code 8100. The 10% and 30% levels depend on how often "prostrating" attacks
    happen, on average, over the last several months. The 50% level also asks whether attacks are completely
    prostrating and prolonged and cause "severe economic inadaptability". In the regulation's words:</p>
  <table class="short-first"><caption>38 CFR 4.124a, diagnostic code 8100, read on {VA_REVIEWED[1]}</caption>
    <thead><tr><th scope="col">Rating</th><th scope="col">VA's words</th></tr></thead><tbody>{rows}</tbody></table>
  <p>VA can change its rating schedule. The current text is on <a href="{ECFR_4124A}">eCFR</a>.</p>
  <h2>What does "prostrating" mean?</h2>
  <p>VA's headaches Disability Benefits Questionnaire (DBQ), the form doctors complete for VA, says: "For VA purposes,
    the term prostrating means 'causing extreme exhaustion, powerlessness, debilitation or incapacitation with
    substantial inability to engage in ordinary activities.'" It then asks how often such attacks happened, on
    average, over the last several months.</p>
  <h2>What should a migraine log record?</h2>
  <ul><li>the date of every attack, the mild ones too, so the record is complete;</li>
    <li>when it started and when it ended;</li>
    <li>how bad it got;</li>
    <li>what you could still do and what you had to stop, for example: carried on, slowed down, stopped, or lay
      down;</li>
    <li>the medicines you took, and whether they helped;</li>
    <li>work or plans you missed.</li></ul>
  <p>Keep it for months, written at the time or soon after. A steady, honest record is easier for your doctor and
    your representative to rely on than memory.</p>
  <h2>What does Migrely record?</h2>
  <p>Migrely, our app for Android, keeps the start and end of each attack, the worst pain, the medicines you took and
    a note. "Impact on your day" lets you record how each attack affected you: No impact, Less active, Had to stop or
    Bed rest. You choose these labels for your own record. They are Migrely's words, not VA's terms, and what counts
    as prostrating is for VA to decide.</p>
  <p>Exporting a spreadsheet (Export for Excel, CSV) is free. It has one row per attack, with its start, end,
    duration, peak pain, symptoms, medicines, impact and note. The PDF report for appointments is part of Premium.</p>
  <p>{play_button(path)}</p>
  <h2>Who can help with a claim?</h2>
  <p>VA accredits three kinds of representatives: VSO representatives, attorneys and claims agents. VA says the
    services an accredited VSO representative provides on your claim are always free. Some accredited attorneys and
    claims agents also help for free on initial claims; VA lets them charge fees once it has decided your initial
    claim. You can <a href="{VA_FIND_REP}">find a VA-accredited representative or VSO</a> on VA's website.</p>
  {urgent_help(VA_CRISIS_LINE)}
  {reviewed_block(VA_SOURCES, VA_REVIEWED, VA_DISCLAIMER)}</section>'''
    ld = json_ld({'@context': 'https://schema.org', '@type': 'WebPage', 'url': f'{BASE}{path}', 'name': VA_TITLE,
                  'inLanguage': 'en', 'lastReviewed': VA_REVIEWED[0], 'publisher': ORGANIZATION})
    return page(path, 'Migraine log for a VA claim: what to record · Migrely',
                'How VA\'s migraine code 8100 rates prostrating attacks, what to record in a migraine log, and where '
                'to get free help. Not affiliated with VA.', body, ld)


def play_page(package):
    return f'https://play.google.com/store/apps/details?id={package}'


def data_safety(package):
    return f'https://play.google.com/store/apps/datasafety?id={package}'


COMPARE_NETWORK = ('Requests "full network access" (Android\'s internet permission), per its Google Play permission '
                   'list')
# Every app the comparison shows (BRD D144), with only facts read from its own listing, website or policy on
# COMPARE_REVIEWED. A fact that could not be verified has no row (EC-WEB-18). Migrely comes first, then A to Z.
COMPARE_APPS = [
    {'name': 'Migrely (our app)', 'maker': 'Sulton UzDev', 'rows': [
        ('Platforms', 'Android only'),
        ('Account', 'None'),
        ('Internet permission', 'None: Android stops it from connecting (see how to check, linked below)'),
        ('Play Data safety: shared', 'Declared to Google Play: none shared. The Play listing is not public yet'),
        ('Free', 'Logging, the whole history, medications, reminders, backup and spreadsheet export, and every safety '
                 'warning'),
        ('Premium', 'Doctor PDF report and Insights: $29.99 a year with a 7-day free trial, $4.99 a month, or $59.99 '
                    'once (US prices)'),
        ('Doctor report', 'A PDF report (Premium); spreadsheet export is free'),
        ('Does well', 'One-tap logging with a dark attack screen; medicine days counted by kind; privacy you can '
                      'check yourself'),
        ('Limits', 'New, with no reviews yet; Android only; English only; no weather, sleep or wearable data'),
    ], 'sources': [('Check Migrely\'s privacy yourself', '/privacy-check/'), ('About Migrely', '/about/')]},
    {'name': 'Bearable', 'maker': 'Bearable Ltd', 'rows': [
        ('Platforms', 'Android and iOS; no browser or desktop version'),
        ('Account', 'Required: "Currently, we require you to create an account". It says throwaway accounts are '
                    'fine'),
        ('Internet permission', COMPARE_NETWORK),
        ('Play Data safety: shared', '"Location, Personal info and 2 others"'),
        ('Free', 'Unlimited tracking and reminders, trends reports for the past 30 days, and export of all data to '
                 'CSV'),
        ('Premium', '60, 90 and 365 days of data in reports: $34.99 a year or $6.99 a month, 7-day trial'),
        ('Doctor report', 'Shares through data export and screenshots of reports'),
        ('Does well', 'Tracks far beyond migraine, with mood and symptoms; Fitbit sync; a generous free tier'),
    ], 'sources': [('Google Play listing', play_page('com.bearable')), ('Bearable', 'https://bearable.app/'),
                   ('Google Play Data safety', data_safety('com.bearable')),
                   ('Why do I need to create an account?',
                    'https://bearable.app/support/common-questions/why-do-i-need-to-create-an-account/'),
                   ('Free vs Premium features',
                    'https://bearable.app/support/common-questions/bearable-free-vs-premium-features/'),
                   ('Pricing and principles', 'https://bearable.app/blog/pricing-and-principles/'),
                   ('Browser or desktop version?',
                    'https://bearable.app/support/common-questions/is-there-a-browser-or-desktop-version-of-bearable/'),
                   ('Sharing data with a doctor', 'https://bearable.app/support/common-questions/'
                    'how-to-share-data-with-a-doctor-therapist-or-other-medical-team-member/')]},
    {'name': 'Dark Logger', 'maker': 'ZephLogic', 'rows': [
        ('Platforms', 'Android and iOS'),
        ('Account', 'None'),
        ('Internet permission', COMPARE_NETWORK + '. Its privacy policy says your logs are stored "solely in your '
                                'device\'s secure local storage", that it looks up weather (such as Met.no) with your '
                                'location when you log an attack, and that it stores an anonymous push token only if '
                                'you turn on notifications'),
        ('Play Data safety: shared', '"No data shared with third parties"'),
        ('Premium', 'Paid "Pro" unlocks: $9.99 to $21.99 per item on Google Play (US)'),
        ('Doctor report', '30- and 90-day PDF summaries; CSV export and import'),
        ('Does well', 'A screen designed for light sensitivity; one-tap logging; automatic pressure readings'),
    ], 'sources': [('Google Play listing', play_page('com.zephlogic.darklogger.pro')),
                   ('Dark Logger', 'https://zephlogic.com/darklogger.html'),
                   ('Privacy policy', 'https://zephlogic.com/dark-logger-privacy')]},
    {'name': 'Migraine Buddy', 'maker': 'Aptar Digital Health', 'rows': [
        ('Platforms', 'Android and iOS'),
        ('Account', 'Its help center: "You have registered with Migraine Buddy so your data synchronizes '
                    'automatically with our server and is linked with your private account."'),
        ('Internet permission', COMPARE_NETWORK),
        ('Play Data safety: shared', '"No data shared with third parties"'),
        ('Premium', 'MBplus: $89.99 a year or $24.99 for two months (its website, US)'),
        ('Does well', 'The most Google Play downloads here (1M+); a community; automatic sleep recording; weather '
                      'forecasts; Health Connect'),
    ], 'sources': [('Google Play listing', play_page('com.healint.migraineapp')),
                   ('Google Play Data safety', data_safety('com.healint.migraineapp')),
                   ('Help center: account and data', 'https://migrainebuddy.com/help-center/category/account-data/'),
                   ('MBplus', 'https://migrainebuddy.com/mbplus/'), ('Migraine Buddy', 'https://migrainebuddy.com/')]},
    {'name': 'Migraine Insight', 'maker': 'Shiny New App', 'rows': [
        ('Platforms', 'Android and iPhone'),
        ('Internet permission', COMPARE_NETWORK),
        ('Play Data safety: shared', '"No data shared with third parties"'),
        ('Free', 'Logging, automated tracking, your logs as a list, and doctor reports ("make doctor\'s reports - '
                 'free forever")'),
        ('Premium', 'The pattern finder, and your logs shown on the calendar: $9.99 a month or $95.99 a year'),
        ('Does well', 'Free doctor reports; automatic weather, pollen, location, workout and sleep tracking'),
    ], 'sources': [('Google Play listing', play_page('com.shinynewapp.migraineinsight2')),
                   ('FAQ', 'https://www.migraineinsight.com/faq/'),
                   ('Premium vs free', 'https://www.migraineinsight.com/kb/premium-vs-free-how-to-see-your-data/'),
                   ('Migraine Insight', 'https://www.migraineinsight.com/')]},
    {'name': 'Migraine Log', 'maker': 'Eskild Hustvedt (open source)', 'rows': [
        ('Platforms', 'Android (Google Play and F-Droid) and iOS'),
        ('Account', 'None mentioned; it has no network access'),
        ('Internet permission', 'None: Google Play lists no permissions, and F-Droid lists only the app\'s own '
                                'internal one'),
        ('Play Data safety: shared', '"No data shared with third parties"'),
        ('Free', 'Everything: free and open source (GPLv3 or later), with an export function; no in-app purchases'),
        ('Does well', 'Open source; minimal; works with no network at all'),
    ], 'sources': [('Google Play listing', play_page('org.zerodogg.migraineLog')),
                   ('F-Droid', 'https://f-droid.org/en/packages/org.zerodogg.migraineLog/'),
                   ('Migraine Log', 'https://migrainelog.zerodogg.org/en')]},
    {'name': 'Migraine Monitor', 'maker': 'RPM Healthcare', 'rows': [
        ('Platforms', 'Android and iPhone, plus a web dashboard for clinicians'),
        ('Account', 'Required: its FAQ says to create an account and log in; records are kept "in a secure cloud '
                    'server"'),
        ('Internet permission', COMPARE_NETWORK),
        ('Play Data safety: shared', '"Personal info"'),
        ('Free', 'Free, with ads; no in-app purchases on Google Play'),
        ('Doctor report', 'A PDF of your report'),
        ('Does well', 'A clinician dashboard with messaging; an anonymous community'),
    ], 'sources': [('Google Play listing', play_page('com.migrainemonitor')),
                   ('FAQ', 'https://migrainemonitor.com/faq/')]},
    {'name': 'Nagi', 'maker': 'Timothé Duc', 'rows': [
        ('Platforms', 'Android and iPhone'),
        ('Account', 'None ("No account is created")'),
        ('Internet permission', COMPARE_NETWORK + '. Its privacy policy says recording an attack, completing it and '
                                'making a report "need no network at all"'),
        ('Play Data safety: shared', '"Personal info, Financial info and 3 others"'),
        ('Free', 'The diary for the last 30 days, and CSV export of all your data'),
        ('Premium', 'The PDF doctor report, among other features: €59.90 a year (shown as €4.99 a month) or €35.99 '
                    'every 3 months, 7-day free trial (prices in euros)'),
        ('Does well', 'A dark, animation-free attack mode; a widget; correlations worked out on the phone'),
    ], 'sources': [('Google Play listing', play_page('com.timotheduc.nagi')),
                   ('Google Play Data safety', data_safety('com.timotheduc.nagi')),
                   ('Nagi', 'https://nagimigraine.com/'), ('Privacy policy', 'https://nagimigraine.com/privacy/')]},
]
COMPARE_SOURCES = [
    ('Android: the INTERNET permission and its label',
     'https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/res/AndroidManifest.xml'),
    ('Android: the label "have full network access"',
     'https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/res/res/values/strings.xml'),
]
COMPARE_DISCLAIMER = ('not reviewed by the other apps\' makers. Prices and features change, so check each app\'s own '
                      'sources before you choose.')


def compare():
    path = '/compare/'
    cards = []
    for app in COMPARE_APPS:
        rows = ''.join(f'<div><dt>{esc(field)}</dt><dd>{esc(text)}</dd></div>' for field, text in app['rows'])
        links = ' · '.join(f'<a href="{esc(url)}">{esc(label)}</a>' for label, url in app['sources'])
        cards.append(f'<article><h3>{esc(app["name"])}</h3><p class="meta">{esc(app["maker"])}</p><dl>{rows}</dl>'
                     f'<p class="meta">Sources, read on {COMPARE_REVIEWED[1]}: {links}</p></article>')
    body = f'''<section class="wrap block narrow prose"><h1>{esc(COMPARE_TITLE)}</h1>
  <p class="lead">We make Migrely, so we are not neutral. This page sticks to what each app's own Google Play listing,
    permission list, Data safety section, website and privacy policy said on {COMPARE_REVIEWED[1]}, with links, and
    it lists what each app does well. Prices and features change, so check the sources before you choose.</p>
  <h2>Which apps have no internet permission?</h2>
  <p>Two of these diaries request no internet permission on Android: Migrely and the open-source Migraine Log. The
    others request "full network access", according to their Google Play permission lists. Nagi and Dark Logger say
    their diaries work offline; their privacy policies list what they use the network for, such as weather.</p>
  <h2>Which apps need an account?</h2>
  <p>Bearable and Migraine Monitor require one. Migraine Buddy's help center says your data synchronizes with its
    server and is linked with your account. Nagi, Dark Logger and Migrely need none, and Migraine Log mentions
    none.</p>
  <h2>The apps, one by one</h2>
  <div class="apps">{''.join(cards)}</div>
  <p>Bearable, Dark Logger, Migraine Buddy, Migraine Insight, Migraine Log, Migraine Monitor and Nagi belong to
    their owners. Migrely is not affiliated with any of them, and none of them reviewed or endorsed this page. Tell us
    at <a href="mailto:{SUPPORT}">{SUPPORT}</a> if anything here is out of date.</p>
  <p>{play_button(path)}</p>
  {reviewed_block(COMPARE_SOURCES, COMPARE_REVIEWED, COMPARE_DISCLAIMER)}</section>'''
    ld = json_ld({'@context': 'https://schema.org', '@type': 'WebPage', 'url': f'{BASE}{path}', 'name': COMPARE_TITLE,
                  'inLanguage': 'en', 'lastReviewed': COMPARE_REVIEWED[0], 'publisher': ORGANIZATION})
    return page(path, 'Migraine diary apps for Android, compared · Migrely',
                'Migraine diary apps for Android compared on their own published facts: account, internet access, '
                'data shared, free tier and price, with sources.', body, ld)


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
         ('/menstrual-migraine/', 'menstrual-migraine/index.html', menstrual_migraine),
         ('/va-migraine-log/', 'va-migraine-log/index.html', va_migraine_log),
         ('/compare/', 'compare/index.html', compare),
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
