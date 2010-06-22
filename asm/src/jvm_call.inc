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
//	call.inc
//
//		include file for invoke and return functions
//
//	2004-08-19	extracted from jvm.asm
//	
//	see changlog in jvm.asm
//

//
//	local vars for tmp storage
//
old_mp			?
old_vp			?
old_jpc			?

old_cp			?		// for now save it on stack

start		?
end			?
args		?
varcnt		?


invokespecial:			// is it really equivalent ????? (not really)
						// there is an object ref on stack (but arg counts for it)
						// is called for privat methods AND <init>!!!
invokestatic:

						// mp = readMem(cp+idx);
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

invoke_main:					// jmp with pointer to pointer to mp on TOS

			stmrac				// read 'real' mp
			wait
			wait
			ldmrd		 		// read ext. mem

jopsys_invoke:					// called from Startup.boot() with pointer to main() method struct
invoke:							// jmp with mp on TOS (pointer to method struct)

			jmp	invoke_vpsave
			ldvp				// save vp
			stm	old_vp			// in branch slots



invokeinterface:

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add	opd

			stmrac	opd			// read constant
			wait
			wait
			ldmrd		 		// off on TOS

			dup
			ldi	255
			and
			stm	a				// arg count (without objectref)
			ldi	8
			ushr
			stm	b				// offset in method table

			ldsp				// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1				// 'real' sp
			add
			ldm	a
			sub

			star				// address of objectref
			nop
			ldmi				// read objectref
			// objectref is now on TOS

			dup					// null pointer check
			nop
			bnz	interface_pointer_ok
			ldvp				// save vp in branch slot
			stm	old_vp

			jmp	null_pointer

interface_pointer_ok:
			ldi	1				// at address ref+1 (in the handle) is the pointer to method table
			add
			stmraf				// read pointer to method table
			wait
			wait
			ldmrd		 		// vt on TOS

			ldi	1				// pointer to interface table
			sub					// befor method table
			stmrac				// read interface table address
			wait
			wait
			ldmrd		 		// it on TOS

			ldm	b
			add					// add offset
			stmrac				// read method pointer
			wait				// from interface table
			wait
			ldmrd		 		// mp on TOS

			jmp	invoke_vpsave
			nop
			nop

invokesuper:					// this was invokespecial, replaced in JOPizer when calling super methods
			ldm	cp opd
			dup	opd
			ld_opd_16u
			add

			stmrac				// read constant
			wait
			wait
			ldmrd		 		// read ext. mem

			dup
			ldi	255
			and
			stm	a				// arg count (without objectref)
			ldi	8
			ushr
			stm	b				// offset in method table

			ldsp				// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1				// 'real' sp
			add
			ldm	a
			sub

			star				// address of objectref
			nop
			ldmi				// read objectref
			// objectref is now on TOS

			nop					// null pointer check
			nop
			bz	null_pointer
			ldvp				// save vp in branch slot
			stm	old_vp

								// cp is already loaded!
			ldi	1				// at address cp-1 is the pointer to class structure
			sub

			stmraf				// read pointer to class of method
			wait
			wait
			ldmrd		 		// read ext. mem

			ldi	3				// superclass pointer is at offset 3
			add

			stmrac				// read pointer to superclass
			wait
			wait
			ldmrd		 		// read ext. mem

			ldi	5				// "super" method table is at superclass+5
			add

			jmp	invoke_addoffset
			nop
			nop

//
//	null pointer exception
//		only used by invoke instructions
//

null_pointer:
			wait				// just for shure if we jump during
			wait				// a memory transaction to this point
			ldi	io_exc
			stmwa				// write ext. mem address
			ldi	exc_np
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

invokevirtual:

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmrac				// read constant
			wait
			wait
			ldmrd		 		// read ext. mem

			dup
			ldi	255
			and
			stm	a				// arg count (without objectref)
			ldi	8
			ushr
			stm	b				// offset in method table

			ldsp				// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1				// 'real' sp
			add
			ldm	a
			sub

			star				// address of objectref
			nop
			ldmi				// read objectref
			// objectref is now on TOS

			dup					// null pointer check
			nop
			bnz	invoke_pointer_ok
			ldvp				// save vp in branch slot
			stm	old_vp

			jmp	null_pointer

invoke_pointer_ok:
			ldi	1				// at address ref+1 (in the handle) is the pointer to method table
			add

			stmraf				// read pointer to method table
			wait
			wait
			ldmrd		 		// read ext. mem

invoke_addoffset:
			ldm	b
			add					// add offset



invoke_vpsave:					// vp is allready saved in old_vp
								// used for invokestatic and invokevirtual

//	mp is on TOS
//		we will not read mem[mp] now,
//		this is done in load_bc
			dup
			ldi	1
			add
			stmrac			// read cp+arrg/locals count
			ldm	mp
			stm	old_mp
			stm	mp

			wait
			wait
			ldmrd			// cp...

					// cp = readMem(mp+1);
					// int locals = (cp>>>5) & 0x01f;
					// int args = cp & 0x01f;
					// cp >>>= 10;

// get 'old' (= current) bc start address
// and store relative jpc in stack frame
			ldjpc
			ldbcstart
			sub		// relative pc
			stm	old_jpc
// TODO: ldjpc and subtraction could be moved down

// get method start address and length

			ldm	mp
			stmrac

			ldm	cp
			stm	old_cp

			wait
			wait
			ldmrd		 		// read ext. mem

					// int len = start & 0x03ff;
					// start >>>= 10;
// start bytecode load....
			stbcrd


			dup
			ldi	31
			and
			stm	args
			ldi	5
			ushr
			dup
			ldi	31
			and
			stm	varcnt
			ldi	5
			ushr
			stm	cp


old_sp			?
real_sp			?


//
// tos and tos-1 are allready written back to memory
//
				// int old_sp = sp-args;
				// vp = old_sp+1;
				// sp += varcnt;

			ldsp	// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1	// 'real' sp			da sp auf rd adr zeigt
			add
			dup
			ldm	args
			sub
			stm	old_sp
			ldm	old_sp
			ldi	1
			add
			stvp
			// sp still on TOS
			ldm	varcnt		// 'real' varcnt (=locals-args)
			add

			nop			// written in adr/read stage!
			stsp
			pop			// flush reg., sp reg is sp-2 again
			pop			// could really be optimized :-(

				// stack[++sp] = old_sp;
				// stack[++sp] = cache.corrPc(pc);
					// in VarBlockCache:
					// save pc relative to start address of the method
					// return (pc - currentBlock*blockSize) & mask;
				// stack[++sp] = old_vp;
				// stack[++sp] = old_cp;
				// stack[++sp] = old_mp;
				// 
				// pc = cache.invoke(start, len);
					// in VarBlockCache:
					// test or load cache, return start address (block number * size)
					// int off = testCache(start, len);
					// return off;

			ldm	old_sp
			ldm old_jpc
// without cache load jpc here
//		ldi 0
//		stjpc
// bc start address is a 'few' cycles after stbcrd available
			ldbcstart			// for future cache
			stjpc
			ldm	old_vp
			ldm	old_cp
			ldm	old_mp

// wait on bytecode load from memory system
			wait
			wait
			nop	nxt
// end load_bc

//
//	thats the pipeline delay from stjpc - jpc -
//	rdaddress - jpaddr - pc!
//
//		could be simpler if a different command to store
//		write address for jbc (or use DMA in mem.vhd!)
//
//			stjpc
//			nop
//			nop
//			nop
//			nop	nxt
//

areturn:
freturn:
ireturn:
			stm	a			// store return value
			dup				// mp is on tos
			stmrac
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
//			stjpc
			stm	old_jpc		// save realtive pc

			nop				// written in adr/read stage!
			stsp			// last is new sp
			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr

			ldbcstart			// start block of method
			ldm	old_jpc			// plus relative jpc
			add
			stjpc

			ldm	a 			// load return value
// wait on bytecode load from memory system
			wait
			wait
			nop	nxt

dreturn:
lreturn:
			stm	a			// store return value
			stm	b
			dup				// mp is on tos
			stmrac
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
//			stjpc
			stm	old_jpc		// save realtive pc

			nop				// written in adr/read stage!
			stsp			// last is new sp
			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr

			ldbcstart			// start block of method
			ldm	old_jpc			// plus relative jpc
			add
			stjpc

			ldm	b
			ldm	a 			// load return value
// wait on bytecode load from memory system
			wait
			wait
			nop	nxt

return:
				// mp = stack[sp--];
				// cp = stack[sp--];
				// vp = stack[sp--];
				// pc = stack[sp--];
				// sp = stack[sp--];
				// 
				// int start = readMem(mp);
				// int len = start & 0x03ff;
				// start >>>= 10;
				// pc = cache.ret(start, len, pc);
					// in VarBlockCache:
					// int off = testCache(start, len);
					// return (off+pc) & mask;



			dup				// mp is on tos
			stmrac
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
//			stjpc
			stm	old_jpc		// save realtive pc

			nop				// written in adr/read stage!
			stsp			// last is new sp

			ldbcstart			// start block of method
			ldm	old_jpc			// plus relative jpc
			add
			stjpc

			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr
// wait on bytecode load from memory system
			wait
			wait
			nop	nxt
// end load_bc

