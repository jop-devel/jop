//
//	jvm.asm
//
//		JVM for JOP3
//
//	2001-10-24	first version works with Jopa (prime is running)
//	2001-10-31	download of bc and ram from serial line (init)
//

//
//	io register
//
io_addr		=	0
io_data		=	1

//
//	io address
//
io_inp		=	0
io_outp		=	0
io_status	=	1
io_uart		=	2
io_ecp		=	3
io_cnt		=	10
io_ms		=	11

mem_rd_addr	= 4		// st
mem_rd_data	= 4		// ld
mem_wr_addr	= 5		// st
mem_status	= 5		// ld
mem_wr_data	= 6		// st
mem_cancel	= 6		// ld

ua_rdrf		= 	2
ua_tdre		= 	1

//
//	first vars for start
//
	mp		?		// pointer to method table
	cp		?		// pointer to constants
	main	?		// number of main method
	static	?		// pointer to static fields
	heap	?		// start of heap ??? not used now
	extmem	?		// flag if bc is in external memory
	memcnt	?		// words of ext. ram download
	
//
//	local vars
//
a			?
b			?
c			?


//
//	special byte codes
//		but starts with pc=0!!! (so init bc is not really necassary)
//
sys_init:

//			jp	start_jvm	// use this without download
//			nop
//			nop

//
//	download bc from serial line
//

			ldi	0
			stjpc

ser1:
			ldi	io_status	// wait for byte from uart
			stp	io_addr
			ldi	ua_rdrf
			ldp	io_data
			and
			nop
			bz	ser1
			nop
			nop

			ldi	io_uart		// read byte from uart
			stp	io_addr
			nop
			ldp	io_data

//			dup				// echo for down.c, 'handshake'
ldjpc
			stp	io_data

			stbc			// store in jbc
			ldjpc
			ldi	1			// increment jpc
			add
			stjpc
			ldjpc
			nop
			bnz	ser1		// jpc is 0 again after 512 bcs
			nop
			nop


//
//	download 128 words in local ram (high byte first!)
//
			ldi	0
			stvp
ram_loop:
			ldi	4			// byte counter
ser2:
			ldi	io_status	// wait for byte from uart
			stp	io_addr
			ldi	ua_rdrf
			ldp	io_data
			and
			nop
			bz	ser2
			nop
			nop

			ldi	io_uart		// read byte from uart
			stp	io_addr
			nop
			ldp	io_data
//			dup				// echo for down.c, 'handshake'
ldvp
			stp	io_data

			ld0				// ld (vp)
			dup				// <<8
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add

			or				// set low byte with uart data
			st0				// store (vp)

			ldi	1			// decrement byte counter
			sub
			dup
			nop
			bnz	ser2
			nop
			nop
			pop				// remove byte counter
			ldvp
			ldi	1			// increment vp
			add
			dup
			stvp
			ldi	64
			dup
			add		// 128
			xor
			nop
			bnz	ram_loop
			nop
			nop

//
//	download n words in extern ram (high byte first!)
//
			ldm	memcnt
			nop
			bz	no_xram
			nop
			nop

			ldi	0
			stm	a			// word counter (ram address)
xram_loop:
			ldi	4			// byte counter
ser4:
			ldi	io_status	// wait for byte from uart
			stp	io_addr
			ldi	ua_rdrf
			ldp	io_data
			and
			nop
			bz	ser4
			nop
			nop

			ldi	io_uart		// read byte from uart
			stp	io_addr
			nop
			ldp	io_data
//			dup				// echo for down.c, 'handshake'
ldm a
			stp	io_data

			ldm	c			// mem word
			dup				// <<8
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add
			dup	
			add

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

			ldi	mem_wr_addr		// write ext. mem
			stp	io_addr
			ldm	a
			stp	io_data
			ldi	mem_wr_data
			stp	io_addr
			ldm	c
			stp	io_data
			ldi	mem_status
			stp	io_addr
			nop
mem_wait:
			ldp	io_data
			nop
			bnz	mem_wait
			nop
			nop
			
			ldm	a			// mem counter
			ldi	1			// increment vp
			add
			dup
			stm a
			ldm	memcnt
			xor
			nop
			bnz	xram_loop
			nop
			nop

no_xram:

//
//	load cp and call main
//
start_jvm:

			ldm	cp
			stcp

			ldm	main		// index in method table
			jp	init_jmp	// simulate invokestatic
			nop
			nop

//
//	begin of jvm code
//
nop:		nop nxt

iconst_m1:	ldi -1 nxt
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

ldc:		nop opd
			ldc nxt

aload:
iload:		nop opd
			ld nxt

aload_0:
iload_0:	ld0 nxt
aload_1:
iload_1:	ld1 nxt
aload_2:
iload_2:	ld2 nxt
aload_3:
iload_3:	ld3 nxt

astore:
istore:		nop opd
			st nxt

astore_0:
istore_0:	st0 nxt
astore_1:
istore_1:	st1 nxt
astore_2:
istore_2:	st2 nxt
astore_3:
istore_3:	st3 nxt

pop:		pop nxt
dup:		dup nxt
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

ishl:
			ldi	31			// to be optimized!!!
			and	
			stm	a
			stm	b
			ldm	a
			nop
			bz	ishl_end
			nop
			nop
ishl_loop:
			ldm	b
			dup
			add
			stm	b
			ldm	a
			ldi	-1
			add
			dup
			stm	a
			nop
			bnz	ishl_loop
			nop
			nop
ishl_end:
			ldm	b nxt

iushr:
			ldi	31			// to be optimized!!!
			and	
			stm	a
			stm	b
			ldm	a
			nop
			bz	iushr_end
			nop
			nop
iushr_loop:
			ldm	b
			shr
			stm	b
			ldm	a
			ldi	-1
			add
			dup
			stm	a
			nop
			bnz	iushr_loop
			nop
			nop
iushr_end:
			ldm	b nxt

iinc:
			ldvp
			dup opd
			ld_opd_8s		// works only for idx < 128 (sign ext)!
			add
			stvp opd
			ld_opd_8s
			ld0
			add
			st0
			stvp
			nop nxt

ifeq:		nop opd
			jbr_eq opd
			nop
			nop nxt
ifne:		nop opd
			jbr_ne opd
			nop
			nop nxt
iflt:		nop opd
			jbr_lt opd
			nop
			nop nxt
ifge:		nop opd
			jbr_ge opd
			nop
			nop nxt
ifgt:		nop opd
			jbr_gt opd
			nop
			nop nxt
ifle:		nop opd
			jbr_le opd
			nop
			nop nxt

if_icmpeq:	nop opd
			jbr_icmpeq opd
			pop
			nop nxt
if_icmpne:	nop opd
			jbr_icmpne opd
			pop
			nop nxt
if_icmplt:	nop opd
			jbr_icmplt opd
			pop
			nop nxt
if_icmpge:	nop opd
			jbr_icmpge opd
			pop
			nop nxt
if_icmpgt:	nop opd
			jbr_icmpgt opd
			pop
			nop nxt
if_icmple:	nop opd
			jbr_icmple opd
			pop
			nop nxt

goto:		nop opd
			jbr opd
			nop
			nop nxt

//
//	invoke static and system' functions:
//
//		1	int rd(int adr)
//		2	void wr(int val, int adr)




//		3	int status()
//		4	int uart_rd()
//		5	void uart_wr(int val)
//		6	int ecp_rd()
//		7	void ecp_wr(int val)
//		8	int cnt()
//		9	int ms()
//		10	void out(int val)
//

invokestatic:	nop opd
			nop opd
			ldc			// ignore index > 255

			dup
			ldi	1
			xor
			nop			// one cycle wait for zf
			bnz	not_rd
			nop
			nop

sys_rd:
			pop			// remove index
			stp	0		// io-address
			nop			// TODO address is too late!!!
			ldp	1 nxt	// read data


not_rd:		dup
			ldi	2
			xor
			nop			// one cycle wait for zf
			bnz	not_wr
			nop
			nop

sys_wr:
			pop			// remove index
			stp	0		// io-address
			nop			// TODO address is too late!!!
			stp	1 nxt	// write data


not_wr:		ldi	127		// mask out bit 7	(set for static functions)
			and
init_jmp:
			dup			// index *= 4
			add
			dup
			add
			ldm	mp		// mt address
//			ldi	80		// method table
			add
			stm	a		// save methtable address

//
//	local vars for tmp storage
//
old_pc			?
old_vp			?
old_fp			?
old_sp			?
new_vp			?
new_sp			?

fp				?

//
// tos and tos-1 are allready written back to memory
//
// meth. tab:
//		start pc	(or memory address if ext. memory)
//		arg count
//		locals count
//		method length in (32 bit) words

			ldsp	// eins zu hoch => 'real' sp = add 1!!! new_vp passt aber!!!
			ldi	2	// 'real' sp			da sp auf rd adr zeigt
			add
			stm	old_sp
			ldvp
			stm	old_vp
			ldjpc
			stm old_pc

			ldm	a	// meth. tab
			stvp
			ldm old_sp
			ld1		// arg count
			sub		// wieviel +- fehlt da sp in Wirklichkeit sp-2 ist?
			stm	new_vp
			ld0
			stjpc
			ldm old_sp
			ld2		// locals count		(stimmt net wirklich da locals args beinhaltet!)
			add		// wieviel +- fehlt da sp in Wirklichkeit sp-2 ist?
			dup
			stm fp
			stsp
			nop		// ???
			ldm	new_vp
////////////////
//ser_invoke:
//			ldi	io_status	// wait for byte from uart
//			stp	io_addr
//			ldi	ua_tdre
//			ldp	io_data
//			and
//			nop
//			bz	ser_invoke
//			nop
//			nop
//ldi	io_uart
//stp	io_addr
//dup			// print new_sp
//stp io_data
//ser_invoke2:
//			ldi	io_status	// wait for byte from uart
//			stp	io_addr
//			ldi	ua_tdre
//			ldp	io_data
//			and
//			nop
//			bz	ser_invoke2
//			nop
//			nop
////////////////7
			stvp
			ldm	old_pc
			ldm	old_vp
			ldm	old_fp
			ldm	new_vp nxt	// = return sp+1

ireturn:
			stm	a	// return value
			stm	new_sp
			stm	fp
			stvp
			stjpc
			ldm	new_sp
			stsp
			nop		// ????
			pop		// flash tos, tos-1 ???
			pop
			pop		// one more because new_sp is one to high
			ldm	a nxt

return:
////////////////
//ser_ret:
//			ldi	io_status	// wait for byte from uart
//			stp	io_addr
//			ldi	ua_tdre
//			ldp	io_data
//			and
//			nop
//			bz	ser_ret
//			nop
//			nop
//ldi	io_uart
//stp	io_addr
//dup			// print new_sp
//stp io_data
//ser_ret2:
//			ldi	io_status	// wait for byte from uart
//			stp	io_addr
//			ldi	ua_tdre
//			ldp	io_data
//			and
//			nop
//			bz	ser_ret2
//			nop
//			nop
////////////////7

			stm	new_sp
			stm	fp
			stvp
			stjpc
			ldm	new_sp
			stsp
			nop		// ????
			pop		// flash tos, tos-1 ???
			pop
			pop	nxt		// one more because new_sp is one to high


getstatic:	ldvp opd	// save vp
			stm	a opd
			ldc			// ignore index > 255
			ldm	static	// address for static fields
			add
			stvp		// use ap when avaliable !!!
			nop			// for set vp ?
			ld0
			ldm	a
			stvp
			nop	nxt


putstatic:	ldvp opd	// save vp
			stm	a opd
			ldc			// ignore index > 255
			ldm	static	// address for static fields
			add
			stvp
			nop			// for set vp ?
			st0
			ldm	a
			stvp
			nop	nxt

newarray:	ldvp opd	// no type info (all as int!)
			stm	a
			ldm	heap
			stvp
			dup
			st0			// count is one before reference
			ldm	a
			stvp
			ldm	heap
			ldi	1
			add
			dup
			stm	a		// reference points to first element
			add			// no clear
			stm	heap	// quick and VERY dirty
			ldm	a nxt	// return ref (= old heap)

arraylength:
			ldvp
			stm	a
			ldi	1
			sub			// count is one befor *ref
			stvp
			nop
			ld0
			ldm	a
			stvp
			nop	nxt

iastore:	ldvp
			stm	a
			stm	b		// value
			add			// ref+index
			stvp
			ldm	b
			st0
			ldm	a
			stvp
			nop	nxt

iaload:		ldvp
			stm	a
			add			// ref+index
			stvp
			nop
			ld0			// value
			ldm	a
			stvp
			nop	nxt



/////////////////////////////
// end of usefull byte codes
/////////////////////////////

sys_noim:

//ser_noim1:
//			ldi	io_status	// wait for byte from uart
//			stp	io_addr
//			ldi	ua_tdre
//			ldp	io_data
//			and
//			nop
//			bz	ser_noim1
//			nop
//			nop
//ldi	io_uart
//stp	io_addr
//ldi 33
//stp io_data

ser_noim2:
			ldi	io_status	// wait for byte from uart
			stp	io_addr
			ldi	ua_tdre
			ldp	io_data
			and
			nop
			bz	ser_noim2
			nop
			nop
ldi	io_uart
stp	io_addr
ldjpc
stp io_data

xx: jp xx
	nop
	nop
