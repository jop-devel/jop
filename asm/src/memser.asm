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
//	memser.asm
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

			ldi	io_wd
			stmwa
			ldi	1
			stmwd
			wait
			wait
wr_loop:

sys_wr_mem:
			ldm	a
			stmwa				// write ext. mem address
			ldm a
			stmwd				// write ext. mem data
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

			ldi	io_wd
			stmwa
			ldi	0
			stmwd
			wait
			wait

rd_loop:


sys_rd_mem:
			ldm	a
			stmra
			wait
			wait
			ldmrd		 		// read ext. mem

			stm	b

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
			ldm	b
			stmwd
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
			bnz	rd_loop
			nop
			nop

			ldi	1
			nop
			bnz	loop
			nop
			nop
