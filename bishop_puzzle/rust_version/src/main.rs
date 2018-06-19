use std::collections::HashSet;
use std::cmp::{min,max};
use std::rc::Rc;
use std::env::args;

type Board = Box<[u8]>;

#[derive(Copy,Clone)]
struct Dimensions { 
  rows: usize, 
  cols: usize, 
}

fn bishop_for_each<F>(dim : &Dimensions, center: usize, mut action: F) 
  where F: FnMut(usize)  {
 
  let icols = dim.cols as isize;
  let cx = (center % dim.cols) as isize;
  let cy = (center / dim.cols) as isize;
  let last_row = (dim.rows - 1) as isize;
  let last_col = (dim.cols - 1) as isize;
  let xc = last_col - cx;
  let yc = last_row - cy;
  let mut idx = (max(cx-cy,0) + icols*max(cy-cx,0)) as usize;
  let end1 = (min(cx+yc,last_col) + icols*min(cy+xc,last_row)) as usize;
  while idx <= end1 {
     if idx != center { action(idx); }  
     idx += dim.cols + 1;
  }
  idx = (min(cx+cy,last_col) + icols*max(cy-xc,0)) as usize;
  let end2 = (max(cx-yc,0) + icols*min(cx+cy,last_row)) as usize;
  while idx <= end2 {
     if idx != center { action(idx); }  
     idx += dim.cols - 1;
  }
}

fn attacked_spaces(dim: &Dimensions, b: &Board) -> Board {
  let mut answer: Board = b.clone();
  for i in 0..answer.len() {
     let p = b[i];
     if p > 0 {
        bishop_for_each(dim, i,|idx| answer[idx] |= p);
     }
  }
  answer
}

/// Generates a hash for a given board
fn hash_board(b: &Board) -> u64 {
   b.iter().fold(0, |acc, v| (acc << 2) | (*v as u64))
}


/// Determine the write value to add to i, to take you
/// diagonally to j
fn bishop_delta(dim: &Dimensions, i: usize, j: usize) -> isize {
   let diff = j as isize - i as isize;
   let icols = dim.cols as isize;
   diff.signum() *  (icols + if diff % (icols+1) == 0 { 1 } else { -1 }) 
}

/// Display a board in ASCII
fn print_board(dim: &Dimensions, b : &Board) {
   for i in 0..dim.rows {
      for j in 0..dim.cols {
         print!("{}", match b[i*dim.cols + j] {
                         0 => "-",
                         1 => "W",
                         2 => "B",
                         _ => "X",
                      });
      }
      println!();
   }
   println!();
}


struct Move {
   board : Board,
   parent : Option<Rc<Move>>,
   hash : u64,
   b_move : (usize, usize)
}

impl Move {
  fn new(b: Board, p: Option<Rc<Move>>, mv: (usize,usize) ) -> Move {
    let bhash = hash_board(&b);
    Move {
      board: b,
      parent: p,
      hash: bhash, 
      b_move: mv,
    }
  }

}


fn print_move_list(dim: &Dimensions, mv: &Rc<Move>) {
  let square = |i| {
    let letters = ['A','B','C','D','E','F','G','H','I'];
    let row = i % dim.cols;
    let col = i / dim.cols + 1;
    format!("{}{}", letters[row], col)
  };
  print_board(dim, &mv.board);
  if mv.parent.is_some() {
     println!("{} to {}",square(mv.b_move.0),square(mv.b_move.1)); 
     println!();
     print_move_list(dim, mv.parent.as_ref().unwrap());
  }
}

struct Game {
   seen : HashSet<u64>,
   dimensions : Dimensions,
}

impl Game {
  fn new(rows: usize, cols: usize) -> Game {
     Game {  
        seen: HashSet::new(),
        dimensions: Dimensions { rows: rows, cols: cols },
     }
  }

  /// Create a starting move
  fn starting_move(self: &mut Self) -> Move {
   let mut b = vec![0u8;self.dimensions.rows*self.dimensions.cols]
                    .into_boxed_slice(); 
   let mut idx = 0;
   let max = b.len();
   while idx < max {
      b[idx] = 1;
      b[idx+self.dimensions.cols-1] = 2;
      idx += self.dimensions.cols;
   } 
   let m = Move::new(b, None, (0,0));
   self.seen.insert(m.hash);
   m 
  }

  fn winning_hash(self: &mut Self) -> u64 {
     let mut sm = self.starting_move();
     for p in sm.board.iter_mut() { if *p > 0 { *p = 3 - *p } };
     hash_board(&sm.board)
  }


  /// generate all possible next moves, inspect them, and push
  /// them onto the res vector
  fn push_next_moves(self: &mut Self, m: &Rc<Move>, res: &mut Vec<Rc<Move>>) {
     let b = &m.board;
     let dim = self.dimensions;
     let attacked = attacked_spaces(&dim, b);
     let mut try_move = |p,i,j| {
        if ((3 - p) & attacked[j]) != 0 { return; }
        let delta = bishop_delta(&dim,j,i);
        let mut x = j;
        while x != i {
           if b[x] != 0 { return ; }
           x = ((x as isize) + delta) as usize;
        } 
        let mut nb = b.clone(); 
        nb[j] = p;
        nb[i] = 0;
        let nm = Move::new(nb, Some(m.clone()), (i,j));
        if self.seen.insert(nm.hash) {
            res.push(Rc::new(nm))
        } 
     };
     for i in 0..b.len() {
        let p = b[i];
        if p > 0 {
           bishop_for_each(&dim, i, |j| try_move(p,i,j));
        }
     }
  }
}

fn main() {
   let mut args = args();
   let rows = args.nth(1).unwrap_or_else(|| String::from("4"))
                  .parse::<usize>().unwrap_or(4);
   let cols = args.next().unwrap_or_else(|| String::from("5"))
                  .parse::<usize>().unwrap_or(5);
   println!("Solving {} by {}...", rows, cols);
   let mut game = Game::new(rows, cols);
   let mut backlog = vec![Rc::new(game.starting_move())];
   let mut moves = 0;
   let winning_hash = game.winning_hash();
   while backlog.len() > 0 {
      let mut next_moves = Vec::new();
      for mv in backlog.iter() {
         if mv.hash == winning_hash {
            print_move_list(&game.dimensions, &mv);
            return;
         }
         game.push_next_moves(&mv,&mut next_moves);
      }
      backlog = next_moves; 
      moves += 1;
      println!("Move {}: Moves to consider: {}", moves, backlog.len());
   }   
}

