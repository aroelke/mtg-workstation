package editor

import scala.util.Random

/**
 * Miscellaneous utilities used throughout the editor.
 * @author Alec Roelke
 */
package object util {
  given Conversion[Random, RandomColors] = RandomColors(_)
}