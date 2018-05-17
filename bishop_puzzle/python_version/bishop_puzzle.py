initial_board = [[1,0,0,0,2],[1,0,0,0,2],[1,0,0,0,2],[1,0,0,0,2]]

def print_board(board):
   for row in board:
      for p in row:
          if p == 0: print('-', end='')
          elif p == 1: print('W', end='')
          elif p == 2: print('B', end='')
          else: print('X', end='')
      print()
   print()

def hash_board(board):
   "hash the board, base 4"
   hash = 0
   for row in board:
      for val in row:
          hash = (hash << 2) | (val & 3)
   return hash

initial_board_hash = hash_board(initial_board)
winning_board_hash = hash_board([[2,0,0,0,1]]*4)

def do_moves(cx, cy, doit):
   x, y = max(cx-cy,0), max(cy-cx,0)
   len = min( 5 - x, 4 - y )
   for idx in range(len):   
       doit(x,y)
       x, y = x + 1, y + 1
   x, y = max(cx-3+cy, 0), min(cy+cx,3)
   len = min( 5 - x, y + 1 )
   for idx in range(len):
      if x != cx: doit(x,y)
      x, y = x + 1, y - 1

def attacked_board(board):
   """return a board marking all the attached squares"""
   ab = [[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]
   p = 0
   def set_ab(dx,dy):
       ab[dy][dx] |= p
   for y in range(4):
     for x in range(5):
        p = board[y][x]
        if p > 0:
           do_moves(x,y,set_ab)
   return ab

def attacked(ab, x, y, p):
   """ tells if a piece p is attacked at (x,y) """
   return (ab[y][x] & (3-p)) != 0

sign = lambda v: (1,-1)[v < 0]

def clear_path(board, x1, y1, x2, y2):
   """ tells if the path between (x1,y1) and (x2,y2) is clear """
   xdir, ydir = sign(x2-x1), sign(y2-y1)
   x,y = x1, y1
   while x != x2:
      x, y = x+xdir, y+ydir
      if board[y][x] != 0:  return False
   return True


desc_square = lambda x,y: "ABCDE"[x] + str(y+1) 

import copy
 
class Move:
  def __init__(self, prev, cur, cur_hash, desc):
     self.prev = prev  # previous move
     self.board = cur
     self.winning = cur_hash == winning_board_hash 
     self.desc = desc

def next_moves(prev, seen):
   ab = attacked_board(prev.board)
   x,y,p = 0,0,0
   moves = [] 
   def gen_move(x2,y2):
      if (not attacked(ab, x2, y2, p)) and clear_path(prev.board, x,y, x2, y2):
          b2 = copy.deepcopy(prev.board)
          b2[y][x], b2[y2][x2] = 0, p
          b2h = hash_board(b2)
          if b2h not in seen:
            seen.add(b2h)
            desc = f'{desc_square(x,y)} -> {desc_square(x2,y2)}'
            moves.append( Move(prev, b2, b2h, desc) )
   for y in range(4):
      for x in range(5):
          p = prev.board[y][x]
          if p != 0:
            do_moves(x,y,gen_move)
   return moves

def solve():
   seen = set()
   seen.add(initial_board_hash)
   backlog = [ Move(None, initial_board, initial_board_hash, '') ]
   iteration = 0
   winner = None
   while winner is None:
      iteration = iteration + 1
      backlog = [ nxt  for m in backlog  for nxt in next_moves(m,seen) ]
      print(f'{iteration}: Backlog is {len(backlog)} deep.')
      winner = next(filter(lambda m: m.winning, backlog), None)
   while winner:
      print_board(winner.board)
      print(winner.desc, end='\n\n')
      winner = winner.prev
   print(f'{len(seen)} boards considered.')
