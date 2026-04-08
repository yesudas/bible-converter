# bible-converter
Converts given Bible text in TheWord format to various output formats including JSON, text files, MS Word, MyBible.Zone SQLite3 databases, HTML, and ThML.

## Overview
This tool processes Bible texts in TheWord format (.ont, .ot, .nt) and converts them to multiple formats for different use cases. It supports multilingual Bible texts including English, Tamil, Hindi, Kannada, Telugu, Malayalam, Hebrew, Greek, Arabic, and more.

## Features
- **Multiple Output Formats**: Convert Bible text to JSON, JsonBible, ThML, plain text files, single text file, MS Word, MyBible.Zone SQLite3, HTML, and more
- **ThML Export**: Generate [Theological Markup Language](https://www.ccel.org/ThML/ThML1.04.htm) (ThML 1.04) files with full metadata, one file per book and a master index
- **HTML Export**: Generate a fully styled, self-contained website with one page per book, table of contents, font size controls, and responsive design
- **MyBible.Zone Support**: Generate ready-to-use `.SQLite3` database files for the MyBible.Zone app
- **Text Normalization**: Advanced text cleaning and normalization for Bible texts
- **Languages JSON Generation**: Automatically generate language metadata for multiple Bible versions
- **HTML Tag Removal**: Clean TheWord Bible modules by removing HTML formatting
- **Unicode Support**: Full UTF-8 support for multilingual Bible texts
- **Strong's Numbers**: Preserves Hebrew (WH) and Greek (WG) Strong's reference numbers
- **Batch Processing**: Process multiple Bible versions at once
- **Cross-Platform**: Uses `File.separator` throughout — works correctly on Windows, macOS, and Linux

## Supported Output Formats
- TextFiles
- TextFilesByDirectory
- JSON
- SingleTextFile
- TheWordWithoutHtmlTags
- NormalizeText
- CreateLanguagesJson
- MSWordByBooks
- MyBibleZone
- HTML
- JsonBible
- ThML
- ThMLSingle

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
├── JsonBible/
│   └── தமிழ்/
│       └── TRHE1836/
│           └── TRHE1836.json
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
│           ├── 01 Genesis.txt
│           ├── 02 Exodus.txt
│           └── ...
├── TextFilesByDirectory/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── 01 Genesis/
│           │   ├── 01.txt
│           │   └── ...
│           └── ...
├── ThML/
│   └── தமிழ்/
│       └── TRHE1836/
│           ├── TRHE1836-index.thml
│           ├── 01-Genesis.thml
│           ├── 02-Exodus.thml
│           └── ...
├── ThMLSingle/
│   └── தமிழ்/
│       └── TRHE1836/
│           └── TRHE1836.thml
└── ...
```

## Supported Formats

### 1. **TextFiles**
Converts Bible text to one text file per book, with all chapters included inline.
- One text file per book (named `[BookNo] [BookName].txt`, e.g., `01 Genesis.txt`)
- Chapter headings in the format: `[BookName] [ChapterNumber]`
- Each verse on its own line with format: `[VerseNumber]. [VerseText]`
- Blank line after each chapter
```bash
java -jar bible-converter.jar TextFiles /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/TextFiles/தமிழ்/TRHE1836/
├── 01 Genesis.txt
├── 02 Exodus.txt
├── ...
└── 66 Revelation.txt
```

### 2. **TextFilesByDirectory**
Converts Bible text to individual chapter files organised in book directories.
- One directory per book (named `[BookNo] [BookName]`)
- One text file per chapter inside each book directory (named `[ChapterNo].txt`)
- Each verse on its own line with format: `[VerseNumber]. [VerseText]`
```bash
java -jar bible-converter.jar TextFilesByDirectory /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/TextFilesByDirectory/தமிழ்/TRHE1836/
├── 01 Genesis/
│   ├── 01.txt
│   ├── 02.txt
│   └── ...
├── 02 Exodus/
│   └── ...
└── ...
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

### 11. **JsonBible**
Converts Bible text to [JsonBible](https://github.com/ChurchApps/json-bible) format — a single, self-contained JSON file containing all books, chapters, and verses together with rich metadata.

- Output file is named `[ABBR].json` (e.g., `TRHE1836.json`)
- Complies with the JsonBible schema defined at [Bible.ts](https://github.com/ChurchApps/json-bible/blob/main/lib/Bible.ts)

```bash
java -jar bible-converter.jar JsonBible /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/JsonBible/தமிழ்/TRHE1836/
└── TRHE1836.json
```

**JSON structure:**
```json
{
  "name": "Bible Name",
  "metadata": { "title": "Bible Name", "identifier": "ABBR", "publisher": "...", "...": "..." },
  "books": [
    {
      "number": 1,
      "name": "Genesis",
      "chapters": [
        {
          "number": 1,
          "verses": [
            { "number": 1, "text": "In the beginning God created..." }
          ]
        }
      ]
    }
  ]
}
```

**Metadata fields:**

| Field | Source |
|---|---|
| title | `bible.getCommonName()` |
| identifier | `bible.getAbbr()` |
| description | `bible.getTranslatedBy() + ". " + bible.getAdditionalInformation()` |
| language | `bible.getLanguageCode()` |
| publisher | `bible.getPublishedBy()` |
| publishDate | `bible.getPublishedYear()` |
| copyright | `bible.getCopyRight()` |
| additionalInformation | `bible.getAdditionalInformation()` |
| translatedBy | `bible.getTranslatedBy()` |
| commonName | `bible.getCommonName()` |
| shortName | `bible.getShortName()` |
| longName | `bible.getLongName()` |
| longEnglishName | `bible.getLongEnglishName()` |
| totalBooks | `bible.getTotalBooks()` |
| totalChapters | `bible.getTotalChapters()` |
| totalVerses | `bible.getTotalVerses()` |
| totalWords | `bible.getTotalWords()` |
| totalUniqueWords | `bible.getTotalUniqueWords()` |
| hasOT | `bible.isHasOT()` |
| hasNT | `bible.isHasNT()` |
| generatedBy | `Created using BibleConverter from https://github.com/yesudas/bible-converter` |

### 12. **ThML**
Converts Bible text to [Theological Markup Language](https://www.ccel.org/ThML/ThML1.04.htm) (ThML 1.04) format — the standard used by the [Christian Classics Ethereal Library (CCEL)](https://www.ccel.org). ThML is an XML-based format that extends HTML 4.0 with theology-specific elements for scripture references, metadata, and hierarchical document structure.

- **One `.thml` file per book** — named `[BookNo]-[EnglishBookName].thml` (e.g., `40-Matthew.thml`)
- **One master index file** — named `[ABBR]-index.thml` with full Bible metadata and links to all book files
- **Full `<ThML.head>` metadata** — `<electronicEdInfo>` block with title, creator, publisher, description, date, identifier, language, rights, source
- **`<revisionHistory>`** — auto-filled with today's date and BibleConverter credit
- **Hierarchical structure**:
  - Each book is a `<div1 type="book">` with a `<head>` element
  - Each chapter is a `<div2 type="chapter">` with `n` (chapter number) and `id` (OSIS-style reference, e.g. `Matt.1`) attributes
  - Each verse is a `<scripRef>` element with a `passage` attribute (OSIS-style reference, e.g. `Matt.1.1`) and a superscript verse number
- **OSIS-style passage references** on every verse (e.g. `Gen.1.1`, `Matt.5.3`)
- **Full XML escaping** — all text is properly escaped for valid XML output

```bash
java -jar bible-converter.jar ThML /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/ThML/தமிழ்/TRHE1836/
├── TRHE1836-index.thml
├── 01-Genesis.thml
├── 02-Exodus.thml
├── ...
└── 66-Revelation.thml
```

**File structure example (`01-Genesis.thml`):**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE ThML PUBLIC "-//CCEL//DTD ThML 1.04//EN"
  "http://www.ccel.org/ThML/ThML1.04.dtd">
<ThML>
  <ThML.head>
    <electronicEdInfo>
      <title>Bible Common Name</title>
      <creator role="translator">Translator Name</creator>
      <publisher>Publisher Name</publisher>
      <subject>Bible</subject>
      <description>Long Bible Name</description>
      <date>1836</date>
      <type>Text</type>
      <format>ThML</format>
      <identifier>TRHE1836</identifier>
      <language>ta</language>
      <rights>Public Domain</rights>
      <source>Converted using BibleConverter program https://github.com/yesudas/bible-converter</source>
    </electronicEdInfo>
    <revisionHistory>
      <revision date="2026-04-08">
        <description>Created using BibleConverter program https://github.com/yesudas/bible-converter</description>
      </revision>
    </revisionHistory>
  </ThML.head>
  <ThML.body>
    <div1 type="book" id="Gen">
      <head>Genesis</head>
      <div2 type="chapter" n="1" id="Gen.1">
        <head>Genesis 1</head>
        <scripRef passage="Gen.1.1"><sup>1</sup> In the beginning God created the heaven and the earth.</scripRef>
        <scripRef passage="Gen.1.2"><sup>2</sup> And the earth was without form, and void...</scripRef>
      </div2>
    </div1>
  </ThML.body>
</ThML>
```

**ThML head metadata fields:**

| ThML element | Source |
|---|---|
| `<title>` | `bible.getCommonName()` |
| `<creator role="translator">` | `bible.getTranslatedBy()` |
| `<publisher>` | `bible.getPublishedBy()` |
| `<description>` | `bible.getLongName()` |
| `<date>` | `bible.getPublishedYear()` |
| `<identifier>` | `bible.getAbbr()` |
| `<language>` | `bible.getLanguageCode()` |
| `<rights>` | `bible.getCopyRight()` |
| `<source>` | BibleConverter GitHub link |

### 13. **ThMLSingle**
Same as **ThML** but outputs the entire Bible — all books, all chapters, all verses — into a **single `.thml` file** instead of one file per book.

The ThML 1.04 specification fully supports this through its hierarchical `<div>` structure. All books sit as sibling `<div1 type="book">` elements inside one `<ThML.body>`, with the same `<ThML.head>` metadata block.

- **Single output file** named `[ABBR].thml` (e.g., `TRHE1836.thml`)
- Same full `<ThML.head>` metadata as the multi-file format
- All books as `<div1 type="book">` elements
- All chapters as `<div2 type="chapter">` elements with OSIS-style `id` (e.g. `Gen.1`)
- All verses as `<scripRef passage="...">` elements with OSIS-style passage references (e.g. `Gen.1.1`)
- Suitable for tools and readers that expect a single self-contained ThML document

```bash
java -jar bible-converter.jar ThMLSingle /path/to/taOV.ont /path/to/taOV-information.ini
```

**Output structure:**
```
Output/ThMLSingle/தமிழ்/TRHE1836/
└── TRHE1836.thml
```

**Comparison:**

| Feature | ThML | ThMLSingle |
|---|---|---|
| Output files | One per book + index | One file for entire Bible |
| Index file | `[ABBR]-index.thml` | — |
| File size | Small per file | Large single file |
| Best for | Book-by-book browsing, linking | Single-document tools, archiving |

## Supported Languages

Supports all languages

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