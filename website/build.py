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
VERSIONS = ROOT / 'gradle' / 'libs.versions.toml'
# _headers caches /assets/ for a year, so the stylesheet's address changes whenever it does.
CSS_VERSION = hashlib.sha256((ASSETS / 'site.css').read_bytes()).hexdigest()[:10]
LANDING_CSS_VERSION = hashlib.sha256((ASSETS / 'landing.css').read_bytes()).hexdigest()[:10]
BASE = 'https://migrely.health'
PLAY = 'https://play.google.com/store/apps/details?id=com.migrely.app'
SUPPORT = 'support@migrely.health'
PRIVACY_MAIL = 'privacy@migrely.health'
DISCLAIMER = ('Migrely is a diary. It is not a medical device and does not diagnose, treat or prevent any condition. '
              'Talk to a doctor about your headaches.')
# The oldest Android the app runs on, as the site states it; check_min_sdk() keeps it equal to the app's (EC-WEB-22).
MIN_SDK = 26
MIN_ANDROID = {26: 'Android 8', 27: 'Android 8.1', 28: 'Android 9', 29: 'Android 10', 30: 'Android 11'}[MIN_SDK]
# How Android's own backup treats Migrely's entries, as the privacy policy says (BRD D74, D147). Without a screen lock
# on Android 9 and later nothing goes to the cloud, so no answer may suggest the diary is backed up anyway.
BACKUP_RULE = ('On Android 9 and later Migrely allows this only end-to-end encrypted, which needs a screen lock; '
               'without one, the entries move only by direct transfer to a new phone. On Android 8 they are backed up '
               'like other apps\' data.')
# Premium's US prices, on /compare/, /support/ and /llms.txt alike (BRD D147).
PREMIUM_PRICES = '$29.99 a year with a 7-day free trial, $4.99 a month, or $59.99 once (US prices)'
# What Migrely is, in 40 to 60 words: the home page's definition and /llms.txt's summary (BRD D147).
WHAT_IS = ('Migrely is a migraine and headache diary for Android. Log an attack in a few taps, see each month as a '
           'calm night sky, and get your medicine days counted. With no account and no internet permission, nothing '
           'you enter can leave your phone through the app. Logging is free; Premium adds a doctor report and '
           'Insights.')
# /headache-diary/ (BRD D138): the date its facts were last checked against the sources, as ISO and as text.
DIARY_REVIEWED = ('2026-11-04', '4 November 2026')
# The printable template, made by website/make_diary_pdf.py (BRD D140).
DIARY_PDFS = {'a4': 'migrely-headache-diary-a4.pdf', 'letter': 'migrely-headache-diary-letter.pdf'}
# The home page's promo video and its poster in /assets/video/ (BRD D146). /assets/ is cached for a year, so a new cut
# gets a new name (EC-WEB-20). It is made in marketing/video/promo-v3, outside the repo, from real app screens only.
PROMO_VIDEO = ('migrely-promo-v3-3.mp4', 'migrely-promo-v3-3-poster.jpg')
PROMO_UPLOADED = '2026-11-13T12:00:00+05:00'
PROMO_SIZE = '2.0 MB'
# What each scene shows and says, for people who can't see the video, which has music and no speech (WCAG 1.2.1).
PROMO_SCENES = [
    '"Log a migraine in 3 taps. Even in a dark room. We\'ll keep the time while you rest." The taps are "Migraine '
    'started", a severity, then "Migraine ended".',
    '"Your month as a calm sky. Every day is drawn from what you logged." The month on the Home screen.',
    '"For your doctor, Premium. A report your doctor can read. A PDF with your summary, calendar, every migraine and '
    'medication." "Create report", then the PDF\'s pages. "Sample data, not a real patient."',
    '"Premium Insights. Know what comes with your migraines. Triggers often logged with your migraines, when they '
    'come, and how you rated your medications." Insights in the app. "Sample data, not a real patient."',
    '"Private by design. No internet permission. Android lists every app\'s permissions. Internet is not on Migrely\'s '
    'list. No account. No ads. Check it yourself: migrely.health/privacy-check." Android\'s list of Migrely\'s '
    'permissions.',
    '"Migrely. A calm migraine diary. Free on Google Play. migrely.health"',
    'Music: Erik Satie, Gymnopédie No. 1, played by Robin Alciatore (Musopen), public domain.',
]
DIARY_TITLE = 'Headache diary: what to write down, with a free printable template'
ICHD_MOH = ('https://ichd-3.org/8-headache-attributed-to-a-substance-or-its-withdrawal/'
            '8-2-medication-overuse-headache-moh/')
ICHD_CHRONIC = 'https://ichd-3.org/1-migraine/1-3-chronic-migraine/'
NHS_MIGRAINE = 'https://www.nhs.uk/conditions/migraine/'
NHS_HEADACHES = 'https://www.nhs.uk/symptoms/headaches/'
NHS_MENINGITIS = 'https://www.nhs.uk/conditions/meningitis/'
MOH_REVIEWED = ('2026-11-04', '4 November 2026')
MOH_TITLE = 'Medication-overuse headache: the 10- and 15-day rules'
MENSTRUAL_REVIEWED = ('2026-11-04', '4 November 2026')
MENSTRUAL_TITLE = 'Menstrual migraine: the day −2 to +3 window'
VA_REVIEWED = ('2026-10-29', '29 October 2026')
VA_TITLE = 'Keeping a migraine log for a VA claim'
COMPARE_REVIEWED = ('2026-10-29', '29 October 2026')
COMPARE_TITLE = 'Migraine diary apps for Android, compared on facts'
# The guides, linked from the home page and every footer (BRD D145): path, name and a summary that makes no claim
# the page itself does not.
GUIDES = [
    ('/headache-diary/', 'Free headache diary',
     'What to write down and for how long, with a free printable 8-week template in A4 and US Letter.'),
    ('/medication-overuse-headache/', 'Medication-overuse headache',
     'How headache and medicine days are counted, how they compare with NHS advice, and how it is treated.'),
    ('/menstrual-migraine/', 'Menstrual migraine',
     'The window around your period that doctors use, how long to track it, and keeping period data private.'),
    ('/va-migraine-log/', 'Migraine log for a VA claim',
     'What to record about each attack, and where veterans get free help. Migrely is not affiliated with VA.'),
    ('/compare/', 'Compare migraine apps',
     'Android migraine diaries side by side, on facts from each app\'s own listing, website and policy.'),
]
# The other inner pages, for /llms.txt and the breadcrumbs (BRD D147): path, name and a one-sentence summary.
TRUST_PAGES = [
    ('/about/', 'About Migrely', 'Who makes Migrely, how Premium pays for it, and what it will never do.'),
    ('/privacy-check/', 'Check Migrely\'s privacy yourself',
     'How to see on your own phone that Migrely has no internet permission, and what each of its permissions is for.'),
    ('/support/', 'Migrely help',
     'What can leave your phone, Premium prices, moving to a new phone, cancelling, refunds and deleting everything.'),
    ('/privacy/', 'Privacy policy',
     'What Migrely keeps on your phone, Android\'s backup, and how to delete everything.'),
    ('/delete-data/', 'Delete your data',
     'How to delete everything Migrely keeps on your phone, and Android\'s backup of it.'),
]
# Every inner page's name in its breadcrumb trail, "Home › name", shown and in JSON-LD (BRD D147).
CRUMBS = {path: name for path, name, _ in GUIDES + TRUST_PAGES}
# The last line of a reviewed page's sources (BRD D143): the medical pages and the VA page say who did not review them.
MEDICAL_DISCLAIMER = 'not reviewed by a clinician. Educational only, not medical advice.'
VA_DISCLAIMER = 'not reviewed by a lawyer, a clinician or VA. General information, not legal or medical advice.'
NICE_CG150 = 'https://www.nice.org.uk/guidance/cg150/chapter/Recommendations'
MT_MENSTRUAL = 'https://migrainetrust.org/understand-migraine/types-of-migraine/menstrual-migraine/'
DIARY_SOURCES = [
    ('NICE CG150, Headaches in over 12s: recommendations 1.1.3, 1.1.4, 1.2.1 (table 1), 1.2.6, 1.2.7, 1.3.1 and 1.3.27 '
     'to 1.3.29', NICE_CG150),
    ('ICHD-3, How to use the classification', 'https://ichd-3.org/how-to-use-the-classification/'),
    ('ICHD-3, Definition of terms: headache days and prodrome', 'https://ichd-3.org/definition-of-terms/'),
    ('ICHD-3, 1.3 Chronic migraine', ICHD_CHRONIC),
    ('ICHD-3, 8.2 Medication-overuse headache', ICHD_MOH),
    ('NHS, Migraine', NHS_MIGRAINE),
    ('NHS, Headaches', NHS_HEADACHES),
    ('NHS, Meningitis', NHS_MENINGITIS),
    ('The Migraine Trust, Keeping a headache diary',
     'https://migrainetrust.org/live-with-migraine/self-management/keeping-a-migraine-diary/'),
    ('The Migraine Trust, Migraine attack triggers',
     'https://migrainetrust.org/live-with-migraine/self-management/common-triggers/'),
    ('The Migraine Trust, Menstrual migraine', MT_MENSTRUAL),
    ('American Migraine Foundation, Headache journals',
     'https://americanmigrainefoundation.org/resource-library/headache-journals/'),
    ('Hougaard A and others, Neurology, 2013: migraine with aura and natural trigger factors',
     'https://pubmed.ncbi.nlm.nih.gov/23345632/'),
    ('Phillip D and others, Cephalalgia, 2007: clinical interview versus diagnostic headache diary',
     'https://pubmed.ncbi.nlm.nih.gov/17212676/'),
    ('Miller VE and others, Cephalalgia, 2020: daily headache diary versus a four-week questionnaire',
     'https://pubmed.ncbi.nlm.nih.gov/32799667/'),
]
# Migrely's own official profiles, by name and URL (BRD D150). Only a profile that is Migrely's, public and presented as
# Migrely belongs here; it becomes the Organization's sameAs, a footer link and a line in /llms.txt (EC-WEB-06).
SOCIAL_PROFILES = [('YouTube', 'https://www.youtube.com/@migrely'),
                   ('Instagram', 'https://www.instagram.com/migrely.health/')]
# One object for the home page and /about/ (BRD D135).
ORGANIZATION = {'@type': 'Organization', '@id': f'{BASE}/#organization', 'name': 'Migrely', 'url': f'{BASE}/',
                'logo': f'{BASE}/assets/img/icon-512.png', 'email': SUPPORT,
                'contactPoint': {'@type': 'ContactPoint', 'contactType': 'customer support', 'email': SUPPORT},
                **({'sameAs': [url for _, url in SOCIAL_PROFILES]} if SOCIAL_PROFILES else {})}
BY_MIGRELY = {'@id': ORGANIZATION['@id']}
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
    ('“view network connections”', 'android.permission.ACCESS_NETWORK_STATE',
     'From version 1.1.1. When you tap to buy, restore or manage Premium, Migrely checks whether your phone is online, '
     'so it can tell you it is offline instead of opening a Google Play screen that cannot load. It reads this on '
     'your phone and sends nothing. It does not let Migrely go on the internet: Migrely has no internet permission, in '
     'every version.'),
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


def page_ld(path, kind, name, *extra, **props):
    """An inner page's JSON-LD graph (BRD D147): the Organization, the page itself as `kind`, its breadcrumbs and any
    extra nodes. Each page carries the Organization, because search engines read every page on its own."""
    url = f'{BASE}{path}'
    trail = {'@type': 'BreadcrumbList', '@id': f'{url}#breadcrumb', 'itemListElement': [
        {'@type': 'ListItem', 'position': position, 'name': crumb, 'item': item}
        for position, (crumb, item) in enumerate([('Home', f'{BASE}/'), (CRUMBS[path], url)], start=1)]}
    node = {'@type': kind, '@id': f'{url}#webpage', 'url': url, 'name': name, 'inLanguage': 'en',
            'publisher': BY_MIGRELY, 'breadcrumb': {'@id': trail['@id']}, **props}
    return json_ld({'@context': 'https://schema.org', '@graph': [ORGANIZATION, node, trail, *extra]})


def questions_ld(faq):
    """Questions and answers as schema.org Question nodes, for a FAQPage's mainEntity."""
    return [{'@type': 'Question', 'name': question, 'acceptedAnswer': {'@type': 'Answer', 'text': answer}}
            for question, answer, *_ in faq]


def how_to_ld(path, name, steps, **props):
    """Numbered steps as a HowTo named like the page, from the same strings as its visible list (BRD D147)."""
    return {'@type': 'HowTo', '@id': f'{BASE}{path}#howto', 'name': name, **props,
            'step': [{'@type': 'HowToStep', 'position': position, 'text': step}
                     for position, step in enumerate(steps, start=1)]}


def steps_list(steps):
    return '<ol>' + ''.join(f'<li>{esc(step)}</li>' for step in steps) + '</ol>'


def campaign(path):
    """The page a Play link sits on, as its UTM campaign: home, or the path's name (BRD D137)."""
    return path.strip('/').removesuffix('.html') or 'home'


def play_link(page_campaign):
    """The Play listing with the site's UTM tags in referrer, the Play URL builder's format (BRD D137). Play Console
    counts the visits; nothing here tracks the visitor."""
    tags = f'utm_source=migrely.health&utm_medium=website&utm_campaign={page_campaign}'
    return f'{PLAY}&referrer={quote(tags, safe="")}'


def page(path, title, description, body, extra_head='', landing=False):
    canonical = f'{BASE}{path}'
    if landing:
        return landing_page(canonical, title, description, body)
    current = ' aria-current="page"'
    nav = ''.join(
        f'<a href="{href}"{current if href == path else ""}>{label}</a>'
        for href, label in [('/', 'Home'), ('/about/', 'About'), ('/support/', 'Help'), ('/privacy/', 'Privacy')])
    guides = ''.join(f'<a href="{esc(href)}"{current if href == path else ""}>{esc(name)}</a>'
                     for href, name, _ in GUIDES)
    profiles = ''.join(f'<a href="{esc(url)}" rel="me">{esc(name)}</a>' for name, url in SOCIAL_PROFILES)
    trail = (f'<nav class="wrap narrow crumbs" aria-label="Breadcrumb"><ol><li><a href="/">Home</a></li>'
             f'<li><span aria-current="page">{esc(CRUMBS[path])}</span></li></ol></nav>\n' if path in CRUMBS else '')
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
{trail}{body}
</main>
<footer class="foot"><div class="wrap">
  <p>{esc(DISCLAIMER)}</p>
  <nav class="links" aria-label="Guides">{guides}</nav>
  <p class="links"><a href="/support/">Help</a><a href="/about/">About</a><a href="/privacy/">Privacy policy</a>
    <a href="/privacy-check/">Check our privacy</a><a href="/delete-data/">Delete your data</a>
    <a href="{esc(play_link(campaign(path)))}">Google Play</a>{profiles}</p>
  <p>© 2026 Migrely, published by Sulton UzDev · <a href="mailto:{SUPPORT}">{SUPPORT}</a></p>
</div></footer>
</body>
</html>
'''


# The paid-ad landing page (tracker T62, test ig-ads-test1). Meta does not let a Traffic ad link to Google Play, so the
# ad opens /android/ and this page's two Play buttons carry the campaign tags, in plain utm_ parameters and in referrer.
AD_TAGS = 'utm_source=instagram&utm_medium=paid_social&utm_campaign=ig-ads-test1'
AD_PLAY = f'{PLAY}&{AD_TAGS}&referrer={quote(AD_TAGS, safe="")}'
AD_PLAY_EXPECTED = ('https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram'
                    '&utm_medium=paid_social&utm_campaign=ig-ads-test1&referrer=utm_source%3Dinstagram'
                    '%26utm_medium%3Dpaid_social%26utm_campaign%3Dig-ads-test1')
# The site-wide disclaimer minus "Talk to a doctor about your headaches", which would speak to a reader as someone
# with headaches (Meta's personal-attributes policy).
AD_DISCLAIMER = ('Migrely is a diary. It is not a medical device and does not diagnose, treat or prevent any '
                 'condition.')


OG_ANDROID = 'og-android-v1.jpg'
OG_ANDROID_ALT = ('Migraine diary for Android. 3 taps. Migraine logged. Even in a dark room. Migrely keeps the time '
                  'until the end is logged. 1 Started, 2 How bad, 3 Ended.')


def landing_page(canonical, title, description, body):
    """A page for paid traffic: noindex, no site navigation, a short footer. Meta's crawler can still read it, because
    robots.txt does not block it. It is not in the sitemap, and it links nowhere that sells or tracks."""
    return f'''<!doctype html>
<html lang="en" dir="ltr">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>{esc(title)}</title>
<meta name="description" content="{esc(description)}">
<meta name="robots" content="noindex">
<link rel="canonical" href="{canonical}">
<meta name="color-scheme" content="light dark">
<meta name="theme-color" content="#0F1018" media="(prefers-color-scheme: dark)">
<meta name="theme-color" content="#F7F6FC" media="(prefers-color-scheme: light)">
<meta property="og:type" content="website">
<meta property="og:site_name" content="Migrely">
<meta property="og:title" content="{esc(title)}">
<meta property="og:description" content="{esc(description)}">
<meta property="og:url" content="{canonical}">
<meta property="og:image" content="{BASE}/assets/img/{OG_ANDROID}">
<meta property="og:image:type" content="image/jpeg">
<meta property="og:image:width" content="1200">
<meta property="og:image:height" content="630">
<meta property="og:image:alt" content="{esc(OG_ANDROID_ALT)}">
<meta name="twitter:card" content="summary_large_image">
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="icon" href="/favicon-32.png" sizes="32x32" type="image/png">
<link rel="apple-touch-icon" href="/apple-touch-icon.png">
<link rel="preload" href="/assets/fonts/fraunces-600.woff" as="font" type="font/woff" crossorigin>
<link rel="stylesheet" href="/assets/site.css?v={CSS_VERSION}">
<link rel="stylesheet" href="/assets/landing.css?v={LANDING_CSS_VERSION}">
</head>
<body class="ad">
<a class="skip" href="#main">Skip to content</a>
<header class="top"><div class="wrap bar">
  <span class="brand"><img src="/favicon.svg" alt="" width="32" height="32"><span>Migrely</span></span>
</div></header>
<main id="main">
{body}
</main>
<footer class="foot"><div class="wrap">
  <p>{esc(AD_DISCLAIMER)}</p>
  <p class="links"><a href="/privacy/">Privacy policy</a><a href="/privacy-check/">Check our privacy</a></p>
  <p>© 2026 Migrely, published by Sulton UzDev · <a href="mailto:{SUPPORT}">{SUPPORT}</a></p>
</div></footer>
</body>
</html>
'''


def ad_play_button(label='Get it on Google Play'):
    return (f'<a class="play" href="{esc(AD_PLAY)}">'
            '<svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">'
            '<path fill="currentColor" d="M4 2.6v18.8c0 .5.5.8.9.6l16.3-9.4c.4-.2.4-.9 0-1.1L4.9 2.1c-.4-.2-.9.1-.9.5z"/>'
            f'</svg><span>{label}</span></a>')


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
     'days around your period and how you rated your medicines.'),
    ('Gentle on your eyes', 'Dark by default, light if you like, and always dark during a migraine. Large text, '
     'TalkBack and keyboards work throughout.'),
]

# Questions asked before installing, answered on the home page (BRD D147). None may repeat one in FAQ (EC-WEB-21).
HOME_FAQ = [
    ('Is Migrely free?',
     'Yes. Logging, your whole history, medications, reminders, export and every safety warning are free, for good. '
     'Premium is optional: it adds the doctor report and Insights, and its yearly plan starts with a 7-day free '
     'trial.'),
    ('Does Migrely need the internet?',
     'No. Migrely has no internet permission, so Android stops it from connecting. Logging, your history, reminders '
     'and export all work in airplane mode. Only Premium (buying, checking and restoring it) and rating the app go '
     'through Google Play, which goes online.'),
    ('Is Migrely only for Android?',
     f'Yes. Migrely is for Android phones with {MIN_ANDROID} or later, and you install it from Google Play.'),
    ('Is my diary backed up?',
     f'If your phone\'s backup is on, Android backs up Migrely\'s entries to your Google account. {BACKUP_RULE} You '
     'can also export a backup file yourself in Settings, Your data.'),
    ('What does Premium add?',
     'A PDF report for your doctor, with a summary, a calendar and every migraine and medication, and Insights: '
     'triggers often logged with your migraines, the days and hours they come, the days around your period and how '
     'you rated your medicines. Safety warnings are never Premium.'),
]

# Questions asked once Migrely is installed, answered on /support/ (BRD D147): question, answer, and an optional
# link to read more.
FAQ = [
    ('What can leave my phone?',
     'Migrely itself sends nothing: it has no internet permission, so Android stops it from connecting. Three things '
     'are up to Android, Google Play or you. Android\'s own backup may copy Migrely\'s entries to your Google account. '
     f'{BACKUP_RULE} Google Play handles buying Premium and ratings, under Google\'s privacy policy. And a backup, '
     'spreadsheet or PDF report goes wherever you choose to send it.',
     ('Check Migrely\'s privacy yourself', '/privacy-check/')),
    ('What does Premium cost?',
     f'Premium is {PREMIUM_PRICES}. The yearly plan\'s free trial is for new subscribers. Google Play shows the '
     'price in your currency before you pay and handles the payment, so we never see your card. Premium adds the '
     'doctor report and Insights; everything else is free.'),
    ('How do I move my diary to a new phone?',
     'Use Android\'s own transfer or a backup file. When you set up the new phone from the old one, by cable or nearby '
     'transfer, Android brings Migrely\'s entries with it. It can also restore them from your Google account\'s '
     'backup, which on Android 9 and later exists only if the old phone had a screen lock. Otherwise, on the old '
     'phone open Settings, Your data, Export a backup (JSON), copy the file to the new phone, and choose Import a '
     'backup in the same place there.'),
    ('How do I restore Premium on a new phone?',
     'Install Migrely with the Google account you bought Premium with, and it is found at the first start. If it is '
     'not, open Settings, Premium, Restore purchases, which checks Google Play again for what that account bought. '
     'Premium belongs to your Google account, not to the phone.'),
    ('How do I cancel my subscription?',
     'In Google Play: Profile, Payments and subscriptions, Subscriptions, Migrely Premium, Cancel. Cancel before a '
     'free trial ends and you are not charged. After you cancel, Premium stays on until the trial or the period you '
     'paid for ends. Your entries are never deleted.',
     ('Google Play: cancel a subscription', 'https://support.google.com/googleplay/answer/7018481')),
    ('Can I get a refund?',
     'Refunds follow Google Play\'s refund policies, because Google Play handles the payment, so ask Google Play for '
     'one. If Google Play refunds and removes the purchase, Premium turns off at Migrely\'s next check; your entries '
     'stay.',
     ('Google Play\'s refund policies', 'https://support.google.com/googleplay/answer/2479637')),
    ('How do I delete everything?',
     'In Migrely, open Settings, Delete all data, and type DELETE to confirm. Everything Migrely keeps on your phone '
     'is removed at once. Android\'s backup of it is replaced the next time your phone backs up, and deleting does '
     'not cancel a subscription.',
     ('Delete your data', '/delete-data/')),
    ('Is Migrely a medical device?',
     'No. Migrely is a diary that helps you keep a record and talk to your doctor. It does not diagnose or treat. If a '
     'headache comes with any of the emergency signs that our guides list, call your local emergency number.',
     ('When to get help straight away', '/headache-diary/')),
]


def home():
    shots = ''.join(
        f'<figure><img src="/assets/img/{name}.webp" alt="{esc(alt)}" width="360" height="640" loading="lazy"></figure>'
        for name, alt in [('01-log', 'Attack mode: Rest now, we will keep time'),
                          ('02-sky-v2', 'Your month as a calm night sky'),
                          ('03-report-v2', 'A clear report for your doctor'),
                          ('04-insights', 'Triggers and patterns in your migraines')])
    features = ''.join(f'<article><h3>{esc(t)}</h3><p>{esc(d)}</p></article>' for t, d in FEATURES)
    scenes = ''.join(f'<li>{esc(scene)}</li>' for scene in PROMO_SCENES)
    guides = ''.join(f'<article><h3><a href="{esc(href)}">{esc(name)}</a></h3><p>{esc(summary)}</p></article>'
                     for href, name, summary in GUIDES)
    questions = ''.join(f'<h3>{esc(question)}</h3><p>{esc(answer)}</p>' for question, answer in HOME_FAQ)
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

<section class="wrap block measure"><h2>What is Migrely?</h2><p>{esc(WHAT_IS)}</p></section>

<section class="wrap block video"><h2 id="video-title">See Migrely in 30 seconds</h2>
  <figure>
    <video controls preload="none" playsinline width="1280" height="720" poster="/assets/video/{PROMO_VIDEO[1]}"
      aria-labelledby="video-title" aria-describedby="video-scenes"><source src="/assets/video/{PROMO_VIDEO[0]}"
      type="video/mp4"></video>
    <figcaption id="video-scenes"><p>Migrely's screens with sample data, with music and no speech. What each scene
      shows and says:</p><ol>{scenes}</ol>
      <p><a href="/assets/video/{PROMO_VIDEO[0]}">Download the video (MP4, {PROMO_SIZE})</a></p></figcaption>
  </figure>
</section>

<section class="shots" aria-label="Screens from Migrely"><div class="wrap strip">{shots}</div></section>

<section class="wrap block"><h2>Everything a migraine diary should be</h2><div class="features">{features}</div></section>

<section class="band"><div class="wrap block">
  <h2>Private by design</h2>
  <ul class="ticks">
    <li>Migrely has no internet access. Nothing you enter can leave your phone through the app.</li>
    <li>No account to make, no tracking, no ads.</li>
    <li>Your entries stay on your phone, and in Android's own backup if it is on: on Android 9 and later only
      end-to-end encrypted, which needs a screen lock.</li>
    <li>Export a backup or a spreadsheet, or delete everything, whenever you like.</li>
  </ul>
  <p><a class="quiet" href="/privacy-check/">Check it yourself in two minutes</a></p>
</div></section>

<section class="wrap block"><h2>Free guides</h2><div class="features">{guides}</div></section>

<section class="wrap block two">
  <div><h2>Free, and Premium when you want more</h2>
    <p>Logging, your whole history, medications, reminders, export and every safety warning are free, for good.
      Premium adds the doctor report and Insights, with a 7-day free trial on the yearly plan.</p></div>
  <div><h2>Safety first</h2>
    <p>The warning signs that need urgent care are always in Settings. Migrely is a diary, not a medical device: it
      helps you keep a record and talk to your doctor.</p></div>
</section>

<section class="wrap block measure faq"><h2>Questions before you install</h2>{questions}
  <p>More answers, from moving to a new phone to cancelling, are on the <a href="/support/">help page</a>.</p></section>

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
               'publisher': BY_MIGRELY}
    video = {'@type': 'VideoObject', 'name': 'Migrely in 30 seconds',
             'description': 'Migrely\'s screens with sample data: logging a migraine in three taps, the month as a sky, the doctor '
                            'report and Insights (Premium), and Android\'s permission list with no internet permission.',
             'thumbnailUrl': f'{BASE}/assets/video/{PROMO_VIDEO[1]}', 'contentUrl': f'{BASE}/assets/video/{PROMO_VIDEO[0]}',
             'uploadDate': PROMO_UPLOADED, 'duration': 'PT30S', 'publisher': BY_MIGRELY}
    faq = {'@type': 'FAQPage', '@id': f'{BASE}/#questions', 'url': f'{BASE}/', 'name': 'Questions before you install',
           'inLanguage': 'en', 'isPartOf': {'@id': website['@id']}, 'publisher': BY_MIGRELY,
           'mainEntity': questions_ld(HOME_FAQ)}
    extra = json_ld({'@context': 'https://schema.org', '@graph': [ORGANIZATION, website, app, video, faq]})
    return page('/', 'Migrely: Migraine Tracker & Private Headache Diary',
                'Log migraines in seconds, see your patterns and share a doctor-ready PDF. A private migraine diary '
                'for Android: no account, no internet, no ads.', body, extra)


def support():
    path = '/support/'
    items = ''.join(f'<h2>{esc(question)}</h2><p>{esc(answer)}</p>'
                    + ''.join(f'<p><a href="{esc(url)}">{esc(label)}</a></p>' for label, url in more)
                    for question, answer, *more in FAQ)
    body = f'''<section class="wrap block narrow prose"><h1>Migrely help</h1>
  <p class="lead">Answers about privacy, backups, Premium and cancelling. Questions asked before installing are on the
    <a href="/">home page</a>. Anything else: write to <a href="mailto:{SUPPORT}">{SUPPORT}</a>.</p>
  {items}</section>'''
    ld = page_ld(path, 'FAQPage', 'Migrely help', mainEntity=questions_ld(FAQ))
    return page(path, 'Migrely help: privacy, backups, Premium and cancelling',
                'Answers about Migrely: what can leave your phone, what Premium costs, moving to a new phone, '
                'restoring Premium, cancelling, refunds and deleting your data.', body, ld)


# The steps on /delete-data/, shown and as a HowTo (BRD D147).
DELETE_STEPS = [
    'Open Migrely, then Settings.',
    'Choose Delete all data at the end of the list.',
    'Type DELETE and confirm. Every entry, setting and copy Migrely keeps on the phone is removed, and the app starts '
    'fresh.',
]


def delete_data():
    path = '/delete-data/'
    body = f'''<section class="wrap block narrow"><h1>Delete your data</h1>
  <p class="lead">Migrely keeps your entries only on your phone, so you delete them on your phone. We hold nothing
    about you that we could delete for you.</p>
  <h2>In the app</h2>
  {steps_list(DELETE_STEPS)}
  <h2>Android's own backup</h2>
  <p>Android's backup of Migrely is replaced the next time your phone backs up, usually within a day. You can also
    delete your phone's backup in your Google account's backup settings.</p>
  <h2>Premium</h2>
  <p>Deleting your data does not cancel a subscription. Cancel it in Google Play: Profile, Payments and subscriptions,
    Subscriptions.</p>
  <p>Questions: <a href="mailto:{PRIVACY_MAIL}">{PRIVACY_MAIL}</a></p></section>'''
    steps = how_to_ld(path, 'Delete your data', DELETE_STEPS)
    ld = page_ld(path, 'WebPage', 'Delete your data', steps, mainEntity={'@id': steps['@id']})
    return page(path, 'Delete your Migrely data and Android\'s backup of it',
                'How to delete everything Migrely keeps on your phone, and Android\'s backup of it. We never receive '
                'your entries.', body, ld)


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
  <h2>Where do the medication day counts come from?</h2>
  <p>Migrely is a diary, not a medical device. Its medication note uses the day counts for
    <a href="/medication-overuse-headache/">medication-overuse headache</a> in the International Classification of
    Headache Disorders (ICHD-3): medicine on 15 or more days a month with simple painkillers alone, or on 10 or more
    with triptans, combination painkillers, other medicines for attacks, or a mix of kinds in total, for more than 3
    months. Doctors use these counts, with headache on 15 or more days a month, to diagnose it. The NHS suggests trying
    not to take painkillers on more than 2 days a week. Migrely counts your days over the last 30 and, once you reach
    one of these counts, suggests talking to your doctor. It does not diagnose.</p>
  <h2>How can I contact you?</h2>
  <p>Help and questions: <a href="mailto:{SUPPORT}">{SUPPORT}</a>. Privacy:
    <a href="mailto:{PRIVACY_MAIL}">{PRIVACY_MAIL}</a>. Answers to common questions are on the
    <a href="/support/">help page</a>.</p>
  <p>{play_button(path)}</p></section>'''
    return page(path, 'About Migrely: who makes it and how it is paid for',
                'Migrely is a migraine diary paid for by Premium, not by your data: no ads, no account and no internet '
                'access. Who makes it, and why.', body, page_ld(path, 'AboutPage', 'About Migrely', about=BY_MIGRELY))


# The phone check on /privacy-check/, shown and as a HowTo (BRD D147).
CHECK_STEPS = [
    'Open your phone\'s Settings, then Apps, then Migrely.',
    'Tap Permissions.',
    'Tap the ⋮ menu at the top, then All permissions.',
    'Look for “have full network access”. Apps that use the internet list it. Migrely does not.',
]


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
  {steps_list(CHECK_STEPS)}
  <p>The names can differ a little between phone makers. The check for developers below is the same on every
    phone.</p>
  <h2>Does airplane mode prove it?</h2>
  <p>It shows half of it. Turn on airplane mode and Migrely works as usual: logging, your history, reminders and export
    all work offline. That shows Migrely does not need the internet; the permission list shows it cannot use it.
    Buying or restoring Premium, and rating Migrely, need Google Play to be online.</p>
  <h2>What are Migrely's {count} permissions for?</h2>
  <p>These are all the permissions Migrely asks for. Those in quotes appear in the list under those names. On
    phones with Google Play, billing may be listed too. “View network connections” is asked for only from version
    1.1.1; earlier versions ask for the other five.</p>
  <p>“View network connections” is not the internet permission. It only lets an app see whether the phone is online;
    it cannot connect. “Have full network access” is the internet permission, and Migrely does not have it.</p>
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
  <p>It prints the permissions above ({count} from version 1.1.1, one fewer before). To look for the internet permission alone, run:</p>
  <pre><code>adb shell dumpsys package com.migrely.app | grep INTERNET</code></pre>
  <p>It prints nothing.</p>
  <p>{play_button(path)}</p></section>'''
    steps = how_to_ld(path, 'Check Migrely\'s privacy yourself', CHECK_STEPS, totalTime='PT2M')
    ld = page_ld(path, 'WebPage', 'Check Migrely\'s privacy yourself', steps, mainEntity={'@id': steps['@id']})
    return page(path, 'Check Migrely\'s privacy yourself · Migrely',
                'Migrely has no internet permission. Check it on your own phone in two minutes, and see what each of '
                f'its {count} permissions is for.', body, ld)


# Copy for /android/, kept in constants so a reviewer's wording change is one edit (T62, UX spec section 3).
AD_SAMPLE = 'Sample data, not a real patient. Not a diagnosis.'
AD_BACKUP = 'Android\'s own backup may copy entries to a Google account, and exported files go wherever they are sent.'
AD_DIARY = 'It doesn\'t diagnose or treat migraine.'
AD_PREMIUM_TERMS = ('Premium is a yearly subscription with a 7-day free trial for new subscribers, a monthly '
                    'subscription, or a single payment. Subscriptions renew until cancelled in Google Play. Google Play '
                    'shows the price in your local currency before you pay.')
AD_PREMIUM_BUY = 'Premium is bought through Google Play, which goes online for the purchase. Migrely itself does not.'
AD_PERMS_NOTE = 'Part of Android\'s permission screen for Migrely. The list continues below.'
AD_QR_NOTE = 'Scan with an Android phone\'s camera to open Migrely on Google Play.'
ALT_HOME = ('Migrely\'s Home screen with sample data: Monday, October 26. Calm so far today. No migraine logged. '
            'Period. October.')
ALT_SKY = ('Migrely\'s month view with sample data: 9:41 AM \u2013 1:20 PM, 6 out of 10, then October, days 1 to 24. '
           'Each day\'s tile shows what was logged, as stars, a cloud, a storm or waves; days not logged are empty '
           'outlines.')
ALT_PERMS = ('Android\'s permission screen for Migrely: All permissions. Migrely. Notifications: show notifications. '
             'Other app capabilities. Top of the list.')
MIN_ANDROID_NUMBER = MIN_ANDROID.split()[1]
AD_FAQ = [
    ('Which phones does Migrely work on?',
     f'Android phones with {MIN_ANDROID} or later and Google Play. There is no iPhone version.'),
    ('Is there an account?', 'No. There is no account to make and nothing to sign in to.'),
    ('Where are the entries kept?',
     f'On the phone. Migrely has no internet permission, so Android stops it from connecting. Android\'s own backup '
     f'may copy entries to a Google account. {BACKUP_RULE} Exported files go wherever they are sent.'),
]
AD_FAQ_PLAY = ('What if Google Play doesn\'t open?',
               'The buttons open Migrely\'s listing on Google Play. If it doesn\'t load, Google Play may be blocked on '
               'this network or device, or Migrely may not be offered in this country. Search for \u201cMigrely\u201d '
               'in the Google Play app, or write to ')
QR_FILE = 'qr-ig-ads-test1-v1.svg'


def android_landing():
    """/android/: where the Instagram ad test ig-ads-test1 sends people (tracker T62). Every sentence comes from the
    ad pack (headline, primary text, on-screen cards, alt text) or the site's own approved privacy lines, or is listed
    for review in migrely-work/landing/ux-spec.md."""
    path = '/android/'
    questions = ''.join(f'<h3>{esc(q)}</h3><p>{esc(a)}</p>' for q, a in AD_FAQ)
    questions += (f'<h3>{esc(AD_FAQ_PLAY[0])}</h3><p>{esc(AD_FAQ_PLAY[1])}<a href="mailto:{SUPPORT}">{SUPPORT}</a>.</p>')
    body = f'''
<section class="ad-hero" id="hero" aria-labelledby="hero-h"><div class="wrap">
  <div class="hero-copy">
    <p class="eyebrow">Migraine diary for Android</p>
    <h1 id="hero-h"><span class="h1-a">3 taps.</span> <span class="h1-b">Migraine logged.</span></h1>
    <p class="lead">Even in a dark room. Migrely keeps the time until the end is logged.</p>
    {ad_play_button()}
    <p class="meta">Free to log on Google Play<br>For Android {MIN_ANDROID_NUMBER} and later. Not available for iPhone.</p>
    <div class="qr"><img src="/assets/img/{QR_FILE}" alt="QR code for Migrely on Google Play" width="160" height="160" loading="lazy">
      <p>{esc(AD_QR_NOTE)}</p></div>
  </div>
  <figure class="window">
    <img src="/assets/img/ad1-home-v1.webp" width="880" height="619" alt="{esc(ALT_HOME)}">
    <figcaption>{AD_SAMPLE}</figcaption>
  </figure>
</div></section>

<div class="rest">
<section class="ad-sec" id="steps" aria-labelledby="steps-h"><div class="wrap stack">
  <h2 id="steps-h">3 taps log an attack, even in a dark room.</h2>
  <ol class="steps" role="list">
    <li><span class="num">1</span><h3>Started</h3><p>\u201cMigraine started\u201d, then \u201cRest now. We'll keep time.\u201d</p></li>
    <li><span class="num">2</span><h3>How bad</h3><p>Mild, Moderate or Severe</p></li>
    <li><span class="num">3</span><h3>Ended</h3><p>Later, when it's over: \u201cMigraine ended\u201d</p></li>
  </ol>
</div></section>

<section class="ad-sec" id="sky" aria-labelledby="sky-h"><div class="wrap split">
  <div class="stack"><p class="eyebrow">Every month</p>
    <h2 id="sky-h">Each month, drawn as a <span class="accent">night sky.</span></h2>
    <p class="lead">Each day shows what was logged.</p></div>
  <figure class="window"><img src="/assets/img/ad1-sky-v1.webp" width="880" height="619" loading="lazy" alt="{esc(ALT_SKY)}">
    <figcaption>{AD_SAMPLE}</figcaption></figure>
</div></section>

<section class="ad-sec band" id="privacy" aria-labelledby="privacy-h"><div class="wrap split">
  <div class="stack"><p class="eyebrow">Private by design</p>
    <h2 id="privacy-h">No internet permission.</h2>
    <p>Android lists every app's permissions. The internet permission, shown as \u201chave full network access\u201d, is not on Migrely's list.</p>
    <ul class="ticks" role="list"><li>No account</li><li>No ads</li></ul>
    <p>{esc(AD_BACKUP)}</p>
    <p>Check it yourself: <a href="/privacy-check/">migrely.health/privacy-check</a></p></div>
  <figure class="window"><img src="/assets/img/ad1-perms-v1.webp" width="880" height="655" loading="lazy" alt="{esc(ALT_PERMS)}">
    <figcaption>{esc(AD_PERMS_NOTE)}</figcaption></figure>
</div></section>

<section class="ad-sec" id="plans" aria-labelledby="plans-h"><div class="wrap stack">
  <h2 id="plans-h">Free, and Premium</h2>
  <div class="plans">
    <article class="plan"><h3>Free</h3><ul class="ticks" role="list"><li>Logging</li><li>The full history</li><li>Medications</li><li>Reminders</li><li>Backup and spreadsheet export</li></ul>
      <p class="meta">Safety warnings are never Premium.</p></article>
    <article class="plan premium"><h3>Premium</h3><ul class="ticks" role="list"><li>The doctor report</li><li>Insights</li></ul>
      <p>{esc(AD_PREMIUM_TERMS)}</p>
      <p class="meta">{esc(AD_PREMIUM_BUY)}</p></article>
  </div>
</div></section>

<section class="ad-sec" id="diary" aria-labelledby="diary-h"><div class="wrap stack">
  <h2 id="diary-h">It's a diary.</h2><p class="lead">{esc(AD_DIARY)}</p>
</div></section>

<section class="ad-sec" id="faq" aria-labelledby="faq-h"><div class="wrap stack faq">
  <h2 id="faq-h">Questions</h2>{questions}
</div></section>

<section class="final-cta" id="final" aria-labelledby="final-h"><div class="wrap stack">
  <h2 id="final-h">Private migraine diary for Android</h2><p class="meta">Free to log on Google Play</p>
</div></section>
<div class="dock">{ad_play_button()}</div>
</div>
'''
    return page(path, 'Migrely: private migraine diary for Android',
                '3 taps log an attack, even in a dark room. No account, no internet permission. Free to log on '
                'Google Play.', body, landing=True)


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
    path = '/privacy/'
    return page(path, 'Migrely privacy policy: what it keeps and where',
                'Migrely has no internet access and no account. What it keeps on your phone, Android\'s backup, and '
                'how to delete everything.', body, page_ld(path, 'WebPage', 'Migrely privacy policy'))


def urgent_help(extra=''):
    """The same urgent-help section on the medical pages and the VA page, with an optional extra line. It carries
    the app's Warning signs page word for word (BRD D171)."""
    return f'''<h2>When should I get help straight away?</h2>
  <h3>Call your local emergency number if a headache comes with any of these:</h3>
  <ul><li>Extremely bad pain that came on suddenly</li>
    <li>Weakness or loss of movement on one side of your body or face</li>
    <li>Problems speaking or remembering things</li>
    <li>Losing your vision, or blurred or double vision</li>
    <li>Feeling drowsy or confused</li>
    <li>A seizure or fit</li>
    <li>A very high temperature and signs of meningitis, such as a stiff neck</li>
    <li>A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or black skin)</li>
    <li>A head injury in the last 3 months</li></ul>
  <p>Don't drive yourself to hospital. Call your local emergency number.</p>
  <h3>Get urgent medical advice if you have a migraine attack and:</h3>
  <ul><li>It has lasted longer than 72 hours</li>
    <li>Aura symptoms last longer than 1 hour at a time</li>
    <li>You're pregnant or have just had a baby</li></ul>
  <p>If you have any other symptoms you're worried about, get urgent medical advice.</p>
  <p>These lists follow the NHS <a href="{NHS_MIGRAINE}">migraine</a>, <a href="{NHS_HEADACHES}">headaches</a> and
    <a href="{NHS_MENINGITIS}">meningitis</a> pages. In the UK, urgent medical advice means an urgent GP appointment
    or NHS 111.</p>{extra}'''


def reviewed_block(sources, reviewed, disclaimer=MEDICAL_DISCLAIMER):
    """A page's sources and the date its facts were last checked against them."""
    items = ''.join(f'<li><a href="{esc(url)}">{esc(name)}</a></li>' for name, url in sources)
    return f'''<h2>Sources</h2>
  <ul>{items}</ul>
  <p class="meta">Last reviewed: {reviewed[1]}. Written by Migrely from the sources above; {disclaimer}</p>'''


def medical_ld(path, name, reviewed, condition):
    """A medical page's MedicalWebPage JSON-LD, dated like its "Last reviewed" line and written by Migrely, as it says.
    It names no reviewer, because no clinician reviewed it (BRD D147)."""
    return page_ld(path, 'MedicalWebPage', name, lastReviewed=reviewed[0], about=condition, author=BY_MIGRELY)


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
    talk about. The Migraine Trust adds that it can show patterns in your attacks and help you recognise triggers and
    warning signs.</p>
  <h2>What should I write down?</h2>
  <p>For each headache, write the date, when it started and stopped, how bad it was and any other symptoms, such as
    nausea, or light or sound bothering you. Add every medicine you took for it, prescribed or bought, and whether it
    helped. NICE also suggests noting possible triggers and any link to your period.</p>
  <p>NICE's list in full:</p>
  <ul><li>how often headaches come, how long they last and how bad they get;</li>
    <li>other symptoms, such as nausea, or light or sound bothering you;</li>
    <li>every medicine you take for them, prescribed and over the counter;</li>
    <li>possible triggers;</li><li>any link to your period, if you have periods.</li></ul>
  <p>Rate how bad each headache was as mild, moderate or severe, or on a scale up to 10, where 10 is the worst pain you
    can imagine.</p>
  <h2>Which possible triggers should I note?</h2>
  <p>Note changes from your usual routine: sleep, meals, caffeine and alcohol, stress, the weather, your period, and
    bright light or noise. But a trigger seen alongside an attack is not proof that it caused it. Some “triggers” are
    early symptoms of the attack itself, and several factors often act together.</p>
  <ul><li><strong>Early symptoms can look like triggers.</strong> Hours or up to 2 days before the pain, some people
      crave certain foods or become more sensitive to light. If you eat chocolate because of a craving, chocolate can
      look like a trigger when it isn't.</li>
    <li><strong>Factors add up.</strong> One thing on its own may be less likely to cause an attack, but two or three
      together may. A real trigger may not cause an attack every time.</li>
    <li><strong>Beliefs don't always hold up.</strong> In a 2013 lab study, 27 people with migraine with aura who said
      bright or flickering light or hard exercise set off their attacks were exposed to them. 6 of the 27 had a
      migraine attack afterwards.</li></ul>
  <p>So don't cut things out on a hunch: the Migraine Trust warns that focusing on triggers too much can do more harm
    than good. A diary may also show that something you blamed matters less than you thought.</p>
  <h2>How long should I keep it?</h2>
  <p>At least 8 weeks, NICE suggests. If headaches seem linked to your period, keep it for at least 2 menstrual cycles;
    the Migraine Trust suggests at least 3 months (<a href="/menstrual-migraine/">more on menstrual migraine</a>). For
    frequent headaches, the international classification (ICHD-3) says a day-by-day diary for at least a month is
    generally needed.</p>
  <h2>How do I keep a headache diary going?</h2>
  <p>Keep it simple, and write in it every day rather than from memory later. In a 2020 study, people remembered fewer
    headache days at the end of each month than their daily diaries showed. Paper, a phone app or a spreadsheet all
    work: the best one is the one you will keep using.</p>
  <ul><li><strong>Keep it short.</strong> The Migraine Trust says basic information is often best. If the diary starts
      to feel overwhelming, the American Migraine Foundation suggests asking your doctor how to simplify it.</li>
    <li><strong>Write it on the day.</strong> That 2020 study used a US trial of 182 people with migraine. A monthly
      questionnaire found fewer headache days than a daily diary in every 4-week period: 11.1 against 13.7 on
      average.</li>
    <li><strong>Daily is the default.</strong> ICHD-3 says frequent headaches generally need a day-by-day record.
      Your doctor may prefer something simpler, such as entries only on days with symptoms, the American Migraine
      Foundation says.</li></ul>
  <h2>What counts as a headache day?</h2>
  <p>Any day with a headache counts as a headache day, whether the headache lasted all day or only for part of it. That
    is the definition in the international classification (ICHD-3), and doctors usually count your headache days over a
    month.</p>
  <p>Fill in a line on pain-free days too, so a blank line never leaves you guessing whether it was a good day or a
    forgotten one.</p>
  <h2>What if I have more than one kind of headache?</h2>
  <p>You can have more than one kind, such as migraine and tension-type headache, and each is diagnosed separately. The
    international classification (ICHD-3) highly recommends a diary that records the main features of every headache.
    It helps a doctor tell the kinds apart and count each one, and helps you learn the difference yourself.</p>
  <p>For each headache, note:</p>
  <ul><li>where the pain is;</li><li>what it feels like, for example throbbing or pressing;</li>
    <li>how bad it is;</li><li>whether routine activity makes it worse;</li><li>other symptoms;</li>
    <li>how long it lasts.</li></ul>
  <p>These are the features NICE uses to tell the main kinds apart, and your doctor makes the diagnosis. A 2007 Danish
    study comparing diaries with clinical interviews found the diary valuable for telling migraine from tension-type
    headache. Keep a row for every headache day, whatever the kind: for chronic migraine, migraine-like and
    tension-type-like days are counted together.</p>
  <h2>Why count medicine days?</h2>
  <p>Taking medicine for attacks on many days a month can itself make headaches more frequent. This is called
    medication-overuse headache. The international classification (ICHD-3) counts the days a month you take each kind
    of medicine, not the number of doses, so a day with two doses counts once.</p>
  <p>ICHD-3 describes it in people who have headache on 15 or more days a month after more than 3 months of
    taking:</p>
  <ul><li>triptans, ergots, opioids or combination painkillers (such as those with caffeine or codeine) on 10 or more
      days a month;</li>
    <li>a mix of these kinds, or of these and simple painkillers, on 10 or more days a month in total;</li>
    <li>simple painkillers alone, such as paracetamol, aspirin or ibuprofen, one or several, on 15 or more days a
      month.</li></ul>
  <p>These counts are for diagnosis; the everyday advice is lower: the NHS suggests not taking painkillers on more than
    2 days a week. Don't stop or change medicines without talking to your doctor.
    <a href="/medication-overuse-headache/">More on medication-overuse headache</a>.</p>
  <h2>What does a doctor look for in a headache diary?</h2>
  <p>A doctor can use your diary to see what kind of headache you have, how many headache days you have a month, how
    often you take medicine for them, and whether treatment is helping. The counts matter because diagnoses such as
    chronic migraine and medication-overuse headache are defined by days a month.</p> <ul><li><strong>How
    often.</strong> Migraine on fewer than 15 days a month is called episodic. Chronic migraine means headache on 15 or
    more days a month for more than 3 months, with migraine features on at least 8 days a month (NICE, ICHD-3).</li>
    <li><strong>Medicine days.</strong> ICHD-3 notes that medication overuse is the most common cause of symptoms that
      look like chronic migraine, so the two counts are read together. A day with two kinds of medicine counts under
      each kind, and once in the total of days with any medicine.</li>
    <li><strong>Whether treatment works.</strong> NICE suggests reviewing preventive treatment 3 to 6 months after it
      starts, and for some preventive treatments it links continuing them to how far migraines or headache days
      fall.</li></ul>
  <p>Before an appointment, add up each month's headache days and medicine days by kind, and list your questions.
    Page 6 of the template has boxes for these counts.</p>
  {urgent_help()}
  <h2>What's in the template?</h2>
  <p>The free template is a 6-page PDF that covers 8 weeks. Page 1 explains how to use it and lists the warning signs
    that need urgent care. Pages 2 to 5 have one row for each day, two weeks to a page. Page 6 is a summary for your
    doctor, with medicine days counted by kind.</p>
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
    ('ICHD-3, 8.2 Medication-overuse headache, with 8.2.2, 8.2.3, 8.2.5, 8.2.6 and 8.2.8', ICHD_MOH),
    ('ICHD-3, 1.3 Chronic migraine', ICHD_CHRONIC),
    ('NICE CG150, Headaches in over 12s: recommendations 1.1.4, 1.2.7, 1.3.6, 1.3.17 and 1.3.42 to 1.3.48', NICE_CG150),
    ('NHS, Migraine', NHS_MIGRAINE),
    ('NHS, Headaches', NHS_HEADACHES),
    ('NHS, Meningitis', NHS_MENINGITIS),
    ('The Migraine Trust, Medication overuse headache',
     'https://migrainetrust.org/understand-migraine/types-of-migraine/medication-overuse-headache/'),
    ('American Migraine Foundation, Medication overuse headache',
     'https://americanmigrainefoundation.org/resource-library/medication-overuse-headache/'),
    ('American Migraine Foundation, Medication overuse headache: a patient guide (PDF)',
     'https://americanmigrainefoundation.org/wp-content/uploads/2024/09/2203_AMF_Medication_Overuse_V5_Digital.pdf'),
    ('Diener HC and others, The Lancet Neurology, 2019: medication overuse headache (abstract)',
     'https://pubmed.ncbi.nlm.nih.gov/31174999/'),
    ('Alstadhaug KB and others, Pain Reports, 2017: preventing and treating medication overuse headache',
     'https://pmc.ncbi.nlm.nih.gov/articles/PMC5741367/'),
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
  <p>Medication-overuse headache is headache on 15 or more days a month that comes from taking medicine for headache
    too often, in someone who already has a headache disorder such as migraine. It used to be called rebound headache.
    Doctors class it as a secondary headache, meaning one with another cause, and it can be diagnosed alongside chronic
    migraine.</p>
  <p>Medicine that eases an attack, taken on many days, can make headaches more frequent. The American Migraine
    Foundation describes the cycle: more pain leads to more medicine, and the medicine itself can then worsen the
    headache and how often it comes.</p>
  <h2>How common is it, and who gets it?</h2>
  <p>About 1 to 2 in every 100 people have medication-overuse headache in a given year, a 2019 review in The Lancet
    Neurology says. It is more common in women and in people with depression, anxiety or other long-term pain. Most
    people who get it already have migraine or tension-type headache, ICHD-3 says.</p>
  <p>It is very common among people with frequent headache: ICHD-3 says studies from many countries show that more than
    half of people with headache on 15 or more days a month have it. A 2017 review puts it at 3 to 4 times as common in
    women as in men.</p>
  <h2>What are the signs?</h2>
  <p>The main sign is headache on most days while you are taking medicine for headache often. The Migraine Trust
    describes a dull headache on most days, with migraine attacks on top, and pain that comes back as each dose wears
    off. Only a doctor can diagnose it, because its headaches have no unique features.</p>
  <p>UK guidance (NICE) asks doctors to think of it when headache started or got worse while someone took medicine for
    headache on 10 or 15 days a month, depending on the kind, for 3 months or more. Other sources mention:</p>
  <ul><li>preventive treatment that works less well than expected (the Migraine Trust);</li>
    <li>taking medicine in case a headache comes, rather than for one you already have (a 2017 review);</li>
    <li>needing medicine on more than 2 days a week as headaches become more frequent, which the American Migraine
      Foundation calls a reason to see your doctor.</li></ul>
  <p>If this sounds familiar, see a GP. Take a record of your headache days and of every medicine you take for them,
    prescribed or bought, plus any you take for other pain. The American Migraine Foundation stresses that it is not
    your fault: overuse can be a sign that migraine is not fully managed.</p>
  <h2>How many days count as overuse?</h2>
  <p>ICHD-3, the international classification, counts days a month, kept up for more than 3 months. Triptans, ergots,
    opioids and combination painkillers count from 10 days a month, and so does a mix of kinds in total. Simple
    painkillers alone, such as paracetamol or ibuprofen, count from 15 days a month.</p>
  <table><caption>ICHD-3: days a month, kept up for more than 3 months</caption>
    <thead><tr><th scope="col">Medicine for the attack</th><th scope="col">Days a month</th></tr></thead>
    <tbody>{rows}</tbody></table>
  <p>Simple painkillers are one kind, so paracetamol on some days and ibuprofen on others still count toward 15, not
    10.</p>
  <h2>Does it count doses or days?</h2>
  <p>Days, not doses. ICHD-3 counts the days each month you take a medicine. A day with two doses counts once. A day
    with a triptan and paracetamol counts once for each kind, and once in the total. Days you take these medicines for
    other pain, such as period or back pain, count too.</p>
  <h2>How do these counts compare with everyday advice?</h2>
  <p>The everyday advice is lower. These are the counts doctors use to diagnose it, not a safe amount. The NHS suggests not taking painkillers on
    more than 2 days a week, and NICE asks doctors to explain the risk to anyone using medicine for headache
    attacks.</p>
  <p>ICHD-3 itself says its day counts come from expert opinion rather than formal evidence.</p>
  <h2>How can I lower my risk?</h2>
  <p>The NHS suggests trying not to take painkillers on more than 2 days a week, because more can cause more headaches
    and make migraine harder to treat. Count the days you take medicine for any pain, not just headache. If attacks are
    frequent, ask a doctor about preventive treatment, so fewer attacks need treating.</p>
  <p>For very frequent attacks, the Migraine Trust says a preventive treatment is usually recommended, because fewer
    attacks mean fewer painkillers. NICE asks doctors to talk through the benefits and risks of preventive treatment
    for migraine, including how much headaches affect your life. The American Migraine Foundation notes that preventive
    medicines have not been linked to medication-overuse headache. The NHS suggests seeing a GP if you have migraine
    attacks more than once a week.</p>
  <h2>How is it treated?</h2>
  <p>UK guidance (NICE) says treatment means stopping the overused medicine for at least a month; the Migraine Trust
    says to agree a plan with your doctor first. Headaches are likely to get worse before they improve, and there may
    be withdrawal symptoms, so doctors offer close follow-up and support, and a review 4 to 8 weeks after
    stopping.</p>
  <p>Doctors may also offer preventive treatment for the underlying headache. NICE says a hospital stay is not
    routinely needed. People who take strong opioids, have other relevant health conditions, or have tried to stop
    several times without success may need a specialist, and sometimes a hospital stay. The best way to stop depends
    on the medicines and on your health, the Migraine Trust says, and the American Migraine Foundation warns that
    stopping some medicines suddenly can be dangerous.</p>
  <p>Plan this with your doctor, especially if you take opioids, and don't stop or change medicines on your own.</p>
  <h2>How long do headaches get worse after stopping?</h2>
  <p>Often for 1 to 2 weeks, though it varies: a 2017 review found many people have withdrawal symptoms for that long,
    most often a worse headache at first. The American Migraine Foundation says the first few weeks tend to be the
    worst, and fully breaking the cycle may take 2 to 6 months.</p>
  <p>Other withdrawal symptoms can include nausea, poor sleep, restlessness, and an upset stomach or diarrhoea. The
    Migraine Trust says they can be hardest after ergotamine, triptans and codeine- or morphine-based medicines, and
    milder after paracetamol or anti-inflammatory painkillers such as ibuprofen. It lists things that can help: regular
    rest and sleep, drinking enough fluids, avoiding caffeine, doing less where you can, and hot or cold packs. Ask your
    doctor what support suits you.</p>
  <h2>Will it go away, and can it come back?</h2>
  <p>It usually gets better once the overuse stops, though not always, ICHD-3 says, and preventive treatment tends to
    work better afterwards. It can come back: a 2017 review says most studies found 25 to 35% of people relapsed within
    the first year after stopping, and few relapsed after that.</p>
  <p>ICHD-3 notes that around half of people who seem to have chronic migraine while overusing medicine go back to
    episodic migraine after stopping it. The same 2017 review says around 50 to 70% of people seem to respond well to
    stopping, that relapse figures vary widely, from 20 to 60%, and that most studies were done in specialist headache
    centres, so they may not reflect everyone.</p>
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
                'Medication-overuse (rebound) headache: the ICHD-3 day counts, how they compare with NHS advice, its '
                'signs, how it is treated and whether it comes back.', body, ld)


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
    ('ICHD-3, A1.1.3 Non-menstrual migraine without aura, with comments on menstrual migraine',
     'https://ichd-3.org/appendix/a1-migraine/a1-1-migraine-without-aura/a1-1-3-non-menstrual-migraine-without-aura/'),
    ('ICHD-3, A1.2.0.3 Non-menstrual migraine with aura, with comments',
     'https://ichd-3.org/appendix/a1-migraine/a1-2-migraine-with-aura-alternative-criteria/'
     'a1-2-0-3-non-menstrual-migraine-aura/'),
    ('ICHD-3, 1.1 Migraine without aura, comments', 'https://ichd-3.org/1-migraine/1-1-migraine-without-aura/'),
    ('NICE CG150, Headaches in over 12s: recommendations 1.1.4, 1.2.5, 1.2.6, 1.3.30 and 1.3.31', NICE_CG150),
    ('NHS, Migraine', NHS_MIGRAINE),
    ('NHS, Headaches', NHS_HEADACHES),
    ('NHS, Meningitis', NHS_MENINGITIS),
    ('NHS, Symptoms of menopause and perimenopause',
     'https://www.nhs.uk/conditions/menopause-and-perimenopause/symptoms/'),
    ('The Migraine Trust, Menstrual migraine', MT_MENSTRUAL),
    ('The Migraine Trust, Migraine and hormones',
     'https://migrainetrust.org/live-with-migraine/migraine-and-hormones/'),
    ('The Migraine Trust, Migraine and contraception',
     'https://migrainetrust.org/live-with-migraine/migraine-and-contraception/'),
    ('The Migraine Trust, Migraine and menopause',
     'https://migrainetrust.org/live-with-migraine/migraine-and-menopause/'),
    ('American Migraine Foundation, Menstrual migraine and hormones',
     'https://americanmigrainefoundation.org/resource-library/'
     'menstrual-migraine-and-hormones-understanding-the-connection/'),
    ('American Migraine Foundation, Hormonal and menstrual migraine',
     'https://americanmigrainefoundation.org/resource-library/hormonal-menstrual-migraine/'),
    ('Vetvik KG and MacGregor EA, The Lancet Neurology, 2021: menstrual migraine (abstract)',
     'https://pubmed.ncbi.nlm.nih.gov/33600767/'),
    ('Raffaelli B and others, The Journal of Headache and Pain, 2023: menstrual migraine and oestrogen withdrawal '
     '(abstract)', 'https://pubmed.ncbi.nlm.nih.gov/37730536/'),
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
  <p>Menstrual migraine is migraine linked to periods: attacks from day −2 to day +3 in at least 2 of 3 cycles. In
    pure menstrual migraine, attacks happen only then. In menstrually-related migraine, they also happen at other times.
    The Migraine Trust says fewer than 1 in 100 women with migraine have the pure kind.</p>
  <p>As many as two-thirds of women with migraine may have attacks linked to their period, the Migraine Trust says. The
    international classification, ICHD-3, sets out both kinds in its appendix of research criteria, because experts are
    not yet sure they are separate conditions. Most period attacks are without aura; forms with aura exist but are
    uncommon.</p>
  <p>The difference matters for treatment: ICHD-3 notes that hormonal prevention is more likely to work for the pure
    kind. A doctor confirms which kind you have, based on your symptoms and your diary.</p>
  <h2>How do I count the days?</h2>
  <p>Day 1 is the first day of bleeding, and the day before it is day −1, so there is no day 0. The window is five
    days: days −2, −1, 1, 2 and 3. A withdrawal bleed in the break between combined-pill packs, or during cyclical
    hormone replacement therapy (HRT), counts as a period too.</p>
  <table><caption>The menstrual migraine window (ICHD-3)</caption>
    <thead><tr><th scope="col">Day</th><th scope="col">When</th></tr></thead><tbody>{rows}</tbody></table>
  <p>If your bleeds come from the pill or HRT, say so in your diary: ICHD-3 notes that the cause of attacks may differ
    from a natural cycle, and so may the best way to manage them.</p>
  <h2>Why does migraine happen around a period?</h2>
  <p>A widely accepted explanation is the natural fall in oestrogen in the days before a period, which makes an
    attack more likely. Prostaglandins, hormone-like substances released around a period that also cause period pain,
    may add to the risk. The evidence is still limited, a 2023 review says,
    and other changes at this point in the cycle may matter too.</p>
  <p>Oestrogen seems to act directly on pain pathways in the nervous system, the Migraine Trust says, though how it
    does so is not fully understood. The American Migraine Foundation says it is often the rapid change in hormone
    levels, rather than oestrogen itself, that matters. A 2021 review in The Lancet Neurology names two mechanisms,
    oestrogen withdrawal and prostaglandin release, and says treatments aimed at them might work, but the evidence is
    not robust.</p>
  <p>Anything that changes oestrogen levels may affect migraine, the Migraine Trust says, including the contraceptive
    pill, HRT and the menopause, covered below.</p>
  <h2>Are period attacks worse than other migraine attacks?</h2>
  <p>Often, yes. The NHS says menstrual migraine attacks are often longer and more severe than attacks at other times.
    The Migraine Trust adds that they may come with more nausea or vomiting and sensitivity to light or sound, and may
    be harder to treat. They are mostly without aura.</p>
  <p>A 2021 review in The Lancet Neurology says that, in women diagnosed with menstrual migraine, attacks around the
    period cause substantially more disability than their other attacks. The American Migraine Foundation adds that
    they can come back after first treatment. Note in your diary how your period attacks behave: it may help your
    doctor work out whether you have menstrual migraine and which treatment is best.</p>
  <h2>How long should I track it?</h2>
  <p>UK guidance (NICE) suggests a headache diary for at least 2 menstrual cycles, and the Migraine Trust at least 3
    months. Mark your period days and every attack. NICE suggests suspecting migraine linked to periods when attacks
    happen mostly from day −2 to day +3 in at least 2 of 3 cycles in a row.</p>
  <p>Write things down as they happen, not from memory: ICHD-3 notes that many women over-report a link between attacks
    and periods, and for research it asks for a diary kept over at least three cycles. The NHS suggests seeing a GP if
    you regularly get attacks around your period. The <a href="/headache-diary/">free printable headache diary</a> has
    a column for period days.</p>
  <h2>What should I bring to my appointment?</h2>
  <p>Bring your diary, covering at least 2 menstrual cycles, with period days and every attack marked. Note how long
    and how bad each attack was, other symptoms, and every medicine you took, including over-the-counter ones. Also say
    whether you use hormonal contraception or HRT, and whether you are planning a pregnancy.</p>
  <p>NICE suggests a diary also records possible triggers, and the American Migraine Foundation adds the first day of
    each period. Mention any aura, and say if it is new. Questions you might ask:</p>
  <ul><li>Is this menstrual migraine, and which kind?</li>
    <li>Which options fit my periods and my contraception?</li>
    <li>How can I keep my medicine days down? (<a href="/medication-overuse-headache/">Why medicine days
      matter</a>)</li></ul>
  <h2>How is it treated?</h2>
  <p>Standard migraine medicines can be used for menstrual migraine, the Migraine Trust says. For period-related
    migraine that is predictable and does not respond well to usual acute treatment, NICE describes options a doctor
    may consider on the days migraine is expected. Days you take medicine for it count toward
    <a href="/medication-overuse-headache/">medication-overuse headache</a>. Talk to your doctor about what suits
    you.</p>
  <p>Which options fit depends on you: the Migraine Trust says they depend on how regular your periods are, whether
    they are painful or heavy, and whether you also need contraception. It is also worth taking extra care with other
    triggers around your period, such as not drinking enough, disturbed sleep and irregular or missed meals.</p>
  <h2>Does contraception matter?</h2>
  <p>It can. NICE advises doctors not to routinely offer combined hormonal contraception (the combined pill, patch or
    ring) for contraception to people who have migraine with aura. If you have aura, or get it for the first time while
    using one of these methods, tell whoever prescribes your contraception soon. They may suggest a different
    method.</p>
  <p>The reason is stroke risk. The Migraine Trust says studies suggest the oestrogen in these methods brings a small
    but significant rise in stroke risk with aura: still a very small risk, but an unnecessary one to take. With other
    stroke risk factors, such as smoking, obesity or high blood pressure, you may be advised not to use them either. It
    says progestogen-only methods (the mini-pill, implant, injection or hormonal coil) and non-hormonal methods can be
    used with aura. Whenever you see anyone about contraception, tell them you have migraine. If you stop a method, it
    no longer protects you from pregnancy.</p>
  <p>Contraception can also change migraine itself, for better or worse, and the effect is hard to predict. The usual
    7-day break in the combined pill brings a sharp fall in oestrogen that can trigger attacks in some people, and a
    doctor may suggest changes, such as fewer or shorter breaks.</p>
  <h2>Does menstrual migraine change in perimenopause and after menopause?</h2>
  <p>Often, yes. In perimenopause, the years before periods stop, hormone levels swing and attacks can become more
    frequent and severe, the Migraine Trust says. After menopause, migraine often gets better, the NHS says. The
    Migraine Trust adds that this can take a few years, and that it is more likely to continue or worsen when menopause
    follows surgery.</p>
  <p>Period-linked attacks can become harder to predict as periods change, the Migraine Trust says. Hot flushes, night
    sweats and anxiety can disrupt sleep and add stress, which makes attacks more likely, and migraine can even start
    for the first time in perimenopause. The NHS lists headaches and migraines that are worse than usual among the
    symptoms of perimenopause and menopause. Keep the diary going, add your menopausal symptoms, and show it to your
    GP.</p>
  <h3>Can I take HRT if I have migraine?</h3>
  <p>The Migraine Trust says having migraine, including migraine with aura, does not mean you can't take HRT. HRT uses
    a different form of oestrogen, at a much lower dose than the combined pill, so the stroke risk is much lower. HRT
    helps migraine for some people and makes it worse for others.</p>
  <p>Doctors usually recommend patches, gel or spray rather than tablets for people with migraine, the Migraine Trust
    says, because they give steadier hormone levels, at the lowest dose that controls symptoms. If HRT seems to make
    migraine worse, tell your doctor: the type or dose can be reviewed. Your doctor will go through the benefits and
    risks before prescribing.</p>
  <h2>Is my period data private in Migrely?</h2>
  <p>Migrely has no internet access and no account, so it cannot send your period days or migraines anywhere. They are
    kept on your phone, and can leave it only in two ways: in Android's own backup to your Google account, if backup
    is on (on Android 9 and later only end-to-end encrypted, which needs a screen lock), and in any file you export
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
                'Migraine around your period: the window doctors use, why it happens, contraception, perimenopause and '
                'HRT, and keeping period data private.', body, ld)


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
    ('NHS, Migraine', NHS_MIGRAINE),
    ('NHS, Headaches', NHS_HEADACHES),
    ('NHS, Meningitis', NHS_MENINGITIS),
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
    ld = page_ld(path, 'WebPage', VA_TITLE, lastReviewed=VA_REVIEWED[0], author=BY_MIGRELY)
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
        ('Premium', f'Doctor PDF report and Insights: {PREMIUM_PRICES}'),
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
    ld = page_ld(path, 'WebPage', COMPARE_TITLE, lastReviewed=COMPARE_REVIEWED[0], author=BY_MIGRELY)
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
    # /assets/ is immutable for a year, so a changed picture gets a new name (EC-WEB-20): 02-sky-v2 and 03-report-v2 are the store
    # screenshot 02-sky without the green "fewer migraine days" line (#209). Bump the suffix for the next change.
    for name in ['01-log', '02-sky-v2', '03-report-v2', '04-insights']:
        src = STORE_ART / 'screenshots' / f'{name.removesuffix("-v2")}.png'
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

    Google trusts lastmod only when it is always right, so a page keeps its date until its HTML changes. The
    stylesheet's version is left out of the hash: a CSS change alone does not change what a page says.
    """
    known = json.loads(LASTMOD.read_text()) if LASTMOD.exists() else {}
    today = date.today().isoformat()
    marks = {}
    for path, content in pages.items():
        digest = hashlib.sha256(content.replace(CSS_VERSION, '').encode()).hexdigest()
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


# Pages built and checked like the others but kept out of the sitemap, llms.txt and the site's navigation: noindex and
# reachable only from an ad. Their lastmod is tracked in lastmod.json all the same.
AD_PAGES = [('/android/', 'android/index.html', android_landing)]


BANNED_AD = re.compile(r'never goes online|100% offline|nothing leaves your phone|your migraine|your month|while you rest'
                       r'|your (head|pain|attack|health|doctor|period)|calm sky|a calm migraine|\bbest\b|#1|\blimit\b'
                       r'|02-sky-v2|01-log|US\$|\$[0-9]', re.I)
AD_BUDGET = 160 * 1024


def check_ad_pages(built):
    """Stops the build when the ad landing page breaks a rule of T62's UX spec, section 9.1: the exact Play link on
    exactly two buttons and no other Play link, noindex and canonical, no script, video, iframe, inline style or
    redirect, one H1 and no skipped heading level, every image sized with alt (lazy below the hero), the sample-data
    caption twice, the backup caveat and the diary line present, no banned wording or price, a viewport that allows
    zoom, the 1200x630 link preview, and a page weight of at most 160 KB."""
    if AD_PLAY != AD_PLAY_EXPECTED:
        raise SystemExit(f'The ad Play link changed: {AD_PLAY}')
    for path, (_, content) in built.items():
        text = html.unescape(content)
        fail = lambda why: SystemExit(f'{path}: {why}')
        if content.count(f'href="{esc(AD_PLAY_EXPECTED)}"') != 2:
            raise fail('must carry the exact campaign Play link on exactly two buttons.')
        if content.count('play.google.com') != 2:
            raise fail('play.google.com must appear exactly twice.')
        if '<meta name="robots" content="noindex">' not in content:
            raise fail('must be noindex.')
        if f'<link rel="canonical" href="{BASE}{path}">' not in content:
            raise fail('needs a canonical to itself.')
        if re.search(r'<script|<video|autoplay|iframe', content):
            raise fail('must have no script, video or iframe.')
        for bad in ['market://', 'intent://', 'http-equiv="refresh"', 'target="_blank"', ' style="', '<style', 'onclick']:
            if bad in content:
                raise fail(f'must not contain {bad}.')
        levels = [int(n) for n in re.findall(r'<h([1-6])\b', content)]
        if levels.count(1) != 1 or any(b > a + 1 for a, b in zip(levels, levels[1:])) or levels[0] != 1:
            raise fail(f'needs one h1 and no skipped heading level, got {levels}.')
        first_figure = content.index('</figure>')
        for tag in re.findall(r'<img\b[^>]*>', content):
            if not all(attr in tag for attr in ('alt="', 'width="', 'height="')):
                raise fail(f'image without alt, width or height: {tag[:80]}')
            if content.index(tag) > first_figure and 'loading="lazy"' not in tag:
                raise fail(f'image below the hero must be lazy: {tag[:80]}')
        if text.count(AD_SAMPLE) != 2:
            raise fail(f'"{AD_SAMPLE}" must appear exactly twice.')
        for needed in (AD_BACKUP, AD_DIARY):
            if needed not in text:
                raise fail(f'missing: {needed}')
        found = BANNED_AD.search(text)
        if found:
            raise fail(f'banned wording: {found.group(0)}')
        viewport = re.search(r'<meta name="viewport" content="([^"]*)"', content).group(1)
        if 'viewport-fit=cover' not in viewport or 'maximum-scale' in viewport or 'user-scalable' in viewport:
            raise fail('viewport needs viewport-fit=cover and must allow zoom.')
        if f'property="og:image" content="{BASE}/assets/img/{OG_ANDROID}"' not in content:
            raise fail('og:image must be the Android preview.')
        if Image.open(ASSETS / 'img' / OG_ANDROID).size != (1200, 630):
            raise fail('the link preview must be 1200x630.')
        weight = len(content.encode())
        for src in sorted(set(re.findall(r'(?:src|href)="(/[^"?#]*\.(?:webp|svg|png|css|woff))', content))):
            if not (SITE / src.lstrip('/')).exists():
                raise fail(f'{src} does not exist.')
            if not src.endswith('.png') and src not in ('/favicon-32.png',):
                weight += (SITE / src.lstrip('/')).stat().st_size
        for font in ('fraunces-600', 'figtree-400', 'figtree-600'):
            weight += (ASSETS / 'fonts' / f'{font}.woff').stat().st_size
        if weight > AD_BUDGET:
            raise fail(f'weighs {weight} bytes, over the {AD_BUDGET} budget.')
        print(f'{path}: page weight {weight} bytes including the HTML')


def check_orphans(built):
    """Stops the build when a sitemap page can't be reached by following links from the home page (BRD EC-WEB-19)."""
    links = {path: set(re.findall(r'<a\b[^>]*?\shref="(/[^"#?]*)"', re.sub(r'<!--.*?-->', '', content, flags=re.S)))
             for path, (_, content) in built.items()}
    reached, todo = {'/'}, ['/']
    while todo:
        for href in links[todo.pop()] & links.keys() - reached:
            reached.add(href)
            todo.append(href)
    orphans = [path for path in built if path not in reached]
    if orphans:
        raise SystemExit(f'No link path from the home page reaches {", ".join(orphans)}: if it is a guide, add it to '
                         'GUIDES; otherwise link it from another page (EC-WEB-19).')


def check_promo_video():
    """Stops the build when the home page's video or its poster would be missing (BRD D146)."""
    missing = [name for name in PROMO_VIDEO if not (ASSETS / 'video' / name).exists()]
    if missing:
        raise SystemExit(f'Missing {", ".join(missing)} in website/public/assets/video/: copy the cut from '
                         'marketing/video/promo-v3/out/ under a new name and update PROMO_VIDEO (EC-WEB-20).')


def check_diary_pdfs():
    """Stops the build when a download on /headache-diary/ would be missing (BRD D140)."""
    missing = [name for name in DIARY_PDFS.values() if not (SITE / 'headache-diary' / name).exists()]
    if missing:
        raise SystemExit(f'Missing {", ".join(missing)}: run python3 website/make_diary_pdf.py first.')


# The guides whose question headings open with a 40- to 60-word answer, and the headings that are not answers to a
# search: the app sections, the privacy section, the sources and the urgent-help section, whose headings are the
# app's Warning signs wording and cannot be shortened (BRD D149, D171).
ANSWER_PAGES = ['/headache-diary/', '/medication-overuse-headache/', '/menstrual-migraine/']
ANSWER_EXEMPT = {'When should I get help straight away?',
                 'Call your local emergency number if a headache comes with any of these:',
                 'Get urgent medical advice if you have a migraine attack and:',
                 'Would you rather use your phone?', 'How can I keep count?', 'Is my period data private in Migrely?',
                 'Sources'}


def check_direct_answers(built):
    """Stops the build when a guide's question heading (H2 or H3) does not open with a 40- to 60-word answer (BRD
    D149)."""
    wrong = []
    for path in ANSWER_PAGES:
        questions = re.findall(r'<h([23])[^>]*>(.*?)</h\1>\s*(?:<p[^>]*>(.*?)</p>)?', built[path][1], re.S)
        for _, heading, answer in questions:
            heading = html.unescape(re.sub(r'<[^>]+>', '', heading))
            count = len(html.unescape(re.sub(r'<[^>]+>', '', answer)).split())
            if heading not in ANSWER_EXEMPT and not 40 <= count <= 60:
                wrong.append(f'{path} "{heading}" ({count} words)')
    if wrong:
        raise SystemExit('These headings need a first paragraph of 40 to 60 words that answers them (BRD D149): '
                         + '; '.join(wrong))


def check_faq_unique():
    """Stops the build when a question is answered both on the home page and on /support/ (BRD EC-WEB-21)."""
    repeated = {question for question, *_ in HOME_FAQ} & {question for question, *_ in FAQ}
    if repeated:
        raise SystemExit(f'Answered on both the home page and /support/: {", ".join(sorted(repeated))}. Keep each '
                         'question in HOME_FAQ or FAQ, not both (EC-WEB-21).')


def check_min_sdk():
    """Stops the build when the app's minSdk no longer matches the Android version the site states (EC-WEB-22)."""
    found = re.search(r'^minSdk\s*=\s*"(\d+)"', VERSIONS.read_text(), re.M)
    if not found or int(found.group(1)) != MIN_SDK:
        stated = f'minSdk {found.group(1)}' if found else 'no minSdk line'
        raise SystemExit(f'gradle/libs.versions.toml has {stated}, but the site says "{MIN_ANDROID} and later" (minSdk '
                         f'{MIN_SDK}). Update MIN_SDK (EC-WEB-22).')


def llms_txt():
    """/llms.txt in the llmstxt.org format (BRD D147): what Migrely is, its facts and every page, with one sentence."""
    facts = [
        f'An Android app ({MIN_ANDROID} and later), in English, published by Sulton UzDev on Google Play: '
        f'{play_link("llms")}',
        'No internet permission: Android stops it from connecting, so nothing entered can leave the phone through the '
        f'app. Anyone can check this on their own phone: {BASE}/privacy-check/',
        'Android\'s own backup may copy the entries to the user\'s Google account. '
        + BACKUP_RULE.replace('Migrely allows this', 'the app allows this')
        + ' Files the user exports go wherever they send them.',
        'No account, no ads, and no analytics or tracking code of its own.',
        'Free, for good: logging, the whole history, medications, reminders, backup and spreadsheet export, and every '
        'safety warning.',
        f'Premium adds the doctor PDF report and Insights: {PREMIUM_PRICES}.',
        'Not a medical device: it does not diagnose, treat or prevent any condition. Its medicine-day note uses the '
        'ICHD-3 day counts for medication-overuse headache and suggests talking to a doctor.',
        f'Contact: {SUPPORT}',
        *(f'Official {name} profile: {url}' for name, url in SOCIAL_PROFILES),
    ]

    def links(pages):
        return ''.join(f'- [{name}]({BASE}{href}): {summary}\n' for href, name, summary in pages)

    return (f'# Migrely\n\n> {WHAT_IS}\n\n' + ''.join(f'- {fact}\n' for fact in facts)
            + f'\n## Guides\n\n{links(GUIDES)}\n## About the app\n\n'
            + links([('/', 'Home page', 'What Migrely does, a 30-second video, and questions asked before installing.'),
                     *TRUST_PAGES]))


def check_llms(text, built):
    """Stops the build when /llms.txt would leave out a sitemap page (BRD D147)."""
    if not text.isascii():
        raise SystemExit('/llms.txt must be plain ASCII: replace curly quotes and dashes in its sources.')
    missing = [path for path in built if f']({BASE}{path})' not in text]
    if missing:
        raise SystemExit(f'/llms.txt does not list {", ".join(missing)}: add it to GUIDES or TRUST_PAGES.')


def main():
    check_permissions()
    check_diary_pdfs()
    check_promo_video()
    check_faq_unique()
    check_min_sdk()
    built = {path: (rel, build()) for path, rel, build in PAGES}
    ads = {path: (rel, build()) for path, rel, build in AD_PAGES}
    check_ad_pages(ads)
    check_orphans(built)
    check_direct_answers(built)
    llms = llms_txt()
    check_llms(llms, built)
    for rel, content in [*built.values(), *ads.values(), ('404.html', not_found())]:
        target = SITE / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content)
    images()
    dates = lastmods({path: content for path, (_, content) in {**built, **ads}.items()})
    (SITE / 'sitemap.xml').write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + ''.join(f'  <url><loc>{BASE}{path}</loc><lastmod>{dates[path]}</lastmod></url>\n' for path in built)
        + '</urlset>\n')
    (SITE / 'robots.txt').write_text(f'User-agent: *\nAllow: /\nSitemap: {BASE}/sitemap.xml\n')
    (SITE / 'llms.txt').write_text(llms)
    print('built', ', '.join(built), '404.html and llms.txt')


if __name__ == '__main__':
    main()
