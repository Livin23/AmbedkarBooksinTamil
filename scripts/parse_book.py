#!/usr/bin/env python3
"""Parse இராசராச_சோழன்_book_for_app.txt → chapter JSON files for the Android reader."""

import re, json, textwrap
from pathlib import Path

SRC  = Path("/Users/livingsonremiyathas/Desktop/Book Reader APP/Book Content/RajaRajaCholan/இராசராச_சோழன்_book_for_app.txt")
OUT  = Path("/Users/livingsonremiyathas/Desktop/RajarajanReader/app/src/main/assets/content")

# ── Patterns ──────────────────────────────────────────────────────────────────
CH_RE   = re.compile(r'^அத்தியாயம்\s+\d+\s*[—:–-]')
PART_RE = re.compile(r'^பகுதி\s+\d+[:.]')
APP_RE  = re.compile(r'^பிற்சேர்க்கை')
NUM_RE  = re.compile(r'^\d+\.\s+.+')          # "1. text"
FN_RE   = re.compile(r'\[\^?\d+\]')           # [1] or [^1]
HBAR_RE = re.compile(r'^[━─=\-]{3,}')         # separator bars

# Strip inline footnote markers and separator bars
def clean(text: str) -> str:
    t = FN_RE.sub('', text)
    t = t.strip()
    return t

def is_chapter_header(line: str) -> bool:
    return bool(CH_RE.match(line.strip()))

def is_part_header(line: str) -> bool:
    return bool(PART_RE.match(line.strip())) or bool(APP_RE.match(line.strip()))

def is_separator(line: str) -> bool:
    return bool(HBAR_RE.match(line.strip()))

def is_toc_block(lines) -> bool:
    """Detect table-of-contents preamble block (lines 1–36 of file)."""
    for l in lines:
        if CH_RE.match(l) or PART_RE.match(l) or APP_RE.match(l):
            return True
    return False

def classify_block(lines) -> list:
    """Return list of section dicts from a block of non-blank lines."""
    # Strip each line
    stripped = [clean(l) for l in lines if clean(l)]
    if not stripped:
        return []

    if len(stripped) == 1:
        text = stripped[0]
        if is_separator(text) or is_chapter_header(text) or is_part_header(text):
            return []
        # Numbered single-line heading  e.g. "1. கல்வெட்டுகள்"
        if NUM_RE.match(text) and len(text) < 90:
            return [{"type": "heading", "content": text}]
        # Short standalone line → sub-section heading
        if len(text) <= 60:
            return [{"type": "heading", "content": text}]
        # Long single line → paragraph
        return [{"type": "paragraph", "content": text}]

    # Multiple lines
    # If ALL are numbered list items → LIST_ITEM sequence
    if all(NUM_RE.match(s) for s in stripped):
        return [{"type": "list_item", "content": s} for s in stripped]

    # Mixed content — classify line by line
    sections = []
    for text in stripped:
        if not text or is_separator(text) or is_chapter_header(text) or is_part_header(text):
            continue
        if NUM_RE.match(text) and len(text) < 90:
            sections.append({"type": "list_item", "content": text})
        elif len(text) <= 60:
            sections.append({"type": "heading", "content": text})
        else:
            sections.append({"type": "paragraph", "content": text})
    return sections


def split_into_blocks(lines) -> list:
    """Split lines on one-or-more blank lines."""
    blocks, buf = [], []
    for line in lines:
        stripped = line.strip()
        if stripped:
            buf.append(stripped)
        else:
            if buf:
                blocks.append(buf)
            buf = []
    if buf:
        blocks.append(buf)
    return blocks


def extract_chapter_title(header_line: str) -> str:
    """'அத்தியாயம் 8 — இராசராசனின் மெய்க்கீர்த்தியும்' → 'இராசராசனின் மெய்க்கீர்த்தியும்'"""
    for sep in ['—', '–', '-', ':']:
        if sep in header_line:
            return header_line.split(sep, 1)[1].strip()
    return header_line.strip()


# ── Read file ─────────────────────────────────────────────────────────────────
raw = SRC.read_text(encoding='utf-8').splitlines()

# ── Split into chapter segments ───────────────────────────────────────────────
chapters_raw = []  # list of (header_line, content_lines)

current_header = None
current_lines  = []
in_preamble    = True

for line in raw:
    s = line.strip()

    # Skip preamble (TOC block before first பகுதி or அத்தியாயம்)
    if in_preamble:
        # Preamble ends at the FIRST part header (not appendix mention in TOC)
        if PART_RE.match(s):
            in_preamble = False
        continue

    if CH_RE.match(s) or APP_RE.match(s):
        if current_header is not None:
            chapters_raw.append((current_header, current_lines))
        current_header = s
        current_lines  = []
    elif PART_RE.match(s):
        pass  # part headers are informational; skip them from content
    else:
        current_lines.append(line)

if current_header is not None:
    chapters_raw.append((current_header, current_lines))

print(f"Found {len(chapters_raw)} chapters/appendix sections")

# ── Generate JSON files ───────────────────────────────────────────────────────
OUT.mkdir(parents=True, exist_ok=True)

chapter_meta = []  # for book_config.json

for idx, (header, content_lines) in enumerate(chapters_raw):
    ch_num  = idx + 1
    ch_id   = f"ch_{ch_num:02d}"

    if APP_RE.match(header.strip()):
        title = "பிற்சேர்க்கை"
    else:
        title = extract_chapter_title(header)

    # Parse blocks → sections
    blocks   = split_into_blocks(content_lines)
    sections = []
    for block in blocks:
        sections.extend(classify_block(block))

    # Remove leading headings that duplicate the chapter title
    while sections and sections[0]["type"] == "heading" and sections[0]["content"] == title:
        sections.pop(0)

    chapter_data = {
        "chapterId": ch_id,
        "title"    : title,
        "sections" : sections
    }

    out_path = OUT / f"{ch_id}.json"
    out_path.write_text(json.dumps(chapter_data, ensure_ascii=False, indent=2), encoding='utf-8')

    print(f"  {ch_id}: {title[:50]} ({len(sections)} sections)")

    chapter_meta.append({
        "id"         : ch_id,
        "index"      : idx,
        "title"      : title,
        "contentPath": f"content/{ch_id}.json"
    })

# ── Write book_config.json ────────────────────────────────────────────────────
config = {
    "id"           : "rajarajan_cholan",
    "title"        : "இராசராச சோழன் - முழுமையான வரலாறு",
    "author"       : "க. த. திருநாவுக்கரசு",
    "coverImagePath": "",
    "language"     : "ta",
    "totalChapters": len(chapter_meta),
    "parts": [
        {
            "title"          : "முதற் பகுதி: சோழப் பேரரசின் எழுச்சி",
            "startChapterIdx": 0,
            "endChapterIdx"  : 4
        },
        {
            "title"          : "இரண்டாம் பகுதி: பேரரசன் இராசராசன்",
            "startChapterIdx": 5,
            "endChapterIdx"  : 14
        },
        {
            "title"          : "மூன்றாம் பகுதி: இராசராசன் காலத்திய தமிழர் நாகரிகம்",
            "startChapterIdx": 15,
            "endChapterIdx"  : 22
        },
        {
            "title"          : "பிற்சேர்க்கை",
            "startChapterIdx": 23,
            "endChapterIdx"  : len(chapter_meta) - 1
        }
    ],
    "chapters"     : chapter_meta
}

cfg_path = Path("/Users/livingsonremiyathas/Desktop/RajarajanReader/app/src/main/assets/book_config.json")
cfg_path.write_text(json.dumps(config, ensure_ascii=False, indent=2), encoding='utf-8')
print(f"\nWrote book_config.json with {len(chapter_meta)} chapters")
print("Done ✓")
