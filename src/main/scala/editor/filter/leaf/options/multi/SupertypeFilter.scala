package editor.filter.leaf.options.multi

import editor.database.attributes.CardAttribute
import editor.filter.FaceSearchOptions
import editor.util.Containment

/**
 * Object containing global information about supertypes.
 * @author Alec Roelke
 */
object SupertypeFilter {
  /** List of all supertypes. */
  var supertypeList = Seq[String]()
}

/**
 * Filter that groups cards by supertype.
 * @author Alec Roelke
 */
final case class SupertypeFilter(faces: FaceSearchOptions = FaceSearchOptions.ANY, contain: Containment = Containment.AnyOf, selected: Set[String] = Set.empty) extends MultiOptionsFilter[String, SupertypeFilter] {
  override def attribute = CardAttribute.Supertype
  override val unified = false
  override val values = _.supertypes
  override def copyFaces(faces: FaceSearchOptions) = copy(faces = faces)
}