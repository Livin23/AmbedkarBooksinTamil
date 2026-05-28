# CLAUDE.md — TamilBookReader

## What this is
Android **Jetpack Compose** book-reader app (Kotlin, MVVM).
Current book: *அம்பேத்கர்: ஒரு வாழ்க்கை வரலாறு* (Tamil) — loaded from local JSON assets.

---

## Project layout

```
TamilBookReader/
├── app/build.gradle                          # compileSdk 35, minSdk 26
├── app/src/main/
│   ├── assets/
│   │   ├── book_config.json                  # Book metadata + chapter list
│   │   └── content/chapter_0N.json           # Per-chapter sections
│   └── kotlin/com/tamilreader/app/
│       ├── BookReaderApp.kt                  # Application — bookRepo + bookmarks StateFlow
│       ├── MainActivity.kt
│       ├── domain/Models.kt                  # Book, Chapter, ChapterContent, Section, ReadingTheme
│       ├── data/
│       │   ├── BookRepository.kt             # Gson loader + in-memory cache
│       │   └── BookDto.kt                    # Raw JSON DTOs
│       └── presentation/
│           ├── navigation/AppNavigation.kt   # splash → index → reader/{idx}
│           ├── theme/ReaderTheme.kt          # LIGHT / DARK / SEPIA + CompositionLocals
│           ├── component/
│           │   ├── SectionRenderer.kt        # HEADING / PARAGRAPH / QUOTE
│           │   └── ChapterProgressBar.kt
│           └── screen/
│               ├── splash/   SplashScreen + SplashViewModel
│               ├── index/    IndexScreen  + IndexViewModel
│               └── reader/   ReaderScreen + ReaderViewModel
```

---

## Architecture rules

| Layer | Package | Rule |
|-------|---------|------|
| Domain | `domain/` | Pure Kotlin data classes + enums. No Android imports. |
| Data | `data/` | Repository only. Gson + assets. In-memory cache only (no Room yet). |
| Presentation | `presentation/` | MVVM. `AndroidViewModel` gets `Application` cast to `BookReaderApp`. |

- State flows up via `StateFlow`; UI collects with `collectAsStateWithLifecycle`.
- Bookmarks live in `BookReaderApp._bookmarks` (in-memory `Set<String>`). ViewModel never owns them.
- `BookRepository` is accessed via `(app as BookReaderApp).bookRepo` — no DI framework.

---

## Theme system

Three themes — `ReadingTheme.LIGHT / DARK / SEPIA` — cycle via the toolbar icon.
Theme is held in `MainActivity` and passed down through `AppNavigation → TamilReaderTheme`.

| Token | Accessed via |
|-------|-------------|
| Colors | `LocalReaderColors.current` → `ReaderColors` |
| Typography | `LocalReaderTypography.current` → `ReaderTypography` |

Font sizes (body 18sp / lineHeight 32sp) are fixed for Tamil readability — **do not reduce them**.

---

## Content JSON format

`book_config.json`
```json
{ "id": "…", "title": "…", "author": "…", "totalChapters": N,
  "chapters": [{ "id":"ch_01","index":0,"title":"…","contentPath":"content/chapter_01.json" }] }
```

`content/chapter_0N.json`
```json
{ "chapterId":"ch_01","title":"…",
  "sections":[{"type":"heading|paragraph|quote","content":"…"}] }
```

`SectionType.from(string)` is case-insensitive; unknown values → `PARAGRAPH`.

---

## Adding a new chapter

1. Create `app/src/main/assets/content/chapter_0N.json`.
2. Add entry to `book_config.json` chapters array, increment `totalChapters`.
3. No code changes needed.

## Adding a new book

1. Replace `book_config.json` and `content/` files.
2. Update `applicationId` / package name only if publishing as a separate app.

---

## Build & run

```bash
cd ~/Desktop/TamilBookReader
./gradlew assembleDebug                  # output: app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug                   # installs on connected device/emulator
```

Build takes ~60–90 s on first run; use `run_in_background: true`.

---

## Design rules (never break)

- **NO AUTHOR LINE** on any UI card or Canva export.
- Tamil body text: `18sp`, `lineHeight 32sp` — never reduce.
- QUOTE sections render with curly `"…"` quotes and italic style — keep that wrapper in `SectionRenderer`.
- Bookmark state is **in-memory only** (resets on app restart). If persistence is added, use `DataStore` (already in `build.gradle`).

---

## Key dependencies

| Library | Version | Use |
|---------|---------|-----|
| Compose BOM | 2024.02.00 | All Compose UI |
| Navigation Compose | 2.7.7 | Route: splash / index / reader/{idx} |
| Lifecycle ViewModel Compose | 2.7.0 | `AndroidViewModel` + `collectAsStateWithLifecycle` |
| Gson | 2.10.1 | JSON asset parsing |
| Coil Compose | 2.5.0 | Cover image (not yet wired) |
| DataStore Preferences | 1.0.0 | Ready for bookmark persistence |

---

## Common tasks

| Task | File(s) to touch |
|------|-----------------|
| Change theme colours | `ReaderTheme.kt` — `light()` / `dark()` / `sepia()` |
| Add a new section type | `Models.kt` `SectionType` + `SectionRenderer.kt` when block |
| Add cover image | `BookRepository` pass `coverImagePath`; wire `Coil` in `IndexScreen` |
| Persist bookmarks | `BookReaderApp` — replace `MutableStateFlow<Set>` with `DataStore` flow |
| Add font size setting | Add to `ReaderState`; multiply `t.body.fontSize` in `SectionRenderer` |
