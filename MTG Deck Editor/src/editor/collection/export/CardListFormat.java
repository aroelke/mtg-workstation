package editor.collection.export;

import editor.collection.CardList;

public interface CardListFormat
{
	String format(CardList list);
	
	CardList parse(String source);
}
