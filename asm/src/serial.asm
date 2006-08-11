//
//	serial.asm
//
//		memory test, write memory content to serial line
//

//
//	io address
//
io_cnt		=	-128
io_wd		=	-125
io_status	=	-112
io_uart		=	-111

ua_rdrf		= 	2
ua_tdre		= 	1

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


			ldi 0
			stm	a

//
//	set WD bit
//
			ldi	io_wd
			stmwa
			ldi	1
			stmwd
			wait
			wait

loop:

ser0:
			ldi	io_status
			stmra
			ldi	ua_tdre
			wait
			wait
			ldmrd
			and
			nop
			bz	ser0			
			nop
			nop
			
			ldi	io_uart
			stmwa
			ldm	a
			stmwd
			wait
			wait


			ldm	a
			ldi	1
			add
			stm	a

			ldi	1
			nop
			bnz	loop
			nop
			nop
