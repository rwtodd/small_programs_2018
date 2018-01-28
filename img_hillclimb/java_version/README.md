# Hill-Climbing Image Maker

This is just a fun project to approximate an image via 
a series of random ellipses. The image is improved by
a 'hill-climbing' algorithm which keeps all ellipses that
improve the image and discards all others. Over time, the
resemblance to the target image steadily improves.

This java version is threaded, and the threads "race" to see
who can make the most progress. Periodically, the all threads
start fresh from the winner of the previous "race."

