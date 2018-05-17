sign = lambda v: (1,-1)[v < 0]
import copy

class Board():
   ROWS = 4
   COLS = 5
   
   @staticmethod
   def empty_places():
       result = []
       for _ in range(Board.ROWS):
          result.append([0]*Board.COLS)
       return result

   def __init__(self, places, prev=None, move=None):
       self.places = places
       self.prev = prev
       self.move = move
       self.hash_code = Board.compute_hash(places)

   def compute_attacks(self):
       ap = Board.empty_places()
       p = 0
       def set_ap(dx,dy):
          ap[dy][dx] |= p
       for y in range(Board.ROWS):
         for x in range(Board.COLS):
           p = self.places[y][x]
           if p > 0:
             Board.bishop_foreach(x,y,set_ap)
       self.attacked_places = ap

   def __hash__(self):
       return self.hash_code

   def __eq__(self, other):
       return self.hash_code == other.hash_code

   def display(self):
       Board.print(self.places)
       if self.move:
          x1,y1,x2,y2 = self.move
          print(f'{Board.desc_square(x1,y1)} -> {Board.desc_square(x2,y2)}', end='\n\n')

   def attacked(self, x, y, p):
      """ tells if a piece p is attacked at (x,y) """
      return (self.attacked_places[y][x] & (3-p)) != 0

   def try_move(self, x1, y1, x2, y2):
      """returns a new board if the move is legal"""
      p = self.places[y1][x1]
      if (not self.attacked(x2, y2, p)) and self.clear_path(x1,y1,x2,y2):
          places = copy.deepcopy(self.places)
          places[y1][x1], places[y2][x2] = 0, p
          return Board(places,self,(x1,y1,x2,y2))
      return None

   def clear_path(self, x1, y1, x2, y2):
      """ tells if the path between (x1,y1) and (x2,y2) is clear """
      xdir, ydir = sign(x2-x1), sign(y2-y1)
      x,y = x1, y1
      while x != x2:
         x, y = x+xdir, y+ydir
         if self.places[y][x] != 0:  return False
      return True

   @staticmethod
   def desc_square(x,y):
      return "ABCDEFGHIJKL"[x] + str(y+1) 

   @staticmethod
   def print(places):
     for row in places:
        for p in row:
            if p == 0: print('-', end='')
            elif p == 1: print('W', end='')
            elif p == 2: print('B', end='')
            else: print('X', end='')
        print()
     print()

   @staticmethod 
   def compute_hash(places):
     hash = 0
     for row in places:
        for val in row:
            hash = (hash << 2) | (val & 3)
     return hash

   @staticmethod
   def bishop_foreach(cx, cy, doit):
      x, y = max(cx-cy,0), max(cy-cx,0)
      len = min( Board.COLS - x, Board.ROWS - y )
      for idx in range(len):   
          doit(x,y)
          x, y = x + 1, y + 1
      x, y = max(cx-(Board.ROWS-1)+cy, 0), min(cy+cx,(Board.ROWS-1))
      len = min( Board.COLS - x, y + 1 )
      for idx in range(len):
         if x != cx: doit(x,y)
         x, y = x + 1, y - 1

def next_moves(board, seen):
   x,y,p = 0,0,0
   moves = [] 
   board.compute_attacks()
   def gen_move(x2,y2):
      nb = board.try_move(x,y,x2,y2)
      if (nb is not None) and (nb.hash_code not in seen):
         seen.add(nb.hash_code)
         moves.append(nb)
   for y in range(Board.ROWS):
      for x in range(Board.COLS):
          p = board.places[y][x]
          if p != 0:
            Board.bishop_foreach(x,y,gen_move)
   return moves
 

def make_initial_places():
   pl = Board.empty_places()
   for y in range(Board.ROWS):
      pl[y][0]  = 1
      pl[y][-1] = 2 
   return pl

def make_winning_places():
   pl = Board.empty_places()
   for y in range(Board.ROWS):
      pl[y][0]  = 2
      pl[y][-1] = 1 
   return pl

def solve():
   initial_board = Board(make_initial_places())
   winning_board = Board(make_winning_places())

   seen = set()
   seen.add(initial_board.hash_code)

   backlog = [ initial_board ]
   iteration = 0
   winner = None
   while (winner is None) and (len(backlog) > 0): 
      iteration = iteration + 1
      backlog = [ nxt  for m in backlog  for nxt in next_moves(m,seen) ]
      print(f'{iteration}: Backlog is {len(backlog)} deep.')
      winner = next(filter(lambda m: m == winning_board, backlog), None)
   while winner:
      winner.display()
      winner = winner.prev
   print(f'{len(seen)} boards considered.')

if __name__ == '__main__':
   solve()
