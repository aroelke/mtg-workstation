package editor.gui

import editor.collection.CardList
import editor.gui.deck.EditorFrame
import editor.gui.display.CardTable

case class SelectionData(table: Option[CardTable], list: Option[CardList], frame: Option[EditorFrame], indices: IndexedSeq[Int]) {
  lazy val entries = list.map(indices.map).getOrElse(IndexedSeq.empty)
  lazy val cards = entries.map(_.card)
}