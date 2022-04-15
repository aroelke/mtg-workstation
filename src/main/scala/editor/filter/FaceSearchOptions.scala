package editor.filter

import javax.swing.ImageIcon
import javax.imageio.ImageIO
import java.io.IOException
import java.awt.Image

enum FaceSearchOptions(val tooltip: String) {
  lazy val icon = try {
    ImageIcon(ImageIO.read(getClass.getResourceAsStream(s"/images/faces/${toString.toLowerCase}.png")))
  } catch case e: IOException => {
    e.printStackTrace
    ImageIcon()
  }

  def scaled(width: Int) = ImageIcon(icon.getImage.getScaledInstance(width, -1, Image.SCALE_SMOOTH))

  case ANY   extends FaceSearchOptions("Can match any face")
  case ALL   extends FaceSearchOptions("Must match all faces")
  case FRONT extends FaceSearchOptions("Only has to match the front face")
  case BACK  extends FaceSearchOptions("Only has to match the back face")
}
