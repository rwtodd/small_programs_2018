import itertools
import struct

class _Token:
    """A class to to hold (number,description), with some classmethods to help build them"""
    def __init__(self, num, desc):
        self.num = num
        self.desc = desc

    @classmethod
    def from_number(cls, num, base):
        if base == 16:
            desc = f'&H{num:X}'
        elif base == 8:
            desc = f'&O{num:o}'
        else:
            desc = str(num)
        return cls(-1, desc) 

    @classmethod
    def from_float(cls, num):
        return cls(-1, f'{num:E}') 

    @classmethod
    def from_string(cls, num, desc):
        return cls(num, desc)

    @classmethod
    def from_opcode(cls, tok):
        desc = _opcodes.get(tok)
        if desc is None:
            desc = f'<UNK 0x{tok:X}>'
        return cls(tok, desc)
       

class _Reader:
    """A class to read various binary data types from the binary file F.""" 
    def __init__(self,f):
        self.f = f
        self.s16  = struct.Struct('<h')
        self.su16 = struct.Struct('<H')
        self.sf32 = struct.Struct('<f')
        self.sf64 = struct.Struct('<d')
   
    def read(self, n):
        """Read _exactly_ N bytes from the file, or throw an exception"""
        data = self.f.read(n) 
        n -= len(data)
        while n > 0:
            nxt = self.f.read(n)
            amt = len(nxt)
            if amt == 0: raise IOError('Early EOF!')
            data += nxt
            n -= amt
        return data 

    def read_u8(self):
        return self.read(1)[0]

    def read_16(self):
        return self.s16.unpack(self.read(2))[0]

    def read_u16(self):
        return self.su16.unpack(self.read(2))[0]

    def read_f32(self):
        bs = bytearray(self.read(4))
        if bs[3] == 0:  return 0.0
        sgn, exp = bs[2] & 0x80, (bs[3] - 2) & 0xff
        bs[3] = sgn | (exp >> 1)
        bs[2] = ((exp << 7) | (bs[2] & 0x7f)) & 0xff
        return self.sf32.unpack(bs)[0]
      
    def read_f64(self):
        bs = bytearray(self.read(8))
        if bs[7] == 0: return 0.0
        sgn = bs[6] & 0x80 
        exp = (bs[3] - 128 - 1 + 1023) & 0xffff
        bs[7] = sgn | ((exp >> 4) & 0xff)
        leftOver = (exp << 4) & 0xff
        for idx in range(6, 0, -1):
            tmp = ((bs[idx] << 1) & 0xff) | (bs[idx-1] >> 7)
            bs[idx] = leftOver | (tmp >> 4)
            leftOver = (tmp << 4) & 0xff
        tmp = (bs[0] << 1) & 0xff
        bs[0] = leftOver | (tmp >> 4)
        return self.sf64.unpack(bs)[0]

# A decrypter for protected BAS files.
# I found the algorithm in a python program ("PC-BASIC"),
#    (  http://sourceforge.net/p/pcbasic/wiki/Home/  )
# ... but the algorithm was published in:
# The Cryptogram computer supplement #19, American Cryptogram Association, Summer 1994
#
# Basically there is a 13-byte and an 11-byte key, which are BOTH applied
# in a cycle across the bytes of the input.  Also, a reversed 11-index is subtracted
# from the byte, while a reversed 13-index is added to the byte. By 'reversed', I
# mean that as the 11-index goes from 0 to 10, the reversed index goes from 11 to 1.
class _Decryptor(_Reader):
    """A _Reader that decrypts data as it goes."""
    key13 = [ 0xA9, 0x84, 0x8D, 0xCD, 0x75, 0x83, 
              0x43, 0x63, 0x24, 0x83, 0x19, 0xF7, 0x9A ]
    key11 = [ 0x1E, 0x1D, 0xC4, 0x77, 0x26, 
              0x97, 0xE0, 0x74, 0x59, 0x88, 0x7C ]
    def __init__(self, f):
        super(_Decryptor,self).__init__(f)
        self.idx11 = 0 
        self.idx13 = 0
    def decrypt(self, b):
        for idx in range(len(b)):
            b[idx] = self.decrypt1(b[idx])
        return b
    def decrypt1(self, b):
        ans = b - (11 - self.idx11)
        ans = ans ^ self.key11[self.idx11] ^ self.key13[self.idx13]
        ans += (13 - self.idx13)
        self.idx11, self.idx13 = (self.idx11+1) % 11, (self.idx13+1) % 13
        return (ans & 0xFF)
    def read(self, n):
        data = super(_Decryptor, self).read(n)
        return self.decrypt(bytearray(data))
        
def _get_reader(f):
    """Determines the right reader object for the file f"""
    first_byte = f.read(1)[0]
    if first_byte == 0xFF:
        return _Reader(f)
    elif first_byte == 0xFE:
        return _Decryptor(f)
    else:
        raise ValueError(f'Not a GW-BASIC file! (first byte was 0x{first_byte:X})')

def _get_token(rdr):
    """Read and create the next _Token from the file"""
    nxt = rdr.read_u8()
    if nxt >= 0x20 and nxt <= 0x7E:
        return _Token.from_string(nxt,chr(nxt))
    elif nxt >= 0xFD and nxt <= 0xFF:
        return _Token.from_opcode( (nxt << 8) | rdr.read_u8() )
    elif nxt == 0x0E:
        return _Token.from_number(rdr.read_u16(), 10)
    elif nxt == 0x0B:
        return _Token.from_number(rdr.read_16(), 8)
    elif nxt == 0x0C:
        return _Token.from_number(rdr.read_16(), 16)
    elif nxt == 0x1C:
        return _Token.from_number(rdr.read_16(), 10)
    elif nxt == 0x0F:
        return _Token.from_number(rdr.read_u8(), 10)
    elif nxt == 0x1D:
        return _Token.from_float(rdr.read_f32())
    elif nxt == 0x1F:
        return _Token.from_float(rdr.read_f64())
    else:
        return _Token.from_opcode(nxt) 

def _tokens(rdr):
    """a generator that reads tokens forever""" 
    while True: yield _get_token(rdr)

def _get_line(rdr):
    """Get a line of tokens out of the file. Return the empty list
       when we are at the end of the file."""
    if rdr.read_u16() == 0: return []
    prefix = [ _Token.from_number(rdr.read_u16(),10), 
               _Token.from_string(-1,'  ') ]
    line = itertools.takewhile(lambda t: t.num != 0, _tokens(rdr))
    return prefix + list(line)

def _format(line):
    """Convert a list of tokens into a string"""
    idx, end, filtered = 0, len(line), []
    def looking_at(*args):
        alen = len(args)
        return ((end - idx) >= alen and all(args[i] == line[idx+i].num for i in range(alen)))
    while idx < end:
        # 3A A1     --> A1   ":ELSE"  --> "ELSE"
        # 3A 8F D9  --> D9   ":REM'"  --> "'" 
        # B1 E9     --> B1   "WHILE+" --> "WHILE"
        if looking_at(0x3A, 0xA1): idx = idx + 1 
        elif looking_at(0x3A,0x8F,0xD9): idx = idx + 2
        elif looking_at(0xB1,0xE9):
            line[idx+1] = line[idx]
            idx = idx + 1
        filtered.append(line[idx].desc)
        idx = idx + 1
    return ''.join(filtered) 

def file_lines(f):
    """Generate the GW-BASIC lines from open file f"""
    rdr = _get_reader(f)
    while True:
        line = _get_line(rdr)
        if len(line) == 0:
            break
        yield _format(line)
    
def gwbas_lines(fn):
    """Create a generator for the lines of a GW-BASIC file."""
    with open(fn,'rb') as f:
        yield from file_lines(f)

_opcodes = {
    0x00: "EOL",
    0x11: "0",
    0x12: "1",
    0x13: "2",
    0x14: "3",
    0x15: "4",
    0x16: "5",
    0x17: "6",
    0x18: "7",
    0x19: "8",
    0x1A: "9",
    0x1B: "10",
    0x81: "END",
    0x82: "FOR",
    0x83: "NEXT",
    0x84: "DATA",
    0x85: "INPUT",
    0x86: "DIM",
    0x87: "READ",
    0x88: "LET",
    0x89: "GOTO",
    0x8A: "RUN",
    0x8B: "IF",
    0x8C: "RESTORE",
    0x8D: "GOSUB",
    0x8E: "RETURN",
    0x8F: "REM",
    0x90: "STOP",
    0x91: "PRINT",
    0x92: "CLEAR",
    0x93: "LIST",
    0x94: "NEW",
    0x95: "ON",
    0x96: "WAIT",
    0x97: "DEF",
    0x98: "POKE",
    0x99: "CONT",
    0x9C: "OUT",
    0x9D: "LPRINT",
    0x9E: "LLIST",
    0xA0: "WIDTH",
    0xA1: "ELSE",
    0xA2: "TRON",
    0xA3: "TROFF",
    0xA4: "SWAP",
    0xA5: "ERASE",
    0xA6: "EDIT",
    0xA7: "ERROR",
    0xA8: "RESUME",
    0xA9: "DELETE",
    0xAA: "AUTO",
    0xAB: "RENUM",
    0xAC: "DEFSTR",
    0xAD: "DEFINT",
    0xAE: "DEFSNG",
    0xAF: "DEFDBL",
    0xB0: "LINE",
    0xB1: "WHILE",
    0xB2: "WEND",
    0xB3: "CALL",
    0xB7: "WRITE",
    0xB8: "OPTION",
    0xB9: "RANDOMIZE",
    0xBA: "OPEN",
    0xBB: "CLOSE",
    0xBC: "LOAD",
    0xBD: "MERGE",
    0xBE: "SAVE",
    0xBF: "COLOR",
    0xC0: "CLS",
    0xC1: "MOTOR",
    0xC2: "BSAVE",
    0xC3: "BLOAD",
    0xC4: "SOUND",
    0xC5: "BEEP",
    0xC6: "PSET",
    0xC7: "PRESET",
    0xC8: "SCREEN",
    0xC9: "KEY",
    0xCA: "LOCATE",
    0xCC: "TO",
    0xCD: "THEN",
    0xCE: "TAB(",
    0xCF: "STEP",
    0xD0: "USR",
    0xD1: "FN",
    0xD2: "SPC(",
    0xD3: "NOT",
    0xD4: "ERL",
    0xD5: "ERR",
    0xD6: "STRING$",
    0xD7: "USING",
    0xD8: "INSTR",
    0xD9: "'",
    0xDA: "VARPTR",
    0xDB: "CSRLIN",
    0xDC: "POINT",
    0xDD: "OFF",
    0xDE: "INKEY$",
    0xE6: ">",
    0xE7: "=",
    0xE8: "<",
    0xE9: "+",
    0xEA: "-",
    0xEB: "*",
    0xEC: "/",
    0xED: "^",
    0xEE: "AND",
    0xEF: "OR",
    0xF0: "XOR",
    0xF1: "EQV",
    0xF2: "IMP",
    0xF3: "MOD",
    0xF4: "\\",
    0xFD81: "CVI",
    0xFD82: "CVS",
    0xFD83: "CVD",
    0xFD84: "MKI$",
    0xFD85: "MKS$",
    0xFD86: "MKD$",
    0xFD8B: "EXTERR",
    0xFE81: "FILES",
    0xFE82: "FIELD",
    0xFE83: "SYSTEM",
    0xFE84: "NAME",
    0xFE85: "LSET",
    0xFE86: "RSET",
    0xFE87: "KILL",
    0xFE88: "PUT",
    0xFE89: "GET",
    0xFE8A: "RESET",
    0xFE8B: "COMMON",
    0xFE8C: "CHAIN",
    0xFE8D: "DATE$",
    0xFE8E: "TIME$",
    0xFE8F: "PAINT",
    0xFE90: "COM",
    0xFE91: "CIRCLE",
    0xFE92: "DRAW",
    0xFE93: "PLAY",
    0xFE94: "TIMER",
    0xFE95: "ERDEV",
    0xFE96: "IOCTL",
    0xFE97: "CHDIR",
    0xFE98: "MKDIR",
    0xFE99: "RMDIR",
    0xFE9A: "SHELL",
    0xFE9B: "ENVIRON",
    0xFE9C: "VIEW",
    0xFE9D: "WINDOW",
    0xFE9E: "PMAP",
    0xFE9F: "PALETTE",
    0xFEA0: "LCOPY",
    0xFEA1: "CALLS",
    0xFEA4: "NOISE",
    0xFEA5: "PCOPY",
    0xFEA6: "TERM",
    0xFEA7: "LOCK",
    0xFEA8: "UNLOCK",
    0xFF81: "LEFT$",
    0xFF82: "RIGHT$",
    0xFF83: "MID$",
    0xFF84: "SGN",
    0xFF85: "INT",
    0xFF86: "ABS",
    0xFF87: "SQR",
    0xFF88: "RND",
    0xFF89: "SIN",
    0xFF8A: "LOG",
    0xFF8B: "EXP",
    0xFF8C: "COS",
    0xFF8D: "TAN",
    0xFF8E: "ATN",
    0xFF8F: "FRE",
    0xFF90: "INP",
    0xFF91: "POS",
    0xFF92: "LEN",
    0xFF93: "STR$",
    0xFF94: "VAL",
    0xFF95: "ASC",
    0xFF96: "CHR$",
    0xFF97: "PEEK",
    0xFF98: "SPACE$",
    0xFF99: "OCT$",
    0xFF9A: "HEX$",
    0xFF9B: "LPOS",
    0xFF9C: "CINT",
    0xFF9D: "CSNG",
    0xFF9E: "CDBL",
    0xFF9F: "FIX",
    0xFFA0: "PEN",
    0xFFA1: "STICK",
    0xFFA2: "STRIG",
    0xFFA3: "EOF",
    0xFFA4: "LOC",
    0xFFA5: "LOF"
}

if __name__=='__main__':
    import argparse
    parser = argparse.ArgumentParser(formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("basic_file", help="the GWBAS/BASICA file to print")
    args = parser.parse_args()
    for l in gwbas_lines(args.basic_file):
        print(l)

