/*
 * This code is released under the GPL. A copy of the licence is in this
 * program's main directory.
 */
package com.darktalents.cmd.bascat;

import java.util.Map;
import static java.util.Map.entry;

/**
 * Just plain data, (opcode,description) pair... with some static helpers
 * to create them.
 * @author richard todd
 */
final class Token {
     public final int    opcode;
     public final String description;

     private Token(int o, String d) { opcode = o; description = d; }

     public static Token fromOpcode(final int op) { 
         String opname = opcodes.get(op);
         if(opname == null) {
            opname = String.format("<OP:%d>", op);
         }
         return new Token(op, opname ); 
     }
     
     public static Token fromLiteral(final String lit) { return new Token(lit.charAt(0),lit); }
     public static Token fromLiteral(final int ch) { return new Token(ch, Character.toString((char)ch)); }
  
     public static Token fromNumber(long num, int base) {
         String desc;
         switch(base) {
             case 8: desc = String.format("&O%o",num); break;
             case 16: desc = String.format("&H%X",num); break;
             default: desc = Long.toString(num); break;
         }    
         return new Token(-1, desc);
     } 

     public static Token fromFloat(double num) {
         return new Token(-1, String.format("%G",num));
     }
     
   /** opcodes represents the BASIC tokens which are shorthand
    * for keywords. 
    */
  private static final Map<Integer,String> opcodes = Map.ofEntries(
    entry(0x00,"EOL"),
	entry(0x81,"END"),
	entry(0x82,"FOR"),
	entry(0x83,"NEXT"),
	entry(0x84,"DATA"),
	entry(0x85,"INPUT"),
	entry(0x86,"DIM"),
	entry(0x87,"READ"),
	entry(0x88,"LET"),
	entry(0x89,"GOTO"),
	entry(0x8A,"RUN"),
	entry(0x8B,"IF"),
	entry(0x8C,"RESTORE"),
	entry(0x8D,"GOSUB"),
	entry(0x8E,"RETURN"),
	entry(0x8F,"REM"),
	entry(0x90,"STOP"),
	entry(0x91,"PRINT"),
	entry(0x92,"CLEAR"),
	entry(0x93,"LIST"),
	entry(0x94,"NEW"),
	entry(0x95,"ON"),
	entry(0x96,"WAIT"),
	entry(0x97,"DEF"),
	entry(0x98,"POKE"),
	entry(0x99,"CONT"),
	entry(0x9C,"OUT"),
	entry(0x9D,"LPRINT"),
	entry(0x9E,"LLIST"),
	entry(0xA0,"WIDTH"),
	entry(0xA1,"ELSE"),
	entry(0xA2,"TRON"),
	entry(0xA3,"TROFF"),
	entry(0xA4,"SWAP"),
	entry(0xA5,"ERASE"),
	entry(0xA6,"EDIT"),
	entry(0xA7,"ERROR"),
	entry(0xA8,"RESUME"),
	entry(0xA9,"DELETE"),
	entry(0xAA,"AUTO"),
	entry(0xAB,"RENUM"),
	entry(0xAC,"DEFSTR"),
	entry(0xAD,"DEFINT"),
	entry(0xAE,"DEFSNG"),
	entry(0xAF,"DEFDBL"),
	entry(0xB0,"LINE"),
	entry(0xB1,"WHILE"),
	entry(0xB2,"WEND"),
	entry(0xB3,"CALL"),
	entry(0xB7,"WRITE"),
	entry(0xB8,"OPTION"),
	entry(0xB9,"RANDOMIZE"),
	entry(0xBA,"OPEN"),
	entry(0xBB,"CLOSE"),
	entry(0xBC,"LOAD"),
	entry(0xBD,"MERGE"),
	entry(0xBE,"SAVE"),
	entry(0xBF,"COLOR"),
	entry(0xC0,"CLS"),
	entry(0xC1,"MOTOR"),
	entry(0xC2,"BSAVE"),
	entry(0xC3,"BLOAD"),
	entry(0xC4,"SOUND"),
	entry(0xC5,"BEEP"),
	entry(0xC6,"PSET"),
	entry(0xC7,"PRESET"),
	entry(0xC8,"SCREEN"),
	entry(0xC9,"KEY"),
	entry(0xCA,"LOCATE"),
	entry(0xCC,"TO"),
	entry(0xCD,"THEN"),
	entry(0xCE,"TAB("),
	entry(0xCF,"STEP"),
	entry(0xD0,"USR"),
	entry(0xD1,"FN"),
	entry(0xD2,"SPC("),
	entry(0xD3,"NOT"),
	entry(0xD4,"ERL"),
	entry(0xD5,"ERR"),
	entry(0xD6,"STRING$"),
	entry(0xD7,"USING"),
	entry(0xD8,"INSTR"),
	entry(0xD9,"'"),
	entry(0xDA,"VARPTR"),
	entry(0xDB,"CSRLIN"),
	entry(0xDC,"POINT"),
	entry(0xDD,"OFF"),
	entry(0xDE,"INKEY$"),
	entry(0xE6,">"),
	entry(0xE7,"="),
	entry(0xE8,"<"),
	entry(0xE9,"+"),
	entry(0xEA,"-"),
	entry(0xEB,"*"),
	entry(0xEC,"/"),
	entry(0xED,"^"),
	entry(0xEE,"AND"),
	entry(0xEF,"OR"),
	entry(0xF0,"XOR"),
	entry(0xF1,"EQV"),
	entry(0xF2,"IMP"),
	entry(0xF3,"MOD"),
	entry(0xF4,"\\"),
	entry(0xFD81,"CVI"),
	entry(0xFD82,"CVS"),
	entry(0xFD83,"CVD"),
	entry(0xFD84,"MKI$"),
	entry(0xFD85,"MKS$"),
	entry(0xFD86,"MKD$"),
	entry(0xFD8B,"EXTERR"),
	entry(0xFE81,"FILES"),
	entry(0xFE82,"FIELD"),
	entry(0xFE83,"SYSTEM"),
	entry(0xFE84,"NAME"),
	entry(0xFE85,"LSET"),
	entry(0xFE86,"RSET"),
	entry(0xFE87,"KILL"),
	entry(0xFE88,"PUT"),
	entry(0xFE89,"GET"),
	entry(0xFE8A,"RESET"),
	entry(0xFE8B,"COMMON"),
	entry(0xFE8C,"CHAIN"),
	entry(0xFE8D,"DATE$"),
	entry(0xFE8E,"TIME$"),
	entry(0xFE8F,"PAINT"),
	entry(0xFE90,"COM"),
	entry(0xFE91,"CIRCLE"),
	entry(0xFE92,"DRAW"),
	entry(0xFE93,"PLAY"),
	entry(0xFE94,"TIMER"),
	entry(0xFE95,"ERDEV"),
	entry(0xFE96,"IOCTL"),
	entry(0xFE97,"CHDIR"),
	entry(0xFE98,"MKDIR"),
	entry(0xFE99,"RMDIR"),
	entry(0xFE9A,"SHELL"),
	entry(0xFE9B,"ENVIRON"),
	entry(0xFE9C,"VIEW"),
	entry(0xFE9D,"WINDOW"),
	entry(0xFE9E,"PMAP"),
	entry(0xFE9F,"PALETTE"),
	entry(0xFEA0,"LCOPY"),
	entry(0xFEA1,"CALLS"),
	entry(0xFEA4,"NOISE"),
	entry(0xFEA5,"PCOPY"),
	entry(0xFEA6,"TERM"),
	entry(0xFEA7,"LOCK"),
	entry(0xFEA8,"UNLOCK"),
	entry(0xFF81,"LEFT$"),
	entry(0xFF82,"RIGHT$"),
	entry(0xFF83,"MID$"),
	entry(0xFF84,"SGN"),
	entry(0xFF85,"INT"),
	entry(0xFF86,"ABS"),
	entry(0xFF87,"SQR"),
	entry(0xFF88,"RND"),
	entry(0xFF89,"SIN"),
	entry(0xFF8A,"LOG"),
	entry(0xFF8B,"EXP"),
	entry(0xFF8C,"COS"),
	entry(0xFF8D,"TAN"),
	entry(0xFF8E,"ATN"),
	entry(0xFF8F,"FRE"),
	entry(0xFF90,"INP"),
	entry(0xFF91,"POS"),
	entry(0xFF92,"LEN"),
	entry(0xFF93,"STR$"),
	entry(0xFF94,"VAL"),
	entry(0xFF95,"ASC"),
	entry(0xFF96,"CHR$"),
	entry(0xFF97,"PEEK"),
	entry(0xFF98,"SPACE$"),
	entry(0xFF99,"OCT$"),
	entry(0xFF9A,"HEX$"),
	entry(0xFF9B,"LPOS"),
	entry(0xFF9C,"CINT"),
	entry(0xFF9D,"CSNG"),
	entry(0xFF9E,"CDBL"),
	entry(0xFF9F,"FIX"),
	entry(0xFFA0,"PEN"),
	entry(0xFFA1,"STICK"),
	entry(0xFFA2,"STRIG"),
	entry(0xFFA3,"EOF"),
	entry(0xFFA4,"LOC"),
	entry(0xFFA5,"LOF"));
}
