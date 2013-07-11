/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.jopdesign.sys;

import util.Timer;

class JVM {

	private static void f_nop() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_aconst_null() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_m1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_4() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_5() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lconst_0() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lconst_1() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fconst_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static int f_fconst_1() { 
		return 0x3f800000;
	}
	private static int f_fconst_2() { 
		return 0x40000000;
	}
	private static void f_dconst_0() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static long f_dconst_1() {
	   return 0x3ff0000000000000L;
  }
	private static void f_bipush() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_sipush() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ldc() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ldc_w() { JVMHelp.noim();}
	private static void f_ldc2_w() { JVMHelp.noim();  /* jvm_long.inc */  }
	private static void f_iload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lload() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fload() { JVMHelp.noim();}
	private static void f_dload() { JVMHelp.noim();}
	private static void f_aload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iload_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iload_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iload_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iload_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lload_0() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lload_1() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lload_2() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lload_3() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fload_0() { JVMHelp.noim();}
	private static void f_fload_1() { JVMHelp.noim();}
	private static void f_fload_2() { JVMHelp.noim();}
	private static void f_fload_3() { JVMHelp.noim();}
	private static void f_dload_0() { JVMHelp.noim();}
	private static void f_dload_1() { JVMHelp.noim();}
	private static void f_dload_2() { JVMHelp.noim();}
	private static void f_dload_3() { JVMHelp.noim();}
	private static void f_aload_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_aload_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_aload_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_aload_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iaload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_laload() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_faload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_daload() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_aaload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_baload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_caload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_saload() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_istore() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lstore() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fstore() { JVMHelp.noim();}
	private static void f_dstore() { JVMHelp.noim();}
	private static void f_astore() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_istore_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_istore_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_istore_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_istore_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lstore_0() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lstore_1() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lstore_2() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lstore_3() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fstore_0() { JVMHelp.noim();}
	private static void f_fstore_1() { JVMHelp.noim();}
	private static void f_fstore_2() { JVMHelp.noim();}
	private static void f_fstore_3() { JVMHelp.noim();}
	private static void f_dstore_0() { JVMHelp.noim();}
	private static void f_dstore_1() { JVMHelp.noim();}
	private static void f_dstore_2() { JVMHelp.noim();}
	private static void f_dstore_3() { JVMHelp.noim();}
	private static void f_astore_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_astore_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_astore_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_astore_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iastore() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lastore() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_fastore() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_dastore() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_aastore(int ref, int index, int value) {
			
		synchronized (GC.mutex) {
			if (Config.USE_SCOPES) {
				
				if (Config.USE_SCOPECHECKS){
					
					// Pointer version
					//	if ((value >>> 25) > (ref  >>> 25)){
					//	GC.log("Illegal array reference");
					//	}
				
					// Handler version (default)
					int ref_level; 
					int val_level; 
					ref_level = Native.rdMem(ref + GC.OFF_SPACE);
					val_level = Native.rdMem(value + GC.OFF_SPACE);
					if (val_level > ref_level){
						GC.log("Illegal array reference");
					};
				}

			} else {
				// snapshot-at-beginning barrier
				int oldVal = Native.arrayLoad(ref, index);
				// Is it white?
				if (oldVal != 0
					&& Native.rdMem(oldVal+GC.OFF_SPACE) != GC.toSpace
					&& Native.rdMem(oldVal+GC.OFF_GREY)==0) {
					// Mark grey
					Native.wrMem(GC.grayList, oldVal+GC.OFF_GREY);
					GC.grayList = oldVal;			
				}				
			}

			Native.arrayStore(ref, index, value);
		}
	}
		
	private static void f_bastore() { JVMHelp.noim();}
	private static void f_castore() { JVMHelp.noim();}
	private static void f_sastore() { JVMHelp.noim();}
	private static void f_pop() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_pop2() { JVMHelp.noim();}
	private static void f_dup() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_dup_x1() { JVMHelp.noim();}
	private static void f_dup_x2() { JVMHelp.noim();}
	private static void f_dup2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_dup2_x1() { JVMHelp.noim();}
	private static void f_dup2_x2() { JVMHelp.noim();}
	private static void f_swap() { JVMHelp.noim();}
	private static void f_iadd() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_ladd(int ah, int al, int bh, int bl) {

		int carry = ((al>>>1) + (bl>>>1) + (al & bl & 1)) >>> 31;
		return Native.makeLong(ah+bh+carry, al+bl);
	}
	private static int f_fadd(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_add(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_dadd(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_add(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static void f_isub() { JVMHelp.noim(); /* jvm.asm */ }

	private static long f_lsub(long a, long b) {

		return a + (~b) + 1;
	}

	private static int f_fsub(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_sub(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static long f_dsub( long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_sub(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static void f_imul() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lmul(long a, long b) {
		
		boolean positive = true;
		if(a<0) {
			a = -a; 
			positive = false;
		  }
		if(b<0) {
			b = -b; 
			positive = !positive;
			}
		
		long aa = a;
		long res = 0;
		for(int i=0;i<64;i=i+8) // @WCA loop=8
		{
			long bb = b;
			int am = ((int)aa) & 0x000000FF;
			
			for(int j=0;j<64;j=j+8) // @WCA loop=8
			{
				int bm = ((int)bb) & 0x000000FF;
				
			    long sres = 0;
				if (i+j < 64) sres = Native.makeLong(0,am*bm) << (i+j);
				res += sres;
				bb = bb>>>8;
			}
			aa = aa>>>8;
		}
		if(!positive) res = -res;
		return res;
	}

	private static int f_fmul(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_mul(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static long f_dmul(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_mul(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static int f_idiv(int a, int b) { 

		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
		}

		if(b==0x80000000) {
			if(a==0x80000000) return 1;
			else return 0;
		}

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		int c = 0;
		int r = 0;
		for (int i=0; i<32; ++i) { // @WCA loop=32
			c <<= 1;
			r <<= 1;
			if (a < 0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
				c |= 1;
			}
		}

		if (neg) {
			c = -c;
		}
		return c;
	}

	private static long f_ldiv(long a, long b) {
		
		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
		}

		if(b==0x8000000000000000L) {
			if(a==0x8000000000000000L) return 1;
			else return 0;
		}
		
		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			neg = !neg;
			b = -b;
		}

		long c = 0;
		long r = 0;
		for (int i=0; i<64; ++i) { // @WCA loop=64 
			c <<= 1;
			r <<= 1;
			if (a < 0) {
				r |= 1;
			}
			a <<= 1;
			if ((r-1)>=(b-1)) //over/underflow problem - other testcases??
			{
				r -= b;
				c |= 1;
			}
//			System.out.println("c="+((int)(c>>32))+" "+((int)c));
//			System.out.println("r="+((int)(r>>32))+" "+((int)r));

		}

		if (neg) {
			c = -c;
		}

		return c;	
	}
	
	private static int f_fdiv(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_div(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static long f_ddiv(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_div(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static int f_irem(int a, int b) {

		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
		}

		if(b==0x80000000) {
			if(a==0x80000000) return 0;
			else return a;
		}

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			b = -b;
		}

		int r = 0;
		for (int i=0; i<32; ++i) { // @WCA loop=32
			r <<= 1;
			if (a < 0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
			}
		}

		if (neg) {
			r = -r;
		}
		return r;
	}

	private static long f_lrem(long a, long b) {

		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
		}

		if(b==0x8000000000000000L) {
			if(a==0x8000000000000000L) return 0;
			else return a;
		}

		boolean neg = false;
		if (a<0) {
			neg = true;
			a = -a;
		}
		if (b<0) {
			b = -b;
		}

		long r = 0;
		for (int i=0; i<64; ++i) { // @WCA loop=64
			r <<= 1;
			if (a < 0) {
				r |= 1;
			}
			a <<= 1;
			if (r>=b) {
				r -= b;
			}
	//		System.out.println("r="+((int)(r>>32))+" "+((int)r));

		}

		if (neg) {
			r = -r;
		}
		return r;
	}

	private static int f_frem(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_rem(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static long f_drem(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_rem(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static void f_ineg() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lneg(long a) {
		return ~a+1;
	}
	private static int f_fneg(int a) { 
		return a ^ 0x80000000;
	}
	private static long f_dneg(long a) {
		return a ^ 0x8000000000000000L;
	}
	private static void f_ishl() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lshl(int ah, int al, int cnt) { 
		
		cnt &= 0x3F;
		if ((cnt==0)) return Native.makeLong(ah, al);	
		if (cnt>31) {
			ah = al << (cnt-32);
			al = 0;
		} else {
			ah = ah << cnt;
			ah += al >>> (32-cnt);
			al = al << cnt;
		}
		return Native.makeLong(ah, al);		
	}
	private static void f_ishr() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lshr(int ah, int al, int cnt) 
	{ 
		cnt &= 0x3F;
		if (cnt==0) return Native.makeLong(ah, al);	
		if (cnt>31) {
			al = ah >> (cnt-32);
			if(ah<0)
			  ah = -1;
			else
		      ah = 0;
		} else {
			al = al >>> cnt;
			al += ah << (32-cnt);
			ah = ah >> cnt;
		}
		return Native.makeLong(ah, al);
	}
	private static void f_iushr() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lushr(int ah, int al, int cnt) {

		cnt &= 0x3F;
		if ((cnt==0)) return Native.makeLong(ah, al);	
		if (cnt>31) {
			al = ah >>> (cnt-32);
			ah = 0;
		} else {
			al = al >>> cnt;
			al += ah << (32-cnt);
			ah = ah >>> cnt;
		}
		return Native.makeLong(ah, al);
	}
	private static void f_iand() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_land(int ah, int al, int bh, int bl) {
		
		ah &= bh;
		al &= bl;
		return Native.makeLong(ah, al);
	}
	private static void f_ior() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lor(int ah, int al, int bh, int bl) {
		
		ah |= bh;
		al |= bl;
		return Native.makeLong(ah, al);
	}
	private static void f_ixor() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lxor(int ah, int al, int bh, int bl) {

		ah ^= bh;
		al ^= bl;
		return Native.makeLong(ah, al);
	}
	private static void f_iinc() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_i2l(int a) {
		return Native.makeLong(a>>31, a);
	}
	private static int f_i2f(int a) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.intToFloat(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_i2d(int a) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.intToDouble(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static void f_l2i() { JVMHelp.noim(); /* jvm_long.inc */}
	private static int f_l2f(long a) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.longToFloat(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_l2d(long a) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.longToDouble(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static int f_f2i(int a) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.intValue(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_f2l(int a) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.longValue(a);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_f2d(int f) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.floatToDouble(f);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static int f_d2i(long d) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.intValue(d);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static long f_d2l(long d) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.longValue(d);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}
	private static int f_d2f(long d) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.doubleToFloat(d);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

// i2x should be done in jvm.asm!!!
// just to lazy and stay compatible with OEBB project

	private static int f_i2b(int a) {
		a &= 0xff;
		if ((a & 0x80)!=0) {
			a |= 0xffffff00;
		}
		return a;
	}
	private static void f_i2c() { JVMHelp.noim(); /* jvm.asm */ }

	private static int f_i2s(int a) {

		a &= 0xffff;
		if ((a&0x8000) != 0) {
			a |= 0xffff0000;
		}
		return a;
	}

	private static int f_lcmp(int ah, int al, int bh, int bl) {

		//overflow, underflow, if a and b have different signs
		if((ah >= 0)&&(bh < 0)) return 1;
		if((ah < 0)&&(bh >= 0)) return -1;
		// I didn't have it in my first implementation
		
		long a = Native.makeLong(ah, al) - Native.makeLong(bh, bl);

		al = (int) a;
		ah = (int) (a>>>32);

		if ((ah | al)==0) return 0;
		if (ah >= 0) {
			return 1;
		} else {
			return -1;
		}
	}

	private static int f_fcmpl(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_cmpl(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static int f_fcmpg(int a, int b) {
		if (Const.SUPPORT_FLOAT) {
			return SoftFloat32.float_cmpg(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static int f_dcmpl(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_cmpl(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static int f_dcmpg(long a, long b) {
		if (Const.SUPPORT_DOUBLE) {
			return SoftFloat64.double_cmpg(a, b);
		} else {
			JVMHelp.noim();
			return 0;
		}
	}

	private static void f_ifeq() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ifne() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iflt() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ifge() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ifgt() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_ifle() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmpeq() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmpne() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmplt() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmpge() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmpgt() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_icmple() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_acmpeq() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_if_acmpne() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_goto() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jsr() { JVMHelp.noim();}
	private static void f_ret() { JVMHelp.noim();}

	private static void f_tableswitch(int idx) { 

		int i;
		int sp = Native.getSP();			// sp of ();
		int pc = Native.rdIntMem(sp-3)-1;	// one to high
		i = Native.rdIntMem(sp);			// mp
		int start = Native.rdMem(i)>>>10;	// address of method

		// memory is addressed in 32 bit words!
		i = (pc>>>2)+1+start;	// points to default word

		int low = Native.rdMem(i+1);

		// if (idx<low || idx>high) {
		if (idx<low || idx>Native.rdMem(i+2)) {
			pc += Native.rdMem(i);		// default case
		} else {
			pc += Native.rdMem(i+3+idx-low);
		}
		Native.wrIntMem(pc, sp-3);
	}

	private static void f_lookupswitch(int key) {

		int i, j;
		int sp = Native.getSP();			// sp of ();
		int pc = Native.rdIntMem(sp-3)-1;	// one to high
		i = Native.rdIntMem(sp);			// mp
		int start = Native.rdMem(i)>>>10;	// address of method

		// memory is addressed in 32 bit words!
		i = (pc>>>2)+1+start;	// points to default word

		int off = Native.rdMem(i);	// default offset
		int cnt = Native.rdMem(i+1);

		i += 2;						// point to pairs
		for (j=0; j<cnt; ++j) {
			if (Native.rdMem(i+(j<<1)) == key) {
				off = Native.rdMem(i+(j<<1)+1);		// found match
				break;
			}
		}

		Native.wrIntMem(pc+off, sp-3);
	}

	private static void f_ireturn() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lreturn() { JVMHelp.noim();}
	private static void f_freturn() { JVMHelp.noim();}
	private static void f_dreturn() { JVMHelp.noim();}
	private static void f_areturn() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_return() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_getstatic() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_putstatic() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_getfield() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_putfield() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_invokevirtual() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_invokespecial() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_invokestatic() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_invokeinterface() { JVMHelp.noim();}
	private static void f_unused_ba() { JVMHelp.noim();}

	static int f_new(int cons) {

		return GC.newObject(cons);

	}

	static int f_newarray(int count, int type) {

		return GC.newArray(count, type);

	}

	static int f_anewarray(int count, int cons) {

		// ignore cons (type info)
		// should be different for the GC!!!
		return GC.newArray(count, 1); //1..type not available=reference
	}



	private static void f_arraylength() { JVMHelp.noim(); /* jvm.asm */ }

	private static Throwable f_athrow(Throwable t) {
		
		if (Const.USE_RTTM) {
			// abort transaction on any exception 
			Native.wrMem(Const.TM_ABORTED, Const.MEM_TM_MAGIC);
		}
		
		int i, j;

		// get frame pointer
		int fp = Native.getSP()-4;

		while (fp > Const.STACK_OFF+5) {

			// save frame information
			int pc = Native.rdIntMem(fp+1)-1; // correct one-off
			int vp = Native.rdIntMem(fp+2);
			int cp = Native.rdIntMem(fp+3);
			int mp = Native.rdIntMem(fp+4);

			// locate exception table
			i = Native.rdMem(mp);
			int tabstart = (i >>> 10) + (i & 0x3ff);
			i = Native.rdMem(tabstart);
			int tablen = i & 0xffff;
			int mode = i & 0x10000;
			
			// search exception table
			for (j = tabstart+1; j < tabstart+1+2*tablen; j+=2) {
				
				// extract table entry
				i = Native.rdMem(j);
				int begin = i >> 16;
				int end = i & 0xffff;
				i = Native.rdMem(j+1);
				int target = i >> 16;
				int type = i & 0xffff;
				
				// check if applicable
				if (pc >= begin && pc < end) {
					if (type == 0
						|| f_instanceof(Native.toInt(t), Native.rdMem(cp+type)) != 0) {
						
						// compute correct stack pointer
						i = Native.rdMem(mp+1);
						int sp = vp + (i & 0x1f) + ((i >>> 5) & 0x1f) + 4;

						// fake return frame
						Native.wrIntMem(sp, fp+0);
						Native.wrIntMem(target, fp+1);

						// return with faked frame
						Native.setSP(fp+4);
						return t;
					}
				}
			}

			// do monitorexit if necessary
			if (mode != 0) {
				i = Native.rdIntMem(fp+5); // reference is right above the frame
				// TODO: object to lock is found somewhere else for static methods
 				Native.monitorExit(i);
			}

			// go up one frame
			i = Native.rdMem(mp+1);
			fp = vp + (i & 0x1f) + ((i >>> 5) & 0x1f);
		}

		JVMHelp.wr("Uncaught exception: ");
		String s = t.getMessage();
		if (s!=null) {
			JVMHelp.wr(s);
		}
		JVMHelp.wr("\n");
		JVMHelp.trace(Native.getSP());

		System.exit(1);
		return t;
	}

	private static int f_checkcast(int objref, int cons) {

		if (objref==0) {
			return objref;
		}

		int p = Native.rdMem(objref+GC.OFF_MTAB_ALEN);	// ptr to MTAB
		p -= Const.CLASS_HEADR;							// start of class info

		// check against interface
		int ifidx = Native.rdMem(cons+Const.CLASS_SUPER);
		if (ifidx < 0) {
			int iftab = Native.rdMem(p+Const.CLASS_IFTAB);
			if (iftab == 0) {
				// the class does not implement any interface
				throw JVMHelp.CCExc;
			} else {
				// check if the appropriate bit is set
				int i = Native.rdMem(iftab-((-ifidx+31)>>>5));
				if (((i >>> (~ifidx & 0x1f)) & 1) != 0) {
					return objref;
				} else {
					throw JVMHelp.CCExc;
				}
			}
		}

		// search for superclass
		for (;;) {
			// always check this bound with TypeGraphTool!
			if (p==cons) { // @WCA loop <= 5
				return objref;
			} else {
				p = Native.rdMem(p+Const.CLASS_SUPER);	// super class ptr
				if (p==0) throw JVMHelp.CCExc;
			}
		}		

	}

	private static int f_instanceof(int objref, int cons) {

		if (objref==0) {
			return 0;
		}
		int p = Native.rdMem(objref+GC.OFF_MTAB_ALEN);	// handle indirection
		p -= Const.CLASS_HEADR;							// start of class info

		// check against interface
		int ifidx = Native.rdMem(cons+Const.CLASS_SUPER);
		if (ifidx < 0) {
			int iftab = Native.rdMem(p+Const.CLASS_IFTAB);
			if (iftab == 0) {
				// the class does not implement any interface
				return 0;
			} else {
				// check if the appropriate bit is set
				int i = Native.rdMem(iftab-((-ifidx+31)>>>5));
				return (i >>> (~ifidx & 0x1f)) & 1;
			}
		}

		// search for superclass
		for (;;) {
			// always check this bound with TypeGraphTool!
			if (p==cons) { // @WCA loop <= 5
				return 1;
			} else {
				p = Native.rdMem(p+Const.CLASS_SUPER);
				if (p==0) return 0;
			}
		}

	}


	private static int enterCnt;

	private static void f_monitorenter(int objAddr) {

/* is now in jvm.asm
*/
	}

	private static void f_monitorexit(int objAddr) {

/* is now in jvm.asm
*/
	}


	private static void f_wide() { JVMHelp.noim();}
	
	private static int f_multianewarray() {

//		JVMHelp.wr("multianewarray - GC issue?");
//		JVMHelp.wr("\r\n");
		//
		// be careful! We have to manipulate the stack frame.
		// If the layout changes we have to change this method.
		//
		int ret = 0;
		int i, j;
		
		int sp = Native.getSP();			// sp after call of f_multi();
		int fp = sp-4;		// first frame point is easy, since last sp points to the end of the frame

		// pc points to the next byte - the first index byte
		int pc = Native.rdIntMem(fp+1);
		
		int cp = Native.rdIntMem(fp+3);
		
		int mp = Native.rdIntMem(fp+4);
		int start = Native.rdMem(mp)>>>10;	// address of method

		// memory is addressed in 32 bit words!
		// get index into cpool and array type
		j = Native.rdMem(start+(pc>>2));
		for (i=(pc&0x03); i<3; ++i) j >>= 8;
		j &= 0xff;
		j <<= 8;
		++pc;
		int type = Native.rdMem(start+(pc>>2));
		for (i=(pc&0x03); i<3; ++i) type >>= 8;
		type &= 0xff;
		type += j;
		type += cp;
		type = Native.rdMem(type);
		
		++pc;	// now to dimensions
		
		int dim = Native.rdMem(start+(pc>>2));
		for (i=(pc&0x03); i<3; ++i) dim >>= 8;
		dim &= 0xff;
		
		// correct pc to point to the next instruction
		Native.wrIntMem(pc+1, fp+1);

		// int vp = Native.rdIntMem(fp+2);
		// sp is now the previous sp
		sp = Native.rdIntMem(fp);
		sp -= dim;		// correct the sp
		Native.wrIntMem(sp, fp);
		if (dim!=2) {
//			System.out.print("multanewarray: ");
			System.out.print(dim);
			System.out.println("dimensions not supported");
			JVMHelp.noim();
		}
/*
		System.out.print("multianewarray: ");
		System.out.println(dim);
		for (i=1; i<=dim; ++i) {
			System.out.println(Native.rdIntMem(sp+i));
		}
*/
		// first dimension
		int cnt = Native.rdIntMem(sp+1);
		int cnt2 = Native.rdIntMem(sp+2);
		// we ignore type on anewarray
		ret = f_anewarray(cnt, 0);
		// handle
		for (i=0; i<cnt; ++i) {
			int arr = f_newarray(cnt2, type);
			synchronized(GC.mutex) {
				Native.wrMem(arr, Native.rdMem(ret)+i);
			}
		}
		
		return ret;
	}

	private static void f_ifnull() { JVMHelp.noim();}
	private static void f_ifnonnull() { JVMHelp.noim();}
	private static void f_goto_w() { JVMHelp.noim();}
	private static void f_jsr_w() { JVMHelp.noim();}
	private static void f_breakpoint() { JVMHelp.noim();}
	private static void f_resCB() { JVMHelp.noim();}
	private static void f_resCC() { JVMHelp.noim();}
	private static void f_resCD() { JVMHelp.noim();}
	private static void f_resCE() { JVMHelp.noim();}
	private static void f_resCF() { JVMHelp.noim();}
	private static void f_jopsys_null() { JVMHelp.noim();}
	private static void f_jopsys_rd() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_wr() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_rdmem() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_wrmem() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_rdint() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_wrint() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_getsp() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_setsp() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_getvp() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_jopsys_setvp() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_resDB() { JVMHelp.noim();}
	private static void f_resDC() { JVMHelp.noim();}
	private static void f_resDD() { JVMHelp.noim();}
	private static void f_resDE() { JVMHelp.noim();}
	private static void f_resDF() { JVMHelp.noim();}
	private static void f_resE0() { JVMHelp.noim();}
	private static void f_putstatic_ref(int val, int addr) {
		
		synchronized (GC.mutex) {
			if (Config.USE_SCOPES) {
				if (Config.USE_SCOPECHECKS) {
					
					// Pointer version
					//	if ((val >>> 25) != 0){
					//	GC.log("Illegal static reference");
					//}

				// Handler version (Default)
					int val_level; 
					val_level = Native.rdMem(val + GC.OFF_SPACE);
					if (val_level != 0){
						GC.log("Illegal static reference");
						}
				
				}

			} else {
				// snapshot-at-beginning barrier
				int oldVal = Native.getStatic(addr);
				// Is it white?
				if (oldVal != 0
					&& Native.rdMem(oldVal+GC.OFF_SPACE) != GC.toSpace
					&& Native.rdMem(oldVal+GC.OFF_GREY)==0) {
					// Mark grey
					Native.wrMem(GC.grayList, oldVal+GC.OFF_GREY);
					GC.grayList = oldVal;
				}				
			}

			Native.putStatic(val, addr);
		}
	}
	private static void f_resE2() { JVMHelp.noim();}
	private static void f_putfield_ref(int ref, int value, int index) {
		
		synchronized (GC.mutex) {
			
			if (Config.USE_SCOPES) {

				if (Config.USE_SCOPECHECKS) {
					
					// Pointer version
					//	if ((value >>> 25) > (ref  >>> 25)){
					//	GC.log("Illegal field reference");
					//	}

				// Handler version (default)
					int ref_level; 
					int val_level; 
					ref_level = Native.rdMem(ref + GC.OFF_SPACE);
					val_level = Native.rdMem(value + GC.OFF_SPACE);
					if (val_level > ref_level){
						//GC.log("Illegal field reference");
					}
				}
				
			} 
			else {
				// snapshot-at-beginning barrier
				int oldVal = Native.getField(ref, index);
				// Is it white?
				if (oldVal != 0
					&& Native.rdMem(oldVal+GC.OFF_SPACE) != GC.toSpace
					&& Native.rdMem(oldVal+GC.OFF_GREY)==0) {
					// Mark grey
					Native.wrMem(GC.grayList, oldVal+GC.OFF_GREY);
					GC.grayList = oldVal;
				}				
			}
			Native.putField(ref, index, value);
		}
	}
	private static void f_resE4() { JVMHelp.noim();}
	private static void f_resE5() { JVMHelp.noim();}
	private static void f_resE6() { JVMHelp.noim();}
	private static void f_resE7() { JVMHelp.noim();}
	private static void f_jopsys_memcpy() { JVMHelp.noim();}
	private static void f_jopsys_getfield() { JVMHelp.noim();}
	private static void f_jopsys_putfield() { JVMHelp.noim();}
	private static void f_resEB() { JVMHelp.noim();}
	private static void f_invokesuper() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_resED() { JVMHelp.noim();}
	private static void f_resEE() { JVMHelp.noim();}
	private static void f_resEF() { JVMHelp.noim();}
	private static void f_resF0() { JVMHelp.noim();}
	private static void f_resF1() { JVMHelp.noim();}
	private static void f_resF2() { JVMHelp.noim();}
	private static void f_resF3() { JVMHelp.noim();}
	private static void f_resF4() { JVMHelp.noim();}
	private static void f_resF5() { JVMHelp.noim();}
	private static void f_resF6() { JVMHelp.noim();}
	private static void f_resF7() { JVMHelp.noim();}
	private static void f_resF8() { JVMHelp.noim();}
	private static void f_resF9() { JVMHelp.noim();}
	private static void f_resFA() { JVMHelp.noim();}
	private static void f_resFB() { JVMHelp.noim();}
	private static void f_resFC() { JVMHelp.noim();}
	private static void f_resFD() { JVMHelp.noim();}
	private static void f_sys_noim() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_sys_init() { JVMHelp.noim(); /* jvm.asm */ }
}
