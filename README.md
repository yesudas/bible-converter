# bible-converter
Converts given Bible text in TheWord format to various output formats including JSON, text files, MS Word, and MyBible.Zone SQLite3 databases.

## Overview
This tool processes Bible texts in TheWord format (.ont, .ot, .nt) and converts them to multiple formats for different use cases. It supports multilingual Bible texts including English, Tamil, Hindi, Kannada, Telugu, Malayalam, Hebrew, Greek, Arabic, and more.

## Features
- **Multiple Output Formats**: Convert Bible text to JSON, plain text files, single text file, MS Word, MyBible.Zone SQLite3, and more
- **MyBible.Zone Support**: Generate ready-to-use `.SQLite3` database files for the MyBible.Zone app
- **Text Normalization**: Advanced text cleaning and normalization for Bible texts
- **Languages JSON Generation**: Automatically generate language metadata for multiple Bible versions
- **HTML Tag Removal**: Clean TheWord Bible modules by removing HTML formatting
- **Unicode Support**: Full UTF-8 support for multilingual Bible texts
- **Strong's Numbers**: Preserves Hebrew (WH) and Greek (WG) Strong's reference numbers
- **Batch Processing**: Process multiple Bible versions at once

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

## Supported Formats

### 1. **TextFiles**
Converts Bible text to individual text files organized by books and chapters.
```bash
java -jar bible-converter.jar TextFiles C:/taOV.ont C:/taOV-information.ini
```

### 2. **TextFilesByDirectory**
Similar to TextFiles, creates text files organized in directories by book.
```bash
java -jar bible-converter.jar TextFilesByDirectory C:/taOV.ont C:/taOV-information.ini
```

### 3. **JSON**
Converts Bible text to JSON format with structured data for books, chapters, and verses.
- Creates a `bibles.json` file with metadata
- Individual JSON files for each chapter
- Compact JSON format for smaller file sizes
```bash
java -jar bible-converter.jar JSON C:/taOV.ont C:/taOV-information.ini
```

### 4. **SingleTextFile**
Converts the entire Bible into a single text file.
```bash
java -jar bible-converter.jar SingleTextFile C:/taOV.ont C:/taOV-information.ini
```

### 5. **TheWordWithoutHtmlTags**
Creates a clean TheWord module by removing all HTML tags while preserving the text.
```bash
java -jar bible-converter.jar TheWordWithoutHtmlTags C:/taOV.ont C:/taOV-information.ini
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
java -jar bible-converter.jar NormalizeText C:/taOV.ont C:/taOV-information.ini
```

### 7. **CreateLanguagesJson**
Generates a comprehensive `languages.json` file from all `*-information.ini` files in the directory.
- Processes ALL `*-information.ini` files in the specified directory
- Groups Bibles by language
- Sorts by language code and Bible abbreviation
- Includes metadata (total languages, total Bibles, last updated date)
```bash
java -jar bible-converter.jar CreateLanguagesJson C:/taOV.ont C:/taOV-information.ini
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
    "lastUpdated": "2026-01-16",
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
- Preserves all original text including Strong's references
- Full Unicode support for multilingual content
```bash
java -jar bible-converter.jar MSWordByBooks C:/taOV.ont C:/taOV-information.ini
```

**Document Structure:**
```
Genesis                           (Heading 1, centered, bold, 24pt)

Genesis 1                         (Heading 2, bold, 18pt)

1. In the beginning God created the heaven and the earth.
2. And the earth was without form, and void...
...

Genesis 2                         (Heading 2, bold, 18pt)

1. Thus the heavens and the earth were finished...
...
```

### 9. **MyBibleZone**
Converts Bible text to a [MyBible.Zone](https://mybible.zone) compatible SQLite3 database file (`.SQLite3`), ready to be imported directly into the MyBible.Zone app on Android or iOS.

- Output file is named `[ABBR].SQLite3` (e.g., `TOV2017.SQLite3`)
- Creates all required MyBible.Zone tables: `info`, `books`, `verses`
- **`info` table** — populated with:
  - `description` — bible common name
  - `language` — ISO language code (e.g. `ta`, `en`)
  - `history_of_changes` — auto-filled with today's date in MyBible format
  - `detailed_info` — full HTML block with common name, short name, long name, published year, publisher, translator, copyright
  - `html_style` — standard MyBible CSS style rules
  - `chapter_string` — localised chapter label (`அதிகாரம் %s` for Tamil, `Chapter %s` for others)
  - `chapter_string_ps` — localised Psalms label (`சங்கீதம் %s` for Tamil, `Psalm %s` for others)
  - `introduction_string` — localised intro label
  - `strong_numbers`, `right_to_left`, `digits0-9` — standard MyBible metadata flags
  - `origin` — credits the BibleConverter program
- **`books` table** — one row per book present in the source file, with localised short and long names and colour-coded by testament/genre
- **`verses` table** — every verse from all books and chapters

```bash
java -jar bible-converter.jar MyBibleZone C:/taOV.ont C:/taOV-information.ini
```

**Output location:**
```
Output/MyBibleZone/[LanguageName]/[BibleAbbr]/[BibleAbbr].SQLite3
```
e.g.
```
Output/MyBibleZone/தமிழ்/TOV2017/TOV2017.SQLite3
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

## Supported Languages

The converter supports the following language codes with automatic language name detection:

| Language Code | Language Name | Script |
|---------------|---------------|--------|
| `en` | English | Latin |
| `ta` | தமிழ் | Tamil |
| `hi` | हिन्दी | Devanagari |
| `kn` | ಕನ್ನಡ | Kannada |
| `te` | తెలుగు | Telugu |
| `ml` | മലയാളം | Malayalam |
| `iw` / `he` | עברית | Hebrew |
| `grc` | Ἑλληνικὴ ἀρχαία | Greek (Ancient) |
| `el` | Ελληνικά | Greek (Modern) |
| `ar` | العربية | Arabic |
| `la` | Latina | Latin |

## Bible Information File Format

The Bible information file (`*-information.ini`) should follow this format:

```ini
bible.info.abbr=TOV2017
bible.info.commonName=Tamil One Version 2017
bible.info.shortName=Tamil OV 2017
bible.info.longName=Tamil One Version Bible 2017
bible.info.longEnglishName=Tamil One Version Bible 2017
bible.info.languageCode=ta
bible.info.publishedYear=2017
bible.info.publishedBy=Word of God Mission
bible.info.translatedBy=Tamil Bible Translation Team
bible.info.copyRight=© 2017 Word of God Mission. All rights reserved.

# Book names
book.name.1=ஆதியாகமம்
book.short.name.1=ஆதி
book.english.name.1=Genesis
# ... continue for all 66 books
```

## Where to Get Bible Databases?

1. Download the Bible Databases along with `*-information.ini` files from:
   - [Tamil Bibles](https://github.com/yesudas/all-bible-databases/tree/main/Bibles/TheWord-Bible-Databases/Tamil)
   - [Other Languages](https://github.com/yesudas/all-bible-databases/tree/main/Bibles/TheWord-Bible-Databases/)

2. Use these Bible databases for `[SOURCE-BIBLE-TEXT-FILE-PATH]`

## File Types

- **`.ont`** - Old and New Testament (Full Bible)
- **`.ot`** - Old Testament only
- **`.nt`** - New Testament only

## Examples

### Convert to JSON
```bash
java -jar bible-converter.jar JSON /path/to/bible.ont /path/to/bible-information.ini
```

### Normalize Text for Multiple Languages
```bash
java -jar bible-converter.jar NormalizeText /path/to/tamil-bible.ont /path/to/tamil-bible-information.ini
```

### Generate Languages Metadata
```bash
java -jar bible-converter.jar CreateLanguagesJson /path/to/any-bible.ont /path/to/any-information.ini
```
*Note: This will process ALL `*-information.ini` files in the directory.*

### Process Multiple Bibles at Once
```bash
java -jar bible-converter.jar JSON bible1.ont,bible2.ont,bible3.ont
```

## Output Directory Structure

The converter creates an `Output` directory with the following structure:

```
Output/
├── [Bible-Name]/
│   ├── bibles.json (for JSON format)
│   ├── books/ (for TextFiles format)
│   │   ├── 01-Genesis/
│   │   │   ├── chapter-1.txt
│   │   │   ├── chapter-2.txt
│   │   │   └── ...
│   │   └── ...
│   └── languages.json (for CreateLanguagesJson format)
```

## Text Normalization Rules

When using the `NormalizeText` format, the following transformations are applied:

1. **Number Formatting**: `6,000` → `6000`, `1,44,000` → `144000`
2. **Fractions**: `5 1/2` → `5.5`, `3 3/4` → `3.75`, ` 1/2 ` → ` 0.5 `
3. **Strong's References**: Preserves `<WH123>` and `<WG456>` tags
4. **HTML Cleanup**: Strips all HTML tags except Strong's references
5. **Entity Decoding**: `&amp;` → `&`, `&lt;` → `<`, `&quot;` → `"`
6. **Punctuation Spacing**: `word.word` → `word. word`, `word,word` → `word, word`
7. **Special Tags**: Replaces `<CL>Interlude<CL>` → `(Interlude)`
8. **Tag Removal**: Removes `<FR>`, `<Fr>`, `<CL>`, `<CM>`, `<pb/>`, `<J>`, `</J>`, `<f>...</f>`
9. **Tag Replacement**: `<n>` → `(`, `</n>` → `)`, `<RF>` → ` (`, `<Rf>` → `) `
10. **Whitespace**: Normalizes multiple spaces to single space, removes control characters

## Dependencies

- Java Runtime Environment 8 or higher
- Jackson JSON library (included)
- Jsoup HTML parser (included)
- Apache POI library (included) - for MS Word document generation
- Bible Parser library (included)

## Building from Source

1. Clone the repository
2. Import into your IDE (Eclipse, IntelliJ, or VS Code)
3. Build the project
4. The JAR file will be generated in the project directory

Alternatively, use the Ant build file:
```bash
ant -f build-ant.xml
```

## License

See LICENSE file for details.

## Support

For issues, questions, or contributions, please visit the project repository.

## Version History

- **Latest**: Added CreateLanguagesJson format, NormalizeText improvements, Strong's number support
- Previous versions: JSON conversion, text file generation, HTML tag removal