# CGA-ify

This is just a fun utility to render an image in an old-school palette.  Even though it is called `cgaify`, 
the following palettes are supported:

 - __mono__: 1-bit black-and-white
 - __gray4__: 4 shades of gray
 - __gray16__: 16 shades of gray
 - __cga0__: Black, Green, Red, Brown
 - __cga0h__: High-Intensity Black, Green, Red, Yellow
 - __cga1__: Black, Cyan, Magenta, White
 - __cga1h__: High-Intensity Black, Cyan, Magenta, White
 - __ega__: 16-color standard EGA palette
 - __vga__: default 256-color VGA palette (actually more like 248-color)

 ## Usage

 <pre><code>
 usage: cgaify.py [-h] [-p {mono,gray4,cga0,cga0h,cga1,cga1h,ega,gray16,vga}]
                 src_file

positional arguments:
  src_file              the original file

optional arguments:
  -h, --help            show this help message and exit
  -p {mono,gray4,cga0,cga0h,cga1,cga1h,ega,gray16,vga}
                        the palette to use (default: cga0)
</code></pre>

## Other Versions

There is a Go version [on github](https://github.com/rwtodd/Go.Cgaify) which 
will also resize the image to an old-school resolution (e.g., 320x200).  I didn't carry 
forward the resizing because pixels are generally square these days, but not so in the 80's
and 90's.  So, the resizing was kind-of bogus anyway, and one can always pre-resize the
source image if wanted.

I also made a javaFX [dithertool](https://github.com/rwtodd/Java.DitherTool) which can dither
down to these classic palettes with several dithering algorithms, and also quantize to 
image-specific palettes.  