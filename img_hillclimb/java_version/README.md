# Hill-Climbing Image Maker

This is just a fun project to approximate an image via 
a series of random shapes (e.g., ellipses). The image is improved by
a 'hill-climbing' algorithm which keeps all shapes that
improve the image and discards all others. Over time, the
resemblance to the target image steadily improves.

This java version is threaded, and the threads "race" to see
who can make the most progress. Periodically, the all threads
start fresh from the winner of the previous "race."

## Options

<pre><code>
Non-option arguments:
[String] -- the source image

Option          Description
------          -----------
-?, -h, --help  print help
-d <String>     the shape to draw (default: filled_ellipse)
-e <Integer>    the number of tries each iteration (default: 3000)
-i <Integer>    the number of iterations to run (default: 100)
-j <Integer>    the number of concurrent jobs (default: 4)
-s <String>     start from this image
Available -d shape options are:
        downleft_line
        filled_rectangle
        filled_circle
        rectangle
        circle
        ellipse
        line
        filled_ellipse
        arc
</code></pre>

