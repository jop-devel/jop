//
//	tmp.asm
//
//		scratch file
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
			ldi	127
			nop			// written in adr/read stage!
			stsp


val = 2097152

loop:
			ldi 0
			stm	a

wr_loop:

			ldm	a
			stmwa				// write ext. mem address
			ldi 0
			stmwd				// write ext. mem data
			nop
			wait
			wait

			ldm	a
			stmra				// read ext. mem, mem_bsy comes one cycle later
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

			stm	b

			ldm	a
			stmwa				// write ext. mem address
			ldi -1
			stmwd				// write ext. mem data
			nop
			wait
			wait

			ldm	a
			stmra				// read ext. mem, mem_bsy comes one cycle later
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

			stm	b

			ldm a
			ldi 1
			add
			stm a

			ldi	1
			nop
			bnz	loop
			nop
			nop
