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
            places: Array[Byte],
            val move: Board.Move)(implicit ss: SearchState) {

   private lazy val attacks = {
       val ap = Board.emptyPlaces
       for (idx <- 0 until places.size) {
          val p = places(idx)
          if (p > 0)  
             Board.bishopForEach(idx) { (idx2) =>
                 ap(idx2) = (ap(idx2) | p).toByte
             }
       }
       ap
   }

   val hash = {
      var h:BigInt = 0 
      for( p <- places ) {
          h = (h << 2) | p 
      }
      h
   }

   private def clearPath(idx1: Int, idx2: Int): Boolean = {
      val delta = Board.delta(idx1,idx2)
      if (delta == 0) return false // path to self!
      var i = idx1
      while (i != idx2) {
         i += delta
         if (places(i) != 0) return false
      }

      // lastly, check that we are not attacked on the final square
      val p = places(idx1)
      (attacks(idx2) & (3-p)) == 0
   }

   def tryMove(attempt: Board.Move) : Option[Board] = {
      val (idx1,idx2) = attempt
      if (clearPath(idx1,idx2)) {
         val np = places.clone
         np(idx2) = places(idx1)
         np(idx1) = 0
         Some(new Board(this, np, attempt))
      } else None
   }

   def nextMoves : ArrayBuffer[Board] = {
      val moves = ArrayBuffer[Board]()
      for (idx <- 0 until places.size) {
         if (places(idx) > 0)  
            Board.bishopForEach(idx) { (idx2) =>
                 tryMove(idx,idx2) foreach { nb =>
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
      val boardPic = places.map { _ match {
                                    case 0 => '-'
                                    case 1 => 'W'
                                    case 2 => 'B' 
                                    case 3 => 'X'
                                  } 
                    }.grouped(ss.cols).map(_.mkString).mkString("\n")
      val desc = if (move == null) "" 
                 else {
                     val x1 = move._1 % ss.cols
                     val y1 = move._1 / ss.cols + 1
                     val x2 = move._2 % ss.cols
                     val y2 = move._2 / ss.cols + 1
                     s"\n\n${letters(x1)}${y1} -> ${letters(x2)}${y2}"
                 }
      boardPic + desc
   }
}
             
object Board {
   type Move = Tuple2[Int,Int]  // IDX to IDX

   //def index(x:Int, y:Int)(implicit ss: SearchState): Int = 
   //   (y * ss.cols) + x

   def emptyPlaces(implicit ss: SearchState) = Array.ofDim[Byte](ss.rows*ss.cols)

   def startingBoard(implicit ss: SearchState) = {
       val pl = emptyPlaces
       for ( idx <- 0 until pl.size by ss.cols ) {
           pl(idx) = 1
       }
       for ( idx <- (ss.cols-1) until pl.size by ss.cols ) {
           pl(idx) = 2
       }
       new Board(null, pl, null)
   }

   def winningBoard(implicit ss: SearchState) = {
       val pl = emptyPlaces
       for ( idx <- 0 until pl.size by ss.cols ) {
           pl(idx) = 2
       }
       for ( idx <- (ss.cols-1) until pl.size by ss.cols ) {
           pl(idx) = 1
       }
       new Board(null, pl, null)
   }

   def delta(idx1: Int, idx2:Int) 
            (implicit ss: SearchState): Int = {
      val delta1 = ss.cols+1
      val delta2 = ss.cols-1
      val diff = idx2 - idx1 
      val sign = Math.signum(diff).toInt
      if ((diff % delta1) == 0) {
         sign * delta1
      } else {
         sign * delta2
      }
   }

   def bishopForEach(center: Int)(action: (Int)=>Unit)
                    (implicit ss: SearchState): Unit = {
      val cx = center %  ss.cols
      val cy = center / ss.cols

      var x = Math.max(cx-cy,0)
      var y = Math.max(cy-cx,0)
      var len = Math.min( ss.cols - x, ss.rows - y )
      var idx = x + (y*ss.cols)
      val delta1 = ss.cols + 1
      for (_ <- 1 to len) { 
          action(idx)
          idx += delta1
      }

      x = Math.max(cx-(ss.rows-1)+cy, 0)
      y = Math.min(cy+cx,(ss.rows-1))
      len = Math.min( ss.cols - x, y + 1 )
      idx = x + (y*ss.cols)
      val delta2 = ss.cols - 1
      for (_ <- 1 to len) {
         if (idx != center) action(idx)
         idx -= delta2
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