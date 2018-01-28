# Image Hill-climbing 
# Generates random ellipses and keeps the ones that make the image
# closer to the given target, by RMSE. 

import numpy as np
import random
from PIL import Image, ImageDraw

def rms_err(test, target):
    """determine the RMS Error between two arrays. N.B.: overwrites test"""
    return np.sqrt(np.mean(np.square(np.subtract(test,target,out=test),out=test)))

def same_size_blank(tgt, color=(0,0,0)):
    """create an empty image of the same size as the tgt"""
    return Image.new('RGB', tgt.size, color)

def get_colors(img):
    """pull the colors out of an image"""
    return [ c[1] for c in img.getcolors(img.size[0]*img.size[1]) ]

def random_ellipse(img, colors):
    """draw a random ellipse into IMG in one of the color from COLORS"""
    x,y = img.size
    d = ImageDraw.Draw(img)
    ulx, uly = random.randrange(x), random.randrange(y)
    wid,ht = random.randrange(1, x // 10), random.randrange(1, y // 10)
    d.ellipse([ulx,uly,ulx+wid,uly+ht],fill=random.choice(colors))
    del d
    
def hill_climb(img, tgtarr, colors, best_err, tries):
    """Start from IMG (which has error BEST_ERR, hill-climb toward TGTARR, 
       using COLORS. Try adding ellipsees TRIES times."""
    for _ in range(tries):
        scratch = img.copy()
        random_ellipse(scratch, colors)
        cur_err = rms_err(np.array(scratch, dtype=np.uint16),tgtarr)
        if cur_err < best_err:
           img = scratch
           best_err = cur_err
    return (img, best_err)

def manage_work(tgt, best_img, iters, ellipses):
    """Generate ELLIPSES ellipses at a time for ITERS iterations.  
       Start from BEST_IMG and hill-climb toward TGT."""
    random.seed()
    tgtarr = np.asarray(tgt)
    colors = get_colors(tgt)
    best_err = rms_err(np.array(best_img, dtype=np.uint16), tgtarr)
    print(f"Starting with an error of {best_err}")

    for counter in range(iters):
       best_img, best_err = hill_climb(best_img, tgtarr, colors, best_err, ellipses)
       print(f"Iteration {counter} err is {best_err}")
       if (counter % 10) == 0: 
          best_img.save(f'out_{counter}.png')

    best_img.save(f'out_final.png')

if __name__=='__main__':
  import argparse
  parser = argparse.ArgumentParser()
  parser.add_argument("target", help="the target image to recreate")
  parser.add_argument("-s","--start", help="image to use as the starting point of the search")
  parser.add_argument("-i","--iterations", help="number of iterations to run (default 30)")
  parser.add_argument("-e","--ellipses", help="number of ellispses to try per iteration (default 1000)")
  args = parser.parse_args()

  myimg = Image.open(args.target).convert('RGB')
  starter = args.start and Image.open(args.start).convert('RGB') or same_size_blank(myimg)
  iterations = args.iterations and int(args.iterations) or 30
  ellipses = args.ellipses and int(args.ellipses) or 1000
  manage_work(myimg, starter, iterations, ellipses)
