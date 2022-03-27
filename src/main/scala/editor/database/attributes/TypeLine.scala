package editor.database.attributes

import scala.collection.immutable.ListSet
import scala.collection.AbstractIterable
import editor.util.UnicodeSymbols

final case class TypeLine(types: ListSet[String], subtypes: ListSet[String] = ListSet.empty, supertypes: ListSet[String] = ListSet.empty) extends AbstractIterable[String] {
  if (types.isEmpty)
    throw IllegalArgumentException("a card must have at least one type")

  def contains(s: String) = types.contains(s) || subtypes.contains(s) || supertypes.contains(s)
  
  def concat(tl: TypeLine) = TypeLine(types ++ tl.types, subtypes ++ tl.subtypes, supertypes ++ tl.supertypes)

  def ++(tl: TypeLine) = concat(tl)

  override def iterator = (supertypes ++ types ++ subtypes).iterator

  override lazy val toString = s"""${if (supertypes.isEmpty) "" else supertypes.mkString("", " ", " ")}${types.mkString(" ")}${if (subtypes.isEmpty) "" else subtypes.mkString(s" ${UnicodeSymbols.EM_DASH} ", " ", "")}"""
}