//
//	Include file for long bytecodes
//
//	Arrangement of longs in the memory:
//		as usual in Java - high word first (at lower address)
//		thet stack grows to higher addresses => TOS is LOWER part
//		of long!

// lreturn: is in jvm_call.inc

ldc2_w:	
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add
			dup

			stmra				// read ext. mem, mem_bsy comes one cycle later

			ldi	1
			add					// address for next word

			wait
			wait

			ldmrd		 		// first word
			stm	a

			stmra				// read ext. mem, mem_bsy comes one cycle later
			ldm	a				// first word again on stack
			wait
			wait
			ldmrd		 nxt	// second word

lconst_0:	ldi	0
			ldi 0 nxt
lconst_1:	ldi	0
			ldi 1 nxt			// TOS is low part

l2i:		stm	a				// low part
			pop					// drop high word
			ldm	a nxt			// low on stack

dload_0:
lload_0:	ld0				// high word
			ld1 nxt			// low word
dload_1:
lload_1:	ld1
			ld2 nxt
dload_2:
lload_2:	ld2
			ld3 nxt
dload_3:
lload_3:	ldvp			// there is no ld4
			dup
			ldi	1
			add
			stvp
			stm	a
			ld2	
			ld3	
			ldm	a			// restore vp
			stvp
			nop nxt

dload:
lload:		ldvp
			dup opd
			ld_opd_8u
			add
			stvp
			stm	a
			ld0
			ld1
			ldm	a
			stvp
			nop	nxt

dstore_0:
lstore_0:	st1				// low word
			st0 nxt			// high word
dstore_1:
lstore_1:	st2
			st1 nxt
dstore_2:
lstore_2:	st3
			st2 nxt
dstore_3:
lstore_3:	ldvp			// there is no ld4
			dup
			ldi	1
			add
			stvp
			stm	a
			st3	
			st2	
			ldm	a			// restore vp
			stvp
			nop nxt

dstore:
lstore:		ldvp
			dup opd
			ld_opd_8u
			add
			stvp
			stm	a
			st1
			st0
			ldm	a
			stvp
			nop	nxt

getstatic_long:
				// int idx = readOpd16u();
				// int addr = readMem(cp+idx);
				// stack[++sp] = readMem(addr);
				// stack[++sp] = readMem(addr+1);

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 	// read ext. mem
long_read:
			dup

			stmra
			ldi	1
			add
			stm	a
			wait
			wait
			ldmrd				// read high word

			ldm	a
			stmra
			wait
			wait
			ldmrd	nxt			// read low word


putstatic_long:
				// int idx = readOpd16u();
				// int addr = readMem(cp+idx);
				// writeMem(addr+1, stack[sp--]);
				// writeMem(addr, stack[sp--]);

			stm	b				// save low word
			stm	a				// save high word
			// could also be done to keep values on
			// stack, but first store at addr+1 (like 
			// in Java simulation)

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 	// read ext. mem
			dup

			stmwa				// write ext. mem address
			ldm	a				// restore value
			stmwd				// write ext. mem data
			ldi	1
			add
			wait
			wait

			stmwa
			ldm	b
			stmwd
			wait
			wait
			nop	nxt

getfield_long:
				// int idx = readOpd16u();
				// int off = readMem(cp+idx);
				// int ref = stack[sp];
				// if (useHandle) {
				// 	ref = readMem(ref);
				// }
				// stack[sp] = readMem(ref+off);
				// stack[++sp] = readMem(ref+off+1);

			dup					// null pointer check
			nop					// could be interleaved with
			bz	null_pointer	// following code
			nop
			nop

#ifdef HANDLE
			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
#endif
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd			 	// read offset

			add					// +objectref
			// address!=0 for the bnz
			dup
			bnz	long_read
			nop
			nop



putfield_long:
				// int idx = readOpd16u();
				// int off = readMem(cp+idx);
				// int val_l = stack[sp--];
				// int val_h = stack[sp--];
				// int ref = stack[sp--];
				// if (useHandle) {
				// 	ref = readMem(ref);
				// }
				// writeMem(ref+off, val_h);
				// writeMem(ref+off+1, val_l);

			stm	b				// save low word
			stm	a				// save high word

			dup					// null pointer check
			nop					// could be interleaved with
			bz	null_pointer	// following code
			nop
			nop

#ifdef HANDLE
			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
#endif
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd			 	// read offset

			add					// +objectref
			dup

			stmwa				// write ext. mem address
			ldm	a				// restore value
			stmwd				// write ext. mem data
			ldi	1
			add
			wait
			wait

			stmwa
			ldm	b
			stmwd
			wait
			wait
			nop	nxt

