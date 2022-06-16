package editor.gui.editor

import editor.gui.generic.ScrollablePanel
import editor.collection.deck.Category
import editor.database.card.Card
import editor.gui.generic.TristateCheckBox
import editor.gui.generic.TristateCheckBoxState
import javax.swing.BoxLayout
import java.awt.Color

/**
 * Panel allowing the user to edit the category inclusion of cards.  It contains a list of check boxes whose labels
 * are the names of categories. A checked box indicates that all cards in the list are included in the category, an
 * unchecked box indicates none of them are, and a partially-checked (square icon) box indicates that some are. The
 * user can only fully check or uncheck boxes.
 * 
 * @constructor create a new panel for editing the inclusion of a list of cards for a list of categories
 * @param categories categories to edit
 * @param cards cards to include or exclude
 * 
 * @author Alec Roelke
 */
class IncludeExcludePanel(categories: Seq[Category], cards: Seq[Card]) extends ScrollablePanel(ScrollablePanel.TrackWidth) {
  private val MaxPreferredRows = 10

  setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
  setBackground(Color.WHITE)

  private val categoryBoxes = categories.map((category) => {
    val matches = cards.count(category.includes)
    val categoryBox = TristateCheckBox(category.getName, 
      if (matches == 0)
        TristateCheckBoxState.Unselected
      else if (matches < cards.size)
        TristateCheckBoxState.Indeterminate
      else
        TristateCheckBoxState.Selected
    )
    categoryBox.setBackground(Color.WHITE)
    category -> categoryBox
  }).toMap
  categories.foreach((c) => add(categoryBoxes(c)))

  /**
   * Get the categories that should have cards included in them. Categories are not actually updated.
   * @return a mapping of cards onto the categories they should be included in
   */
  def included = cards.map((c) => c -> categoryBoxes.collect{ case (category, box) if (box.state == TristateCheckBoxState.Selected && !category.includes(c)) => category }.toSet).toMap

  /**
   * Get the categories that should have cards excluded from them. Categories are not actually updated.
   * @return a mapping of cards onto the categories they should be excluded from
   */
  def excluded = cards.map((c) => c -> categoryBoxes.collect{ case (category, box) if (box.state == TristateCheckBoxState.Unselected && category.includes(c)) => category }.toSet).toMap

  /** @return copies of the provided categories, updated to include or exclude cards as selected by the user. */
  def updates = {
    val inc = included
    val exc = excluded
    categories.collect{ case category if inc.values.exists(_.contains(category)) || exc.values.exists(_.contains(category)) =>
      val c = Category(category)
      inc.foreach{ case (card, in) => if (in.contains(category)) c.include(card) }
      exc.foreach{ case (card, ex) => if (ex.contains(category)) c.exclude(card) }
      c
    }
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
