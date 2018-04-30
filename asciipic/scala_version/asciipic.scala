package org.rwtodd.app.asciipic 

import java.awt.{Color,RenderingHints}
import java.awt.image.{BufferedImage,ColorConvertOp}
import java.io.File
import javax.imageio.ImageIO

object Thumbnail {
  private def scaleImage(orig: BufferedImage, width: Int, height: Int) : BufferedImage = {
      val scaled = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
      val gfx2d = scaled.createGraphics()
      gfx2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                             RenderingHints.VALUE_INTERPOLATION_BICUBIC)
      gfx2d.drawImage(orig, 0, 0, width, height, null)
      gfx2d.dispose()                             
      scaled
  }

  // load the given image, and progressively scale it down to the given width
  def apply(fname: String, scaledWidth: Int, aspectRatio: Double) : BufferedImage = {
      var image = {
          val orig = ImageIO.read(new File(fname))
          val gray = new BufferedImage(orig.getWidth, orig.getHeight, BufferedImage.TYPE_BYTE_GRAY)

          // first convert it to grayscale
          val cc = new ColorConvertOp(orig.getColorModel.getColorSpace,
                                      gray.getColorModel.getColorSpace,
                                      new RenderingHints(RenderingHints.KEY_DITHERING,
                                                         RenderingHints.VALUE_DITHER_ENABLE) ) 
          cc.filter(orig,gray)
          gray
      }

      val scaledHeight = ((scaledWidth / aspectRatio / image.getWidth) * image.getHeight).toInt

      while(scaledWidth < image.getWidth/2) {
          image = scaleImage(image, image.getWidth/2, image.getHeight/2)
      }

      scaleImage(image, scaledWidth, scaledHeight) 
  }

}

object AsciiPic {

  // select a character to use based on the given brightness
  private var charsets = Map("ascii" -> "#A@%$+=*:,. ".toArray,
                             "block" -> "\u2588\u2593\u2592\u2591 ".toArray)
  private var chars : Array[Char] = null
  private def selectChar(b: Int) = chars((b*chars.length/256.0).toInt)

  // convert an entire image from RGB to ascii
  private def convertImage(im: BufferedImage) : String = {
      var raster = im.getData
      val width = im.getWidth
      var line = new Array[Int](width)
      (0 until im.getHeight)
          .map { y => 
             raster.getSamples(0,y,width,1,0,line)
                   .map(selectChar)
                   .mkString
          }
          .mkString("\n")
  }
           
  def main(cmdline: Array[String]) : Unit = {
    import org.rwtodd.argparse._
    var width : Int = 0 
    var ar : Double = 0.0 
    var reversed : Boolean = false

    lazy val argp: Args = new Args(
       new IntArg("-w","<width> sets the width (default 72)")
             .defaultsTo(72).does { width = _ },
       new DoubleArg("-ar","<ratio> sets the text aspect ratio (h/w), default 2.0")
             .defaultsTo(2.0).does {ar = _},
       new FlagArg("-wob","reverses the video for white-on-black terminals")
             .does { reversed = _ },
       new StrArg("-charset","<ascii|block> select chars to use, default ascii")
             .choices(charsets.keys.toList).defaultsTo("ascii").does { cs => chars = charsets(cs) },
       new FlagArg("-help","shows this help message").does { _ => argp.showHelp() }
    ) { 
       override def showHelp() = {
         println("Usage: asciipic [options] fname") ; println()
         super.showHelp()
         System.exit(1)
       }
    }

    val fnames = argp.parse(cmdline)
    if (fnames.isEmpty) argp.showHelp() 
    if (reversed) chars = chars.reverse

    fnames.foreach { fname =>
       println(convertImage(Thumbnail(fname, width, ar)))
       println()
    }
  }
}
