/**
 * Converts Bible text to Zefania XML format.
 * Specification: https://github.com/biblenerd/Zefania-XML-Preservation/raw/refs/heads/main/zef2014.xsd
 *
 * Zefania XML is a widely used open XML format for Bible texts.
 * All books are written into a single .xml file.
 *
 * Tag naming follows real-world Zefania files (UPPERCASE element names):
 *   <XMLBIBLE> / <INFORMATION> / <BIBLEBOOK> / <CHAPTER> / <VERS>
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

public class ZefaniaXML {

	public static void createZefaniaXML() {
		System.out.println("ZefaniaXML Creation Started...");
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
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		// Comments matching real-world Zefania files
		sb.append("<!--Visit the online documentation for Zefania XML Markup-->\n");
		sb.append("<!--https://github.com/biblenerd/Zefania-XML-Preservation-->\n");
		sb.append("<!--Created using BibleConverter program https://github.com/yesudas/bible-converter-->\n");

		// <XMLBIBLE> root element
		// version="2.0.1.18" and revision="0" are the standard values used in real Zefania files
		sb.append("<XMLBIBLE")
		  .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
		  .append(" xsi:noNamespaceSchemaLocation=\"zef2014.xsd\"")
		  .append(" version=\"2.0.1.18\"")
		  .append(" status=\"v\"")
		  .append(" biblename=\"").append(xmlEscape(bible.getCommonName())).append("\"")
		  .append(" type=\"x-bible\"")
		  .append(" revision=\"0\"")
		  .append(">\n");

		// ---- <INFORMATION> block ----
		sb.append(buildInformation(bible));

		// ---- <BIBLEBOOK> per book ----
		for (Book book : bible.getBooks()) {

			int bnumber   = getBookNumber(book);
			String bname  = notEmpty(book.getLongName())    ? book.getLongName()    : book.getEnglishName();
			String bsname = notEmpty(book.getThreeLetterCode()) ? book.getThreeLetterCode()
			              : notEmpty(book.getAbbr())         ? book.getAbbr()
			              : bname;

			sb.append("  <BIBLEBOOK")
			  .append(" bnumber=\"").append(bnumber).append("\"")
			  .append(" bname=\"").append(xmlEscape(bname)).append("\"")
			  .append(" bsname=\"").append(xmlEscape(bsname)).append("\"")
			  .append(">\n");

			int expectedChapterNumber = 1;
			for (Chapter chapter : book.getChapters()) {
				if (chapter.getChapter() == null || chapter.getChapter().isEmpty()) {
					System.out.println("Invalid chapter in " + book.getLongName()
							+ " Chapter number: " + expectedChapterNumber);
					chapter.setChapter(String.valueOf(expectedChapterNumber));
				}

				String chNum = chapter.getChapter();

				sb.append("    <CHAPTER cnumber=\"").append(chNum).append("\">\n");

				for (Verse verse : chapter.getVerses()) {
					String vNum = verse.getNumber();
					String text = verse.getUnParsedText();

					sb.append("      <VERS vnumber=\"").append(xmlEscape(vNum)).append("\">")
					  .append(xmlEscape(text))
					  .append("</VERS>\n");
				}

				sb.append("    </CHAPTER>\n");
				expectedChapterNumber++;
			}

			sb.append("  </BIBLEBOOK>\n");
		}

		sb.append("</XMLBIBLE>\n");

		String outFileName = bible.getAbbr() + ".xml";
		writeFile(outFileName, sb.toString());

		System.out.println("ZefaniaXML Creation Completed...");
		System.out.println("Results are saved in: " + BibleConverter.outputPath + File.separator + outFileName);
	}

	// -------------------------------------------------------------------------
	// <INFORMATION> block
	// -------------------------------------------------------------------------

	private static String buildInformation(Bible bible) {
		StringBuilder sb = new StringBuilder();
		sb.append("  <INFORMATION>\n");

		appendTag(sb, "title",       bible.getCommonName());
		appendTag(sb, "creator",     bible.getTranslatedBy());
		appendTag(sb, "subject",     "The Holy Bible");
		appendTag(sb, "description", notEmpty(bible.getLongName()) ? bible.getLongName() : bible.getCommonName());
		appendTag(sb, "publisher",   bible.getPublishedBy());
		sb.append("    <contributors/>\n");
		appendTag(sb, "date",        notEmpty(bible.getPublishedYear()) ? bible.getPublishedYear() : String.valueOf(LocalDate.now().getYear()));
		sb.append("    <type>Bible</type>\n");
		sb.append("    <format>Zefania XML Bible Markup Language</format>\n");
		appendTag(sb, "identifier",  bible.getAbbr());
		sb.append("    <source>Converted using BibleConverter program https://github.com/yesudas/bible-converter</source>\n");
		appendTag(sb, "language",    bible.getLanguageCode());
		appendTag(sb, "rights",      bible.getCopyRight());

		sb.append("  </INFORMATION>\n");
		return sb.toString();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	/**
	 * Returns the Zefania book number (bnumber).
	 * Uses the ZefID from the BookID enum (1=Gen … 66=Rev).
	 * Falls back to the book's own bookNo field, then sequential position.
	 */
	private static int getBookNumber(Book book) {
		if (book.getId() != null && book.getId().getZefID() > 0) {
			return book.getId().getZefID();
		}
		// Fallback: try to match by English name
		String engName = book.getEnglishName();
		if (notEmpty(engName)) {
			for (BookID bid : BookID.values()) {
				if (bid.getEnglishName().equalsIgnoreCase(engName) && bid.getZefID() > 0) {
					return bid.getZefID();
				}
			}
		}
		// Last resort: bookNo field
		return book.getBookNo();
	}

	private static void appendTag(StringBuilder sb, String tag, String value) {
		if (notEmpty(value)) {
			sb.append("    <").append(tag).append(">")
			  .append(xmlEscape(value))
			  .append("</").append(tag).append(">\n");
		} else {
			sb.append("    <").append(tag).append("/>\n");
		}
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
