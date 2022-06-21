package editor.database.card

/**
 * All of the possible types of card layouts, with a flag indicating if it's multi-faced or not.
 * 
 * @param toString string representation of the card layout
 * @param isMultiFaced whether or not the layout has multiple faces or inset cards
 * 
 * @author Alec Roelke
 */
enum CardLayout(override val toString: String, val isMultiFaced: Boolean) extends Ordered[CardLayout] {
  def compare(that: CardLayout) = ordinal - that.ordinal

  /** Normal, single-faced card. */
  case NORMAL extends CardLayout("Normal", false)
  /** Card with mulitple inset mini-cards present on the front face. Usually there's only two mini-cards. */
  case SPLIT extends CardLayout("Split", true)
  /** Special split card where one mini-card can be cast from hand and the other from the graveyard. */
  case AFTERMATH extends CardLayout("Aftermath", true)
  /** Card with one face on top and the other on the bottom that is accessible by rotating it 180 degrees. */
  case FLIP extends CardLayout("Flip", true)
  /** Card with one face on the front and one on the back that can transform after casting it. */
  case TRANSFORM extends CardLayout("Transform", true)
  /** Special transform card where two physical cards share a back half and combine together during play to transform into it. */
  case MELD extends CardLayout("Meld", true)
  /** Single-faced card with multiple text boxes that gradually advances over the course of the game. */
  case LEVELER extends CardLayout("Leveler", false)
  /** Single-faced card, usually an enchantment, with multiple text boxes that advances every turn. */
  case SAGA extends CardLayout("Saga", false)
  /** Single-faced card, usually an enchantment, with multiple text boxes that accumulate based on payment of a cost. */
  case CLASS extends CardLayout("Class", false)
  /** Special split card with a "main" permanent face and an "adventure" non-permanent face that can later be cast as the permanent. */
  case ADVENTURE extends CardLayout("Adventure", true)
  /** Special double-faced card that can be played as either side, but can't transform after. */
  case MODAL_DFC extends CardLayout("Modal DFC", true)
  /** Special double-faced card where both faces are the same, but one is full-art. Treated as a single-faced card. */
  case REVERSIBLE_CARD extends CardLayout("Reversible", false)
  /** Special single-faced card that can host an [[AUGMENT]]. */
  case HOST extends CardLayout("Host", false)
  /** Special single-faced card that can only be played attached to a [[HOST]]. */
  case AUGMENT extends CardLayout("Augment", false)

  /** An extra-large single-faced card for use in the Planechase format. */
  case PLANAR extends CardLayout("Planar", false)
  /** An extra-large single-faced card for use in the Archenemy format. */
  case SCHEME extends CardLayout("Scheme", false)
  /** An extra-large single-faced card for use in the Vanguard format. */
  case VANGUARD extends CardLayout("Vanguard", false)
}