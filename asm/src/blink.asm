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
//	blink.asm
//
//		Blinking WD LED to see if FPGA is running.
//		Also writes to serial line.
//
version		= 20091128

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

			ldi	128
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

			jmp blink
			nop
			nop
