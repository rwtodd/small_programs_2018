# Asciipic

This is a fun utility that I've written in a number of languages now.
I just did a python version to get more familiar with PIL.  It takes
a graphical image file, and tries to reproduce it in low-res ascii on 
the terminal.

<pre><code>
With --chars ascii:
        ,,,,,,,,                                                               
       =*:$$$$$$+.                                  .=.                        
      .$=*++$$$$$:                              *.  .$,                        
  ,****=====$$$$$:.,,.        .,,.    .     .  ,$:. .$, ...     .,,.     .,,.  
 ,$$$$$$$$$$$$$$%::***,     :=,,,*+. ,$.   ,$. :$*, .$*:,:+*  :=:,,=*  *=,,,=+.
 :$$$$$$++++++++=,:::*:     +=    ++ ,$.   ,$. .$,  .$,   ,$ ,$,    $* +*   .$:
 :$$$$+::::::::::::::*:     +=    =+ ,$.   ,$. .$,  .$,   ,$ ,$.    += +*   .$,
 :%$$$,:*:::**********:     ==. .:$: .$*. .*$. .$:  .$,   ,$. ==. .*+. +*   .$:
  *+++,::::::::::::::,      ==,:::.   .:::,:$.  .:,  :.   .:   ,:::,   ,,    :.
       :::::::,,:.          +=             *=                                  
       :******,.:.          *:          ,:::                                   
        ,,,,,,,,.                                                           

With --chars blocks:
       ▒░▒▒▒▒▒▒▒░                                    ░
      ░▓░░▓▓▓▓▓▓▓                               ░   ░▒
   ░░░░▒▒▒▒▒▓▓▓▓▓                              ░▓   ░▒
 ░▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ ░░░░      ░░░░░▒░  ░▒    ░▒  ▒▒░  ░▒░░░░▒   ░▒░░░▒   ░░░░░▒░
 ▒▓▓▓▓▓▓▓▒▒▒▒▒▓▒▒░░░░░      ▓░    ▓░ ▒▒    ▒▒  ░▒   ░▒    ▒▒ ░▒    ░▓  ▓    ░▒
 ▒▓▓▓▓▒░░░░░░░░░░░░░░░      ▓░    ▓▒ ▒▒    ▒▒  ░▒   ░▒    ▒▒ ▒▒     ▓  ▓    ░▒
 ▒▓▓▓▒ ░░░░░░░░░░░░░░░      ▓░   ░▒  ░▒    ▒▒  ░▓   ░▒    ▒▒ ░▒░   ▒▒  ▓    ░▒
  ▒▒▒▒ ░░░░░░░░░░░░░░░      ▓░░░░░    ░░░░░▒▒   ░░   ░    ░░   ░░░░░   ░     ░
       ░░░░░░░░░░           ▓░             ▒░
       ░░░░░░░  ░           ▒           ░░░░

</code></pre>

I installed this utility as toys.y2018.asciipic.

<pre><code>
$ python3 -m toys.y2018.asciipic --help
usage: asciipic.py [-h] [--ar AR] [--width WIDTH] [--wob]
                   [--chars {ascii,blocks}]
                   image

positional arguments:
  image                 the target image to convert

optional arguments:
  -h, --help            show this help message and exit
  --ar AR               aspect ratio of terminal chars (h:w) (default: 2.0)
  --width WIDTH         desired width in chars of the output (default: 72)
  --wob                 set if terminal is white chars on black background
                        (default: False)
  --chars {ascii,blocks}
                        charset to use (default: ascii)
</code></pre>

