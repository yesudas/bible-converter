/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * 
 */
public class Json {

	public static void createJson() throws URISyntaxException {
		System.out.println("Json Creation Started...");
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

		BibleInfoJson info = new BibleInfoJson();
		info.abbr = bible.getAbbr();
		info.commonName = bible.getCommonName();
		info.shortName = bible.getShortName();
		info.longName = bible.getLongName();
		info.longEnglishName = bible.getLongEnglishName();
		info.languageCode = bible.getLanguageCode();
		info.publishedYear = bible.getPublishedYear();
		info.publishedBy = bible.getPublishedBy();
		info.translatedBy = bible.getTranslatedBy();
		info.copyRight = bible.getCopyRight();
		info.additionalInformation = bible.getAdditionalInformation();

		List<BookJson> booksJson = new ArrayList<>();

		BibleJson bible1 = new BibleJson(1, false, info, booksJson);
		BiblesJson biblesJson = new BiblesJson(Arrays.asList(bible1));

		for (Book book : bible.getBooks()) {
			booksJson.add(new BookJson(book.getBookNo(), book.getShortName(), book.getLongName(), book.getEnglishName(),
					book.getChapters().size()));
			String bookDir = book.getBookNo() + "-" + book.getLongName();
			createDir(bookDir);
			int chapterCount = 1;
			for (Chapter chapter : book.getChapters()) {
				if(chapter.getChapter()==null || chapter.getChapter().isEmpty()) {
					System.out.println("Invalid chapter in " + book.getLongName() + " Chapter number: " + chapterCount);
					chapter.setChapter("" + chapterCount);
				}
				List<VerseJson> verses = new ArrayList<>();
				for (Verse verse : chapter.getVerses()) {
					verses.add(new VerseJson(Integer.valueOf(verse.getNumber()), verse.getUnParsedText()));
				}
				String filePath = bookDir + "/" + chapter.getChapter() + ".json";
				try {
					ChapterJson chap = new ChapterJson(bible.getAbbr(), book.getLongName(), book.getEnglishName(),
							Integer.valueOf(chapter.getChapter()), verses);
					createFile(filePath, chap);
				} catch (NumberFormatException e) {
					System.out.println(
							"Error creating JSON for " + book.getLongName() + " Chapter " + chapter.getChapter() + " : File Path: "+ filePath);
					e.printStackTrace();
					throw e;
				}
			}
		}

		String filePath = BibleConverter.outputPath + "/" + "bibles.json";
		createBibleInfoFile(filePath, biblesJson);

		System.out.println("Json Creation Completed...");
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

	private static void createFile(String filePath, ChapterJson chapter) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			// Removed pretty print for smaller file size
			mapper.writeValue(new File(BibleConverter.outputPath + "/" + filePath), chapter);
			System.out.println("Created the file: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void createBibleInfoFile(String filePath, BiblesJson biblesJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			// Removed pretty print for smaller file size
			mapper.writeValue(new File(filePath), biblesJson);
			System.out.println("Created the file: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
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

}