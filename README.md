# bible-converter
Converts given Bible text in TheWord format to various output formats including JSON, text files, MS Word, MyBible.Zone SQLite3 databases, and HTML.

## Overview
This tool processes Bible texts in TheWord format (.ont, .ot, .nt) and converts them to multiple formats for different use cases. It supports multilingual Bible texts including English, Tamil, Hindi, Kannada, Telugu, Malayalam, Hebrew, Greek, Arabic, and more.

## Features
- **Multiple Output Formats**: Convert Bible text to JSON, plain text files, single text file, MS Word, MyBible.Zone SQLite3, HTML, and more
- **HTML Export**: Generate a fully styled, self-contained website with one page per book, table of contents, font size controls, and responsive design
- **MyBible.Zone Support**: Generate ready-to-use `.SQLite3` database files for the MyBible.Zone app
- **Text Normalization**: Advanced text cleaning and normalization for Bible texts
- **Languages JSON Generation**: Automatically generate language metadata for multiple Bible versions
- **HTML Tag Removal**: Clean TheWord Bible modules by removing HTML formatting
- **Unicode Support**: Full UTF-8 support for multilingual Bible texts
- **Strong's Numbers**: Preserves Hebrew (WH) and Greek (WG) Strong's reference numbers
- **Batch Processing**: Process multiple Bible versions at once
- **Cross-Platform**: Uses `File.separator` throughout — works correctly on Windows, macOS, and Linux

## Installation

### From Source
If you clone from Git or download a source zip, you will need a Java JDK 8 or above (tested up to 11) to build. You can use Eclipse, IntelliJ, or Visual Studio Code IDEs to build the binary and you will find a suitable distribution .jar file in the project folder.

### From JAR
If you download a bible-converter.jar file, you will need a Java Runtime Environment 8 or above, available from [https://www.oracle.com/in/java/technologies/downloads/](https://www.oracle.com/in/java/technologies/downloads/).

## Usage

### Basic Command
Just run:
```bash
java -jar bible-converter.jar
```
on the command line for usage information.

### Syntax
```bash
java -jar bible-converter.jar [OUTPUT-FORMAT] [SOURCE-BIBLE-TEXT-FILE-PATH] [BIBLE-INFORMATION-FILE-PATH]
```

**Note:** `[BIBLE-INFORMATION-FILE-PATH]` is optional. If not provided, the program will look for a file with the pattern `[SOURCE-FILE-NAME]-information.ini` in the same directory.

### Output Directory Structure
All output is written under an `Output/` folder next to the JAR file, organised by format, language, and Bible abbreviation:
```
Output/
├── HTML/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── style.css
│           ├── index.html
│           ├── 01-Genesis.html
│           └── ...
├── JSON/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── bibles.json
│           └── 1-Genesis/
│               ├── 1.json
│               └── ...
├── MSWordByBooks/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── TRHE1836-01-Genesis.docx
│           └── ...
├── MyBibleZone/
│   └── தமிழ்/
│       └── TRHE1836/
│           └── TRHE1836.SQLite3
├── SingleTextFile/
│   └── தமிழ்/
│       └── TRHE1836/
│           └── TRHE1836.txt
├── TextFiles/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── 01 Genesis/
│           │   ├── 01.txt
│           │   └── ...
│           └── ...
└── ...
```

## Supported Formats

### 1. **TextFiles**
Converts Bible text to individual text files organised by book directories and chapter files.
- One directory per book (named `[BookNo] [BookName]`)
- One text file per chapter (named `[ChapterNo].txt`)
- Each verse on its own line with format: `[VerseNumber]. [VerseText]`
```bash
java -jar bible-converter.jar TextFiles /path/to/taOV.ont /path/to/taOV-information.ini
```

### 2. **TextFilesByDirectory**
Identical to `TextFiles` — creates text files organised in directories by book.
```bash
java -jar bible-converter.jar TextFilesByDirectory /path/to/taOV.ont /path/to/taOV-information.ini
```

### 3. **JSON**
Converts Bible text to JSON format with structured data for books, chapters, and verses.
- Creates a `bibles.json` file with full Bible metadata
- Individual JSON files for each chapter under `[BookNo]-[BookName]/[Chapter].json`
- Compact JSON format for smaller file sizes
```bash
java -jar bible-converter.jar JSON /path/to/taOV.ont /path/to/taOV-information.ini
```

### 4. **SingleTextFile**
Converts the entire Bible into a single text file named `[ABBR].txt`.
- Chapter headings in the format: `[BookName] [ChapterNumber]`
- Each verse on its own line with format: `[VerseNumber]. [VerseText]`
```bash
java -jar bible-converter.jar SingleTextFile /path/to/taOV.ont /path/to/taOV-information.ini
```

### 5. **TheWordWithoutHtmlTags**
Creates a clean TheWord module by removing all HTML tags while preserving the text, saved as `[ABBR].txt`.
```bash
java -jar bible-converter.jar TheWordWithoutHtmlTags /path/to/taOV.ont /path/to/taOV-information.ini
```

### 6. **NormalizeText**
Advanced text normalization with the following features:
- Removes commas from numbers (6,000 → 6000, 1,44,000 → 144000)
- Converts fractions (5 1/2 → 5.5, 3 3/4 → 3.75)
- Strips HTML tags and decodes HTML entities
- Preserves Strong's references (`<WH123>` and `<WG456>`)
- Adds proper spacing after punctuation
- Removes control characters and non-breaking spaces
- Normalizes multiple spaces to single space
- Replaces specific Bible formatting tags
```bash
java -jar bible-converter.jar NormalizeText /path/to/taOV.ont /path/to/taOV-information.ini
```

### 7. **CreateLanguagesJson**
Generates a comprehensive `languages.json` file from all `*-information.ini` files in the directory.
- Processes ALL `*-information.ini` files in the specified directory
- Groups Bibles by language
- Sorts by language code and Bible abbreviation
- Includes metadata (total languages, total Bibles, last updated date)
```bash
java -jar bible-converter.jar CreateLanguagesJson /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output Example:**
```json
{
  "biblesByLanguage": {
    "தமிழ்": {
      "languageCode": "ta",
      "languageName": "தமிழ்",
      "bibles": [
        {
          "abbr": "TOV2017",
          "isDefault": false,
          "commonName": "Tamil One Version 2017",
          "hide": false
        }
      ]
    }
  },
  "metadata": {
    "totalLanguages": 1,
    "totalBibles": 1,
    "lastUpdated": "2026-04-08",
    "supportedLanguages": ["தமிழ்"]
  }
}
```

### 8. **MSWordByBooks**
Converts Bible text to Microsoft Word (.docx) format with separate files for each book.
- Creates one Word document per book
- File naming: `[ABBR]-[BookNumber]-[BookName].docx` (e.g., `TOV2017-01-Genesis.docx`)
- Book title as Heading 1 (centered, bold, 24pt)
- Chapter headings as Heading 2 (bold, 18pt) with format: `[BookName] [ChapterNumber]`
- Each verse on a separate line with format: `[VerseNumber]. [VerseText]`
- Full Unicode support for multilingual content
```bash
java -jar bible-converter.jar MSWordByBooks /path/to/taOV.ont /path/to/taOV-information.ini
```

**Document Structure:**
```
Genesis                           (Heading 1, centered, bold, 24pt)

Genesis 1                         (Heading 2, bold, 18pt)

1. In the beginning God created the heaven and the earth.
2. And the earth was without form, and void...

Genesis 2                         (Heading 2, bold, 18pt)

1. Thus the heavens and the earth were finished...
```

### 9. **MyBibleZone**
Converts Bible text to a [MyBible.Zone](https://mybible.zone) compatible SQLite3 database file (`.SQLite3`), ready to be imported directly into the MyBible.Zone app on Android or iOS.

- Output file is named `[ABBR].SQLite3` (e.g., `TOV2017.SQLite3`)
- Creates all required MyBible.Zone tables: `info`, `books`, `verses`
- **`info` table** — populated with:
  - `description` — Bible common name
  - `language` — ISO language code (e.g. `ta`, `en`)
  - `history_of_changes` — auto-filled with today's date
  - `detailed_info` — full HTML block with common name, short name, long name, published year, publisher, translator, copyright
  - `html_style` — standard MyBible CSS style rules
  - `chapter_string` — localised chapter label (`அதிகாரம் %s` for Tamil, `Chapter %s` for others)
  - `chapter_string_ps` — localised Psalms label (`சங்கீதம் %s` for Tamil, `Psalm %s` for others)
  - `introduction_string` — localised intro label
  - `strong_numbers`, `right_to_left`, `digits0-9` — standard MyBible metadata flags
  - `origin` — credits the BibleConverter program
- **`books` table** — one row per book, with localised names and colour-coded by testament/genre
- **`verses` table** — every verse from all books and chapters

```bash
java -jar bible-converter.jar MyBibleZone /path/to/taOV.ont /path/to/taOV-information.ini
```

**Book Colour Scheme:**

| Testament / Genre | Colour |
|---|---|
| OT Pentateuch (Gen–Deut) | `#ff7b7b` |
| OT History (Josh–Esth) | `#ff9d77` |
| OT Wisdom (Job–Song) | `#fbdf7f` |
| OT Major Prophets (Isa–Dan) | `#92d46e` |
| OT Minor Prophets (Hos–Mal) | `#67b8d6` |
| NT Gospels (Matt–John) | `#ff7b7b` |
| NT Acts | `#ff9d77` |
| NT Pauline Epistles (Rom–Phm) | `#fbdf7f` |
| NT General Epistles (Heb–Jude) | `#92d46e` |
| NT Revelation | `#67b8d6` |

### 10. **HTML**
Converts Bible text to a fully styled, self-contained HTML website — one page per book plus a main `index.html` with table of contents. Open `index.html` in any browser; no server required.

- **`index.html`** — includes full Bible metadata and a linked table of contents
- **One HTML file per book** — named `[BookNo]-[EnglishBookName].html` (e.g., `40-Matthew.html`)
- **`style.css`** — single shared stylesheet, saved alongside the HTML files
- **Chapter navigation bar** at the top of each book page — click any chapter number to jump directly to it
- **One verse per line** with superscript verse numbers
- **Font size controls** (A− / A / A+) in the header — preference is saved in `localStorage` and persists across all pages and browser sessions
- **Responsive design** — works on desktop and mobile
- **Footer** on every page with a link to the BibleConverter program and [www.WordOfGod.in](https://www.WordOfGod.in)

```bash
java -jar bible-converter.jar HTML /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/HTML/தமிழ்/TRHE1836/
├── style.css
├── index.html
├── 01-Genesis.html
├── 02-Exodus.html
├── ...
└── 66-Revelation.html
```

**index.html includes:**
| Field | Source |
|---|---|
| Abbr | `bible.getAbbr()` |
| Common Name | `bible.getCommonName()` |
| Short Name | `bible.getShortName()` |
| Long Name | `bible.getLongName()` |
| Long English Name | `bible.getLongEnglishName()` |
| Language Code | `bible.getLanguageCode()` |
| Published Year | `bible.getPublishedYear()` |
| Published By | `bible.getPublishedBy()` |
| Translated By | `bible.getTranslatedBy()` |
| Copyright | `bible.getCopyRight()` |
| Additional Information | `bible.getAdditionalInformation()` |
| Total Books | `bible.getTotalBooks()` |
| Total Chapters | `bible.getTotalChapters()` |
| Total Verses | `bible.getTotalVerses()` |
| Total Words | `bible.getTotalWords()` |
| Total Unique Words | `bible.getTotalUniqueWords()` |
| Has OT | `bible.isHasOT()` |
| Has NT | `bible.isHasNT()` |
| Generated By | Link to BibleConverter on GitHub |

## Supported Languages

| Code | Language |
|---|---|
| `ta` | தமிழ் (Tamil) |
| `en` | English |
| `kn` | ಕನ್ನಡ (Kannada) |
| `te` | తెలుగు (Telugu) |
| `hi` | हिन्दी (Hindi) |
| `ml` | മലയാളം (Malayalam) |
| `he` / `iw` | Hebrew |
| `grc` / `el` | Greek |
| `ar` | Arabic |
| `la` | Latin |
| `bn` | Bengali |
| `gu` | Gujarati |
| `mr` | Marathi |
| `ne` | Nepali |
| `or` | Odia |
| `pa` | Punjabi |
| `sa` | Sanskrit |
| `si` | Sinhala |
| `ur` | Urdu |
| `ms` | Malay |
| `awa` | Awadhi |
| `mai` | Maithili |
| `mni` | Manipuri |

## Bible Information File (`-information.ini`)

Each Bible source file should have a corresponding `-information.ini` file with the following keys:

```ini
bible.info.abbr=TRHE1836
bible.info.commonName=Tamil Revised Henry Edition 1836
bible.info.shortName=TRHE
bible.info.longName=தமிழ் திருத்திய ஹென்றி பதிப்பு 1836
bible.info.longEnglishName=Tamil Revised Henry Edition 1836
bible.info.languageCode=ta
bible.info.publishedYear=1836
bible.info.publishedBy=Publisher Name
bible.info.translatedBy=Translator Name
bible.info.copyright=Public Domain
```

See `sample-information.ini` in the project root for a full example.

## Batch Processing
To process multiple Bible files at once, separate the paths with a comma:
```bash
java -jar bible-converter.jar JSON /path/to/taOV.ont,/path/to/enKJV.ont
```

## Links
- **Project**: [https://github.com/yesudas/bible-converter](https://github.com/yesudas/bible-converter)
- **Website**: [https://www.WordOfGod.in](https://www.WordOfGod.in)