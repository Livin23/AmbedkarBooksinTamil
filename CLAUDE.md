# CLAUDE.md — RajarajanReader

## What this is
Android **Jetpack Compose** book-reader app (Kotlin, MVVM).  
Book: *இராசராச சோழன்* (Tamil historical novel) — 23 chapters loaded from local JSON assets.  
Forked from TamilBookReader; redesigned with a **Royal Chola** theme.

---

## Project layout

```
RajarajanReader/
├── app/build.gradle                               # namespace com.rajarajanreader.app, compileSdk 35
├── app/src/main/
│   ├── assets/
│   │   ├── book_config.json                       # Book metadata + 23 chapter entries
│   │   └── content/ch_01.json … ch_23.json        # Per-chapter sections
│   └── kotlin/com/rajarajanreader/app/
│       ├── BookReaderApp.kt                       # Application — bookRepo + bookmarks StateFlow
│       ├── MainActivity.kt
│       ├── domain/Models.kt                       # Book, Chapter, ChapterContent, Section, ReadingTheme
│       ├── data/
│       │   ├── BookRepository.kt                  # Gson loader + in-memory cache
│       │   └── BookDto.kt                         # Raw JSON DTOs
│       └── presentation/
│           ├── navigation/AppNavigation.kt        # splash → index → reader/{idx}
│           ├── theme/ReaderTheme.kt               # IMPERIAL / PARCHMENT / PALACE palettes
│           ├── component/
│           │   ├── SectionRenderer.kt             # HEADING / PARAGRAPH (drop cap) / QUOTE
│           │   ├── OrnamentalSectionDivider()     # ✦ ✦ ✦ divider (in SectionRenderer.kt)
│           │   └── ChapterProgressBar.kt          # Gold gradient progress bar
│           └── screen/
│               ├── splash/   SplashScreen + SplashViewModel
│               ├── index/    IndexScreen  + IndexViewModel
│               └── reader/   ReaderScreen + ReaderViewModel
├── scripts/
│   └── extract_pdf_content.py                     # OCR script to populate ch_NN.json from PDF
```

---

## Architecture rules

| Layer | Package | Rule |
|-------|---------|------|
| Domain | `domain/` | Pure Kotlin — no Android imports |
| Data | `data/` | Repository only. Gson + assets. In-memory cache (no Room). |
| Presentation | `presentation/` | MVVM. `AndroidViewModel` cast to `BookReaderApp`. |

- State flows up via `StateFlow`; UI collects with `collectAsStateWithLifecycle`.
- Bookmarks: `BookReaderApp._bookmarks` (in-memory `Set<String>`). ViewModel never owns them.
- Repository accessed via `(app as BookReaderApp).bookRepo` — no DI framework.

---

## Theme system

Three Royal Chola themes cycle via the ☀ toolbar icon (`ReadingTheme.DARK → LIGHT → SEPIA`).  
Theme is held in `MainActivity` and threaded through `AppNavigation → TamilReaderTheme`.

| Theme enum | Name | Background | Accent | Feel |
|-----------|------|-----------|--------|------|
| `DARK`  | Imperial | `#0D0005` deep crimson | `#D4A017` molten gold | Royal night court |
| `LIGHT` | Parchment | `#F7EDD0` warm cream | `#8B2500` crimson | Ancient manuscript |
| `SEPIA` | Palace | `#050A1A` midnight sapphire | `#C9A84C` silver-gold | Twilight palace |

`ReaderColors` adds three extra tokens vs the original:
```
gold            — decorative highlights, drop caps, chapter numbers
dropCapColor    — same as gold by default; change per-theme if needed
dividerColor    — ornamental rule colour
headerGradientStart / headerGradientEnd — index hero header brush
```

`ReaderTypography` adds:
```
dropCap       — 64sp Bold for first-paragraph letter
chapterNumber — 48sp Bold (reserved for future chapter-number header)
```

---

## Content JSON format

`book_config.json`
```json
{
  "id": "rajarajan_cholan",
  "title": "இராசராச சோழன்",
  "author": "",
  "totalChapters": 23,
  "chapters": [
    { "id": "ch_01", "index": 0, "title": "வீரனின் பிறப்பு", "contentPath": "content/ch_01.json" }
  ]
}
```

`content/ch_NN.json`
```json
{
  "chapterId": "ch_01",
  "title": "வீரனின் பிறப்பு",
  "sections": [
    { "type": "heading",   "content": "…" },
    { "type": "paragraph", "content": "…" },
    { "type": "quote",     "content": "…" }
  ]
}
```

`SectionType.from(string)` is case-insensitive; unknown values → `PARAGRAPH`.

### Section rendering details
| Type | Rendered as |
|------|-------------|
| `heading` | Gold text + decorative ✦ top rule + gradient underline |
| `paragraph` | Body text. First paragraph of each chapter gets a **drop cap** (64sp gold first letter). |
| `quote` | Gold left border + large decorative `"` + italic body text |

`OrnamentalSectionDivider` (`✦ ✦ ✦`) is injected automatically every 6th paragraph by `ReaderScreen` — not stored in JSON.

---

## Adding content

### Add a new chapter
1. Create `app/src/main/assets/content/ch_NN.json`.
2. Add entry to `book_config.json` chapters array; increment `totalChapters`.
3. No code changes needed.

### Fill content from the PDF via OCR
The source PDF (`~/Desktop/TamilBooks/இராசராச_சோழன்.pdf`) is fully image-based.
Requires Tesseract with Tamil language data:
```bash
brew install tesseract tesseract-lang     # one-time install
pip3 install pytesseract pymupdf          # one-time install
python3 scripts/extract_pdf_content.py   # writes ch_01.json … ch_23.json
```
After running, rebuild the app — no other changes needed.

---

## Build & run

```bash
cd ~/Desktop/RajarajanReader
export JAVA_HOME=$(/usr/libexec/java_home)   # must use JDK 17, not system Java 1.7
./gradlew assembleDebug                      # → app/build/outputs/apk/debug/app-debug.apk  (~21 MB)
./gradlew installDebug                       # install on connected device/emulator
```

First build: ~2 min (Compose compiler).  
Use `run_in_background: true` when triggering from Claude Code.

---

## Design rules (never break)

- **NO AUTHOR LINE** anywhere in the UI.
- Tamil body text: **18sp, lineHeight 36sp** — never reduce.
- Gold accent (`c.gold`) must be used for: drop caps, chapter numbers, progress bar, ornamental dividers, chapter-card badges. Never use a plain accent colour for these.
- `SectionRenderer` receives `isFirstParagraph: Boolean` — the **caller** (`ReaderScreen`) tracks which paragraph is first; `SectionRenderer` must not track state internally.
- The ornamental `✦ ✦ ✦` divider is injected by `ReaderScreen` every 6 paragraphs, not stored in JSON content.
- Quote left border is drawn via `drawBehind` — do not replace with a `Box` padding workaround.
- Bookmark state is **in-memory only** (resets on restart). Persist via `DataStore` if needed (dep already in `build.gradle`).

---

## Key dependencies

| Library | Version | Use |
|---------|---------|-----|
| Compose BOM | 2024.02.00 | All Compose UI |
| Navigation Compose | 2.7.7 | splash / index / reader/{idx} |
| Lifecycle ViewModel Compose | 2.7.0 | `AndroidViewModel` + `collectAsStateWithLifecycle` |
| Gson | 2.10.1 | JSON asset parsing |
| Coil Compose | 2.5.0 | Cover image (not yet wired) |
| DataStore Preferences | 1.0.0 | Ready for bookmark persistence |
| Google Mobile Ads | (via AdManager) | Interstitial between chapters |

---

## Common tasks

| Task | File(s) to touch |
|------|-----------------|
| Change theme colours | `ReaderTheme.kt` — `imperial()` / `parchment()` / `palace()` |
| Add new section type | `Models.kt` `SectionType` + new `when` branch in `SectionRenderer.kt` |
| Change ornamental divider frequency | `ReaderScreen.kt` — `idx % 6 == 5` condition |
| Add drop cap to more screens | Pass `isFirstParagraph = true` from the calling composable |
| Persist bookmarks | `BookReaderApp` — swap `MutableStateFlow<Set>` for `DataStore` flow |
| Add font size slider | Add to `ReaderState`; scale `t.body.fontSize` in `SectionRenderer` |
| Wire cover image | `BookRepository` already passes `coverImagePath`; add `AsyncImage` in `IndexScreen` header |
| Release build | Add keystore to `keystore.properties`; run `./gradlew assembleRelease` |
