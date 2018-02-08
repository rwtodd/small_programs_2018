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
    """Start from IMG, hill-climb toward TGTARR, 
       using COLORS. Try adding shapes with DRAWFUNC TRIES times."""
    for _ in range(tries):
        bbox = random_bbox(img)
        scratch = img.crop(bbox)
        subtgt  = tgtarr[ bbox[1]:bbox[3], bbox[0]:bbox[2], : ]
        cur_err = rms_err(np.array(scratch, dtype=np.uint16), subtgt)
        random_shape(scratch, colors, drawfunc)
        new_err = rms_err(np.array(scratch, dtype=np.uint16), subtgt)
        if new_err < cur_err:
           img.paste(scratch,(bbox[0],bbox[1]))
    best_err = rms_err(np.array(img, dtype=np.uint16), tgtarr)
    return (img, best_err)

def hill_climb_worker(tgtp, mgr, each, start_img, start_err, shape):
    """control a worker for image TGTP and pipe MGR. Start with START_IMG+START_ERR
       and generate EACH SHAPEs at a time."""
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
    drawfunc = drawers[shape]

    # hill-climb and loop
    while True:
        img, nerr = hill_climb(img, tgtarr, colors, each, drawfunc)
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


def manage_workers(tgt, starting_point, procs, iters, each, shape):
    """Create PROCS workers, each of which will generate EACH SHAPEs at
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
                           args=(ptgt, ccon, each, best_img, best_err, shape))
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
  parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument("target", help="the target image to recreate")
  parser.add_argument("-s", dest="start", help="image to use as the starting point of the search")
  parser.add_argument("-i", dest="iterations", type=int, default=100, help="number of iterations to run")
  parser.add_argument("-e", dest="each", type=int, default=5000, help="number of shapes to try per iteration")
  parser.add_argument("-d", dest="shape", choices=list(drawers.keys()), default="filled_ellipse", help="the type of shape to draw")
  parser.add_argument("-j", dest="jobs", type=int, default=3, help="how many processes to launch")
  args = parser.parse_args()

  myimg = Image.open(args.target).convert('RGB')
  starter = args.start and Image.open(args.start).convert('RGB') or same_size_blank(myimg)
  manage_workers(myimg, starter, args.jobs, args.iterations, args.each, args.shape)


