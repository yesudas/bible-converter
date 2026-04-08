/**
 * 
 */
package in.wordofgod.bible.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.wordofgod.bible.parser.Bible;
import in.wordofgod.bible.parser.TheWord;
import in.wordofgod.bible.parser.vosgson.Book;
import in.wordofgod.bible.parser.vosgson.Chapter;
import in.wordofgod.bible.parser.vosgson.Verse;

/**
 * Converts Bible text to JsonBible format (https://github.com/ChurchApps/json-bible).
 * Produces a single JSON file containing all books, chapters, and verses
 * together with a rich metadata block.
 */
public class JsonBible {

	public static void createJsonBible() throws java.net.URISyntaxException {
		System.out.println("JsonBible Creation Started...");
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

		// Build metadata map
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("title",                  bible.getCommonName());
		metadata.put("identifier",             bible.getAbbr());
		metadata.put("description",            trimmedConcat(bible.getTranslatedBy(), ". ", bible.getAdditionalInformation()));
		metadata.put("language",               bible.getLanguageCode());
		metadata.put("publisher",              bible.getPublishedBy());
		metadata.put("publishDate",            bible.getPublishedYear());
		metadata.put("copyright",              bible.getCopyRight());
		metadata.put("additionalInformation",  bible.getAdditionalInformation());
		metadata.put("translatedBy",           bible.getTranslatedBy());
		metadata.put("commonName",             bible.getCommonName());
		metadata.put("shortName",              bible.getShortName());
		metadata.put("longName",               bible.getLongName());
		metadata.put("longEnglishName",        bible.getLongEnglishName());
		metadata.put("totalBooks",             bible.getTotalBooks());
		metadata.put("totalChapters",          bible.getTotalChapters());
		metadata.put("totalVerses",            bible.getTotalVerses());
		metadata.put("totalWords",             bible.getTotalWords());
		metadata.put("totalUniqueWords",       bible.getTotalUniqueWords());
		metadata.put("hasOT",                  bible.isHasOT());
		metadata.put("hasNT",                  bible.isHasNT());
		metadata.put("generatedBy",            "Created using BibleConverter from https://github.com/yesudas/bible-converter");

		// Build books list
		List<Map<String, Object>> books = new ArrayList<>();
		for (Book book : bible.getBooks()) {
			Map<String, Object> bookMap = new LinkedHashMap<>();
			bookMap.put("number", book.getBookNo());
			bookMap.put("name",   book.getLongName());

			List<Map<String, Object>> chapters = new ArrayList<>();
			int chapterCount = 1;
			for (Chapter chapter : book.getChapters()) {
				if (chapter.getChapter() == null || chapter.getChapter().isEmpty()) {
					System.out.println("Invalid chapter in " + book.getLongName() + " Chapter number: " + chapterCount);
					chapter.setChapter("" + chapterCount);
				}

				Map<String, Object> chapterMap = new LinkedHashMap<>();
				chapterMap.put("number", Integer.valueOf(chapter.getChapter()));

				List<Map<String, Object>> verses = new ArrayList<>();
				for (Verse verse : chapter.getVerses()) {
					Map<String, Object> verseMap = new LinkedHashMap<>();
					verseMap.put("number", Integer.valueOf(verse.getNumber()));
					verseMap.put("text",   verse.getUnParsedText());
					verses.add(verseMap);
				}
				chapterMap.put("verses", verses);
				chapters.add(chapterMap);
				chapterCount++;
			}
			bookMap.put("chapters", chapters);
			books.add(bookMap);
		}

		// Assemble root object
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("name",     bible.getCommonName());
		root.put("metadata", metadata);
		root.put("books",    books);

		// Write output file
		String fileName = bible.getAbbr() + ".json";
		String filePath = BibleConverter.outputPath + File.separator + fileName;
		try {
			File outputDir = new File(BibleConverter.outputPath);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), root);
			System.out.println("Created the file: " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("JsonBible Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	/** Concatenates two strings with a separator only when both are non-empty. */
	private static String trimmedConcat(String first, String separator, String second) {
		boolean hasFirst  = first  != null && !first.trim().isEmpty();
		boolean hasSecond = second != null && !second.trim().isEmpty();
		if (hasFirst && hasSecond) {
			return first.trim() + separator + second.trim();
		} else if (hasFirst) {
			return first.trim();
		} else if (hasSecond) {
			return second.trim();
		}
		return "";
	}
}
