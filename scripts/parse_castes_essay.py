#!/usr/bin/env python3
"""
Parse 'இந்தியாவில் சாதிகள்' text into chapter JSON files for TamilBookReader app.
"""
import json, re, sys

SRC  = "/Users/livingsonremiyathas/Desktop/TamilBookReader/App_Icon/இந்தியாவில் சாதிகள் - Dr. Ambedkar in Tamil.txt"
DEST = "/Users/livingsonremiyathas/Desktop/TamilBookReader/app/src/main/assets"

# ─── load & clean raw lines ───────────────────────────────────────────────────
with open(SRC, encoding="utf-8-sig") as f:
    raw = f.readlines()

lines = [l.rstrip() for l in raw]

# ─── helper ───────────────────────────────────────────────────────────────────
def is_blank(l):   return l.strip() == ""
def is_ref(l):     return l.strip().startswith("*")
def section(t, c): return {"type": t, "content": c.strip()}

def classify(l):
    s = l.strip()
    if not s: return None
    if is_ref(l): return section("quote", s.lstrip("* ").strip())
    # numbered list items become paragraphs
    return section("paragraph", s)

# ─── chapter boundaries (line indices, 0-based) ──────────────────────────────
# Chapter 1: முன்னுரை மற்றும் சாதியின் இயல்பு (Lines 1-27)
# Chapter 2: சாதியின் வரையறைகள் மற்றும் அகமண முறை (Lines 28-43)
# Chapter 3: சாதியின் அமைப்பியக்கம் (Lines 44-70)
# Chapter 4: சாதியின் தோற்றம் (Lines 71-82)
# Chapter 5: சாதியின் வளர்ச்சியும் முடிவும் (Lines 83-102)

CHAPTERS = [
    {
        "id":    "ch_01",
        "title": "முன்னுரை மற்றும் சாதியின் இயல்பு",
        "start": 21,   # 0-based, skip title/author block
        "end":   27,
        "heading_first": True,
    },
    {
        "id":    "ch_02",
        "title": "சாதியின் வரையறைகளும் அகமண முறையும்",
        "start": 27,
        "end":   43,
        "heading_first": False,
    },
    {
        "id":    "ch_03",
        "title": "சாதியின் அமைப்பியக்கம்",
        "start": 43,
        "end":   70,
        "heading_first": False,
    },
    {
        "id":    "ch_04",
        "title": "சாதியின் தோற்றம்",
        "start": 70,
        "end":   82,
        "heading_first": False,
    },
    {
        "id":    "ch_05",
        "title": "சாதியின் வளர்ச்சியும் முடிவும்",
        "start": 82,
        "end":   102,
        "heading_first": False,
    },
]

# ─── build chapter JSONs ──────────────────────────────────────────────────────
def build_chapter(ch_meta, all_lines):
    secs = []
    # first section is always the chapter heading
    secs.append(section("heading", ch_meta["title"]))

    for line in all_lines[ch_meta["start"]:ch_meta["end"]]:
        s = line.strip()
        if not s:
            continue
        # footnote / reference lines → quote
        if s.startswith("*"):
            secs.append(section("quote", s.lstrip("* ").strip()))
            continue
        # numbered list items that are short (< 80 chars) look like sub-headings
        if re.match(r"^[1-9]\)\s", s) and len(s) < 120:
            secs.append(section("heading", s))
            continue
        # plain numbered short items
        if re.match(r"^[1-9]\.\s", s) and len(s) < 100:
            secs.append(section("paragraph", s))
            continue
        secs.append(section("paragraph", s))

    return {
        "chapterId": ch_meta["id"],
        "title":     ch_meta["title"],
        "sections":  secs,
    }

import os, pathlib
pathlib.Path(f"{DEST}/content").mkdir(parents=True, exist_ok=True)

for ch in CHAPTERS:
    data = build_chapter(ch, lines)
    out_path = f"{DEST}/content/{ch['id']}.json"
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"✓ {out_path}  ({len(data['sections'])} sections)")

# ─── book_config.json ─────────────────────────────────────────────────────────
config = {
    "id":           "ambedkar_castes_in_india",
    "title":        "இந்தியாவில் சாதிகள்",
    "author":       "டாக்டர் பீ. ஆர். அம்பேத்கர்",
    "coverImagePath": "",
    "language":     "ta",
    "totalChapters": len(CHAPTERS),
    "chapters": [
        {
            "id":          ch["id"],
            "index":       i,
            "title":       ch["title"],
            "contentPath": f"content/{ch['id']}.json",
        }
        for i, ch in enumerate(CHAPTERS)
    ],
}

cfg_path = f"{DEST}/book_config.json"
with open(cfg_path, "w", encoding="utf-8") as f:
    json.dump(config, f, ensure_ascii=False, indent=2)
print(f"✓ {cfg_path}")
print("Done.")
