package editor.gui.settings

import editor.collection.deck.Category
import editor.database.attributes.CardAttribute
import editor.filter.leaf.options.multi.CardTypeFilter

import java.awt.Color
import scala.jdk.CollectionConverters._

/**
 * Settings structure containing informationa about default categories and category appearance.
 * 
 * @param presets list of preset categories
 * @param rows number of rows of cards to show in categories
 * @param explicits number of rows of cards in the category editor dialog to show for whitelist/
 * blacklist tables
 * 
 * @author Alec Roelke
 */
case class CategoriesSettings(presets: Seq[Category] = Nil, rows: Int = 6, explicits: Int = 3) {
  @deprecated def this() = this(
    Map(
      "Artifacts" -> Seq("Artifact"),
      "Creatures" -> Seq("Creature"),
      "Lands" -> Seq("Land"),
      "Instants/Sorceries" -> Seq("Instant", "Sorcery")
    ).map{ case (name, types) => {
      val filter = CardAttribute.createFilter(CardAttribute.CARD_TYPE).asInstanceOf[CardTypeFilter]
      filter.selected.addAll(types.asJava)
      new Category(name, Seq.empty.asJava, Seq.empty.asJava, Color.WHITE, filter)
    }}.toSeq,
    6, 3
  )
}