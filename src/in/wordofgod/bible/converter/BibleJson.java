package in.wordofgod.bible.converter;

import java.util.List;

public class BibleJson {

	public int id;
    public boolean isDefault;
    public BibleInfoJson info;
    public List<BookJson> books;

    public BibleJson(int id, boolean isDefault, BibleInfoJson info, List<BookJson> books) {
        this.id = id;
        this.isDefault = isDefault;
        this.info = info;
        this.books = books;
    }
    
}
