//
//	memser.asm
//
//		memory test, write memory content to serial line
//
//
//	io register
//
io_addr		=	0
io_data		=	1
mem_rd_addr	= 2		// st
mem_rd_data	= 2		// ld
mem_wr_addr	= 3		// st
mem_status	= 3		// ld
mem_wr_data	= 4		// st
mem_cancel	= 4		// ld

//
//	io address
//
io_cnt		=	0
io_wd		=	3
io_status	=	4
io_uart		=	5

ua_rdrf		= 	2
ua_tdre		= 	1

//
//	first vars for start
//
	mp		?		// pointer to method struct (two words in cpool)
	cp		?		// pointer to constants
	heap	?		// start of heap

	extbc	?		// flag if bc load is neccessary

//
//	local vars
//
a			?
b			?
c			?


//
//	but starts with pc=0!!! (so init bc is not really necassary)
//
			nop
			nop
			ldi	127
			nop			// written in adr/read stage!
			stsp




loop_cnt	= 32768

loop:
			ldi 0
			stm	a

// ldi 1
// nop
// bnz rd_loop
// nop
// nop

			ldi	io_outp
			stioa
			ldi	1
			stiod
wr_loop:

sys_wr_mem:
			ldm	a
			stmwa				// write ext. mem address
			ldm a
			stmwd				// write ext. mem data
			nop
			wait
			wait

			ldm	a
			ldi	1
			add
			stm	a

			ldm	a
			ldi	loop_cnt
			xor
			nop
			bnz	wr_loop
			nop
			nop


			ldi 0
			stm	a

			ldi	io_outp
			stioa
			ldi	0
			stiod
rd_loop:


sys_rd_mem:
			ldm	a
			stmra				// read ext. mem, mem_bsy comes one cycle later
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

			stm	b

ser0:
			ldi	io_status
			stioa
			ldi	ua_tdre
			ldiod
			and
			nop
			bz	ser0
			nop
			nop
			ldi	io_uart
			stioa
			ldm	b
			stiod


			ldm	a
			ldi	1
			add
			stm	a

			ldm	a
			ldi	loop_cnt
			xor
			nop
			bnz	rd_loop
			nop
			nop

			ldi	1
			nop
			bnz	loop
			nop
			nop
