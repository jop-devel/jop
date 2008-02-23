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
//	mem_wave.asm
//
//	test generate test pattern for memory interface (for waveform)
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
io_int_ena	=	0
io_status	=	4
io_uart		=	5


ua_rdrf		= 	2
ua_tdre		= 	1

max_words	= 16384	// words loaded from serial line to ram
//
//	first vars for start
//	keep order! these vars are accessed from Java progs.
//
	mp		?		// pointer to method struct
	cp		?		// pointer to constants
	heap	?		// start of heap

	jjp		?		// pointer to meth. table of Java JVM functions
	jjhp	?		// pointer to meth. table of Java JVM help functions

	moncnt	?		// counter for monitor

//
//	local vars
//
a			?
b			?
c			?
d			?
e			?
f			?

addr		?			// address used for bc load from flash
						// only in jvmflash.asm

//
//	JVM starts here.
//
//	new fetch does NOT reset address of ROM =>
//		it starts with pc+1
			nop			// this gets never executed
			nop			// for shure during reset (perhaps two times executed)

			ldi	128
			nop			// written in adr/read stage!
			stsp		// someting strange in stack.vhd A->B !!!

// back to back read

			ldi 15
			stmra				// start read ext. mem
			nop					// mem_bsy comes one cycle later
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

			ldi	16
			stmra
			nop
			wait
			wait
			ldmrd

// back to back write

			ldi	16
			stmwa				// write ext. mem address
			ldi	32
			stmwd				// write ext. mem data
			nop
			wait
			wait

			ldi	15
			stmwa				// write ext. mem address
			ldi	32
			stmwd				// write ext. mem data
			nop
			wait
			wait

// read back from addr 15

			ldi 15
			stmra				// start read ext. mem
			nop					// mem_bsy comes one cycle later
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

// pop former read vaules

			pop
			pop
			pop

			nop
			nop
			nop
			nop
			nop
			nop
			nop

// 'real' back to back rd rd wr wr rd rd

			ldi	128
			ldi	129
			ldi	130
			ldi	131
			ldi	132

			stmra
			nop
			wait
			wait
			ldmrd

			stmra
			nop
			wait
			wait
			ldmrd

			stmwa
			stmwd
			nop
			wait
			wait
			ldmrd

			stmwa
			stmwd
			nop
			wait
			wait
			ldmrd

			stmra
			nop
			wait
			wait
			ldmrd

			stmra
			nop
			wait
			wait
			ldmrd

			pop
