package in.wordofgod.bible.converter;

public class BookJson {

	public int bookNo;
	public String shortName;
	public String longName;
	public String englishName;
	public int chapterCount;

	public BookJson(int bookNo, String shortName, String longName, String englishName, int chapterCount) {
		this.bookNo = bookNo;
		this.shortName = shortName;
		this.longName = longName;
		this.englishName = englishName;
		this.chapterCount = chapterCount;
	}
}
