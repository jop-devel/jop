package com.jopdesign.sys;

class JVM {

	private static void f_nop() { JVMHelp.noim(); /* jvm.asm */ }
	private static int f_aconst_null() { 
		return 0;
	}
	private static void f_iconst_m1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_0() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_1() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_2() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_3() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_4() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_iconst_5() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_lconst_0() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static void f_lconst_1() { JVMHelp.noim(); /* jvm_long.inc */ }
	private static int f_fconst_0() { 
		return 0;
	}
	private static int f_fconst_1() { 
		return 0x3f800000;
	}
	private static int f_fconst_2() { 
		return 0x40000000;
	}
	private static void f_dconst_0() { JVMHelp.noim();}
	private static void f_dconst_1() { JVMHelp.noim();}
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
	private static void f_laload() { JVMHelp.noim(); /* jvm.asm */}
	private static void f_faload() { JVMHelp.noim();}
	private static void f_daload() { JVMHelp.noim();}
	private static void f_aaload() { JVMHelp.noim();}
	private static void f_baload() { JVMHelp.noim();}
	private static void f_caload() { JVMHelp.noim();}
	private static void f_saload() { JVMHelp.noim();}
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
	private static void f_lastore() { JVMHelp.noim(); /* jvm.asm */ }
	private static void f_fastore() { JVMHelp.noim();}
	private static void f_dastore() { JVMHelp.noim();}
	private static void f_aastore() { JVMHelp.noim();}
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

		return SoftFloat.float32_add(a, b);
	}
	private static void f_dadd() { JVMHelp.noim();}
	private static void f_isub() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lsub(long a, long b) {

		return a+(~b)+1;
	}
	private static int f_fsub(int a, int b) {

		return SoftFloat.float32_sub(a, b);
	}
	private static void f_dsub() { JVMHelp.noim();}
	private static void f_imul() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lmul(long a, long b) {
		
		boolean positive = true;
		if(a<0) {
			a = (~a)+1; 
			positive = false;
		  }
		if(b<0) {
			b = (~b)+1; 
			positive = !positive;
			}
		
		long aa = a;
		long res = 0;
		for(int i=0;i<64;i=i+8)
		{
			long bb = b;
			int am = ((int)aa) & 0x000000FF;
			
			for(int j=0;j<64;j=j+8)
			{
				int bm = ((int)bb) & 0x000000FF;
				
			    long sres = 0;
				if (i+j < 64) sres=f_lshl(0,am*bm,i+j);
				res += sres;
				bb = bb>>>8;
			}
			aa = aa>>>8;
		}
		if(!positive) res = (~res)+1;
		return res;
	}
	private static int f_fmul(int a, int b) {

		return SoftFloat.float32_mul(a, b);
	}
	private static void f_dmul() { JVMHelp.noim();}

	private static int f_idiv(int a, int b) { 

		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
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
		for (int i=0; i<32; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x80000000)!=0) {
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
		
	//	System.out.println("a="+((int)(a>>32))+" "+((int)a));
	//	System.out.println("b="+((int)(b>>32))+" "+((int)b));

		if(b==0x8000000000000000L)
			{
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
		for (int i=0; i<64; ++i) {
			c <<= 1;
			r <<= 1;
			if ((a & 0x8000000000000000L)!=0) {
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

		return SoftFloat.float32_div(a, b);
	}
	private static void f_ddiv() { JVMHelp.noim();}

	private static int f_irem(int a, int b) {

		if (b==0) {
			// division by zero exception
			Native.wrMem(Const.EXC_DIVZ, Const.IO_EXCPT);
			return 0;
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
		for (int i=0; i<32; ++i) {
			r <<= 1;
			if ((a & 0x80000000)!=0) {
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

		if(b==0x8000000000000000L)
		{
			if(a!=0x8000000000000000L) return a;
			else return 0;
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
		for (int i=0; i<64; ++i) {
			r <<= 1;
			if ((a & 0x8000000000000000L)!=0) {
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

		return SoftFloat.float32_rem(a, b);
	}
	private static void f_drem() { JVMHelp.noim();}
	private static void f_ineg() { JVMHelp.noim(); /* jvm.asm */ }
	private static long f_lneg(long a) {

		return ~a+1;
	}
	private static int f_fneg(int a) { 

                return a ^ 0x80000000;
	}
	private static void f_dneg() { JVMHelp.noim();}
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
			al = ah >>> (cnt-32);
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

		return SoftFloat.int32_to_float32(a);
	}
	private static void f_i2d() { JVMHelp.noim();}
	private static void f_l2i() { JVMHelp.noim(); /* jvm_long.inc */}
	private static void f_l2f() { JVMHelp.noim();}
	private static void f_l2d() { JVMHelp.noim();}
	private static int f_f2i(int a) {

		return SoftFloat.float32_to_int32_round_to_zero(a);
	}
	private static void f_f2l() { JVMHelp.noim();}
	private static void f_f2d() { JVMHelp.noim();}
	private static void f_d2i() { JVMHelp.noim();}
	private static void f_d2l() { JVMHelp.noim();}
	private static void f_d2f() { JVMHelp.noim();}

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
	private static int f_lcmp(long a, long b) {

		// is this really necessary?
		// Change by Peter & Christof
		int ah = (int)(a>>>32);
		int bh = (int)(b>>>32);
		//overflow, underflow, if a and b have different signs
		if(((ah & 0x80000000)==0)&&((bh & 0x80000000)!=0)) return 1;
		if(((ah & 0x80000000)!=0)&&((bh & 0x80000000)==0)) return -1;
		// I didn't have it in my first implementation
		
		a -= b;
		int al = (int) a;
		ah = (int) (a>>>32);

		if ((ah | al)==0) return 0;
		if ((ah & 0x80000000)==0) {
			return 1;
		} else {
			return -1;
		}
	}
	private static int f_fcmpl(int a, int b) {

		return SoftFloat.float32_cmpl(a, b);
	}
	private static int f_fcmpg(int a, int b) {

		return SoftFloat.float32_cmpg(a, b);
	}
	private static void f_dcmpl() { JVMHelp.noim();}
	private static void f_dcmpg() { JVMHelp.noim();}
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

// TODO: synchronized on functions that change JVM state (e.g. heap pointer)
static Object o;

	private static int f_new(int cons) {

		int ret;

		synchronized (o) {

			ret = GC.newObject(cons);
		}
		return ret;

	}

	static int f_newarray(int count, int type) {

		int ret;

		synchronized (o) {
			
			ret = GC.newArray(count, type);
		}
		return ret;

	}

	static int f_anewarray(int count, int cons) {

		// ignore cons (type info)
		// should be different for the GC!!!
		int ret;

		synchronized (o) {
			
			ret = GC.newArray(count, 1); //1..type not available=reference
		}
		return ret;
	}



	private static void f_arraylength() { JVMHelp.noim(); /* jvm.asm */ }

	private static Throwable f_athrow(Throwable t) {
		JVMHelp.wr("Exception ");
		String s = t.getMessage();
		if (s!=null) {
			JVMHelp.wr(s);			
		}
		JVMHelp.wr(" thrown\n");
		JVMHelp.wr("catch not implemented!");
		return t;
	}

	private static int f_checkcast(int objref, int cons) {

		if (objref==0) {
			return objref;
		}

		int p = Native.rdMem(objref+1);	// handle indirection
		p -= 4;							// start of class info

		for (;;) {
			if (p==cons) {
				return 1;
			} else {
				p = Native.rdMem(p+2);
				if (p==0) break;		// we are at Object
			}
		}
		
		throw new ClassCastException();
		
//		return objref;
	}
	private static int f_instanceof(int objref, int cons) {

		// TODO: check if it works for interfaces
		// TODO: simplify the code
		if (objref==0) {
			return 0;
		}
		int p = Native.rdMem(objref+1);	// handle indirection
		p -= 4;							// start of class info

		for (;;) {
			if (p==cons) {
				return 1;
			} else {
				p = Native.rdMem(p+2);
				if (p==0) break;		// we are at Object
			}
		}
		
		return 0;
	}


	private static int enterCnt;

	private static void f_monitorenter(int objAddr) {

/* is now in jvm.asm
*/
		// is there a race condition???????????????? when timer int happens NOW!
		Native.wr(0, Const.IO_INT_ENA);
		++enterCnt;
		// JVMHelp.wr('M');
	}

	private static void f_monitorexit(int objAddr) {

/* is now in jvm.asm
*/
		// JVMHelp.wr('E');
		--enterCnt;
if (enterCnt<0) {
	JVMHelp.wr('^');
	for (;;);
}
		if (enterCnt==0) {
			Native.wr(1, Const.IO_INT_ENA);
		}
	}


	private static void f_wide() { JVMHelp.noim();}
	
	private static int f_multianewarray() {

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
		pc += 2;	// now to dimensions
		int mp = Native.rdIntMem(fp+4);
		int start = Native.rdMem(mp)>>>10;	// address of method

		// memory is addressed in 32 bit words!
		
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
		int ref = Native.rdMem(ret);
		for (i=0; i<cnt; ++i) {
			Native.wrMem(f_newarray(cnt2,10), ref+i);
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
	private static void f_resE1() { JVMHelp.noim();}
	private static void f_resE2() { JVMHelp.noim();}
	private static void f_resE3() { JVMHelp.noim();}
	private static void f_resE4() { JVMHelp.noim();}
	private static void f_resE5() { JVMHelp.noim();}
	private static void f_resE6() { JVMHelp.noim();}
	private static void f_resE7() { JVMHelp.noim();}
	private static void f_resE8() { JVMHelp.noim();}
	private static void f_resE9() { JVMHelp.noim();}
	private static void f_resEA() { JVMHelp.noim();}
	private static void f_resEB() { JVMHelp.noim();}
	private static void f_resEC() { JVMHelp.noim();}
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
