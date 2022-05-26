package editor.gui

import _root_.editor.collection.CardList
import _root_.editor.gui.display.CardTable
import _root_.editor.gui.editor.EditorFrame

case class SelectionData(table: Option[CardTable], list: Option[CardList], frame: Option[EditorFrame], indices: IndexedSeq[Int]) {
  lazy val entries = list.map(indices.map).getOrElse(IndexedSeq.empty)
  lazy val cards = entries.map(_.card)
}