package editor.gui.filter

import _root_.editor.database.attributes.CardAttribute
import _root_.editor.database.attributes.CombatStat
import _root_.editor.database.attributes.Expansion
import _root_.editor.database.attributes.HasMultiOptionsFilter
import _root_.editor.database.attributes.HasOptions
import _root_.editor.database.attributes.HasSingletonOptionsFilter
import _root_.editor.database.attributes.Loyalty
import _root_.editor.database.attributes.ManaCost
import _root_.editor.database.attributes.ManaType
import _root_.editor.database.attributes.Rarity
import _root_.editor.database.attributes.TypeLine
import _root_.editor.database.card.CardLayout
import _root_.editor.filter.leaf._
import _root_.editor.gui.filter.editor._

import scala.reflect.ClassTag

sealed trait FilterAttribute[T, F <: FilterLeaf] {
  def attribute: CardAttribute[T, F]
  def panel(selector: FilterSelectorPanel): FilterEditorPanel[F]

  override def toString = attribute.toString
}

sealed trait TextFilterAttribute { this: FilterAttribute[Seq[String], TextFilter] =>
  override def panel(selector: FilterSelectorPanel) = TextFilterPanel(attribute.filter, selector)
}

sealed trait NumberFilterAttribute { this: FilterAttribute[?, NumberFilter] =>
  override def panel(selector: FilterSelectorPanel) = NumberFilterPanel(attribute.filter, selector)
}

sealed trait ColorFilterAttribute { this: FilterAttribute[Set[ManaType], ColorFilter] =>
  override def panel(selector: FilterSelectorPanel) = ColorFilterPanel(attribute.filter, selector)
}

sealed trait SingletonOptionsFilterAttribute[T <: AnyRef : ClassTag] { this: FilterAttribute[T, SingletonOptionsFilter[T]] =>
  override def attribute: CardAttribute[T, SingletonOptionsFilter[T]] with HasSingletonOptionsFilter[T]
  override def panel(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait MultiOptionsFilterAttribute[T <: AnyRef : ClassTag, F <: MultiOptionsFilter[T] : ClassTag] { this: FilterAttribute[Set[T], F] =>
  override def attribute: CardAttribute[Set[T], F] with HasMultiOptionsFilter[T]
  override def panel(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

object FilterAttribute {
  case object NameFilter extends FilterAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.Name
  }

  case object RulesTextFilter extends FilterAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.RulesText
  }

  case object PrintedTextFilter extends FilterAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.FlavorText
  }

  case object ManaCostFilter extends FilterAttribute[Seq[ManaCost], ManaCostFilter] {
    override def attribute = CardAttribute.ManaCost
    override def panel(selector: FilterSelectorPanel) = ManaCostFilterPanel(selector)
  }

  case object RealManaValueFilter extends FilterAttribute[Double, NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.RealManaValue
  }

  case object EffManaValueFilter extends FilterAttribute[Seq[Double], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.EffManaValue
  }

  case object ColorsFilter extends FilterAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.Colors
  }

  case object ColorIdentityFilter extends FilterAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.ColorIdentity
  }

  case object TypeLineFilter extends FilterAttribute[Seq[TypeLine], _root_.editor.filter.leaf.TypeLineFilter] {
    override def attribute = CardAttribute.TypeLine
    override def panel(selector: FilterSelectorPanel) = TypeLineFilterPanel(selector)
  }

  case object PrintedTypesFilter extends FilterAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.PrintedTypes
  }

  case object CardTypeFilter extends FilterAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.CardType
  }

  case object SubtypeFilter extends FilterAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Subtype
  }

  case object SupertypeFilter extends FilterAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Supertype
  }

  case object PowerFilter extends FilterAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Power
  }

  case object ToughnessFilter extends FilterAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Toughness
  }

  case object LoyaltyFilter extends FilterAttribute[Seq[Loyalty], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Loyalty
  }

  case object LayoutFilter extends FilterAttribute[CardLayout, SingletonOptionsFilter[CardLayout]] with SingletonOptionsFilterAttribute[CardLayout] {
    override def attribute = CardAttribute.Layout
  }

  case object ExpansionFilter extends FilterAttribute[Expansion, SingletonOptionsFilter[Expansion]] with SingletonOptionsFilterAttribute[Expansion] {
    override def attribute = CardAttribute.Expansion
  }

  case object BlockFilter extends FilterAttribute[String, SingletonOptionsFilter[String]] with SingletonOptionsFilterAttribute[String] {
    override def attribute = CardAttribute.Block
  }

  case object RarityFilter extends FilterAttribute[Rarity, SingletonOptionsFilter[Rarity]] with SingletonOptionsFilterAttribute[Rarity] {
    override def attribute = CardAttribute.Rarity
  }

  case object ArtistFilter extends FilterAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.Artist
  }

  case object CardNumberFilter extends FilterAttribute[Seq[String], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.CardNumber
  }

  case object LegalInFilter extends FilterAttribute[Set[String], LegalityFilter] {
    override def attribute = CardAttribute.LegalIn
    override def panel(selector: FilterSelectorPanel) = LegalityFilterPanel(attribute.filter, selector)
  }

  case object TagsFilter extends FilterAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Tags
  }

  case object AnyCardFilter extends FilterAttribute[Unit, BinaryFilter] {
    override def attribute = CardAttribute.AnyCard
    override def panel(selector: FilterSelectorPanel) = BinaryFilterPanel(true)
  }

  case object NoCardFilter extends FilterAttribute[Unit, BinaryFilter] {
    override def attribute = CardAttribute.NoCard
    override def panel(selector: FilterSelectorPanel) = BinaryFilterPanel(false)
  }

  val values: IndexedSeq[FilterAttribute[?, ?]] = IndexedSeq(NameFilter, RulesTextFilter, PrintedTextFilter, ManaCostFilter, RealManaValueFilter, EffManaValueFilter, ColorsFilter, ColorIdentityFilter, TypeLineFilter, PrintedTypesFilter, CardTypeFilter, SubtypeFilter, SupertypeFilter, PowerFilter, ToughnessFilter, LoyaltyFilter, LayoutFilter, ExpansionFilter, BlockFilter, RarityFilter, ArtistFilter, CardNumberFilter, LegalInFilter, TagsFilter, AnyCardFilter, NoCardFilter)

  val fromAttribute = values.map((a) => a.attribute -> a).toMap
}