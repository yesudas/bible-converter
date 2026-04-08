/**
 * Converts Bible text to ThML (Theological Markup Language) format.
 * Specification: https://www.ccel.org/ThML/ThML1.04.htm
 *
 * ThML is an XML-based format developed by CCEL (Christian Classics Ethereal Library).
 * It extends HTML 4.0 with theology-specific elements for scripture, notes, and metadata.
 *
 * Output: one .thml file per book + a master index file ([ABBR]-index.thml)
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

public class ThML {

	// ThML namespace / DOCTYPE
	private static final String DOCTYPE =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE ThML PUBLIC \"-//CCEL//DTD ThML 1.04//EN\"\n" +
			"  \"http://www.ccel.org/ThML/ThML1.04.dtd\">\n";

	public static void createThML() {
		System.out.println("ThML Creation Started...");
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

		// Ensure output directory exists
		try {
			Files.createDirectories(Path.of(BibleConverter.outputPath));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// Create one ThML file per book
		StringBuilder indexEntries = new StringBuilder();
		for (Book book : bible.getBooks()) {
			String bookFileName = getBookFileName(book);
			createBookFile(bible, book, bookFileName);
			indexEntries.append("      <li><a href=\"").append(bookFileName).append("\">")
					.append(xmlEscape(book.getLongName())).append("</a></li>\n");
		}

		// Create master index file
		createIndexFile(bible, indexEntries.toString());

		System.out.println("ThML Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	// -------------------------------------------------------------------------
	// Single-file ThML output  (all books in one .thml file)
	// -------------------------------------------------------------------------

	public static void createThMLSingleFile() {
		System.out.println("ThML Single-File Creation Started...");
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
		sb.append(DOCTYPE);
		sb.append("<ThML>\n");
		sb.append(buildThMLHead(bible));
		sb.append("  <ThML.body>\n");

		int bookCount = 0;
		for (Book book : bible.getBooks()) {
			bookCount++;
			sb.append("    <div1 type=\"book\"");
			if (notEmpty(book.getBookId())) {
				sb.append(" id=\"").append(xmlEscape(book.getBookId())).append("\"");
			}
			sb.append(">\n");
			sb.append("      <head>").append(xmlEscape(book.getLongName())).append("</head>\n");

			int expectedChapterNumber = 1;
			for (Chapter chapter : book.getChapters()) {
				if (chapter.getChapter() == null || chapter.getChapter().isEmpty()) {
					System.out.println("Invalid chapter in " + book.getLongName()
							+ " Chapter number: " + expectedChapterNumber);
					chapter.setChapter(String.valueOf(expectedChapterNumber));
				}

				String chNum = chapter.getChapter();
				String osisId = getOsisBookId(book) + "." + chNum;

				sb.append("      <div2 type=\"chapter\" n=\"").append(chNum)
				  .append("\" id=\"").append(xmlEscape(osisId)).append("\">\n");
				sb.append("        <head>")
				  .append(xmlEscape(book.getLongName())).append(" ").append(chNum)
				  .append("</head>\n");

				for (Verse verse : chapter.getVerses()) {
					String vNum = verse.getNumber();
					String verseOsisId = osisId + "." + vNum;
					sb.append("        <scripRef passage=\"")
					  .append(xmlEscape(verseOsisId)).append("\">");
					sb.append("<sup>").append(vNum).append("</sup> ");
					sb.append(xmlEscape(verse.getUnParsedText()));
					sb.append("</scripRef>\n");
				}

				sb.append("      </div2>\n");
				expectedChapterNumber++;
			}

			sb.append("    </div1>\n");
		}

		sb.append("  </ThML.body>\n");
		sb.append("</ThML>\n");

		String outFileName = bible.getAbbr() + ".thml";
		writeFile(outFileName, sb.toString());

		System.out.println("ThML Single-File Creation Completed. Total books: " + bookCount);
		System.out.println("Results are saved in: " + BibleConverter.outputPath + File.separator + outFileName);
	}

	// -------------------------------------------------------------------------
	// Index file
	// -------------------------------------------------------------------------

	private static void createIndexFile(Bible bible, String bookListItems) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOCTYPE);
		sb.append("<ThML>\n");
		sb.append(buildThMLHead(bible));

		sb.append("  <ThML.body>\n");
		sb.append("    <div class=\"bibleIndex\">\n");

		// Bible information as a ThML description block
		sb.append("      <div class=\"bibleInfo\">\n");
		sb.append("        <table>\n");
		sb.append(infoRow("Abbr",                  bible.getAbbr()));
		sb.append(infoRow("Common Name",           bible.getCommonName()));
		sb.append(infoRow("Short Name",            bible.getShortName()));
		sb.append(infoRow("Long Name",             bible.getLongName()));
		sb.append(infoRow("Long English Name",     bible.getLongEnglishName()));
		sb.append(infoRow("Language Code",         bible.getLanguageCode()));
		sb.append(infoRow("Published Year",        bible.getPublishedYear()));
		sb.append(infoRow("Published By",          bible.getPublishedBy()));
		sb.append(infoRow("Translated By",         bible.getTranslatedBy()));
		sb.append(infoRow("Copyright",             bible.getCopyRight()));
		sb.append(infoRow("Additional Information",bible.getAdditionalInformation()));
		sb.append(infoRow("Total Books",           String.valueOf(bible.getTotalBooks())));
		sb.append(infoRow("Total Chapters",        String.valueOf(bible.getTotalChapters())));
		sb.append(infoRow("Total Verses",          String.valueOf(bible.getTotalVerses())));
		sb.append(infoRow("Total Words",           String.valueOf(bible.getTotalWords())));
		sb.append(infoRow("Total Unique Words",    String.valueOf(bible.getTotalUniqueWords())));
		sb.append(infoRow("Has OT",                String.valueOf(bible.isHasOT())));
		sb.append(infoRow("Has NT",                String.valueOf(bible.isHasNT())));
		sb.append(infoRow("Generated By",
				"Created using BibleConverter program https://github.com/yesudas/bible-converter"));
		sb.append("        </table>\n");
		sb.append("      </div>\n");

		// Table of contents
		sb.append("      <div class=\"tableOfContents\">\n");
		sb.append("        <h1>").append(xmlEscape(bible.getCommonName())).append("</h1>\n");
		sb.append("        <h2>Table of Contents</h2>\n");
		sb.append("        <ul>\n");
		sb.append(bookListItems);
		sb.append("        </ul>\n");
		sb.append("      </div>\n");
		sb.append("    </div>\n");
		sb.append("  </ThML.body>\n");
		sb.append("</ThML>\n");

		writeFile(bible.getAbbr() + "-index.thml", sb.toString());
	}

	// -------------------------------------------------------------------------
	// Book file
	// -------------------------------------------------------------------------

	private static void createBookFile(Bible bible, Book book, String bookFileName) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOCTYPE);
		sb.append("<ThML>\n");
		sb.append(buildThMLHead(bible));

		sb.append("  <ThML.body>\n");

		// Each book is a <div1> (top-level division)
		sb.append("    <div1 type=\"book\"");
		if (book.getBookId() != null && !book.getBookId().isEmpty()) {
			sb.append(" id=\"").append(xmlEscape(book.getBookId())).append("\"");
		}
		sb.append(">\n");
		sb.append("      <head>").append(xmlEscape(book.getLongName())).append("</head>\n");

		int expectedChapterNumber = 1;
		for (Chapter chapter : book.getChapters()) {
			if (chapter.getChapter() == null || chapter.getChapter().isEmpty()) {
				System.out.println("Invalid chapter in " + book.getLongName()
						+ " Chapter number: " + expectedChapterNumber);
				chapter.setChapter(String.valueOf(expectedChapterNumber));
			}

			String chNum = chapter.getChapter();

			// Each chapter is a <div2> (second-level division)
			// scripture reference: OSIS-style book.chapter (e.g. Matt.1)
			String osisId = getOsisBookId(book) + "." + chNum;
			sb.append("      <div2 type=\"chapter\" n=\"").append(chNum)
			  .append("\" id=\"").append(xmlEscape(osisId)).append("\">\n");
			sb.append("        <head>")
			  .append(xmlEscape(book.getLongName())).append(" ").append(chNum)
			  .append("</head>\n");

			for (Verse verse : chapter.getVerses()) {
				String vNum = verse.getNumber();
				String verseOsisId = osisId + "." + vNum;
				// <scripRef> is the ThML element for a scripture reference/verse
				sb.append("        <scripRef passage=\"")
				  .append(xmlEscape(verseOsisId)).append("\">");
				sb.append("<sup>").append(vNum).append("</sup> ");
				sb.append(xmlEscape(verse.getUnParsedText()));
				sb.append("</scripRef>\n");
			}

			sb.append("      </div2>\n");
			expectedChapterNumber++;
		}

		sb.append("    </div1>\n");
		sb.append("  </ThML.body>\n");
		sb.append("</ThML>\n");

		writeFile(bookFileName, sb.toString());
	}

	// -------------------------------------------------------------------------
	// ThML head block  (<ThML.head> with full <electronicEdInfo> metadata)
	// -------------------------------------------------------------------------

	private static String buildThMLHead(Bible bible) {
		StringBuilder sb = new StringBuilder();
		sb.append("  <ThML.head>\n");

		// <electronicEdInfo> — ThML metadata section
		sb.append("    <electronicEdInfo>\n");

		// <title>
		appendMeta(sb, "title",       bible.getCommonName());

		// <creator> — translated by
		if (notEmpty(bible.getTranslatedBy())) {
			sb.append("      <creator role=\"translator\">")
			  .append(xmlEscape(bible.getTranslatedBy()))
			  .append("</creator>\n");
		}

		// <publisher>
		appendMeta(sb, "publisher",   bible.getPublishedBy());

		// <subject>
		sb.append("      <subject>Bible</subject>\n");

		// <description> — long name
		appendMeta(sb, "description", bible.getLongName());

		// <date>
		if (notEmpty(bible.getPublishedYear())) {
			sb.append("      <date>").append(xmlEscape(bible.getPublishedYear())).append("</date>\n");
		} else {
			sb.append("      <date>").append(LocalDate.now().getYear()).append("</date>\n");
		}

		// <type>
		sb.append("      <type>Text</type>\n");

		// <format>
		sb.append("      <format>ThML</format>\n");

		// <identifier> — abbreviation
		appendMeta(sb, "identifier",  bible.getAbbr());

		// <language>
		appendMeta(sb, "language",    bible.getLanguageCode());

		// <rights> — copyright
		appendMeta(sb, "rights",      bible.getCopyRight());

		// <source>
		sb.append("      <source>Converted using BibleConverter program " +
				"https://github.com/yesudas/bible-converter</source>\n");

		sb.append("    </electronicEdInfo>\n");

		// <revisionHistory>
		sb.append("    <revisionHistory>\n");
		sb.append("      <revision date=\"").append(LocalDate.now()).append("\">\n");
		sb.append("        <description>Created using BibleConverter program " +
				"https://github.com/yesudas/bible-converter</description>\n");
		sb.append("      </revision>\n");
		sb.append("    </revisionHistory>\n");

		sb.append("  </ThML.head>\n");
		return sb.toString();
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------

	private static void appendMeta(StringBuilder sb, String tag, String value) {
		if (notEmpty(value)) {
			sb.append("      <").append(tag).append(">")
			  .append(xmlEscape(value))
			  .append("</").append(tag).append(">\n");
		}
	}

	private static String infoRow(String label, String value) {
		if (value == null || value.trim().isEmpty()) return "";
		return "          <tr><td><b>" + xmlEscape(label) + "</b></td><td>"
				+ xmlEscape(value) + "</td></tr>\n";
	}

	/**
	 * Returns an OSIS-style book identifier.
	 * Uses bookId if available, otherwise falls back to the English name with spaces removed.
	 */
	private static String getOsisBookId(Book book) {
		if (book.getBookId() != null && !book.getBookId().isEmpty()) {
			return book.getBookId();
		}
		if (book.getEnglishName() != null && !book.getEnglishName().isEmpty()) {
			return book.getEnglishName().replaceAll("\\s+", "");
		}
		return book.getLongName().replaceAll("\\s+", "");
	}

	private static String getBookFileName(Book book) {
		String bookNo = String.format("%02d", book.getBookNo());
		String safeName = (book.getEnglishName() != null && !book.getEnglishName().isEmpty())
				? book.getEnglishName()
				: book.getLongName();
		safeName = safeName.replaceAll("[^a-zA-Z0-9\\-]", "_");
		return bookNo + "-" + safeName + ".thml";
	}

	/** Escapes characters that are special in XML/HTML. */
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