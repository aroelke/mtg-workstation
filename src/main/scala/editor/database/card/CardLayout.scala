package editor.database.card

/**
 * All of the possible types of card layouts, with a flag indicating if it's multi-faced or not.
 * 
 * @param name string representation of the card layout
 * @param faces the number of faces or inset mini-cards the layout has (0 means any number)
 * @param combine if the card layout contains multiple faces, combine a list of single faces into a set of [[MultiCard]]s (otherwise throw an exception)
 * 
 * @author Alec Roelke
 */
enum CardLayout(name: String, val faces: Int = 1, combine: (singles: Seq[Card]) => Set[MultiCard] = _ => throw UnsupportedOperationException(s"$this does not have multiple faces to collect")) extends Ordered[CardLayout] {
  /** @return true if the layout represents a card with multiple faces or mini-cards, and false otherwise */
  def isMultiFaced = faces != 1

  /**
   * Collect a list of single-faced cards into a set of multi-faced cards based on layout
   * 
   * @param singles list of cards to collect
   * @return the set of [[MultiCard]]s represented by the list of single-faced cards
   */
  @throws[IllegalArgumentException]("if the list of cards is the wrong size or has the wrong layouts")
  def collect(singles: Seq[Card]) = if (singles.size < faces) {
    throw IllegalArgumentException(s"can't find other face(s) for ${name.toLowerCase} card")
  } else if (singles.size > faces && faces > 0) {
    throw IllegalArgumentException(s"too many faces for ${name.toLowerCase} card")
  } else if (singles.forall(_.layout != this)) {
    throw IllegalArgumentException(s"can't join non-${name.toLowerCase} faces into a ${name.toLowerCase} card")
  } else {
    combine(singles)
  }

  override def compare(that: CardLayout) = ordinal - that.ordinal
  override def toString = name

  /** Normal, single-faced card. */
  case NORMAL extends CardLayout("Normal")
  /** Card with mulitple inset mini-cards present on the front face. Usually there's only two mini-cards. */
  case SPLIT extends CardLayout("Split", 0, (f) => Set(SplitCard(f)))
  /** Special split card where one mini-card can be cast from hand and the other from the graveyard. */
  case AFTERMATH extends CardLayout("Aftermath", 2, (f) => Set(SplitCard(f)))
  /** Card with one face on top and the other on the bottom that is accessible by rotating it 180 degrees. */
  case FLIP extends CardLayout("Flip", 2, _ match { case Seq(top, bottom) => Set(FlipCard(top, bottom)) })
  /** Card with one face on the front and one on the back that can transform after casting it. */
  case TRANSFORM extends CardLayout("Transform", 2, _ match { case Seq(front, back) => Set(TransformCard(front, back)) })
  /** Special transform card where two physical cards share a back half and combine together during play to transform into it. */
  case MELD extends CardLayout("Meld", 3, _ match { case Seq(a, b, back) => Set(MeldCard(a, b, back), MeldCard(b, a, back)) })
  /** Single-faced card with multiple text boxes that gradually advances over the course of the game. */
  case LEVELER extends CardLayout("Leveler")
  /** Single-faced card, usually an enchantment, with multiple text boxes that advances every turn. */
  case SAGA extends CardLayout("Saga")
  /** Single-faced card, usually an enchantment, with multiple text boxes that accumulate based on payment of a cost. */
  case CLASS extends CardLayout("Class")
  /** Single-faced card with the mutate ability. */
  case MUTATE extends CardLayout("Mutate")
  /** Single-faced card with the prototype ability. */
  case PROTOTYPE extends CardLayout("Prototype")
  /** Special split card with a "main" permanent face and an "adventure" non-permanent face that can later be cast as the permanent. */
  case ADVENTURE extends CardLayout("Adventure", 2, (f) => Set(SplitCard(f)))
  /** Special double-faced card that can be played as either side, but can't transform after. */
  case MODAL_DFC extends CardLayout("Modal DFC", 2, _ match { case Seq(front, back) => Set(ModalCard(front, back)) })
  /** Single-faced card, usually an enchantment, with multiple text boxes including an initial one, a solve condition, and a solution reward. */
  case CASE extends CardLayout("Case")
  /** Special double-faced card where both faces are the same, but one is full-art. Treated as a single-faced card. */
  case REVERSIBLE_CARD extends CardLayout("Reversible")
  /** Special single-faced card that can host an [[AUGMENT]]. */
  case HOST extends CardLayout("Host")
  /** Special single-faced card that can only be played attached to a [[HOST]]. */
  case AUGMENT extends CardLayout("Augment")

  /** An extra-large single-faced card for use in the Planechase format. */
  case PLANAR extends CardLayout("Planar")
  /** An extra-large single-faced card for use in the Archenemy format. */
  case SCHEME extends CardLayout("Scheme")
  /** An extra-large single-faced card for use in the Vanguard format. */
  case VANGUARD extends CardLayout("Vanguard")
}