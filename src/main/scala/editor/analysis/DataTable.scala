package editor.analysis

case class DataTable[E, R, C](data: IndexedSeq[IndexedSeq[E]], rowLabels: IndexedSeq[R], columnLabels: IndexedSeq[C]) {
  require(data.size == rowLabels.size)
  data.foreach((c) => require(c.size == columnLabels.size))

  private lazy val rowIndices = rowLabels.zipWithIndex.toMap
  private lazy val colIndices = columnLabels.zipWithIndex.toMap

  def rows = rowLabels.size
  def columns = columnLabels.size

  def get(row: R) = rowIndices.get(row).map(data)
  def get(row: R, col: C) = (rowIndices.get(row), colIndices.get(col)) match {
    case (Some(r), Some(c)) => Some(data(r)(c))
    case _ => None
  }

  def apply(index: Int) = data(index)
  def apply(row: R) = data(rowIndices(row))
  def apply(row: R, col: C) = data(rowIndices(row))(colIndices(col))

  def map[B, R2, C2](f: (E) => B, r: IndexedSeq[R2] = rowLabels, c: IndexedSeq[C2] = columnLabels) = DataTable(data.map(_.map(f)), r, c)
  def foreach[U](f: (E) => U) = data.foreach(_.foreach(f))
  def zip[B, R2, C2](that: IterableOnce[IterableOnce[B]], r: IndexedSeq[R2] = rowLabels, c: IndexedSeq[C2] = columnLabels) = DataTable((data zip that).map{ case (thisrow, thatrow) => thisrow zip thatrow }, r, c)

  def transpose = DataTable(data.transpose, columnLabels, rowLabels)
}