//
//	Include file for long bytecodes
//

lreturn:
dreturn:
			stm	a	// return value
			stm b
			stm	mp
			stm	cp
			stvp

			stm	new_jpc
			nop			// written in adr/read stage!
			stsp	// last is new sp
			pop		// flash tos, tos-1 (registers)
			pop		// sp must be two lower, points to rd adr
			ldm b
			ldm	a 
			ldi	1
			nop
			bnz	load_bc
			nop
			nop

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
			ldi 1 nxt			// is TOS low part? yes... see ldc2_w and JOPWriter

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


// dload:
// lload:		nop
// 			ld				// high word
// 			ldvp
// 			dup
// 			stm	a
// 			ldi	1
// 			add
// 			stvp
// 			nop	
// 			ld				// low word
// 			ldm	a			// restore vp
// 			stvp opd
// 			nop nxt

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

// dstore:
// lstore:		ldvp
//			dup
//			stm	a
//			ldi	1
//			add
//			stvp
//			nop	
//			st				// low word
//			ldm	a			// restore vp
//			stvp
//			nop opd
//			st	nxt			// high word

