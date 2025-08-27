package in.wordofgod.bible.converter;

import java.util.List;

public class ChapterJson {
	private String bibleAbbr;
	private String bookName;
	private String bookEnglishName;
	private int chapterNumber;
	private List<VerseJson> verses;

	public ChapterJson(String bibleAbbr, String bookName, String bookEnglishName, int chapterNumber,
			List<VerseJson> verses) {
		this.bibleAbbr = bibleAbbr;
		this.bookName = bookName;
		this.bookEnglishName = bookEnglishName;
		this.chapterNumber = chapterNumber;
		this.verses = verses;
	}

	// Getters and setters
	public String getBibleAbbr() {
		return bibleAbbr;
	}

	public void setBibleAbbr(String bibleAbbr) {
		this.bibleAbbr = bibleAbbr;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getBookEnglishName() {
		return bookEnglishName;
	}

	public void setBookEnglishName(String bookEnglishName) {
		this.bookEnglishName = bookEnglishName;
	}

	public int getChapterNumber() {
		return chapterNumber;
	}

	public void setChapterNumber(int chapterNumber) {
		this.chapterNumber = chapterNumber;
	}

	public List<VerseJson> getVerses() {
		return verses;
	}

	public void setVerses(List<VerseJson> verses) {
		this.verses = verses;
	}
}