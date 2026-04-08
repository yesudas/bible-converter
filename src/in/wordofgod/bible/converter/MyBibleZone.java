/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.BookID;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to MyBible.Zone SQLite3 format.
 * Output file is named after the bible abbreviation with .SQLite3 extension.
 */
public class MyBibleZone {

	// MyBible.Zone standard book colors
	private static final String COLOR_OT_LAW           = "#ff7b7b"; // Pentateuch
	private static final String COLOR_OT_HISTORY        = "#ff9d77"; // Historical books
	private static final String COLOR_OT_WISDOM         = "#fbdf7f"; // Wisdom/Poetry
	private static final String COLOR_OT_MAJOR_PROPHET  = "#92d46e"; // Major Prophets
	private static final String COLOR_OT_MINOR_PROPHET  = "#67b8d6"; // Minor Prophets
	private static final String COLOR_NT_GOSPEL         = "#ff7b7b"; // Gospels
	private static final String COLOR_NT_ACTS           = "#ff9d77"; // Acts
	private static final String COLOR_NT_EPISTLE        = "#fbdf7f"; // Epistles
	private static final String COLOR_NT_GENERAL_EP     = "#92d46e"; // General Epistles
	private static final String COLOR_NT_REVELATION     = "#67b8d6"; // Revelation
	private static final String COLOR_DEFAULT           = "#ffffff"; // Default/Apocrypha

	// Path inside the classpath where SQL seed files live
	private static final String SQL_RESOURCE_BASE = "in/wordofgod/bible/sql/MyBibleZone/";

	public static void createMyBibleZone() throws URISyntaxException {
		System.out.println("MyBible.Zone SQLite3 Creation Started...");
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

		String dbFileName = bible.getAbbr() + ".SQLite3";
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

			// --- Step 1: create all tables and seed from SQL files ---
			createTablesFromSql(db, "info.sql");
			createTablesFromSql(db, "books.sql");
			createTablesFromSql(db, "verses.sql");
			db.commit();

			// --- Step 2: populate bible-specific data ---
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			updateInfoRows(db, bible);
			insertBooksRows(db, bible);
			insertVersesRows(db, bible);
			db.commit();

			System.out.println("MyBible.Zone SQLite3 Creation Completed.");
			System.out.println("Output file: " + dbFile.getAbsolutePath());

		} catch (SqlJetException e) {
			System.err.println("Error creating SQLite3 database: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (db != null) {
				try { db.close(); } catch (SqlJetException e) { e.printStackTrace(); }
			}
		}
	}

	// -------------------------------------------------------------------------
	// SQL file import
	// -------------------------------------------------------------------------

	/**
	 * Reads a .sql seed file from the classpath and executes every
	 * CREATE TABLE / CREATE INDEX / INSERT statement it finds.
	 * BEGIN TRANSACTION and COMMIT lines are intentionally skipped —
	 * SQLjet manages its own transaction model.
	 */
	private static void createTablesFromSql(SqlJetDb db, String sqlFileName) throws SqlJetException {
		String sql = readSqlResource(sqlFileName);
		if (sql == null) {
			System.out.println("WARNING: SQL resource not found, skipping: " + sqlFileName);
			return;
		}
		executeSqlStatements(db, sql, sqlFileName);
		System.out.println("Imported SQL file: " + sqlFileName);
	}

	/**
	 * Reads the named SQL file from the classpath (in/wordofgod/bible/sql/MyBibleZone/).
	 * Returns null if not found.
	 */
	private static String readSqlResource(String fileName) {
		String resourcePath = SQL_RESOURCE_BASE + fileName;
		try {
			// Try classloader first (works in IDE and in a fat-jar)
			URL url = MyBibleZone.class.getClassLoader().getResource(resourcePath);
			if (url == null) {
				// Fallback: look relative to the source tree (for run-from-IDE without packaging)
				File f = new File("src/" + resourcePath);
				if (!f.exists()) f = new File(resourcePath);
				if (!f.exists()) return null;
				url = f.toURI().toURL();
			}
			try (InputStream is = url.openStream()) {
				return new String(is.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			System.err.println("Failed to read SQL resource: " + resourcePath + " — " + e.getMessage());
			return null;
		}
	}

	/**
	 * Splits the SQL content into individual statements (split on ';') and
	 * executes each one that is a CREATE TABLE, CREATE INDEX, or INSERT.
	 * SQLjet does not support raw SQL execution for INSERT via its public API,
	 * so INSERT statements are parsed and handled via table.insert().
	 */
	private static void executeSqlStatements(SqlJetDb db, String sql, String fileName) throws SqlJetException {
		// Normalise line endings
		sql = sql.replace("\r\n", "\n").replace("\r", "\n");

		// Split on semicolons to get individual statements
		String[] parts = sql.split(";");
		for (String part : parts) {
			String stmt = part.trim();
			if (stmt.isEmpty()) continue;
			// Skip transaction control — SQLjet handles this externally
			String upper = stmt.toUpperCase();
			if (upper.startsWith("BEGIN") || upper.startsWith("COMMIT") || upper.startsWith("ROLLBACK")) continue;

			if (upper.startsWith("CREATE TABLE")) {
				try {
					db.createTable(stmt);
				} catch (SqlJetException e) {
					// IF NOT EXISTS — table already created, ignore
					if (!e.getMessage().toLowerCase().contains("already exists")) {
						throw e;
					}
				}
			} else if (upper.startsWith("CREATE UNIQUE INDEX") || upper.startsWith("CREATE INDEX")) {
				try {
					db.createIndex(stmt);
				} catch (SqlJetException e) {
					if (!e.getMessage().toLowerCase().contains("already exists")) {
						throw e;
					}
				}
			} else if (upper.startsWith("INSERT INTO")) {
				executeSqlInsert(db, stmt, fileName);
			}
			// All other statement types (e.g. PRAGMA) are silently skipped
		}
	}

	/**
	 * Parses a single INSERT INTO "table" ("col1","col2",...) VALUES (...),(...)
	 * statement and inserts each row via ISqlJetTable.insert().
	 *
	 * SQLjet's ISqlJetTable.insert() accepts Object varargs in column-definition
	 * order, so we extract the column list from the INSERT statement and map each
	 * parsed value to the correct positional slot.
	 */
	private static void executeSqlInsert(SqlJetDb db, String stmt, String fileName) throws SqlJetException {
		try {
			// ---- parse table name ----
			// INSERT INTO "table_name" ...  or  INSERT INTO table_name ...
			String afterInsert = stmt.substring("INSERT INTO".length()).trim();
			String tableName;
			int nameEnd;
			if (afterInsert.startsWith("\"")) {
				nameEnd = afterInsert.indexOf('"', 1);
				tableName = afterInsert.substring(1, nameEnd);
				afterInsert = afterInsert.substring(nameEnd + 1).trim();
			} else {
				nameEnd = afterInsert.indexOf(' ');
				tableName = afterInsert.substring(0, nameEnd);
				afterInsert = afterInsert.substring(nameEnd).trim();
			}

			// ---- parse column list ----
			// ("col1","col2",...)
			int colStart = afterInsert.indexOf('(');
			int colEnd   = afterInsert.indexOf(')');
			String colSection = afterInsert.substring(colStart + 1, colEnd);
			String[] cols = parseSimpleCsvTokens(colSection);
			// strip surrounding quotes from column names
			for (int i = 0; i < cols.length; i++) {
				cols[i] = cols[i].trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
			}

			// ---- get table column order from DB ----
			// We need to know the canonical column order for table.insert()
			// We derive it by mapping the parsed column names to positional indices.

			// ---- parse VALUES section ----
			String afterCols = afterInsert.substring(colEnd + 1).trim();
			// Should start with "VALUES"
			if (!afterCols.toUpperCase().startsWith("VALUES")) {
				System.out.println("Skipping unparseable INSERT in " + fileName);
				return;
			}
			String valuesSection = afterCols.substring("VALUES".length()).trim();

			ISqlJetTable table = db.getTable(tableName);

			// Split individual row value groups: (...),(...)
			// We need to handle nested quotes and parentheses correctly
			java.util.List<String> rowGroups = splitValueGroups(valuesSection);

			for (String rowGroup : rowGroups) {
				// strip outer parens
				String inner = rowGroup.trim();
				if (inner.startsWith("(")) inner = inner.substring(1);
				if (inner.endsWith(")"))   inner = inner.substring(0, inner.length() - 1);

				String[] values = parseValues(inner);
				if (values.length != cols.length) {
					System.out.println("Column/value count mismatch in " + fileName
							+ " table=" + tableName + ", skipping row.");
					continue;
				}

				// Build Object[] in column-definition order using column names
				Object[] row = new Object[cols.length];
				for (int i = 0; i < cols.length; i++) {
					row[i] = parseSqlValue(values[i].trim());
				}

				try {
					table.insertByFieldNames(buildFieldMap(cols, row));
				} catch (SqlJetException e) {
					// Duplicate primary key — row already seeded, ignore
					if (!e.getMessage().toLowerCase().contains("constraint")) {
						throw e;
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof SqlJetException) throw (SqlJetException) e;
			System.err.println("Failed to parse INSERT statement in " + fileName + ": " + e.getMessage());
		}
	}

	/** Build a column-name → value map for insertByFieldNames */
	private static Map<String, Object> buildFieldMap(String[] cols, Object[] values) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < cols.length; i++) {
			map.put(cols[i], values[i]);
		}
		return map;
	}

	/**
	 * Splits  (v1,v2),(v3,v4)  into individual "(v1,v2)" strings,
	 * correctly handling quoted strings that may contain commas.
	 */
	private static java.util.List<String> splitValueGroups(String valuesSection) {
		java.util.List<String> groups = new java.util.ArrayList<>();
		int depth = 0;
		boolean inSingleQuote = false;
		int start = -1;
		for (int i = 0; i < valuesSection.length(); i++) {
			char c = valuesSection.charAt(i);
			if (c == '\'' && (i == 0 || valuesSection.charAt(i - 1) != '\\')) {
				inSingleQuote = !inSingleQuote;
			}
			if (!inSingleQuote) {
				if (c == '(') {
					if (depth == 0) start = i;
					depth++;
				} else if (c == ')') {
					depth--;
					if (depth == 0 && start >= 0) {
						groups.add(valuesSection.substring(start, i + 1));
						start = -1;
					}
				}
			}
		}
		return groups;
	}

	/**
	 * Splits the values inside a single row group, respecting quoted strings.
	 * e.g.:  'hello','world',42  →  ["'hello'", "'world'", "42"]
	 */
	private static String[] parseValues(String inner) {
		java.util.List<String> tokens = new java.util.ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean inQuote = false;
		for (int i = 0; i < inner.length(); i++) {
			char c = inner.charAt(i);
			if (c == '\'') {
				// check for escaped '' inside a string
				if (inQuote && i + 1 < inner.length() && inner.charAt(i + 1) == '\'') {
					cur.append('\'');
					i++; // skip the second quote
				} else {
					inQuote = !inQuote;
					cur.append(c);
				}
			} else if (c == ',' && !inQuote) {
				tokens.add(cur.toString());
				cur = new StringBuilder();
			} else {
				cur.append(c);
			}
		}
		tokens.add(cur.toString());
		return tokens.toArray(new String[0]);
	}

	/** Splits a simple CSV of column names (no nested parens needed) */
	private static String[] parseSimpleCsvTokens(String csv) {
		return csv.split(",");
	}

	/**
	 * Converts a raw SQL token to a Java object:
	 *   'text'   → String  (with '' → ' unescaping)
	 *   42       → Long
	 *   NULL     → null
	 */
	private static Object parseSqlValue(String token) {
		token = token.trim();
		if (token.equalsIgnoreCase("NULL")) return null;
		if (token.startsWith("'") && token.endsWith("'")) {
			String inner = token.substring(1, token.length() - 1);
			// unescape '' → '
			return inner.replace("''", "'");
		}
		try {
			return Long.parseLong(token);
		} catch (NumberFormatException e) {
			return token; // return as-is
		}
	}

	// -------------------------------------------------------------------------
	// info table  — update only the bible-specific rows; all other rows from
	// info.sql are kept intact.
	// -------------------------------------------------------------------------

	private static void updateInfoRows(SqlJetDb db, Bible bible) throws SqlJetException {
		String historyOfChanges = "("
				+ LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				+ ") Published";

		boolean isTamil = "ta".equalsIgnoreCase(bible.getLanguageCode());

		upsertInfo(db, "description",        bible.getCommonName());
		upsertInfo(db, "language",            bible.getLanguageCode());
		upsertInfo(db, "history_of_changes",  historyOfChanges);
		upsertInfo(db, "detailed_info",       buildDetailedInfo(bible));
		upsertInfo(db, "origin",
				"Created using BibleConverter program https://github.com/yesudas/bible-converter");
		upsertInfo(db, "html_style",
				"i  { color: %COLOR_BLUE%; } \n" +
				"em { color: %COLOR_BLUE%; } \n" +
				"a  { text-decoration: none; } \n" +
				"span.sc { font-variant: small-caps; } \n");
		upsertInfo(db, "chapter_string",      isTamil ? "அதிகாரம் %s"  : "Chapter %s");
		upsertInfo(db, "chapter_string_ps",   isTamil ? "சங்கீதம் %s"  : "Psalm %s");
		upsertInfo(db, "introduction_string", isTamil ? "மொழிபெயர்ப்பு விவரம்" : "About this Bible");
		upsertInfo(db, "strong_numbers",      "false");
		upsertInfo(db, "right_to_left",       "false");
		upsertInfo(db, "digits0-9",           "0123456789");

		System.out.println("Info rows updated.");
	}

	/**
	 * Updates the row if it already exists (seeded from info.sql), otherwise
	 * inserts it.
	 */
	private static void upsertInfo(SqlJetDb db, String name, String value) throws SqlJetException {
		if (value == null || value.trim().isEmpty()) {
			System.out.println("Skipping info row with empty value for name: " + name);
			return;
		}
		ISqlJetTable table = db.getTable("info");
		// Try to locate and update the existing row via the primary-key index
		ISqlJetCursor cursor = table.lookup(table.getPrimaryKeyIndexName(), name);
		try {
			if (!cursor.eof()) {
				cursor.update(name, value);
			} else {
				table.insert(name, value);
			}
		} finally {
			cursor.close();
		}
	}

	private static String buildDetailedInfo(Bible bible) {
		StringBuilder sb = new StringBuilder();
		sb.append("<p><b>Common Name:</b><br/> ").append(nullSafe(bible.getCommonName())).append("</p>\n");
		sb.append("<p><b>Short Name:</b><br/> ").append(nullSafe(bible.getAbbr())).append("</p>\n");
		sb.append("<p><b>Long Name:</b><br/> ").append(nullSafe(bible.getLongName())).append("</p>\n");
		sb.append("<p><b>Long English Name:</b><br/> ").append(nullSafe(bible.getLongEnglishName())).append("</p>\n");
		sb.append("<p><b>Published Year:</b><br/> ").append(nullSafe(bible.getPublishedYear())).append("</p>\n");
		sb.append("<p><b>Published By:</b><br/> ").append(nullSafe(bible.getPublishedBy())).append("</p>\n");
		sb.append("<p><b>Translated By:</b><br/> ").append(nullSafe(bible.getTranslatedBy())).append("</p>\n");
		sb.append("<p><b>Copyright:</b><br/> ").append(nullSafe(bible.getCopyRight())).append("</p>");
		return sb.toString();
	}

	// -------------------------------------------------------------------------
	// books table  (only books present in this bible)
	// -------------------------------------------------------------------------

	private static void insertBooksRows(SqlJetDb db, Bible bible) throws SqlJetException {
		ISqlJetTable table = db.getTable("books");
		int order = 0;
		for (Book book : bible.getBooks()) {
			int bookNumber = getBookNumber(book);
			if (bookNumber <= 0) {
				System.out.println("Skipping book (no MyBibleZone id): " + book.getLongName());
				continue;
			}
			String shortName = nullSafe(book.getShortName());
			String longName  = nullSafe(book.getLongName());
			if (shortName.isEmpty() || longName.isEmpty()) {
				System.out.println("Skipping book with empty name: " + book.getLongName());
				continue;
			}
			order++;
			// column order: book_number, short_name, long_name, book_color, sorting_order
			table.insert((long) bookNumber, shortName, longName, getBookColor(bookNumber), (long) order);
		}
		System.out.println("books rows inserted: " + order);
	}

	// -------------------------------------------------------------------------
	// verses table
	// -------------------------------------------------------------------------

	private static void insertVersesRows(SqlJetDb db, Bible bible) throws SqlJetException {
		ISqlJetTable table = db.getTable("verses");
		int totalVerses = 0;

		for (Book book : bible.getBooks()) {
			int bookNumber = getBookNumber(book);
			if (bookNumber <= 0) {
				System.out.println("Skipping verses for book (no MyBibleZone id): " + book.getLongName());
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
					// column order: book_number, chapter, verse, text
					table.insert((long) bookNumber, (long) chapterNum, (long) verseNum, verseText);
					totalVerses++;
				}
			}
		}
		System.out.println("verses rows inserted: " + totalVerses);
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static int getBookNumber(Book book) {
		if (book.getId() != null) {
			return book.getId().getMyBibleZoneId();
		}
		if (book.getBookId() != null && !book.getBookId().isEmpty()) {
			try {
				return BookID.fromOsisId(book.getBookId()).getMyBibleZoneId();
			} catch (IllegalArgumentException ignored) {
				// unknown OSIS id — fall through
			}
		}
		return book.getBookNo();
	}

	private static String getBookColor(int bookNumber) {
		if (bookNumber >= 10  && bookNumber <= 50)  return COLOR_OT_LAW;
		if (bookNumber >= 60  && bookNumber <= 190) return COLOR_OT_HISTORY;
		if (bookNumber >= 220 && bookNumber <= 260) return COLOR_OT_WISDOM;
		if (bookNumber >= 290 && bookNumber <= 340) return COLOR_OT_MAJOR_PROPHET;
		if (bookNumber >= 350 && bookNumber <= 460) return COLOR_OT_MINOR_PROPHET;
		if (bookNumber >= 470 && bookNumber <= 500) return COLOR_NT_GOSPEL;
		if (bookNumber == 510)                      return COLOR_NT_ACTS;
		if (bookNumber >= 520 && bookNumber <= 640) return COLOR_NT_EPISTLE;
		if (bookNumber >= 650 && bookNumber <= 720) return COLOR_NT_GENERAL_EP;
		if (bookNumber == 730)                      return COLOR_NT_REVELATION;
		return COLOR_DEFAULT;
	}

	private static String nullSafe(String value) {
		return value != null ? value : "";
	}
}