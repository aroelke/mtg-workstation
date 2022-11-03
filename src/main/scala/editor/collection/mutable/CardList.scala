package editor.collection.mutable

import editor.collection.CardListEntry

/**
 * Mutable list of [[CardListEntry]]s. Adding and removing entries should only create new entries (and increase [[size]]) if there is not
 * already an entry with the same card. Otherwise, the existing entry should be modified instead, changing [[total]] but not [[size]] unless
 * the modification causes the entry to be removed (for example, if its count reaches 0).
 * 
 * @author Alec Roelke
 */
trait CardList extends editor.collection.immutable.CardList
    with collection.mutable.IndexedSeq[CardListEntry]
    with collection.mutable.Growable[CardListEntry]
    with collection.mutable.Shrinkable[CardListEntry]
    with collection.mutable.Clearable {
  override def knownSize: Int = size
}