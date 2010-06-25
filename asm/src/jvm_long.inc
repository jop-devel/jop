//
//  This file is a part of JOP, the Java Optimized Processor
//
//  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

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

			stmrac				// read ext. mem, mem_bsy comes one cycle later

			ldi	1
			add					// address for next word

			wait
			wait

			ldmrd		 		// first word
			stm	a

			stmrac				// read ext. mem, mem_bsy comes one cycle later
			ldm	a				// first word again on stack
			wait
			wait
			ldmrd		 nxt	// second word

dconst_0:
lconst_0:	ldi	0
			ldi 0 nxt
lconst_1:	ldi	0
			ldi 1 nxt			// TOS is low part

l2i:		stm	a				// low part
			pop					// drop high word
			ldm	a nxt			// low on stack

i2l:		dup
			stm a
			ldi 31
			shr					// high part is a >> 31
			ldm a nxt			// low part is a

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
				// int addr = readOpd16u();
				// stack[++sp] = readMem(addr);
				// stack[++sp] = readMem(addr+1);

			nop opd
			nop	opd
			ld_opd_16u

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
				// int addr = readOpd16u();
				// writeMem(addr+1, stack[sp--]);
				// writeMem(addr, stack[sp--]);

			stm	b opd		// save low word
			stm	a opd		// save high word
			// could also be done to keep values on
			// stack, but first store at addr+1 (like 
			// in Java simulation)

			ld_opd_16u

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
				// int off = readOpd16u();
				// int ref = stack[sp];
				// if (useHandle) {
				//	// handle needs indirection
				//	ref = readMem(ref);
				// }
				// stack[sp] = readMem(ref+off);
				// stack[++sp] = readMem(ref+off+1);

			dup					// null pointer check
			nop					// could be interleaved with
			bz	long_null_pointer	// following code
			nop
			nop

			stmraf				// read handle indirection
			wait				// for the GC
			wait
			ldmrd

			nop	opd
			nop	opd
			ld_opd_16u			// CP index is the offset for the field
			add					// +objectref

			dup

			stmraf
			ldi	1
			add
			stm	a
			wait
			wait
			ldmrd				// read high word

			ldm	a
			stmraf
			wait
			wait
			ldmrd	nxt			// read low word

long_null_pointer:
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_np
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

putfield_long:
				// int off = readOpd16u();
				// int val_l = stack[sp--];
				// int val_h = stack[sp--];
				// int ref = stack[sp--];
				// if (useHandle) {
				// 	// handle needs indirection
				// 	ref = readMem(ref);
				// }
				// writeMem(ref+off, val_h);
				// writeMem(ref+off+1, val_l);

			stm	b				// save low word
			stm	a				// save high word

			dup					// null pointer check
			nop					// could be interleaved with
			bz	long_null_pointer	// following code
			nop
			nop

			stmraf				// read handle indirection
			wait				// for the GC
			wait
			ldmrd

			nop	opd
			nop	opd
			ld_opd_16u			// CP index is the offset for the field
			add					// +objectref

			dup

			stmwa				// write ext. mem address
			ldm	a				// restore value
			stmwdf				// write ext. mem data
			ldi	1
			add
			wait
			wait

			stmwa
			ldm	b
			stmwdf
			wait
			wait
			nop	nxt

long_null_pointer_arr_store:
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_np
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

//
//	array bound exception
//		now only used by long bytecodes
//

array_bound_store:
			wait				// just for shure if we jump during
			wait				// a memory transaction to this point
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_ab
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

dastore:
lastore:
			stm	a				// value_h
			stm c               // value_l
			stm	b				// index
			// arrayref is TOS
			dup					// for null pointer check
			dup					// for bound check, one cycle wait for bz
			bz	long_null_pointer_arr_store	// 
			// we do the following in the
			// branch slot -> one more element
			// from the former dup on the stack
			ldi	1
			add					// arrayref+1
			stmraf				// read ext. mem, mem_bsy comes one cycle later
			wait				// is this ok? - wait in branch slot
			wait
			ldmrd		 		// read ext. mem (array length)

			ldi	1
			sub					// length-1
			ldm	b				// index
			sub					// TOS = length-1-index
			ldm	b				// check if index is negativ
			or					// is one of both checks neagtv?
         	ldi	-2147483648		//  0x80000000
			and
			nop
			bnz	array_bound_store
			nop
			nop

			stmraf				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
			ldm	b
			ldi 1
			shl                 // index*2
			add					// index*2+arrayref
			stm d
			ldm d
			stmwa				// write ext. mem address
			ldm	c
			stmwd				// write ext. mem data
			ldm d
			ldi 1
			add					// index*2+arrayref+1
			wait
			wait
			stmwa				// write ext. mem address
			ldm	a
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

long_null_pointer_arr:
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_np
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

//
//	array bound exception
//		now only used by long bytecodes
//

array_bound:
			wait				// just for shure if we jump during
			wait				// a memory transaction to this point
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_ab
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

daload:
laload:                           
			stm	b				// index
			// arrayref is TOS
			dup					// for null pointer check
			dup					// for bound check, one cycle wait for bz
			bz	long_null_pointer_arr			// we do the following in the
			// branch slot -> one more element
			// from the former dup on the stack
			ldi	1
			add					// arrayref+1

			stmraf				// read array length
			wait				// is this ok? - wait in branch slot
			wait
			ldmrd		 		// read ext. mem (array length)er	// 

			ldi	1
			sub					// length-1
			ldm	b				// index
			sub					// TOS = length-1-index
			ldm	b				// check if index is negative
			or					// is one of both checks negative?
         	ldi	-2147483648		//  0x80000000
			and
			nop
			bnz	array_bound
			nop
			nop

			stmraf				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
			ldm	b	
			ldi 1
			shl                 // index*2
			add					// index*2+arrayref
			dup
			stm d
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 
			ldm d
			ldi 1
			add					// index*2+arrayref+1
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd 	nxt	// read ext. mem

lneg: // WCET=8+26=34
			ldi -1
			xor
			stm a
			ldi -1
			xor
			ldm a
			ldi 0
			ldi 1
			// fall through to addition
ladd: // WCET=26
			stm a // bl
			stm b // bh
			stm c // al
			stm d // hh
			
			ldm c // al>>>1
			ldi 1
			ushr

			ldm a // bl>>>1
			ldi 1
			ushr

			add // (al>>>1) + (bl>>>1)

			ldm a // (al & bl & 1)
			ldm c
			and
			ldi 1
			and

			add // (al>>>1) + (bl>>>1) + (al & bl & 1)

			ldi 31 // carry, ((al>>>1) + (bl>>>1) + (al & bl & 1)) >>> 31
			ushr

			ldm b
			add
			ldm d
			add // ah+bh+carry

			ldm a
			ldm c
			add nxt

lsub: // WCET=38
			ldi -1 // bl = ~bl
			xor
			stm a // bl
			ldi -1 // bh = ~bh
			xor			
			stm b // bh
			stm c // al
			stm d // ah
			
			ldm c // al>>>1
			ldi 1
			ushr

			ldm a // bl>>>1
			ldi 1
			ushr

			add // (al>>>1) + (bl>>>1)

			ldm a // (al&1 + bl&1 + 1)>>1
			ldi 1
			and
			ldm c
			ldi 1
			and
			add
			ldi 1
			add
			ldi 1
			shr

			add // (al>>>1) + (bl>>>1) + (al&1 + bl&1 + 1)>>1

			ldi 31
			ushr

			ldm b
			add
			ldm d
			add // ah+bh+carry

			ldm a
			ldm c
			add
			ldi 1
			add nxt
			
lcmp: // WCET=80 (?)
			stm a // bl
			stm b // bh
			stm c // al
			stm d // ah

lcmp_chk_overflow1:
			ldm d // ah < 0
			ldi 31
			shr
			ldm b
			ldi 31 // bh >= 0
			shr
			ldi -1
			xor
			or // !(ah < 0 || bh >= 0)
			nop
			bnz lcmp_chk_overflow2
			nop
			nop
			ldi 1 nxt

lcmp_chk_overflow2:
			ldm d // ah >= 0
			ldi 31
			shr
			ldi -1
			xor
			ldm b // bh < 0
			ldi 31
			shr
			or // !(ah >= 0 || bh < 0)
			nop
			bnz lcmp_sub
			nop
			nop
			ldi -1 nxt
lcmp_sub:
			ldm a
			ldi -1 // bl = ~bl
			xor
			stm a // bl
			ldm b
			ldi -1 // bh = ~bh
			xor			
			stm b // bh
			
			ldm c // al>>>1
			ldi 1
			ushr

			ldm a // bl>>>1
			ldi 1
			ushr

			add // (al>>>1) + (bl>>>1)

			ldm a // (al&1 + bl&1 + 1)>>1
			ldi 1
			and
			ldm c
			ldi 1
			and
			add
			ldi 1
			add
			ldi 1
			shr

			add // (al>>>1) + (bl>>>1) + (al&1 + bl&1 + 1)>>1

			ldi 31
			ushr

			ldm b
			add
			ldm d
			add // ah+bh+carry
			stm d // hi(a-b)

			ldm a
			ldm c
			add
			ldi 1
			add
			stm c // lo(a-b)

			ldm c
			ldm d
			or
			nop
			bnz lcmp_notequal
			nop
			nop
			ldi 0 nxt

lcmp_notequal:
			ldm d
			ldi 31
			shr
			nop
			bnz lcmp_less
			nop
			nop
			ldi 1 nxt
lcmp_less:
			ldi -1 nxt


lushr: // WCET=28
			ldi 63 // cnt &= 0x3f
			and
			
			dup
			bnz lushr_not0
			nop
			nop
			pop nxt

			nop // dummy nop to keep offtbl.vhd happy

lushr_not0:
			dup // cnt > 31?
			ldi 32
			sub
			ldi 31
			ushr
			nop
			bnz lushr_le31
			stm c // cnt
			stm a // al

lushr_gt31:
			stm b // ah

			ldi 0 // ah = 0

			ldm b // al = ah >>> (cnt-32)
			ldm c
			ldi 32
			sub
			ushr nxt

			nop // dummy nop to keep offtbl.vhd happy
			nop // dummy nop to keep offtbl.vhd happy
			nop // dummy nop to keep offtbl.vhd happy

lushr_le31:
			stm b // ah

			ldm b // ah = ah >>> cnt
			ldm c
			ushr

			ldm a // al = (al >>> cnt) + (ah << (32-cnt))
			ldm c
			ushr
			ldm b
			ldi 32
			ldm c
			sub
			shl
			add nxt

lshr: // WCET=28
			ldi 63 // cnt &= 0x3f
			and
			
			dup
			bnz lshr_not0
			nop
			nop
			pop nxt

			nop // dummy nop to keep offtbl.vhd happy

lshr_not0:
			dup // cnt > 31?
			ldi 32
			sub
			ldi 31
			ushr
			nop
			bnz lshr_le31
			stm c // cnt
			stm a // al

lshr_gt31:
			stm b // ah

			ldm b // ah = ah >> 31
			ldi 31
			shr

			ldm b // al = ah >> (cnt-32)
			ldm c
			ldi 32
			sub
			shr nxt

			nop // dummy nop to keep offtbl.vhd happy

lshr_le31:
			stm b // ah

			ldm b // ah = ah >> cnt
			ldm c
			shr

			ldm a // al = (al >>> cnt) + (ah << (32-cnt))
			ldm c
			ushr
			ldm b
			ldi 32
			ldm c
			sub
			shl
			add nxt

lshl: // WCET=28
			ldi 63 // cnt &= 0x3f
			and
			
			dup
			bnz lshl_not0
			nop
			nop
			pop nxt

			nop // dummy nop to keep offtbl.vhd happy

lshl_not0:
			dup // cnt > 31?
			ldi 32
			sub
			ldi 31
			ushr
			nop
			bnz lshl_le31
			stm c // cnt
			stm a // al

lshl_gt31:
			stm b // ah

			ldm a // ah = al << (cnt-32)
			ldm c
			ldi 32
			sub
			shl

			ldi 0 nxt // al = 0

			nop // dummy nop to keep offtbl.vhd happy
			nop // dummy nop to keep offtbl.vhd happy
			nop // dummy nop to keep offtbl.vhd happy

lshl_le31:
			stm b // ah

			ldm b // ah = (ah << cnt) + (al >>> (32-cnt))
			ldm c
			shl
			ldm a
			ldi 32
			ldm c
			sub
			ushr
			add

			ldm a // al = al << cnt
			ldm c
			shl nxt

lxor: // WCET=8
			stm a
			stm b
			stm c
			ldm b
			xor
			ldm a
			ldm c
			xor nxt

land: // WCET=8
			stm a
			stm b
			stm c
			ldm b
			and
			ldm a
			ldm c
			and nxt

lor: // WCET=8
			stm a
			stm b
			stm c
			ldm b
			or
			ldm a
			ldm c
			or nxt
