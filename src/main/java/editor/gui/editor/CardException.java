package editor.gui.editor;

import java.util.Collection;
import java.util.Collections;

import editor.database.card.Card;

public class CardException extends RuntimeException
{
    public final Collection<Card> cards;

    public CardException(Collection<Card> c, String msg)
    {
        super(c.toString() + (msg.isEmpty() ? "" : ": " + msg));
        cards = Collections.unmodifiableCollection(c);
    }

    public CardException(Card c, String msg)
    {
        this(Collections.singleton(c), msg);
    }

    public CardException(Collection<Card> c)
    {
        this(c, "");
    }

    public CardException(Card c)
    {
        this(c, "");
    }
}