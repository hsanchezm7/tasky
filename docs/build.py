"""Genera la web de la documentación a partir de los .md de docs/.

Para cada docs/<nombre>.md crea docs/site/<nombre>.html, más un index.html
que enlaza a todos, y copia la carpeta docs/media/ con las capturas.

Uso:  python docs/build.py
Requiere:  pip install -r docs/requirements.txt
"""

import html
import re
import shutil
from pathlib import Path

from markdown_it import MarkdownIt

DOCS = Path(__file__).resolve().parent
SITE = DOCS / "site"
MEDIA = DOCS / "media"

# CommonMark (listas anidadas con 2 espacios) + tablas y tachado de GFM
md = MarkdownIt("commonmark", {"html": True}).enable(["table", "strikethrough"])

CSS = """
:root {
  --bg: #ffffff; --fg: #1f2328; --muted: #59636e; --border: #d1d9e0;
  --surface: #f6f8fa; --accent: #0969da; --code-bg: #eff1f3;
}
@media (prefers-color-scheme: dark) {
  :root {
    --bg: #0d1117; --fg: #e6edf3; --muted: #9198a1; --border: #3d444d;
    --surface: #151b23; --accent: #4493f8; --code-bg: #262c36;
  }
}
* { box-sizing: border-box; }
body {
  margin: 0; background: var(--bg); color: var(--fg);
  font: 16px/1.6 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}
main { max-width: 920px; margin: 0 auto; padding: 24px 16px 64px; }
nav { border-bottom: 1px solid var(--border); padding: 12px 16px; }
nav a { font-weight: 600; }
a { color: var(--accent); text-decoration: none; }
a:hover { text-decoration: underline; }
h1, h2, h3, h4 { line-height: 1.25; margin: 1.6em 0 .6em; }
h1 { font-size: 2em; margin-top: .4em; }
h2 { font-size: 1.5em; padding-bottom: .3em; border-bottom: 1px solid var(--border); }
img { max-width: 100%; height: auto; border: 1px solid var(--border); border-radius: 6px; }
code { background: var(--code-bg); padding: .15em .35em; border-radius: 4px; font-size: .9em; }
pre { background: var(--surface); border: 1px solid var(--border); border-radius: 6px;
      padding: 12px 16px; overflow-x: auto; }
pre code { background: none; padding: 0; }
blockquote { margin: 1em 0; padding: 0 1em; color: var(--muted); border-left: 4px solid var(--border); }
.table-wrap { overflow-x: auto; margin: 1em 0; }
table { border-collapse: collapse; width: 100%; }
th, td { border: 1px solid var(--border); padding: 6px 12px; text-align: left; vertical-align: top; }
th { background: var(--surface); }
.docs { list-style: none; padding: 0; }
.docs li { border: 1px solid var(--border); border-radius: 8px; margin: 12px 0; background: var(--surface); }
.docs a { display: block; padding: 14px 18px; }
.docs small { display: block; color: var(--muted); font-weight: normal; }
"""

PAGE = """<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<style>{css}</style>
</head>
<body>
{nav}
<main>
{body}
</main>
</body>
</html>
"""


def sort_key(path: Path):
    """Ordena boletin2 antes que boletin10 (orden natural)."""
    return [int(t) if t.isdigit() else t for t in re.split(r"(\d+)", path.stem)]


def title_of(text: str, path: Path) -> str:
    """Primer encabezado '# ...' del documento, o el nombre del archivo."""
    for line in text.splitlines():
        if line.startswith("# "):
            return line[2:].strip()
    return path.stem


def to_html(text: str) -> str:
    body = md.render(text)
    # los enlaces entre memorias apuntan a .md; en la web son .html
    body = re.sub(r'href="([^":#]+)\.md(#[^"]*)?"', r'href="\1.html\2"', body)
    # las tablas anchas hacen scroll horizontal en vez de romper la página
    body = body.replace("<table>", '<div class="table-wrap"><table>').replace("</table>", "</table></div>")
    return body


def page(title: str, body: str, nav: str = "") -> str:
    return PAGE.format(title=html.escape(title), css=CSS, nav=nav, body=body)


def main():
    if SITE.exists():
        shutil.rmtree(SITE)
    SITE.mkdir()
    if MEDIA.is_dir():
        shutil.copytree(MEDIA, SITE / "media")

    entries = []
    for path in sorted(DOCS.glob("*.md"), key=sort_key):
        text = path.read_text(encoding="utf-8")
        title = title_of(text, path)
        # boletin1-alejandro.md -> "Alejandro" para distinguir memorias del mismo boletín
        author = path.stem.split("-", 1)[1].capitalize() if "-" in path.stem else ""
        nav = '<nav><a href="index.html">← Índice</a></nav>'
        (SITE / f"{path.stem}.html").write_text(page(title, to_html(text), nav), encoding="utf-8")
        entries.append((path.stem, title, author))
        print(f"  {path.name} -> site/{path.stem}.html")

    items = "\n".join(
        f'<li><a href="{stem}.html">{html.escape(title)}'
        + (f"<small>Memoria de {html.escape(author)}</small>" if author else "")
        + "</a></li>"
        for stem, title, author in entries
    )
    body = (
        "<h1>Tasky · Documentación</h1>\n"
        "<p>Memorias de las sesiones de prácticas del proyecto "
        '<a href="https://github.com/hsanchezm7/tasky">hsanchezm7/tasky</a>.</p>\n'
        f'<ul class="docs">\n{items}\n</ul>'
    )
    (SITE / "index.html").write_text(page("Tasky · Documentación", body), encoding="utf-8")
    print(f"  index.html con {len(entries)} documentos")


if __name__ == "__main__":
    main()
