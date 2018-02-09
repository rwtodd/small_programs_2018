package com.darktalents.cmd.toys.imhc;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

import joptsimple.OptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;


/** A class to hold results from a Hill-Climbing expedition.
  * It consists of an image and an RMS-error.
  */
final class Results {
   public final BufferedImage img;
   public final double rmse;
   Results(final BufferedImage bi, final double rms) {
       img = bi;
       rmse = rms;
   }
}

/** A class of static utility functions for manipulating images. */
final class ImageUtil {

  /** Compute the RMS error between images one and two. */
  static double rmsError(BufferedImage one, BufferedImage two) {
     double total = 0.0;
     final int h = one.getHeight();
     final int w = one.getWidth();
     for(int y = 0; y < h; y++) {
       for(int x = 0; x < w; x++) {
           final int rgb1 = one.getRGB(x,y);
           final int rgb2 = two.getRGB(x,y);
           final int bdiff = (rgb1 & 0xff) - (rgb2 & 0xff);
           final int gdiff = ((rgb1 >> 8) & 0xff) - ((rgb2 >> 8) & 0xff);
           final int rdiff = ((rgb1 >> 16) & 0xff) - ((rgb2 >> 16) & 0xff);
           total += (double)( (rdiff*rdiff)+(gdiff*gdiff)+(bdiff*bdiff) ) / (h*w*3) ;
       }
     }
     return Math.sqrt(total);
  }

  /** Create an empty image, the same size as TGT */
  static BufferedImage sameSizeEmpty(BufferedImage tgt) {
     return new BufferedImage(tgt.getWidth(), tgt.getHeight(), BufferedImage.TYPE_INT_RGB);
  }

  /** Add an arc to a copy of SRC, returning the result */
  final static BufferedImage addArc(final BufferedImage src, final Color color) {
      final Random rnd = ThreadLocalRandom.current();
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      g.drawArc(0,0,src.getWidth()-1, src.getHeight()-1, rnd.nextInt(360), rnd.nextInt(300)+1);
      g.dispose();
      return result;
  }

  /** Add a line to a copy of SRC, returning the result */
  final static BufferedImage addLine(final BufferedImage src, final Color color) {
      final Random rnd = ThreadLocalRandom.current();
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      if(rnd.nextBoolean()) {
         g.drawLine(0, 0, src.getWidth()-1, src.getHeight()-1);
      } else {
         g.drawLine(src.getWidth()-1, 0, 0, src.getHeight()-1);
      }
      g.dispose();
      return result;
  }


  /** Add a down-from-the-left line to a copy of SRC, returning the result */
  final static BufferedImage addDownLeftLine(final BufferedImage src, final Color color) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      g.drawLine(0, 0, src.getWidth()-1, src.getHeight()-1);
      g.dispose();
      return result;
  }

  /** Add a filled rectangle to a copy of  SRC, returning the result. */
  final static BufferedImage addFilledRectangle(final BufferedImage src, final Color color) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.setPaint(color);
      g.fillRect( 0, 0, src.getWidth(), src.getHeight() );
      g.dispose();
      return result;
  }


  /** Add a rectangle to a copy of  SRC, returning the result. */
  final static BufferedImage addRectangle(final BufferedImage src, final Color color) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      g.drawRect( 0, 0, src.getWidth(), src.getHeight() );
      g.dispose();
      return result;
  }

  /** Add a filled circle to a copy of  SRC, returning the result. */
  final static BufferedImage addFilledCircle(final BufferedImage src, final Color color) {
      final Random rnd = ThreadLocalRandom.current();
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      final int srcWidth = src.getWidth();
      final int srcHeight = src.getHeight();
      final int diameter = Math.min(srcWidth, srcHeight);
      int x = 0; int y = 0;
      if(srcWidth > diameter) {
         x = rnd.nextInt(srcWidth - diameter);
      }
      if(srcHeight > diameter) {
         y = rnd.nextInt(srcHeight - diameter);
      }
      g.fillOval( x, y, diameter, diameter);
      g.dispose();
      return result;
  }

  /** Add a circle to a copy of  SRC, returning the result. */
  final static BufferedImage addCircle(final BufferedImage src, final Color color) {
      final Random rnd = ThreadLocalRandom.current();
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      final int srcWidth = src.getWidth();
      final int srcHeight = src.getHeight();
      final int diameter = Math.min(srcWidth, srcHeight);
      int x = 0; int y = 0;
      if(srcWidth > diameter) {
         x = rnd.nextInt(srcWidth - diameter);
      }
      if(srcHeight > diameter) {
         y = rnd.nextInt(srcHeight - diameter);
      }
      g.drawOval( x, y, diameter, diameter);
      g.dispose();
      return result;
  }

  /** Add an ellipse to a copy of  SRC, returning the result. */
  final static BufferedImage addEllipse(final BufferedImage src, final Color color) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      g.drawOval( 0, 0, src.getWidth(), src.getHeight() );
      g.dispose();
      return result;
  }

  /** Add a filled ellipse to a copy of  SRC, returning the result. */
  final static BufferedImage addFilledEllipse(final BufferedImage src, final Color color) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(color);
      g.fillOval( 0, 0, src.getWidth(), src.getHeight() );
      g.dispose();
      return result;
  }

  /** copy SRC into TGT.. assumes they are the same size */
  final static void copyInto(final BufferedImage tgt, final BufferedImage src) {
      Graphics2D g = tgt.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.dispose();
  }

  /** Pull all the unique colors out of an image, returning them in an array. */
  static Color[] getColors(BufferedImage bi) {
     final Set<Integer> s = new HashSet<>();
     final int h = bi.getHeight();
     final int w = bi.getWidth();
     for(int y = 0; y < h; y++) {
       for(int x = 0; x < w; x++) {
           s.add(bi.getRGB(x,y));
       }
     }
     return s.stream().map(Color::new).toArray(Color[]::new);
  }

  /** Read an image, and convert it to TYPE_INT_RGB if necessary. */
  static BufferedImage readAsRGB(String fn) {
     BufferedImage bi = null;
     try(InputStream is = new FileInputStream(fn)) {
       bi = ImageIO.read(is);
       if(bi.getType() != BufferedImage.TYPE_INT_RGB) {
          BufferedImage ni = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
          Graphics2D g = ni.createGraphics();
          g.drawImage(bi, 0, 0, null);
          g.dispose();
          bi = ni;
       }
     } catch(Exception e) { }
     return bi;
  }
}

/** HillClimber randomly attempts to approximate an image with ellipses.
  * It keeps an ellipse that improves the result so far, and throws away
  * all attempts which hurt the outcome (as measured by RMS-error).
  * It is a Callable<Results>, meant to be executed in an Executor in 
  * parallel with other HillClimbers.
  */
final class HillClimber implements Callable<Results> {
  final int steps;   // how many ellipses to attempt and measure
  final BufferedImage tgt;  // the target image
  final Color[] tgtColors;  // the list of possible colors
  final Results soFar;      // our best outcome so far
  final BiFunction<BufferedImage,Color,BufferedImage> drawFunc; // our drawing method

  HillClimber(final int steps, 
              final BufferedImage tgt, 
              final Color[] tgtColors, 
              final BiFunction<BufferedImage,Color,BufferedImage> drawFunc,
              final Results soFar) {
     this.steps = steps;
     this.tgt = tgt;
     this.tgtColors = tgtColors;
     this.drawFunc = drawFunc;

     if(soFar == null) {
        BufferedImage empty = ImageUtil.sameSizeEmpty(tgt);
        this.soFar = new Results(empty, ImageUtil.rmsError(empty, tgt));
     }
     else {
        this.soFar = soFar;
     }
  }

  public Results call()  {
      final Random rnd = ThreadLocalRandom.current();
      double best_err = soFar.rmse;
      BufferedImage best_img = soFar.img;
      final int tgtWd = tgt.getWidth();
      final int tgtHt = tgt.getHeight();
      final int maxX = tgtWd / 10;
      final int maxY = tgtHt / 10;

      for(int i = 0; i < this.steps; i++) {
          // determine a random bounding box
          final int ulx = rnd.nextInt(tgtWd-1);
          final int uly = rnd.nextInt(tgtHt-1);
          final int wd = Math.min(tgtWd - ulx, rnd.nextInt(maxX)+1);
          final int ht = Math.min(tgtHt - uly, rnd.nextInt(maxY)+1);
          // determine a random color
          final Color c = tgtColors[ rnd.nextInt(tgtColors.length) ];
          // get a sub-image of tgt, and a drawn-on version
          BufferedImage subtgt = tgt.getSubimage(ulx,uly,wd,ht);
          BufferedImage subbst = best_img.getSubimage(ulx,uly,wd,ht);
          BufferedImage scratch = drawFunc.apply(subbst, c);
          // determine if the error is better or worse...
          final double cur_err = ImageUtil.rmsError(scratch, subtgt);
          final double bst_err = ImageUtil.rmsError(subbst, subtgt);
          if(cur_err < bst_err) {
               // copy the scratch into the best_image
               ImageUtil.copyInto(subbst, scratch);
          }
      }

      best_err = ImageUtil.rmsError(best_img, tgt);
      return new Results(best_img, best_err);
  }

}

public final class Cmd {

  static final Map<String,BiFunction<BufferedImage,Color,BufferedImage>> drawers = 
      Map.of(
             "arc",              ImageUtil::addArc,
             "circle",           ImageUtil::addCircle,
             "filled_circle",    ImageUtil::addFilledCircle,
             "ellipse",          ImageUtil::addEllipse,
             "filled_ellipse",   ImageUtil::addFilledEllipse,
             "line",             ImageUtil::addLine, 
             "downleft_line",    ImageUtil::addDownLeftLine,
             "rectangle",        ImageUtil::addRectangle,
             "filled_rectangle", ImageUtil::addFilledRectangle
      );

  /** Drive a parallel search for better images, by letting
    * HillClimber's "race" each other to get better results.
    * Periodically save an output.
    */
  static final void doPic(final String tgtFn, 
                          final String startFn, 
                          final BiFunction<BufferedImage,Color,BufferedImage> drawer,
                          final int numThreads, 
                          final int numIterations, 
                          final int numSteps) throws Exception {
      final BufferedImage tgt = ImageUtil.readAsRGB( tgtFn );
      final Color[] tgtColors = ImageUtil.getColors(tgt);
      Results best = null;
      final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
      if(startFn  != null) {
		  final BufferedImage start = ImageUtil.readAsRGB( startFn ); 
          best = new Results(start, ImageUtil.rmsError(start, tgt));
      }

      ArrayList<Future<Results>> futures = new ArrayList<>(numThreads);
      for(int i = 0; i < numIterations; i++) {
        futures.clear();
        for(int j = 0; j < numThreads; j++) {
            futures.add( executor.submit(new HillClimber(numSteps, tgt, tgtColors, drawer, best))  );
        }
        
        for(int j = 0; j < numThreads; j++) {
            final Results cur = futures.get(j).get();
            if((best == null) || (cur.rmse < best.rmse)) {
               best = cur;
            }
		}
        System.out.printf("Iteration %d: error %f\n", i, best.rmse);
        if(i % 10 == 0) {
          ImageIO.write(best.img,"PNG", new File(String.format("Output_%05d.png",i)));
        }
      }
      ImageIO.write(best.img,"PNG", new File("Output_final.png"));
      
      executor.shutdown();
      try {
          executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
      } catch(Exception e) {
          System.err.println(e);
      }
  }

 
  public static void printDrawers() {
     System.err.println("Available -d shape options are:"); 
     drawers.keySet().stream().forEachOrdered( k -> System.err.printf("\t%s\n",k) );
  }

  public static void main(String[] args) {
     OptionParser op = new OptionParser();
   
     OptionSpec<Void> helpOption = op.acceptsAll(List.of("h","?","help"), "print help");
     OptionSpec<String> startOption = op.accepts("s", "start from this image").withRequiredArg().ofType(String.class);
     OptionSpec<Integer> njOption = op.accepts("j", "the number of concurrent jobs").withRequiredArg().ofType(Integer.class).defaultsTo(4);
     OptionSpec<Integer> niOption = op.accepts("i", "the number of iterations to run").withRequiredArg().ofType(Integer.class).defaultsTo(100);
     OptionSpec<Integer> neOption = op.accepts("e", "the number of tries each iteration").withRequiredArg().ofType(Integer.class).defaultsTo(3000);
     OptionSpec<String> ndOption = op.accepts("d", "the shape to draw").withRequiredArg().ofType(String.class).defaultsTo("filled_ellipse");
     OptionSpec<String> files = op.nonOptions("the source image").ofType(String.class);
    try {
      OptionSet os = op.parse(args);

      if(os.has(helpOption)) {
			op.printHelpOn(System.err); 
            printDrawers(); 
            System.exit(1);
      }

      for(String fn : files.values(os)) {
          doPic(fn, 
                startOption.value(os), 
                drawers.get(ndOption.value(os)), 
                njOption.value(os), 
                niOption.value(os), 
                neOption.value(os));
          break;
      }

    } catch(java.io.IOException e) {
			System.err.println(e);
			System.exit(1);
    } catch(Exception ex) {
			System.err.println(ex);
			try { op.printHelpOn(System.err); printDrawers(); } catch(Exception ex2) { /* already in an exception... */ }
    }
  }
}
