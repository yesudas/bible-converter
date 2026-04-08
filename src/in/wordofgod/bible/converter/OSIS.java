/**
 * Converts Bible text to OSIS (Open Scripture Information Standard) format.
 * Specification: https://crosswire.org/osis/osisCore.2.1.1.xsd
 *
 * OSIS is an XML-based standard for encoding biblical texts maintained by CrossWire Bible Society.
 * All books are written into a single .xml file.
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.BookID;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

public class OSIS {

	// OSIS 2.1.1 XML declaration and root element attributes
	private static final String XML_DECLARATION =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private static final String OSIS_OPEN =
			"<osis\n" +
			"  xmlns=\"http://www.bibletechnologies.net/2003/OSIS/namespace\"\n" +
			"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
			"  xsi:schemaLocation=\"http://www.bibletechnologies.net/2003/OSIS/namespace " +
			"https://crosswire.org/osis/osisCore.2.1.1.xsd\">\n";

	public static void createOSIS() {
		System.out.println("OSIS Creation Started...");
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

		try {
			Utils.setOutputFolder(bible.getLanguageCode());
		} catch (java.net.URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		try {
			Files.createDirectories(Path.of(BibleConverter.outputPath));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		StringBuilder sb = new StringBuilder();

		// XML declaration
		sb.append(XML_DECLARATION);

		// <osis> root
		sb.append(OSIS_OPEN);

		// <osisText> — the text container
		// osisIDWork = bible abbreviation (used as the work identifier)
		// osisRefWork = default reference system
		sb.append("  <osisText osisIDWork=\"").append(xmlEscape(bible.getAbbr())).append("\"")
		  .append(" osisRefWork=\"Bible\"")
		  .append(" xml:lang=\"").append(xmlEscape(bible.getLanguageCode())).append("\">\n");

		// ---- <header> ----
		sb.append(buildHeader(bible));

		// ---- <div type="x-testament"> groupings then <div type="book"> per book ----
		boolean otOpen = false;
		boolean ntOpen = false;

		for (Book book : bible.getBooks()) {

			boolean isNT = isNewTestament(book);

			// Open/close testament <div> wrappers
			if (!isNT && !otOpen) {
				if (ntOpen) {
					sb.append("    </div>\n"); // close NT (shouldn't happen but guard)
					ntOpen = false;
				}
				sb.append("    <div type=\"x-testament\">\n");
				otOpen = true;
			} else if (isNT && !ntOpen) {
				if (otOpen) {
					sb.append("    </div>\n"); // close OT
					otOpen = false;
				}
				sb.append("    <div type=\"x-testament\">\n");
				ntOpen = true;
			}

			String osisBookId = getOsisBookId(book);

			// <div type="book">
			sb.append("      <div type=\"book\" osisID=\"").append(xmlEscape(osisBookId)).append("\">\n");

			// <title type="main"> — book title
			sb.append("        <title type=\"main\">").append(xmlEscape(book.getLongName())).append("</title>\n");

			int expectedChapterNumber = 1;
			for (Chapter chapter : book.getChapters()) {
				if (chapter.getChapter() == null || chapter.getChapter().isEmpty()) {
					System.out.println("Invalid chapter in " + book.getLongName()
							+ " Chapter number: " + expectedChapterNumber);
					chapter.setChapter(String.valueOf(expectedChapterNumber));
				}

				String chNum = chapter.getChapter();
				// OSIS chapter osisID: e.g. "Gen.1"
				String chapterOsisId = osisBookId + "." + chNum;

				// <chapter>
				sb.append("        <chapter osisID=\"").append(xmlEscape(chapterOsisId)).append("\"")
				  .append(" n=\"").append(chNum).append("\">\n");

				// <title type="chapter"> — optional chapter heading
				sb.append("          <title type=\"chapter\">")
				  .append(xmlEscape(book.getLongName())).append(" ").append(chNum)
				  .append("</title>\n");

				for (Verse verse : chapter.getVerses()) {
					String vNum = verse.getNumber();
					// OSIS verse osisID: e.g. "Gen.1.1"
					String verseOsisId = chapterOsisId + "." + vNum;

					// <verse> with osisID per OSIS 2.1.1 spec
					sb.append("          <verse osisID=\"").append(xmlEscape(verseOsisId)).append("\"")
					  .append(" n=\"").append(xmlEscape(vNum)).append("\">")
					  .append(xmlEscape(verse.getUnParsedText()))
					  .append("</verse>\n");
				}

				sb.append("        </chapter>\n");
				expectedChapterNumber++;
			}

			sb.append("      </div>\n"); // close book
		}

		// Close open testament div
		if (otOpen || ntOpen) {
			sb.append("    </div>\n");
		}

		sb.append("  </osisText>\n");
		sb.append("</osis>\n");

		String outFileName = bible.getAbbr() + ".xml";
		writeFile(outFileName, sb.toString());

		System.out.println("OSIS Creation Completed...");
		System.out.println("Results are saved in: " + BibleConverter.outputPath + File.separator + outFileName);
	}

	// -------------------------------------------------------------------------
	// <header> block per OSIS 2.1.1 spec
	// -------------------------------------------------------------------------

	private static String buildHeader(Bible bible) {
		StringBuilder sb = new StringBuilder();
		sb.append("    <header>\n");

		// <revisionDesc> — when this file was created
		sb.append("      <revisionDesc resp=\"BibleConverter\">\n");
		sb.append("        <date>").append(LocalDate.now()).append("</date>\n");
		sb.append("        <p>Created using BibleConverter program " +
				"https://github.com/yesudas/bible-converter</p>\n");
		sb.append("      </revisionDesc>\n");

		// <work osisWork="..."> — describes the primary text work
		sb.append("      <work osisWork=\"").append(xmlEscape(bible.getAbbr())).append("\">\n");

		// <title>
		appendWorkMeta(sb, "title", bible.getCommonName());

		// <contributor role="translator">
		if (notEmpty(bible.getTranslatedBy())) {
			sb.append("        <contributor role=\"translator\">")
			  .append(xmlEscape(bible.getTranslatedBy()))
			  .append("</contributor>\n");
		}

		// <creator role="publisher">
		if (notEmpty(bible.getPublishedBy())) {
			sb.append("        <creator role=\"publisher\">")
			  .append(xmlEscape(bible.getPublishedBy()))
			  .append("</creator>\n");
		}

		// <description>
		appendWorkMeta(sb, "description", bible.getLongName());

		// <publisher>
		appendWorkMeta(sb, "publisher", bible.getPublishedBy());

		// <date>
		if (notEmpty(bible.getPublishedYear())) {
			sb.append("        <date>").append(xmlEscape(bible.getPublishedYear())).append("</date>\n");
		}

		// <identifier type="OSIS">
		if (notEmpty(bible.getAbbr())) {
			sb.append("        <identifier type=\"OSIS\">Bible.")
			  .append(xmlEscape(bible.getAbbr()))
			  .append("</identifier>\n");
		}

		// <rights>
		appendWorkMeta(sb, "rights", bible.getCopyRight());

		// <language type="ISO-639-1">
		if (notEmpty(bible.getLanguageCode())) {
			sb.append("        <language type=\"ISO-639-1\">")
			  .append(xmlEscape(bible.getLanguageCode()))
			  .append("</language>\n");
		}

		// <source>
		sb.append("        <source>Converted using BibleConverter program " +
				"https://github.com/yesudas/bible-converter</source>\n");

		// <type type="OSIS">Bible</type>
		sb.append("        <type type=\"OSIS\">Bible</type>\n");

		// <format type="OSIS">text/xml</format>
		sb.append("        <format type=\"OSIS\">text/xml</format>\n");

		sb.append("      </work>\n");

		// <work osisWork="Bible"> — reference work declaration required by OSIS spec
		sb.append("      <work osisWork=\"Bible\">\n");
		sb.append("        <type type=\"OSIS\">Bible</type>\n");
		sb.append("        <identifier type=\"OSIS\">Bible</identifier>\n");
		sb.append("      </work>\n");

		sb.append("    </header>\n");
		return sb.toString();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static void appendWorkMeta(StringBuilder sb, String tag, String value) {
		if (notEmpty(value)) {
			sb.append("        <").append(tag).append(">")
			  .append(xmlEscape(value))
			  .append("</").append(tag).append(">\n");
		}
	}

	/**
	 * Returns the OSIS book identifier.
	 * Priority: BookID enum osisID → bookId field → English name (no spaces) → long name (no spaces).
	 */
	private static String getOsisBookId(Book book) {
		// Best: use the BookID enum which has canonical OSIS IDs (Gen, Matt, Rev, etc.)
		if (book.getId() != null) {
			return book.getId().getOsisID();
		}
		// Fallback: bookId field (may already be an OSIS ID)
		if (notEmpty(book.getBookId())) {
			return book.getBookId();
		}
		// Fallback: try matching by English name against BookID enum
		String engName = book.getEnglishName();
		if (notEmpty(engName)) {
			for (BookID bid : BookID.values()) {
				if (bid.getEnglishName().equalsIgnoreCase(engName)) {
					return bid.getOsisID();
				}
			}
			return engName.replaceAll("\\s+", "");
		}
		return book.getLongName().replaceAll("\\s+", "");
	}

	/**
	 * Determines if a book belongs to the New Testament.
	 * Uses BookID enum when available, otherwise checks book number >= 40.
	 */
	private static boolean isNewTestament(Book book) {
		if (book.getId() != null) {
			return book.getId().isNT();
		}
		// Fallback: NT books start at Matthew = book number 40
		return book.getBookNo() >= 40;
	}

	/** Escapes characters that are special in XML. */
	private static String xmlEscape(String text) {
		if (text == null) return "";
		return text.replace("&",  "&amp;")
				   .replace("<",  "&lt;")
				   .replace(">",  "&gt;")
				   .replace("\"", "&quot;")
				   .replace("'",  "&apos;");
	}

	private static boolean notEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static void writeFile(String fileName, String content) {
		try {
			Path path = Path.of(BibleConverter.outputPath + File.separator + fileName);
			Files.createDirectories(path.getParent());
			Files.writeString(path, content);
			System.out.println("Created: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}