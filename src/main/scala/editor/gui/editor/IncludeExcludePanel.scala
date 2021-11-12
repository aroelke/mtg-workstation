package editor.gui.editor

import editor.gui.generic.ScrollablePanel
import editor.collection.deck.Category
import editor.database.card.Card
import editor.gui.generic.TristateCheckBox
import javax.swing.BoxLayout
import java.awt.Color

class IncludeExcludePanel(categories: Seq[Category], cards: Seq[Card]) extends ScrollablePanel(ScrollablePanel.TRACK_WIDTH) {
  private val MaxPreferredRows = 10

  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(Color.WHITE)

  private val categoryBoxes = categories.map((category) => {
    val matches = cards.count(category.includes(_))
    val categoryBox = TristateCheckBox(category.getName, 
      if (matches == 0)
        TristateCheckBox.State.UNSELECTED
      else if (matches < cards.size)
        TristateCheckBox.State.INDETERMINATE
      else
        TristateCheckBox.State.SELECTED
    )
    categoryBox.setBackground(Color.WHITE)
    category -> categoryBox
  }).toMap
  categories.foreach((c) => add(categoryBoxes(c)))

  def included = cards.map((c) => c -> categoryBoxes.collect{ case (category, box) if (box.getState == TristateCheckBox.State.SELECTED && !category.includes(c)) => category }.toSet).toMap

  def excluded = cards.map((c) => c -> categoryBoxes.collect{ case (category, box) if (box.getState == TristateCheckBox.State.UNSELECTED && category.includes(c)) => category }.toSet).toMap

  override def getPreferredScrollableViewportSize = {
    val size = getPreferredSize
    if (!categoryBoxes.isEmpty)
      size.height = Math.min(
        categoryBoxes.map{ case (_, b) => b.getPreferredSize.height }.fold(0)(_ + _),
        categoryBoxes.headOption.map{ case (_, b) => b.getPreferredSize.height*MaxPreferredRows }.getOrElse(0)
      )
    size
  }
}
