/*
 * This code is released under the GPL. A copy of the licence is in this
 * program's main directory.
 */
package com.darktalents.cmd.bascat;

import java.util.function.IntSupplier;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Utilities for reading binary data.
 * @author richa
 */
class BinaryReader {
    final IntSupplier src;
    
    public BinaryReader(IntSupplier source) {
        src = source;
    }
    
    public int readu8() { return src.getAsInt(); }
    
    public int readu16() { 
        final var b0 = src.getAsInt();
        final var b1 = src.getAsInt();
        return (b0 | (b1 << 8));
    }

    public int read16() {
        final var u16 = (short)this.readu16();
        return u16; 
    }

    public double readf32() {
     final var bs0 = src.getAsInt();
     final var bs1 = src.getAsInt();
     final var bs2 = src.getAsInt();
     final var bs3 = src.getAsInt();
     if (bs3 == 0) return 0.0;
     var sign = new BigDecimal( (0x80 & bs2) == 0 ? 1 : -1, MathContext.UNLIMITED );
     final var exp = bs3 - 129;
     final var TWO = new BigDecimal(2,MathContext.UNLIMITED);
     final var expt = (exp < 0)?BigDecimal.ONE.divide(TWO.pow(-exp,MathContext.UNLIMITED),MathContext.UNLIMITED) : TWO.pow(exp,MathContext.UNLIMITED);
     var scand = new BigDecimal( bs0 | (bs1 << 8) | ((bs2 & 0x7f) << 16), MathContext.UNLIMITED );
     return sign.multiply(
             scand.divide(new BigDecimal(0x800000L,MathContext.UNLIMITED)).add(BigDecimal.ONE)).multiply(expt).doubleValue();
  }

  public double readf64() {
     var bs0 = src.getAsInt();
     var bs1 = src.getAsInt();
     var bs2 = src.getAsInt();
     var bs3 = src.getAsInt();
     var bs4 = src.getAsInt();
     var bs5 = src.getAsInt();
     var bs6 = src.getAsInt();
     var bs7 = src.getAsInt();

     if (bs7 == 0) return 0.0;
     var sign = new BigDecimal( (0x80 & bs6) == 0 ? 1 : -1, MathContext.UNLIMITED );
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
