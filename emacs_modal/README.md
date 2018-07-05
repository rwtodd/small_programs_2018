# Modal Emacs

I know there are lots of 'vi'-emulations for emacs,
and aside from lisp/SLIME I am a vi user. However,
I prefer a more native emacs experience without any
cognitive dissonance on the key sequences.

So, my idea to overcome that is a modal minor mode for 
emacs that:

 - makes a raw key equivalent to `C-<key>`.
 - makes `C-<key>` equivalent to the raw key.
 - makes a shifted key equivalent to `M-<key>`.
 - is activated by `C-,`.

...with the following exceptions:

 - makes `m` acts as `<Esc>`, which makes `m <key>` equivalent to 
   the chord: `C-M-<key>`
 - makes typing shift-, (`<`) equivalent to `M-<` (instead of `M-,`).
 - makes typing shift-. (`>`) equivalent to `M->` (instead of `M-.`).
 - leaves the backspace key alone, so it works as normal when
   the mode is turned on.

So, you use all the normal emacs keys, without as much
chording.  Example: from the normal editing mode, to 
page down three times and move over two words:

    C-,3vFF,

That's `C-,` to activate the minor mode, `3v` to page down
three times, `FF` moves forward a word, and `,` toggles the
mode back off. 

This works great for lisp code, since many sexp-navigation 
commands take C-M-key chords.  In this minor mode, that's
`m k` instead of `C-M-k`.
