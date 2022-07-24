package editor.gui

import _root_.editor.collection.Categorization
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
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import scala.reflect.ClassTag
import java.time.LocalDate
import _root_.editor.collection.mutable.Deck

sealed trait GuiAttribute[T : ClassTag, F <: FilterLeaf] {
  def attribute: CardAttribute[T, F]
  def filter(selector: FilterSelectorPanel): FilterEditorPanel[F]
  def render(value: T): JComponent
  def tooltip(value: T): String

  final def getRendererComponent(value: AnyRef) = value match { case t: T => render(t) }
  final def getToolTipText(value: AnyRef) = value match { case t: T => s"<html>${tooltip(t)}</html>" }

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

sealed trait SingletonOptionsFilterAttribute[T <: AnyRef : ClassTag] { this: GuiAttribute[T, SingletonOptionsFilter[T]] =>
  override def attribute: CardAttribute[T, SingletonOptionsFilter[T]] with HasSingletonOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait MultiOptionsFilterAttribute[T <: AnyRef : ClassTag, F <: MultiOptionsFilter[T] : ClassTag] { this: GuiAttribute[Set[T], F] =>
  override def attribute: CardAttribute[Set[T], F] with HasMultiOptionsFilter[T]
  override def filter(selector: FilterSelectorPanel) = OptionsFilterPanel(attribute.filter, attribute.options, selector)
}

sealed trait SimpleStringRenderer[T] { this: GuiAttribute[T, ?] =>
  override def render(value: T) = JLabel(value.toString)
  override def tooltip(value: T) = value.toString
}

sealed trait SimpleIterableRenderer[T, I <: Iterable[T]](toSeq: (I) => Seq[T] = (it: I) => it.toSeq, delim: String = Card.FaceSeparator) { this: GuiAttribute[I, ?] =>
  private def getString(value: I) = toSeq(value).mkString(delim)
  override def render(value: I) = JLabel(getString(value))
  override def tooltip(value: I) = getString(value)
}

sealed trait OptionIterableRenderer[T, I <: Iterable[Option[T]]](toSeq: (I) => Seq[Option[T]] = (it: I) => it.toSeq, delim: String = Card.FaceSeparator) { this: GuiAttribute[I, ?] =>
  private def getString(value: I) = if (value.flatten.isEmpty) "" else value.map(_.map(_.toString).getOrElse("")).mkString(Card.FaceSeparator)
  override def render(value: I) = JLabel(getString(value))
  override def tooltip(value: I) = getString(value)
}

sealed trait CantBeRendered[T] { this: GuiAttribute[T, ?] =>
  override def attribute: CardAttribute[T, ?] with CantCompare[T]
  override def render(value: T) = throw UnsupportedOperationException(s"$attribute can't be rendered")
  override def tooltip(value: T) = throw UnsupportedOperationException(s"$attribute can't be rendered")
}

object GuiAttribute {
  case object NameFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.Name
  }

  case object RulesTextFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute with CantBeRendered[Seq[String]] {
    override def attribute = CardAttribute.RulesText
  }

  case object PrintedTextFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute with CantBeRendered[Seq[String]] {
    override def attribute = CardAttribute.FlavorText
  }

  case object ManaCostFilter extends GuiAttribute[Seq[ManaCost], ManaCostFilter] {
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

  case object RealManaValueFilter extends GuiAttribute[Double, NumberFilter] with NumberFilterAttribute {
    private[GuiAttribute] def getString(value: Double) = if (value == value.toInt) value.toInt.toString else value.toString
    override def attribute = CardAttribute.RealManaValue
    override def render(value: Double) = JLabel(getString(value))
    override def tooltip(value: Double) = getString(value)
  }

  case object EffManaValueFilter extends GuiAttribute[Seq[Double], NumberFilter] with NumberFilterAttribute {
    override def attribute = CardAttribute.EffManaValue
    override def render(value: Seq[Double]) = JLabel(value.map(RealManaValueFilter.getString).mkString(Card.FaceSeparator))
    override def tooltip(value: Seq[Double]) = value.map(RealManaValueFilter.getString).mkString(Card.FaceSeparator)
  }

  case object ColorsFilter extends GuiAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.Colors
  }

  case object ColorIdentityFilter extends GuiAttribute[Set[ManaType], ColorFilter] with ColorFilterAttribute {
    override def attribute = CardAttribute.ColorIdentity
  }

  case object TypeLineFilter extends GuiAttribute[Seq[TypeLine], _root_.editor.filter.leaf.TypeLineFilter] with SimpleIterableRenderer[TypeLine, Seq[TypeLine]]() {
    override def attribute = CardAttribute.TypeLine
    override def filter(selector: FilterSelectorPanel) = TypeLineFilterPanel(selector)
  }

  case object PrintedTypesFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.PrintedTypes
  }

  case object CardTypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.CardType
  }

  case object SubtypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Subtype
  }

  case object SupertypeFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Supertype
  }

  case object PowerFilter extends GuiAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute with OptionIterableRenderer[CombatStat, Seq[Option[CombatStat]]]() {
    override def attribute = CardAttribute.Power
  }

  case object ToughnessFilter extends GuiAttribute[Seq[Option[CombatStat]], NumberFilter] with NumberFilterAttribute with OptionIterableRenderer[CombatStat, Seq[Option[CombatStat]]]() {
    override def attribute = CardAttribute.Toughness
  }

  case object LoyaltyFilter extends GuiAttribute[Seq[Option[Loyalty]], NumberFilter] with NumberFilterAttribute with OptionIterableRenderer[Loyalty, Seq[Option[Loyalty]]]() {
    override def attribute = CardAttribute.Loyalty
  }

  case object LayoutFilter extends GuiAttribute[CardLayout, SingletonOptionsFilter[CardLayout]] with SingletonOptionsFilterAttribute[CardLayout] with SimpleStringRenderer[CardLayout] {
    override def attribute = CardAttribute.Layout
  }

  case object ExpansionFilter extends GuiAttribute[Expansion, SingletonOptionsFilter[Expansion]] with SingletonOptionsFilterAttribute[Expansion] with SimpleStringRenderer[Expansion] {
    override def attribute = CardAttribute.Expansion
  }

  case object BlockFilter extends GuiAttribute[String, SingletonOptionsFilter[String]] with SingletonOptionsFilterAttribute[String] with SimpleStringRenderer[String] {
    override def attribute = CardAttribute.Block
  }

  case object RarityFilter extends GuiAttribute[Rarity, SingletonOptionsFilter[Rarity]] with SingletonOptionsFilterAttribute[Rarity] with SimpleStringRenderer[Rarity] {
    override def attribute = CardAttribute.Rarity
  }

  case object ArtistFilter extends GuiAttribute[Seq[String], TextFilter] with TextFilterAttribute {
    override def attribute = CardAttribute.Artist
    override def render(value: Seq[String]) = JLabel(value(0)) // assume artist of all faces is the same
    override def tooltip(value: Seq[String]) = value(0)
  }

  case object CardNumberFilter extends GuiAttribute[Seq[String], NumberFilter] with NumberFilterAttribute with SimpleIterableRenderer[String, Seq[String]]() {
    override def attribute = CardAttribute.CardNumber
  }

  case object LegalInFilter extends GuiAttribute[Set[String], LegalityFilter] with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.LegalIn
    override def filter(selector: FilterSelectorPanel) = LegalityFilterPanel(attribute.filter, selector)
  }

  case object TagsFilter extends GuiAttribute[Set[String], MultiOptionsFilter[String]]
      with MultiOptionsFilterAttribute[String, MultiOptionsFilter[String]]
      with SimpleIterableRenderer[String, Set[String]](_.toSeq.sorted, ", ") {
    override def attribute = CardAttribute.Tags
  }

  case object AnyCardFilter extends GuiAttribute[Unit, BinaryFilter] with CantBeRendered[Unit] {
    override def attribute = CardAttribute.AnyCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(true)
  }

  case object NoCardFilter extends GuiAttribute[Unit, BinaryFilter] with CantBeRendered[Unit] {
    override def attribute = CardAttribute.NoCard
    override def filter(selector: FilterSelectorPanel) = BinaryFilterPanel(false)
  }

  case object CategoriesFilter extends GuiAttribute[Set[Categorization], Nothing] {
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
    override def tooltip(value: Set[Categorization]) = value.map(_.name).toSeq.sorted.mkString(
      s"Categories:<br>${UnicodeSymbols.Bullet} ",
      s"<br>${UnicodeSymbols.Bullet} ",
      ""
    )
  }

  case object CountFilter extends GuiAttribute[Int, Nothing] with SimpleStringRenderer[Int] {
    override def attribute = CardAttribute.Count
    override def filter(selector: FilterSelectorPanel) = throw UnsupportedOperationException("can't filter by count")
  }

  case object DateAddedFilter extends GuiAttribute[LocalDate, Nothing] {
    override def attribute = CardAttribute.DateAdded
    override def filter(selector: FilterSelectorPanel) = throw UnsupportedOperationException("can't filter by date")
    override def render(value: LocalDate) = JLabel(Deck.DateFormatter.format(value))
    override def tooltip(value: LocalDate) = Deck.DateFormatter.format(value)
  }

  val values: IndexedSeq[GuiAttribute[?, ?]] = IndexedSeq(NameFilter, RulesTextFilter, PrintedTextFilter, ManaCostFilter, RealManaValueFilter, EffManaValueFilter, ColorsFilter, ColorIdentityFilter, TypeLineFilter, PrintedTypesFilter, CardTypeFilter, SubtypeFilter, SupertypeFilter, PowerFilter, ToughnessFilter, LoyaltyFilter, LayoutFilter, ExpansionFilter, BlockFilter, RarityFilter, ArtistFilter, CardNumberFilter, LegalInFilter, TagsFilter, AnyCardFilter, NoCardFilter, CategoriesFilter, CountFilter, DateAddedFilter)

  val fromAttribute = values.map((a) => a.attribute -> a).toMap
}