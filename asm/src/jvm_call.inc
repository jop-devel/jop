//
//	call.inc
//
//		include file for invoke and return functions
//
//	2004-08-19	extracted from jvm.asm
//
//	TODO

//
//	local vars for tmp storage
//
old_mp			?
old_vp			?

old_cp			?		// for now save it on stack

start		?
end			?
args		?
varcnt		?


invokespecial:			// is it really equivilant ????? (not really)
						// there is an object ref on stack (but arg counts for it)
						// is called for privat methods AND <init>!!!
invokestatic:

						// mp = readMem(cp+idx);
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

invoke_main:					// jmp with pointer to pointer to mp on TOS

			stmra				// read 'real' mp
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

jopsys_invoke:					// called from Startup.boot() with pointer to main() method struct
invoke:							// jmp with mp on TOS (pointer to method struct)

			ldi	1
//			nop			// mp is != 0
			bnz	invoke_vpsave
			ldvp				// save vp
			stm	old_vp			// in branch slots



invokeinterface:

		//	int idx = readOpd16u();
		//	readOpd16u();						// read historical argument count and 0
		//	int off = readMem(cp+idx);			// index in interface table
		//	int args = off & 0xff;				// this is args count without obj-ref
		//	off >>>= 8;
		//	int ref = stack[sp-args];
		//	int vt = readMem(ref-1);			// pointer to virtual table in obj-1
		//	int it = readMem(vt-1);				// pointer to interface table one befor vt
		//	int mp = readMem(it+off);
		//	invoke(mp);

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add	opd

			stmra	opd			// read constant
			nop
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

			ldsp		// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1		// 'real' sp			da sp auf rd adr zeigt
			add
			ldm	a
			sub

// could do the vp save in old_vp here
			ldvp
			stm	old_vp
			stvp			// address in vp
			nop				// ???
			ld0				// read objectref
			// objectref is now on TOS

			dup					// null pointer check
			nop					// could be interleaved with
			bz	null_pointer	// following code
			ldi	1				// at address ref-1 is pointer to method table
			sub
			stmra				// read pointer to method table
			nop
			wait
			wait
			ldmrd		 		// vt on TOS

			ldi	1				// pointer to interface table
			sub					// befor method table
			stmra				// read interface table address
			nop
			wait
			wait
			ldmrd		 		// it on TOS

			ldm	b
			add					// add offset
			stmra				// read method pointer
			nop					// from interface table
			wait
			wait
			ldmrd		 		// mp on TOS

			ldi	1
//			nop		// mp is != 0
			bnz	invoke_vpsave
			nop
			nop




invokevirtual:

		//	int idx = readOpd16u();
		//	int off = readMem(cp+idx);	// index in vt and arg count (-1)
		//	int args = off & 0xff;
		//	off >>>= 8;
		//	int ref = stack[sp-args];
		//	int vt = readMem(ref-1);
		//	invoke(vt+off);

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read constant
			nop
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

			ldsp		// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1		// 'real' sp			da sp auf rd adr zeigt
			add
			ldm	a
			sub

			ldvp
			stm	old_vp
			stvp			// address in vp
			nop				// ???
			ld0				// read objectref
			// objectref is now on TOS

			dup					// null pointer check
			nop					// could be interleaved with
			bz	null_pointer	// following code
			ldi	1			// at address ref-1 is pointer to method table
			sub

			stmra				// read pointer to method table
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

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
			stmra			// read cp+arrg/locals count
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

// start bytecode load....
			ldm	mp
			stmra

			ldm	cp
			stm	old_cp

			wait
			wait
			ldmrd		 		// read ext. mem

					// int len = start & 0x03ff;
					// start >>>= 10;
			stbcrd				// start bytecode load


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
				// stack[++sp] = pc;
				// stack[++sp] = old_vp;
				// stack[++sp] = old_cp;
				// stack[++sp] = old_mp;


			ldm	old_sp
			ldjpc
// without cache load jpc here
		ldi 0
		stjpc
			ldm	old_vp
			ldm	old_cp
			ldm	old_mp

// wait on bytecode load from memory system
			wait
			wait

// TODO: use ldbcstart in jpc save and in return for correct cache handling
//			ldbcstart			// for future cache
//			stjpc
//			nop
//			nop
//			nop
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
			stmra
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
			stjpc

			nop				// written in adr/read stage!
			stsp			// last is new sp
			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr
			ldm	a 			// load return value
			wait			// wait on bytecode load from memory system
			wait
			nop	nxt

dreturn:
lreturn:
			stm	a			// store return value
			stm	b
			dup				// mp is on tos
			stmra
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
			stjpc

			nop				// written in adr/read stage!
			stsp			// last is new sp
			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr
			ldm	b
			ldm	a 			// load return value
			wait			// wait on bytecode load from memory system
			wait
			nop	nxt

return:
			dup				// mp is on tos
			stmra
			stm	mp
			stm	cp
			stvp
			wait			// wait for mem(mp)
			wait
			ldmrd		 	// read ext. mem
			stbcrd			// start bytecode load
			stjpc

			nop				// written in adr/read stage!
			stsp			// last is new sp
			pop				// flash tos, tos-1 (registers)
			pop				// sp must be two lower, points to rd adr
			wait			// wait on bytecode load from memory system
			wait
			nop	nxt


