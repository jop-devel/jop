//
//	serout.asm
//
//		just write to serial line.
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
//	special byte codes
//		but starts with pc=0!!! (so init bc is not really necassary)
//
			nop
			nop
sys_init:
			ldi	128
			nop			// written in adr/read stage!
			stsp

			ldi	0
			stm	a

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
			ldm	a
			stiod

			ldm	a
			ldi	1
			add
			stm	a
			ldi	1
			nop
			bnz	ser0
			nop
			nop
