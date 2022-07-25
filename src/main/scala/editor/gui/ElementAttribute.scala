package editor.gui

import _root_.editor.collection.Categorization
import _root_.editor.collection.mutable.Deck
import _root_.editor.database.attributes.CantCompare
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
import _root_.editor.database.card.Card
import _root_.editor.database.card.CardLayout
import _root_.editor.database.symbol.ManaSymbolInstances.ColorSymbol
import _root_.editor.filter.leaf._
import _root_.editor.gui.filter.FilterSelectorPanel
import _root_.editor.gui.filter.editor._
import _root_.editor.gui.generic.ComponentUtils
import _root_.editor.util.UnicodeSymbols

import java.awt.Color
import java.awt.Graphics
import java.time.LocalDate
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import scala.reflect.ClassTag

sealed trait ElementAttribute[T : ClassTag, F <: FilterLeaf] {
  def attribute: CardAttribute[T, F]
  def filter(selector: FilterSelectorPanel): FilterEditorPanel[F]
  def render(value: T): JComponent
  def tooltip(value: T): String

  final def getRendererComponent(value: AnyRef) = value match { case t: T => render(t) }
  final def getToolTipText(value: AnyRef) = value match { case t: T => s"<html>${tooltip(t)}</html>" }

  override def toString = attribute.toString
}

sealed trait TextElement { this: ElementAttribute[Seq[String], TextFilter] =>
  override def filter(selector: FilterSelectorPanel) = TextFilterPanel(attribute.filter, selector)
}

sealed trait NumberElement { this: ElementAttribute[?, NumberFilter] =>
  override def filter(selector: FilterSelectorPanel) = NumberFilterPanel(attribute.filter, selector)
}

sealed trait ColorElement { this: ElementAttribute[Set[ManaType], ColorFilter] =>
  override def filter(selector: FilterSelectorPanel) = ColorFilterPanel(attribute.filter, selector)
  override def render(value: Set[ManaType]) = {
    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
    ManaType.sorted(value).foreach((t) => panel.add(JLabel(ColorSymbol(t).scaled(ComponentUtils.TextSize))))
    panel
  }
  override def tooltip(value: Set[ManaType]) = ManaType.sorted(value).map((t) => {
    s"""<img src="${getClass.getResource(s"/images/icons/${ColorSymbol(t).name}")}" width="${ComponentUtils.TextSize}" height="${ComponentUtils.TextSize}"/>"""
  }).mkString
}

sealed trait SingletonOptionsElement[T <: AnyRef : ClassTag] { this: ElementAttribute[T, SingletonOptionsFilter[T]] =>
  override def attribute: CardAttribute[T, SingletonOptionsFilter[T]] with HasSingletonOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait MultiOptionsElement[T <: AnyRef : ClassTag, F <: MultiOptionsFilter[T] : ClassTag] { this: ElementAttribute[Set[T], F] =>
  override def attribute: CardAttribute[Set[T], F] with HasMultiOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait SimpleStringRenderer[T] { this: ElementAttribute[T, ?] =>
  override def render(value: T) = JLabel(value.toString)
  override def tooltip(value: T) = value.toString
}

sealed trait SimpleIterableRenderer[T, I <: Iterable[T]](toSeq: (I) => Seq[T] = (it: I) => it.toSeq, delim: String = Card.FaceSeparator) { this: ElementAttribute[I, ?] =>
  private def getString(value: I) = toSeq(value).mkString(delim)
  override def render(value: I) = JLabel(getString(value))
  override def tooltip(value: I) = getString(value)
}

sealed trait OptionIterableRenderer[T, I <: Iterable[Option[T]]](toSeq: (I) => Seq[Option[T]] = (it: I) => it.toSeq, delim: String = Card.FaceSeparator) { this: ElementAttribute[I, ?] =>
  private def getString(value: I) = if (value.flatten.isEmpty) "" else value.map(_.map(_.toString).getOrElse("")).mkString(Card.FaceSeparator)
  override def render(value: I) = JLabel(getString(value))
  override def tooltip(value: I) = getString(value)
}

sealed trait CantBeRendered[T] { this: ElementAttribute[T, ?] =>
  override def attribute: CardAttribute[T, ?] with CantCompare[T]
  override def render(value: T) = throw UnsupportedOperationException(s"$attribute can't be rendered")
  override def tooltip(value: T) = throw UnsupportedOperationException(s"$attribute can't be rendered")
}

object ElementAttribute {
  case object NameElement extends ElementAttribute[Seq[String], TextFilter] with TextElement with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.Name
  }

  case object RuleTextElement extends ElementAttribute[Seq[String], TextFilter] with TextElement with CantBeRendered[Seq[String]] {
    override def attribute = CardAttribute.RulesText
  }

  case object PrintedTextElement extends ElementAttribute[Seq[String], TextFilter] with TextElement with CantBeRendered[Seq[String]] {
    override def attribute = CardAttribute.FlavorText
  }

  case object ManaCostElement extends ElementAttribute[Seq[ManaCost], ManaCostFilter] {
    private val cache = collection.mutable.Map[Seq[ManaCost], Seq[Seq[Icon]]]()

    override def attribute = CardAttribute.ManaCost
    override def filter(selector: FilterSelectorPanel) = ManaCostFilterPanel(selector)
    override def render(value: Seq[ManaCost]) = {
      val icons = cache.getOrElseUpdate(value, value.map(_.map(_.getIcon(ComponentUtils.TextSize))))
      val panel = JPanel()
      panel.setLayout(BoxLayout(panel, BoxLayout.X_AXIS))
      for (i <- 0 until icons.size) {
        if (!icons(i).isEmpty) {
          if (i > 0)
            panel.add(JLabel(Card.FaceSeparator))
          icons(i).foreach((icon) => panel.add(JLabel(icon)))
        }
      }
      panel
    }
    override def tooltip(value: Seq[ManaCost]) = {
      value.map(_.map((s) => s"""<img src="${getClass.getResource(s"/images/icons/${s.name}")}" width="${ComponentUtils.TextSize}" height="${ComponentUtils.TextSize}"/>""").mkString).mkString(Card.FaceSeparator)
    }
  }

  case object RealManaValueElement extends ElementAttribute[Double, NumberFilter] with NumberElement {
    private[ElementAttribute] def getString(value: Double) = if (value == value.toInt) value.toInt.toString else value.toString
    override def attribute = CardAttribute.RealManaValue
    override def render(value: Double) = JLabel(getString(value))
    override def tooltip(value: Double) = getString(value)
  }

  case object EffManaValueElement extends ElementAttribute[Seq[Double], NumberFilter] with NumberElement {
    override def attribute = CardAttribute.EffManaValue
    override def render(value: Seq[Double]) = JLabel(value.map(RealManaValueElement.getString).mkString(Card.FaceSeparator))
    override def tooltip(value: Seq[Double]) = value.map(RealManaValueElement.getString).mkString(Card.FaceSeparator)
  }

  case object ColorsElement extends ElementAttribute[Set[ManaType], ColorFilter] with ColorElement {
    override def attribute = CardAttribute.Colors
  }

  case object ColorIdentityElement extends ElementAttribute[Set[ManaType], ColorFilter] with ColorElement {
    override def attribute = CardAttribute.ColorIdentity
  }

  case object TypeLineElement extends ElementAttribute[Seq[TypeLine], TypeLineFilter] with SimpleIterableRenderer[TypeLine, Seq[TypeLine]]() {
    override def attribute = CardAttribute.TypeLine
    override def filter(selector: FilterSelectorPanel) = TypeLineFilterPanel(selector)
  }

  case object PrintedTypesElement extends ElementAttribute[Seq[String], TextFilter] with TextElement with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.PrintedTypes
  }

  case object CardTypeElement extends ElementAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsElement[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.CardType
  }

  case object SubtypeElement extends ElementAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsElement[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Subtype
  }

  case object SupertypeElement extends ElementAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsElement[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Supertype
  }

  case object PowerElement extends ElementAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberElement with OptionIterableRenderer[CombatStat, Seq[Option[CombatStat]]]() {
    override def attribute = CardAttribute.Power
  }

  case object ToughnessElement extends ElementAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberElement with OptionIterableRenderer[CombatStat, Seq[Option[CombatStat]]]() {
    override def attribute = CardAttribute.Toughness
  }

  case object LoyaltyElement extends ElementAttribute[Seq[Option[Loyalty]], NumberFilter] with NumberElement with OptionIterableRenderer[Loyalty, Seq[Option[Loyalty]]]() {
    override def attribute = CardAttribute.Loyalty
  }

  case object LayoutElement extends ElementAttribute[CardLayout, SingletonOptionsFilter[CardLayout]] with SingletonOptionsElement[CardLayout] with SimpleStringRenderer[CardLayout] {
    override def attribute = CardAttribute.Layout
  }

  case object ExpansionElement extends ElementAttribute[Expansion, SingletonOptionsFilter[Expansion]] with SingletonOptionsElement[Expansion] with SimpleStringRenderer[Expansion] {
    override def attribute = CardAttribute.Expansion
  }

  case object BlockElement extends ElementAttribute[String, SingletonOptionsFilter[String]] with SingletonOptionsElement[String] with SimpleStringRenderer[String] {
    override def attribute = CardAttribute.Block
  }

  case object RarityElement extends ElementAttribute[Rarity, SingletonOptionsFilter[Rarity]] with SingletonOptionsElement[Rarity] with SimpleStringRenderer[Rarity] {
    override def attribute = CardAttribute.Rarity
  }

  case object ArtistElement extends ElementAttribute[Seq[String], TextFilter] with TextElement {
    override def attribute = CardAttribute.Artist
    override def render(value: Seq[String]) = JLabel(value(0)) // assume artist of all faces is the same
    override def tooltip(value: Seq[String]) = value(0)
  }

  case object CardNumberElement extends ElementAttribute[Seq[String], NumberFilter] with NumberElement with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.CardNumber
  }

  case object LegalInElement extends ElementAttribute[Set[String], LegalityFilter] with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.LegalIn
    override def filter(selector: FilterSelectorPanel) = LegalityFilterPanel(attribute.filter, selector)
  }

  case object TagsElement extends ElementAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsElement[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Tags
  }

  case object AnyCardElement extends ElementAttribute[Unit, BinaryFilter] with CantBeRendered[Unit] {
    override def attribute = CardAttribute.AnyCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(true)
  }

  case object NoCardElement extends ElementAttribute[Unit, BinaryFilter] with CantBeRendered[Unit] {
    override def attribute = CardAttribute.NoCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(false)
  }

  case object CategoriesElement extends ElementAttribute[Set[Categorization], Nothing] {
    override def attribute = CardAttribute.Categories
    override def filter(selector: FilterSelectorPanel) = throw UnsupportedOperationException("can't filter by category")
    override def render(value: Set[Categorization]) = {
      val categories = value.toSeq.sortBy(_.name)
      val panel = new JPanel {
        override def paintComponent(g: Graphics) = {
          super.paintComponent(g)
          for (i <- 0 until categories.size) {
            val x = i*(getHeight + 1) + 1
            val y = 1
            g.setColor(categories(i).color)
            g.fillRect(x, y, getHeight - 3, getHeight - 3)
            g.setColor(Color.BLACK)
            g.drawRect(x, y, getHeight - 3, getHeight - 3)
          }
        }
      }
      panel
    }
    override def tooltip(value: Set[Categorization]) = s"""Categories:${value.map((c) => s"<br>${UnicodeSymbols.Bullet} ${c.name}").toSeq.sorted.mkString}"""
  }

  case object CountElement extends ElementAttribute[Int, Nothing] with SimpleStringRenderer[Int] {
    override def attribute = CardAttribute.Count
    override def filter(selector: FilterSelectorPanel) = throw UnsupportedOperationException("can't filter by count")
  }

  case object DateAddedElement extends ElementAttribute[LocalDate, Nothing] {
    override def attribute = CardAttribute.DateAdded
    override def filter(selector: FilterSelectorPanel) = throw UnsupportedOperationException("can't filter by date")
    override def render(value: LocalDate) = JLabel(Deck.DateFormatter.format(value))
    override def tooltip(value: LocalDate) = Deck.DateFormatter.format(value)
  }

  val values: IndexedSeq[ElementAttribute[?, ?]] = IndexedSeq(NameElement, RuleTextElement, PrintedTextElement, ManaCostElement, RealManaValueElement, EffManaValueElement, ColorsElement, ColorIdentityElement, TypeLineElement, PrintedTypesElement, CardTypeElement, SubtypeElement, SupertypeElement, PowerElement, ToughnessElement, LoyaltyElement, LayoutElement, ExpansionElement, BlockElement, RarityElement, ArtistElement, CardNumberElement, LegalInElement, TagsElement, AnyCardElement, NoCardElement, CategoriesElement, CountElement, DateAddedElement)

  val fromAttribute = values.map((a) => a.attribute -> a).toMap
}