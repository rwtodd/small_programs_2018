# Bishops Puzzle

I was watching a stream of someone playing
[the 7th Guest](https://en.wikipedia.org/wiki/The_7th_Guest)
and was intrigued by the Bishop chess puzzle.

Basically, you have 4 white bishops and 4 black bishops on the
first and last columns of a 4x5 chess board.  You have to
make normal bishop chess moves to switch their positions 
without ever putting a bishop in harm's way from the other side.
I don't think there was any prohibition against moving the
same side twice in a row, so I did not include one in my
programs.

Initial position (W = White bishop/B = Black bishop):

```
W---B
W---B
W---B
W---B
```

I wrote the python version first, mostly as functions on lists of lists. 
Then, I revised it into a more OO format.  Unsatisfied with the speed,
though, I re-coded it in Scala, which was about 7x faster on the given
problem.  As a bonus, the Scala version lets you choose different 
board sizes on the command-line.

A while later, I implemented the program in common lisp, which was 
faster (it solves the 4x6 puzzle in 2 seconds versus 7 in scala).

Shortly after that, I implemented the program in rust, which was even
faster (it solves the 4x6 puzzle in 0.6 seconds).

If you are curious: the program uses a basic breadth-first search of the
possibilities, and so is guaranteed to get the shortest solution, which
turns out to be 36 moves (searching 9,993 positions).  

It was interesting: a 4x7 board can be solved in only 24 moves
(searching 944,784 positions). On reflection, though, it makes 
sense that with more open spaces, you might not have to do as
much maneuvering. 

