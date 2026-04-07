/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to MS Word (.docx) format
 * Creates separate Word document for each book
 */
public class MSWord {

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
					XWPFRun verseRun = verseParagraph.createRun();
					
					// Format: verseNumber. verseText
					String verseText = verse.getNumber() + ". " + verse.getUnParsedText();
					verseRun.setText(verseText);
					verseRun.setFontSize(12);
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
}