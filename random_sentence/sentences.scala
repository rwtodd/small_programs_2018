package org.rwtodd.app.sentences

import scala.util.Random

class Grammar(pairs: (Symbol,String)*) {
   val g = Map(pairs.map { case (k,v) => 
                  (k, v.split('|')
                       .map(_.trim.split(' ').map(Symbol(_)))
                  ) 
               }:_*
              )

   // Replace symbol with a random entry in g (recursively); 
   // join into a string.
   def generate(sym: Symbol='S): String = 
      if (!(g contains sym))
          sym.name
      else {
          val rhs = g(sym)
          rhs(Random.nextInt(rhs.size)).map(generate).mkString(" ")
      }

   // Unlike in python, we have to be specific about what
   // our generated tree can contain
   sealed class Tree
   case class Terminal(str: String) extends Tree
   case class NonTerminal(cat: String, expansion: Seq[Tree]) extends Tree

   // Replace symbol with a random entry in g (recursively); 
   // return a tree.
   def generateTree(sym: Symbol='S): Tree = 
      if (!(g contains sym))
          Terminal(sym.name)
      else {
          val rhs = g(sym)
          val chosen = rhs(Random.nextInt(rhs.size)).toList
          NonTerminal(sym.name, chosen.map(generateTree))
      }
}

object Main extends App {

   val grammar = new Grammar(
        'S   -> "NP VP",
        'NP  -> "Art N",
        'VP  -> "V NP",
        'Art -> "the | a",
        'N   -> "man | ball | woman | table",
        'V   -> "hit | took | saw | liked")
   
   // one advantage we have over the python version is, 
   // thanks to case classes and Lists, the tree prints 
   // nicely.  In python3.6, I got 
   // {'S': <map object at 0x000001A0C45F9780>}
   println(grammar.generate()) ; println()
   println(grammar.generateTree()) ; println()
}
