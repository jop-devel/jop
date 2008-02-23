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
			ldi	128
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
