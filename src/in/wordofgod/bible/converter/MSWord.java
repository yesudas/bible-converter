/**
 *
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHyperlinkRun;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTColumns;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to MS Word (.docx) format.
 * createMSWord()      — single document with title page, index, and all content
 * createMSWordByBooks() — one document per book
 */
public class MSWord {

	private static final String FONT_TITLE = "Cambria";
	private static final String FONT_BODY = "Calibri (Body)";

	private static int uniqueBookMarkCounter = 1;

	// =========================================================================
	// Public entry points
	// =========================================================================

	public static void createMSWord() throws URISyntaxException {
		System.out.println("MS Word (Single File) Creation Started...");
		File file = new File(BibleConverter.bibleSourcePath);

		System.out.println("TheWord Bible loading started...");
		Bible bible;
		try {
			bible = TheWord.getBible(file.getAbsolutePath(), BibleConverter.bibleInformationPath);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (bible == null) {
			System.err.println("Failed to load Bible.");
			return;
		}
		System.out.println("TheWord Bible loaded successfully...");

		Utils.setOutputFolder(bible.getLanguageCode());

		File outputDir = new File(BibleConverter.outputPath);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
			System.out.println("Created output directory: " + outputDir.getAbsolutePath());
		}

		uniqueBookMarkCounter = 1;

		try {
			XWPFDocument document = new XWPFDocument();

			createPageSettings(document);
			createMetaData(document, bible);
			createTitlePage(document, bible);
			createBookDetailsPage(document, bible);
			createPDFIssuePage(document);
			createIndex(document, bible);
			createContent(document, bible);
			outputStatistics(bible);

			String fileName = bible.getAbbr() + ".docx";
			String filePath = BibleConverter.outputPath + File.separator + fileName;
			FileOutputStream out = new FileOutputStream(filePath);
			document.write(out);
			out.close();
			document.close();

			System.out.println("MS Word document created: " + fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("MS Word (Single File) Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	public static void createMSWordByBooks() throws URISyntaxException {
		System.out.println("MS Word Creation Started...");
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

		// Process each book
		int bookCount = 0;
		for (Book book : bible.getBooks()) {
			bookCount++;
			try {
				createWordDocumentForBook(bible, book);
				System.out.println("Created Word document for book " + bookCount + ": " + book.getLongName());
			} catch (IOException e) {
				System.err.println("Error creating Word document for book: " + book.getLongName());
				e.printStackTrace();
			}
		}

		System.out.println("MS Word Creation Completed...");
		System.out.println("Total books processed: " + bookCount);
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	// =========================================================================
	// createMSWordByBooks helpers
	// =========================================================================

	private static void createWordDocumentForBook(Bible bible, Book book) throws IOException {
		// Create filename: abbr-01-BookName.docx
		String bookNumber = String.format("%02d", book.getBookNo());
		String fileName = bible.getAbbr() + "-" + bookNumber + "-" + book.getLongName() + ".docx";
		String filePath = BibleConverter.outputPath + File.separator + fileName;

		// Create new Word document
		XWPFDocument document = new XWPFDocument();

		try {
			// Add book title as Heading 1
			XWPFParagraph bookTitle = document.createParagraph();
			bookTitle.setStyle("Heading1");
			bookTitle.setAlignment(ParagraphAlignment.CENTER);
			XWPFRun bookTitleRun = bookTitle.createRun();
			bookTitleRun.setText(book.getLongName());
			bookTitleRun.setBold(true);
			bookTitleRun.setFontSize(24);

			// Add blank line after book title
			document.createParagraph();

			// Process each chapter
			for (Chapter chapter : book.getChapters()) {
				// Add chapter heading as Heading 2
				XWPFParagraph chapterHeading = document.createParagraph();
				chapterHeading.setStyle("Heading2");
				XWPFRun chapterRun = chapterHeading.createRun();
				chapterRun.setText(book.getLongName() + " " + chapter.getChapter());
				chapterRun.setBold(true);
				chapterRun.setFontSize(18);

				// Add blank line after chapter heading
				document.createParagraph();

				// Add each verse
				for (Verse verse : chapter.getVerses()) {
					XWPFParagraph verseParagraph = document.createParagraph();
					String verseText = verse.getNumber() + ". " + verse.getUnParsedText();
					applyVerseText(verseParagraph, verseText);
				}

				// Add blank line after each chapter
				document.createParagraph();
			}

			// Write document to file
			FileOutputStream out = new FileOutputStream(filePath);
			document.write(out);
			out.close();
			document.close();

			System.out.println("Created Word document: " + fileName);

		} catch (Exception e) {
			document.close();
			throw e;
		}
	}

	// =========================================================================
	// createMSWord page builders
	// =========================================================================

	private static void createPageSettings(XWPFDocument document) {
		CTDocument1 doc = document.getDocument();
		CTBody body = doc.getBody();
		if (!body.isSetSectPr()) {
			body.addNewSectPr();
		}
		CTSectPr section = body.getSectPr();
		if (!section.isSetPgSz()) {
			section.addNewPgSz();
		}
		CTPageSz pageSize = section.getPgSz();
		pageSize.setOrient(STPageOrientation.PORTRAIT);
		pageSize.setW(BigInteger.valueOf(595 * 20));
		pageSize.setH(BigInteger.valueOf(842 * 20));
		System.out.println("Page settings applied (A4 portrait)");
	}

	private static void createMetaData(XWPFDocument document, Bible bible) {
		CoreProperties props = document.getProperties().getCoreProperties();
		String creator = "Yesudas Solomon, www.WordOfGod.in";
		props.setCreator(creator);
		props.setLastModifiedByUser(creator);
		try {
			document.getProperties().commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Metadata creation completed");
	}

	private static void createTitlePage(XWPFDocument document, Bible bible) {
		XWPFParagraph paragraph;
		XWPFRun run;

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(72);
		run.setText(bible.getCommonName());
		run.addBreak();
		run.addBreak();

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(22);
		run.setText("| " + bible.getTotalBooks() + " Books |");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(22);
		run.setText("| " + bible.getTotalChapters() + " Chapters |");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(22);
		run.setText("| " + bible.getTotalVerses() + " Verses |");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(22);
		run.setText("| " + bible.getTotalWords() + " Words |");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(22);
		run.setText("| " + bible.getTotalUniqueWords() + " Unique Words |");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(16);
		run.addBreak();
		run.addBreak();
		run.addBreak();
		run.addBreak();
		run.setText("By:");

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_TITLE);
		run.setFontSize(20);
		run.setText(nvl(bible.getPublishedBy()));
		run.addBreak();
		run.addBreak();
		run.addBreak();

		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(18);
		run.setText("Given free of cost based on Matthew 10:8 - \"Freely Received; Freely Give\". So, Share it freely!");
		run.addBreak(BreakType.PAGE);

		System.out.println("Title page creation completed");
	}

	private static void createBookDetailsPage(XWPFDocument document, Bible bible) {
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.LEFT);

		XWPFRun run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("First Edition " + nvl(bible.getPublishedYear()));
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Common Name: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getCommonName()));

		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Short Name: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getShortName()));

		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Long Name: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getLongName()));

		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Long Name English: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getLongEnglishName()));

		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Translated By: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getTranslatedBy()));

		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Published By: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getPublishedBy()));
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Created By: (Word Document with Easy Navigation)");
		run.addCarriageReturn();
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("Yesudas Solomon, www.WordOfGod.in");
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Copyright: ");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(nvl(bible.getCopyRight()));
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("This book is free to download, print and share without any permission.");
		run.addCarriageReturn();
		run = paragraph.createRun();
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Download:");
		run.addCarriageReturn();
		run = paragraph.createRun();
		createExternalLink(paragraph, "www.WordOfGod.in", "https://www.WordOfGod.in");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText(" and ");
		createExternalLink(paragraph, "www.Archive.org", "https://www.Archive.org");
		run = paragraph.createRun();
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Contact Us:");
		run.addCarriageReturn();
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("Email: wordofgod@wordofgod.in");
		run.addCarriageReturn();
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("Mobile/WhatsApp: +91 7676 50 5599");
		run.addBreak();
		run.addBreak();

		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setBold(true);
		run.setText("Follow Us:");
		run.addCarriageReturn();
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("YouTube: ");
		createExternalLink(paragraph, "Bible Minutes", "https://www.youtube.com/c/BibleMinutes");
		run = paragraph.createRun();
		run.addCarriageReturn();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(14);
		run.setText("Facebook: ");
		createExternalLink(paragraph, "Bible Minutes", "https://www.facebook.com/BibleMinutesForChrist");

		addSectionBreak(document, 1, false);
		System.out.println("Book details page creation completed");
	}

	private static void createPDFIssuePage(XWPFDocument document) {
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = paragraph.createRun();
		run.addBreak();
		run.addBreak();
		run.addBreak();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(24);
		run.setText("இந்த PDF-ஐ மொபைலில் பயன்படுத்தினால், அட்டவணையில் உள்ள லிங்க்கள்(Index) கூகிள் ட்ரைவ்(Google Drive PDF Viewer) என்னும் ஆப்பில் (செயலியில்) வேலை செய்யாது, ReadEra என்னும் ஆப்பை (செயலி) பயன்படுத்துங்கள். "
				+ "If you are using this PDF in mobile, Navigation by Index may not work with Google Drive's PDF viewer. I would recommend ReadEra App for better performance and navigation experience.");

		addSectionBreak(document, 1, false);
		System.out.println("Navigation guidance page creation completed");
	}

	private static void createIndex(XWPFDocument document, Bible bible) {
		System.out.println("Index creation started...");
		XWPFParagraph paragraph;
		XWPFRun run;

		// Index heading with grey shading and bookmark
		paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		paragraph.setStyle("Heading1");
		run = paragraph.createRun();
		run.setFontFamily(FONT_BODY);
		run.setFontSize(35);
		run.setText("Index");
		CTShd cTShd = run.getCTR().addNewRPr().addNewShd();
		cTShd.setVal(STShd.CLEAR);
		cTShd.setFill("ABABAB");

		CTBookmark bookmark = paragraph.getCTP().addNewBookmarkStart();
		bookmark.setName("Index");
		bookmark.setId(BigInteger.valueOf(uniqueBookMarkCounter));
		paragraph.getCTP().addNewBookmarkEnd().setId(BigInteger.valueOf(uniqueBookMarkCounter));
		uniqueBookMarkCounter++;

		// Books list (clickable links)
		paragraph = document.createParagraph();
		paragraph.setSpacingAfter(0);
		for (Book book : bible.getBooks()) {
			createAnchorLink(paragraph, book.getLongName(), bookmarkName(book.getLongName()), true, "", FONT_BODY, 19);
		}

		paragraph = document.createParagraph();
		run = paragraph.createRun();
		addSectionBreak(document, 3, true);

		// Chapter index per book (3-column layout)
		for (Book book : bible.getBooks()) {
			paragraph = document.createParagraph();
			run = paragraph.createRun();
			run.setFontFamily(FONT_BODY);
			run.setFontSize(12);
			run.setBold(true);
			run.setText(book.getLongName() + " : ");
			for (Chapter chapter : book.getChapters()) {
				createAnchorLink(paragraph, chapter.getChapter(),
						bookmarkName(book.getLongName()) + "_" + chapter.getChapter(),
						false, "   ", FONT_BODY, 12);
			}
		}

		paragraph = document.createParagraph();
		run = paragraph.createRun();
		addSectionBreak(document, 2, true);
		System.out.println("Index creation completed");
	}

	private static void createContent(XWPFDocument document, Bible bible) {
		System.out.println("Content creation started...");
		XWPFParagraph paragraph;
		XWPFRun run;

		for (Book book : bible.getBooks()) {
			// Book heading page (centered, large, with bookmark and chapter links)
			paragraph = document.createParagraph();
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			run = paragraph.createRun();
			run.addBreak();
			run.addBreak();
			run.setFontFamily(FONT_TITLE);
			run.setFontSize(36);
			run.setText(book.getLongName());

			CTBookmark bookmark = paragraph.getCTP().addNewBookmarkStart();
			bookmark.setName(bookmarkName(book.getLongName()));
			bookmark.setId(BigInteger.valueOf(uniqueBookMarkCounter));
			paragraph.getCTP().addNewBookmarkEnd().setId(BigInteger.valueOf(uniqueBookMarkCounter));
			uniqueBookMarkCounter++;

			paragraph = document.createParagraph();
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			run = paragraph.createRun();
			run.addBreak();
			for (Chapter chapter : book.getChapters()) {
				createAnchorLink(paragraph, chapter.getChapter(),
						bookmarkName(book.getLongName()) + "_" + chapter.getChapter(),
						false, "    ", FONT_BODY, 24);
			}
			run = paragraph.createRun();
			run.addBreak();

			addSectionBreak(document, 1, false);

			// Chapters and verses (2-column layout applied by the closing section break)
			for (Chapter chapter : book.getChapters()) {
				paragraph = document.createParagraph();
				paragraph.setAlignment(ParagraphAlignment.CENTER);
				run = paragraph.createRun();
				run.setFontFamily(FONT_BODY);
				run.setFontSize(18);
				run.setText(book.getLongName() + " " + chapter.getChapter());
				CTShd ctShd = run.getCTR().addNewRPr().addNewShd();
				ctShd.setVal(STShd.CLEAR);
				ctShd.setFill("ABABAB");

				CTBookmark chapterMark = paragraph.getCTP().addNewBookmarkStart();
				chapterMark.setName(bookmarkName(book.getLongName()) + "_" + chapter.getChapter());
				chapterMark.setId(BigInteger.valueOf(uniqueBookMarkCounter));
				paragraph.getCTP().addNewBookmarkEnd().setId(BigInteger.valueOf(uniqueBookMarkCounter));
				uniqueBookMarkCounter++;

				for (Verse verse : chapter.getVerses()) {
					paragraph = document.createParagraph();
					paragraph.setAlignment(ParagraphAlignment.BOTH);

					// Bold verse number
					run = paragraph.createRun();
					run.setFontFamily(FONT_BODY);
					run.setFontSize(14);
					run.setBold(true);
					run.setText(verse.getNumber() + ". ");

					// Verse text with color markup applied
					applyVerseText(paragraph, verse.getUnParsedText(), FONT_BODY, 14);
				}
			}

			paragraph = document.createParagraph();
			run = paragraph.createRun();
			addSectionBreak(document, 1, true);
		}

		System.out.println("Content creation completed");
	}

	private static void outputStatistics(Bible bible) {
		System.out.println("=== Bible Statistics ===");
		System.out.println("Total Books:        " + bible.getTotalBooks());
		System.out.println("Total Chapters:     " + bible.getTotalChapters());
		System.out.println("Total Verses:       " + bible.getTotalVerses());
		System.out.println("Total Words:        " + bible.getTotalWords());
		System.out.println("Total Unique Words: " + bible.getTotalUniqueWords());
		System.out.println("========================");
	}

	// =========================================================================
	// Document structure helpers
	// =========================================================================

	private static CTSectPr addSectionBreak(XWPFDocument document, int noOfColumns, boolean setMargin) {
		document.createParagraph();
		XWPFParagraph paragraph = document.createParagraph();
		CTSectPr ctSectPr = paragraph.getCTP().addNewPPr().addNewSectPr();
		CTColumns ctColumns = ctSectPr.addNewCols();
		ctColumns.setNum(BigInteger.valueOf(noOfColumns));
		if (setMargin) {
			CTPageMar pageMar = ctSectPr.getPgMar();
			if (pageMar == null) {
				pageMar = ctSectPr.addNewPgMar();
			}
			// 648 twips = 0.45 inches
			pageMar.setLeft(BigInteger.valueOf(648));
			pageMar.setRight(BigInteger.valueOf(648));
			pageMar.setTop(BigInteger.valueOf(648));
			pageMar.setBottom(BigInteger.valueOf(648));
		}
		return ctSectPr;
	}

	private static void createAnchorLink(XWPFParagraph paragraph, String linkText, String bookMarkName,
			boolean carriageReturn, String space, String fontFamily, int fontSize) {
		CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
		cthyperLink.setAnchor(bookMarkName);
		CTR ctr = cthyperLink.addNewR();
		XWPFHyperlinkRun hyperlinkRun = new XWPFHyperlinkRun(cthyperLink, ctr, paragraph);
		hyperlinkRun.setFontFamily(fontFamily);
		hyperlinkRun.setFontSize(fontSize);
		hyperlinkRun.setText(linkText);
		hyperlinkRun.setColor("0000FF");
		hyperlinkRun.setUnderline(UnderlinePatterns.SINGLE);
		if (space != null && !space.isEmpty()) {
			XWPFRun run = paragraph.createRun();
			run.setText(space);
		}
		if (carriageReturn) {
			XWPFRun run = paragraph.createRun();
			run.addCarriageReturn();
		}
	}

	private static void createExternalLink(XWPFParagraph paragraph, String linkText, String linkURL) {
		String id = paragraph.getDocument().getPackagePart()
				.addExternalRelationship(linkURL, XWPFRelation.HYPERLINK.getRelation()).getId();
		CTHyperlink cthyperLink = paragraph.getCTP().addNewHyperlink();
		cthyperLink.setId(id);
		CTR ctr = cthyperLink.addNewR();
		XWPFHyperlinkRun hyperlinkRun = new XWPFHyperlinkRun(cthyperLink, ctr, paragraph);
		hyperlinkRun.setFontFamily(FONT_BODY);
		hyperlinkRun.setFontSize(14);
		hyperlinkRun.setText(linkText);
		hyperlinkRun.setColor("0000FF");
		hyperlinkRun.setUnderline(UnderlinePatterns.SINGLE);
	}

	private static String bookmarkName(String name) {
		return name.replaceAll("\\s+", "_");
	}

	private static String nvl(String value) {
		return value != null ? value : "";
	}

	// =========================================================================
	// Shared verse text helpers (used by both createMSWord and createMSWordByBooks)
	// =========================================================================

	private static void applyVerseText(XWPFParagraph paragraph, String verseText) {
		applyVerseText(paragraph, verseText, null, 12);
	}

	private static void applyVerseText(XWPFParagraph paragraph, String verseText, String fontFamily, int fontSize) {
		Pattern pattern = Pattern.compile("<n>(.*?)</n>|<e>(.*?)</e>");
		Matcher matcher = pattern.matcher(verseText);

		int lastEnd = 0;
		while (matcher.find()) {
			if (matcher.start() > lastEnd) {
				createRun(paragraph, verseText.substring(lastEnd, matcher.start()), null, fontFamily, fontSize);
			}
			if (matcher.group(1) != null) {
				createRun(paragraph, matcher.group(1), "808080", fontFamily, fontSize); // grey for notes
			} else {
				createRun(paragraph, matcher.group(2), "3A7DCC", fontFamily, fontSize); // blue for emphasis
			}
			lastEnd = matcher.end();
		}

		if (lastEnd < verseText.length()) {
			createRun(paragraph, verseText.substring(lastEnd), null, fontFamily, fontSize);
		}
	}

	private static void createRun(XWPFParagraph paragraph, String text, String hexColor, String fontFamily,
			int fontSize) {
		XWPFRun run = paragraph.createRun();
		run.setText(text);
		run.setFontSize(fontSize);
		if (fontFamily != null) {
			run.setFontFamily(fontFamily);
		}
		if (hexColor != null) {
			run.setColor(hexColor);
		}
	}
}
