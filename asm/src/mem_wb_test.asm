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
//	mem_wb_test.asm
//
//	test for simulation
//
//
//	'special' constant for a version number
//	gets written in RAM at position 64
//	update it when changing .asm, .inc or .vhdl files
//
version		= 20050220


			nop
			nop
			ldi	128
			nop			// written in adr/read stage!
			stsp		// someting strange in stack.vhd A->B !!!
			nop
			ldi	-1
			nop


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop


			ldi	3
			stmra
			wait
			wait
			ldmrd
ldi 1
ldi 2
ldi 3


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop

// test mem interface
//
			ldi 15

			// this sequence takes 6 cycles with ram_cnt=3
			stmra				// start read ext. mem
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

			ldi 7				// read addr.for back to back wr/rd
			ldi	32				// write data
			ldi	16				// write address

			// this sequence takes 6 cycles with ram_cnt=3
			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait

			stmra				// start read ext. mem
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

			pop
			pop



			nop
			nop
			nop

// test wishbone interface
// we use negativ addresses to access the wishbone devices
//

			ldi	-2
			stmra
			wait
			wait
			ldmrd
			ldi	-1
			stmra
			wait
			wait
			ldmrd


			nop
			nop

			pop
			pop


			ldi	16
			ldi	-1
			stmwa
			stmwd
			wait
			wait

			ldi	-1
			stmra
			wait
			wait
			ldmrd

			nop
			nop

			pop

			nop
			nop
			nop

			// back to back write
			ldi	33				// write data
			ldi	17				// write address

			ldi	32				// write data
			ldi	16				// write address

			// this sequence takes 6 cycles with ram_cnt=3
			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait

			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
