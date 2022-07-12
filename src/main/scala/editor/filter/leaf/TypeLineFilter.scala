package editor.filter.leaf

import editor.database.attributes.CardAttribute
import editor.database.card.Card
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * Filter that groups cards by all of their types.
 * 
 * @constructor create a new type line filter
 * @param contain function to use to compare with the card's type line
 * @param line string containing types to search for
 * 
 * @author Alec Roelke
 */
final case class TypeLineFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, line: String = "") extends FilterLeaf[TypeLineFilter] {
  override def attribute = CardAttribute.TypeLine
  override val unified = false
  override protected def testFace(c: Card) = !line.isEmpty && contain(c.typeLine.toSet.map(_.toLowerCase), line.toLowerCase.split("\\s").toSeq)
  override def copyFaces(faces: FaceSearchOptions) = copy(faces = faces)
}