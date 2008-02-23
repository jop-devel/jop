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
//	memser2.asm
//
//		memory test
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

//			ldi	io_outp
//			stioa
//			ldi	1
//			stiod
wr_loop:

			ldm	a
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

//			ldi	io_outp
//			stioa
//			ldi	0
//			stiod
rd_loop:


sys_rd_mem:
			ldm	a
			stmra				// read ext. mem, mem_bsy comes one cycle later
			nop
			wait
			wait
			ldmrd		 		// read ext. mem

			stm	b

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


stop:
			ldi	1
			nop
			bnz	loop
			nop
			nop
