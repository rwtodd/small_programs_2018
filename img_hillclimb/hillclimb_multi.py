# Image Hill-climbing via multiple processes...

# This is the same program as toys.y2018.hillclimb, only it adds
# a --jobs argument which start multiple processes which race to
# compute the best image.  This can make a difference with the 
# finer points of the image, when far fewer ellipses actually
# improve the image.  Until the starting point is fairly good,
# extra processes do little to improve the outcome.

import multiprocessing as mult
import numpy as np
import random
from PIL import Image, ImageDraw

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Need to be able to pickle and unpickle an image
def pickle_img(img):
    return { 'pixels': img.tobytes(), 'size': img.size, 'mode': img.mode }

def unpickle_img(pickled):
    return Image.frombytes(pickled['mode'], pickled['size'], pickled['pixels'])
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

def hill_climb_worker(tgtp, mgr, ellipses, start_img, start_err):
    """control a worker for image TGTP and pipe MGR. Start with START_IMG+START_ERR
       and generate ELLIPSES ellipses at a time."""
    # set up, and delete unneeded large objects
    random.seed()
    tgt = unpickle_img(tgtp)
    del tgtp 
    colors = get_colors(tgt)
    tgtarr = np.asarray(tgt)
    del tgt
    img = unpickle_img(start_img)
    del start_img
    best_err = start_err
    # hill-climb and loop
    while True:
        img, nerr = hill_climb(img, tgtarr, colors, best_err, ellipses)
        if nerr < best_err:
           best_err = nerr
           mgr.send( (pickle_img(img), best_err) )
        else:
           mgr.send( (None, 999999) )
        (nimg, n_err) = mgr.recv()
        if n_err == 0:
           return   # done!
        if n_err < best_err:
           img, best_err = unpickle_img(nimg), n_err


def manage_workers(tgt, starting_point, procs, iters, ellipses):
    """Create PROCS workers, each of which will generate ELLIPSES ellipses at
       a time for ITERS iterations.  Start from STARTING_POINT and hill-climb
       towrad TGT."""
    pipes = [] 
    jobs = []
    ptgt = pickle_img(tgt)
    best_img = pickle_img(starting_point)
    best_err = rms_err(np.array(starting_point, dtype=np.uint16), np.asarray(tgt))
    print(f"Starting with an error of {best_err}")
    for jno in range(procs):
       pcon, ccon = mult.Pipe()
       pipes.append(pcon)
       proc = mult.Process(target=hill_climb_worker,
                           args=(ptgt, ccon, ellipses, best_img, best_err))
       jobs.append(proc)
       print(f"Created job {jno}")
       proc.start()
    for counter in range(iters):
       for p in pipes:
           (nimg, nerr) = p.recv()
           if nerr < best_err:
               best_err, best_img = nerr, nimg
       if counter < (iters - 1):
           for p in pipes:
               p.send( (best_img, best_err) )
       print(f"Iteration {counter} err is {best_err}")
       if (counter % 10) == 0: 
          to_save = unpickle_img(best_img)
          to_save.save(f'out_{counter}.png')

    # save the last image we got
    to_save = unpickle_img(best_img)
    to_save.save(f'out_final.png')

    # tell the workers to quit:
    for p in pipes: p.send( (None, 0) )
    for j in jobs: j.join()

if __name__=='__main__':
  import argparse
  parser = argparse.ArgumentParser()
  parser.add_argument("target", help="the target image to recreate")
  parser.add_argument("-s","--start", help="image to use as the starting point of the search")
  parser.add_argument("-i","--iterations", help="number of iterations to run (default 30)")
  parser.add_argument("-e","--ellipses", help="number of ellispses to try per iteration (default 1000)")
  parser.add_argument("-j", "--jobs", help="how many processes to launch (default 2)")
  args = parser.parse_args()

  myimg = Image.open(args.target).convert('RGB')
  starter = args.start and Image.open(args.start).convert('RGB') or same_size_blank(myimg)
  procs = args.jobs and int(args.jobs) or 2 
  iterations = args.iterations and int(args.iterations) or 30
  ellipses = args.ellipses and int(args.ellipses) or 1000
  manage_workers(myimg, starter, procs, iterations, ellipses)
