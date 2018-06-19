package org.rwtodd.app.bishops 

import scala.collection.mutable.Set
import scala.collection.mutable.ArrayBuffer

class GameContext(val rows : Int, val cols : Int) {
   private val seenCache = Set[Long]()
   def seen(hash: Long) = seenCache contains hash
   def add(hash: Long) = seenCache add hash
   override def toString() = s"$rows by $cols Search (${seenCache.size} boards considered)"
}

class Board(val parent: Board, 
            places: Array[Byte],
            val move: Board.Move)(implicit ctx: GameContext) {

   private lazy val attacks = {
       val ap = Board.emptyPlaces
       for (idx <- places.indices) {
          val p = places(idx)
          if (p > 0)  
             ap(idx) = (ap(idx) | p).toByte
             Board.bishopForEach(idx) { (idx2) =>
                 ap(idx2) = (ap(idx2) | p).toByte
             }
       }
       ap
   }

   val hash = places.foldLeft(0L) { (h,p) => (h << 2) | p }

   private def clearPath(idx1: Int, idx2: Int): Boolean = 
      ((3-places(idx1) & attacks(idx2)) == 0) &&
      (idx2 until idx1 by Board.delta(idx2,idx1)).forall(places(_) == 0) 

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
      for (idx <- places.indices if places(idx) > 0) {
          Board.bishopForEach(idx) { (idx2) =>
               tryMove(idx,idx2) foreach { nb =>
                    if (!ctx.seen(nb.hash)) {
                       ctx.add(nb.hash)
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
                    }.grouped(ctx.cols).map(_.mkString).mkString("\n")
      val desc = if (move == null) "" 
                 else {
                     val x1 = move._1 % ctx.cols
                     val y1 = move._1 / ctx.cols + 1
                     val x2 = move._2 % ctx.cols
                     val y2 = move._2 / ctx.cols + 1
                     s"\n\n${letters(x1)}${y1} -> ${letters(x2)}${y2}"
                 }
      boardPic + desc
   }
}
             
object Board {
   type Move = Tuple2[Int,Int]  // IDX to IDX

   def emptyPlaces(implicit ctx: GameContext) = Array.ofDim[Byte](ctx.rows*ctx.cols)

   def startingBoard(implicit ctx: GameContext) = {
       val pl = emptyPlaces
       for ( idx <- 0 until pl.size by ctx.cols ) {
           pl(idx) = 1
           pl(idx + ctx.cols - 1) = 2
       }
       new Board(null, pl, null)
   }

   def winningBoard(implicit ctx: GameContext) = {
       val pl = emptyPlaces
       for ( idx <- 0 until pl.size by ctx.cols ) {
           pl(idx) = 2
           pl(idx + ctx.cols - 1) = 1
       }
       new Board(null, pl, null)
   }

   def delta(idx1: Int, idx2:Int) 
            (implicit ctx: GameContext): Int = {
      val sign = if (idx2 > idx1) 1 else -1
      val downright = ctx.cols + 1
      if ((idx2 - idx1) % downright == 0)
          sign*downright
      else
          sign*(ctx.cols-1) 
   }

   def bishopForEach(center: Int)(action: (Int)=>Unit)
                    (implicit ctx: GameContext): Unit = {
      val cx = center %  ctx.cols
      val cy = center / ctx.cols
      val lastrow = ctx.rows - 1
      val lastcol = ctx.cols - 1
      val xc = lastcol - cx
      val yc = lastrow - cy

      val idx1 = Math.max(cx-cy,0) + ctx.cols * Math.max(cy-cx,0)
      val idx2 = Math.min(cx+yc,lastcol) + ctx.cols * Math.min(cy+xc,lastrow)
      for (idx <- idx1 to idx2 by ctx.cols+1 if idx != center) action(idx)

      val idx3 = Math.min(cx+cy, lastcol) + ctx.cols * Math.max(cy-xc,0)
      val idx4 = Math.max(cx-yc, 0) + ctx.cols * Math.min(cy+cx,lastrow)
      for (idx <- idx3 to idx4 by ctx.cols-1 if idx != center) action(idx)
   }

   def displayChain(chain : Board) : Unit = 
      Stream.iterate(chain)(_.parent).takeWhile(_ != null).foreach { b =>
        println(b) ; println()
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

    implicit val ctx = new GameContext(rows, cols)

    val winningHash = Board.winningBoard.hash
    var backlog = ArrayBuffer(Board.startingBoard)
    ctx.add(backlog(0).hash)
    var winner : Option[Board] = None
    var iteration = 0
    while ((backlog.size > 0) && (winner == None)) {
        iteration += 1
        backlog = backlog.flatMap(_.nextMoves)
        println(s"$iteration: Backlog is ${backlog.size} deep.")
        winner = backlog.find { b => b.hash == winningHash }
    }

    winner.foreach(Board.displayChain)
    println(ctx)
  }
}
