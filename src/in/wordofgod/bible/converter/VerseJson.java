package in.wordofgod.bible.converter;

public class VerseJson {
	private int number;
	private String verse;

	public VerseJson(int number, String verse) {
		this.number = number;
		this.verse = verse;
	}

	// Getters and setters (needed for Jackson)
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getVerse() {
		return verse;
	}

	public void setVerse(String verse) {
		this.verse = verse;
	}
}