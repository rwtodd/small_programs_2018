from PIL import Image

def proportional_resize(orig_size, desired_width, aspect_ratio=2.0):
    """proportionally resize an image with a given aspect-ratio"""
    w,h = orig_size
    return (desired_width, int(round(desired_width / aspect_ratio / w * h)) )

def convert_for_terminal(img, desired_width, aspect_ratio=2.0):
    """Convert an image to a screen-sized grayscale representation"""
    sized = img.resize(proportional_resize(img.size, desired_width, aspect_ratio), resample=Image.BICUBIC)
    return sized.convert('L', dither=Image.FLOYDSTEINBERG)

def to_bytes(img, reverse_video=False):
    """convert an image to a bytes object of ASCII chars that approximate the brightness"""
    CHARS = b'#A@%$+=*:,. '
    if reverse_video:  CHARS = CHARS[::-1]
    CHARSLEN = len(CHARS)
    return img.point(lambda lvl: CHARS[int(lvl * CHARSLEN / 256.0)]).tobytes()

def asciipic(fn, ar, w, wob):
    """convert an image with filename FN to ascii with aspect ratio AR. The result
       will have width W, and display in white-on-black mode if WOB is True."""
    
    orig = Image.open(fn)
    term = convert_for_terminal(orig, w, ar)
    orig.close()
    termbytes = to_bytes(term, wob)

    for idx in range(0,len(termbytes), w):
        print(str(termbytes[idx:idx+w],encoding='utf-8'))

if __name__=='__main__':
  import argparse
  parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument("image", help="the target image to convert")
  parser.add_argument("--ar", type=float, default=2.0, help="aspect ratio of terminal chars (h:w)")
  parser.add_argument("--width", type=int, default=72, help="desired width in chars of the output")
  parser.add_argument("--wob", action="store_true", help="set if terminal is white chars on black background")
  args = parser.parse_args()

  asciipic(args.image, args.ar, args.width, args.wob)
