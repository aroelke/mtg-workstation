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

  def getIncluded = {
    val included = collection.mutable.HashMap[Card, collection.mutable.Set[Category]]()
    cards.foreach((card) => {
      categoryBoxes.keys.foreach((category) => {
        if (categoryBoxes(category).getState == TristateCheckBox.State.SELECTED && !category.includes(card)) {
          if (!included.contains(card))
            included(card) = collection.mutable.HashSet[Category]()
          included(card) += category
        }
      })
    })
    included.map{ case (c, s) => c -> s.toSet }.toMap
  }

  def getExcluded = {
    val excluded = collection.mutable.HashMap[Card, collection.mutable.Set[Category]]()
    cards.foreach((card) => {
      categoryBoxes.keys.foreach((category) => {
        if (categoryBoxes(category).getState == TristateCheckBox.State.UNSELECTED && category.includes(card)) {
          if (!excluded.contains(card))
            excluded(card) = collection.mutable.HashSet[Category]()
          excluded(card) += category
        }
      })
    })
    excluded.map{ case (c, s) => c -> s.toSet }.toMap
  }

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
