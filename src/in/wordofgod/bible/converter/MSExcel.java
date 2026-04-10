/**
 *
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.BookID;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to Microsoft Excel (.xlsx) format.
 * Output file is named after the Bible abbreviation with .xlsx extension.
 *
 * The workbook contains four sheets:
 *   1. info       — key/value metadata about the Bible translation
 *   2. verses     — every verse (book no, book name, chapter, verse, text)
 *   3. books      — books present in this Bible with chapter counts
 *   4. books_all  — all 66 canonical books, flagging which ones are present
 */
public class MSExcel {

	public static void createMSExcel() throws URISyntaxException {
		System.out.println("MS Excel Creation Started...");
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

		String fileName = bible.getAbbr() + ".xlsx";
		String filePath = BibleConverter.outputPath + File.separator + fileName;

		try (XSSFWorkbook workbook = new XSSFWorkbook()) {

			// Pre-build shared styles
			Styles styles = new Styles(workbook);

			// ---- sheets ----
			createInfoSheet(workbook, styles, bible);
			createVersesSheet(workbook, styles, bible);
			createBooksSheet(workbook, styles, bible);
			createBooksAllSheet(workbook, styles, bible);

			// ---- write ----
			try (FileOutputStream out = new FileOutputStream(filePath)) {
				workbook.write(out);
			}

			System.out.println("MS Excel Creation Completed.");
			System.out.println("Output file: " + filePath);

		} catch (IOException e) {
			System.err.println("Error creating Excel file: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------
	// Sheet 1 — info
	// -------------------------------------------------------------------------

	/**
	 * Writes Bible metadata as key/value pairs.
	 *
	 * Columns: Property | Value
	 */
	private static void createInfoSheet(XSSFWorkbook wb, Styles s, Bible bible) {
		Sheet sheet = wb.createSheet("info");
		sheet.setColumnWidth(0, 36 * 256);
		sheet.setColumnWidth(1, 80 * 256);

		Row header = sheet.createRow(0);
		createHeaderCell(header, 0, "Property", s.header);
		createHeaderCell(header, 1, "Value",    s.header);

		String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		Object[][] rows = {
			{ "Abbreviation",          nullSafe(bible.getAbbr()) },
			{ "Common Name",           nullSafe(bible.getCommonName()) },
			{ "Short Name",            nullSafe(bible.getShortName()) },
			{ "Long Name",             nullSafe(bible.getLongName()) },
			{ "Long English Name",     nullSafe(bible.getLongEnglishName()) },
			{ "Language Code",         nullSafe(bible.getLanguageCode()) },
			{ "Published Year",        nullSafe(bible.getPublishedYear()) },
			{ "Published By",          nullSafe(bible.getPublishedBy()) },
			{ "Translated By",         nullSafe(bible.getTranslatedBy()) },
			{ "Copyright",             nullSafe(bible.getCopyRight()) },
			{ "Additional Information",nullSafe(bible.getAdditionalInformation()) },
			{ "Has OT",                bible.isHasOT() ? "Yes" : "No" },
			{ "Has NT",                bible.isHasNT() ? "Yes" : "No" },
			{ "Total Books",           bible.getTotalBooks() },
			{ "Total Chapters",        bible.getTotalChapters() },
			{ "Total Verses",          bible.getTotalVerses() },
			{ "Total Words",           bible.getTotalWords() },
			{ "Total Unique Words",    bible.getTotalUniqueWords() },
			{ "Generated Date",        today },
			{ "Generated By",          "BibleConverter https://github.com/yesudas/bible-converter" },
		};

		for (int i = 0; i < rows.length; i++) {
			Row row = sheet.createRow(i + 1);
			createLabelCell(row, 0, rows[i][0].toString(), s.label);
			Object val = rows[i][1];
			if (val instanceof Integer) {
				Cell c = row.createCell(1);
				c.setCellValue((Integer) val);
				c.setCellStyle(s.number);
			} else {
				createValueCell(row, 1, val.toString(), s.value);
			}
		}
	}

	// -------------------------------------------------------------------------
	// Sheet 2 — verses
	// -------------------------------------------------------------------------

	/**
	 * Writes every verse.
	 *
	 * Columns: Book No | Book Name | Chapter | Verse | Scripture
	 */
	private static void createVersesSheet(XSSFWorkbook wb, Styles s, Bible bible) {
		Sheet sheet = wb.createSheet("verses");
		sheet.setColumnWidth(0,  8 * 256);   // Book No
		sheet.setColumnWidth(1, 28 * 256);   // Book Name
		sheet.setColumnWidth(2,  8 * 256);   // Chapter
		sheet.setColumnWidth(3,  8 * 256);   // Verse
		sheet.setColumnWidth(4, 80 * 256);   // Scripture
		sheet.createFreezePane(0, 1);        // freeze header row

		Row header = sheet.createRow(0);
		createHeaderCell(header, 0, "Book No",   s.header);
		createHeaderCell(header, 1, "Book Name", s.header);
		createHeaderCell(header, 2, "Chapter",   s.header);
		createHeaderCell(header, 3, "Verse",     s.header);
		createHeaderCell(header, 4, "Scripture", s.header);

		int rowNum = 1;
		for (Book book : bible.getBooks()) {
			int bookNumber = getBookNumber(book);
			String bookName = nullSafe(book.getLongName());

			int chapterCount = 0;
			for (Chapter chapter : book.getChapters()) {
				chapterCount++;
				int chapterNum = parseIntSafe(chapter.getChapter(), chapterCount);

				for (Verse verse : chapter.getVerses()) {
					String text = verse.getUnParsedText();
					if (text == null || text.trim().isEmpty()) continue;
					int verseNum = parseIntSafe(verse.getNumber(), 0);
					if (verseNum <= 0) continue;

					Row row = sheet.createRow(rowNum++);
					createNumberCell(row, 0, bookNumber, s.number);
					createValueCell (row, 1, bookName,   s.value);
					createNumberCell(row, 2, chapterNum, s.number);
					createNumberCell(row, 3, verseNum,   s.number);
					createVerseCell (row, 4, text,       s.verse);
				}
			}
		}
		System.out.println("verses sheet rows written: " + (rowNum - 1));
	}

	// -------------------------------------------------------------------------
	// Sheet 3 — books  (only books present in this Bible)
	// -------------------------------------------------------------------------

	/**
	 * Writes one row per book that is present in this Bible.
	 *
	 * Columns: Book No | OSIS ID | Short Name | Long Name | English Name | Chapters
	 */
	private static void createBooksSheet(XSSFWorkbook wb, Styles s, Bible bible) {
		Sheet sheet = wb.createSheet("books");
		sheet.setColumnWidth(0,  8 * 256);   // Book No
		sheet.setColumnWidth(1, 10 * 256);   // OSIS ID
		sheet.setColumnWidth(2, 20 * 256);   // Short Name
		sheet.setColumnWidth(3, 36 * 256);   // Long Name
		sheet.setColumnWidth(4, 36 * 256);   // English Name
		sheet.setColumnWidth(5, 10 * 256);   // Chapters
		sheet.createFreezePane(0, 1);

		Row header = sheet.createRow(0);
		createHeaderCell(header, 0, "Book No",     s.header);
		createHeaderCell(header, 1, "OSIS ID",     s.header);
		createHeaderCell(header, 2, "Short Name",  s.header);
		createHeaderCell(header, 3, "Long Name",   s.header);
		createHeaderCell(header, 4, "English Name",s.header);
		createHeaderCell(header, 5, "Chapters",    s.header);

		int rowNum = 1;
		for (Book book : bible.getBooks()) {
			int bookNumber = getBookNumber(book);
			Row row = sheet.createRow(rowNum++);
			createNumberCell(row, 0, bookNumber,                   s.number);
			createValueCell (row, 1, nullSafe(book.getBookId()),   s.value);
			createValueCell (row, 2, nullSafe(book.getShortName()),s.value);
			createValueCell (row, 3, nullSafe(book.getLongName()), s.value);
			createValueCell (row, 4, nullSafe(book.getEnglishName()), s.value);
			createNumberCell(row, 5, book.getChapters().size(),    s.number);
		}
	}

	// -------------------------------------------------------------------------
	// Sheet 4 — books_all  (all 66 canonical books)
	// -------------------------------------------------------------------------

	/**
	 * Writes all 66 canonical books (Genesis–Revelation) with a flag indicating
	 * whether each book is present in this Bible.
	 *
	 * Columns: Book No | OSIS ID | English Name | Testament | Present | Chapters
	 */
	private static void createBooksAllSheet(XSSFWorkbook wb, Styles s, Bible bible) {
		Sheet sheet = wb.createSheet("books_all");
		sheet.setColumnWidth(0,  8 * 256);   // Book No
		sheet.setColumnWidth(1, 10 * 256);   // OSIS ID
		sheet.setColumnWidth(2, 28 * 256);   // English Name
		sheet.setColumnWidth(3, 10 * 256);   // Testament
		sheet.setColumnWidth(4, 10 * 256);   // Present
		sheet.setColumnWidth(5, 10 * 256);   // Chapters
		sheet.createFreezePane(0, 1);

		Row header = sheet.createRow(0);
		createHeaderCell(header, 0, "Book No",     s.header);
		createHeaderCell(header, 1, "OSIS ID",     s.header);
		createHeaderCell(header, 2, "English Name",s.header);
		createHeaderCell(header, 3, "Testament",   s.header);
		createHeaderCell(header, 4, "Present",     s.header);
		createHeaderCell(header, 5, "Chapters",    s.header);

		// Build a quick lookup: ZefID → Book (for books present in this Bible)
		Map<Integer, Book> presentBooks = new HashMap<>();
		for (Book book : bible.getBooks()) {
			int zefId = getBookNumber(book);
			if (zefId > 0) presentBooks.put(zefId, book);
		}

		int rowNum = 1;
		for (int zefId = 1; zefId <= 66; zefId++) {
			BookID bid;
			try {
				bid = BookID.fromZefId(zefId);
			} catch (IllegalArgumentException e) {
				continue; // skip gaps (there are none 1-66 but be safe)
			}

			Book present = presentBooks.get(zefId);
			boolean isPresent = present != null;
			int chapters = isPresent ? present.getChapters().size() : 0;

			Row row = sheet.createRow(rowNum++);
			createNumberCell(row, 0, zefId,                         s.number);
			createValueCell (row, 1, bid.getOsisID(),               s.value);
			createValueCell (row, 2, bid.getEnglishName(),          s.value);
			createValueCell (row, 3, bid.isNT() ? "NT" : "OT",     s.value);

			// "Present" cell — green if yes, red/grey if no
			Cell presentCell = row.createCell(4);
			presentCell.setCellValue(isPresent ? "Yes" : "No");
			presentCell.setCellStyle(isPresent ? s.yes : s.no);

			createNumberCell(row, 5, chapters, s.number);
		}
	}

	// -------------------------------------------------------------------------
	// Shared cell-creation helpers
	// -------------------------------------------------------------------------

	private static void createHeaderCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private static void createLabelCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private static void createValueCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private static void createVerseCell(Row row, int col, String value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	private static void createNumberCell(Row row, int col, int value, CellStyle style) {
		Cell cell = row.createCell(col);
		cell.setCellValue(value);
		cell.setCellStyle(style);
	}

	// -------------------------------------------------------------------------
	// Styles container
	// -------------------------------------------------------------------------

	/**
	 * Builds and caches all CellStyles used in the workbook.
	 * Creating styles per-cell would hit Excel's 64K style limit on large Bibles.
	 */
	private static class Styles {
		final CellStyle header;
		final CellStyle label;
		final CellStyle value;
		final CellStyle verse;
		final CellStyle number;
		final CellStyle yes;
		final CellStyle no;

		Styles(XSSFWorkbook wb) {
			// --- fonts ---
			Font headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 11);
			headerFont.setColor(IndexedColors.WHITE.getIndex());

			Font boldFont = wb.createFont();
			boldFont.setBold(true);
			boldFont.setFontHeightInPoints((short) 10);

			Font normalFont = wb.createFont();
			normalFont.setFontHeightInPoints((short) 10);

			// --- header ---
			header = wb.createCellStyle();
			header.setFont(headerFont);
			header.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
			header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			header.setAlignment(HorizontalAlignment.CENTER);
			header.setVerticalAlignment(VerticalAlignment.CENTER);
			setBorder(header, BorderStyle.THIN);

			// --- label (info sheet key column) ---
			label = wb.createCellStyle();
			label.setFont(boldFont);
			label.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			label.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBorder(label, BorderStyle.THIN);

			// --- value (general string) ---
			value = wb.createCellStyle();
			value.setFont(normalFont);
			value.setWrapText(false);
			setBorder(value, BorderStyle.THIN);

			// --- verse (wrap text for scripture column) ---
			verse = wb.createCellStyle();
			verse.setFont(normalFont);
			verse.setWrapText(true);
			setBorder(verse, BorderStyle.THIN);

			// --- number ---
			number = wb.createCellStyle();
			number.setFont(normalFont);
			number.setAlignment(HorizontalAlignment.CENTER);
			setBorder(number, BorderStyle.THIN);

			// --- yes (green) ---
			yes = wb.createCellStyle();
			yes.setFont(boldFont);
			yes.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			yes.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			yes.setAlignment(HorizontalAlignment.CENTER);
			setBorder(yes, BorderStyle.THIN);

			// --- no (light orange) ---
			no = wb.createCellStyle();
			no.setFont(normalFont);
			no.setFillForegroundColor(IndexedColors.TAN.getIndex());
			no.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			no.setAlignment(HorizontalAlignment.CENTER);
			setBorder(no, BorderStyle.THIN);
		}

		private static void setBorder(CellStyle cs, BorderStyle bs) {
			cs.setBorderTop(bs);
			cs.setBorderBottom(bs);
			cs.setBorderLeft(bs);
			cs.setBorderRight(bs);
		}
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
				// fall through
			}
		}
		return book.getBookNo();
	}

	private static int parseIntSafe(String value, int fallback) {
		if (value == null || value.trim().isEmpty()) return fallback;
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static String nullSafe(String value) {
		return value != null ? value : "";
	}
}