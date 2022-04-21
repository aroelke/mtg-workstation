package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card

import scala.jdk.CollectionConverters._

/**
 * Filter that groups cards by user-defined tags.
 * @author Alec Roelke
 */
class TagsFilter extends MultiOptionsFilter[String](CardAttribute.TAGS, true, Card.tags.get(_).map(_.toSet).getOrElse(Set.empty))