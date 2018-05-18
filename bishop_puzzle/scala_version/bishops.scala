package org.rwtodd.app.bishops 

import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer

class SearchState(val rows : Int, val cols : Int) {
   private val seenCache = Set[BigInt]()
   def seen(hash: BigInt) = seenCache contains hash
   def add(hash: BigInt) = seenCache add hash
   override def toString() = s"$rows by $cols Search (${seenCache.size} boards considered)"
}


class Board(val parent: Board, 
            places: Array[Array[Byte]],
            val move: Board.Move)(implicit ss: SearchState) {

   private lazy val attacks = {
       val ap = Board.emptyPlaces
       for (cy <- 0 until ss.rows;
            cx <- 0 until ss.cols) { 
          val p = places(cy)(cx)
          if (p > 0)  
             Board.bishopForEach(cx,cy) { (x,y) =>
                 ap(y)(x) = (ap(y)(x) | p).toByte
             }
       }
       ap
   }

   val hash = {
      var h:BigInt = 0 
      for( row <- places ; p  <- row ) {
          h = (h << 2) | p 
      }
      h
   }

   private def clearPath(x1: Int, y1: Int, x2: Int, y2: Int): Boolean = {
      val xdir = Math.signum(x2-x1).toInt
      if (xdir == 0) return false // path to self!
      val ydir = Math.signum(y2-y1).toInt

      var x = x1
      var y = y1
      while (x != x2) {
         x += xdir ; y += ydir
         if (places(y)(x) != 0) return false
      }

      // lastly, check that we are not attacked on the final square
      val p = places(y1)(x1)
      (attacks(y2)(x2) & (3-p)) == 0
   }

   def tryMove(attempt: Board.Move) : Option[Board] = {
      val (x1,y1,x2,y2) = attempt
      if (clearPath(x1,y1,x2,y2)) {
         val np = places.map(_.clone)
         np(y2)(x2) = places(y1)(x1)
         np(y1)(x1) = 0
         Some(new Board(this, np, attempt))
      } else None
   }

   def nextMoves : ArrayBuffer[Board] = {
      val moves = ArrayBuffer[Board]()
      for (cy <- 0 until ss.rows;
           cx <- 0 until ss.cols) { 
         if (places(cy)(cx) > 0)  
            Board.bishopForEach(cx,cy) { (x,y) =>
                 tryMove(cx,cy,x,y) foreach { nb =>
                      if (!ss.seen(nb.hash)) {
                         ss.add(nb.hash)
                         moves += nb
                      }
                 }
            }
      }
      moves 
   }

   override def toString() = {
      val letters = "ABCDEFGHIJKLMNO"
      val boardPic = places.map { row => 
                          row.map { _ match {
                                    case 0 => '-'
                                    case 1 => 'W'
                                    case 2 => 'B' 
                                    case 3 => 'X'
                                  } 
                          }.mkString("") 
                     }.mkString("\n")
      val desc = if (move == null) "" 
                 else s"\n\n${letters(move._1)}${move._2+1} -> ${letters(move._3)}${move._4+1}"
      boardPic + desc
   }
}
             
object Board {
   type Move = Tuple4[Int,Int,Int,Int]

   def emptyPlaces(implicit ss: SearchState) = Array.ofDim[Byte](ss.rows, ss.cols)

   def startingBoard(implicit ss: SearchState) = {
       val pl = emptyPlaces
       for( row <- pl ) {
           row(0) = 1
           row(row.length - 1) = 2
       }
       new Board(null, pl, null)
   }

   def winningBoard(implicit ss: SearchState) = {
       val pl = emptyPlaces
       for( row <- pl ) {
           row(0) = 2
           row(row.length - 1) = 1
       }
       new Board(null, pl, null)
   }

   def bishopForEach(cx: Int, cy: Int)(action: (Int,Int)=>Unit)
                    (implicit ss: SearchState): Unit = {
      var x = Math.max(cx-cy,0)
      var y = Math.max(cy-cx,0)
      var len = Math.min( ss.cols - x, ss.rows - y )
      for (_ <- 1 to len) {
          action(x,y) 
          x += 1 ; y += 1
      }
      x = Math.max(cx-(ss.rows-1)+cy, 0)
      y = Math.min(cy+cx,(ss.rows-1))
      len = Math.min( ss.cols - x, y + 1 )
      for (_ <- 1 to len) { 
         if (x != cx) action(x,y)
         x += 1 ; y -= 1
      }
   }

   def displayChain(chain : Board) : Unit = {
      var b = chain
      while(b != null) {
        println(b)
        println()
        b = b.parent
      }
   }
}


object Bishops {

  def main(cmdline: Array[String]) : Unit = {
    import org.rwtodd.argparse._
    var rows : Int = 0
    var cols : Int = 0

    lazy val argp: Args = new Args(
       new IntArg("-rows","<height> sets the number of rows (default 4)")
             .defaultsTo(4).does { rows = _ },
       new IntArg("-cols","<width> sets the number of columns (default 5)")
             .defaultsTo(5).does { cols = _ },
       new FlagArg("-help","shows this help message")
             .does { _ => argp.showHelp() }
    ) { 
       override def showHelp() = {
         println("Usage: bishops [options]") ; println()
         super.showHelp()
         System.exit(1)
       }
    }

    argp.parse(cmdline)
    println(s"Solving $rows by $cols...")
    println()

    implicit val ss = new SearchState(rows, cols)

    val winningHash = Board.winningBoard.hash
    var backlog = ArrayBuffer(Board.startingBoard)
    ss.add(backlog(0).hash)
    var winner : Option[Board] = None
    var iteration = 0
    while ((backlog.size > 0) && (winner == None)) {
        iteration += 1
        backlog = backlog.flatMap(_.nextMoves)
        println(s"$iteration: Backlog is ${backlog.size} deep.")
        winner = backlog.find { b => b.hash == winningHash }
    }

    winner.foreach(Board.displayChain)
    println(ss)
  }
}
