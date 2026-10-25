"""Turns privacy-policy.md into the self-contained privacy-policy.html (no scripts, fonts or trackers).

Run from this folder: python3 make_policy_html.py
"""
import html
import re
from pathlib import Path

HERE = Path(__file__).parent


def inline(text):
    text = html.escape(text, quote=False)
    text = re.sub(r'\*\*(.+?)\*\*', r'<strong>\1</strong>', text)
    # Email addresses and web addresses become links.
    text = re.sub(r'(?<![\w@/])([\w.+-]+@[\w-]+(?:\.[\w-]+)+)', r'<a href="mailto:\1">\1</a>', text)
    return re.sub(r'(?<![">])(https://[^\s<]+[^\s<.,;:)])', r'<a href="\1">\1</a>', text)


def to_html(source):
    out, para, item = [], [], None

    def close_list():
        nonlocal item
        if item is not None:
            out.append('<li>' + inline(item) + '</li></ul>')
            item = None

    def close_para():
        if para:
            out.append('<p>' + inline(' '.join(para)) + '</p>')
            para.clear()

    for line in source.split('\n'):
        if line.startswith('# ') or line.startswith('## '):
            close_para()
            close_list()
            level = 1 if line.startswith('# ') else 2
            out.append(f'<h{level}>' + inline(line.lstrip('# ')) + f'</h{level}>')
        elif line.startswith('- '):
            close_para()
            if item is None:
                out.append('<ul>')
            else:
                out.append('<li>' + inline(item) + '</li>')
            item = line[2:]
        elif line.startswith('  ') and item is not None:
            item += ' ' + line.strip()
        elif not line.strip():
            close_list()
            close_para()
        else:
            close_list()
            para.append(line.strip())
    close_list()
    close_para()
    return '\n'.join(out)


PAGE = '''<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="color-scheme" content="light dark">
<title>Migrely privacy policy</title>
<!-- Generated from privacy-policy.md by make_policy_html.py; edit the Markdown and regenerate. -->
<style>
:root { --bg: #F5F3EE; --text: #1F2328; --rule: #D9D5CD; }
@media (prefers-color-scheme: dark) { :root { --bg: #0F1115; --text: #C8CCD2; --rule: #2A3038; } }
html { background: var(--bg); color: var(--text); }
body { font: 17px/1.6 system-ui, -apple-system, "Segoe UI", Roboto, sans-serif; max-width: 42rem; margin: 0 auto;
  padding: 2rem 1rem 4rem; }
h1 { font-size: 1.8rem; line-height: 1.25; margin: 0 0 1.5rem; }
h2 { font-size: 1.2rem; margin: 2rem 0 .5rem; padding-top: 1rem; border-top: 1px solid var(--rule); }
ul { padding-left: 1.25rem; }
li { margin: .25rem 0; }
</style>
</head>
<body>
<main>
BODY
</main>
</body>
</html>
'''

if __name__ == '__main__':
    body = to_html((HERE / 'privacy-policy.md').read_text())
    (HERE / 'privacy-policy.html').write_text(PAGE.replace('BODY', body))
