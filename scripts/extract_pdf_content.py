#!/usr/bin/env python3
"""
Extract text from இராசராச_சோழன்.pdf and convert to chapter JSON files.

Requirements:
    pip3 install pymupdf pytesseract pillow
    brew install tesseract tesseract-lang  # includes Tamil (tam)

Usage:
    python3 scripts/extract_pdf_content.py
"""

import os, json, sys, re
from pathlib import Path

PDF_PATH    = os.path.expanduser("~/Desktop/TamilBooks/இராசராச_சோழன்.pdf")
OUTPUT_DIR  = os.path.join(os.path.dirname(__file__), "../app/src/main/assets/content")
CONFIG_PATH = os.path.join(os.path.dirname(__file__), "../app/src/main/assets/book_config.json")

# --- Tesseract OCR (requires: brew install tesseract tesseract-lang) ----------
def ocr_page_tesseract(page, dpi=200):
    import pytesseract
    from PIL import Image
    import io
    mat = page.get_pixmap(matrix=__import__("fitz").Matrix(dpi/72, dpi/72))
    img = Image.open(io.BytesIO(mat.tobytes("png")))
    return pytesseract.image_to_string(img, lang="tam+eng", config="--psm 6")

# --- Section classifier -------------------------------------------------------
def classify_sections(raw_text):
    """Split raw OCR text into typed sections."""
    sections = []
    lines = [l.rstrip() for l in raw_text.split("\n")]
    buf = []

    def flush(kind="paragraph"):
        text = " ".join(buf).strip()
        if text:
            sections.append({"type": kind, "content": text})
        buf.clear()

    for line in lines:
        stripped = line.strip()
        if not stripped:
            flush()
            continue
        # Short all-caps / short lines → likely headings
        if len(stripped) < 40 and stripped == stripped.upper() and any(c.isalpha() for c in stripped):
            flush()
            sections.append({"type": "heading", "content": stripped.title()})
        # Lines starting with quote chars → quote
        elif stripped.startswith(("\"", """, "'")):
            flush()
            sections.append({"type": "quote", "content": stripped.strip('""\'"')})
        else:
            buf.append(stripped)

    flush()
    return sections

# --- Chapter splitter ---------------------------------------------------------
def split_into_chapters(all_sections, n_chapters=23):
    """Evenly distribute sections across chapters (rough heuristic)."""
    total = len(all_sections)
    per   = max(1, total // n_chapters)
    chapters = []
    for i in range(n_chapters):
        start = i * per
        end   = start + per if i < n_chapters - 1 else total
        chapters.append(all_sections[start:end])
    return chapters

# --- Main --------------------------------------------------------------------
def main():
    try:
        import fitz
    except ImportError:
        sys.exit("Install PyMuPDF:  pip3 install pymupdf")

    try:
        import pytesseract
    except ImportError:
        sys.exit("Install pytesseract:  pip3 install pytesseract")

    pdf = fitz.open(PDF_PATH)
    print(f"PDF: {len(pdf)} pages")

    all_text = []
    for i, page in enumerate(pdf):
        print(f"  OCR page {i+1}/{len(pdf)}", end="\r", flush=True)
        try:
            text = ocr_page_tesseract(page)
            all_text.append(text)
        except Exception as e:
            print(f"\n  Warning page {i+1}: {e}")
            all_text.append("")
    print()

    raw = "\n".join(all_text)
    sections = classify_sections(raw)
    print(f"Extracted {len(sections)} sections")

    # Load existing config for chapter titles
    with open(CONFIG_PATH, encoding="utf-8") as f:
        config = json.load(f)

    chapter_sections = split_into_chapters(sections, config["totalChapters"])

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    for i, ch in enumerate(config["chapters"]):
        ch_id    = ch["id"]
        ch_title = ch["title"]
        secs     = chapter_sections[i] if i < len(chapter_sections) else []
        if not secs:
            secs = [{"type":"paragraph","content":"(உள்ளடக்கம் கிடைக்கவில்லை)"}]

        # Prepend heading if not already there
        if not secs or secs[0]["type"] != "heading":
            secs.insert(0, {"type":"heading","content":ch_title})

        out = {
            "chapterId": ch_id,
            "title":     ch_title,
            "sections":  secs
        }
        out_path = os.path.join(OUTPUT_DIR, f"{ch_id}.json")
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(out, f, ensure_ascii=False, indent=2)
        print(f"  Wrote {ch_id}.json ({len(secs)} sections)")

    print(f"\nDone. Copy {OUTPUT_DIR} back into the app assets and rebuild.")

if __name__ == "__main__":
    main()
