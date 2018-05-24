# Simple Sentence Generator

I ran across Peter Norvig's page [comparing lisp and python][1],
and wanted to see how a scala version would look.  Then, for extra
fun, I made a c++ version (non-tree only).

I think they turned out
pretty well.  In scala, the biggest difference is that I have to define
what I mean by "Tree" for the type system's benefit.  Otherwise,
I was easily able to match the grammar definition syntax 
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

... and C++ only fared slightly worse:

```c++
vector<rule> grammar { 
    { "S"    , { "NP VP", "S and S" } },
    { "NP"   , { "Art N", "Name" } },
    { "VP"   , { "V NP" } },
    { "Art"  , { "the", "a", "every", "some" } },
    { "N"    , { "man", "ball", "woman", "table", "dog", "cat", "wombat" } },
    { "V"    , { "hit", "took", "saw", "liked", "worshiped", "remembered" } },
    { "Name" , { "Alice", "Bob", "Carlos", "Dan", "Eve" } }
};
```

Also, I should mention that when I run the python tree
generator in Python 3.6, it just tells me about a `map object`,
but thanks to scala's case classes and Lists I get a readable
output for the tree.

[1]: http://norvig.com/python-lisp.html
