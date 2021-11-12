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
  private val categoryBoxes = collection.mutable.HashMap[Category, TristateCheckBox]()
  private var preferredViewportHeight = 0

  categories.foreach((category) => {
    val categoryBox = TristateCheckBox(category.getName)
    val matches = cards.count(category.includes(_))
    if (matches == 0)
      categoryBox.setState(TristateCheckBox.State.UNSELECTED)
    else if (matches < cards.size)
      categoryBox.setState(TristateCheckBox.State.INDETERMINATE)
    else
      categoryBox.setState(TristateCheckBox.State.SELECTED)
    categoryBox.setBackground(Color.WHITE)
    add(categoryBox)
    categoryBoxes.put(category, categoryBox)
    preferredViewportHeight = Math.min(preferredViewportHeight + categoryBox.getPreferredSize.height, categoryBox.getPreferredSize.height * MaxPreferredRows)
  })

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
    if (categoryBoxes.isEmpty) {
      getPreferredSize
    } else {
      val size = getPreferredSize
      size.height = preferredViewportHeight
      size
    }
  }
}
