//
//	blink.asm
//
//		Blinking WD LED to see if FPGA is running.
//		Also writes to serial line.
//

//
//	io address
//
io_cnt		=	-128
io_wd		=	-125
io_status	=	-112
io_uart		=	-111

usb_status	=	-96
usb_data	=	-95

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

			nop			// this gets never executed
			nop			// for shure during reset (perhaps two times executed)

			ldi	127
			nop			// written in adr/read stage!
			stsp


//
//	blink loop (stops when serial line note read - hw hs)
//

loop_cnt	= 1000000

blink:
			ldi	io_wd
			stmwa
			ldi	0
			stmwd		
			wait
			wait

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
			ldi	48
			stmwd		
			wait
			wait

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
			stmwa
			ldi	31
			stmwd		
			wait
			wait

ser1:
			ldi	io_status
			stmra
			ldi	ua_tdre
			wait
			wait
			ldmrd
			and
			nop
			bz	ser1
			nop
			nop
			ldi	io_uart
			stmwa
			ldi	49
			stmwd		
			wait
			wait

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
