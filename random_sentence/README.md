# Simple Sentence Generator

I ran across Peter Norvig's page [comparing lisp and python][1],
and wanted to see how a scala version would look.  Then, for extra
fun, I made a Rust version and a c++ version (the latter doesn't do
the tree part--only sentence gen).

I think they turned out
pretty well.  In scala and Rust, the biggest difference is that I have to define
what I mean by "Tree" for the type system's benefit.  

In scala and c++, I was easily able to approximate the grammar definition syntax
provided by Norvig's idiomatic python example:

Python:

```python
grammar = Grammar(
  S   = 'NP VP',
  NP  = 'Art N',
  VP  = 'V NP',
  Art = 'the | a',
  N   = 'man | ball | woman | table',
  V   = 'hit | took | saw | liked'
  )
```
Scala:

```scala
val grammar = new Grammar(
     'S   -> "NP VP",
     'NP  -> "Art N",
     'VP  -> "V NP",
     'Art -> "the | a",
     'N   -> "man | ball | woman | table",
     'V   -> "hit | took | saw | liked")
```

C++:

```cpp
grammar g {
    { "S"   , { "NP VP" } },
    { "NP"  , { "Art N" } },
    { "VP"  , { "V NP" } },
    { "Art" , { "the", "a" } },
    { "N"   , { "man", "ball", "woman", "table" } },
    { "V"   , { "hit", "took", "saw", "liked" } }
};
```

... and in Rust I know it's possible to make things nicer
with macros, but as of this writing I'm more comfortable just
using plain calls:

```rust
let mut grammar = HashMap::new();
grammar.insert("S"   , vec!["NP VP"]);
grammar.insert("NP"  , vec!["Art N"]);
grammar.insert("VP"  , vec!["V NP"]);
grammar.insert("Art" , vec!["the","a"]);
grammar.insert("N"   , vec!["man","ball","woman","table"]);
grammar.insert("V"   , vec!["hit","took","saw","liked"]);
```

## On the Trees

The scala and Rust versions implement the tree function for comparison
with the original. Maybe Python2 was different, but when I run
the python tree generator in Python 3.6, it just tells me
about a `map object`.  However, scala has printable `case class`es, and
Rust can `#[derive(Debug)]`, so I get a readable output for the tree.

[1]: http://norvig.com/python-lisp.html
