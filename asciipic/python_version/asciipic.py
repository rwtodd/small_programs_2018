from PIL import Image

def proportional_resize(orig_size, desired_width, aspect_ratio=2.0):
    """proportionally resize an image with a given aspect-ratio"""
    w,h = orig_size
    return (desired_width, int(round(desired_width / aspect_ratio / w * h)) )

def convert_for_terminal(img, desired_width, aspect_ratio=2.0):
    """Convert an image to a screen-sized grayscale representation"""
    sized = img.resize(proportional_resize(img.size, desired_width, aspect_ratio), resample=Image.BICUBIC)
    return sized.convert('L', dither=Image.FLOYDSTEINBERG)

CHARSETS = { 
   'ascii':   '#A@%$+=*:,. ',
   'blocks':  '\u2588\u2593\u2592\u2591 '
}

def to_text(img, charset, reverse_video=False):
    """convert an image to a text that approximates the brightness of
       the pixels in IMG, using CHARSET, and possibly in REVERSE_VIDEO"""
    chars = CHARSETS[charset]
    if reverse_video: chars = chars[::-1]
    CHARSLEN = len(chars)
    imgbytes = img.point(lambda lvl: int(lvl * CHARSLEN / 256.0)).tobytes()
    return '\n'.join( ''.join(chars[ imgbytes[i] ] for i in range(row,row+img.width)) 
                         for row in range(0, len(imgbytes), img.width))
     
     
def asciipic(fn, ar, w, wob, charset):
    """convert an image with filename FN to ascii with aspect ratio AR. The result
       will have width W, and display in white-on-black mode if WOB is True."""
    
    orig = Image.open(fn)
    term = convert_for_terminal(orig, w, ar)
    orig.close()
    return to_text(term, charset, wob)
    
if __name__=='__main__':
  import argparse
  parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument("image", help="the target image to convert")
  parser.add_argument("--ar", type=float, default=2.0, help="aspect ratio of terminal chars (h:w)")
  parser.add_argument("--width", type=int, default=72, help="desired width in chars of the output")
  parser.add_argument("--wob", action="store_true", help="set if terminal is white chars on black background")
  parser.add_argument("--chars", default='ascii', choices=list(CHARSETS.keys()), help="charset to use")
  args = parser.parse_args()

  print(asciipic(args.image, args.ar, args.width, args.wob, args.chars))
