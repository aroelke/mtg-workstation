package editor.gui.display

import editor.database.card.Card
import editor.database.card.CardLayout
import editor.database.symbol.FunctionalSymbol
import editor.gui.generic.ComponentUtils
import editor.gui.settings.SettingsDialog

import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import javax.swing.UIManager
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument
import scala.jdk.CollectionConverters._
import scala.util.Using

/**
 * Structure containing information about card images and where to get them and downloads missing card images when they are
 * set to be displayed in a [[CardImagePanel]].
 * 
 * @author Alec Roelke
 */
object CardImagePanel {
  /** Aspect ratio of a Magic: The Gathering card. */
  val AspectRatio = 63.0/88.0
  /** String to format for generating the link to a card's image on Scryfall. */
  val ScryfallFormat = "https://api.scryfall.com/cards/%s?format=image%s"
  /** String to format for generating the link to a card's image on Gatherer. */
  val GathererFormat = "https://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=%d&type=card%s"

  private val progressBars = collection.mutable.ArrayBuffer[JProgressBar]()
  private val progressLabels = collection.mutable.ArrayBuffer[JLabel]()

  /** @return the list of file names that will contain the image(s) to display for a card */
  def getFiles(card: Card): Seq[File] = SettingsDialog.settings.inventory.imageSource match {
    case "Scryfall" => (0 until card.imageNames.size).map((i) => Paths.get(SettingsDialog.settings.inventory.scans, s"${card(i).scryfallid}$i.jpg").toFile).toSeq
    case "Gatherer" => (0 until card.imageNames.size).map((i) => Paths.get(SettingsDialog.settings.inventory.scans, s"${card(i).multiverseid}$i.jpg").toFile).toSeq
    case _ => Seq.empty
  }

  /** @return the URLs to download a card's image(s) if they are missing */
  def getURLs(card: Card): Seq[Option[URL]] = SettingsDialog.settings.inventory.imageSource match {
    case "Scryfall" => card.layout match {
      case CardLayout.FLIP => Seq(Some(URL(ScryfallFormat.format(card.scryfallid, ""))))
      case CardLayout.MELD => (0 until card.imageNames.size).map((i) => Some(URL(ScryfallFormat.format(card(i).scryfallid, ""))))
      case _ => (0 until card.imageNames.size).map((i) => Some(URL(ScryfallFormat.format(card(i).scryfallid, if (i > 0 && i == card.imageNames.size - 1) "&face=back" else "")))).toSeq
    }
    case "Gatherer" => card.layout match {
      case CardLayout.FLIP => card.faces.map(_.multiverseid).zipWithIndex.map{ case (id, i) => if (id >= 0) {
        Some(URL(GathererFormat.format(id, if (i > 0 && i == card.faces.size - 1) "options=rotate180" else "")))
      } else {
        None
      }}
      case _ => card.faces.map((f) => Option.when(f.multiverseid >= 0)(URL(GathererFormat.format(f.multiverseid, ""))))
    }
    case _ => Seq.empty
  }

  /**
   * Create a progress bar and label indicating progress in downloading an image.
   * @return a panel containing the progress bar and image
   */
  def createStatusBar = {
    val panel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
    panel.setBorder(BorderFactory.createEtchedBorder())
    val bar = JProgressBar()
    bar.setEnabled(false)
    panel.add(bar)
    panel.add(Box.createHorizontalStrut(5))
    val label = JLabel()
    panel.add(label)

    progressBars += bar
    progressLabels += label
    panel
  }

  private case class DownloadRequest(source: CardImagePanel, card: Card)

  private object ImageDownloadWorker extends SwingWorker[Unit, Integer] {
    private val toDownload = java.util.concurrent.LinkedBlockingQueue[DownloadRequest]
    private var size = 0

    def downloadCard(source: CardImagePanel, card: Card) = {
      try {
        toDownload.put(DownloadRequest(source, card))
      } catch case e: InterruptedException => e.printStackTrace()
    }

    protected override def doInBackground() = {
      while (true) {
        val req = toDownload.take()
        val files = getFiles(req.card)
        val urls = getURLs(req.card)
        for (i <- 0 until urls.size) {
          if (!files(i).exists) {
            urls(i).foreach{ site =>
              files(i).getParentFile.mkdirs()
              try {
                val connection = site.openConnection()
                size = connection.getContentLength
                var downloaded = 0
                SwingUtilities.invokeLater(() => {
                  progressBars.foreach{ bar =>
                    bar.setEnabled(true)
                    bar.setMaximum(size)
                  }
                  progressLabels.foreach(_.setText(s"Downloading image of ${req.card.name} ..."))
                })

                Using.resources(BufferedInputStream(connection.getInputStream), BufferedOutputStream(FileOutputStream(files(i)))){ (in, out) =>
                  val data = new Array[Byte](1024)
                  var x = 0
                  while ({ x = in.read(data); x } > 0) {
                    out.write(data, 0, x)
                    downloaded += x
                    publish(downloaded)
                  }
                }
              } catch case e: Exception => System.err.println(s"Error downloading ${files(i)}: ${e.getMessage}")
            }
          }
        }
        if (req.card.layout == CardLayout.FLIP && files(0).exists) {
          try {
            val original = ImageIO.read(files(0))
            val flipped = BufferedImage(original.getWidth, original.getHeight, original.getType)
            val op = AffineTransformOp(AffineTransform.getRotateInstance(math.Pi, flipped.getWidth/2, flipped.getHeight/2), AffineTransformOp.TYPE_BILINEAR)
            ImageIO.write(op.filter(original, flipped), "jpg", files(1))
          } catch case e: Exception => e.printStackTrace()
        }
        if (SettingsDialog.settings.inventory.imageLimitEnable) {
          var images: Array[File] = null // Gets assigned in the very next statement, so null should be okay here
          while ({ images = Paths.get(SettingsDialog.settings.inventory.scans).toFile.listFiles; images.size > SettingsDialog.settings.inventory.imageLimit }) {
            images.minBy(_.lastModified).delete()
          }
        }
        SwingUtilities.invokeLater(() => {
          if (req.source.card.exists(_ == req.card))
            req.source.loadImages()
        })
        publish(0)
        if (toDownload.isEmpty) {
          SwingUtilities.invokeLater(() => {
            progressBars.foreach(_.setEnabled(false))
            progressLabels.foreach(_.setText(""))
          })
        }
      }
    }

    override protected def process(chunks: java.util.List[Integer]) = progressBars.foreach(_.setValue(chunks.asScala.last))

    execute()
  }
}

/**
 * Panel for displaying card images.  The card image will resize itself to fit the panel, but maintain its aspect ratio.
 * 
 * @constructor create a new card image panel, optionally displaying a card
 * @param card card to display, if defined
 * 
 * @author Alec Roelke
 */
class CardImagePanel(private var card: Option[Card] = None) extends JPanel {
  import CardImagePanel._

  private var image: Option[BufferedImage] = None
  private var faceImages = Seq.empty[Option[BufferedImage]]
  private var face = 0

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) = {
      face = card.map((face + 1) % _.imageNames.size).getOrElse(0)
      getParent.revalidate()
      repaint()
    }
  })
  card.foreach(setCard(_))

  /**
   * Change the card to display.  If the image has not been downloaded, download it first.
   * @param c card to display
   */
  def setCard(c: Card) = if (!card.exists(_ == c)) {
    card = Some(c)
    face = 0
    faceImages = Seq.empty

    try {
      Files.createDirectories(Path.of(SettingsDialog.settings.inventory.scans))
      if (getFiles(card.get).forall(_.exists))
        loadImages()
      else
        ImageDownloadWorker.downloadCard(this, c)
    } catch case e: IOException => {}
    revalidate()
    repaint()
  }

  /** Clear the panel so it displays nothing. */
  def clearCard() = {
    card = None
    face = 0
    faceImages = Seq.empty
    revalidate()
    repaint()
  }

  private def loadImages() = synchronized {
    card.foreach{ c =>
      faceImages = getFiles(c).map{ file => if (file.exists) {
        try {
          Some(ImageIO.read(file))
        } catch case e: IOException => None
      } else None }
      Option(getParent).foreach{ p => SwingUtilities.invokeLater(() => {
        p.revalidate()
        repaint()
      })}
    }
  }

  override def getPreferredSize = (Option(getParent), image) match {
    case (Some(p), Some(img)) => Dimension((p.getHeight*(img.getWidth.toDouble/img.getHeight.toDouble)).toInt, p.getHeight)
    case _ => super.getPreferredSize
  }

  override def setBounds(x: Int, y: Int, width: Int, height: Int) = {
    super.setBounds(x, y, width, height)

    if (!card.isDefined || width == 0 || height == 0)
      image = None
    else {
      val (w: Int, h: Int) = if (faceImages.size <= face || faceImages(face).isEmpty)
        ((height*AspectRatio).toInt, height)
      else
        faceImages(face).map((img) => (img.getWidth, img.getHeight)).getOrElse((0, 0))

      image = Some(BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB))
      val g = image.get.createGraphics
      if (faceImages.size <= face || !faceImages(face).isDefined) {
        val faceWidth = (h*AspectRatio).toInt

        val missingCardPane = JTextPane()
        val document = missingCardPane.getDocument.asInstanceOf[StyledDocument]
        val textStyle = document.addStyle("text", null)
        StyleConstants.setFontFamily(textStyle, UIManager.getFont("Label.font").getFamily)
        StyleConstants.setFontSize(textStyle, ComponentUtils.TextSize)
        val reminderStyle = document.addStyle("reminder", textStyle)
        StyleConstants.setItalic(reminderStyle, true)
        card.foreach(_.formatDocument(document, false, face))
        missingCardPane.setSize(Dimension(faceWidth - 4, h - 4))

        val img = BufferedImage(faceWidth, h, BufferedImage.TYPE_INT_ARGB)
        missingCardPane.paint(img.getGraphics)
        g.drawImage(img, 2, 2, null)
        g.setColor(Color.BLACK)
        g.drawRect(0, 0, faceWidth - 1, h - 1)
      }
      else
        faceImages(face).foreach(g.drawImage(_, 0, 0, null))
    }
  }

  override protected def paintComponent(g: Graphics) = {
    super.paintComponent(g)

    image.foreach{ img => g match {
      case g2: Graphics2D =>
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        val aspectRatio = img.getWidth.toDouble/img.getHeight.toDouble
        var width = (getHeight*aspectRatio).toInt
        var height = getHeight
        if (width > getWidth) {
          width = getWidth
          height = (width/aspectRatio).toInt
        }
        g2.drawImage(img, (getWidth - width)/2, (getHeight - height)/2, width, height, null)

        if (card.get.imageNames.size > 1) {
          val size = 15
          val border = 3
          FunctionalSymbol.values(if (face % 2 == 0) "T" else "Q").getIcon(size).paintIcon(this, g2, getWidth - size - border, getHeight - size - border)
        }
    }}
  }
}