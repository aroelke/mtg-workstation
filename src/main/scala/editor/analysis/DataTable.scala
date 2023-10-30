package editor.analysis

import scala.annotation.targetName
import scala.collection.immutable.ListMap

/**
 * Helper object for creating [[DataTable]]s.
 * @author Alec Roelke
 */
object DataTable {
  /**
   * Convenience constructor for creating a [[DataTable]].
   * 
   * @param data data in the table
   * @param rowLabels table row labels
   * @param columnLabels table column labels
   * @tparam E type of data in the table
   * @tparam R type of data for labeling rows
   * @tparam C type of data for labeling columns
   * @return a new [[DataTable]] with the given data, row labels, and column labels
   */
  def apply[E, R, C](data: IndexedSeq[IndexedSeq[E]], rowLabels: IndexedSeq[R], columnLabels: IndexedSeq[C]): DataTable[E, R, C] = new DataTable(data, rowLabels, columnLabels)

  /**
   * Create a [[DataTable]] using a more user-friendly mapping of row labels onto data rows.
   * 
   * @param labeled mapping of row labels onto data rows
   * @param columnLabels table column labels
   * @tparam E type of data in the table
   * @tparam R type of data for labeling rows
   * @tparam C type of data for labeling columns
   * @return a new [[DataTable]] with the given data, using the map keys as row labels, and the given column labels
   */
  def apply[E, R, C](labeled: ListMap[R, IndexedSeq[E]], columnLabels: IndexedSeq[C]): DataTable[E, R, C] = DataTable(
    labeled.map{ case (_, row) => row }.toIndexedSeq,
    labeled.map{ case (label, _) => label }.toIndexedSeq,
    columnLabels
  )

  /** @return an empty [[DataTable]] with the given data and label types. */
  def empty[E, R, C] = DataTable(IndexedSeq.empty[IndexedSeq[E]], IndexedSeq.empty[R], IndexedSeq.empty[C])
}

/**
 * Two-dimensional array of data with row and column labels.
 * 
 * @constructor create a new table of labeled data
 * @param data data in the table
 * @param rowLabels row header
 * @param columnLabels column header
 * 
 * @tparam E type of data in the table
 * @tparam R type of data in the row header
 * @tparam C type of data in the column header
 * 
 * @note an instance of this class only does dimension operations on rows; to operate on columns, transpose it first
 */
class DataTable[E, R, C](data: IndexedSeq[IndexedSeq[E]], val rowLabels: IndexedSeq[R], val columnLabels: IndexedSeq[C]) extends IndexedSeq[IndexedSeq[E]] {
  require(data.size == rowLabels.size)
  data.foreach((c) => require(c.size == columnLabels.size))

  private lazy val rowIndices = rowLabels.zipWithIndex.toMap
  private lazy val colIndices = columnLabels.zipWithIndex.toMap

  /** @return the number of rows in the table */
  def rows = rowLabels.size
  /** @return the number of columns in the table */
  def columns = columnLabels.size

  /**
   * Get a row of data with a specified label, if it exists.
   * 
   * @param row label of the row to get
   * @return the data in the row with the label, or None if there isn't a row with that label
   */
  def get(row: R) = rowIndices.get(row).map(data)

  /**
   * Get a table element with specified row and column labels.
   * 
   * @param row label of the row
   * @param col label of the column
   * @return the element with the corresponding row and column label, or None if there isn't one (i.e. either the row or column doesn't exist)
   */
  def get(row: R)(col: C) = (rowIndices.get(row), colIndices.get(col)) match {
    case (Some(r), Some(c)) => Some(data(r)(c))
    case _ => None
  }

  /**
   * Get a row by label.
   * 
   * @param row label of the row to get
   * @return the data in the row with the given label
   */
  def apply(row: R) = data(rowIndices(row))

  /**
   * Get an element by row and column label.
   * 
   * @param row label of the row containing the element
   * @param col label of the column containing the element
   * @return the element in the row and column corresponding to the labels
   */
  def apply(row: R)(col: C) = data(rowIndices(row))(colIndices(col))

  override def filter(pred: (IndexedSeq[E]) => Boolean): DataTable[E, R, C] = {
    val allowed = data.zipWithIndex.collect{ case (d, i) if pred(d) => i }
    if (allowed.isEmpty) DataTable.empty else DataTable(allowed.map(data), allowed.map(rowLabels), columnLabels)
  }

  /**
   * Create a new table whose data is the result of an operation on this table's data.
   * 
   * @param f function mapping the data in this table to another type of data
   * @param r new row labels for the data
   * @param c new column labels for the data
   * @return a new [[DataTable]] with the result of the computation and new labels
   */
  def map[B, R2, C2](f: (E) => B, r: IndexedSeq[R2] = rowLabels, c: IndexedSeq[C2] = columnLabels) = DataTable(data.map(_.map(f)), r, c)

  /**
   * Perform an operation on each element in the table.
   * @param f operation to perform
   */
  @targetName("element_foreach") def foreach[U](f: (E) => U) = data.foreach(_.foreach(f))

  /**
   * Create a new table whose elements are comprised of the data in this table concatenated with the elements of the given iterable.
   * 
   * @param that two-dimensional iterable containing data to concatenate
   * @param r new row labels for the data
   * @param c new column labels for the data
   * @return a new [[DataTable]] with the result of the concatenation and new labels
   */
  def zip[B, R2, C2](that: IterableOnce[IterableOnce[B]], r: IndexedSeq[R2] = rowLabels, c: IndexedSeq[C2] = columnLabels) = DataTable((data zip that).map{ case (thisrow, thatrow) => thisrow zip thatrow }, r, c)

  def transpose = DataTable(data.transpose, columnLabels, rowLabels)

  override def apply(index: Int) = data(index)
  override def iterator = data.iterator
  override def length = data.size
}