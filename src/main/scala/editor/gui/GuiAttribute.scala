package editor.gui

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
import _root_.editor.gui.filter.FilterSelectorPanel
import _root_.editor.gui.filter.editor._

import scala.reflect.ClassTag

sealed trait GuiAttribute[T, F <: FilterLeaf] {
  def attribute: CardAttribute[T, F]
  def filter(selector: FilterSelectorPanel): FilterEditorPanel[F]

  override def toString = attribute.toString
}

sealed trait TextFilterAttribute { this: GuiAttribute[Seq[String], TextFilter] =>
  override def filter(selector: FilterSelectorPanel) = TextFilterPanel(attribute.filter, selector)
}

sealed trait NumberFilterAttribute { this: GuiAttribute[?, NumberFilter] =>
  override def filter(selector: FilterSelectorPanel) = NumberFilterPanel(attribute.filter, selector)
}

sealed trait ColorFilterAttribute { this: GuiAttribute[Set[ManaType], ColorFilter] =>
  override def filter(selector: FilterSelectorPanel) = ColorFilterPanel(attribute.filter, selector)
}

sealed trait SingletonOptionsFilterAttribute[T <: AnyRef : ClassTag] { this: GuiAttribute[T, SingletonOptionsFilter[T]] =>
  override def attribute: CardAttribute[T, SingletonOptionsFilter[T]] with HasSingletonOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait MultiOptionsFilterAttribute[T <: AnyRef : ClassTag, F <: MultiOptionsFilter[T] : ClassTag] { this: GuiAttribute[Set[T], F] =>
  override def attribute: CardAttribute[Set[T], F] with HasMultiOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

object GuiAttribute {
  case object NameFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.Name
  }

  case object RulesTextFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.RulesText
  }

  case object PrintedTextFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.FlavorText
  }

  case object ManaCostFilter extends GuiAttribute[Seq[ManaCost], ManaCostFilter] {
    override def attribute = CardAttribute.ManaCost
    override def filter(selector: FilterSelectorPanel) = ManaCostFilterPanel(selector)
  }

  case object RealManaValueFilter extends GuiAttribute[Double, NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.RealManaValue
  }

  case object EffManaValueFilter extends GuiAttribute[Seq[Double], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.EffManaValue
  }

  case object ColorsFilter extends GuiAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.Colors
  }

  case object ColorIdentityFilter extends GuiAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.ColorIdentity
  }

  case object TypeLineFilter extends GuiAttribute[Seq[TypeLine], _root_.editor.filter.leaf.TypeLineFilter] {
    override def attribute = CardAttribute.TypeLine
    override def filter(selector: FilterSelectorPanel) = TypeLineFilterPanel(selector)
  }

  case object PrintedTypesFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.PrintedTypes
  }

  case object CardTypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.CardType
  }

  case object SubtypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Subtype
  }

  case object SupertypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Supertype
  }

  case object PowerFilter extends GuiAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Power
  }

  case object ToughnessFilter extends GuiAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Toughness
  }

  case object LoyaltyFilter extends GuiAttribute[Seq[Option[Loyalty]], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.Loyalty
  }

  case object LayoutFilter extends GuiAttribute[CardLayout, SingletonOptionsFilter[CardLayout]] with SingletonOptionsFilterAttribute[CardLayout] {
    override def attribute = CardAttribute.Layout
  }

  case object ExpansionFilter extends GuiAttribute[Expansion, SingletonOptionsFilter[Expansion]] with SingletonOptionsFilterAttribute[Expansion] {
    override def attribute = CardAttribute.Expansion
  }

  case object BlockFilter extends GuiAttribute[String, SingletonOptionsFilter[String]] with SingletonOptionsFilterAttribute[String] {
    override def attribute = CardAttribute.Block
  }

  case object RarityFilter extends GuiAttribute[Rarity, SingletonOptionsFilter[Rarity]] with SingletonOptionsFilterAttribute[Rarity] {
    override def attribute = CardAttribute.Rarity
  }

  case object ArtistFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.Artist
  }

  case object CardNumberFilter extends GuiAttribute[Seq[String], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.CardNumber
  }

  case object LegalInFilter extends GuiAttribute[Set[String], LegalityFilter] {
    override def attribute = CardAttribute.LegalIn
    override def filter(selector: FilterSelectorPanel) = LegalityFilterPanel(attribute.filter, selector)
  }

  case object TagsFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]] with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]] {
    override def attribute = CardAttribute.Tags
  }

  case object AnyCardFilter extends GuiAttribute[Unit, BinaryFilter] {
    override def attribute = CardAttribute.AnyCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(true)
  }

  case object NoCardFilter extends GuiAttribute[Unit, BinaryFilter] {
    override def attribute = CardAttribute.NoCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(false)
  }

  val values: IndexedSeq[GuiAttribute[?, ?]] = IndexedSeq(NameFilter, RulesTextFilter, PrintedTextFilter, ManaCostFilter, RealManaValueFilter, EffManaValueFilter, ColorsFilter, ColorIdentityFilter, TypeLineFilter, PrintedTypesFilter, CardTypeFilter, SubtypeFilter, SupertypeFilter, PowerFilter, ToughnessFilter, LoyaltyFilter, LayoutFilter, ExpansionFilter, BlockFilter, RarityFilter, ArtistFilter, CardNumberFilter, LegalInFilter, TagsFilter, AnyCardFilter, NoCardFilter)

  val fromAttribute = values.map((a) => a.attribute -> a).toMap
}