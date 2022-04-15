package editor.filter

import java.awt.Image
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.ImageIcon

/**
 * All the ways a filter can be applied to the faces of a multi-faced card. Has no effect on single-
 * faced cards.
 * 
 * @constructor create a new way to apply a filter to a card's faces
 * @param tooltip hint on what the symbol used to represent the option means
 * 
 * @author Alec Roelke
 */
enum FaceSearchOptions(val tooltip: String) {
  /** Icon to display to indicate the current way to filter faces for a filter. */
  lazy val icon = try {
    ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/faces/${toString.toLowerCase}.png")))
  } catch case e: IOException => {
    e.printStackTrace
    ImageIcon()
  }

  /**
   * Get a resized version of the icon for a filtering option.
   * 
   * @param width new side size of the icon
   * @return a scaled instance of the option's icon
   */
  def scaled(width: Int) = ImageIcon(icon.getImage.getScaledInstance(width, -1, Image.SCALE_SMOOTH))

  /** A card passes the filter if any of its faces pass. */
  case ANY   extends FaceSearchOptions("Can match any face")
  /** A card only passes the filter if all of its faces pass. */
  case ALL   extends FaceSearchOptions("Must match all faces")
  /** A card passes the filter if its front face passes, regardless of whether or not the back does. */
  case FRONT extends FaceSearchOptions("Only has to match the front face")
  /** A card passes the filter if its back face passes, regardless of whether or not the front does. */
  case BACK  extends FaceSearchOptions("Only has to match the back face")
}
