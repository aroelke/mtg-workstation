package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.database.card.Card

/**
 * Filter that groups cards by user-defined tags.
 * @author Alec Roelke
 */
class TagsFilter extends MultiOptionsFilter[String, TagsFilter](CardAttribute.Tags, true, Card.tags.get(_).map(_.toSet).getOrElse(Set.empty))