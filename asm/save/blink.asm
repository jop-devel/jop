//
//	blink.asm
//
//		Blinking WD LED to see if FPGA is running.
//		Also writes to serial line.
//		read Flash ID
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
io_inp		=	0
io_outp		=	0
io_status	=	1
io_uart		=	2
io_ecp		=	3
io_wd		=	7
io_cnt		=	10
io_ms		=	11


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


			ldi	127
			nop			// written in adr/read stage!
			stsp

//	/* Read ID from Flash
//	Native.wrMem(0xaa, 0x80555);
//	Native.wrMem(0x55, 0x802aa);
//	Native.wrMem(0x90, 0x80555);
//	Dbg.hexVal(Native.rdMem(0x80000));
//	Native.wrMem(0xaa, 0x80555);
//	Native.wrMem(0x55, 0x802aa);
//	Native.wrMem(0x90, 0x80555);
//	Dbg.hexVal(Native.rdMem(0x80001));
//	*/
//
//	read flash ID and write to serial line
//
//			ldi	525653	// 0x80555
//			stmwa
//			ldi	170		// 0xaa
//			stmwd
//			nop
//			wait
//			wait
//			ldi	524970	// 0x802aa
//			stmwa
//			ldi	85		// 0x55
//			stmwd
//			nop
//			wait
//			wait
//			ldi	525653	// 0x80555
//			stmwa
//			ldi	144		// 0x90
//			stmwd
//			nop
//			wait
//			wait
//			ldi	524288	// 0x80000
//			stmra
//			nop
//			wait
//			wait
//			ldmrd
//fl0:
//			ldi	io_status
//			stioa
//			ldi	ua_tdre
//			ldiod
//			and
//			nop
//			bz	fl0
//			nop
//			nop
//			ldi	io_uart
//			stioa
//			stiod
//
//
//
//			ldi	525653	// 0x80555
//			stmwa
//			ldi	170		// 0xaa
//			stmwd
//			nop
//			wait
//			wait
//			ldi	524970	// 0x802aa
//			stmwa
//			ldi	85		// 0x55
//			stmwd
//			nop
//			wait
//			wait
//			ldi	525653	// 0x80555
//			stmwa
//			ldi	144		// 0x90
//			stmwd
//			nop
//			wait
//			wait
//			ldi	524281	// 0x80001
//			stmra
//			nop
//			wait
//			wait
//			ldmrd
//fl1:
//			ldi	io_status
//			stioa
//			ldi	ua_tdre
//			ldiod
//			and
//			nop
//			bz	fl1
//			nop
//			nop
//			ldi	io_uart
//			stioa
//			stiod
//

//
//	blink loop (stops when serial line note read - hw hs)
//

loop_cnt	= 1000000

blink:
			ldi	io_wd
			stioa
			ldi	0
			stiod

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
			ldi	48
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

ser1:
			ldi	io_status
			stioa
			ldi	ua_tdre
			ldiod
			and
			nop
			bz	ser1
			nop
			nop
			ldi	io_uart
			stioa
			ldi	49
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
