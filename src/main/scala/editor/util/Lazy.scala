package editor.util

/**
 * Deprecated class for enabling lazy evaluation of code in Java, which is replaced by the "lazy" keyword in Scala.
 * 
 * @constructor create a new lazy evaluator
 * @param supplier code to lazily evaluate
 *
 * @author Alec Roelke
 */
@deprecated class Lazy[T](supplier: java.util.function.Supplier[T]) extends java.util.function.Supplier[T] {
  override lazy val get = supplier.get
}
