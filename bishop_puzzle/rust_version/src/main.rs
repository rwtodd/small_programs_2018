use std::collections::HashSet;
use std::cmp::{min,max};
use std::rc::Rc;

const ROWS : usize = 4;
const COLS : usize = 5;

type Board = Box<[u8; ROWS*COLS]>;

fn empty_board() -> Board {
   Box::new([0; ROWS*COLS])
}

/// Create a starting board from scratch
fn starting_board() -> Board {
   let mut b = empty_board();
   let mut idx = 0;
   let max = b.len();
   while idx < max {
      b[idx] = 1;
      b[idx+COLS-1] = 2;
      idx += COLS;
   } 
   b
}

fn invert_board(b : &mut Board) {
   for p in b.iter_mut() { if *p > 0 { *p = 3 - *p } };
}

/// Create a winning board by inverting a starting board
fn winning_board() -> Board {
   let mut answer = starting_board();
   invert_board(&mut answer);
   answer
}

/// Generates a hash for a given board
fn hash_board(b: &Board) -> u64 {
   b.iter().fold(0, |acc, v| (acc << 2) | (*v as u64))
}

/// Determine the write value to add to i, to take you
/// diagonally to j
fn bishop_delta(i: usize, j: usize) -> isize {
   let diff = j as isize - i as isize;
   let icols = COLS as isize;
   diff.signum() *  (icols + if diff % (icols+1) == 0 { 1 } else { -1 }) 
}

/// Display a board in ASCII
fn print_board(b : &Board) {
   for i in 0..ROWS {
      for j in 0..COLS {
         print!("{}", match b[i*COLS + j] {
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

fn bishop_for_each<F>(center: usize, mut action: F) 
  where F: FnMut(usize)  {
 
  let icols = COLS as isize;
  let cx = (center % COLS) as isize;
  let cy = (center / COLS) as isize;
  let last_row = (ROWS - 1) as isize;
  let last_col = (COLS - 1) as isize;
  let xc = last_col - cx;
  let yc = last_row - cy;
  let mut idx = (max(cx-cy,0) + icols*max(cy-cx,0)) as usize;
  let end1 = (min(cx+yc,last_col) + icols*min(cy+xc,last_row)) as usize;
  while idx <= end1 {
     if idx != center { action(idx); }  
     idx += COLS + 1;
  }
  idx = (min(cx+cy,last_col) + icols*max(cy-xc,0)) as usize;
  let end2 = (max(cx-yc,0) + icols*min(cx+cy,last_row)) as usize;
  while idx <= end2 {
     if idx != center { action(idx); }  
     idx += COLS - 1;
  }
}

fn attacked_spaces(b: &Board) -> Board {
  let mut answer: Board = b.clone();
  for i in 0..answer.len() {
     let p = b[i];
     if p > 0 {
        bishop_for_each(i,|idx| answer[idx] |= p);
     }
  }
  answer
}

struct Move {
   board : Board,
   parent : Option<Rc<Move>>,
   won : bool,
   b_move : (usize, usize),
}

impl Move {
  fn new(b: Board, p: Option<Rc<Move>>, w: bool, mv: (usize,usize) ) -> Move {
    Move {
      board: b,
      parent: p,
      won: w,
      b_move: mv,
    }
  }
}

fn print_move_list(mv: &Rc<Move>) {
  let square = |i| {
    let letters = ['A','B','C','D','E','F','G','H','I'];
    let row = i % COLS;
    let col = i / COLS + 1;
    format!("{}{}", letters[row], col)
  };
  print_board(&mv.board);
  if mv.parent.is_some() {
     println!("{} to {}",square(mv.b_move.0),square(mv.b_move.1)); 
     println!();
     print_move_list(mv.parent.as_ref().unwrap());
  }
}

struct Game {
   seen : HashSet<u64>,
   winning_hash : u64,
}

impl Game {
  fn new() -> Game {
     Game {  
        seen: HashSet::new(),
        winning_hash: hash_board(&winning_board())
     }
  }

  /// put a board in the seen hash, and return if it's new, and
  /// if it is a winning board  
  fn inspect_board(self: &mut Self, b: &Board) -> (bool, bool) {
     let bh = hash_board(b);
     (self.seen.insert(bh), bh == self.winning_hash)
  }

  /// generate all possible next moves, inspect them, and push
  /// them onto the res vector
  fn push_next_moves(self: &mut Self, m: &Rc<Move>, res: &mut Vec<Rc<Move>>) {
     let b = &m.board;
     let attacked = attacked_spaces(b);
     let clear_path = |p,i,j| {
        if ((3 - p) & attacked[j]) != 0 { return false; }
        let delta = bishop_delta(j,i);
        let mut x = j;
        while x != i {
           if b[x] != 0 { return false; }
           x = ((x as isize) + delta) as usize;
        } 
        return true;
     };
     for i in 0..b.len() {
        let p = b[i];
        if p > 0 {
           bishop_for_each(i, |j| {
              if clear_path(p,i,j) {
                 let mut nb: Board = b.clone();
                 nb[j] = p;
                 nb[i] = 0;
                 let (newb, won) = self.inspect_board(&nb);
                 if newb {
                     res.push(Rc::new(Move::new(nb, Some(m.clone()), won, (i,j))));
                 }
              }
           });
        }
     }
  }
}

fn main() {
   let mut backlog = vec![Rc::new(Move::new(starting_board(), None, false, (0,0)))];
   let mut moves = 0;
   let mut game = Game::new();
   while backlog.len() > 0 {
      let mut next_moves = Vec::new();
      for mv in backlog.iter() {
         game.push_next_moves(&mv,&mut next_moves);
      }
      backlog = next_moves; 
      moves += 1;
      println!("Move {}: Moves to consider: {}", moves, backlog.len());
      for mv in backlog.iter() {
        if mv.won {
           print_move_list(&mv);
           return; 
        }
      }
   }   
}

