/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * 
 */
public class TextFiles {

	public static void createTextFilesByDirectory() {
		System.out.println("TextFilesByDirectory Creation Started...");
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

		for (Book book : bible.getBooks()) {
			String bookDir = getBookNo(book.getBookNo()) + " " + book.getLongName();
			createDir(bookDir);
			for (Chapter chapter : book.getChapters()) {
				String filePath = bookDir + "/" + getChapterNo(book.getBookNo(), chapter.getChapter()) + ".txt";
				StringBuilder verses = new StringBuilder();
				for (Verse verse : chapter.getVerses()) {
					String verseText = removeHTMLTags(verse.getText());
					verses.append(verse.getNumber() + ". " + verseText).append("\n");
				}
				createFile(filePath, verses.toString());
			}
		}
		System.out.println("TextFilesByDirectory Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	private static void createDir(String dirPath) {
		File dir = new File(BibleConverter.outputPath + "/" + dirPath);
		if (dir.exists()) {
			System.out.println("Directory already exists: " + dir.getAbsolutePath());
		} else {
			dir.mkdirs();
			System.out.println("Created the directory: " + dir.getAbsolutePath());
		}
	}

	private static void createFile(String filePath, String text) {
		createDir(BibleConverter.outputPath);
		try {
			Files.writeString(Path.of(BibleConverter.outputPath + "/" + filePath), text);
			System.out.println("Created the file: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getBookNo(int bookNumber) {
		DecimalFormat df = new DecimalFormat("00");
		return df.format(bookNumber);
	}

	private static String getChapterNo(int bookNumber, String number) {
		int chapterNumber = Integer.parseInt(number);

		if (bookNumber == 19) {
			DecimalFormat df = new DecimalFormat("000");
			return df.format(chapterNumber);
		} else {
			DecimalFormat df = new DecimalFormat("00");
			return df.format(chapterNumber);
		}
	}

	private static String removeHTMLTags(String text) {
		while (text.contains("<")) {
			int startPos = text.indexOf("<");
			int endPos = text.indexOf(">");
			String htmlTag = text.substring(startPos, endPos + 1);
			text = text.replace(htmlTag, "");
		}
		return text;
	}

	public static void createSingleTextFile() {
		System.out.println("SingleTextFile Creation Started...");
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

		StringBuilder sb = new StringBuilder();
		for (Book book : bible.getBooks()) {
			for (Chapter chapter : book.getChapters()) {
				String chapterHeading = capitalizeFirstLetter(book.getLongName());
				sb.append(chapterHeading).append(" ").append(chapter.getChapter()).append("\n");
				StringBuilder verses = new StringBuilder();
				for (Verse verse : chapter.getVerses()) {
					String verseText = removeHTMLTags(verse.getText());
					verses.append(verse.getNumber() + ". " + verseText + "\n");
				}
				sb.append(verses.toString()).append("\n");
			}
		}
		createFile(bible.getAbbr() + ".txt", sb.toString());
		System.out.println("SingleTextFile Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	public static void createTheWordModuleWithoutHtmlTags() {
		System.out.println("TheWordWithoutHtmlTags Creation Started...");
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

		StringBuilder sb = new StringBuilder();
		for (Book book : bible.getBooks()) {
			for (Chapter chapter : book.getChapters()) {
				// sb.append(book.getLongName()).append("
				// ").append(chapter.getChapter()).append("\n");
				StringBuilder verses = new StringBuilder();
				for (Verse verse : chapter.getVerses()) {
					String verseText = removeHTMLTags(verse.getText());
					verses.append(verseText + "\n");
				}
				sb.append(verses.toString());
			}
		}
		createFile(bible.getAbbr() + ".txt", sb.toString());
		System.out.println("TheWordWithoutHtmlTags Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);

	}

	public static String capitalizeFirstLetter(String str) {

		if (str == null || str.length() == 0)
			return str;

		return str.substring(0, 1).toUpperCase() + str.substring(1);

	}
}
