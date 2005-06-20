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
//
//		idiv, irem	WRONG when one operand is 0x80000000
//			but is now in JVM.java

#define HANDLE 1
//
//	'special' constant for a version number
//	gets written in RAM at position 64
//	update it when changing .asm, .inc or .vhdl files
//
version		= 20050620

//
//	io register
//
io_addr		=	0
io_data		=	1

//
//	io address
//
io_cnt		=	0
io_int_ena	=	0
io_status	=	4
io_uart		=	5


ua_rdrf		= 	2
ua_tdre		= 	1

max_words	= 16384	// words loaded from serial line to ram
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

			ldi	127
			nop			// written in adr/read stage!
			stsp		// someting strange in stack.vhd A->B !!!


// TEST read after write

// ldi 1
// ldi 2
// ldi 3
// add
// add
// pop

////////////
// test mem interface
//
//			ldi 15
//
//			// this sequence takes 6 cycles with ram_cnt=3
//			stmra				// start read ext. mem
// not any more necessary
//			// nop					// mem_bsy comes one cycle later
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
// not any more necessary
//			wait
//			wait
//
//			ldi 7
//			stmra				// start read ext. mem
// not any more necessary
//			wait				// one for fetch
//			wait				// one for decode
//			ldmrd		 		// read ext. mem
//
//			pop
//			pop
///////////


////////////////
//ser_invoke2:
//ldi	io_status	// wait for uart ready
//stioa
//ldi	ua_tdre
//ldiod
//and
//nop
//bz	ser_invoke2
//nop
//nop
//ldi	io_uart
//stioa
//ldi	65
//stiod
//ldi	1
//nop
//bnz ser_invoke2
//nop
//nop
////////////////
//			ldi	1
//			nop
//			bnz	start_jvm	// use this without download
//			nop
//			nop

			ldi	1			// disable int's
			stm	moncnt		// monitor counter gets zeroed in startMission

#ifdef SIMULATION
//
//	Main memory (ram) is loaded by the simulation.
//	Just set the heap pointer and load mp from ram
//		This could be done in all versions!
//
			ldi	max_words
			stm	heap
			ldi	0
			stmra
			wait
			wait
			ldmrd
			stm	mp
#else
//
//
//	download n words in extern ram (high byte first!)
//
			ldi	0
			stm	heap		// word counter (ram address)

//
//	start address of Java program in flash: 0x80000
//
			ldi	524288		// only for jvmflash usefull
			stm	addr

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
// ************** change for load from serial line *********************
			ldi	io_status	// wait for byte from uart
			stioa
			ldi	ua_rdrf
			ldiod
			and
			nop
			bz	ser4
			nop
			nop

			ldi	io_uart		// read byte from uart
			stioa
			nop
			ldiod

			dup				// echo for down.c, 'handshake'
			stiod

// ************** end change for load from serial line *********************
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
			ldm	c				// first 'real' data is pointer to main struct
			stm	mp

cnt_not_0:
			ldm	heap		// mem counter
			ldi	1			// increment
			add
			stm heap

not_first:
			ldm	heap
			ldi	max_words
			xor
			nop
			bnz	xram_loop
			nop
			nop

#endif // SIMULATION
//
//	ram is now loaded, heap points to free ram
//	load pointer to main struct and invoke
//
			ldi	1
			nop
			bnz	start_jvm	// two jumps for long distance
			nop
			nop

////////////////
//ser_invoke2:
//ldi	io_status	// wait for uart ready
//stioa
//ldi	ua_tdre
//ldiod
//and
//nop
//bz	ser_invoke2
//nop
//nop
//ldi	io_uart
//stioa
//ldjpc 
//stiod
////////////////

dbg0	?
dbg1	?
dbg2	?
dbg3	?
dbg4	?
dbg5	?

//stop:
// print dbg0..n
//ldi	6
//stm a
//ldi	dbg0
//stvp
//ser_w_0:
//ldi	io_status	// wait for uart ready
//stioa
//ldi	ua_tdre
//ldiod
//and
//nop
//bz	ser_w_0
//nop
//nop
//ldi	io_uart
//stioa
//ld0
//stiod
//ldvp
//ldi	1
//add
//stvp
//ldm	a
//ldi 1
//sub
//stm a
//ldm a
//nop
//bnz ser_w_0
//nop
//nop
//
//ldi 1
//nop
//endless:	bnz endless
//			ldi	1
//			nop

//
//	begin of jvm code
//
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
dup2:		stm	a
			stm	b
			ldm b
			ldm a
			ldm b
			ldm a nxt
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
			stmul			// store both operands and start
			pop				// pop second operand

			ldi	5			// 6*5+3 wait ok!!
imul_loop:					// plus 1 for the ldi 5... to many cycles?
			dup
			nop
			bnz	imul_loop
			ldi	-1			// decrement in branch slot
			add

			pop				// remove counter

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

///////////////////////////////////////////////////////////////////////////
//
//	part of init, split of jump!
//	TODO: should get simplified
//
start_jvm:
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



iinc:
			ldvp
			dup opd
			ld_opd_8u
			add
			stvp opd
			ld_opd_8s
			ld0
			add
			st0
			stvp
			nop nxt

i2c:		ldi	65535
			and	nxt

ifnull:
ifnonnull:
ifeq:
ifne:
iflt:
ifge:
ifgt:
ifle:
			nop	opd
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
			nop	opd
			jbr opd
			pop
			pop nxt

goto:		nop opd
			jbr opd
			nop
			nop nxt



getstatic:
				// int idx = readOpd16u();
				// int addr = readMem(cp+idx);	// not now
				// stack[++sp] = readMem(addr);

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 	// read ext. mem

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem



putstatic:
				// int idx = readOpd16u();
				// int addr = readMem(cp+idx);	// not now
				// writeMem(addr, stack[sp--]);

			ldm	cp opd
			nop	opd
			ld_opd_16u
			add

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 	// read ext. mem

			stmwa				// write ext. mem address
//			nop					// ??? tos is val
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

getfield:
				// int idx = readOpd16u();
				// int off = readMem(cp+idx);
				// stack[sp] = readMem(stack[sp]+off);

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

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem



				// int idx = readOpd16u();
				// int addr = readMem(cp+idx);	// not now
				// writeMem(addr, stack[sp--]);
putfield:
				// int idx = readOpd16u();
				// int off = readMem(cp+idx);
				// int val = stack[sp--];
				// writeMem(stack[sp--]+off, val);

			stm	a				// save value

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

			stmwa				// write ext. mem address
			ldm	a				// restore value
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

// TODO: initialize to zero
// or move to JVM.java (with synchronized())
//
//newarray:	nop	opd				// no type info
//			stm	a				// save count
//			ldm	heap 
//			stmwa				// write ext. mem address
//			ldm	a
//			stmwd				// store count
//			ldm	heap
//			ldi	1
//			add					// arrayref to first element
//			dup
//			ldm	a
//			add					// +count
//			stm	heap
//			wait
//			wait
//			nop	nxt


newarray:
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
#ifdef HANDLE
			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
#endif
			ldi	-1
			add					// arrayref-1
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem


aastore:
bastore:
castore:
fastore:
iastore:
sastore:
			stm	a				// value
			stm	b				// index
			// arrayref is TOS
#ifdef HANDLE
			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
#endif

			dup					// for null pointer check
			dup					// bound check
			ldi	-1
			add					// arrayref-1
			stmra				// read ext. mem, mem_bsy comes one cycle later
// could this nop be removed?
			nop					// wait one cycle for flags of ref
			bz	null_pointer	// 
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
			ldm	b				// in branch slot....
			add					// index+arrayref

			stmwa				// write ext. mem address
			ldm	a
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

aaload:
baload:
caload:
faload:
iaload:
saload:
//
//	ideas for enhancements:
//		array pointer points to length and not the first element
//		load and checks in memory interface
//
			stm	b				// index
			// arrayref is TOS
#ifdef HANDLE
			stmra				// read handle indirection
			wait				// for the GC
			wait
			ldmrd
#endif

			dup					// for null pointer check
			dup					// for bound check
			ldi	-1
			add					// arrayref-1

			stmra				// read array length
// could this nop be removed?
			nop					// wait one cycle for flags of ref
			bz	null_pointer	// 
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
			ldm	b				// in branch slot...
			add					// index+arrayref

			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem


monitorenter:
			pop					// we don't use the objref
			ldi	io_int_ena
			stioa
			ldi	0
			stiod
			ldm	moncnt
			ldi	1
			add
			stm	moncnt	nxt

monitorexit:
			pop					// we don't use the objref
			ldm	moncnt
			ldi	1
			sub
			dup
			stm	moncnt
			bnz	mon_no_ena
			ldi	io_int_ena		// exec in branch delay
			stioa				// exec in branch delay

			ldi	1
			stiod	nxt

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
			ldi	4				// second method (index 2 * 2 word);
			add

			ldi	1
			nop
			bnz	invoke			// simulate invokestatic with ptr to meth. str. on stack
			nop
			nop

// long bytecodes
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

//****************

// special byte codes for native functions

jopsys_rd:
			stioa		// io-address
			nop
			ldiod	nxt	// read data

jopsys_wr:
			stioa		// io-address
			nop
			stiod	nxt	// write data

jopsys_rdmem:
			stmra				// read ext. mem, mem_bsy comes one cycle later
			wait
			wait
			ldmrd		 nxt	// read ext. mem

jopsys_wrmem:
			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait
			nop	nxt

jopsys_rdint:
			ldvp
			stm	a
			stvp			// address in vp
			nop				// ???
			ld0				// read value
			ldm	a			// restore vp
			stvp
			nop nxt

jopsys_wrint:
			ldvp
			stm	a
			stvp			// address in vp
			nop				// ???
			st0				// write value
			ldm	a			// restore vp
			stvp
			nop nxt

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
			ldvp
			stm	d			// save vp
			stm	c			// save counter
			stm	a			// extern address
			stm	b			// intern address
			ldm	a
			ldm	c
			add
			ldi	1
			add
			stm	e			// extern end address+1
intext_loop:
			ldm	b
			stvp
			ldm	a
			stmwa				// write ext. mem address
			ld0
			stmwd				// write ext. mem data
			ldm	a				// could be on the stack
			ldi	1				// but I'm now to lazy to think
			add
			stm a
			ldm	b
			ldi	1
			add
			stm b
			wait				// wait for write
			wait
			ldm	a				// finished?
			ldm	e
			sub
			nop
			bnz		intext_loop
			ldm	d				// restore vp in branch
			stvp				// slots
			nop		nxt

// public static native void ext2intMem(int extAdr, int intAdr, int cnt);

jopsys_ext2int:
			ldvp
			stm	d			// save vp
			stm	c			// save counter
			stm	b			// intern address
			stm	a			// extern address
			ldm	a
			ldm	c
			add
			ldi	1
			add
			stm	e			// extern end address+1
extint_loop:
			ldm	a
			stmra			// read ext. mem, mem_bsy comes one cycle later
			ldm	a
			ldi	1
			add
			stm a
			ldm	b
			stvp
			ldm	b
			ldi	1
			add
			stm b
			wait
			wait
			ldmrd			// read ext val
			st0
			ldm	a			// finished?
			ldm	e
			sub
			nop
			bnz		extint_loop
			ldm	d			// restore vp in branch
			stvp			// slots
			nop		nxt


//
//	some conversions only need a nop!
//
jopsys_nop:
			nop	nxt

//jopsys_invoke: see invoke
















