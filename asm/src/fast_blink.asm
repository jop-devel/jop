//
//	fast_blink.asm
//
//		Blinking WD LED for the simulation.
//
//
//	io register
//
io_addr		=	0
io_data		=	1

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

// test max neg. value
         	ldi	-2147483648		//  0x80000000
//
//	blink loop 
//

loop_cnt	= 10

blink:
			ldi	io_wd
			stioa
			ldi	0
			stiod

			ldi	loop_cnt
bl0:		ldi	1
			sub
			dup
			nop
			bnz	bl0
			nop
			nop
			pop

			ldi	io_wd
			stioa
			ldi	31
			stiod

			ldi	loop_cnt
bl1:		ldi	1
			sub
			dup
			nop
			bnz	bl1
			nop
			nop
			pop

			ldi	1
			nop
			bnz	blink
			nop
			nop
