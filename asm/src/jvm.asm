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
//	jvm.asm
//
//		JVM for JOP3
//
//	2001-10-24	first version works with Jopa (prime is running)
//	2001-10-31	download of bc and ram from serial line (init)
//	2001-11-30	change mem acces to direct
//	2001-11-31	change to new 'class file' (in memory)
//	2001-12-05	new shift loop
//				nop !befor! stsp to write correct A value
//	2001-12-06	init sp
//	2001-12-07	only one jbr, decoding in bcfetch, i2c
//				fixed download size
//	2001-12-08	changes for ldp, stp instruction coding (8 bit intruction set!)
//	2001-12-10	automatic load of bc from external memory if prog.
//				does not fit in internal ram
//	2002-01-16	imul, idiv, irem, ishr
//	2002-02-25	areturn
//	2002-03-22	use the booth hw multiplier
//	2002-03-24	new JOP instructions shr, shl, ushr
//				bc only in extern memory, autoincrement jpc on stbc
//	2002-05-10	dup2
//	2002-07-24	added special bytecodes jopsys_*
//	2002-07-26	sys_rd/wr* from invokestatic removed
//				method table => single word const
//				call JVM.f() for not implemented byte codes
//	2002-07-27	changed len field of method struct to 10 bit
//	2002-08-02	changed max_words from 4096 to 8192
//	2002-10-08	use JVM.java for new
//	2002-10-11	call JVM.java for anewarray
//	2002-10-21	added if(non)null, if_acmp
//	2002-12-02	use new instruction wait for mem io
//	2003-08-13	merged jvm.asm and jvmflash.asm, use C preprocessor
//	2003-08-14	changed jbc write to a single 32 bit write (jbc does byte stuff)
//	2003-09-11	jopsys function for save and restore of stack in RtThread.schedule()
//	2003-09-13	invokespecial still like invokestatic
//		Ein offenes Problem! Aber private geht jetzt.
//				jjp and jjhp now point to first method
//	2003-09-15	cbsf-a-load/store for String class (all array elements are one word)
//	2003-10-06	changed max_words from 8192 to 16384, and flash address for Java
//				program at 0x80000
//	2003-10-23	long load_n, store_n, const_n, l2i added, ldc2_w error corretion
//	2003-10-27	invokeinterface
//	2004-01-18	ldc_w, pop2; float load, store and return; NOT tested!
//	2004-02-07	null pointer check in xaload/xastore, put/getfield and invoke,
//				array check, div/rem moved to JVM.java
//	2004-02-12	added instruction ld_opd_8u and ld_opd_16u
//	2004-03-12	added support for long: lreturn, lload, lstore
//	2004-04-06	first two instructions nop because of change in fetch.vhd
//				stjpc to nxt pipeline is now one longer (simpler mux for jbc rdaddr)
//	2004-05-24	jopsys_invoke to call main() from Startup.boot()
//	2004-10-08	single instruction to start mul (with a and b as operands)
//	2004-12-11	Enhancements in array access
//	2004-09-16	new bc read hardware (jvm_call.inc)
//	2005-01-10	changes for bytecode cache (jvm_call.inc)
//	2005-02-05	include version number
//	2005-02-18	switch for simulation version
//	2005-04-27	dup_x2
//	2005-05-12	remove nops after mem rd/wr start. the 'io' wr
//				generates the first bsy cycle (extension.vhd)
//	2005-05-30	wishbone interface (extension.vhd and wb_top.vhd)
//	2005-06-13	move newarray to JVM.java
//				use indirection (handle) for objects and arrays
//	2005-06-14	added multianewarray to JVM.java, version is 
//				again without handles
//	2005-06-20  use indirection, GC info in class struct
//	2005-07-28	fix missing indirection bug in thread stack move (int2ext and ext2int)
//	2005-08-13	moved null pointer check in xaload/store to check the handle!
//	2005-08-16	new file/download format with a size field in the first word
//	2005-08-27	added boot from USB interface (dspio board)
//	2005-12-01	IO devices are memory mapped - no more stioa, stiod, ldiod
//	2005-12-20	Changed dspio devices (USB) to SimpCon
//	2006-01-11	Generate HW exception and invoke JVMHelp.exception()
//	2006-01-12	Additional register for int. memory addressing (ar)
//				Instructions: star, stmi, ldmi
//				removed stioa, stiod, and ldiod
//	2006-01-20	add get/put field/static _ref/_long
//	2006-01-22	add type info for newarray (for long)
//	2006-01-23	use offset instead of cp index for get/putfield
//	2006-06-15	enhanced memory/cache interface (less cycles)
//	2006-11-04	move mtab pointer and array length to the handle
//				little optimization in array load/store
//	2006-12-27	add a special bytecode for Peter's single path
//				programming
//	2006-12-29	2K ROM, laload, lastore enabled again, dup2_x1, dup2_x2
//	2006-12-30	add instanceof to invoke JVM.java with constant on TOS
//	2007-03-17	new VHDL structure: jopcpu and more records (SimpCon)
//	2007-04-14	iaload and iastore in hardware (mem_sc.vhd)
//	2007-05-28	putfield_ref and putstatic_ref in JVM.java
//  2007-06-01  added multiprocessor startup (CP)
//				aastore in JVM.java
//	2007-06-17	new instruction jopsys_memcpy, jopsys_cond_move disabled
//				speed-up ext2int and int2ext
//	2007-08-31	non wrapping stack pointer, version at new address (62)
//				start stack at 64 instead of 128
//	2007-09-02	new instructions for hardware floating point operation with FPU
//	2007-11-21	stack tracing enabled, more space in stack.vhd
//				use 33 bit for the comparison (compare bug for diff > 2^31 corrected)
//	2007-11-22	update for JOP CMP version (by CP)
//	2007-12-03	new interrupt logic
//	2008-02-19	WP: field access in HW
//	2008-02-20	IO modules after the memory controller (mem_sc) to keep HWO working
//	2008-02-24	Changed license to GPL
//	2008-03-03	Added scratchpad RAM
//	2008-03-04	correct MUX selection
//	2008-03-11	Interrupt enable also in bcfetch (bug fix)
//	2008-06-11	Remove offtbl adjustment nops
//	2008-06-24	moncnt starts with 0, new CMP scheduler
//	2008-06-25	WP: bug fix in cache controller
//  2008-07-03	WP: Fixed null pointer handling of invokexxx instructions
//	2008-07-13	MS: mapping of Native.put/getfield to jopsys version
//	2008-08-21	MS: Corrected data out enable in SRAM/Flash interface
//	2008-12-10	MS: static field access uses index as address
//
//		idiv, irem	WRONG when one operand is 0x80000000
//			but is now in JVM.java

//
//	'special' constant for a version number
//	gets written in RAM at position 64
//	update it when changing .asm, .inc or .vhdl files
//
version		= 20081210

//
//	start of stack area in the on-chip RAM
//
stack_init	= 64

//
//	io address are negativ memory addresses
//
//	CNT=-128
//	IO_INT_ENA=-128
//	UART status=-112
//	UART=-111
//	USB status=-96
//	USB date=-95
//
io_cnt		=	-128
io_wd		=	-125
io_int_ena	=	-128
io_status	=	-112
io_uart		=	-111

io_lock = -123
io_cpu_id = -122
io_signal = -121

usb_status	=	-96
usb_data	=	-95

ua_rdrf		= 	2
ua_tdre		= 	1

#ifdef FPU_ATTACHED
// assuming we've attached the FPU as IO_FPU=IO_BASE+0x70=-16
fpu_const_a   = -16
fpu_const_b   = -15
fpu_const_op  = -14
fpu_const_res = -13
#endif


//
//	first vars for start
//	keep order! these vars are accessed from Java progs.
//
	mp		?		// pointer to method struct
	cp		?		// pointer to constants
	heap	?		// start of heap

	jjp		?		// pointer to meth. table of Java JVM functions
	jjhp	?		// pointer to meth. table of Java JVM help functions

	moncnt	?		// counter for monitor

//
//	local vars
//
a			?
b			?
c			?
d			?
e			?
f			?

addr		?			// address used for bc load from flash
						// only in jvmflash.asm

//
//	JVM starts here.
//
//	new fetch does NOT reset address of ROM =>
//		it starts with pc+1
			nop			// this gets never executed
			nop			// for shure during reset (perhaps two times executed)

			ldi	stack_init
			nop			// written in adr/read stage!
			stsp		// someting strange in stack.vhd A->B !!!


// TEST read after write

// ldi 1
// ldi 2
// ldi 3
// add
// add
// pop

//////////
// test mem interface
//
//			ldi 15
//
//			// this sequence takes 6 cycles with ram_cnt=3
//			stmra				// start read ext. mem
//			wait				// one for fetch
//			wait				// one for decode
//			ldmrd		 		// read ext. mem
//
//			ldi	32				// write data
//			ldi	16				// write address
//
//			// this sequence takes 6 cycles with ram_cnt=3
//			stmwa				// write ext. mem address
//			stmwd				// write ext. mem data
//			wait
//			wait
//
//			ldi 7
//			stmra				// start read ext. mem
//			wait				// one for fetch
//			wait				// one for decode
//			ldmrd		 		// read ext. mem
//
//			pop
//			pop
/////////
// test iaload
//			ldi	1
//			ldi 5
//			stald
//			pop
//			wait
//			wait
//			ldmrd
//			pop
//
//			nop
//			nop
//			nop
// test iastore
//			ldi 1
//			ldi 5
//			ldi 3
//			stast
//			pop
//			pop
//			wait
//			wait
/////////


// Checks whether the cpu_id != 0, waits and jumps then further down to the invoke of the boot!!!

			ldi io_cpu_id
			stmra
			wait
			wait
			ldmrd
			nop
			bz cpu0_load
			nop
			nop
			
cpux_loop:
			ldi io_signal
			stmra
			wait
			wait
			ldmrd
			nop
			bz cpux_loop
			nop
			nop
			ldi io_signal
			stmra
			wait
			wait
			ldmrd
			nop
			bnz cpux_boot
			nop
			nop
			
			
cpu0_load:
#ifdef SIMULATION
//
//	Main memory (ram) is loaded by the simulation.
//	Just set the heap pointer from the size field.
//
			ldi	0
			stmra
			wait
			wait
			ldmrd
			stm	heap
#else
//
//
//	download n words in extern ram (high byte first!)
//
			ldi	0
			stm	heap		// word counter (ram address)

#ifdef FLASH
//
//	start address of Java program in flash: 0x80000
//
			ldi	524288		// only for jvmflash usefull
			stm	addr
#endif

//
//	Variable a will be the length set by the first word.
//	Variable c is used to assemble the word from the
//	serial transmitted bytes.
//

xram_loop:
			ldi	4			// byte counter
ser4:
#ifdef FLASH
// ************** change for load from flash *********************
			ldm addr
			stmra				// read ext. mem, mem_bsy comes one cycle later
			ldm	addr
			ldi	1
			add
			stm	addr
			wait
			wait
			ldmrd		 		// read ext. mem
// ************** end change for load from flash *********************
#else
#ifdef USB
// ************** change for load from USB interface *********************
			ldi	usb_status		// wait for byte from USB
			stmra
			ldi	ua_rdrf
			wait
			wait
			ldmrd
			and
			nop
			bz	ser4
			nop
			nop

			ldi	usb_data		// read byte from USB
			stmra
			wait
			wait
			ldmrd

//	We don't do the byte handshake on the USB connection.
//	The FTDI chip handles flow control.
//
//wait_usb_tx:
//			ldi	usb_status		// wait for TX-buffer ready
//			stmra
//			ldi	ua_tdre
//			wait
//			wait
//			ldmrd
//			and
//			nop
//			bz	wait_usb_tx
//			nop
//			nop
//
//			ldi	usb_data		// write byte to USB
//			stmwa
//			dup					// echo for down.c, 'handshake'
//			stmwd		
//			wait
//			wait

// ************** end change for load from USB interface *********************
#else
// ************** change for load from serial line *********************
			ldi	io_status		// wait for byte from uart
			stmra
			ldi	ua_rdrf
			wait
			wait
			ldmrd
			and
			nop
			bz	ser4
			nop
			nop

			ldi	io_uart			// read byte from uart
			stmra
			wait
			wait
			ldmrd

			ldi	io_uart			// write byte to uart
			stmwa
			dup					// echo for down.c, 'handshake'
			stmwd		
			wait
			wait
// ************** end change for load from serial line *********************
#endif
#endif

			ldm	c			// mem word
			ldi	8
			shl

			or				// set low byte with uart data
			stm c			// store 

			ldi	1			// decrement byte counter
			sub
			dup
			nop
			bnz	ser4
			nop
			nop
			pop				// remove byte counter

			ldm	heap
			stmwa				// write ext. mem address
			ldm	c
			stmwd				// write ext. mem data
			wait
			wait

//****
// could be changed to load mp from ram and not from the first word!!!
//	cleaner
//****
			ldm	heap
			nop
			bnz	cnt_not_0
			nop
			nop
			ldm	c				// first data word is the size of the application
			stm a

cnt_not_0:
			ldm	heap		// mem counter
			ldi	1			// increment
			add
			stm heap

not_first:
			ldm	heap
			ldm	a
			xor
			nop
			bnz	xram_loop
			nop
			nop

#endif // SIMULATION


// jump to here with cpu_id other than 0

cpux_boot:
//
//	Load mp from the second word in ram.
//
			ldi	1
			stmra
			wait
			wait
			ldmrd
			stm	mp
//
//	ram is now loaded, heap points to free ram
//	load pointer to main struct and invoke
//
			ldm	mp			// pointer to 'special' pointer list
			ldi	1
			add
			dup

			stmra				// read jjp
			wait
			wait
			ldmrd			 	// read ext. mem
			stm	jjp

			ldi	1
			add
			stmra				// read jjhp
			wait
			wait
			ldmrd			 	// read ext. mem
			stm	jjhp

			ldm	mp			// pointer to pointer to main meth. struct
			ldi	1
			nop
			bnz	invoke_main	// simulate invokestatic
			nop
			nop
///////////////////////////////////////////////////////////////////////////
//
//	begin of jvm code
//
///////////////////////////////////////////////////////////////////////////

nop:		nop nxt

iconst_m1:	ldi -1 nxt
aconst_null:
iconst_0:	ldi 0 nxt
iconst_1:	ldi 1 nxt
iconst_2:	ldi 2 nxt
iconst_3:	ldi 3 nxt
iconst_4:	ldi 4 nxt
iconst_5:	ldi 5 nxt

bipush:		nop opd
			ld_opd_8s nxt

sipush:		nop opd
			nop opd
			ld_opd_16s nxt

ldc:		ldm	cp opd
			ld_opd_8u
			add
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem

ldc_w:	
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem

aload:
fload:
iload:		nop opd
			ld nxt

aload_0:
fload_0:
iload_0:	ld0 nxt
aload_1:
fload_1:
iload_1:	ld1 nxt
aload_2:
fload_2:
iload_2:	ld2 nxt
aload_3:
fload_3:
iload_3:	ld3 nxt


astore:
fstore:
istore:		nop opd
			st nxt

astore_0:
fstore_0:
istore_0:	st0 nxt
astore_1:
fstore_1:
istore_1:	st1 nxt
astore_2:
fstore_2:
istore_2:	st2 nxt
astore_3:
fstore_3:
istore_3:	st3 nxt


pop:		pop nxt
pop2:		pop
			pop	nxt
dup:		dup nxt
dup_x1:		stm	a
			stm	b
			ldm a
			ldm b
			ldm a nxt
dup_x2:		stm	a
			stm	b
			stm	c
			ldm a
			ldm c
			ldm b
			ldm a nxt
dup2:		stm	a
			stm	b
			ldm b
			ldm a
			ldm b
			ldm a nxt
dup2_x1:	stm	a
			stm	b
			stm	c
			ldm b
			ldm a
			ldm c
			ldm b
			ldm a nxt
dup2_x2:	stm	a
			stm	b
			stm	c
			stm	d
			ldm b
			ldm a
			ldm d
			ldm c
			ldm b
			ldm a nxt
swap:		stm	a		// not tested, javac does not generate it!
			stm	b
			ldm	a
			ldm	b nxt

iadd:		add nxt
isub:		sub nxt

ineg:
			ldi -1
			xor
			ldi 1
			add nxt

iand:		and nxt
ior:		or nxt
ixor:		xor nxt

ishl:		shl nxt
ishr:		shr nxt
iushr:		ushr nxt


imul:
			stmul		// store both operands and start
			pop			// pop second operand

			ldi	2		// 2*7+2 wait ok!
imul_loop:
			ldi	-1
			add
			dup
			nop
			bnz	imul_loop
			nop
			nop
	
			pop			// remove counter

			ldmul	nxt


// 	moved to JVM.java
// 
// idiv:
// 			stm	b
// 			stm	a
// 			ldm	a
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			dup					// make a positiv
// 			nop
// 			bz	idiv_apos
// 			nop
// 			nop
// 			ldm	a
// 			ldi -1
// 			xor
// 			ldi 1
// 			add
// 			stm	a
// idiv_apos:
// 			ldm	b
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			dup					// make b positiv
// 			nop
// 			bz	idiv_bpos
// 			nop
// 			nop
// 			ldm	b
// 			ldi -1
// 			xor
// 			ldi 1
// 			add
// 			stm	b
// idiv_bpos:
// 			xor					//	sign
// 			stm	e
// 
// 			ldi	0
// 			stm	c			//	c is quotient
// 			ldi	0
// 			stm	d			//	d is remainder
// 			ldi	32			//	loop counter
// idiv_loop:
// 			ldm	c
// 			dup
// 			add
// 			stm	c
// 			ldm	d
// 			dup
// 			add
// 			stm	d
// 			ldm	a
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			nop
// 			bz	idiv_noor
// 			nop
// 			nop
// 			ldm	d
// 			ldi	1
// 			or
// 			stm	d
// idiv_noor:
// 			ldm	a
// 			dup
// 			add
// 			stm	a
// 			ldm	d
// 			ldm	b
// 			sub
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			nop
// 			bnz	idiv_nosub
// 			nop
// 			nop
// 			ldm	d
// 			ldm	b
// 			sub
// 			stm	d
// 			ldm	c
// 			ldi	1
// 			or
// 			stm	c
// idiv_nosub:
// 
// 			ldi	1
// 			sub
// 			dup
// 			nop
// 			bnz	idiv_loop
// 			nop
// 			nop
// 			pop				// remove loop counter
// 			ldm	e
// 			nop
// 			bz	idiv_nosign
// 			nop
// 			nop
// 			ldm	c
// 			ldi -1
// 			xor
// 			ldi 1
// 			add	nxt
// idiv_nosign:
// 			ldm	c	nxt
// 
// irem:
// 			stm	b
// 			stm	a
// 			ldm	a
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			dup					// make a positiv
// 			stm	e				//	sign
// 			nop
// 			bz	irem_apos
// 			nop
// 			nop
// 			ldm	a
// 			ldi -1
// 			xor
// 			ldi 1
// 			add
// 			stm	a
// irem_apos:
// 			ldm	b
//         	ldi	-2147483648		//  0x80000000
// 			and					// make b positiv
// 			nop
// 			bz	irem_bpos
// 			nop
// 			nop
// 			ldm	b
// 			ldi -1
// 			xor
// 			ldi 1
// 			add
// 			stm	b
// irem_bpos:
// 
// 			ldi	0
// 			stm	c			//	c is quotient
// 			ldi	0
// 			stm	d			//	d is remainder
// 			ldi	32			//	loop counter
// irem_loop:
// 			ldm	c
// 			dup
// 			add
// 			stm	c
// 			ldm	d
// 			dup
// 			add
// 			stm	d
// 			ldm	a
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			nop
// 			bz	irem_noor
// 			nop
// 			nop
// 			ldm	d
// 			ldi	1
// 			or
// 			stm	d
// irem_noor:
// 			ldm	a
// 			dup
// 			add
// 			stm	a
// 			ldm	d
// 			ldm	b
// 			sub
//         	ldi	-2147483648		//  0x80000000
// 			and
// 			nop
// 			bnz	irem_nosub
// 			nop
// 			nop
// 			ldm	d
// 			ldm	b
// 			sub
// 			stm	d
// 			ldm	c
// 			ldi	1
// 			or
// 			stm	c
// irem_nosub:
// 
// 			ldi	1
// 			sub
// 			dup
// 			nop
// 			bnz	irem_loop
// 			nop
// 			nop
// 			pop				// remove loop counter
// 			ldm	e
// 			nop
// 			bz	irem_nosign
// 			nop
// 			nop
// 			ldm	d
// 			ldi -1
// 			xor
// 			ldi 1
// 			add	nxt
// irem_nosign:
// 			ldm	d	nxt
// 


// Floating point operations in HW with FPU
#ifdef FPU_ATTACHED
fadd:
		ldi fpu_const_b   // load address: FPU_B
		stmwa             // store memory address
		stmwd             // store memory data - b already on stack
		wait
		wait              // execute 1+nws
		ldi fpu_const_a   // load address: FPU_A
		stmwa             // store memory data
		stmwd             // store memory data - a already on stack
		wait
		wait              // execute 1+nws
		ldi 0             // load FPU_OP_ADD (data)
		ldi fpu_const_op  // load FPU_OP (address)
		stmwa             // store FPU_OP
		stmwd             // store FPU_OP_ADD
		wait
		wait              // execute 1+nws
		ldi fpu_const_res // load address of FPU_RES
		stmra             // read memory
		wait
		wait              // execute 1+nws
		ldmrd nxt         // read ext. mem

fsub:
		ldi fpu_const_b   // load address: FPU_B
		stmwa             // store memory address
		stmwd             // store memory data - b already on stack
		wait
		wait              // execute 1+nws
		ldi fpu_const_a   // load address: FPU_A
		stmwa             // store memory data
		stmwd             // store memory data - a already on stack
		wait
		wait              // execute 1+nws
		ldi 1             // load FPU_OP_ADD (data)
		ldi fpu_const_op  // load FPU_OP (address)
		stmwa             // store FPU_OP
		stmwd             // store FPU_OP_ADD
		wait
		wait              // execute 1+nws
		ldi fpu_const_res // load address of FPU_RES
		stmra             // read memory
		wait
		wait              // execute 1+nws
		ldmrd nxt         // read ext. mem

fmul:
		ldi fpu_const_b   // load address: FPU_B
		stmwa             // store memory address
		stmwd             // store memory data - b already on stack
		wait
		wait              // execute 1+nws
		ldi fpu_const_a   // load address: FPU_A
		stmwa             // store memory data
		stmwd             // store memory data - a already on stack
		wait
		wait              // execute 1+nws
		ldi 2             // load FPU_OP_ADD (data)
		ldi fpu_const_op  // load FPU_OP (address)
		stmwa             // store FPU_OP
		stmwd             // store FPU_OP_ADD
		wait
		wait              // execute 1+nws
		ldi fpu_const_res // load address of FPU_RES
		stmra             // read memory
		wait
		wait              // execute 1+nws
		ldmrd nxt         // read ext. mem

fdiv:
		ldi fpu_const_b   // load address: FPU_B
		stmwa             // store memory address
		stmwd             // store memory data - b already on stack
		wait
		wait              // execute 1+nws
		ldi fpu_const_a   // load address: FPU_A
		stmwa             // store memory data
		stmwd             // store memory data - a already on stack
		wait
		wait              // execute 1+nws
		ldi 3             // load FPU_OP_ADD (data)
		ldi fpu_const_op  // load FPU_OP (address)
		stmwa             // store FPU_OP
		stmwd             // store FPU_OP_ADD
		wait
		wait              // execute 1+nws
		ldi fpu_const_res // load address of FPU_RES
		stmra             // read memory
		wait
		wait              // execute 1+nws
		ldmrd nxt         // read ext. mem
#endif



iinc:
			ldvp opd
			ld_opd_8u
			add
			star opd
			ld_opd_8s
			ldmi
			add
			stmi nxt

i2c:
			ldi	65535
			and	nxt

ifnull:
ifnonnull:
ifeq:
ifne:
iflt:
ifge:
ifgt:
ifle:
			nop opd
			jbr opd
			pop
			nop nxt

if_acmpeq:
if_acmpne:
if_icmpeq:
if_icmpne:
if_icmplt:
if_icmpge:
if_icmpgt:
if_icmple:
			nop opd
			jbr opd
			pop
			pop nxt

goto:
			nop opd
			jbr opd
			nop
			nop nxt


			// new 'customized' getfield
			// index is now the address
getstatic_ref:
getstatic:
			nop opd
			nop	opd
			ld_opd_16u

			stmra
			wait
			wait
			ldmrd		 nxt

			// new 'customized' getfield
			// index is now the address
putstatic:
			nop opd
			nop	opd
			ld_opd_16u

			stmwa				// write ext. mem address
//			nop				// ??? tos is val
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

			// new 'customized' getfield instructions
			// generated by JOPizer
			// at the moment it's just a copy of the original
getfield_ref:
getfield:
			nop	opd			// push index
			nop	opd
			ld_opd_16u
jopsys_getfield:				// version from Native
			stgf				// let the HW do the work
			pop
			wait
			wait
			ldmrd nxt			// read result

putfield:
			stm	a opd			// push index
			nop	opd
			ld_opd_16u
			ldm	a
jopsys_putfield:				// Version from Native
			stpf				// let the HW do the work
			pop
			wait
			wait
			pop nxt

newarray:
			nop opd
			ld_opd_8u
			stm a
			ldjpc
			ldi	2
			sub
			stjpc				// get last byte code
			nop					// ???
			nop					// one more now (2004-04-06) ?
			ldm	a
			ldm	jjp
			nop	opd
			ld_opd_8u
			ldi	255 
			and opd				// remove type info
			dup
			add					// *2
			add					// jjp+2*bc

// invoke JVM.fxxx();
			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop


arraylength:

			ldi	1
			add					// arrayref+1 (in handle)
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem


//aastore: is now in JVM.java for the write barrier
bastore:
castore:
fastore:
iastore:
sastore:
// new HW version :-)))
			stast
			pop
			pop
			wait
			wait
			nop nxt

//*******************************
// test for oohw change
//			ldi	6			// 7*5+2+1=38
//dly7:
//			dup
//			nop
//			bnz	dly7
//			ldi	-1			// decrement in branch slot
//			add
//			pop				// remove counter
//			nop
//*******************************

// original SW version
//			stm	a				// value
//			stm	b				// index
//			// arrayref is TOS
//			dup					// for null pointer check
//			dup					// for bound check, one cycle wait for bz
//			bz	null_pointer	// 
//			// we do the following in the
//			// branch slot -> one more element
//			// from the former dup on the stack
//			ldi	1
//			add					// arrayref+1
//			stmra				// read ext. mem, mem_bsy comes one cycle later
//			wait				// is this ok? - wait in branch slot
//			wait
//			ldmrd		 		// read ext. mem (array length)
//
//			ldi	1
//			sub					// length-1
//			ldm	b				// index
//			sub					// TOS = length-1-index
//			ldm	b				// check if index is negativ
//			or					// is one of both checks negativ?
//         	ldi	-2147483648		//  0x80000000
//			and
//			nop
//			bnz	array_bound
//			nop
//			nop
//
//// we could save one or two cycles when
//// starting the read in the branch slot
//			stmra				// read handle indirection
//			wait				// for the GC
//			wait
//			ldmrd
//			ldm	b
//			add					// index+arrayref
//
//			stmwa				// write ext. mem address
//			ldm	a
//			stmwd				// write ext. mem data
//			wait
//			wait
//			nop	nxt

aaload:
baload:
caload:
faload:
iaload:
saload:
// new HW version :-)))
			stald
			pop
			wait
			wait
			ldmrd nxt

//*******************************
// test for oohw change
//			ldi	5			// 6*5+2+3=35
//dly6:
//			dup
//			nop
//			bnz	dly6
//			ldi	-1			// decrement in branch slot
//			add
//			pop				// remove counter
//			nop
//			nop
//			nop
//*******************************

// original SW version
//
//	ideas for enhancements:
//		array pointer points to length and not the first element
//		load and checks in memory interface
//
//			stm	b				// index
//			// arrayref is TOS
//			dup					// for null pointer check
//			dup					// for bound check, one cycle wait for bz
//			bz	null_pointer	// 
//			// we do the following in the
//			// branch slot -> one more element
//			// from the former dup on the stack
//			ldi	1
//			add					// arrayref+1
//
//			stmra				// read array length
//			wait				// is this ok? - wait in branch slot
//			wait
//			ldmrd		 		// read ext. mem (array length)
//
//			ldi	1
//			add					// length+1
//			ldm	b				// index
//			sub					// TOS = length-1-index
//			ldm	b				// check if index is negativ
//			or					// is one of both checks neagtv?
//         	ldi	-2147483648		//  0x80000000
//			and
//			nop
//			bnz	array_bound
//			nop
//			nop
//
//// we could save one ot two cycles when
//// starting the read in the branch slot
//			stmra				// read handle indirection
//			wait				// for the GC
//			wait
//			ldmrd
//			ldm	b
//			add					// index+arrayref
//
//			stmra				// read ext. mem, mem_bsy comes one cycle later
//			wait
//			wait
//			ldmrd		 nxt	// read ext. mem


monitorenter:
 			pop					// drop reference
//			bz null_pointer		// null pointer check
			ldi	io_int_ena
			stmwa				// write ext. mem address
			ldi	0
			stmwd				// write ext. mem data
			ldm	moncnt
			ldi	1
			add
			wait
			wait
			stm	moncnt
			// request the global lock
			ldi	io_lock
			stmwa				// write ext. mem address
			ldi	1
			stmwd				// write ext. mem data
			wait
			wait
			nop nxt

monitorexit:
			pop					// drop reference
//			bz null_pointer		// null pointer check
			ldm	moncnt
			ldi	1
			sub
			dup
			stm	moncnt
			bnz	mon_no_ena
			// can be exec in in branch delay?
			// up to now yes, but we change the write
			// some time....
			// nop
			// nop
			// free the global lock
			ldi	io_lock
			stmwa				// write ext. mem address
			ldi	0
			stmwd				// write ext. mem data
			wait
			wait
			ldi	io_int_ena
			stmwa
			ldi	1
			stmwd				// write ext. mem data
			wait
			wait
mon_no_ena:	nop		nxt

//
//	invoke and return functions
//
#include "jvm_call.inc"

//
//	null pointer
//		call JVMHelp.nullPoint();
//

null_pointer:
			wait				// just for shure if we jump during
			wait				// a memory transaction to this point
			ldm	jjhp			// interrupt() is at offset 0
								// jjhp points in method table to first
								// method after methods inherited from Object
			ldi	2				// second method (index 1 * 2 word);
			add

			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop


//
//	array bound exception
//		call JVMHelp.arrayBound();
//

array_bound:
			wait				// just for shure if we jump during
			wait				// a memory transaction to this point
			ldm	jjhp			// interrupt() is at offset 0
								// jjhp points in method table to first
								// method after methods inherited from Object
			ldi	4				// third method (index 2 * 2 word);
			add

			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop

//		
// long bytecodes
//
#include "jvm_long.inc"

//
//	this is an interrupt, (bytecode 0xf0)
//	call com.jopdesign.sys.JVMHelp.interrupt()	(
//		oder gleich eine f aus JVMHelp ????
//		... JVM in Java!
//
sys_int:
			ldjpc				// correct wrong increment on jpc
			ldi	1				//    could also be done in bcfetch.vhd
			sub					//    but this is simpler :-)
			stjpc
			ldm	jjhp			// interrupt() is at offset 0
								// jjhp points in method table to first
								// method after methods inherited from Object

			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop


//
//	this is an exception, (bytecode 0xf1)
//	call com.jopdesign.sys.JVMHelp.except()	(
//
sys_exc:
			ldjpc				// correct wrong increment on jpc
			ldi	1				//    could also be done in bcfetch.vhd
			sub					//    but this is simpler :-)
			stjpc
			ldm	jjhp			// interrupt() is at offset 0
								// jjhp points in method table to first
			ldi	6				// forth method (index 3 * 2 word);
			add


			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop


//
//	call com.jopdesign.sys.JMV.fxxx() for not implemented  byte codes.
//		... JVM in Java!
//
sys_noim:
			ldjpc
			ldi	1
			sub
			stjpc				// get last byte code
			nop					// ???
			nop					// one more now (2004-04-06) ?
			ldm	jjp
			nop	opd
			ld_opd_8u
			ldi	255
			and
			dup
			add					// *2
			add					// jjp+2*bc

			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop

//
//	call com.jopdesign.sys.JMV.fxxx(int constant) for not implemented  byte codes.
//		... JVM in Java!
//		with constant on stack
//

new:
anewarray:
checkcast:
instanceof:

//
//	find address for JVM function
//
			ldjpc
			ldi	1
			sub
			stjpc				// get last byte code
			nop					// ???
			nop					// one more now (2004-04-06) ?
			ldm	jjp
			nop	opd
			ld_opd_8u
			ldi	255
			and
			dup
			add					// *2
			add					// jjp+2*bc
			stm	a				// save

//
//	get constant
//
			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 		// read ext. mem

			ldm	a				// restore mp

//
//	invoke JVM.fxxx(int cons)
//
			ldi	1
			nop
			bnz	invoke
			nop
			nop

//
//	call com.jopdesign.sys.JMV.fxxx(int index) for not implemented  byte codes.
//		... JVM in Java!
//		with index into constant pool on stack
//

putfield_ref:
putstatic_ref:

//
//	find address for JVM function
//
			ldjpc
			ldi	1
			sub
			stjpc				// get last byte code
			nop					// ???
			nop					// one more now (2004-04-06) ?
			ldm	jjp
			nop	opd
			ld_opd_8u
			ldi	255
			and
			dup
			add					// *2
			add					// jjp+2*bc
			stm	a				// save

//
//	get index
//
			nop	opd
			nop	opd
			ld_opd_16u

			ldm	a				// restore mp

//
//	invoke JVM.fxxx(int index)
//
			ldi	1
			nop
			bnz	invoke
			nop
			nop

//****************

// special byte codes for native functions

//jopsys_rd:
//			stioa		// io-address
//			nop
//			ldiod	nxt	// read data
//
//jopsys_wr:
//			stioa		// io-address
//			nop
//			stiod	nxt	// write data

//
//	this sequence takes ram_cnt + 3 cycles
//	means ram_cnt-1 wait states (bsy)
//	nws = ram_cnt-1
//
//	or in other words 4+nws
//
//	For the 100MHz JOP version this sequnce takes
//	5 cycles.
//
//
jopsys_rd:
jopsys_rdmem:
			stmra				// read memory, mem_bsy comes one cycle later
			wait
			wait				// execute 1+nws
			ldmrd		 nxt	// read ext. mem


//
//	The wait states for the write are the same as
//	for the read: nws = ram_cnt-1
//
//	The sequence executes for 5+nws cycles - for the
//	100MHz version in 6 cycles
//
jopsys_wr:
jopsys_wrmem:
			stmwa				// store memory address
			stmwd				// store memory data
			wait
			wait				// execute 1+nws
			nop	nxt

jopsys_rdint:
			star			// address in ar
			nop				// due to pipelining
			ldmi nxt		// read value (ar indirect)

jopsys_wrint:
			star			// address in ar
			nop				// due to pipelining
			stmi nxt		// write value (ar indirect)

jopsys_getsp:
			ldsp		// one increment but still one to low ('real' sp is sp+2 because of registers)
			ldi	1		// 'real' sp			da sp auf rd adr zeigt
			add	nxt
jopsys_setsp:
			nop			// written in adr/read stage!
			stsp		// new sp
			pop			// flash tos, tos-1 (registers)
			pop	nxt		// sp must be two lower, points to rd adr
jopsys_getvp:
			ldvp	nxt
jopsys_setvp:
			stvp
			nop	nxt

// public static native void int2extMem(int intAdr, int extAdr, int cnt);

jopsys_int2ext:
			ldi	-1
			add
			stm c			// counter-1
			stmra			// read handle indirection
			stm	b			// intern address
			wait			// for the GC
			wait
			ldmrd
			stm	a			// extern address
			ldm	c			// keep counter on the stack

intext_loop:
			dup
			ldm	b
			add
			star
			dup
			ldm	a
			add
			stmwa
			ldmi
			stmwd
			dup
			wait
			wait

			bnz		intext_loop
			ldi	-1	// decrement in branch slot
			add

			pop	nxt	// remove counter

// public static native void ext2intMem(int extAdr, int intAdr, int cnt);

jopsys_ext2int:
			ldi	-1
			add
			stm c			// counter-1
			stm	b			// intern address
			stmra			// read handle indirection
			wait			// for the GC
			wait
			ldmrd
			stm	a			// extern address
			ldm	c			// keep counter on the stack

extint_loop:
			dup
			ldm	a
			add
			stmra
			dup
			ldm	b
			add
			star
			wait
			wait
			ldmrd			// read ext val
			stmi

			dup
			nop
			bnz		extint_loop
			ldi	-1	// decrement in branch slot
			add

			pop	nxt	// remove counter

//	public static native void memCopy(int src, int dest, int cnt);

jopsys_memcpy:
// 			ldi	-1
// 			add
// 			stm c	// counter-1
// 			stm b	// destination
// 			stm a	// source
// 			ldm	c	// keep counter on the stack

// memcpy_loop:
// 			dup
// 			ldm	a
// 			add
// 			stmra
// 			dup
// 			ldm	b
// 			add
// 			stmwa	// should be ok
// 			wait
// 			wait
// 			ldmrd
// 			stmwd
// 			dup
// 			wait
// 			wait

// 			bnz	memcpy_loop
// 			ldi	-1	// decrement in branch slot
// 			add

			stcp
			pop
			wait
			wait
			pop nxt



//
//	some conversions only need a nop!
//
jopsys_nop:
			nop	nxt

//jopsys_invoke: see invoke


//jopsys_cond_move:
//			nop		// one cycle for the condition
//			bz		false_path
//			stm		b
//			stm		c
//			ldm		c nxt
//false_path:	ldm		b nxt
