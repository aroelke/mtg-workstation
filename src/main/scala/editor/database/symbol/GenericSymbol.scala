package editor.database.symbol

import scala.jdk.OptionConverters._

object GenericSymbol {
  val Max = 20

  val Contiguous = (0 to Max).map(new GenericSymbol(_)).toSeq

  val Hundred = new GenericSymbol(100)

  val Million = new GenericSymbol(1000000)

  val values = (Contiguous.zipWithIndex.map{ case (a, b) => a -> b } ++ Seq(100 -> Hundred, 1000000 -> Million)).toMap

  def apply(n: Int) = n match {
    case 1000000 => Million
    case 100 => Hundred
    case _ if n <= 20 => Contiguous(n)
    case _ => throw ArrayIndexOutOfBoundsException(n)
  }

  def parse(s: String) = try {
    Some(apply(s.toInt))
  } catch case _ @ (_: NumberFormatException | _: ArrayIndexOutOfBoundsException) => None

  @deprecated val HIGHEST_CONSECUTIVE = Max
  @deprecated val N = Contiguous.toArray
  @deprecated val HUNDRED = Hundred
  @deprecated val MILLION = Million
  @deprecated def get(n: Int) = apply(n)
  @deprecated def tryParseGenericSymbol(s: String) = parse(s).toJava
  @deprecated def parseGenericSymbol(s: String) = apply(s.toInt)
}

class GenericSymbol private(amount: Int) extends ManaSymbol(s"${amount}_mana.png", amount.toString, amount) {
  override def colorIntensity = ManaSymbol.createIntensity()

  override def compareTo(o: ManaSymbol) = o match {
    case g: GenericSymbol => value.toInt - o.value.toInt
    case _ => super.compareTo(o)
  }
}