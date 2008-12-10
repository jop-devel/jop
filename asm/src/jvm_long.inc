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
			bz	null_pointer	// following code
			nop
			nop

			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd

			nop	opd
			nop	opd
			ld_opd_16u			// CP index is the offset for the field
			add					// +objectref

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
			bz	null_pointer	// following code
			nop
			nop

			stmra				// read handle indirection
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

lastore:
			stm	a				// value_h
			stm c               // value_l
			stm	b				// index
			// arrayref is TOS
			dup					// for null pointer check
			dup					// for bound check, one cycle wait for bz
			bz	null_pointer	// 
			// we do the following in the
			// branch slot -> one more element
			// from the former dup on the stack
			ldi	1
			add					// arrayref+1
			stmra				// read ext. mem, mem_bsy comes one cycle later
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
			bnz	array_bound
			nop
			nop

			stmra				// read handle indirection
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

laload:                           
			stm	b				// index
			// arrayref is TOS
			dup					// for null pointer check
			dup					// for bound check, one cycle wait for bz
			bz	null_pointer			// we do the following in the
			// branch slot -> one more element
			// from the former dup on the stack
			ldi	1
			add					// arrayref+1

			stmra				// read array length
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

			stmra				// read handle indirection
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

