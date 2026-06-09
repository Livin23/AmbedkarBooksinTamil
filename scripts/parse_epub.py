#!/usr/bin/env python3
"""Parse Kindle EPUB → chapter JSON files + copy images for the Android reader."""

import json, re, shutil
from pathlib import Path
from bs4 import BeautifulSoup, NavigableString

EPUB_DIR  = Path("/tmp/epub_extracted/EPUB")
EPUB_SRC  = Path("/Users/livingsonremiyathas/Desktop/KindleBooks/RajaRajaCholan/முதலாம்_இராசராச_சோழன்_Kindle.epub")
OUT_JSON  = Path("/Users/livingsonremiyathas/Desktop/RajarajanReader/app/src/main/assets/content")
OUT_IMG   = Path("/Users/livingsonremiyathas/Desktop/RajarajanReader/app/src/main/assets/images")
CFG_PATH  = Path("/Users/livingsonremiyathas/Desktop/RajarajanReader/app/src/main/assets/book_config.json")

# Re-extract EPUB every time for freshness
import zipfile
extract_dir = Path("/tmp/epub_extracted2")
if extract_dir.exists():
    shutil.rmtree(extract_dir)
with zipfile.ZipFile(EPUB_SRC) as z:
    z.extractall(extract_dir)

EPUB_DIR = extract_dir / "EPUB"

# ── Copy images ───────────────────────────────────────────────────────────────
OUT_IMG.mkdir(parents=True, exist_ok=True)
epub_img_dir = EPUB_DIR / "Images"
copied = 0
for img in epub_img_dir.iterdir():
    if img.suffix.lower() in {".jpg", ".jpeg", ".png", ".webp"}:
        dest = OUT_IMG / img.name
        shutil.copy2(img, dest)
        copied += 1
print(f"Copied {copied} images → {OUT_IMG}")

# ── Helper: extract title from h1 ─────────────────────────────────────────────
def get_chapter_title(soup):
    h1 = soup.find("h1")
    if not h1:
        return None
    text = h1.get_text(strip=True)
    # Strip "அத்தியாயம் N — " prefix
    for sep in ['—', '–', '-', ':']:
        if sep in text:
            return text.split(sep, 1)[1].strip()
    return text

# ── HTML → sections ───────────────────────────────────────────────────────────
def node_to_sections(element):
    """Recursively convert HTML element to a list of section dicts."""
    sections = []

    for child in element.children:
        if isinstance(child, NavigableString):
            text = str(child).strip()
            if text:
                sections.append({"type": "paragraph", "content": text})
            continue

        tag  = child.name
        cls  = child.get("class", [])
        text = child.get_text(separator=" ", strip=True)

        if not text and tag not in ("figure",):
            continue

        # Chapter title (already handled)
        if tag == "h1":
            continue

        # Section headings
        if tag in ("h2", "h3", "h4"):
            if text:
                sections.append({"type": "heading", "content": text})

        # Paragraphs
        elif tag == "p":
            if "verse-attribution" in cls:
                if text:
                    sections.append({"type": "verse_attribution", "content": text})
            elif text:
                sections.append({"type": "paragraph", "content": text})

        # Verse / poem blocks
        elif tag == "div" and "verse" in cls and "verse-meaning" not in cls:
            if text:
                sections.append({"type": "verse", "content": text})

        # Verse meaning (translation)
        elif tag == "div" and "verse-meaning" in cls:
            if text:
                sections.append({"type": "verse_meaning", "content": text})

        # Blockquote
        elif tag == "blockquote":
            if text:
                sections.append({"type": "quote", "content": text})

        # Unordered list → list items
        elif tag == "ul":
            for li in child.find_all("li", recursive=False):
                li_text = li.get_text(strip=True)
                if li_text:
                    sections.append({"type": "list_item", "content": li_text})

        # Ordered list
        elif tag == "ol":
            for li in child.find_all("li", recursive=False):
                li_text = li.get_text(strip=True)
                if li_text:
                    sections.append({"type": "list_item", "content": li_text})

        # Figure with image
        elif tag == "figure":
            img_tag    = child.find("img")
            fig_cap    = child.find("figcaption")
            img_src    = img_tag.get("src", "") if img_tag else ""
            img_name   = Path(img_src).name if img_src else ""
            caption    = fig_cap.get_text(strip=True) if fig_cap else ""
            if img_name:
                sections.append({"type": "image", "content": img_name, "caption": caption})

        # Image gallery — recurse into figures
        elif tag == "div" and "image-gallery" in cls:
            sections.extend(node_to_sections(child))

        # Generic div — recurse
        elif tag == "div":
            sections.extend(node_to_sections(child))

    return sections

def clean_sections(sections):
    """Merge consecutive short paragraphs that look like run-on verse lines."""
    return [s for s in sections if s.get("content", "").strip()]

# ── Parse chapters ────────────────────────────────────────────────────────────
text_dir = EPUB_DIR / "Text"
chapter_files = sorted(text_dir.glob("chap_*.xhtml"))
appendix_file = text_dir / "appendix_24.xhtml"

OUT_JSON.mkdir(parents=True, exist_ok=True)
chapter_meta = []

def parse_xhtml(path, ch_id, idx):
    soup = BeautifulSoup(path.read_text(encoding="utf-8"), "lxml-xml")
    body_div = soup.find("div", class_=re.compile(r"chapter|appendix"))
    if not body_div:
        body_div = soup.find("body")

    title = get_chapter_title(soup)
    if not title:
        title = path.stem.replace("_", " ")

    sections = clean_sections(node_to_sections(body_div))

    data = {"chapterId": ch_id, "title": title, "sections": sections}
    out = OUT_JSON / f"{ch_id}.json"
    out.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"  {ch_id} ({idx+1:2}): {title[:55]:<56} {len(sections):3} sec")
    return {"id": ch_id, "index": idx, "title": title, "contentPath": f"content/{ch_id}.json"}

all_files = list(chapter_files)
if appendix_file.exists():
    all_files.append(appendix_file)

for i, f in enumerate(all_files):
    ch_id = f"ch_{i+1:02d}"
    chapter_meta.append(parse_xhtml(f, ch_id, i))

# ── Write book_config.json ────────────────────────────────────────────────────
config = {
    "id"           : "rajarajan_cholan",
    "title"        : "இராசராச சோழன் - முழுமையான வரலாறு",
    "author"       : "க. த. திருநாவுக்கரசு",
    "coverImagePath": "",
    "language"     : "ta",
    "totalChapters": len(chapter_meta),
    "parts": [
        {"title": "முதற் பகுதி: சோழப் பேரரசின் எழுச்சி",           "startChapterIdx": 0,  "endChapterIdx": 4},
        {"title": "இரண்டாம் பகுதி: பேரரசன் இராசராசன்",              "startChapterIdx": 5,  "endChapterIdx": 14},
        {"title": "மூன்றாம் பகுதி: இராசராசன் காலத்திய தமிழர் நாகரிகம்","startChapterIdx": 15, "endChapterIdx": 22},
        {"title": "பிற்சேர்க்கை",                                      "startChapterIdx": 23, "endChapterIdx": len(chapter_meta)-1}
    ],
    "chapters": chapter_meta
}
CFG_PATH.write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding="utf-8")
print(f"\nWrote book_config.json — {len(chapter_meta)} chapters")
print("Done ✓")
