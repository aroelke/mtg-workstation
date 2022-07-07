package editor.database.card

import scala.jdk.CollectionConverters._

import CardLayout._

/**
 * A [[Card]] with all faces printed on the front, with any one castable at a time (with some exceptions).
 * The mana value of such a card is the sum of its faces.
 * 
 * @constructor create a new split card
 * @param faces individual mini cards on the front of the split card
 * @author Alec Roelke
 */
@throws[IllegalArgumentException]("if any face isn't a type of split card or all faces are not the same type")
class SplitCard(faces: Seq[Card]) extends MultiCard(faces(0).layout, faces.toIndexedSeq) {
  if (!faces.forall((f) => Seq(SPLIT, AFTERMATH, ADVENTURE).contains(f.layout)))
    throw IllegalArgumentException("can't create split cards out of non-split cards")
  if (!faces.forall((f) => f.layout == faces(0).layout))
    throw IllegalArgumentException("all faces of a split card must be of the same type")
  
  override lazy val manaValue = faces.map(_.manaValue).sum
  override def imageNames = faces(0).imageNames
  override def multiverseid = faces(0).multiverseid
  override def scryfallid = faces(0).scryfallid
}