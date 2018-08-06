/*
 * This code is released under the GPL. A copy of the licence is in this
 * program's main directory.
 */
package org.rwtodd.cmd.bascat;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Utilities for reading binary data.
 * @author richa
 */
class BinaryReader {
    private final byte[] src;
    private int idx;
    private final int last2;
 
    public BinaryReader(final byte[] source) {
        src = source;
        last2 = src.length - 2;
        idx = 0;
    }
    
    public boolean atEOF() { return idx >= src.length; }
    public void skip(int amt) { idx += amt; }
    public void reset() { idx = 0; }

    public boolean peek(int val) { 
       return (idx < src.length) && (src[idx]&0xff)==val; 
    }
    public boolean peek2(int v1, int v2) { 
       return (idx <= last2) && (src[idx]&0xff)==v1 && (src[idx+1]&0xff)==v2; 
    }

    public int readu8() { 
       return (idx < src.length) ? src[idx++] & 0xff : 0;
    }
    
    public int readu16() { 
        if (idx > last2) return 0;

        final var b0 = src[idx++] & 0xff;
        final var b1 = src[idx++] & 0xff;
        return (b0 | (b1 << 8));
    }

    public int read16() {
        final var u16 = (short)this.readu16();
        return u16; 
    }

    public double readf32() {
     final var bs0 = readu8();
     final var bs1 = readu8();
     final var bs2 = readu8();
     final var bs3 = readu8();
     if (bs3 == 0) return 0.0;
     final var sign = new BigDecimal( (0x80 & bs2) == 0 ? 1 : -1, MathContext.UNLIMITED );
     final var exp = bs3 - 129;
     final var TWO = new BigDecimal(2,MathContext.UNLIMITED);
     final var expt = (exp < 0)?BigDecimal.ONE.divide(TWO.pow(-exp,MathContext.UNLIMITED),MathContext.UNLIMITED) : TWO.pow(exp,MathContext.UNLIMITED);
     final var scand = new BigDecimal( bs0 | (bs1 << 8) | ((bs2 & 0x7f) << 16), MathContext.UNLIMITED );
     return sign.multiply(
             scand.divide(new BigDecimal(0x800000L,MathContext.UNLIMITED)).add(BigDecimal.ONE)).multiply(expt).doubleValue();
  }

  public double readf64() {
     var bs0 = readu8();
     var bs1 = readu8();
     var bs2 = readu8();
     var bs3 = readu8();
     var bs4 = readu8();
     var bs5 = readu8();
     var bs6 = readu8();
     var bs7 = readu8();
     if (bs7 == 0) return 0.0;

     final var sign = new BigDecimal( (0x80 & bs6) == 0 ? 1 : -1, MathContext.UNLIMITED );
     final var exp = bs7 - 129;
     final var TWO = new BigDecimal(2,MathContext.UNLIMITED);
     final var expt = (exp < 0)?BigDecimal.ONE.divide(TWO.pow(-exp,MathContext.UNLIMITED),MathContext.UNLIMITED) : TWO.pow(exp,MathContext.UNLIMITED);
     var scand = new BigDecimal( 
         (long)bs0 | 
         ((long)bs1 << 8L) |
         ((long)bs2 << 16L) |
         ((long)bs3 << 24L) |
         ((long)bs4 << 32L) |
		 ((long)bs5 << 40L) |
         ((long)(bs6 & 0x7f) << 48L) 
       , MathContext.UNLIMITED );
     return sign.multiply(
             scand.divide(new BigDecimal(0x80000000000000L,MathContext.UNLIMITED)).add(BigDecimal.ONE)).multiply(expt,MathContext.UNLIMITED).doubleValue();
  } 
   
}
