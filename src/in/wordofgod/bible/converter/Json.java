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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		info.totalBooks = bible.getTotalBooks();
		info.totalChapters = bible.getTotalChapters();
		info.totalVerses = bible.getTotalVerses();
		info.totalWords = bible.getTotalWords();
		info.totalUniqueWords = bible.getTotalUniqueWords();
		info.hasOT = bible.isHasOT();
		info.hasNT = bible.isHasNT();

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
				String tempVerse = null;
				for (Verse verse : chapter.getVerses()) {
					tempVerse = verse.getUnParsedText();
					
					tempVerse = applyStyleForEmphasisWords(tempVerse);
					tempVerse = applyStyleForNotes(tempVerse);
					
					verses.add(new VerseJson(Integer.valueOf(verse.getNumber()), tempVerse));
				}
				String filePath = bookDir + File.separator + chapter.getChapter() + ".json";
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

		String filePath = BibleConverter.outputPath + File.separator + "bibles.json";
		createBibleInfoFile(filePath, biblesJson);

		System.out.println("Json Creation Completed...");
		System.out.println("Results are saved in the directory: " + BibleConverter.outputPath);
	}

	private static String applyStyleForNotes(String verseText) {
		return wrapWordsWithSpan(verseText, "n", "notes");
	}

	private static String applyStyleForEmphasisWords(String verseText) {
		return wrapWordsWithSpan(verseText, "e", "emphasis");
	}

	private static String wrapWordsWithSpan(String verseText, String tag, String spanClass) {
		Pattern pattern = Pattern.compile("<" + tag + ">(.*?)</" + tag + ">");
		Matcher matcher = pattern.matcher(verseText);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String inner = matcher.group(1).trim();
			String[] words = inner.isEmpty() ? new String[0] : inner.split("\\s+");
			StringBuilder replacement = new StringBuilder();
			for (int i = 0; i < words.length; i++) {
				if (i > 0) replacement.append(" ");
				replacement.append("<span class=\"").append(spanClass).append("\">").append(words[i]).append("</span>");
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private static void createDir(String dirPath) {
		File dir = new File(BibleConverter.outputPath + File.separator + dirPath);
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
			mapper.writeValue(new File(BibleConverter.outputPath + File.separator + filePath), chapter);
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