package com.darktalents.cmd.toys.imhc;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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

  /** Add an ellipse to a copy of  SRC, returning the result. */
  final static BufferedImage addRandomEllipse(final Random rnd, final BufferedImage src, final Color[] colors, final int maxSize) {
      BufferedImage result = sameSizeEmpty(src);
      Graphics2D g = result.createGraphics();
      g.drawImage(src, 0, 0, null);
      g.setPaint(colors[ rnd.nextInt(colors.length) ] );
      g.fillOval( rnd.nextInt(src.getWidth()), rnd.nextInt(src.getHeight()),
                 rnd.nextInt(maxSize)+1, rnd.nextInt(maxSize)+1); 
      g.dispose();
      return result;
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

  HillClimber(final int steps, final BufferedImage tgt, final Color[] tgtColors, Results soFar) {
     this.steps = steps;
     this.tgt = tgt;
     this.tgtColors = tgtColors;

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
      final int maxSize = Math.min(tgt.getWidth(), tgt.getHeight()) / 10;

      for(int i = 0; i < this.steps; i++) {
          BufferedImage scratch = ImageUtil.addRandomEllipse(rnd, best_img, tgtColors, maxSize);
          double cur_err = ImageUtil.rmsError(scratch, tgt);
          if(cur_err < best_err) {
             best_err = cur_err;
             best_img = scratch;
          }
      }
      return new Results(best_img, best_err);
  }

}

public final class Cmd {

  /** Drive a parallel search for better images, by letting
    * HillClimber's "race" each other to get better results.
    * Periodically save an output.
    */
  static final void doPic(final String tgtFn, 
                          final String startFn, 
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
            futures.add( executor.submit(new HillClimber(numSteps, tgt, tgtColors, best))  );
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

 
  public static void main(String[] args) {
     OptionParser op = new OptionParser();
     OptionSpec<String> startOption = op.accepts("s", "start from this image").withRequiredArg().ofType(String.class);
    OptionSpec<Integer> njOption = op.accepts("j", "the number of concurrent jobs").withRequiredArg().ofType(Integer.class).defaultsTo(4);
    OptionSpec<Integer> niOption = op.accepts("i", "the number of iterations to run").withRequiredArg().ofType(Integer.class).defaultsTo(10);
    OptionSpec<Integer> neOption = op.accepts("e", "the number of ellipses per iteration").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
    OptionSpec<String> files = op.nonOptions("the image").ofType(String.class);
    try {
      OptionSet os = op.parse(args);
      for(String fn : files.values(os)) {
          doPic(fn, startOption.value(os), njOption.value(os), niOption.value(os), neOption.value(os));
          break;
      }
    } catch(java.io.IOException e) {
			System.err.println(e);
			System.exit(1);
    } catch(Exception ex) {
			System.err.println(ex);
			try { op.printHelpOn(System.err); } catch(Exception ex2) { /* already in an exception... */ }
    }
  }
}
