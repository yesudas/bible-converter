/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.BookID;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to MySword SQLite3 format (.bbl.mybible).
 * Output file is named after the bible abbreviation with .bbl.mybible extension.
 *
 * MySword database has two tables:
 *   - Bible   : Book (INT), Chapter (INT), Verse (INT), Scripture (TEXT)
 *   - Details : metadata about the Bible translation
 */
public class MySword {

	// Path inside the classpath where SQL seed files live
	private static final String SQL_RESOURCE_BASE = "in/wordofgod/bible/sql/MySword/";

	public static void createMySword() throws URISyntaxException {
		System.out.println("MySword SQLite3 Creation Started...");
		File file = new File(BibleConverter.bibleSourcePath);

		System.out.println("TheWord Bible loading started...");
		Bible bible;
		try {
			bible = TheWord.getBible(file.getAbsolutePath(), BibleConverter.bibleInformationPath);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (bible != null) {
			System.out.println("TheWord Bible loaded successfully...");
		}

		Utils.setOutputFolder(bible.getLanguageCode());

		// Create output directory
		File outputDir = new File(BibleConverter.outputPath);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
			System.out.println("Created output directory: " + outputDir.getAbsolutePath());
		}

		// MySword Bible module extension is .bbl.mybible
		String dbFileName = bible.getAbbr() + ".bbl.mybible";
		File dbFile = new File(BibleConverter.outputPath + File.separator + dbFileName);

		// Delete existing DB file so we start fresh
		if (dbFile.exists()) {
			dbFile.delete();
			System.out.println("Deleted existing database: " + dbFile.getAbsolutePath());
		}

		SqlJetDb db = null;
		try {
			db = SqlJetDb.open(dbFile, true);
			db.getOptions().setAutovacuum(true);
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			db.getOptions().setUserVersion(0);

			// --- Step 1: create tables from SQL files ---
			createTableFromSql(db, "Bible.sql");
			createTableFromSql(db, "Details.sql");
			db.commit();

			// --- Step 2: populate data ---
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			insertDetailsRow(db, bible);
			insertBibleRows(db, bible);
			db.commit();

			System.out.println("MySword SQLite3 Creation Completed.");
			System.out.println("Output file: " + dbFile.getAbsolutePath());

		} catch (SqlJetException e) {
			System.err.println("Error creating MySword database: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (db != null) {
				try { db.close(); } catch (SqlJetException e) { e.printStackTrace(); }
			}
		}
	}

	// -------------------------------------------------------------------------
	// Table creation from SQL files
	// -------------------------------------------------------------------------

	/**
	 * Reads a .sql seed file from the classpath and executes CREATE TABLE /
	 * CREATE INDEX statements. BEGIN TRANSACTION and COMMIT are skipped —
	 * SQLjet manages its own transaction model.
	 */
	private static void createTableFromSql(SqlJetDb db, String sqlFileName) throws SqlJetException {
		String sql = readSqlResource(sqlFileName);
		if (sql == null) {
			System.out.println("WARNING: SQL resource not found, skipping: " + sqlFileName);
			return;
		}

		// Normalise line endings and split on semicolons
		sql = sql.replace("\r\n", "\n").replace("\r", "\n");
		String[] parts = sql.split(";");
		for (String part : parts) {
			String stmt = part.trim();
			if (stmt.isEmpty()) continue;
			String upper = stmt.toUpperCase();
			// Skip transaction control — SQLjet handles this externally
			if (upper.startsWith("BEGIN") || upper.startsWith("COMMIT") || upper.startsWith("ROLLBACK")) continue;

			if (upper.startsWith("CREATE TABLE")) {
				try {
					db.createTable(stmt);
				} catch (SqlJetException e) {
					if (!e.getMessage().toLowerCase().contains("already exists")) throw e;
				}
			} else if (upper.startsWith("CREATE UNIQUE INDEX") || upper.startsWith("CREATE INDEX")) {
				try {
					db.createIndex(stmt);
				} catch (SqlJetException e) {
					if (!e.getMessage().toLowerCase().contains("already exists")) throw e;
				}
			}
		}
		System.out.println("Imported SQL file: " + sqlFileName);
	}

	/**
	 * Reads the named SQL file from the classpath (in/wordofgod/bible/sql/MySword/).
	 * Returns null if the resource cannot be found.
	 */
	private static String readSqlResource(String fileName) {
		String resourcePath = SQL_RESOURCE_BASE + fileName;
		try {
			java.net.URL url = MySword.class.getClassLoader().getResource(resourcePath);
			if (url == null) {
				// Fallback: look relative to the source tree (run-from-IDE without packaging)
				File f = new File("src/" + resourcePath);
				if (!f.exists()) f = new File(resourcePath);
				if (!f.exists()) return null;
				url = f.toURI().toURL();
			}
			try (java.io.InputStream is = url.openStream()) {
				return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			System.err.println("Failed to read SQL resource: " + resourcePath + " — " + e.getMessage());
			return null;
		}
	}

	// -------------------------------------------------------------------------
	// Details table  (single metadata row)
	// -------------------------------------------------------------------------

	/**
	 * Inserts a single row into the Details table with metadata about the
	 * Bible translation.
	 *
	 * Details columns:
	 *   Title, Description, Abbreviation, Comments, Version, VersionDate,
	 *   PublishDate, Publisher, Author, Creator, Source, EditorialComments,
	 *   Language, RightToLeft, OT, NT, Strong, VerseRules, CustomCSS
	 */
	private static void insertDetailsRow(SqlJetDb db, Bible bible) throws SqlJetException {
		ISqlJetTable table = db.getTable("Details");

		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		String title        = nullSafe(bible.getCommonName());
		String description  = buildDescription(bible);
		String abbreviation = nullSafe(bible.getAbbr());
		String comments     = nullSafe(bible.getCopyRight());
		String version      = "1.0";
		String versionDate  = today;
		String publishDate  = nullSafe(bible.getPublishedYear());
		String publisher    = nullSafe(bible.getPublishedBy());
		String author       = nullSafe(bible.getTranslatedBy());
		String creator      = "BibleConverter https://github.com/yesudas/bible-converter";
		String source       = "";
		String editComments = "";
		String language     = nullSafe(bible.getLanguageCode());
		long   rightToLeft  = isRightToLeft(bible.getLanguageCode()) ? 1L : 0L;
		long   ot           = bible.isHasOT() ? 1L : 0L;
		long   nt           = bible.isHasNT() ? 1L : 0L;
		long   strong       = hasStrongNumbers(bible) ? 1L : 0L;
		String verseRules   = "";
		String customCSS    = "";

		table.insert(
			title, description, abbreviation, comments,
			version, versionDate, publishDate, publisher,
			author, creator, source, editComments,
			language, rightToLeft, ot, nt, strong,
			verseRules, customCSS
		);
		System.out.println("Details row inserted.");
	}

	private static String buildDescription(Bible bible) {
			java.util.StringJoiner sj = new java.util.StringJoiner(". ");
			if (!nullSafe(bible.getCommonName()).isEmpty())    sj.add(bible.getCommonName());
			if (!nullSafe(bible.getPublishedBy()).isEmpty())   sj.add(bible.getPublishedBy());
			if (!nullSafe(bible.getPublishedYear()).isEmpty()) sj.add(bible.getPublishedYear());
			if (!nullSafe(bible.getTranslatedBy()).isEmpty())  sj.add(bible.getTranslatedBy());
			return sj.toString();
		}

	// -------------------------------------------------------------------------
	// Bible table  (one row per verse)
	// -------------------------------------------------------------------------

	/**
	 * Iterates all books → chapters → verses in the loaded Bible and inserts
	 * each verse into the Bible table.
	 *
	 * Bible columns: Book (INT), Chapter (INT), Verse (INT), Scripture (TEXT)
	 *
	 * MySword uses the same book-number scheme as MyBible.Zone
	 * (stored as an integer in the Book column).
	 */
	private static void insertBibleRows(SqlJetDb db, Bible bible) throws SqlJetException {
		ISqlJetTable table = db.getTable("Bible");
		int totalVerses = 0;

		for (Book book : bible.getBooks()) {
			int bookNumber = getBookNumber(book);
			if (bookNumber <= 0) {
				System.out.println("Skipping book (no MySword id): " + book.getLongName());
				continue;
			}

			int chapterCount = 0;
			for (Chapter chapter : book.getChapters()) {
				chapterCount++;
				String chapterStr = chapter.getChapter();
				if (chapterStr == null || chapterStr.isEmpty()) chapterStr = String.valueOf(chapterCount);
				int chapterNum;
				try {
					chapterNum = Integer.parseInt(chapterStr.trim());
				} catch (NumberFormatException e) {
					System.out.println("Invalid chapter number '" + chapterStr + "' in "
							+ book.getLongName() + ", using " + chapterCount);
					chapterNum = chapterCount;
				}

				for (Verse verse : chapter.getVerses()) {
					String verseText = verse.getUnParsedText();
					if (verseText == null || verseText.trim().isEmpty()) {
						System.out.println("Skipping empty verse: " + book.getLongName()
								+ " " + chapterNum + ":" + verse.getNumber());
						continue;
					}
					int verseNum;
					try {
						verseNum = Integer.parseInt(verse.getNumber().trim());
					} catch (NumberFormatException e) {
						System.out.println("Invalid verse number '" + verse.getNumber()
								+ "' in " + book.getLongName() + " " + chapterNum);
						continue;
					}
					// column order: Book, Chapter, Verse, Scripture
					table.insert((long) bookNumber, (long) chapterNum, (long) verseNum, verseText);
					totalVerses++;
				}
			}
		}
		System.out.println("Bible rows inserted: " + totalVerses);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static int getBookNumber(Book book) {
		if (book.getId() != null) {
			return book.getId().getZefID();
		}
		if (book.getBookId() != null && !book.getBookId().isEmpty()) {
			try {
				return BookID.fromOsisId(book.getBookId()).getZefID();
			} catch (IllegalArgumentException ignored) {
				// unknown OSIS id — fall through
			}
		}
		return book.getBookNo();
	}

	private static String nullSafe(String value) {
		return value != null ? value : "";
	}

	/**
	 * Returns true if the given ISO 639-1 language code is a known
	 * right-to-left language (Arabic, Hebrew, Farsi, Urdu, Syriac, Thaana, Yiddish).
	 */
	private static boolean isRightToLeft(String languageCode) {
		if (languageCode == null || languageCode.isEmpty()) return false;
		switch (languageCode.toLowerCase()) {
			case "ar": // Arabic
			case "he": // Hebrew
			case "fa": // Persian / Farsi
			case "ur": // Urdu
			case "syr": // Syriac
			case "dv": // Thaana (Dhivehi)
			case "yi": // Yiddish
				return true;
			default:
				return false;
		}
	}

	/**
	 * Detects whether any verse in the Bible contains TheWord-style Strong number
	 * tags ({@code <WH...>} for Hebrew or {@code <WG...>} for Greek).
	 * Returns true as soon as the first tagged verse is found.
	 */
	private static boolean hasStrongNumbers(Bible bible) {
		for (Book book : bible.getBooks()) {
			for (Chapter chapter : book.getChapters()) {
				for (Verse verse : chapter.getVerses()) {
					String text = verse.getUnParsedText();
					if (text != null && (text.contains("<WH") || text.contains("<WG"))) {
						return true;
					}
				}
			}
		}
		return false;
	}
}