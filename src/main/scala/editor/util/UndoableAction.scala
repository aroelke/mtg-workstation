package editor.util

/**
 * Convenience object for creating [[UndoableAction]]s.
 * @author Alec Roelke
 */
object UndoableAction {
  /**
   * Create a new undoable action using given forward and reverse actions.
   * 
   * @param forward action to perform when doing or redoing
   * @param reverse action to perform when undoing
   * @return an [[UndoableAction]] representing the action
   */
  def apply[R, U](forward: () => R, reverse: () => U) = new UndoableAction[R, U] {
    def redo() = forward()
    def undo() = reverse()
  }
}

/**
 * An action that can be undone.
 * 
 * @tparam R return type of the action
 * @tparam U return type of the undoing action
 * 
 * @author Alec Roelke
 */
trait UndoableAction[R, U] {
  /**
   * Perform the action, or redo it if it was undone.
   * @return a result related to the action
   */
  def redo(): R

  /**
   * Undo the action.
   * @return a result related to undoing the action
   */
  def undo(): U
}