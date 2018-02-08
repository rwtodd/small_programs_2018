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

def random_bbox(img):
    """generate random bounding-box inside the img"""
    x,y = img.size
    ulx, uly = random.randrange(x-1), random.randrange(y-1)
    wid,ht = random.randrange(1, x // 10), random.randrange(1, y // 10)
    return [ulx,uly,min(x,ulx+wid),min(uly+ht,y)]

def random_shape(img, colors, func):
    """draw a random ellipse into IMG in one of the color from COLORS"""
    x,y = img.size
    d = ImageDraw.Draw(img)
    func(d,[0,0,x,y],random.choice(colors))
    del d
    
def draw_arc(d, bbox, color):
    a1 = random.randrange(350)
    a2 = random.randrange(a1+1, 360)
    d.arc(bbox, a1, a2, fill=color)
    
def draw_line(d,bbox,color):
    if random.randrange(2) == 1:
       bbox[0],bbox[2] = bbox[2], bbox[0]
    d.line(bbox, width=random.randrange(1,3), fill=color),

drawers = {
  'filled_ellipse': lambda d,b,c: d.ellipse(b,fill=c),
  'ellipse': lambda d,b,c: d.ellipse(b,outline=c),
  'rectangle': lambda d,b,c: d.rectangle(b,outline=c),
  'filled_rectangle': lambda d,b,c: d.rectangle(b,fill=c),
  'line': draw_line,
  'downleft_line': lambda d,b,c: d.line(b, width=random.randrange(1,3), fill=c),
  'arc': draw_arc,
}

def hill_climb(img, tgtarr, colors, tries, drawfunc):
    """Start from IMG (which has error BEST_ERR, hill-climb toward TGTARR, 
       using COLORS. Try adding ellipsees TRIES times."""

    # print(f'TGT SIZE tgtarr {tgtarr.shape}')
    for _ in range(tries):
        bbox = random_bbox(img)
        scratch = img.crop(bbox)
        subtgt  = tgtarr[ bbox[1]:bbox[3], bbox[0]:bbox[2], : ]
        # scar = np.array(scratch, dtype=np.uint16)
        # print(f'FOR {bbox}: scratch {scratch.size}, scar {scar.shape} and subtgt {subtgt.shape}')
        cur_err = rms_err(np.array(scratch, dtype=np.uint16), subtgt)
        random_shape(scratch, colors, drawfunc)
        new_err = rms_err(np.array(scratch, dtype=np.uint16), subtgt)
        if new_err < cur_err:
           img.paste(scratch,(bbox[0],bbox[1]))
    best_err = rms_err(np.array(img, dtype=np.uint16), tgtarr)
    return (img, best_err)

def manage_work(tgt, best_img, iters, each, shape):
    """Generate EACH random SHAPE at a time for ITERS iterations.  
       Start from BEST_IMG and hill-climb toward TGT."""
    random.seed()
    drawfunc = drawers[shape]
    tgtarr = np.asarray(tgt)
    colors = get_colors(tgt)
    best_err = rms_err(np.array(best_img, dtype=np.uint16), tgtarr)
    print(f"Starting with an error of {best_err}")

    for counter in range(iters):
       best_img, best_err = hill_climb(best_img, tgtarr, colors, each, drawfunc)
       print(f"Iteration {counter} err is {best_err}")
       if (counter % 10) == 0: 
          best_img.save(f'out_{counter}.png')

    best_img.save(f'out_final.png')

if __name__=='__main__':
  import argparse
  parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument("target", help="the target image to recreate")
  parser.add_argument("-s", dest="start", help="image to use as the starting point of the search")
  parser.add_argument("-i", dest="iterations", type=int, default=30, help="number of iterations to run")
  parser.add_argument("-e", dest="each", type=int, default=1000, help="number of shapes to try per iteration")
  parser.add_argument("-d", dest="shape", choices=list(drawers.keys()), default="filled_ellipse", help="the type of shape to draw")
  args = parser.parse_args()

  myimg = Image.open(args.target).convert('RGB')
  starter = args.start and Image.open(args.start).convert('RGB') or same_size_blank(myimg)
  manage_work(myimg, starter, args.iterations, args.each, args.shape)
